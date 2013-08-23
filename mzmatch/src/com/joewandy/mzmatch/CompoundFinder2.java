package com.joewandy.mzmatch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mzmatch.util.Tool;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Molecule;
import peakml.chemistry.PeriodicTable;
import peakml.io.ParseResult;
import peakml.io.chemistry.MoleculeIO;
import peakml.io.peakml.PeakMLParser;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.mzmatch.query.CompoundQuery;
import com.joewandy.mzmatch.query.KeggQuery;
import com.joewandy.mzmatch.query.PubChemQuery;

public class CompoundFinder2 {

	// main entrance
	final static String version = "1.0.1";
	final static String application = "Identify";
	@OptionsClass(name=application, version=version,  author="Joe Wandy (j.wandy.1@research.gla.ac.uk)",
		description="Retrieves related formulae, given existing compound XML files."
	)
	
	public static class Options {

		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input database file, in peakml format.")
		public String input = null;

		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Option for the output database file, in compound-xml format.")
		public String output = null;

		@Option(name="db", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Option for the output database file, in compound-xml format.")
		public String db = null;
		
		@Option(name="type", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Which database to query. Options are 'pubchem' or 'kegg'.")
		public String type = null;
		
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"The accuracy of the measurement in parts-per-milion. This value is used for " +
				"matching the masses to those found in the supplied databases. This value is " +
				"obligitory.")
		public double ppm = -1;
		
		@Option(name="m", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"For positive or negative ionisation mode ?")
		public String mode = null;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;

		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;

	}
	
	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
				
		try {

			Tool.init();

			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);

			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help) {
				Tool.printHeader(System.out, application, version);
				cmdline.printUsage(System.out, "");
				return;
			}

			if (options.verbose) {
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}

			if (options.input == null || options.input == "") {
				System.err.println("[ERROR]: no input file.");
				System.exit(0);				
			}

			if (options.ppm == -1) {
				System.err.println("[ERROR]: the ppm-value was not set.");
				System.exit(0);
			}
									
			OutputStream output = System.out;
			if (options.output != null) {
				output = new FileOutputStream(options.output);
			}
			
			// load the molecule-data
			if (options.verbose) {
				System.out.println("Loading molecule data");
			}
			Map<String, Molecule> molecules = MoleculeIO.parseXml(new FileInputStream(options.db));

			// load the data
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			if (options.verbose)
				System.out.println("Loading data");
			ParseResult result = PeakMLParser.parse(input, true);
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;
			
			// begin processing here
			if (options.verbose) {
				System.out.println("Processing peaks");
				System.out.println();
			}
			
			/*
			 * FOR FUTURE NEED
			 */
			
			String[] posStr = { "M+H", "M+K", "M+2ACN+H" };
			double[] posValues = { -1.007276, -38.963158, -83.060370 } ;
			
			String[] negStr = { "M-H", "M+Cl", "M+Br" };
			double[] negValues = { 1.007276, 34.969402, 78.918885 };

			double vals[] = null;
			String adductStr[] = null;
			if ("positive".equalsIgnoreCase(options.mode)) {
				vals = posValues;
				adductStr = posStr;
			} else if ("negative".equalsIgnoreCase(options.mode)) {
				vals = negValues;
				adductStr = negStr;
			}
			assert(vals.length == adductStr.length);

			/*
			 * FOR NOW ...
			 */

//			String[] posStr = { "M+H" };
//			double[] posValues = { -1.007276 } ;
//			
//			String[] negStr = { "M-H" };
//			double[] negValues = { 1.007276 };
//
//			double vals[] = null;
//			String adductStr[] = null;
//			if ("positive".equalsIgnoreCase(options.mode)) {
//				vals = posValues;
//				adductStr = posStr;
//			} else if ("negative".equalsIgnoreCase(options.mode)) {
//				vals = negValues;
//				adductStr = negStr;
//			}
//			assert(vals.length == adductStr.length);

			/* 
			 * important for initialization to be outside the loop below ...
			 * because we're compounding each query results inside compoundQuery itself
			 * TODO: bad design, fix this !
			 */
			CompoundQuery compoundQuery = null;
			if ("pubchem".equalsIgnoreCase(options.type)) {
				final int queryNumber = 10;
				compoundQuery = new PubChemQuery(molecules, queryNumber, queryNumber);
			} else if ("kegg".equalsIgnoreCase(options.type)) {
				compoundQuery = new KeggQuery(molecules);
			}
			
			for (int i = 0; i < vals.length; i++) {
				
				double val = vals[i];
				String adduct = adductStr[i];
				System.out.println("============================================");
				System.out.println("PROCESSING " + adduct);
				System.out.println("============================================");				
				
				int counter = 0;
				for (IPeak peak : peakset) {
			
					// remember here we'd swap the signs because we're going from peaks to compounds
					double mass = peak.getMass() + val;
					
					// this is mass tolerance expressed in absolute m/z values. Use either this or options.ppm (relative value).
					double delta = PeriodicTable.PPM(mass, options.ppm);

					Set<Molecule> currentResult = new HashSet<Molecule>();
					try {
						if ("pubchem".equalsIgnoreCase(options.type)) {
							currentResult.addAll(compoundQuery.findCompoundsByMass(mass, options.ppm, 0));
						} else if ("kegg".equalsIgnoreCase(options.type)) {
							currentResult.addAll(compoundQuery.findCompoundsByMass(mass, 0, delta));
						}					
					} catch(IOException ioe) {
						System.err.println(ioe.getMessage());
					}
					
					counter++;
					System.out.println("Peak #" + counter + " found " + currentResult.size() + " compounds");
					System.out.println();
					
				}
				
			}
									
			// remove isomers
			Set<Molecule> totalResults = compoundQuery.getResult();
			Map<String, Molecule> uniqueMap = new HashMap<String, Molecule>();
			for (Molecule mol : totalResults) {
				uniqueMap.put(mol.getPlainFormula(), mol);
			}
			
			System.out.println();
			System.out.println("================================================================");
			System.out.println(molecules.size() + " molecules processed " + ", found " + uniqueMap.size()
					+ " other unique compounds (ppm=" + String.format("%.2f", options.ppm) + ")");
			System.out.println("================================================================");
			System.out.println();
			
			Map<String, Molecule> toWrite = new HashMap<String, Molecule>();
			for (Entry<String, Molecule> e : uniqueMap.entrySet()) {
				Molecule mol = e.getValue();
				// assume that even in different databases (pubchem, kegg), the ids are still unique
				toWrite.put(mol.getDatabaseID(), mol); 
			}
			shuffleAndWrite(output, toWrite);
			
		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}
		
	}
	
	private static void shuffleAndWrite(OutputStream output,
			Map<String, Molecule> molecules) throws IOException {
		List<String> keys = new ArrayList<String>(molecules.keySet());
		HashMap<String, Molecule> mapResults = new HashMap<String, Molecule>();
		for (String key : keys) {
			mapResults.put(key, molecules.get(key));
		}
		MoleculeIO.writeXmlRandomized(mapResults, output);
	}	

}