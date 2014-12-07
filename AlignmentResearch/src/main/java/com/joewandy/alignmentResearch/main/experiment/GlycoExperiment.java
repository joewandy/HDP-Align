package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class GlycoExperiment extends MultiAlignBaseExp implements
		MultiAlignExperiment {

	public static final double[] ALL_ALPHA = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6,
			0.7, 0.8, 0.9, 1.0 };
	public static final double[] ALL_GROUPING_RT = { 2, 4, 6, 8, 10 };
	public static final double[] ALL_ALIGNMENT_MZ = { 0.05, 0.1, 0.25 };
	public static final double[] ALL_ALIGNMENT_RT = { 5, 10, 15, 20, 25, 30,
			35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };

//	public static final double[] ALL_ALPHA = { 0.7, 0.8, 0.9 };
//	public static final double[] ALL_GROUPING_RT = { 2, 4, 6 };
//	public static final double[] ALL_ALIGNMENT_MZ = { 0.05 };
//	public static final double[] ALL_ALIGNMENT_RT = { 85, 90, 95, 100 };	
	
	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {

		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("testing");
		results.add(expResult);

		double[] alphas = new double[] { options.alpha };
		double[] groupingRts = new double[] { options.groupingRtWindow };
		if (options.useGroup && options.autoAlpha) {
			alphas = ALL_ALPHA;
		}
		if (options.useGroup
				&& options.autoOptimiseGreedy
				&& MultiAlignConstants.GROUPING_METHOD_GREEDY
						.equals(options.groupingMethod)) {
			groupingRts = ALL_GROUPING_RT;
		}

		// hardcoded training-testing indices .. obtained from randsample(12, 2)
		int[][] trainingIndices = { 
				{19, 21}, 	{3, 15}, 	{23, 4}, 	{12, 19}, 	{2, 1},
				{20, 1}, 	{18, 10}, 	{17, 1}, 	{19, 3}, 	{22, 1},
				{18, 19},	{2, 8}, 	{6, 10}, 	{12, 3}, 	{14, 6},
				{12, 17},	{10, 11}, 	{20, 6}, 	{9, 22},	{3, 9},
				{14, 13},	{18, 9},	{2, 13},	{14, 3},	{8, 4},
				{13, 4}, 	{18, 16},	{6, 22},	{13, 23}, 	{3, 23}
		};
		int[][] testingIndices= {
				{19, 20},	{6, 19}, 	{7, 5},		{20, 14},	{4, 12},
				{10, 2},	{6, 5}, 	{22, 21}, 	{21, 8}, 	{9, 18},
				{3, 4}, 	{10, 5},	{6, 8}, 	{17, 15},	{13, 7},
				{5, 16},	{9, 4},		{12, 11},	{19, 12}, 	{8, 3},
				{21, 22},	{5, 14},	{6, 20},	{6, 4},		{7, 9},
				{11, 23},	{10, 14},	{6, 17},	{10, 8}, 	{7, 19}						
		};

		assert (trainingIndices.length == testingIndices.length);

		// manually set index for experiment
		final long startTime = System.currentTimeMillis();
		int i = options.trainingIndex;

		System.out.println("Experiment Type = Glycomic");

		int[] trainingSet = trainingIndices[i];
		int[] testingSet = testingIndices[i];

		// pick n files randomly to 'train'
		MultiAlignExpResult tempResult = new MultiAlignExpResult("training");
		System.out.println("\n##################  TRAINING PHASE ################## ");
		AlignmentData data = getData(options, trainingSet);
		
		EvaluationResult evalRes = null;
		if (options.alignmentMzTol != -1 && options.alignmentRtTol != -1) {
			// if specified from the command line, then use it
			evalRes = doAlign(options, data, i);			
			if (evalRes != null) {
				tempResult.addResult(evalRes);								
			}
		} else {
			// otherwise do parameter scans
			System.out.println("Running parameter scans");
			System.out.println();
			for (double alignmentMz : ALL_ALIGNMENT_MZ) {
				for (double alignmentRt : ALL_ALIGNMENT_RT) {
					for (int k = 0; k < groupingRts.length; k++) {
						for (int j = 0; j < alphas.length; j++) {
							System.out.println("--- alignmentMz = " + alignmentMz
									+ " alignmentRt = " + alignmentRt
									+ " groupingRt = " + groupingRts[k]
									+ " alpha = " + alphas[j] + " ---");
							options.alignmentMzTol = alignmentMz;
							options.alignmentRtTol = alignmentRt;
							options.alpha = alphas[j];
							options.groupingRtWindow = groupingRts[k];
							evalRes = doAlign(options, data, i);
							if (evalRes != null) {
								tempResult.addResult(evalRes);								
							}
						}
					}

				}
			}
		}


		// report the result on another set of random n files
		EvaluationResult bestResult = tempResult.getResultBestF1();
		System.out.println("Best training result of f1=" + String.format("%.3f", bestResult.getF1()) + 
				" found at mz=" + bestResult.getDmz() + " rt=" + bestResult.getDrt() + " other parameters=" + bestResult.getNote());
		super.printRes("!BEST_TRAINING, ", bestResult);
		System.out.println("\n##################  TESTING PHASE ################## ");
		data = getData(options, testingSet);
		if (bestResult != null) {
			
			double bestMz = bestResult.getDmz();
			double bestRt = bestResult.getDrt();
			options.alignmentMzTol = bestMz;
			options.alignmentRtTol = bestRt;
			String bestNote = bestResult.getNote();
			String[] toks = bestNote.split(",");
			double bestAlpha = Double.parseDouble(toks[0].trim());
			double bestGroupingRtWindow = Double.parseDouble(toks[1].trim());
			options.alpha = bestAlpha;
			options.groupingRtWindow = bestGroupingRtWindow;
			
			System.out.println("--- alignmentMz = " + bestMz
					+ " alignmentRt = " + bestRt + " groupingRt = "
					+ bestGroupingRtWindow + " alpha = " + bestAlpha + " ---");
			System.out.println();

			MultiAlign multiAlign = new MultiAlign(options, data);
			multiAlign = new MultiAlign(options, data);
			evalRes = multiAlign.runExperiment();
			if (evalRes != null) {
				evalRes.setTh(options.alpha);
				String note = options.alpha + ", " + options.groupingRtWindow
						+ ", " + i;
				evalRes.setNote(note);
				expResult.addResult(evalRes);
			}

		} 

		final long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime);
		System.out
				.println("==================================================");
		System.out.println("Total execution time: " + totalTime / 1000
				+ " seconds");
		System.out
				.println("==================================================");

		return results;

	}

	private EvaluationResult doAlign(MultiAlignCmdOptions options, AlignmentData data, int i)
			throws FileNotFoundException {
		MultiAlign multiAlign = new MultiAlign(options, data);
		EvaluationResult evalRes = multiAlign.runExperiment();
		if (evalRes != null) {
			evalRes.setTh(options.alpha);
			String note = options.alpha + ", "
					+ options.groupingRtWindow + ", " + i;
			evalRes.setNote(note);
			return evalRes;
		}
		else {
			return null;
		}
	}

}
