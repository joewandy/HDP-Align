package com.joewandy.alignmentResearch.main;

import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;

@OptionsClass(
		name = MultiAlignCmdOptions.APPLICATION, 
		version = MultiAlignCmdOptions.VERSION, 
		author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", 
		description = "A simple feature-based alignment pipeline.")
public class MultiAlignCmdOptions {

	public static final String VERSION = "1.0";

	public static final String APPLICATION = "MultiAlignCmd";

	/*
	 * Basic options
	 */
	
	@Option(name = "d", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The directory of input file in the FeatureML file format.")
	public String inputDirectory = null;

	@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The output file in text format.")
	public String output = null;

	@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the help is shown.")
	public boolean help = false;

	@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
	public boolean verbose = false;
	
	/*
	 * Data type options
	 */
	
	@Option(name = "dataType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Read from file or generate data.")
	public String dataType = AlignmentDataGeneratorFactory.FEATURE_XML_DATA;

	@Option(name = "gt", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The ground truth file for these data, if any.")
	public String gt = null;
	
	@Option(name = "measureType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Which way to compute performance measures: lange or set combination")
	public String measureType = MultiAlignConstants.PERFORMANCE_MEASURE_COMBINATION;	

	@Option(name = "gtCombinationSize", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Size for the k-combinations to enumerate from the ground truth")
	public int gtCombinationSize = 2;
	
	/*
	 * Grouping experiment options
	 */
	
	@Option(name = "experimentType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "greedy or mzMine join aligner.")
	public String experimentType = null;	

	@Option(name = "experimentIter", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "No. of iterations")
	public int experimentIter = 1;

	@Option(name = "trainingIndex", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Index of training-testing set")
	public int trainingIndex = -1;
	
	@Option(name = "autoAlpha", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, automatically adjust alpha from 0 to 1.")
	public boolean autoAlpha = false;

	@Option(name = "autoOptimiseGreedy", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, automatically tries to some combinations of grouping rt windows")
	public boolean autoOptimiseGreedy = false;

	
	/*
	 * Common alignment parameters
	 */
	
	@Option(name = "method", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Which alignment method to choose.")
	public String method = AlignmentMethodFactory.ALIGNMENT_METHOD_PYTHON_MW;

	@Option(name = "alignmentMzTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The accuracy of the m/z measurement, usually in parts-per-milion. This value is used for the "
			+ "matching of mass chromatogram (collections) and needs to be reasonable for the equipment "
			+ "used to make the measurement (the LTQ-Orbitrap manages approximately 3 ppm).")
	public double alignmentMzTol = -1;

	@Option(name = "alignmentRtTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The retention time window in seconds, defining the range where to look for matches.")
	public double alignmentRtTol = -1;
	
	@Option(name="usePpm", param="boolean", type=Option.Type.NO_ARGUMENT, 
			usage="Whether to use calculate mass difference in parts-per-million (ppm) or absolute value")
	public boolean usePpm = MultiAlignConstants.USE_PPM;
	
	/*
	 * RANSAC options
	 */
	
	@Option(name = "ransacRtToleranceBeforeCorrection", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacRtToleranceBeforeCorrection = MultiAlignConstants.PARAM_RT_TOLERANCE_BEFORE_CORRECTION;

	@Option(name = "ransacIteration", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public int ransacIteration = MultiAlignConstants.PARAM_RANSAC_ITERATION;
	
	@Option(name = "ransacNMinPoints", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacNMinPoints = MultiAlignConstants.PARAM_MINIMUM_NO_OF_POINTS;

	@Option(name = "ransacThreshold", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacThreshold = MultiAlignConstants.PARAM_THRESHOLD_VALUE;
	
	@Option(name="ransacLinearModel", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Ransac parameter")
	public boolean ransacLinearModel = MultiAlignConstants.PARAM_LINEAR_MODEL;

	@Option(name="ransacSameChargeRequired", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Ransac parameter")
	public boolean ransacSameChargeRequired = MultiAlignConstants.PARAM_REQUIRE_SAME_CHARGE_STATE;

	/*
	 * OpenMS options
	 */
	
	@Option(name = "mzPairMaxDistance", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "OpenMS parameter.")
	public double openMsMzPairMaxDistance = MultiAlignConstants.PARAM_MZ_PAIR_MAX_DISTANCE;

	/*
	 * Max-weight matching options
	 */
	
	@Option(name="useGroup", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Whether to use grouping")
	public boolean useGroup = MultiAlignConstants.USE_GROUP;

	@Option(name="exactMatch", param="boolean", type=Option.Type.NO_ARGUMENT, 
			usage="Exact or approximate matching ?")
	public boolean exactMatch = MultiAlignConstants.EXACT_MATCH;
	
	@Option(name="usePeakShape", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Whether to use peak shape correlation when grouping")
	public boolean usePeakShape = MultiAlignConstants.USE_PEAK_SHAPE;
	
	@Option(name = "minCorrSignal", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Min corr threshold for greedy peakshape grouping")
	public double minCorrSignal = MultiAlignConstants.GROUPING_MIN_CORR_SIGNAL;
	
	@Option(name = "alpha", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Controls the ratio of weights used in similarity calculation during matching.")
	public double alpha = MultiAlignConstants.PARAM_ALPHA;
	
	/*
	 * Grouping options
	 */
	
	// greedy grouping or DP model-based grouping

	@Option(name = "groupingMethod", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Grouping method")
	public String groupingMethod = MultiAlignConstants.GROUPING_METHOD_GREEDY;
	
	@Option(name = "groupingNSamples", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "No. of samples")
	public int groupingNSamples = MultiAlignConstants.GROUPING_METHOD_NUM_SAMPLES;

	@Option(name = "groupingBurnIn", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "No. of burn-in samples")
	public int groupingBurnIn = MultiAlignConstants.GROUPING_METHOD_BURN_IN;
	
	@Option(name="alwaysRecluster", param="boolean", type=Option.Type.NO_ARGUMENT, 
			usage="Always recluster instead of using previous clustering results")
	public boolean alwaysRecluster = MultiAlignConstants.ALWAYS_RECLUSTER;
	
	// for greedy grouping
	
	@Option(name = "groupingRtWindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Grouping RT window for greedy grouping")
	public double groupingRtWindow = MultiAlignConstants.GROUPING_METHOD_RT_TOLERANCE;

	// for model-based grouping using DP

	@Option(name = "groupingDpAlpha", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "DP concentration param")
	public double groupingDpAlpha = MultiAlignConstants.GROUPING_METHOD_ALPHA;
		
	// for model-based grouping using HDP
	@Option(name = "hdpAlphaRt", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "HDP DP concentraton parameter for local RT cluster")
	public double hdpAlphaRt = MultiAlignConstants.HDP_ALPHA_RT;

	@Option(name = "hdpTopAlpha", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "HDP DP concentraton parameter for global RT cluster")
	public double hdpTopAlpha = MultiAlignConstants.HDP_TOP_ALPHA;
	
	@Option(name = "hdpAlphaMass", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "HDP DP concentraton parameter for mass cluster")
	public double hdpAlphaMass = MultiAlignConstants.HDP_ALPHA_MASS;

	@Option(name = "hdpGlobalRtClusterStdev", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "HDP global RT cluster standard deviation")
	public double hdpGlobalRtClusterStdev = MultiAlignConstants.HDP_GLOBAL_RT_CLUSTER_STDEV;

	@Option(name = "hdpLocalRtClusterStdev", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "HDP local RT cluster standard deviation")
	public double hdpLocalRtClusterStdev = MultiAlignConstants.HDP_LOCAL_RT_CLUSTER_STDEV;
	
	@Option(name = "hdpMassTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "HDP mass cluster tolerance (in ppm)")
	public double hdpMassTol = MultiAlignConstants.HDP_MASS_TOLERANCE;
		
	@Option(name="hdpSpeedUp", param="boolean", type=Option.Type.NO_ARGUMENT, 
			usage="Enable various experimental speed-up hacks")
	public boolean hdpSpeedUp = MultiAlignConstants.HDP_SPEED_UP;

	@Option(name = "hdpSpeedUpNumSample", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Optional. No. of samples a peak can stay in a singleton cluster before removed from the model")
	public int hdpSpeedUpNumSample = MultiAlignConstants.HDP_SPEED_UP_NUM_SAMPLE;

	@Option(name = "hdpRefFileIdx", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Optional. Reference file index.")
	public int hdpRefFileIdx = MultiAlignConstants.HDP_REF_FILE_IDX;

	@Option(name = "hdpClusteringResultsPath", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The output file for HDP clustering results.")
	public String hdpClusteringResultsPath = null;	

	@Option(name = "mode", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Ionisation mode. If this is provided, then the model will also annotate peaks by possible adduct transformations.")
	public String mode = null;
	
	@Option(name = "idDatabase", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Identification database. If this is provided, then the model will also annotate peaks by putative identities of formulae")
	public String idDatabase = null;	

	@Option(name = "gtDatabase", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Ground truth database -- for debugging only")
	public String gtDatabase = null;		
	
	@Option(name = "scoringMethod", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Scoring method")
	public String scoringMethod = MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT_JAVA;	
	
	// for precursor clustering alignment

	@Option(name = "trans", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "transformation file")
	public String trans = null;	
	
	@Option(name = "withinFileMassTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "mass tolerance in ppm when binning within the same file")
	public double withinFileMassTol = MultiAlignConstants.PRECURSOR_WITHIN_FILE_MASS_TOL;

	@Option(name = "withinFileRtTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "rt tolerance in seconds when binning within the same file")
	public double withinFileRtTol = MultiAlignConstants.PRECURSOR_WITHIN_FILE_RT_TOL;
		
	@Option(name = "acrossFileMassTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "mass tolerance in ppm when binning across files")
	public double acrossFileMassTol = MultiAlignConstants.PRECURSOR_ACROSS_FILE_MASS_TOL;

	@Option(name = "acrossFileRtTol", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "rt tolerance in seconds when matching peak features across bins in the same cluster but coming from different files")
	public double acrossFileRtTol = MultiAlignConstants.PRECURSOR_ACROSS_FILE_MASS_TOL;
		
	@Option(name = "alphaMass", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Dirichlet parameter for precursor mass clustering")
	public double alphaMass = MultiAlignConstants.PRECURSOR_ALPHA_MASS;
	
	@Option(name = "alphaRt", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Dirichlet Process concentration parameter for mixture on RT")
	public double alphaRt = MultiAlignConstants.PRECURSOR_ALPHA_RT;
	
	@Option(name = "t", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "threshold for cluster membership for precursor mass clustering")
	public double t = MultiAlignConstants.PRECURSOR_T;
	
	@Option(name = "massClusteringNoIters", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "no. of iterations for VB precursor clustering")
	public int massClusteringNoIters = MultiAlignConstants.PRECURSOR_MASS_CLUSTERING_NO_ITERS;
	
	@Option(name = "rtClusteringNsamps", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "no. of total samples for Gibbs RT clustering")
	public int rtClusteringNsamps = MultiAlignConstants.PRECURSOR_RT_CLUSTERING_NSAMPS;
	
	@Option(name = "rtClusteringBurnIn", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "no. of burn-in samples for Gibbs RT clustering")
	public int rtClusteringBurnIn = MultiAlignConstants.PRECURSOR_RT_CLUSTERING_BURNIN;

	@Option(name = "matchMode", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "matching mode")
	public int matchMode = MultiAlignConstants.PRECURSOR_MATCH_MODE;
	
	@Option(name = "seed", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "random seed for debugging")
	public int seed = MultiAlignConstants.SEED;	
		
	/*
	 * Generative model parameters
	 */
	public GenerativeModelParameter generativeParams = new GenerativeModelParameter();

	public static AlignmentMethodParam getAlignmentMethodParam(MultiAlignCmdOptions options) {
		AlignmentMethodParam param = new AlignmentMethodParam();
		param.setUsePpm(options.usePpm);
		param.setMassTolerance(options.alignmentMzTol);
		param.setRtTolerance(options.alignmentRtTol);
		param.setUseGroup(options.useGroup);
		param.setExactMatch(options.exactMatch);
		param.setUsePeakShape(options.usePeakShape);
		param.setMinCorrSignal(options.minCorrSignal);
		param.setRansacRtToleranceBeforeMinute(options.ransacRtToleranceBeforeCorrection);
		param.setRansacRtToleranceAfterMinute(options.alignmentRtTol);
		param.setRansacIteration(options.ransacIteration);
		param.setRansacNMinPoints(options.ransacNMinPoints);
		param.setRansacThreshold(options.ransacThreshold);
		param.setRansacLinearModel(options.ransacLinearModel);
		param.setRansacSameChargeRequired(options.ransacSameChargeRequired);
		param.setOpenMsMzPairMaxDistance(options.openMsMzPairMaxDistance);
		param.setGroupingNSamples(options.groupingNSamples);
		param.setGroupingBurnIn(options.groupingBurnIn);
		param.setAlpha(options.alpha);
		param.setGroupingMethod(options.groupingMethod);
		param.setGroupingRtTolerance(options.groupingRtWindow);
		param.setGroupingDpAlpha(options.groupingDpAlpha);	
		param.setHdpAlphaRt(options.hdpAlphaRt);
		param.setHdpAlphaMass(options.hdpAlphaMass);
		param.setHdpTopAlpha(options.hdpTopAlpha);
		param.setHdpGlobalRtClusterStdev(options.hdpGlobalRtClusterStdev);
		param.setHdpLocalRtClusterStdev(options.hdpLocalRtClusterStdev);
		param.setHdpMassTol(options.hdpMassTol);
		param.setHdpSpeedUp(options.hdpSpeedUp);
		param.setHdpSpeedUpNumSample(options.hdpSpeedUpNumSample);
		param.setHdpRefFileIdx(options.hdpRefFileIdx);
		param.setScoringMethod(options.scoringMethod);
		param.setIdentificationDatabase(options.idDatabase);
		param.setGroundTruthDatabase(options.gtDatabase);
		param.setMode(options.mode);
		param.setVerbose(options.verbose);
		param.setAlwaysRecluster(options.alwaysRecluster);
		param.setHdpClusteringResultsPath(options.hdpClusteringResultsPath);
		param.setTrans(options.trans);
		param.setWithinFileMassTol(options.withinFileMassTol);
		param.setWithinFileRtTol(options.withinFileRtTol);
		param.setAcrossFileMassTol(options.acrossFileMassTol);
		param.setAcrossFileRtTol(options.acrossFileRtTol);
		param.setAlphaMass(options.alphaMass);
		param.setAlphaRt(options.alphaRt);
		param.setT(options.t);
		param.setMassClusteringNoIters(options.massClusteringNoIters);
		param.setRtClusteringNsamps(options.rtClusteringNsamps);
		param.setRtClusteringBurnIn(options.rtClusteringBurnIn);
		param.setMatchMode(options.matchMode);
		
		param.setSeed(options.seed);
		return param;
	}
	
	
}
