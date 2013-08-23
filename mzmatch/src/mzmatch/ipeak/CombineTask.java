package mzmatch.ipeak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Vector;

import mzmatch.ipeak.Combine.Options;
import mzmatch.ipeak.sort.CorrelationMeasure;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;

public interface CombineTask {
	
	public static final int NO_OF_ALIGNMENT_CLUSTERS = 40;
	public static final String MATLAB_OUTPUT_FILENAME = "test-3-mini.mat";
	public static final String MATLAB_OUTPUT_PATH = "/home/joewandy/Project/mixture_model";

	public static final int SELECT_N_CLUSTERS_FOR_PEAKS = 20;
	public static final int TOP_N_CLUSTERS_FOR_ALIGNMENT = Integer.MAX_VALUE;
	public static final int TOP_N_EDGES_FOR_GRAPH = Integer.MAX_VALUE;
	public static final int TOP_N_CLUSTERS_FOR_GRAPH = Integer.MAX_VALUE;
	
	public static final String GREEDY_GROUPING = "greedy_grouping";
	public static final String CORR_GROUPING = "corr_grouping";

	public static final String GREEDY_ALIGNMENT = "greedy_alignment";
	public static final String MODEL_BASED_ALIGNMENT = "model_alignment";
	public static final String EXPERIMENT_ALIGNMENT = "experiment_alignment";	
	public static final String INTERACTIVE_ALIGNMENT = "interactive_alignment";	

	public void process(final Options options, Header header,
			Vector<IPeakSet<IPeak>> peaksets, ParseResult[] results,
			final Random random, CorrelationMeasure measure, float rangeMin,
			float rangeMax, int totalPeaks, OutputStream output) throws IOException, FileNotFoundException;
	
}
