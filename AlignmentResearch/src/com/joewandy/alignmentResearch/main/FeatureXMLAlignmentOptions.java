package com.joewandy.alignmentResearch.main;

import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;

@OptionsClass(
		name = FeatureXMLAlignmentOptions.APPLICATION, 
		version = FeatureXMLAlignmentOptions.VERSION, 
		author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", 
		description = "A simple feature-based alignment pipeline.")
public class FeatureXMLAlignmentOptions {

	public static final String VERSION = "1.0";

	public static final String APPLICATION = "FeatureXMLAlignment";

	@Option(name = "d", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The directory of input file in the FeatureML file format.")
	public String inputDirectory = null;

	@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The output file in text format.")
	public String output = null;

	@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the help is shown.")
	public boolean help = false;

	@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
	public boolean verbose = false;

	@Option(name = "useGroup", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
	public boolean grouping = false;

	@Option(name = "useGtFeaturesOnly", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
	public boolean useGtFeaturesOnly = false;

	@Option(name = "dataType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "greedy or mzMine join aligner.")
	public String dataType = AlignmentDataGeneratorFactory.ALIGNMENT_DATA_BENCHMARK;
	
	@Option(name = "experimentType", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "greedy or mzMine join aligner.")
	public String experimentType = "";	
	
	@Option(name = "method", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "greedy or mzMine join aligner.")
	public String method = AlignmentMethodFactory.ALIGNMENT_METHOD_BASELINE;

	@Option(name = "graphFilter", param = "", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Graph filter method of matched peaks.")
	public String graphFilter = null;

	@Option(name = "th", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Graph filter threshold of matched peaks.")
	public double th = -1;

	@Option(name = "alignmentPpm", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The accuracy of the measurement in parts-per-milion. This value is used for the "
			+ "matching of mass chromatogram (collections) and needs to be reasonable for the equipment "
			+ "used to make the measurement (the LTQ-Orbitrap manages approximately 3 ppm).")
	public double alignmentPpm = -1;

	@Option(name = "alignmentRtwindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The retention time window in seconds, defining the range where to look for matches.")
	public double alignmentRtwindow = -1;

	@Option(name = "groupingRtwindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The retention time window in seconds, defining the range where to look for matches.")
	public double groupingRtwindow = -1;

	@Option(name = "gt", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The ground truth file for these data.")
	public String gt = null;

	@Option(name = "rtDriftMean", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Constant rt drift in seconds to add to all features' rt (and gt too).")
	public double rtDriftMean = 0;

	@Option(name = "rtDriftVariance", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Constant rt drift in seconds to add to all features' rt (and gt too).")
	public double rtDriftVariance = 0;

}
