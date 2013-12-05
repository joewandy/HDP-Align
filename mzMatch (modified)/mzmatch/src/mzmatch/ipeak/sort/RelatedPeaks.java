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



package mzmatch.ipeak.sort;


// java
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Mass;
import peakml.chemistry.PeriodicTable;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;
import peakml.io.peakml.PeakMLWriter;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

@SuppressWarnings("unchecked")
public class RelatedPeaks
{
	// implementation
	final static double isotope_c13 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.CARBON,		1);
	final static double isotope_n15 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.NITROGEN,	1);
	final static double isotope_o18 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.OXYGEN,		1);
	final static double isotope_s34 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.SULFUR,		1);
	
	final static double H2 =  2*PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(Mass.MONOISOTOPIC);
	final static double H2O = 2*PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(Mass.MONOISOTOPIC) + PeriodicTable.elements[PeriodicTable.OXYGEN].getMass(Mass.MONOISOTOPIC);
	final static double NH3 = 3*PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(Mass.MONOISOTOPIC) + PeriodicTable.elements[PeriodicTable.NITROGEN].getMass(Mass.MONOISOTOPIC);
	
	
	public static final double[] getIntensityCourse(IPeak peak, Header header)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			double intensitycourse[] = new double[header.getNrMeasurementInfos()];
			for (int i=0; i<intensitycourse.length; ++i)
				intensitycourse[i] = 0;
			for (IPeak p : (IPeakSet<? extends IPeak>) peak)
				intensitycourse[header.indexOfMeasurementInfo(p.getMeasurementID())] = p.getIntensity();
			return intensitycourse;
		}
		
		return new double[]{peak.getIntensity()};
	}
	
	public static void findIsotopes(IPeak basepeak, int z, Vector<IPeak> peaks, double ppm, int i)
	{
		double basepeakmass = basepeak.getMass();
		String relation = (basepeak.getAnnotation(Annotation.relationship)==null ? "" : basepeak.getAnnotation(Annotation.relationship).getValue() + "|");
		for (IPeak peak : peaks)
		{
			double mass = peak.getMass();
			
			double _ppm = ppm;
			if (peak.getIntensity() < 100000)
				_ppm *= 2;
			
			boolean match = false;
			if (PeriodicTable.inRange(basepeakmass+i*(isotope_c13/z), mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "C13 isotope #" + i);
				match = true;
			}
			else if (PeriodicTable.inRange(basepeakmass+i*isotope_n15, mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "N15 isotope #" + i);
				match = true;
			}
			else if (PeriodicTable.inRange(basepeakmass+i*isotope_o18, mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "O18 isotope #" + i);
				match = true;
			}
			else if (PeriodicTable.inRange(basepeakmass+i*isotope_s34, mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "S34 isotope #" + i);
				match = true;
			}
			
			if (match == true)
				findIsotopes(basepeak, z, peaks, ppm, i+1);
		}
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "RelatedPeaks";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"LC-MS experiments yield large amounts of peaks, many of which correspond to derivatives of peaks " +
		"of interest (eg, isotope peaks, adducts, fragments, multiply charged molecules), termed here as " +
		"related peaks. This tool clusters groups of related peaks together and attempts to identify their " +
		"relationship. Each cluster is given a unique id, which is stored as an annotation with the name '" +
		Annotation.relationid + "'. The relationship is stored as an annotation with the name '" +
		Annotation.relationship + "'." +
		"\n\n" +
		"The possibility is offered only to store the most intense peak of each cluster (option '" +
		"basepeak'). This is useful for cleaning up the data before attempting statistical approaches " +
		"to mine the data. However, as the peak of interest is not per definition the most intense peak " +
		"one cannot rely on this file for identification purposes." +
		"\n\n" +
		"It is advised to only run this application on a combined file containing a complex experiment (e.g. " +
		"time-series, comparison, etc), as the performance of the approach is expected to improve with " +
		"increasingly complex setups. The approach iterates through the peak list sorted on descending " +
		"intensity and works threefold for identifying all of the derivatives. (1) the most intense, not " +
		"yet processed peak is selected and all not yet processed peaks within the specified retention time " +
		"window are collected. (2) First all of the collected peaks are correlated to the most intense " +
		"peak on signal shape. For this only the peaks from the same measurement are compared, as otherwise " +
		"distorted peak shapes would unfairly reduce the score. (3) The surviving peaksets are then " +
		"correlated on intensity trend (hence the need for complex setups). This is the most informative " +
		"feature, as it is not likely that that two co-eluting peaks have the same intensity trend for " +
		"complex setups. All signals to correlate well enough to the selected, most intense signal are " +
		"then clustered with this most intense signal.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i raw\\*.mzXML -o peaks\\ -ppm 3\n" +
		"\n" +
		"REM combine the individual timepoints\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\24hr_*.peakml -o 24hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\28hr_*.peakml -o 28hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\32hr_*.peakml -o 32hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"\n" +
		"REM combine all timepoints in a single file\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i *hr.peakml -o timeseries.peakml -ppm 3 -rtwindow 30 -combination set\n" +
		"\n" +
		"REM detect the related peaks\n" +
		"%JAVA% mzmatch.ipeak.sort.RelatedPeaks -v -i timeseries.peakml -o timeseries_related.peakml -ppm 3 -rtwindow 15\n",
		references=
		"1. Tautenhahn R, Bottcher C, Neumann S: Annotation of LC/ESI-MS Mass Signals. In: Bioinformatics Research and Development. Hochreiter S, Wagner R (Ed.), Springer-Verlag, Heidelberg, Germany, 371-380 (2007).\n" +
		"2. Scheltema RA, Decuypere S, Dujardin JC, Watson DG, Jansen RC, and Breitling R: A simple data reduction method for high resolution LC/MS data in metabolomics. Bioanalysis - in press."
	)
	
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input file. The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in. The contents of the file is enforced " +
			"to be peaksets (result of Combine) as this tool utilizes the full information " +
			"of peaksets in order to identify related peaks.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional filename where the output is written. If this is not set the output is " +
			"written to the standard output.")
		public String output = null;
		@Option(name="basepeaks", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional filename where the output is written. If this is not set the file with the " +
			"basepeaks is not written.")
		public String basepeaks = null;
		
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The mass tolerance in ppm. This value is used for the identification of the relations " +
			"in the found sets of related peaks.")
		public double ppm = -1;
		
		@Option(name="rtwindow", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The retention time window in seconds, defining the range where to look for matches.")
		public double rtwindow = -1;
		
		@Option(name="minrt", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Denotes the minimum retention time in seconds peaks should occur before being taken " +
			"into account for the relation process. When this value is not set all peaks are " +
			"taken into account.")
		public double minrt = -1;
		
		@Option(name="ignoreIntensity", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Ignore the intensity correlation across profiles")
		public boolean ignoreIntensity = false;
		
		@Option(name="minCorrSignals", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The minimum correlation value for two chromatographic peaks to be correlated.")
		public double minCorrSignals = 0.75;
		
		@Option(name="measure", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The measure of correlations between peaks. Valid options are pearson and cosine."
		)
		public String measure = "pearson";

		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
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
			
			// check the commandline parameters
			{
				if (options.ppm == -1)
				{
					System.err.println("[ERROR]: the ppm value needs to be specified.");
					System.exit(1);
				}
				if (options.rtwindow == -1)
				{
					System.err.println("[ERROR]: the rtwindow value needs to be specified.");
					System.exit(1);
				}
				if ( ! "pearson".equals(options.measure) && ! "cosine".equals(options.measure) ) {
					System.err.println("[ERROR]: valid options for measure are 'pearson' and 'cosine'.");
					System.err.println("'" + options.measure + "'");
					System.exit(1);
				}
				
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, true);
				if (options.basepeaks != null)
					Tool.createFilePath(options.basepeaks, true);
			}
			
			
			// 
			if (options.verbose)
				System.err.println("loading data");
			ParseResult result = PeakMLParser.parseIPeakSet(new FileInputStream(options.input), null);

			final Header header = result.header;
			final IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			IdentifyPeaksets.identify(peaks);
			
			// annotate the peaks with an id for the hashing
			int id = 0;
			for (IPeak peak : peaks)
				peak.setPatternID(id++);
			
			// match the peaks
			if (options.verbose)
				System.err.println("locating related peaks");
			final HashMap<Integer,double[]> intensity_courses = new HashMap<Integer,double[]>();

			Vector<IPeak> basepeaks = null;
			CorrelationMeasure measure = null;
			
			if ( "pearson".equals(options.measure) ) {
				measure = new PeakComparer.PearsonMeasure();
			} else if ( "cosine".equals(options.measure) ) {
				measure = new PeakComparer.CosineMeasure();
			}
			assert measure != null;

			final PeakComparer comparer = new PeakComparer(intensity_courses, header, measure, options.ignoreIntensity,
					options.minCorrSignals);
			basepeaks = IPeak.findRelatedPeaks(peaks.getPeaks(), options.minrt, options.rtwindow, comparer);
			assert basepeaks != null;
			
			labelRelationships(peaks, options.verbose, options.ppm);

			if (options.verbose)
				System.err.println("writing results");
			
			PeakMLWriter.write(
					result.header, peaks.getPeaks(), null,
					new GZIPOutputStream(new FileOutputStream(options.output)), null
				);
			
			if (options.basepeaks != null)
			{
				PeakMLWriter.write(
						result.header, basepeaks, null,
						new GZIPOutputStream(new FileOutputStream(options.basepeaks)), null
					);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
			e.printStackTrace();
		}
	}
	
	public static void labelRelationships(final IPeakSet<IPeak> peaks, final boolean verbose, final double ppm) {
		// locate the relationships
		if (verbose)
			System.err.println("determining relationships");
		int index = 0;
		while (index < peaks.size())
		{
			// check whether we're at a base-peak
			IPeak basepeak = peaks.get(index);
			basepeak.addAnnotation(Annotation.relationship, "bp");
			int basepeakid = basepeak.getAnnotation(IPeak.relationid).getValueAsInteger();
			
			// get properties
			double basepeakmass = basepeak.getMass();
			
			// get all the peaks related to this basepeak into a separate vector
			Vector<IPeak> related = new Vector<IPeak>();
			for (index+=1; index<peaks.size(); ++index)
			{
				IPeak peak = peaks.get(index);
				if (basepeakid != peak.getAnnotation(IPeak.relationid).getValueAsInteger())
					break;
				related.add(peak);
			}
			IPeakSet<IPeak> related_peakset = null;
			if (related.size() > 0)
				related_peakset = new IPeakSet<IPeak>(related);
			
			// determine charge-state
			int charge = 1;
			if (related_peakset != null)
			{
				for (int _charge=5; _charge>=1; _charge--)
				{
					// TODO the isotopes need to go down in intensity
					int nravailable = 0;
					for (int isotopenr=1; isotopenr<3; ++isotopenr)
					{
						double mass = basepeakmass+isotopenr*(isotope_c13/_charge);
						double delta = PeriodicTable.PPM(mass, ppm);
						
						Vector<IPeak> neighbourhood = related_peakset.getPeaksInMassRange(mass-delta, mass+delta);
						
						if (neighbourhood.size() > 0)
							nravailable++;
						else if (neighbourhood.size()==0 && isotopenr==1)
							break; // we haven't found the first isotope peak
					}
					
					if (nravailable != 0)
					{
						charge = _charge;
						break;
					}
				}
			}
			basepeak.addAnnotation("charge", Integer.toString(charge));
			
			// get the real basepeak when the most intense peak is multiply charged
			
			// find the isotopes
			findIsotopes(basepeak, charge, related, ppm, 1);
			
			// try to find the relationship
			for (IPeak peak : related)
			{
				// get properties
				double mass = peak.getMass();
				
				// centroiding artefact
				if (PeriodicTable.PPMNr(mass, Math.abs(mass-basepeakmass)) < 500)
				{
					peak.addAnnotation(Annotation.relationship, "centroid artefact");
					continue;
				}
				
				// multi-charged
				for (int z=1; z<5; ++z)
				{
					if (z == charge)
						continue;
					if (PeriodicTable.inRange(basepeakmass/z, mass, ppm))
					{
						peak.addAnnotation(Annotation.relationship, "multi-charged (" + z + ")");
						
						// check for isotopes
						findIsotopes(peak, z, related, ppm, 1);
					}
				}
				
				// *-mer's
				for (int i=2; i<10; ++i)
				{
					double mermass = i*basepeakmass;
					if (PeriodicTable.inRange(mermass, mass, ppm))
					{
						peak.addAnnotation(Annotation.relationship, String.format("%d-mer", i));
					
						// check for isotopes
						findIsotopes(peak, 1, related, ppm, 1);
					}
				}
				
				// oxidatie
				if (PeriodicTable.inRange(basepeakmass+H2, mass, ppm))
				{
					peak.addAnnotation(Annotation.relationship, "oxidation (a = c+H2)");
					findIsotopes(peak, 1, related, ppm, 1);
				}
				if (PeriodicTable.inRange(basepeakmass-H2, mass, ppm))
				{
					peak.addAnnotation(Annotation.relationship, "oxidation (a = c-H2)");
					findIsotopes(peak, 1, related, ppm, 1);
				}
				
				// adducts
				for (PeriodicTable.Derivative derivative : PeriodicTable.adducts_positive)
				{
					if (derivative.adductdeduct==PeriodicTable.ADDUCT || derivative.adductdeduct==PeriodicTable.ADDUCT_DEDUCT)
					{
						if (PeriodicTable.inRange(basepeakmass+derivative.getMass(), mass, ppm))
						{
							peak.addAnnotation(Annotation.relationship, derivative.getName());
							findIsotopes(peak, 1, related, ppm, 1);
						}
					}
					if (derivative.adductdeduct==PeriodicTable.ADDUCT || derivative.adductdeduct==PeriodicTable.ADDUCT_DEDUCT)
					{
						if (PeriodicTable.inRange(basepeakmass-derivative.getMass(), mass, ppm))
						{
							peak.addAnnotation(Annotation.relationship, derivative.getName());
							findIsotopes(peak, 1, related, ppm, 1);
						}
					}
				}
				if (PeriodicTable.inRange(basepeakmass+H2O, mass, ppm))
				{
					peak.addAnnotation(Annotation.relationship, "plus h2o");
					findIsotopes(peak, 1, related, ppm, 1);
				}
				
				// deducts
				// -carboxyl group
				if (PeriodicTable.inRange(basepeakmass-NH3, mass, ppm))
				{
					peak.addAnnotation(Annotation.relationship, "minus nh3");
					findIsotopes(peak, 1, related, ppm, 1);
				}
				else if (PeriodicTable.inRange(basepeakmass-H2O, mass, ppm))
				{
					peak.addAnnotation(Annotation.relationship, "minus h2o");
					findIsotopes(peak, 1, related, ppm, 1);
				}
			}
			
			// try to find +h2
			if (related_peakset != null)
			{
				for (IPeak peak : related_peakset)
				{
					String mass = String.format("%5.2f", peak.getMass());
					
					for (IPeak neighbour : related_peakset.getPeaksOfMass(peak.getMass()+H2, PeriodicTable.PPM(peak.getMass(), ppm)))
					{
						neighbour.addAnnotation(Annotation.relationship, mass + "+H2");
						findIsotopes(neighbour, 1, related, ppm, 1);
					}
					for (IPeak neighbour : related_peakset.getPeaksOfMass(peak.getMass()-H2, PeriodicTable.PPM(peak.getMass(), ppm)))
					{
						neighbour.addAnnotation(Annotation.relationship, mass + "-H2");
						findIsotopes(neighbour, 1, related, ppm, 1);
					}
				}
			}
			
			// try to find isotope-differences for all unidentified peaks
			if (related_peakset != null)
			{
				for (IPeak peak : related_peakset)
				{
					if (peak.getAnnotation(Annotation.relationship) != null)
						continue;
					
					if (peak.getMass() < basepeakmass)
						peak.addAnnotation(Annotation.relationship, "fragment?");
					else
						peak.addAnnotation(Annotation.relationship, "potential bp");
					findIsotopes(peak, 1, related, ppm, 1);
				}
			}
		}
	}
}
