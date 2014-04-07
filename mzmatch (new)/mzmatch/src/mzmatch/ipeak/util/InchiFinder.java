package mzmatch.ipeak.util;

// java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.comparator.NaturalOrderComparator;
import com.joewandy.mzmatch.model.PubChemMolecule;
import com.joewandy.mzmatch.query.PubChemQuery;

public class InchiFinder {
	
	final static String version = "1.0.1";
	final static String application = "Identify";

	@OptionsClass(name = application, version = version, author = "RA Scheltema (r.a.scheltema@rug.nl)", description = "Find inchi keys of compounds for ronan")	
	public static class Options {
		@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option for the input file. The only allowed file format is PeakML and no "
				+ "limitations are set to its contents. When this is not set the input is read "
				+ "from the standard in.")
		public String input = null;
		@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option for the output file. This file is writen in the same PeakML file format "
				+ "as the input file, with the addition of identification annotations (tag: "
				+ "'"
				+ Annotation.identification
				+ "' containing the database id's).")
		public String output = null;
		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, usage = "When this is set, the help is shown.")
		public boolean help = false;
	}

	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
		
		try {
			
			Tool.init();

			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			cmdline.parse(args);
			if (options.help) {
				Tool.printHeader(System.out, application, version);
				cmdline.printUsage(System.out, "");
				return;
			}

			Tool.printHeader(System.out, application, version);
			cmdline.printOptions();

			// open the streams
			System.out.println("Loading molecule data");
			Map<String, Molecule> molecules = new TreeMap<String, Molecule>(new NaturalOrderComparator());
			molecules.putAll(MoleculeIO.parseXml(new FileInputStream(options.input)));

			PubChemQuery query = new PubChemQuery();
			for (Entry<String, Molecule> entry : molecules.entrySet()) {
				Molecule mol = entry.getValue();
				System.out.print("Processing " + entry.getKey() + "\n\t");
				PubChemMolecule retrieved = query.findCompoundsByNameFormula(mol,  true);
				if (retrieved == null) {
					// second try, use only the name
					retrieved = query.findCompoundsByNameFormula(mol, false);
				}
				if (retrieved != null) {
					mol.setInChi(retrieved.getInChi());
					System.out.println("\tRetrieved");
					System.out.println("\t\tname=" + retrieved.getIupacName());
					System.out.println("\t\tformula=" + retrieved.getMolecularFormula());
					System.out.println("\t\tinchi=" + retrieved.getInChi());
				}
			}
			
			// write the data
			OutputStream output = System.out;
			if (options.output != null) {
				output = new FileOutputStream(options.output);
			}
			HashMap<String, Molecule> temp = new HashMap<String, Molecule>();
			MoleculeIO.writeXml(temp, output);

		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}
	}

}
