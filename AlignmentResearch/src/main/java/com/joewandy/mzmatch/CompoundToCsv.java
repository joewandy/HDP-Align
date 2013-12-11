package com.joewandy.mzmatch;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralDerivative;
import mzmatch.util.Tool;
import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;
import au.com.bytecode.opencsv.CSVWriter;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

public class CompoundToCsv {

	// main entrance
	final static String version = "1.0.1";
	final static String application = "Identify";

	@OptionsClass(name = application, version = version, author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", description = "Convert compound XML file to CSV.")
	public static class Options {

		@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option for the input database file, in compound-xml format.")
		public String input = null;

		@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option for the output database file, in csv format.")
		public String output = null;

		@Option(name = "minDistributionValue", param = "double", type = Option.Type.REQUIRED_ARGUMENT, usage = "The minimum probability mass that a mass needs to be kept in the "
				+ "distribution of the spectrum")
		public double minDistributionValue = 10e-6;

		@Option(name = "maxValues", param = "int", type = Option.Type.REQUIRED_ARGUMENT, usage = "The maximum number of entries in a compound's spectrum")
		public int maxValues = 10;

		@Option(name = "adducts", param = "string", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option to specify which adducts to search for.")
		public String adducts = "M+H,M-H";

		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, usage = "When this is set, the help is shown.")
		public boolean help = false;

		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, usage = "When this is set, the progress is shown on the standard output.")
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
				System.err.println("[ERROR]: no input database.");
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
			Map<String, Molecule> molecules = new HashMap<String, Molecule>();
			molecules.putAll(MoleculeIO.parseXml(new FileInputStream(
					options.input)));
			final String[] adducts = options.adducts.split(",");

			// begin processing here
			if (options.verbose) {
				System.out.println("Processing molecule data");
			}

			// write header to csv
			CSVWriter writer = new CSVWriter(new OutputStreamWriter(output),
					',', CSVWriter.NO_QUOTE_CHARACTER);
			String[] header = new String[] { "ID", "m/z", "Retention",
					"Identity", "Formula" };
			writer.writeNext(header);

			// write content to csv
			int counter = 0;
			for (Molecule molecule : molecules.values()) {

				if (options.verbose) {
					System.out.println("Processing Molecule="
							+ molecule.getName() + " ("
							+ molecule.getPlainFormula() + ")");
				}
				counter++;

				for (int i = 0; i < adducts.length; i++) {
					String adduct = adducts[i];
					GeneralDerivative gd = new GeneralDerivative(
							molecule.getFormula(), i, false, adducts);
					if (!gd.isValid()) {
						continue;
					}
					double mass = gd.getMass();
					double rt = molecule.getRetentionTime();
					try {

						String[] arr = new String[5];
						arr[0] = molecule.getDatabaseID();

						// arr[1] = String.format("%.5f",
						// molecule.getMass(Mass.MONOISOTOPIC, true));
						arr[1] = String.format("%.6f", mass);

						arr[2] = String.format("%.2f", rt);

						String name = molecule.getName().replace(',', '-');
						arr[3] = name + " [" + adduct + "]";

						arr[4] = molecule.getPlainFormula();
						// arr[4] = ms.getIsotopicFormula(i).toString();

						System.out.println("\t" + Common.join(arr, ","));
						writer.writeNext(arr);
						writer.flush();

					} catch (NoSuchElementException e) {
						System.out.println("ERROR: " + e.getMessage());
					}

					System.out.println();
				}

			}
			writer.close();

			System.out.println();
			System.out
					.println("================================================================");
			System.out.println("Processed " + counter + " molecules");
			System.out
					.println("================================================================");
			System.out.println();

		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}

	}

}