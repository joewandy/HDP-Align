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



package mzmatch.ipeak.util;


// java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import com.csvreader.CsvReader;

import mzmatch.ipeak.sort.IdentifyPeaksets;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Mass;
import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Molecule;
import peakml.chemistry.PeriodicTable;
import peakml.chemistry.Polarity;
import peakml.io.ParseResult;
import peakml.io.chemistry.MoleculeIO;
import peakml.io.peakml.PeakMLParser;
import peakml.io.peakml.PeakMLWriter;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;




/* HISTORY:
 * 1.0.1	- fixed a bug in the upper limitation of the retention time.
 *			- complete documentation for the command-line options.
 *			- changed the option '-molecules' to '-databases'.
 */
public class Identify
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
		
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The accuracy of the measurement in parts-per-milion. This value is used for " +
			"matching the masses to those found in the supplied databases. This value is " +
			"obligitory.")
		public double ppm = -1;
		@Option(name="databases", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the molecule databases to match the contents of the input file to. " +
			"These files should adhere to the compound-xml format.")
		public Vector<String> databases = new Vector<String>();
		
		@Option(name="minrt", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Optional minimum retention time for excluding signals from the input-file. This " +
			"is for example convenient for excluding lipids on an LC/MS setup with a HILIC " +
			"column.")
		public double minrt = -1;
		@Option(name="maxrt", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Optional maximum retention time for excluding signals from the input-file. This " +
			"is for example convenient for including lipids on an LC/MS setup with a HILIC " +
			"column.")
		public double maxrt = -1;
		
		@Option(name="rtwindow", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Optional retention time window for finding matches from the databases. If this " +
			"is set and the database contains entries with previously registered retention " +
			"times for molecules. When this value is not set, the stored retention times are " +
			"ignored.")
		public double rtwindow = -1;

		@Option(name="massOverride", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option to say whether to override the calculated formula mass with the " +
			"monoisotopic mass element from the database. If this option is specified, and" +
			"the mass element is not present, then the calculated formula mass is used")
		public boolean massOverride = false;
		
		@Option(name="polarity", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option to discard molecules of the opposite polarity. If this option is specified, " +
			"any molecules of the opposite polarity will not be checked. Molecules with no polarity " +
			"specified will always be checked"
			)
		public String polarity = null;
		
		@Option(name="adducts", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option to specify which adducts to search for."
			)
		public String adducts = "M+H,M-H";
		
		@Option(name="moleculeMasses", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Experimental optional filename that contains a list of molecular masses for each peak"
			)
		public String moleculeMasses = null;
		
		@Option(name="identAnnotationName", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The name of the annotation to use for the weighted identification."
			)
		public String identAnnotationName = "simpleIdentification";
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	@SuppressWarnings("unchecked")
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
				if (options.ppm == -1)
				{
					System.err.println("[ERROR]: the ppm-value was not set.");
					System.exit(0);
				}
				
				if (options.databases.size() == 0)
				{
					System.err.println("[ERROR]: the molecule filename was not set.");
					System.exit(0);
				}
				for (String file : options.databases)
				{
					if (!new File(file).exists())
					{
						System.err.println("[ERROR]: the database '" + options.databases + "' does not exist.");
						System.exit(0);
					}
				}

				Tool.createFilePath(options.output, true);
			}
			Polarity givenPolarity = options.polarity == null ? null : Polarity.fromString(options.polarity);
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// load the data
			if (options.verbose)
				System.out.println("Loading data");
			ParseResult result = PeakMLParser.parse(input, true);
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;
			IdentifyPeaksets.identify(peakset);
			// load the molecule-data
			if (options.verbose)
				System.out.println("Loading molecule data");
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			for (String file : options.databases)
				molecules.putAll(MoleculeIO.parseXml(new FileInputStream(file)));

			final String[] adducts = options.adducts.split(",");
			
			List<Map<String,Double>> masses = null;
			if ( options.moleculeMasses != null ) {
				masses = new ArrayList<Map<String,Double>>();
				
				final CsvReader reader = new CsvReader(options.moleculeMasses, ',');
				reader.readHeaders();
				while (reader.readRecord()) {
					final String value = reader.get("adduct");
					final String[] values = value.split(" ");
					final Map<String,Double> mList = new HashMap<String,Double>();
					for ( int i = 1; i < values.length; i += 2) {
						final String adduct = values[i - 1];
						final double mass = Double.parseDouble(values[i]);
						mList.put(adduct, mass);
					}
					masses.add(mList);
				}
			}
			
			// process the data
			if (options.verbose)
				System.out.println("Identifying peaks");
			for (Molecule molecule : molecules.values())
			{
				final Polarity p = molecule.getPolarity();
				if ( givenPolarity != null && p != null && ! p.equals(givenPolarity) ) {
					continue;
				}
				
				if ( options.moleculeMasses != null ) {
					annotatePeaks(molecule, masses, peakset, options);
				} else {
					annotatePeaks(molecule, adducts, options, peakset, result);
				}
			}
			
			for ( IPeak peak : peakset ) {
				final Annotation idAnnotation = peak.getAnnotation(Annotation.identification);
				if ( idAnnotation == null ) {
					continue;
				}
				final String[] ids = idAnnotation.getValue().split(", ");
				if ( ids.length == 0 ) {
					continue;
				}
				final Annotation adductAnnotation = peak.getAnnotation(Annotation.adduct);
				final String[] adds = adductAnnotation.getValue().split(", ");
				final double val = 1.0 / ids.length;
				final String[] newAnnotations = new String[ids.length];
				for ( int i = 0; i < ids.length; ++i ) {
					final String id = ids[i];
					final Molecule m = molecules.get(id);
					final String adduct = adds[i];
					newAnnotations[i] = String.format("%s, %s, %s, monoisotopic, %.5f", id, m.getName(), adduct, val);
				}
				peak.addAnnotation(options.identAnnotationName, Common.join(newAnnotations, "; "));
			}
			
			// write the data
			if (options.verbose)
				System.out.println("Writing results");
			PeakMLWriter.write(result.header, peakset.getPeaks(), null, new GZIPOutputStream(output), null);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
	
	private static void annotatePeaks(final Molecule molecule, final String[] adducts, final Options options,
			final IPeakSet<IPeak> peakset, final ParseResult result) {
		for ( int adductIndex = 0; adductIndex < adducts.length; ++adductIndex ) {
			GeneralDerivative gd = new GeneralDerivative(molecule.getFormula(), adductIndex, options.massOverride, adducts);
			if ( ! gd.isValid() ) {
				continue;
			}
			final Polarity adductPolarity = gd.getPolarity();
			double mass = gd.getMass();
			double delta = PeriodicTable.PPM(mass, options.ppm);
			Vector<IPeak> neighbourhood = peakset.getPeaksOfMass(mass, delta);
			
			for (IPeak peak : neighbourhood)
			{
				final IPeakSet<IPeak> ps = (IPeakSet<IPeak>)peak;
				final IPeak firstPeak = ps.get(0);
				if ( adductPolarity != Common.getPeakPolarity(firstPeak, result.header) ) {
					continue;
				}
				
				if ((options.minrt!=-1&&peak.getRetentionTime()<=options.minrt) || (options.maxrt!=-1&&peak.getRetentionTime()>=options.maxrt))
					continue;
				if (options.rtwindow!=-1 && molecule.getRetentionTime()!=-1)
					if (Math.abs(molecule.getRetentionTime()-peak.getRetentionTime()) > options.rtwindow)
						continue;
				
				addAnnotation(peak, molecule, adducts[adductIndex], mass);
			}
		}
	}
	
	private static void annotatePeaks(final Molecule molecule, List<Map<String,Double>> masses, final IPeakSet<IPeak> peakset,
			final Options options) {
		final double moleculeMass = molecule.getFormula().getMass(Mass.MONOISOTOPIC);
		double delta = PeriodicTable.PPM(moleculeMass, options.ppm);
		
		final double lowerBound = moleculeMass - delta;
		final double upperBound = moleculeMass + delta;
		
		for ( int peakId = 0; peakId < peakset.size(); ++peakId ) {
			final Map<String,Double> mList = masses.get(peakId);
			final IPeak peak = peakset.get(peakId);
			for ( Map.Entry<String, Double> m : mList.entrySet()) {
				final String adduct = m.getKey();
				final double mass = m.getValue();
				if ( mass > lowerBound && mass < upperBound ) {
					addAnnotation(peak, molecule, adduct, mass);
				}
			}
		}
	}
	
	private static void addAnnotation(final IPeak peak, final Molecule molecule, final String adduct, final double mass) {
		final Annotation idAnnotation = peak.getAnnotation(Annotation.identification);
		final Annotation adductAnnotation = peak.getAnnotation(Annotation.adduct);
		final Annotation ppmAnnotation = peak.getAnnotation("ppm");
		double accuracy = PeriodicTable.PPMNr(mass, mass-peak.getMass());
		if (idAnnotation != null)
		{
			boolean add = true;
			String moleculeid = molecule.getDatabaseID();
			
			for (String id : idAnnotation.getValue().split(","))
			{
				if (id.trim().equals(moleculeid))
				{
					add = false;
					break;
				}
			}
			if (add == true) {
				peak.addAnnotation(Annotation.identification, idAnnotation.getValue() + ", " + moleculeid);
				peak.addAnnotation(Annotation.adduct, adductAnnotation.getValue() + ", " + adduct);
				peak.addAnnotation("ppm", ppmAnnotation.getValue() + ", " + accuracy);
			}
		}
		else {
			peak.addAnnotation(Annotation.identification, molecule.getDatabaseID());
			peak.addAnnotation(Annotation.adduct, adduct);
			peak.addAnnotation("ppm", accuracy);
		}
	}
}
