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
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import mzmatch.ipeak.util.GeneralMassSpectrumDatabase;
import mzmatch.util.RandomSeedException;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.Peak;
import peakml.chemistry.Mass;
import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Molecule;
import peakml.chemistry.PeriodicTable;
import peakml.chemistry.Polarity;
import peakml.io.Header;
import peakml.io.MeasurementInfo;
import peakml.io.ParseResult;
import peakml.io.chemistry.MoleculeIO;
import peakml.io.chemistry.MoleculeIO.KeyValueContainer;
import peakml.io.peakml.PeakMLParser;
import peakml.io.peakml.PeakMLWriter;
import peakml.math.Signal;
import peakml.math.Statistical;
import peakml.util.Pair;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;
import domsax.XmlParserException;

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
		
		@Option(name="method", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Which method to use to perform the clustering of related peaksets. Valid options are greedy and sample."
		)
		public String method = "greedy";
		
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
		
		@Option(name="p1", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for mixture model clustering."
		)
		public double p1 = 0.001;
		
		@Option(name="p0", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for mixture model clustering."
		)
		public double p0 = 0.97;
		
		@Option(name="alpha", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for mixture model clustering."
		)
		public double alpha = 1.0;
		
		@Option(name="alpha0", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for BetaBeta mixture model clustering."
		)
		public double alpha0 = 2.0;
		
		@Option(name="alpha1", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for BetaBeta mixture model clustering."
		)
		public double alpha1 = 10.0;
		
		@Option(name="numSamples", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Number of samples to take after burn in."
		)
		public int numSamples = 20;
		
		@Option(name="burnIn", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The amount of initial samples to discard."
		)
		public int burnIn = 30;
		
		@Option(name="initialNumClusters", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for mixture model clustering."
		)
		public int initialNumClusters = 10;
		
		@Option(name="compoundDatabases", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
				"Parameter for optional compound database to match against during mixture model clustering."
		)
		public Vector<String> compoundDatabases = new Vector<String>();
		
		@Option(name="adducts", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option to specify which adducts to search for."
		)
		public String adducts = "M+H,M-H";
		
		@Option(name="minDistributionValue", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The minimum probability mass that a mass needs to be kept in the " +
			"distribution of the spectrum"
		)
		public double minDistributionValue = 10e-6;
		
		@Option(name="maxValues", param="int", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The maximum number of entries in a compound's spectrum"
		)
		public int maxValues = 3;
		/*
		@Option(name="massPrecision", param="float", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Theoretical mass precision in ppm"
		)
		public double massPrecisionPPM = 3.0;
		*/
		@Option(name="rho", param="float", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The ratio of the maximum peak distribution to the baseline distribution"
		)
		public double rho = 100.0;
		
		@Option(name="retentionTimeSD", param="float", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The ratio of the maximum peak distribution to the baseline distribution"
		)
		public double retentionTimeSD = 2.5;
		
		@Option(name="identificationPeaks", param="int", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"A comma seperated list of the identification rules to use. Options are "
		)
		public int identificationPeaks = 3;
		
		@Option(name="filterPPM", param="float", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"A comma seperated list of the identification rules to use. Options are "
		)
		public double filterPPM = 5.0;
		
		@Option(name="rtClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"A flag to specify whether clustering using retention time should be used"
		)
		public boolean rtClustering = true;
		
		@Option(name="corrClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"A flag to specify whether clustering using peak shape correlations should be used"
		)
		public boolean corrClustering = true;
		
		@Option(name="seed", param="long", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for mixture model clustering."
		)
		public long seed = -1;
		
		@Option(name="debug", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Should debugging information be printed."
		)
		public boolean debug = false;
		
		@Option(name="debugOut", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Where debuging information is output to."
		)
		public String debugOut = null;
		
		@Option(name="sampleOut", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Where sample information is output to."
		)
		public String sampleOut = null;

		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	public static void main(String args[])
	{
		long seed = -1;
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
				if ( ! "greedy".equals(options.method) && ! "sample".equals(options.method) ) {
					System.err.println("[ERROR]: valid options for method are 'greedy' and 'sample'.");
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
				if (options.sampleOut != null)
					Tool.createFilePath(options.sampleOut, true);
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
			
			final Random random = new Random();
			seed = options.seed == -1 ? random.nextLong() : options.seed;
			random.setSeed(seed);
			//if (options.verbose)
			System.err.println("Random seed is: " + seed);
			Vector<IPeak> basepeaks = null;
			CorrelationMeasure measure = null;
			
			if ( "pearson".equals(options.measure) ) {
				measure = new PearsonMeasure();
			} else if ( "cosine".equals(options.measure) ) {
				measure = new CosineMeasure();
			}
			assert measure != null;

			if ( "greedy".equals(options.method) ) {
				final PeakComparer comparer = new PeakComparer(intensity_courses, header, measure, options.ignoreIntensity,
						options.minCorrSignals);
				basepeaks = IPeak.findRelatedPeaks(peaks.getPeaks(), options.minrt, options.rtwindow, comparer);
			} else if ( "sample".equals(options.method) ) {
				basepeaks = modelBasedClustering(options, peaks, header, random, measure);
			}
			assert basepeaks != null;
			
			labelRelationships(peaks, options);

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
			throw new RandomSeedException(seed, e);
		}
	}
	
	private static Vector<IPeak> modelBasedClustering(final Options options, IPeakSet<IPeak> peaks, final Header header,
			final Random random, final CorrelationMeasure measure) throws IOException, XmlParserException {
		float rangeMin = -1.0f;
		float rangeMax = 1.0f;

		if ( "pearson".equals(options.measure) ) {
			rangeMin = -1.0f;
			rangeMax = 1.0f;
		} else if ( "cosine".equals(options.measure) ) {
			rangeMin = 0.0f;
			rangeMax = 1.0f;
		}
		
		//SortedMap<String,Molecule> molecules = new TreeMap<String,Molecule>();
		final List<peakml.util.Pair<String, Molecule>> molecules = new ArrayList<Pair<String,Molecule>>();
		final KeyValueContainer<String,Molecule> adapter = new MoleculeIO.KeyValueContainerListAdapter<String,Molecule>(molecules);
		for (String file : options.compoundDatabases)
			MoleculeIO.parseXml(new FileInputStream(file), adapter);
		

		Clusterer.LikelihoodScorer inScorer = new Clusterer.BetaInLikelihoodScorer(options.p1, options.alpha1, rangeMin, rangeMax);
		Clusterer.LikelihoodScorer outScorer = new Clusterer.BetaOutLikelihoodScorer(options.p0, options.alpha0, rangeMin, rangeMax);

		
		
		//Clusterer<FormulaClustering> clusterer = null;
		Vector<IPeak> basepeaks;
		if ( molecules.size() == 0 ) {
			System.err.println("No database, so only performing clustering");
			final Data data = new Data(header, peaks);
			//System.out.println(data);
			//System.exit(1); 
			final CorrelationParameters parameters = new CorrelationParameters(options.rtwindow, options.p1, options.p0,
					options.alpha, options.numSamples, options.burnIn, options.retentionTimeSD, options.debug, options.initialNumClusters);
			final List<PeakLikelihoodScorer<SimpleClustering>> scorers = new ArrayList<PeakLikelihoodScorer<SimpleClustering>>();
			if ( options.corrClustering ) {
				scorers.add(new CorrelationClusterer.ClusteringScorer<SimpleClustering>(data, parameters, inScorer, outScorer, measure));
			}
			if ( options.rtClustering ) {
				scorers.add(new RetentionTimeClusteringScorer<SimpleClustering>(data, parameters));
			}
			final PeakPosteriorScorer<SimpleClustering> scorer = new PeakPosteriorScorer<SimpleClustering>(scorers, parameters);
			Clusterer<Data,SimpleClustering> clusterer = new CorrelationClusterer(data, parameters, random,
					inScorer, outScorer, measure, scorer);
			final List<SampleHandler<Data,SimpleClustering>> handlers = new ArrayList<SampleHandler<Data,SimpleClustering>>();
			basepeaks = Clusterer.findRelatedPeaks(peaks, clusterer, random, handlers);
		} else {
			final List<String> moleculeNames = new ArrayList<String>();
			final int mSize = molecules.size();
			
			for ( int ei = 0; ei < mSize; ++ei ) {
				final Pair<String, Molecule> entry = molecules.get(ei);
				moleculeNames.add(entry.v1);
			}
			moleculeNames.add("RoNaN");
			
			final String[] adducts = options.adducts.split(",");
			GeneralMassSpectrumDatabase theoreticalSpectrums = new GeneralMassSpectrumDatabase(molecules, adducts,
					options.minDistributionValue, options.maxValues);
			/*
			final List<GeneralMassSpectrum> theoreticalSpectrums = new ArrayList<GeneralMassSpectrum>();
			
			//int i = 0;
			for ( Pair<String, Molecule> entry : molecules ) {
				final Molecule m = entry.v2;
				final MolecularFormula formula = m.getFormula();
				
				final GeneralMassSpectrum ms = GeneralMassSpectrum.getGeneralMassSpectrum(formula, adducts,
						options.minDistributionValue, options.maxValues);
				//System.out.println("ms: " + i++ + " size: " + ms.size());
				theoreticalSpectrums.add(ms);
			}
			 */
			final int databaseSize = theoreticalSpectrums.size();
			if ( options.filterPPM > 0.0 ) {
				final FormulaData tempData = new FormulaData(header, peaks, theoreticalSpectrums);
				final Polarity[] polarities = tempData.polarities;
				//final Polarity[] polarities = Clusterer.getPolarities(header, peaks);
				//System.err.println(Arrays.toString(polarities));
				//assert false;
				final List<IPeak> filterList = new ArrayList<IPeak>();
				final boolean[] foundMono = new boolean[theoreticalSpectrums.size()];
				for (int peak = 0; peak < peaks.size(); ++peak) {
					final IPeak thePeak = peaks.get(peak);
					final double peakMass = thePeak.getMass();
					final double logPeakMass = Math.log(peakMass);
					final double deviation = Common.onePPM * options.filterPPM;
					final Polarity peakPolarity = polarities[peak];
					boolean found = false;
					
					final int msSize = tempData.theoreticalSpectrums.size();
					outside: for ( int msi = 0; msi < msSize; ++msi ) {
						final GeneralMassSpectrum ms = tempData.theoreticalSpectrums.get(msi);
						final List<Polarity> theoreticalPolarities = ms.getPolarities();
						final List<Double> masses = ms.getMasses();
						
						for ( int theoPeak = 0; theoPeak < masses.size(); ++theoPeak) {
							//System.err.println("theoPolarity: " + theoPolarity);
							final double logTheoMass = Math.log(masses.get(theoPeak));
							final Polarity theoPolarity = theoreticalPolarities.get(theoPeak);
							if ( peakPolarity == theoPolarity &&
									logPeakMass < logTheoMass + deviation &&
									logPeakMass > logTheoMass - deviation) {
								found = true;
								final int adduct = ms.getAdduct(theoPeak);
								final List<Integer> ranks = theoreticalSpectrums.getRanks(msi, peakPolarity, adduct);
								if ( theoPeak == ranks.get(0) ) {
									foundMono[msi] = true;
								}
								break outside;
							}
						}

					}
					if ( found ) {
						//System.err.println("Added peak " + peak);
						filterList.add(thePeak);
					} else {
						thePeak.addAnnotation(IPeak.relationid, "-1", Annotation.ValueType.INTEGER);
					}
					
				}
				peaks = new IPeakSet<IPeak>(filterList);
				final List<Integer> spectrumSubset = new ArrayList<Integer>();
				for ( int i = 0; i < foundMono.length; ++i ) {
					if ( foundMono[i] ) {
						spectrumSubset.add(i);
					}
				}
				final GeneralMassSpectrumDatabase newDatabase = theoreticalSpectrums.getSubset(spectrumSubset);
				System.err.println("Old database size: " + theoreticalSpectrums.size() + " new database size: " + newDatabase.size());
				theoreticalSpectrums = newDatabase;
			}
			//final Polarity[] polarities = Clusterer.getPolarities(header, peaks);
			//System.err.println(Arrays.toString(polarities));
			//assert false;
			
			
			final FormulaData data = new FormulaData(header, peaks, theoreticalSpectrums);
			final PrintStream dout = options.debugOut == null ? System.err : new PrintStream(options.debugOut);
			
			final FormulaParameters parameters = new FormulaParameters(options.rtwindow, options.p1, options.p0, options.alpha,
					options.numSamples, options.burnIn, options.debug, dout, options.minDistributionValue, options.maxValues,
					options.ppm, options.rho, options.retentionTimeSD);
			final List<PeakLikelihoodScorer<FormulaClustering>> scorers = new ArrayList<PeakLikelihoodScorer<FormulaClustering>>();
			if ( options.corrClustering ) {
				scorers.add(new CorrelationClusterer.ClusteringScorer<FormulaClustering>(data, parameters, inScorer, outScorer, measure));
			}
			if ( options.rtClustering ) {
				scorers.add(new RetentionTimeClusteringScorer<FormulaClustering>(data, parameters));
			}
			final FormulaClusterer.MassIntensityClusteringScorer miScorer = new FormulaClusterer.MassIntensityClusteringScorer(
					data, parameters, inScorer, outScorer, measure, adducts, random);
			scorers.add(miScorer);
			final PeakPosteriorScorer<FormulaClustering> scorer = new PeakPosteriorScorer<FormulaClustering>(scorers, parameters);
			Clusterer<FormulaData,FormulaClustering> clusterer = new FormulaClusterer(data, parameters, random, inScorer,
					outScorer, measure, adducts, scorer, miScorer);

			final List<SampleHandler<FormulaData,FormulaClustering>> handlers = new ArrayList<SampleHandler<FormulaData,FormulaClustering>>();
			/*
			final String[] identificationRules = options.identificationRules.split(",");
			for ( String idRule : identificationRules ) {
				handlers.add(getIdRule(idRule, moleculeNames, theoreticalSpectrums, polarities, adducts));
			}
			*/
			//handlers.add(getIdRule("mostLikelyPeak", moleculeNames, theoreticalSpectrums, polarities, adducts));
			//final List<String> moleculeNames = theoreticalSpectrums.getNames();
			for ( int neededSupport = 1; neededSupport <= options.identificationPeaks; ++neededSupport ) {
				handlers.add(new IdentifyingSampleHandler.IdentifyNthLikelyPeaks(moleculeNames, theoreticalSpectrums, data.polarities, adducts, neededSupport));
			}
			// This is the main call into the sampler
			
			basepeaks = Clusterer.findRelatedPeaks(peaks, clusterer, random, handlers);
			final PrintStream out = options.sampleOut == null ? System.out : new PrintStream(options.sampleOut);
			out.print("number,name");
			for (int i = 1; i <= options.identificationPeaks; ++i ) {
				out.print(",id" + i);
			}
			out.println();
			//System.out.println("name," + options.identificationRules);
			for (int formula = 0; formula < databaseSize; ++formula) {
				final List<String> line = new ArrayList<String>();
				line.add(Integer.toString(formula));
				line.add(moleculeNames.get(formula));
				for ( SampleHandler<?,?> handler : handlers) {
					line.add(handler.output(formula));
				}
				final String output = Common.join(line, ",");
				out.println(output);
			}

		}
		return basepeaks;
	}
	/*
	private static SampleHandler<FormulaClustering> getIdRule(final String idRuleName, final List<String> moleculeNames,
			final List<GeneralMassSpectrum> theoreticalSpectrums, final Polarity[] polarities, final String[] adducts) {
		if ("singlePeak".equals(idRuleName)) {
			return new IdentifyingSampleHandler.IdentifyOnePeak(moleculeNames, theoreticalSpectrums);
		}
		if ("mostLikelyPeak".equals(idRuleName)) {
			return new IdentifyingSampleHandler.IdentifyMostLikelyPeak(moleculeNames, theoreticalSpectrums, polarities, adducts);
		}
		if ("mostAndNextLikelyPeak".equals(idRuleName)) {
			return new IdentifyingSampleHandler.IdentifyMostAndNextLikelyPeak(moleculeNames, theoreticalSpectrums, polarities, adducts);
		}
		assert false;
		return null;
	}
	*/
	private static void labelRelationships(final IPeakSet<IPeak> peaks, final Options options) {
		// locate the relationships
		if (options.verbose)
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
						double delta = PeriodicTable.PPM(mass, options.ppm);
						
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
			findIsotopes(basepeak, charge, related, options.ppm, 1);
			
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
					if (PeriodicTable.inRange(basepeakmass/z, mass, options.ppm))
					{
						peak.addAnnotation(Annotation.relationship, "multi-charged (" + z + ")");
						
						// check for isotopes
						findIsotopes(peak, z, related, options.ppm, 1);
					}
				}
				
				// *-mer's
				for (int i=2; i<10; ++i)
				{
					double mermass = i*basepeakmass;
					if (PeriodicTable.inRange(mermass, mass, options.ppm))
					{
						peak.addAnnotation(Annotation.relationship, String.format("%d-mer", i));
					
						// check for isotopes
						findIsotopes(peak, 1, related, options.ppm, 1);
					}
				}
				
				// oxidatie
				if (PeriodicTable.inRange(basepeakmass+H2, mass, options.ppm))
				{
					peak.addAnnotation(Annotation.relationship, "oxidation (a = c+H2)");
					findIsotopes(peak, 1, related, options.ppm, 1);
				}
				if (PeriodicTable.inRange(basepeakmass-H2, mass, options.ppm))
				{
					peak.addAnnotation(Annotation.relationship, "oxidation (a = c-H2)");
					findIsotopes(peak, 1, related, options.ppm, 1);
				}
				
				// adducts
				for (PeriodicTable.Derivative derivative : PeriodicTable.adducts_positive)
				{
					if (derivative.adductdeduct==PeriodicTable.ADDUCT || derivative.adductdeduct==PeriodicTable.ADDUCT_DEDUCT)
					{
						if (PeriodicTable.inRange(basepeakmass+derivative.getMass(), mass, options.ppm))
						{
							peak.addAnnotation(Annotation.relationship, derivative.getName());
							findIsotopes(peak, 1, related, options.ppm, 1);
						}
					}
					if (derivative.adductdeduct==PeriodicTable.ADDUCT || derivative.adductdeduct==PeriodicTable.ADDUCT_DEDUCT)
					{
						if (PeriodicTable.inRange(basepeakmass-derivative.getMass(), mass, options.ppm))
						{
							peak.addAnnotation(Annotation.relationship, derivative.getName());
							findIsotopes(peak, 1, related, options.ppm, 1);
						}
					}
				}
				if (PeriodicTable.inRange(basepeakmass+H2O, mass, options.ppm))
				{
					peak.addAnnotation(Annotation.relationship, "plus h2o");
					findIsotopes(peak, 1, related, options.ppm, 1);
				}
				
				// deducts
				// -carboxyl group
				if (PeriodicTable.inRange(basepeakmass-NH3, mass, options.ppm))
				{
					peak.addAnnotation(Annotation.relationship, "minus nh3");
					findIsotopes(peak, 1, related, options.ppm, 1);
				}
				else if (PeriodicTable.inRange(basepeakmass-H2O, mass, options.ppm))
				{
					peak.addAnnotation(Annotation.relationship, "minus h2o");
					findIsotopes(peak, 1, related, options.ppm, 1);
				}
			}
			
			// try to find +h2
			if (related_peakset != null)
			{
				for (IPeak peak : related_peakset)
				{
					String mass = String.format("%5.2f", peak.getMass());
					
					for (IPeak neighbour : related_peakset.getPeaksOfMass(peak.getMass()+H2, PeriodicTable.PPM(peak.getMass(), options.ppm)))
					{
						neighbour.addAnnotation(Annotation.relationship, mass + "+H2");
						findIsotopes(neighbour, 1, related, options.ppm, 1);
					}
					for (IPeak neighbour : related_peakset.getPeaksOfMass(peak.getMass()-H2, PeriodicTable.PPM(peak.getMass(), options.ppm)))
					{
						neighbour.addAnnotation(Annotation.relationship, mass + "-H2");
						findIsotopes(neighbour, 1, related, options.ppm, 1);
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
					findIsotopes(peak, 1, related, options.ppm, 1);
				}
			}
		}
	}
	
	public static class PeakComparer implements IPeak.RelationCompare<IPeak> {
		private final HashMap<Integer,double[]> intensity_courses;
		private final Header header;
		private final CorrelationMeasure measure;
		private final boolean ignoreIntensity;
		private final double minCorrSignals;
		
		public PeakComparer(final HashMap<Integer,double[]> intensity_courses, final Header header,
				final CorrelationMeasure measure, final boolean ignoreIntensity, final double minCorrSignals) {
			this.intensity_courses = intensity_courses;
			this.header = header;
			this.measure = measure;
			this.ignoreIntensity = ignoreIntensity;
			this.minCorrSignals = minCorrSignals;
		}
		
		public boolean related(IPeak peak1, IPeak peak2)
		{
			// retrieve the intensity-courses
			double intensity_course1[] = intensity_courses.get(peak1.getPatternID());
			if (intensity_course1 == null)
			{
				intensity_course1 = getIntensityCourse(peak1, header);
				Statistical.normalize(intensity_course1);
				intensity_courses.put(peak1.getPatternID(), intensity_course1);
			}
			double intensity_course2[] = intensity_courses.get(peak2.getPatternID());
			if (intensity_course2 == null)
			{
				intensity_course2 = getIntensityCourse(peak2, header);
				Statistical.normalize(intensity_course2);
				intensity_courses.put(peak2.getPatternID(), intensity_course2);
			}
			assert intensity_course1.length == intensity_course2.length;
			
			double stddev_course1 = Statistical.stddev(intensity_course1);
			double stddev_course2 = Statistical.stddev(intensity_course2);
			if (stddev_course1<0.1 && stddev_course2>0.15)
				return false;
			double corr_course = Statistical.pearsonsCorrelation(intensity_course1, intensity_course2)[Statistical.PEARSON_CORRELATION];
			
			// retrieve the correlations of the signals
			IPeakSet<MassChromatogram<Peak>> peakset1 = (IPeakSet<MassChromatogram<Peak>>) peak1;
			IPeakSet<MassChromatogram<Peak>> peakset2 = (IPeakSet<MassChromatogram<Peak>>) peak2;
			
			double corr_signals[] = new double[header.getNrMeasurementInfos()];
			for (int i=0; i<header.getNrMeasurementInfos(); ++i)
			{
				MassChromatogram<Peak> mc1=null, mc2=null;
				MeasurementInfo measurement = header.getMeasurementInfo(i);
				int measurementid = measurement.getID();
				for (MassChromatogram<Peak> _mc1 : peakset1)
					if (measurementid == _mc1.getMeasurementID()) { mc1=_mc1; break; }
				for (MassChromatogram<Peak> _mc2 : peakset2)
					if (measurementid == _mc2.getMeasurementID()) { mc2=_mc2; break; }
				
				if (mc1==null || mc2==null)
					corr_signals[i] = 0;
				else {
					corr_signals[i] = measure.correlation(mc1.getSignal(), mc2.getSignal());
					//corr_signals[i] = mc1.getSignal().pearsonsCorrelation(mc2.getSignal())[Statistical.PEARSON_CORRELATION];
				}
			}
			
			// check whether the correlation is significant
			double max_corr_signals = Statistical.max(corr_signals);
			if (stddev_course1 < 0.1) {
				return max_corr_signals>minCorrSignals;
			} else if ( ignoreIntensity ) {
				//System.err.println("Ignoring intensity");
				return max_corr_signals>minCorrSignals;
			} else if ( intensity_course1.length == 1 ) {
				return max_corr_signals>minCorrSignals;
			} else {
				return max_corr_signals>minCorrSignals && corr_course>0.75;
			}
		}
	}
	
	public static class PearsonMeasure implements CorrelationMeasure {
		public double correlation(Signal signal1, Signal signal2) {
			if ( signal1 == null || signal2 == null ) {
				return 0.0;
			}
			return signal1.pearsonsCorrelation(signal2)[Statistical.PEARSON_CORRELATION];
		}
	}
	
	public static class CosineMeasure implements CorrelationMeasure {
		public double correlation(final Signal signal1, final Signal signal2) {
			final Pair<float[],float[]> signals = synchronizedValues(signal1, signal2);
			final float[] xvals = signals.v1;
			final float[] yvals = signals.v2;
			
			final float xy = dotProduct(xvals, yvals);
			final float xx = dotProduct(xvals, xvals);
			final float yy = dotProduct(yvals, yvals);
			assert ! Float.isNaN(xy);
			assert ! Float.isNaN(xx);
			assert ! Float.isNaN(yy);
			
			float corr = xy / (float)Math.sqrt(xx * yy);
			if ( xx == 0.0f || yy == 0.0f ) {
				corr = 0.0f;
			}
			assert ! Float.isNaN(corr) : xy + " " + xx + " " + yy;
			return corr;
		}
		
		private Pair<float[],float[]> synchronizedValues(final Signal signal1, final Signal signal2) {
			final Pair<float[],float[]> signal1values = preparedValues(signal1);
			final float[] signal1x = signal1values.v1;
			final float[] signal1y = signal1values.v2;
			
			final Pair<float[],float[]> signal2values = preparedValues(signal2);
			final float[] signal2x = signal2values.v1;
			final float[] signal2y = signal2values.v2;
			
			final int totalSize = signal1.getSize() + signal2.getSize();
			
			float xvals[] = new float[totalSize];
			float yvals[] = new float[totalSize];
			
			for (int i = 0; i < signal1x.length; ++i) {
				xvals[i] = signal1x[i];
				yvals[i] = getY(signal1x[i], signal2x, signal2y);
			}
			
			for (int i = 0; i < signal2x.length; ++i) {
				xvals[i + signal1x.length] = signal2x[i];
				yvals[i + signal1x.length] = getY(signal2x[i], signal1x, signal1y);
			}
			
			return new Pair<float[],float[]>(xvals, yvals);
		}
		
		private static Pair<float[],float[]> preparedValues(final Signal signal) {
			final Signal signalCopy = new Signal(signal);
			signalCopy.normalize();
			final double[][] signalvalues = signalCopy.getXY();
			final double[] signalx = signalvalues[0];
			final double[] signaly = signalvalues[1];
			final int[] signalIndices = Common.sortedIndices(signalx);
			final float[] sortedSignalx = new float[signalx.length];
			final float[] sortedSignaly = new float[signaly.length];
			for (int i = 0; i < signalx.length; ++i) {
				sortedSignalx[i] = (float)signalx[signalIndices[i]];
				sortedSignaly[i] = (float)signaly[signalIndices[i]];
			}
			return new Pair<float[],float[]>(sortedSignalx, sortedSignaly);
		}
		
		private static float getY(final float x, final float[] xvals, final float[] yvals) {
			if (x<xvals[0] || xvals[xvals.length-1]<x) {
				return 0.0f;
			}
		
			float ymin = 0.0f;
			float ymax = 0.0f;
			float xmin = 0.0f;
			float xmax = 0.0f;
			for (int i=0; i<xvals.length; ++i)
			{
				if (xvals[i] == x) {
					return yvals[i];
				} else if (xvals[i] < x) {
					ymin = yvals[i];
					xmin = xvals[i];
				} else {
					ymax = yvals[i];
					xmax = xvals[i];
					break;
				}
			}
			final float xdiff = xmax - xmin;
			final float otherdiff = x - xmin;
			final float ydiff = ymax - ymin;
			final float yval = otherdiff / xdiff * ydiff + ymin;
			
			return yval;
		}
		
		private float dotProduct(final float[] a, final float[] b) {
			assert a.length == b.length;
			
			float accum = 0.0f;
			
			for (int i = 0; i < a.length; ++i) {
				accum += a[i] * b[i];
			}
			return accum;
		}
		
	}
 }
