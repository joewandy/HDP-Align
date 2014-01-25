package com.joewandy.alignmentResearch.main;

import java.util.ArrayList;
import java.util.List;

import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;

@OptionsClass(
		name = MultiAlignCmdOptions.APPLICATION, 
		version = MultiAlignCmdOptions.VERSION, 
		author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", 
		description = "A simple feature-based alignment pipeline.")
public class MultiAlignCmdOptions {

	public static final String VERSION = "1.0";

	public static final String APPLICATION = "FeatureXMLAlignment";

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

	@Option(name = "dataType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "greedy or mzMine join aligner.")
	public String dataType = AlignmentDataGeneratorFactory.ALIGNMENT_DATA_BENCHMARK;
	
	@Option(name = "experimentType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "greedy or mzMine join aligner.")
	public String experimentType = "none";	

	@Option(name = "experimentIter", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "No. of iterations")
	public int experimentIter = 1;
		
	@Option(name = "gt", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The ground truth file for these data.")
	public String gt = null;
	
	/*
	 * Common alignment parameters
	 */
	
	@Option(name = "method", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Which alignment method to choose.")
	public String method = AlignmentMethodFactory.ALIGNMENT_METHOD_BASELINE;

	@Option(name = "alignmentPpm", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The accuracy of the measurement in parts-per-milion. This value is used for the "
			+ "matching of mass chromatogram (collections) and needs to be reasonable for the equipment "
			+ "used to make the measurement (the LTQ-Orbitrap manages approximately 3 ppm).")
	public double alignmentPpm = -1;

	@Option(name = "alignmentRtWindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The retention time window in seconds, defining the range where to look for matches.")
	public double alignmentRtWindow = -1;
	
	/*
	 * RANSAC options
	 */
	
	@Option(name = "ransacRtToleranceBeforeCorrection", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Ransac parameter.")
	public double ransacRtToleranceBeforeCorrection = AlignmentMethodParam.PARAM_RT_TOLERANCE_BEFORE_CORRECTION;

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
	 * OpenMS options
	 */
	
	@Option(name = "mzPairMaxDistance", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "OpenMS parameter.")
	public double openMsMzPairMaxDistance = AlignmentMethodParam.PARAM_MZ_PAIR_MAX_DISTANCE;

	/*
	 * Stable matching options
	 */
	
	@Option(name="useGroup", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Stable marriage - use cluster weight when scoring")
	public boolean useGroup = AlignmentMethodParam.USE_GROUP;

	@Option(name="usePeakShape", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
			usage="Whether to use peak shape correlation when grouping")
	public boolean usePeakShape = AlignmentMethodParam.USE_PEAK_SHAPE;
	
	@Option(name = "alpha", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
			usage = "Stable marriage weight parameter.")
	public double alpha = AlignmentMethodParam.PARAM_ALPHA;

	@Option(name = "autoAlpha", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, automatically adjust alpha from 0 to 1.")
	public boolean autoAlpha = false;

	@Option(name = "autoOptimiseGreedy", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, automatically tries to some combinations of grouping rt windows")
	public boolean autoOptimiseGreedy = false;
	
	// valid choices are defined in MultiAlign
	@Option(name = "groupingMethod", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Grouping method")
	public String groupingMethod = MultiAlign.GROUPING_METHOD_GREEDY;
	
	/*
	 * Stable matching options - for greedy RT grouping
	 */
	
	@Option(name = "groupingRtWindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Grouping RT window for greedy grouping")
	public double groupingRtWindow;

	/*
	 * Stable matching options - for mixture model RT grouping
	 */
	
	@Option(name = "groupingNSamples", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "No. of samples")
	public int groupingNSamples = MultiAlign.GROUPING_METHOD_NUM_SAMPLES;

	
	/*
	 * Graph filtering options
	 */
	
	@Option(name = "graphFilter", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Graph filter method of matched peaks.")
	public String graphFilter = null;

	@Option(name = "th", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Graph filter threshold of matched peaks.")
	public double th = -1;
		
}
