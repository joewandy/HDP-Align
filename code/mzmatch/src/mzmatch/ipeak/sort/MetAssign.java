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
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import mzmatch.ipeak.sort.FormulaClusterer.ScoringCache.NonZeros;
import mzmatch.ipeak.sort.MoleculeClustering.InitialClusteringMethod;
import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import mzmatch.ipeak.util.GeneralMassSpectrumDatabase;
import mzmatch.util.RandomSeedException;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Mass;
import peakml.chemistry.Molecule;
import peakml.chemistry.PeriodicTable;
import peakml.chemistry.Polarity;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.chemistry.MoleculeIO;
import peakml.io.chemistry.MoleculeIO.KeyValueContainer;
import peakml.io.peakml.PeakMLParser;
import peakml.io.peakml.PeakMLWriter;
import peakml.util.Pair;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

import com.google.common.base.Joiner;

import domsax.XmlParserException;

@SuppressWarnings("unchecked")
public class MetAssign
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
	final static String application = "MetAssign";
	@OptionsClass(name=application, version=version, author="R—n‡n Daly (Ronan.Daly@glasgow.ac.uk)",
		description=
		"LC-MS experiments yield large amounts of peaks, many of which correspond to derivatives of peaks " +
		"of interest (eg, isotope peaks, adducts, fragments, multiply charged molecules), termed here as " +
		"related peaks. This tools identifies peaks by grouping them together and assigning these groups" +
		"to compounds that have been specified in a database. At the same time, the probability of the" +
		"existence of each of the database entries is given. The results of the peak identification step" +
		"are given as an annotation " + AnnotationSampleHandler.filteredAnnotation + " on each peak of " +
		"the output file. The results of the compound identification step are output ",
		
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i raw\\*.mzXML -o peaks\\ -ppm 3\n" +
		"\n" +
		"REM combine all timepoints in a single file\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\*.peakml -o data.peakml -ppm 3 -rtwindow 30 -combination set\n" +
		"\n" +
		"REM detect the related peaks\n" +
		"%JAVA% mzmatch.ipeak.sort.MetAssign -v -i data.peakml -o identifiedPeaks.peakml -ppm 3 -numDraws 200 -burnIn 50 " +
		"-databases hmdb.xml,lipidmaps.xml -v -adducts M-H2O-H,M-H,M+Na-2H,M+Cl,M+K-2H,M+FA-H,2M-H,2M+FA-H -dbIdentOut " +
		"identifiedCompounds.tsv\n",
		references=
		""
	)
	
	public static class TestOptions {
		MoleculeClustering.InitialClusteringMethod clusteringMethod = InitialClusteringMethod.SinglePeakClusters;
		int fixedClustersSize = 200;
		final Set<String> peakLikelihoodTrace = new HashSet<String>();
		final List<String> moleculeLikelihoodTrace = new ArrayList<String>();
		double kappa0 = 1e-5;
		double intensityKappa0 = 1e-14;
		double intensityKappa = 1e-8;
		boolean sampleClusterMoleculeAssignments = true;
		boolean useRetentionTimePrediction = false;
		String connectivityNetwork = null;
		boolean alternativeSampleHandler = false;
		boolean outputNames = false;
		
		public static TestOptions parseTestOptions(Options op) {
			final TestOptions test = new TestOptions();
			if ( op.test == null ) {
				return test;
			}
			final String testOptions = op.test;
			
			final String[] options = testOptions.split(",");
			for ( String option : options ) {
				final String[] nameValue = option.split("=");
				final String name = nameValue[0];
				final String value = nameValue[1];
				
				if ( name.equals("initialClusteringMethod") ) {
					test.clusteringMethod = InitialClusteringMethod.valueOf(value);
				} else if ( name.equals("fixedClustersSize") ) {
					test.fixedClustersSize = Integer.parseInt(value);
				} else if (name.equals("likelihoodTrace") ) {
					final String[] nv = value.split("-");
					final String peaks = nv[0];
					final String molecules = nv[1];
					for ( String peak : peaks.split("\\+") ) {
						test.peakLikelihoodTrace.add(peak);
					}
					for ( String molecule : molecules.split("\\+") ) {
						test.moleculeLikelihoodTrace.add(molecule);
					}
				} else if (name.equals("kappa0") ) {
					test.kappa0 = Double.parseDouble(value);
				} else if (name.equals("intensityKappa0") ) {
					test.intensityKappa0 = Double.parseDouble(value);
				} else if (name.equals("intensityKappa") ) {
					test.intensityKappa = Double.parseDouble(value);
				} else if (name.equals("sampleClusterMoleculeAssignments") ) {
					test.sampleClusterMoleculeAssignments = Boolean.parseBoolean(value);
				} else if (name.equals("useRetentionTimePrediction") ) {
					test.useRetentionTimePrediction = Boolean.parseBoolean(value);
				} else if (name.equals("connectivityNetwork") ) {
					test.connectivityNetwork = value;
				} else if (name.equals("alternativeSampleHandler") ) {
					test.alternativeSampleHandler = Boolean.parseBoolean(value);
				} else if (name.equals("outputNames") ) {
					test.outputNames = Boolean.parseBoolean(value);
				} else {
					throw new RuntimeException("Don't recognise test option: " + name + " with value: " + value);
				}
			}
			return test;
		}
		
		public String toString() {
			return String.format("initialClusteringMethod: %s fixedClustersSize: %d", clusteringMethod, fixedClustersSize);
		}

	}
	
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
			"This should be set to parts-per-million accuracy of the MS equipment. The probability " + 
			"distributions over the theoretical peaks have been defined so that 95% of the probability " +
			"mass is covered by this value in each direction.")
		public double ppm = -1;
		
		//@Option(name="rtwindow", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
		//	"The retention time window in seconds, defining the range where to look for matches.")
		public double rtwindow = 0;
		
		//@Option(name="minrt", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
		//	"Denotes the minimum retention time in seconds peaks should occur before being taken " +
		//	"into account for the relation process. When this value is not set all peaks are " +
		//	"taken into account.")
		public double minrt = -1;
		
		//@Option(name="ignoreIntensity", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
		//	"Ignore the intensity correlation across profiles")
		public boolean ignoreIntensity = false;
		
		//@Option(name="minCorrSignals", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
		//	"The minimum correlation value for two chromatographic peaks to be correlated.")
		public double minCorrSignals = 0.75;
		
		//@Option(name="measure", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
		//	"The measure of correlations between peaks. Valid options are pearson and cosine."
		//)
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
		public double alpha = 1000.0;
		
		@Option(name="alpha0", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for BetaBeta mixture model clustering."
		)
		public double alpha0 = 2.0;
		
		@Option(name="alpha1", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for BetaBeta mixture model clustering."
		)
		public double alpha1 = 10.0;
		
		@Option(name="numDraws", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"This parameter says how many posterior samples to take. Generally 200 samples " +
			"should be sufficient for most uses. For data with very large numbers of peaks, " +
			"more samples might be needed, perhaps up to 500."
		)
		public int numDraws = 200;
		
		@Option(name="burnIn", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"This parameters says how many initial samples should be discarded before saving " +
			"posterior samples. For larger datasets, it is recommended to set this to 200."
		)
		public int burnIn = 200;
		
		//@Option(name="initialNumClusters", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
		//	"Parameter for mixture model clustering."
		//)
		public int initialNumClusters = 10;
		
		@Option(name="databases", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
				"Parameter for optional compound databases to match against during mixture model clustering."
		)
		public Vector<String> databases = new Vector<String>();
		
		@Option(name="adducts", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"This is a comma separated list of adduct types that will be used in the generation " +
			"of theoretical peaks. Whilst an exhaustive list can be provided, it is better to " +
			"stick with those adducts that are known to be generated, as spurious adducts can " +
			"generate more false positives."
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
			"This parameter describes the spread of LC/MS peaks that are generated by a " +
			"single chromatographic peak and should be set so that the retention time of " +
			"most of the peaks would be within 2 times this value, of the chromatographic " +
			"retention time. This can vary widely because of difficulties in detecting " +
			"accurate retention times from noisy peaks."
		)
		public double retentionTimeSD = 1.0;
		
		@Option(name="retentionTimePredSD", param="float", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The spread of possible retention time values from a theoretical value"
		)
		public double retentionTimePredSD = 300.0;
		
		@Option(name="identificationPeaks", param="int", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"This parameter says to output compound identifications at support levels 1 to this number"
		)
		public int identificationPeaks = 5;
		
		@Option(name="filterPPM", param="float", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"This is an optimisation measure to speed up processing by ignoring peaks that " +
			"are not closer than this value to a theoretical peak. Generally a value " +
			"between 1.1 * ppm and 1.5 * ppm should be appropriate."
		)
		public double filterPPM = 3.5;
		
		@Option(name="rtClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"A flag to specify whether clustering using retention time should be used"
		)
		public boolean rtClustering = true;
		
		//@Option(name="corrClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, usage=
		//	"A flag to specify whether clustering using peak shape correlations should be used"
		//)
		public boolean corrClustering = false;
		
		@Option(name="seed", param="long", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Random number generator seed"
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
		
		@Option(name="dbIdentOut", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Where compound identification results are stored."
		)
		public String dbIdentOut = null;
		
		@Option(name="test", param="string", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Options for testing purposes"
		)
		public String test = null;

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
				if (options.dbIdentOut != null)
					Tool.createFilePath(options.dbIdentOut, true);
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
			
			final Random random = new Random();
			seed = options.seed == -1 ? random.nextLong() : options.seed;
			random.setSeed(seed);
			//if (options.verbose)
			System.err.println("Random seed is: " + seed);
			Vector<IPeak> basepeaks = null;
			CorrelationMeasure measure = null;

			if ( "pearson".equals(options.measure) ) {
				measure = new PeakComparer.PearsonMeasure();
			} else if ( "cosine".equals(options.measure) ) {
				measure = new PeakComparer.CosineMeasure();
			}
			assert measure != null;

			basepeaks = modelBasedClustering(options, peaks, header, random, measure);
			assert basepeaks != null;
			
			RelatedPeaks.labelRelationships(peaks, options.verbose, options.ppm);

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
		
		final List<peakml.util.Pair<String, Molecule>> molecules = new ArrayList<Pair<String,Molecule>>();
		final KeyValueContainer<String,Molecule> adapter = new MoleculeIO.KeyValueContainerListAdapter<String,Molecule>(molecules);
		for (String file : options.databases)
			MoleculeIO.parseXml(new FileInputStream(file), adapter);

		Clusterer.LikelihoodScorer inScorer = new Clusterer.BetaInLikelihoodScorer(options.p1, options.alpha1, rangeMin, rangeMax);
		Clusterer.LikelihoodScorer outScorer = new Clusterer.BetaOutLikelihoodScorer(options.p0, options.alpha0, rangeMin, rangeMax);

		Vector<IPeak> basepeaks;
		final TestOptions testOptions = TestOptions.parseTestOptions(options);
		if ( options.verbose ) {
			System.err.println(testOptions);
		}

		if ( molecules.size() == 0 ) {
			System.err.println("No database, so only performing clustering");
			final Data data = new Data(header, peaks);
			final CorrelationParameters parameters = new CorrelationParameters(options.rtwindow, options.p1, options.p0,
					options.alpha, options.numDraws, options.burnIn, options.retentionTimeSD, options.debug,
					options.initialNumClusters, testOptions);
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
			final List<String> moleculeRealNames = new ArrayList<String>();
			final int mSize = molecules.size();
			
			for ( int ei = 0; ei < mSize; ++ei ) {
				final Pair<String, Molecule> entry = molecules.get(ei);
				moleculeNames.add(entry.v1);
				moleculeRealNames.add(entry.v2.getName());
			}
			
			final String[] adducts = options.adducts.split(",");
			GeneralMassSpectrumDatabase theoreticalSpectrums = new GeneralMassSpectrumDatabase(molecules, adducts,
					options.minDistributionValue, options.maxValues);

			final int originalDatabaseSize = theoreticalSpectrums.size();
			if ( options.filterPPM > 0.0 ) {
				final MoleculeData tempData = new MoleculeData(header, peaks, theoreticalSpectrums);
				final Polarity[] polarities = tempData.polarities;
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
					for ( int msi = 0; msi < msSize; ++msi ) {
						final GeneralMassSpectrum ms = tempData.theoreticalSpectrums.get(msi);
						final List<Polarity> theoreticalPolarities = ms.getPolarities();
						final List<Double> masses = ms.getMasses();
						
						for ( int theoPeak = 0; theoPeak < masses.size(); ++theoPeak) {
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
							}
						}

					}
					if ( found ) {
						filterList.add(thePeak);
					} else {
						thePeak.addAnnotation(IPeak.relationid, "-1", Annotation.ValueType.INTEGER);
					}
					
				}
				final int oldPeaksSize = peaks.size();
				peaks = new IPeakSet<IPeak>(filterList);
				final List<Integer> spectrumSubset = new ArrayList<Integer>();
				for ( int i = 0; i < foundMono.length; ++i ) {
					if ( foundMono[i] ) {
						spectrumSubset.add(i);
					}
				}
				final GeneralMassSpectrumDatabase newDatabase = theoreticalSpectrums.getSubset(spectrumSubset);
				System.err.println("Old database size: " + theoreticalSpectrums.size() + " new database size: " + newDatabase.size());
				System.err.println("Old number of peaks: " + oldPeaksSize + " new number of peaks: " + peaks.size());
				theoreticalSpectrums = newDatabase;
			}

			final MoleculeData data = new MoleculeData(header, peaks, theoreticalSpectrums);
			final PrintStream dout = options.debugOut == null ? System.err : new PrintStream(options.debugOut);
			
			final MoleculeParameters parameters = new MoleculeParameters(options.rtwindow, options.p1, options.p0, options.alpha,
					options.numDraws, options.burnIn, options.debug, dout, options.minDistributionValue, options.maxValues,
					options.ppm, options.rho, options.retentionTimeSD, options.retentionTimePredSD, testOptions);

			final List<PeakLikelihoodScorer<MoleculeClustering>> scorers = new ArrayList<PeakLikelihoodScorer<MoleculeClustering>>();
			if ( options.corrClustering ) {
				scorers.add(new CorrelationClusterer.ClusteringScorer<MoleculeClustering>(data, parameters, inScorer, outScorer, measure));
			}
			final RetentionTimeClusteringScorer<MoleculeClustering> rtScorer =
					new RetentionTimeClusteringScorer<MoleculeClustering>(data, parameters);
			if ( options.rtClustering ) {
				scorers.add(rtScorer);
			}
			final FormulaClusterer.MassIntensityClusteringScorer miScorer = new FormulaClusterer.MassIntensityClusteringScorer(
					data, parameters, inScorer, outScorer, measure, adducts, random);
			scorers.add(miScorer);
			final PeakPosteriorScorer<MoleculeClustering> scorer = new PeakPosteriorScorer<MoleculeClustering>(scorers, parameters);
			Clusterer<MoleculeData,MoleculeClustering> clusterer = new MoleculeClusterer(data, parameters, random, inScorer,
					outScorer, measure, adducts, scorer, miScorer, rtScorer);

			final List<SampleHandler<MoleculeData,MoleculeClustering>> allHandlers = new ArrayList<SampleHandler<MoleculeData,MoleculeClustering>>();
			final List<IdentifyingSampleHandler> identifyingHandlers = new ArrayList<IdentifyingSampleHandler>();
			
			for ( int neededSupport = 1; neededSupport <= options.identificationPeaks; ++neededSupport ) {
				final IdentifyingSampleHandler h = new IdentifyingSampleHandler.IdentifyNthLikelyPeaks(moleculeNames,
						theoreticalSpectrums, data.polarities, adducts, neededSupport, parameters.options.alternativeSampleHandler);
				allHandlers.add(h);
				identifyingHandlers.add(h);
			}
			final AnnotationSampleHandler ah = new AnnotationSampleHandler.IdentifyingAnnotationSampleHandler(moleculeNames,
					theoreticalSpectrums, data.polarities, adducts, peaks, parameters);
			allHandlers.add(ah);

			// This is the main call into the sampler
			priorMassLikelihoodAnnotations(miScorer, peaks, theoreticalSpectrums, adducts);
			
			basepeaks = Clusterer.findRelatedPeaks(peaks, clusterer, random, allHandlers);
			final PrintStream out = options.dbIdentOut == null ? System.out : new PrintStream(options.dbIdentOut);
			//out.print("id\tcompoundId\tcompoundName");
			out.print("compoundId\tcompoundName");
			for (int i = 1; i <= options.identificationPeaks; ++i ) {
				out.print("\tp." + i);
			}
			out.print("\tp.combined");
			out.println();
			final DecimalFormat format = new DecimalFormat("0.0###");
			for (int formula = 0; formula < originalDatabaseSize; ++formula) {
				final List<String> line = new ArrayList<String>();
				//line.add(Integer.toString(formula));
				line.add(moleculeNames.get(formula));
				line.add(moleculeRealNames.get(formula));
				double runningTotal = 0.0;
				for ( IdentifyingSampleHandler handler : identifyingHandlers) {
					final String posterior = handler.output(formula);
					line.add(handler.output(formula));
					runningTotal += Double.parseDouble(posterior);
				}
				final double mean = runningTotal / identifyingHandlers.size();
				line.add(format.format(mean));
				final String output = Common.join(line, "\t");
				out.println(output);
			}
			ah.writeAnnotations();
		}
		return basepeaks;
	}
	
	private static void priorMassLikelihoodAnnotations(final FormulaClusterer.MassIntensityClusteringScorer miScorer,
			IPeakSet<IPeak> peaks, final GeneralMassSpectrumDatabase theoreticalSpectrums, final String[] adducts) {
		for ( int peak = 0; peak < peaks.size(); ++peak ) {
			final NonZeros nz = miScorer.massCache.getNonZeros(peak);
			final int numValues = nz.formulaList.size();
			if ( numValues == 0 ) {
				continue;
			}
			final double[] normalisedDistribution = Clusterer.normaliseDistribution(nz.valueList.elements(), -1, numValues);
			final List<String> outputs = new ArrayList<String>();
			
			for ( int i = 0; i < numValues; ++i ) {
				final int molecule = nz.formulaList.get(i);
				final int position = nz.positionList.get(i);
				final double value = normalisedDistribution[i];
				if ( value < 0.01 ) {
					continue;
				}
				
				AnnotationSampleHandler.doAnnotation(molecule, position, theoreticalSpectrums, value, adducts, outputs);
			}
			final String output = Joiner.on("; ").join(outputs);
			peaks.get(peak).addAnnotation("priorIdentification", output);
		}
		
	}
}