package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class StandardExperiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public static final double[] ALL_ALPHA = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0  };
	public static final double[] ALL_GROUPING_RT = { 
		2, 4, 6, 8, 10
	};
	public static final double[] ALL_ALIGNMENT_MZ = { 0.05, 0.1, 0.25 };
	public static final double[] ALL_ALIGNMENT_RT = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };
	public static final double[] ALL_MIN_CORR = { 0.70, 0.75, 0.80, 0.85, 0.90, 0.95 };
	
	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");
		results.add(expResult);
		
		double[] alphas = new double[] { options.alpha };	
		double[] groupingRts = new double[] { options.groupingRtWindow };														
		double[] minCorrs = new double[] { options.minCorrSignal };														
		if (options.autoAlpha) {
			alphas = ALL_ALPHA;
		}
		if (options.autoOptimiseGreedy && options.useGroup && MultiAlignConstants.GROUPING_METHOD_GREEDY.equals(options.groupingMethod)) {
			groupingRts = ALL_GROUPING_RT;
			minCorrs = ALL_MIN_CORR;
		}	
		
		// quick hack: obtained from randsample(11, 2)
		int[][] trainingIndices = { 
				{9, 10}, 	{2, 7}, 	{11, 2}, 	{6, 9}, 	{6, 4},
				{10, 1}, 	{9, 5}, 	{8, 1}, 	{10, 2}, 	{11, 1},
				{9, 3},		{8, 9}, 	{2, 8}, 	{11, 4}, 	{3, 9},
				{11, 10},	{3, 2}, 	{7, 3}, 	{7, 10},	{9, 4},
				{1, 7},		{7, 2},		{6, 1},		{9, 4},		{3, 7},
				{5, 9}, 	{11, 2},	{10, 1},	{1, 5}, 	{2, 3}
		};
		int[][] testingIndices= {
				{6, 4}, 	{9, 2}, 	{2, 5}, 	{7, 11}, 	{4, 10},
				{8, 2}, 	{5, 7}, 	{3, 8}, 	{7, 9}, 	{6, 9},
				{4, 6},		{8, 9}, 	{3, 7}, 	{3, 10}, 	{5, 11},
				{5, 4},		{3, 9}, 	{9, 6}, 	{11, 6},	{4, 8},
				{6, 10},	{11, 2},	{6, 1},		{8, 9},		{5, 10},
				{7, 8}, 	{11, 1},	{5, 3},		{7, 5}, 	{11, 4}
		};
		
		assert(trainingIndices.length == testingIndices.length);
		
		// manually set index for experiment
		final long startTime = System.currentTimeMillis();
		int i = options.experimentIter;	
//		for (int i = 0; i < trainingIndices.length; i++) {
			
			System.out.println();
			System.out.println("################## TRAINING PHASE iter " + (i+1) + " ################## ");
			
			int[] trainingSet = trainingIndices[i];
			int[] testingSet = testingIndices[i];
			
			// pick n files randomly to 'train'
			MultiAlignExpResult tempResult = new MultiAlignExpResult("test");	
			AlignmentData data = getData(options, trainingSet);	
			// cluster peaks within files
			for (double alignmentMz : ALL_ALIGNMENT_MZ) {
				for (double alignmentRt : ALL_ALIGNMENT_RT) {
					for (int k = 0; k < groupingRts.length; k++) {
							for (int j = 0; j < alphas.length; j++) {
								for (int l = 0; l < minCorrs.length; l++) {
					
								System.out.println();
								System.out.println("--- alignmentMz = " + alignmentMz + " alignmentRt = " + alignmentRt + 
										" groupingRt = " + groupingRts[k] + " minCorrSignal = " + minCorrs[l] + 
										" alpha = " + alphas[j] + " ---");
								System.out.println();
	
								options.alignmentPpm = alignmentMz;
								options.alignmentRtWindow = alignmentRt;
								options.alpha = alphas[j];
								options.groupingRtWindow = groupingRts[k];
								options.minCorrSignal = minCorrs[l];
								MultiAlign multiAlign = new MultiAlign(options, data);
								EvaluationResult evalRes = multiAlign.runExperiment();	
								if (evalRes != null) {
									evalRes.setTh(options.alpha);
									String note = options.alpha + ", " + options.groupingRtWindow + ", " + i + ", " + options.minCorrSignal;
									evalRes.setNote(note);
									tempResult.addResult(evalRes);	
								}		
						
							}
						}
					}

				}
			}
			
			super.printTrainingResult(tempResult);
			
			// report the result on another set of random n files
			System.out.println();
			EvaluationResult bestResult = tempResult.getResultBestF1();	
			super.printRes("!BEST_TRAINING, ", bestResult);
			data = getData(options,	testingSet);	
			if (bestResult != null) {

				double bestMz = bestResult.getDmz();
				double bestRt = bestResult.getDrt();
				options.alignmentPpm = bestMz;
				options.alignmentRtWindow = bestRt;
				String bestNote = bestResult.getNote();
				String[] toks = bestNote.split(",");
				double bestAlpha = Double.parseDouble(toks[0].trim());
				double bestGroupingRtWindow = Double.parseDouble(toks[1].trim());
				double bestMinCorrSignal = Double.parseDouble(toks[3].trim());
				options.alpha = bestAlpha;
				options.groupingRtWindow = bestGroupingRtWindow;
				options.minCorrSignal = bestMinCorrSignal;
				System.out.println();
				System.out.println("##################  TESTING PHASE ################## ");
				System.out.println("--- alignmentMz = " + bestMz + " alignmentRt = " + bestRt + 
						" groupingRt = " + bestGroupingRtWindow + " minCorrSignal = " + bestMinCorrSignal + 
						" alpha = " + bestAlpha + " ---");
				System.out.println();
				// cluster peaks within files
				MultiAlign multiAlign = new MultiAlign(options, data);
				multiAlign = new MultiAlign(options, data);
				EvaluationResult evalRes = multiAlign.runExperiment();	
				if (evalRes != null) {
					evalRes.setTh(options.alpha);
					String note = options.alpha + ", " + options.groupingRtWindow + ", " + i;
					evalRes.setNote(note);
					expResult.addResult(evalRes);	
				}
				
			} else {
				
				System.err.println("NO BEST RESULT FOUND ?! ");
				
			}
																
//		} 
			
		final long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime);
		System.out.println("==================================================");
		System.out.println("Total execution time: " + totalTime/1000  + " seconds");
		System.out.println("==================================================");
						
		return results;
		
	}
	
}
