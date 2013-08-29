package mzmatch.ipeak.combineMethod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;
import java.util.Vector;

import mzmatch.ipeak.CombineOptions;
import mzmatch.ipeak.sort.CorrelationMeasure;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;

public interface CombineMethod {
	
	public static final String MATLAB_OUTPUT_FILENAME = "test-3-mini.mat";
	public static final String MATLAB_OUTPUT_PATH = "/home/joewandy/Project/mixture_model";
	
	public static final String GROUPING_GREEDY = "greedy_grouping";
	public static final String GROUPING_MIXTURE = "corr_grouping";

	public void process(final CombineOptions options, Header header,
			Vector<IPeakSet<IPeak>> peaksets, ParseResult[] results,
			final Random random, CorrelationMeasure measure, float rangeMin,
			float rangeMax, int totalPeaks, OutputStream output) throws IOException, FileNotFoundException;
	
}
