package mzmatch.ipeak;

import java.util.Vector;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;

import cmdline.Option;
import cmdline.OptionsClass;

@OptionsClass(name = CombineOptions.APPLICATION, version = CombineOptions.VERSION, author = "RA Scheltema (r.a.scheltema@rug.nl)", 
		description = "Combines the contents of a set of PeakML files, containing either mass chromatograms "
		+ "or backgroundions at the lowest level (the signal of which is needed in order to make "
		+ "a correct assesment of similarness). The approach starts from the most intense, "
		+ "unprocessed peak in the complete set signals, covering all the measurements, and attempts "
		+ "to find all those signals from the other measurements falling within the mass window "
		+ "(option 'ppm') and the retention time window (option 'rtwindow'). The correct match from "
		+ "each measurement to the currently most intense signal is consequently identified by "
		+ "optimizing on the difference in area under the curve. Signals caused by the same analyte "
		+ "are expected to roughly have a similar shape and retention time. All matched signals are "
		+ "then clustered for the output and marked as processed, after which a new iteration is "
		+ "begun."
		+ "\n\n"
		+ "This tool can be used to iteratively compound files. For example, when analyzing a time series "
		+ "experiment, firstly the biological replicates of each timepoint can first be combined into "
		+ "a single file, the set of which can be labeled as biological replicates with the option "
		+ "'combination'. Additional filtering operations can be applied in order to the timepoint "
		+ "combinations before proceeding to combine all the timepoints in a final set.", example = "Windows batch-file:\n"
		+ "SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n"
		+ "\n"
		+ "REM extract all the mass chromatograms\n"
		+ "%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i raw\\*.mzXML -o peaks\\ -ppm 3\n"
		+ "\n"
		+ "REM combine the individual timepoints\n"
		+ "%JAVA% mzmatch.ipeak.Combine -v -i peaks\\24hr_*.peakml -o 24hr.peakml -ppm 3 -rtwindow 30 -combination biological\n"
		+ "%JAVA% mzmatch.ipeak.Combine -v -i peaks\\28hr_*.peakml -o 28hr.peakml -ppm 3 -rtwindow 30 -combination biological\n"
		+ "%JAVA% mzmatch.ipeak.Combine -v -i peaks\\32hr_*.peakml -o 32hr.peakml -ppm 3 -rtwindow 30 -combination biological\n"
		+ "\n"
		+ "REM combine all timepoints in a single file\n"
		+ "%JAVA% mzmatch.ipeak.Combine -v -i *hr.peakml -o timeseries.peakml -ppm 3 -rtwindow 30 -combination set\n")

public class CombineOptions {
	
	public static final String APPLICATION = "Combine";
	public static final String VERSION = "1.0.0";
	
	/*
	 * BASIC OPTIONS
	 */
	
