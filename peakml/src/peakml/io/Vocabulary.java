/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.io;


// java
import java.io.*;
import java.util.*;

// libraries
import domsax.*;





/**
 * This class is a simple implementation, which keeps track of a controlled
 * vocabulary. Its most obvious use is the MS-vocabulary defined by the
 * Proteomics Standards Initiative, which goes along with the mzml
 * standard. In a controlled vocabulary terms are related, which have a
 * fixed ID and a fixed meaning. An example of this is MS:1000484 'orbitrap'.
 * This means that when a controlled vocabulary with id MS:1000484 is encountered,
 * the meaning is orbitrap. This class can also be used for other controlled
 * vocabularies.
 * <p/>
 * Terms are stored in a hashmap which utilizes the term.id as the key in order
 * to improve the speed of retrieving terms from the vocabulary.
 * <p/>
 * This class is slated for disposal in favor of the sesame library.
 */
public class Vocabulary implements Iterable<Vocabulary.Term>
{
	/**
	 * This class is a simple implementation, which keeps track of a single term
	 * in a controlled vocabulary.
	 */
	public static class Term
	{
		// constructor(s)
		/**
		 * Constructs a new term with the given parameters.
		 * 
		 * @param id			The unique id for this term.
		 * @param name			The name of the term.
		 * @param definition	The definition (free-text description) of the term.
		 * @param is_a			Vector containing the id's of all the parents of this term.
		 * @param alt_id		Vector containing alternative id's for this term.
		 * @param is_obsolete	Boolean indicating whether this term is obsolete.
		 */
		public Term(String id, String name, String definition, Vector<String> is_a, Vector<String> alt_id, boolean is_obsolete)
		{
			this.id = id;
			this.name = name;
			this.definition = definition;
			this.is_a = is_a;
			this.alt_id = alt_id;
			this.is_obsolete = is_obsolete;
		}
		
		// access
		/** @see Term#toCVParam(String) */
		public XmlAttribute[] toCVParam()
		{
			return toCVParam(null);
		}
		
		/**
		 * Creates the xml-attributes needed for a cvParam element in the mzML standard.
		 * Basically this is a convenience method and up for transfer to mzML writer.
		 * 
		 * @param value		An optional value with the value for the value attribute.
		 * @return			An array containing all the xml-attributes.
		 */
		public XmlAttribute[] toCVParam(String value)
		{
			return new XmlAttribute[] {
				new XmlAttribute("cvRef",		id.substring(0, id.indexOf(':'))),
				new XmlAttribute("accession",	id),
				new XmlAttribute("name",		name),
				new XmlAttribute("value",		(value!=null ? value : ""))
			};
		}
		
		// Object overrides
		@Override
		public String toString()
		{
			return "Term[" + id + ", " + name + ", " + definition + "]";
		}
		
		// data
		/** The unique id of the term. */
		public final String id;
		/** The name describing the term. */
		public final String name;
		/** A free-text description of the term. */
		public final String definition;
		/** Vector with all the parents of this term. Multiple inheritance is allowed. */
		public final Vector<String> is_a;
		/** Vector with all the alternative id's for this term. */
		public final Vector<String> alt_id;
		/** Indicates whether this term is obsolete. */
		public final boolean is_obsolete;
	}
	
	
	// constructor(s)
	/**
	 * Basic constructor for a vocabulary, which initializes an empty set.
	 */
	public Vocabulary()
	{
	}
	
	/**
	 * Basic constructor, which reads the contents of the given input-stream and stores
	 * them internally.
	 * 
	 * @param input		The input-stream to read from.
	 * @throws IOException
	 * 					Thrown when something goes wrong with the input-stream.
	 */
	public Vocabulary(InputStream input) throws IOException
	{
		parseOBO(input);
	}
	
	
	// header access
	/**
	 * Returns the default namespace of this vocabulary.
	 * 
	 * @return			The default namespace.
	 */
	public String getDefaultNamespace()
	{
		return default_namespace;
	}
	
	
	// term access
	/**
	 * Returns the term with the given id. When the term-id is not available
	 * null is returned.
	 * 
	 * @param id		The id of the term to be retrieved.
	 * @return			The retrieved term or null when not available.
	 */
	public Term getTerm(String id)
	{
		return terms.get(id);
	}
	
	/**
	 * Adds the given term to the hashmap, with the {@link Term#id} field used as a key. When
	 * alternative id's ({@link Term#alt_id}) are available, extra entries are made.
	 * 
	 * @param term		The term to be added to the vocabulary.
	 */
	public void addTerm(Term term)
	{
		terms.put(term.id, term);
		for (String altid : term.alt_id)
			terms.put(altid, term);
	}
	
