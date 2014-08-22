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





public class CreateKEGGDB
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "CreateKEGGDB";
	@OptionsClass(name=application, version=version, description=
		"Converts a KEGG database to the moleculedb format.")
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"KEGG compounds data file in plain text format. Can be downloaded from: ftp://ftp.genome.jp/pub/kegg/ligand/compound/")
		public String input = null;
		@Option(name="inchi", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"InCHI strings files. Can be downloaded from: ftp://ftp.genome.jp/pub/kegg/ligand/compound/")
		public String inchi = null;
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
		final int STATE_NONE			= 0;
		final int STATE_NAME			= 1;
		
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
			InputStream input = new FileInputStream(options.input);
			InputStream inchi = new FileInputStream(options.inchi);
			
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			
			// create the container
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			
			// parse the bget data
			int state = STATE_NONE;
			
			String id = null;
			String mass = null;
			String formula = null;
			Vector<String> synonyms = new Vector<String>();
			
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			while ((line = in.readLine()) != null)
			{
				if (line.startsWith("///")) // end of entry
				{
					try {
						double m = Double.parseDouble(mass);
						MolecularFormula f = new MolecularFormula(formula);
						
						if (Math.abs(m - f.getMass(Mass.MONOISOTOPIC)) < 0.1)
						{
							Molecule molecule = new Molecule(id, synonyms.firstElement(), f);
							molecule.setSynonyms(synonyms.subList(1, synonyms.size()));
							molecules.put(id, molecule);
						}
						else
							System.err.println(formula + " - " + m + " != " + f.getMass(Mass.MONOISOTOPIC));
					} catch (Exception e) {
						//System.err.println(e.getMessage());
					}
					state = STATE_NONE;
					synonyms.clear();
				}
				else
				{
					String tokens[] = ("E" + line).split("\\s{2,}");
					if (tokens.length < 2)
						continue;
					String lbl = tokens[0];
					String val = tokens[1];
					if (val.endsWith(";"))
						val = val.substring(0, val.length()-1);
					
					if (lbl.equals("EENTRY"))
						id = val;
					else if (lbl.equals("EMASS"))
						mass = val;
					else if (lbl.equals("EFORMULA"))
						formula = val;
					else
					{
						if (lbl.equals("ENAME") || (state==STATE_NAME && lbl.equals("E")))
						{
							state = STATE_NAME;
							synonyms.add(val);
						}
						else
							state = STATE_NONE;
					}
				}
			}
			
			// parse the inchi data
			in = new BufferedReader(new InputStreamReader(inchi));
			while ((line = in.readLine()) != null)
			{
				String tokens[] = line.split("\t");
				if (tokens.length != 2)
					continue;
				if (molecules.containsKey(tokens[0]))
					molecules.get(tokens[0]).setInChi(tokens[1]);
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
