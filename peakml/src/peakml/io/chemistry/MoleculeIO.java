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



package peakml.io.chemistry;


// java
import java.io.*;
import java.util.*;

import org.w3c.dom.*;

import au.com.bytecode.opencsv.CSVWriter;

// libraries
import domsax.*;

// peakml
import peakml.chemistry.*;
import peakml.util.Pair;





/**
 * 
 */
public class MoleculeIO
{
	/**
	 * 
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static HashMap<String,Molecule> parseTxt(InputStream input) throws IOException
	{
		HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
		
		int i = 0;
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(input));
		while ((line = in.readLine()) != null)
		{
			String tokens[] = line.trim().split("\t");
			
			String id = String.format("%d", i);
			try {
				molecules.put(id, new Molecule(id, tokens[0], Double.parseDouble(tokens[1])));
			}
			catch (Exception e) {
				try {
					molecules.put(id, new Molecule(id, tokens[0], tokens[1]));
				} catch (Exception es) { es.printStackTrace(); }
			}
			i++;
		}
		
		return molecules;
	}
	
	public interface KeyValueContainer<K,V> {
		public V put(K key, V value);
	}
	
	public static class KeyValueContainerMapAdapter<K,V> implements KeyValueContainer<K,V> {
		final Map<K,V> map;
		public KeyValueContainerMapAdapter(Map<K,V> map) {
			this.map = map;
		}
		public V put(K key, V value) {
			return map.put(key,  value);
		}
	}
	
	public static class KeyValueContainerListAdapter<K,V> implements KeyValueContainer<K,V> {
		final List<Pair<K, V>> list;
		public KeyValueContainerListAdapter(List<Pair<K, V>> list) {
			this.list = list;
		}
		public V put(K key, V value) {
			list.add(new Pair<K, V>(key,  value));
			return value;
		}
	}
	
	public static void parseXml(InputStream input, final KeyValueContainer<String,Molecule> map) throws IOException, XmlParserException
	{
		//final HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
		
		XmlParser parser = new XmlParser();
		parser.addListener(
				new XmlParserListener() {
					public void onDocument(Document document, String xpath) throws XmlParserException
					{
						Node parent = document.getChildNodes().item(0);
						
						Vector<String> synonyms = new Vector<String>();
						String id=null, name=null, formula=null, smiles=null,
							inchi=null, description=null, classdescription=null,
							retentiontime=null, mass=null, polarity=null;

						NodeList nodes = parent.getChildNodes();
						for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
						{
							Node node = nodes.item(nodeid);
							if (node.getNodeType() != Node.ELEMENT_NODE)
								continue;
							
							Element element = (Element) node;
							if (element.getTagName().equals("id"))
								id = element.getTextContent();
							else if (element.getTagName().equals("name"))
								name = element.getTextContent();
							else if (element.getTagName().equals("formula"))
								formula = element.getTextContent();
							else if (element.getTagName().equals("inchi"))
								inchi = element.getTextContent();
							else if (element.getTagName().equals("smiles"))
								smiles = element.getTextContent();
							else if (element.getTagName().equals("synonyms"))
								synonyms = parseSynonyms(element);
							else if (element.getTagName().equals("retentiontime"))
								retentiontime = element.getTextContent();
							else if (element.getTagName().equals("description"))
								description = element.getTextContent();
							else if (element.getTagName().equals("class"))
								classdescription = element.getTextContent();
							else if (element.getTagName().equals("monoisotopicmass"))
								mass = element.getTextContent();
							else if (element.getTagName().equals("polarity"))
								polarity = element.getTextContent();
						}

						Molecule molecule = new Molecule(id, name, formula);
						molecule.setInChi(inchi);
						molecule.setSmiles(smiles);
						molecule.setSynonyms(synonyms);
						molecule.setDescription(description);
						molecule.setClassDescription(classdescription);
						if (retentiontime != null)
							molecule.setRetentionTime(Double.parseDouble(retentiontime));
						if (mass != null && mass.length() > 0)
							molecule.setMass(Double.parseDouble(mass));
						if (polarity != null)
							molecule.setPolarity(Polarity.fromSymbol(polarity));
						map.put(id, molecule);
					}
					
					protected Vector<String> parseSynonyms(Node parent) throws XmlParserException
					{
						Vector<String> synonyms = new Vector<String>();
						
						NodeList nodes = parent.getChildNodes();
						for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
						{
							Node node = nodes.item(nodeid);
							if (node.getNodeType() != Node.ELEMENT_NODE)
								continue;
							
							Element element = (Element) node;
							if (element.getTagName().equals("synonym"))
								synonyms.add(element.getTextContent());
						}
						
						return synonyms;
					}
				},
				"/compounds/compound"
			);
		parser.parse(input);
	}
	

	/**
	 * 
	 * @param input
	 * @return
	 * @throws IOException
	 * @throws XmlParserException
	 */
	public static HashMap<String,Molecule> parseXml(InputStream input) throws IOException, XmlParserException
	{
		final HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
		final KeyValueContainer<String,Molecule> adapter = new KeyValueContainerMapAdapter<String, Molecule>(molecules);
		parseXml(input, adapter);
		return molecules;
	}
	