	/**
	 * Determines whether the given term is a child of the given parent. To do this, the
	 * function recursively iterates through all of the is_a relations.
	 * 
	 * @param term			The term to evaluate.
	 * @param parent		The parent for the term.
	 * @return				True when the term is a child, false otherwise.
	 */
	public boolean isTermChildOf(Term term, Term parent)
	{
		if (term==null || parent==null)
			return false;
		for (String parentid : term.is_a)
		{
			if (parent.id.equals(parentid))
				return true;
			else
				return isTermChildOf(term, getTerm(parentid));
		}
		return false;
	}
	
	
	// Iterable overrides
	public Iterator<Term> iterator()
	{
		return terms.values().iterator();
	}
	
	
	// parser access
	/**
	 * Simple parser for OWL-files issued by the Open Biomedial Ontologies foundry. It is
	 * by no means a complete parser. The contents of the file are stored internally in
	 * this class.
	 * 
	 * @param input		The input-stream to read from.
	 */
	public void parseOBO(InputStream input) throws IOException
	{
		terms.clear();
		
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		
		// read the header
		String line;
		while ((line = in.readLine()) != null)
		{
			line = line.trim();
			if (line.length() == 0)
				break;
			
			String tokens[] = split(line);
			if (tokens[0].equals("format-version"))
				format_version = tokens[1];
			else if (tokens[0].equals("date"))
				date = tokens[1];
			else if (tokens[0].equals("saved-by"))
				saved_by = tokens[1];
			else if (tokens[0].equals("auto-generated-by"))
				auto_generated_by = tokens[1];
			else if (tokens[0].equals("default-namespace"))
				default_namespace = tokens[1];
			else if (tokens[0].equals("remark"))
				;
			else if (tokens[0].equals("import"))
				;
			else
				System.err.println("[WARNING]: unexpected token '" + tokens[0] + "' in header");
		}
		
		// read the terms
		while ((line = in.readLine()) != null)
		{
			line = line.trim();
			if (line.equals("[Term]"))
			{
				String id = null;								// 
				String name = null;								// 
				String definition = null;						// 
				boolean is_obsolete = false;					// 
				Vector<String> is_a = new Vector<String>();		// multiple inheritance is allowed
				Vector<String> alt_id = new Vector<String>();	// 
				
				while ((line = in.readLine().trim()) != null)
				{
					if (line.length() == 0)
						break;
					String tokens[] = split(line);
					
					// id
					if (tokens[0].equals("id"))
						id = tokens[1];
					else if (tokens[0].equals("alt_id"))
						alt_id.add(tokens[1]);
					// name and definition
					else if (tokens[0].equals("name"))
						name = tokens[1];
					else if (tokens[0].equals("def"))
						definition = tokens[1];
					else if (tokens[0].equals("comment"))
						;
					else if (tokens[0].equals("is_obsolete") && tokens[1].equals("true"))
						is_obsolete = true;
					else if (tokens[0].equals("replaced_by"))
						;
					// synonyms
					else if (tokens[0].equals("synonym"))
						;
					else if (tokens[0].equals("exact_synonym"))
						;
					else if (tokens[0].equals("narrow_synonym"))
						;
					else if (tokens[0].equals("related_synonym"))
						;
					// relationships
					else if (tokens[0].equals("xref"))
						;
					else if (tokens[0].endsWith("xref_analog"))
						;
					else if (tokens[0].equals("is_a"))
						is_a.add(removeComment(tokens[1]));
					else if (tokens[0].equals("relationship"))
						;
					else
						System.err.println("[WARNING]: unexpected token '" + tokens[0] + "' in term.");
				}
				
				terms.put(id, new Term(id, name, definition, is_a, alt_id, is_obsolete));
			}
		}
	}
	
	public static Vocabulary getVocabulary(String filename) throws IOException
	{
		File f = new File(filename);
		Vocabulary vocabulary = null;
		if (f.exists())
			(vocabulary=new Vocabulary()).parseOBO(new FileInputStream(filename));
		else
			(vocabulary=new Vocabulary()).parseOBO(ClassLoader.getSystemClassLoader().getResourceAsStream(filename));
		return vocabulary;
	}
	
	
	// data
	protected String format_version;
	protected String date;
	protected String saved_by;
	protected String auto_generated_by;
	protected String default_namespace;
	
	protected HashMap<String,Term> terms = new HashMap<String,Term>();
	
	
	// helper functions
	private static String[] split(String line)
	{
		String tokens[] = new String[2];
		int pos = line.indexOf(':');
		if (pos == -1)
			return tokens;
		tokens[0] = line.substring(0, pos).trim();
		tokens[1] = line.substring(pos+1).trim();
		return tokens;
	}
	
	private static String removeComment(String line)
	{
		int pos = line.indexOf('!');
		if (pos == -1)
			return line;
		return line.substring(0, pos).trim();
	}
}
