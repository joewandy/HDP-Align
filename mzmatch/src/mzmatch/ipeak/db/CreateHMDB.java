/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package mzmatch.ipeak.db;


// java
import java.io.*;
import java.util.*;

// libraries
import cmdline.*;

// peakml
import peakml.chemistry.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.util.*;





public class CreateHMDB
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "CreateHMDB";
	@OptionsClass(name=application, version=version, description=
		"Converts a human metabolome database database to the moleculedb format.")
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Filename of the HMDB database.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Filename of the moleculedb file.")
		public String output = null;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	public static void main(String args[])
	{
		final int STATE_NONE				= 0;
		final int STATE_ID					= 1;
		final int STATE_CARD				= 2;
		final int STATE_FORMULA				= 3;
		final int STATE_INCHI				= 4;
		final int STATE_NAME				= 5;
		final int STATE_SYNONYMS			= 6;
		final int STATE_TAXONOMY_SPECIES	= 7;
		
		try
		{
			Tool.init();
			
			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			
			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help)
			{
				Tool.printHeader(System.out, application, version);
				
				cmdline.printUsage(System.out, "");
				return;
			}
			
			if (options.verbose)
			{
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}
			
			// check the command-line parameters
			{
			}
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			
			// create the container
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			
			// parse the data
			int state = STATE_NONE;
			
			String id = null;
			String name = null;
			String formula = null;
			String inchi = null;
			int anion=0, cation=0;
			Vector<String> synonyms = new Vector<String>();
			
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			while ((line = in.readLine()) != null)
			{
				// loose the comments
				if (line.startsWith("#"))
				{
					if (line.contains("END_METABOCARD"))
					{
						try {
							Molecule molecule = new Molecule(id, name, formula);
							if (anion==0 && cation!=0)
								molecule.getFormula().getSubFormulas().get(0).setCharge(1);
							if (anion!=0 && cation==0)
								molecule.getFormula().getSubFormulas().get(0).setCharge(-1);
							molecule.setInChi(inchi);
							molecule.setSynonyms(synonyms);
//							if (cation==0 && anion!=0)
							molecules.put(id, molecule);
						} catch (Exception e) {
							System.out.println(e.getMessage());
						}
						
						id =  name = formula = inchi = null;
						anion = cation = 0;
						synonyms.clear();
					}
					else if (line.contains("BEGIN_METABOCARD"))
						state = STATE_CARD;
					else if (line.equals("# hmdb_id:"))
						state = STATE_ID;
					else if (line.equals("# name:"))
						state = STATE_NAME;
					else if (line.equals("# chemical_formula:"))
						state = STATE_FORMULA;
					else if (line.equals("# inchi_identifier:"))
						state = STATE_INCHI;
					else if (line.equals("# synonyms:"))
						state = STATE_SYNONYMS;
					else if (line.equals("# taxonomy_species:"))
						state = STATE_TAXONOMY_SPECIES;
				}
				else if (line.trim().length() == 0)
				{
					state = STATE_CARD;
				}
				else
				{
					if (state == STATE_ID)
						id = line;
					else if (state == STATE_NAME)
						name = line;
					else if (state == STATE_FORMULA)
						formula = line;
					else if (state == STATE_INCHI)
						inchi = line;
					else if (state == STATE_SYNONYMS)
					{
						for (String str : line.split("; "))
							synonyms.add(str);
					}
					else if (state == STATE_TAXONOMY_SPECIES)
					{
						if (line.equals("cation"))
						{
							System.out.println(id + " - " + "cation");
							cation++;
						}
						else if (line.equals("anion"))
						{
							System.out.println(id + " - " + "anion");
							anion++;
						}
					}
				}
			}
			
			// write the data
			MoleculeIO.writeXml(molecules, output);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
