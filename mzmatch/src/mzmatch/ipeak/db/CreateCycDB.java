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
import java.util.regex.*;

// libraries
import cmdline.*;

// peakml
import peakml.chemistry.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.util.*;





public class CreateCycDB
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "CreateCycDB";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Converts a Cyc metabolite database to the moleculedb format.")
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Filename of the Cyc database.")
		public String input = "f:/compounds.dat";
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
			
			// set up the regular expression
			Pattern pa_tag = Pattern.compile("</?\\w+\\s*[^>]*>");
			Pattern pa_atom = Pattern.compile("([A-Z][A-Z]*)\\s*([0-9]+)");
			
			// process the data
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			
			String id="", mass="", name="", formula="", smiles="";
			Vector<String> types = new Vector<String>();
			Vector<String> synonyms = new Vector<String>();
			
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			while ((line = in.readLine()) != null)
			{
				// loose the comments
				if (line.startsWith("#"))
					continue;
				
				// are we finished with an entry ?
				if (line.startsWith("//"))
				{
					if (formula.contains("R"))
					{
						System.err.println("r-group: " + id + "(" + formula + ")");
					}
					else if (formula.length() == 0)
					{
						System.err.println("no formula: " + id);
					}
					else
					{
						try
						{
							MolecularFormula f = new MolecularFormula(formula);
							
							if (mass.length() != 0)
							{
								double mw = Double.parseDouble(mass);
								if (Math.abs(mw - f.getMass(Mass.MOLECULAR)) > 0.1)
									System.err.println(
											"err in mass: "
											+ id +  ", " + f + "(" + formula + ")" + " - "
											+ mw + "-" + f.getMass(Mass.MOLECULAR));
							}
							
							Molecule molecule = new Molecule(id, name, f);
							molecule.setSmiles(smiles);
							molecule.setSynonyms(synonyms);
							molecule.setDescription(types.toString());
							molecules.put(id, molecule);
						}
						catch (Exception e)
						{
							System.err.println(id + ", " + name + ", " + formula);
							e.printStackTrace();
						}
					}
					
					id = mass = name = formula = smiles = "";
					types.clear();
					synonyms.clear();
				}
				else
				{
					if (line.startsWith("/") || line.startsWith("COMMENT"))
						continue;
					
					// break up
					String tokens[] = line.trim().split("\\s\\-\\s");
					if (tokens.length != 2)
					{
						System.err.println(line);
						continue;
					}

					if (tokens[0].equals("UNIQUE-ID"))
						id = tokens[1];
					else if (tokens[0].equals("COMMON-NAME"))
					{
						Matcher ma_tag = pa_tag.matcher(tokens[1]);
						name = ma_tag.replaceAll("");
					}
					else if (tokens[0].equals("MOLECULAR-WEIGHT"))
						mass = tokens[1];
					else if (tokens[0].equals("SMILES"))
						smiles = tokens[1];
					else if (tokens[0].equals("TYPES"))
						types.add(tokens[1]);
					else if (tokens[0].equals("SYNONYMS"))
					{
						Matcher ma_tag = pa_tag.matcher(tokens[1]);
						synonyms.add(ma_tag.replaceAll(""));
					}
					else if (tokens[0].equals("CHEMICAL-FORMULA"))
					{
						Matcher ma_atom = pa_atom.matcher(tokens[1]);
						while (ma_atom.find())
						{
//							System.out.print(tokens[1] + ", " + ma_atom.group() + ":");
//							for (int i=1; i<=ma_atom.groupCount(); ++i)
//								System.out.print(" '" + ma_atom.group(i) + "'");
//							System.out.println();
							
							String atom = ma_atom.group(1);
							String count = ma_atom.group(2);
							
							if (atom.equals("COBALT"))
								atom = "Co";
							else
							{
								char array[] = atom.toCharArray();
								for (int i=1; i<array.length; ++i)
									array[i] = Character.toLowerCase(array[i]);
								atom = new String(array);
							}
							
							formula = formula + atom + count;
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