	@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Option for the input files. Multiple files can be passed by separating them "
			+ "with a comma (ie ,) or the use of a name with a wildcard (eg samples_*hrs.xml). The "
			+ "only allowed file format is PeakML containing either mass chromatograms or backgroundions "
			+ "at the lowest level (ie the result of another Combine can be used).")
	public Vector<String> input = new Vector<String>();

	@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Option for the ouput file. The resulting matches are written to this file in the "
			+ "PeakML file format."
			+ "\n"
			+ "When this option has not been set the output is written to the standard output. Be sure "
			+ "to unset the verbose option when setting up a pipeline reading and writing from the "
			+ "standard in- and outputs.")
	public String output = null;

	@Option(name = "label", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Optional label for the set being made. The labels are stored in the header "
			+ "of the resulting file and used for display purposes.")
	public String label = null;

	@Option(name = "labels", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Optional labels for the input files. When these are used make sure to give "
			+ "as many labels as there are input files. The labels are stored in the header "
			+ "of the resulting file and used for display purposes.")
	public Vector<String> labels = new Vector<String>();

	@Option(name = "ppm", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "The accuracy of the measurement in parts-per-milion. This value is used for the "
			+ "matching of mass chromatogram (collections) and needs to bereasonable for the equipment "
			+ "used to make the measurement (the LTQ-Orbitrap manages approximately 3 ppm).")
	public double ppm = -1;

	@Option(name = "rtwindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "The retention time window in seconds, defining the range where to look for matches.")
	public double rtwindow = -1;
	
	@Option(name = "combination", param = "see description", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "- set\n"
			+ "  The files are to be combined as a true set.\n"
			+ "- technical\n"
			+ "  The files are to be combined as technical replicates.\n"
			+ "- biological\n"
			+ "  The files are to be combined as biological replicates.\n")
	public String combination = "";

	@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, 
			usage = "When this is set, the help is shown.")
	public boolean help = false;
	
	@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, 
			usage = "When this is set, the progress is shown on the standard output.")
	public boolean verbose = false;

	/*
	 * OPTIONS FOR VARIOUS OTHER ALIGNMENT ALGORITHMS THAT CAN BE CALLED
	 */
	
	@Option(name = "method", param = "string", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Which method to use to perform the alignment of matching peaksets. " +
					"Valid options are greedy, join, ransac, sima and groupingInfo (experimental).")
	public String method = "greedy";
	
	@Option(name = "ransacRtToleranceBeforeMinute", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacRtToleranceBeforeMinute = AlignmentMethodParam.PARAM_RT_TOLERANCE_BEFORE_CORRECTION;

	@Option(name = "ransacIteration", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public int ransacIteration = AlignmentMethodParam.PARAM_RANSAC_ITERATION;
	
	@Option(name = "ransacNMinPoints", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacNMinPoints = AlignmentMethodParam.PARAM_MINIMUM_NO_OF_POINTS;

	@Option(name = "ransacThreshold", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacThreshold = AlignmentMethodParam.PARAM_THRESHOLD_VALUE;
	
	@Option(name="ransacLinearModel", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Ransac parameter")
	public boolean ransacLinearModel = AlignmentMethodParam.PARAM_LINEAR_MODEL;

	@Option(name="ransacSameChargeRequired", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Ransac parameter")
	public boolean ransacSameChargeRequired = AlignmentMethodParam.PARAM_REQUIRE_SAME_CHARGE_STATE;
	
	/*
	 * OPTIONS FOR PEAK GROUPING
	 */

	@Option(name = "measure", param = "string", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "The measure of correlations between peaks. Valid options are pearson and cosine.")
	public String measure = "pearson";

	@Option(name = "p1", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for mixture model clustering.")
	public double p1 = 0.001f;

	@Option(name = "p0", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for mixture model clustering.")
	public double p0 = 0.97f;

	@Option(name = "alpha", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for mixture model clustering.")
	public double alpha = 1.0f;

	@Option(name = "alpha0", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for BetaBeta mixture model clustering.")
	public float alpha0 = 2.0f;

	@Option(name = "alpha1", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for BetaBeta mixture model clustering.")
	public float alpha1 = 10.0f;

	@Option(name = "numSamples", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for mixture model clustering.")
	public int numSamples = 20;

	@Option(name = "initialNumClusters", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for mixture model clustering.")
	public int initialNumClusters = 10;

	@Option(name = "compoundDatabases", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for optional compound database to match against during mixture model clustering.")
	public Vector<String> compoundDatabases = new Vector<String>();

	@Option(name = "seed", param = "long", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Parameter for mixture model clustering.")
	public long seed = -1;
	
	@Option(name="burnIn", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, 
			usage="The amount of initial samples to discard."
	)
	public int burnIn = 30;
	
	@Option(name="retentionTimeSD", param="float", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="The ratio of the maximum peak distribution to the baseline distribution"
	)
	public double retentionTimeSD = 2.5;
	
	@Option(name="rtClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="A flag to specify whether clustering using retention time should be used"
	)
	public boolean rtClustering = true;
	
	@Option(name="corrClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="A flag to specify whether clustering using peak shape correlations should be used"
	)
	public boolean corrClustering = true;		

	@Option(name="debug", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, 
			usage="Should debugging information be printed."
	)
	public boolean debug = true;		

}