	/**
	 * 
	 * @param molecules
	 * @param out
	 * @throws IOException
	 */
	public static void writeXml(HashMap<String,Molecule> molecules, OutputStream out) throws IOException
	{
		XmlWriter xml = new XmlWriter(out);
		xml.writeTag("compounds", XmlWriter.Tag.OPEN);
		
		for (Molecule molecule : molecules.values())
		{
			xml.writeTag("compound", XmlWriter.Tag.OPEN);
			xml.writeElement("id", molecule.getDatabaseID());
			xml.writeElement("name", molecule.getName());
			xml.writeElement("formula", molecule.getFormula().toString());
			xml.writeElement("inchi", molecule.getInChi());
			xml.writeElement("smiles", molecule.getSmiles());
			xml.writeElement("description", molecule.getDescription());
			xml.writeElement("monoisotopicmass", String.format("%.5f", molecule.getMass(Mass.MONOISOTOPIC, true)));
			if (molecule.getRetentionTime() != -1)
				xml.writeElement("retentiontime", Double.toString(molecule.getRetentionTime()));
			
			xml.writeTag("synonyms", XmlWriter.Tag.OPEN);
			for (String synonym : molecule.getSynonyms())
				xml.writeElement("synonym", synonym);
			xml.writeTag("synonyms", XmlWriter.Tag.CLOSE);
			
//			xml.writeTag("pathways", XmlWriter.Tag.OPEN);
//			for (CodeMap cm : compoundmaps)
//			{
//				if (!cm.compoundcode.equals(compoundcode))
//					continue;
//				
//				Pathway pathway = kegg.retrievePathway(cm.mapcode);
//				if (pathway == null)
//				{
//					System.err.println("[WARNING]: pathway failed: " + cm.mapcode);
//					continue;
//				}
//				
//				xml.writeTag("pathway", XmlWriter.Tag.OPEN);
//				xml.writeElement("id", pathway.getDatabaseID());
//				xml.writeElement("name", pathway.getName());
//				xml.writeTag("pathway", XmlWriter.Tag.CLOSE);
//			}
//			xml.writeTag("pathways", XmlWriter.Tag.CLOSE);
			
			xml.writeTag("compound", XmlWriter.Tag.CLOSE);
			xml.flush();
		}
		
		xml.writeTag("compounds", XmlWriter.Tag.CLOSE);
		xml.flush();
	}

	// same as writeXml, but randomize the order of molecules first before writing them out
	public static void writeXmlRandomized(HashMap<String,Molecule> molecules, OutputStream out) throws IOException
	{
		XmlWriter xml = new XmlWriter(out);
		xml.writeTag("compounds", XmlWriter.Tag.OPEN);
		
		List<String> keys = new ArrayList<String>(molecules.keySet());
		Collections.shuffle(keys);
		for (String key : keys) {
			
			Molecule molecule = molecules.get(key);
			
			xml.writeTag("compound", XmlWriter.Tag.OPEN);
			xml.writeElement("id", molecule.getDatabaseID());
			xml.writeElement("name", molecule.getName());
			xml.writeElement("formula", molecule.getFormula().toString());
			xml.writeElement("inchi", molecule.getInChi());
			xml.writeElement("smiles", molecule.getSmiles());
			xml.writeElement("description", molecule.getDescription());
			xml.writeElement("monoisotopicmass", String.format("%.5f", molecule.getMass(Mass.MONOISOTOPIC, true)));
			if (molecule.getRetentionTime() != -1)
				xml.writeElement("retentiontime", Double.toString(molecule.getRetentionTime()));
			
			xml.writeTag("synonyms", XmlWriter.Tag.OPEN);
			for (String synonym : molecule.getSynonyms())
				xml.writeElement("synonym", synonym);
			xml.writeTag("synonyms", XmlWriter.Tag.CLOSE);
			
//			xml.writeTag("pathways", XmlWriter.Tag.OPEN);
//			for (CodeMap cm : compoundmaps)
//			{
//				if (!cm.compoundcode.equals(compoundcode))
//					continue;
//				
//				Pathway pathway = kegg.retrievePathway(cm.mapcode);
//				if (pathway == null)
//				{
//					System.err.println("[WARNING]: pathway failed: " + cm.mapcode);
//					continue;
//				}
//				
//				xml.writeTag("pathway", XmlWriter.Tag.OPEN);
//				xml.writeElement("id", pathway.getDatabaseID());
//				xml.writeElement("name", pathway.getName());
//				xml.writeTag("pathway", XmlWriter.Tag.CLOSE);
//			}
//			xml.writeTag("pathways", XmlWriter.Tag.CLOSE);
			
			xml.writeTag("compound", XmlWriter.Tag.CLOSE);
			xml.flush();
			
		}
		
		xml.writeTag("compounds", XmlWriter.Tag.CLOSE);
		xml.flush();
		

	}
	
}
