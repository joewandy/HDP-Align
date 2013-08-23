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



package org.ronandaly.metasign;


// java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

import au.com.bytecode.opencsv.CSVWriter;

import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;





/* HISTORY:
 * 1.0.1	- fixed a bug in the upper limitation of the retention time.
 *			- complete documentation for the command-line options.
 *			- changed the option '-molecules' to '-databases'.
 */
public class ExtractIsotopePatterns
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.1";
	final static String application = "Identify";
	@OptionsClass(name=application, version=version,  author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Identifies the contents of the given PeakML file with the given databases. The databases are " +
		"expected to have the format of the example below (a standard file format for this would be " +
		"preferable) and contain all of the compounds to be tested for. Within the tool-chain, in the " +
		"package 'mzmatch.ipeak.db', several tools are provided for converting downloadable files (usually " +
		"on an ftp-server) from the major metabolite databases. Before identifying your files, please " +
		"make sure you have the most recent version." +
		"\n\n" +
		"Matching is performed only on mass, which is taken from the topmost structure. In other words, if " +
		"the PeakML file contains a list of mass chromatograms the mass of each individual mass chromatogram " +
		"is matched to the database. However, if the PeakML file contains a list of matched mass " +
		"chromatograms, the mean mass of the matched mass chromatograms is used for identification." +
		"\n\n" +
		"When a peak is positively identified the annotation '" + Annotation.identification + "' is " +
		"extended with the unique database ID corresponding to the match. This will keep the clutter " +
		"in the PeakML file to a minimum and the information associated to the tag up-to-date when " +
		"the database is updated with a new version. Additionally, the tag provides a convenient way" +
		"of removing false identifications from the PeakML file from a UI environment.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM identify the data in the PeakML file\n" +
		"%JAVA% mzmatch.ipeak.util.Identify -v -ppm 3 -i data.peakml -o data_identified.peakml -databases \"hmdb.xml,lipidmaps.xml\"\n" +
		"\n" +
		"----------\n" +
		"\n" +
		"Database xml example:\n" +
		"&lt;?xml version=\"1.0\" encoding=\"UTF-8\"?&gt;\n" +
		"\n" +
		"&lt;compounds&gt;\n" +
		"\t&lt;compound&gt;\n" +
		"\t\t&lt;id&gt;HMDB00162&lt;/id&gt;\n" +
		"\t\t&lt;name&gt;L-Proline&lt;/name&gt;\n" +
		"\t\t&lt;formula&gt;[M1];[C5H9NO2]n&lt;/formula&gt;\n" +
		"\t\t&lt;inchi&gt;InChI=1/C5H9NO2/c7-5(8)4-2-1-3-6-4/h4,6H,1-3H2,(H,7,8)/t4-/m0/s1&lt;/inchi&gt;\n" +
		"\t\t&lt;smiles /&gt;\n" +
		"\t\t&lt;description /&gt;\n" +
		"\t\t&lt;synonyms&gt;\n" +
		"\t\t\t&lt;synonym&gt;(-)-(S)-Proline&lt;/synonym&gt;\n" +
		"\t\t\t&lt;synonym&gt;(-)-2-Pyrrolidinecarboxylate&lt;/synonym&gt;\n" +
		"\t\t&lt;/synonyms&gt;\n" +
		"\t&lt;/compound&gt;\n" +
		"&lt;/compounds&gt;\n"
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input file. The only allowed file format is PeakML and no " +
			"limitations are set to its contents. When this is not set the input is read " +
			"from the standard in.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the output file. This file is writen in the same PeakML file format " +
			"as the input file, with the addition of identification annotations (tag: " +
			"'" + Annotation.identification + "' containing the database id's).")
		public String output = null;
		
		
		@Option(name="minDistributionValue", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The minimum probability mass that a mass needs to be kept in the " +
			"distribution of the spectrum"
		)
		public double minDistributionValue = 10e-6;
		
		@Option(name="maxValues", param="int", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The maximum number of entries in a compound's spectrum"
		)
		public int maxValues = 10;
		
		@Option(name="adducts", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option to specify which adducts to search for."
			)
		public String adducts = "M+H,M-H";
		
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
				Tool.createFilePath(options.output, true);
			}
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// load the data
			//if (options.verbose)
			//	System.out.println("Loading data");
			//ParseResult result = PeakMLParser.parse(input, true);
			//IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;
			
			// load the molecule-data
			if (options.verbose)
				System.out.println("Loading molecule data");
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			molecules.putAll(MoleculeIO.parseXml(input));
			final String[] adducts = options.adducts.split(",");
			
			// process the data
			if (options.verbose)
				System.out.println("Identifying Isotope Patterns");
			CSVWriter writer = new CSVWriter(new OutputStreamWriter(output), ',');
			writer.writeNext(new String[]{ "Name", "Formula", "Distribution", "Masses", "Adducts", "Isotopes" });
			
			
			
			for (Molecule molecule : molecules.values()) {
				final MolecularFormula formula = molecule.getFormula();
				//System.err.println(molecule.getName());				
				final GeneralMassSpectrum ms = GeneralMassSpectrum.getGeneralMassSpectrum(formula, adducts,
					options.minDistributionValue, options.maxValues);
				String[] arr = new String[6];
				arr[0] = molecule.getName();
				arr[1] = formula.toString();
				arr[2] = Common.join(ms.getDistribution(), " ");
				arr[3] = Common.join(ms.getMasses(), " ");
				arr[4] = Common.join(ms.getAdducts(), " ");
				arr[5] = Common.join(ms.getGeneralMolecularFormulas(), " ");
				//System.err.println(ms.toString());
				writer.flush();
				
				
				
				/*
				final StringBuilder distBuilder = new StringBuilder();
				final StringBuilder massBuilder = new StringBuilder();
				final StringBuilder adductBuilder = new StringBuilder();
				for (String adduct : adducts) {
					final MolecularFormula formulaCopy = new MolecularFormula(formula);
					System.err.println("Adduct: " + adduct + " " + formula);
					final GeneralDerivative gd = new GeneralDerivative(formulaCopy, adduct, false);
					if ( ! gd.isValid() ) {
						continue;
					}
					System.err.println("Here: " + adduct + " maxValues: " + options.maxValues);
					final GeneralMassSpectrum ms = gd.getDistribution(options.minDistributionValue, options.maxValues);
					System.err.println("Done: " + adduct);
					
					
					writer.write(arg0)
					//writer.write(molecule.getName());
					//writer.write(formula.toString());
					
					final List<Double> distribution = ms.getDistribution();
					final List<Double> masses = ms.getMasses();
					//final double[] distribution = ms.getDistribution();
					//final double[] masses = ms.getMasses();
					assert distribution.size() == masses.size();
					assert distribution.size() <= options.maxValues;
					assert distribution.size() <= 10;
					for (int i = 0; i < distribution.size(); ++i) {
						final String distValue = Double.toString(distribution.get(i));
						final String massValue = Double.toString(masses.get(i));
						distBuilder.append(distValue);
						distBuilder.append(" ");
						massBuilder.append(massValue);
						massBuilder.append(" ");
						adductBuilder.append(adduct);
						adductBuilder.append(" ");
					}
					
				}
				writer.write(distBuilder.toString());
				writer.write(massBuilder.toString());
				writer.write(adductBuilder.toString());
				writer.endRecord();
				writer.flush();
*/
				//final MolecularFormula formula = molecule.getFormula();
				//final NewMassSpectrum ms = PeakDistributionCalculator.getIntensityDistribution(formula,
				//	options.minDistributionValue, options.maxValues);

			}
			writer.close();
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
