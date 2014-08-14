package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class ProteoExperiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public static final double[] ALL_ALPHA = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0  };
	public static final double[] ALL_GROUPING_RT = { 
		2, 4, 6, 8, 10
	};
	public static final double[] ALL_ALIGNMENT_MZ = { 0.05, 0.1, 0.25 };
	public static final double[] ALL_ALIGNMENT_RT = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 110, 120 };
	
	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");
		results.add(expResult);
		
		double[] alphas = new double[] { options.alpha };	
		double[] groupingRts = new double[] { options.groupingRtWindow };														
		if (options.useGroup && options.autoAlpha) {
			alphas = ALL_ALPHA;
		}
		if (options.useGroup && options.autoOptimiseGreedy && MultiAlignConstants.GROUPING_METHOD_GREEDY.equals(options.groupingMethod)) {
			groupingRts = ALL_GROUPING_RT;
		}
		
		// quick hack: obtained from randsample(20, 2)
		int[][] trainingIndices = { 
				{17, 19}, 	{2, 13}, 	{20, 4}, 	{10, 17}, 	{19, 16},
				{17, 1}, 	{15, 16}, 	{4, 15}, 	{1, 2}, 	{7, 20},
				{16, 8},	{9, 10}, 	{16, 6}, 	{3, 4}, 	{7, 12},
				{6, 11},	{11, 20}, 	{6, 17}, 	{19, 5},	{13, 6},
				{12, 17},	{16, 6},	{2, 12},	{16, 19},	{10, 1},
				{16, 7}, 	{6, 13},	{10, 15},	{19, 4}, 	{20, 2}
		};
		int[][] testingIndices= {
				{1, 20},	{18, 2}, 	{17, 9},	{3, 6},		{11, 12},
				{10, 2},	{6, 5}, 	{22, 21}, 	{21, 8}, 	{9, 18},
				{13, 8}, 	{2, 5},		{5, 9}, 	{19, 10},	{8, 19},
				{8, 5},		{19, 3},	{2, 5},		{1, 4}, 	{13, 10},
				{15, 4},	{13, 8},	{19, 16},	{7, 9},		{17, 16},
				{11, 17},	{18, 12},	{7, 5},		{17, 4}, 	{5, 9}						
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
				
							System.out.println();
							System.out.println("--- alignmentMz = " + alignmentMz + " alignmentRt = " + alignmentRt + 
									" groupingRt = " + groupingRts[k] + " alpha = " + alphas[j] + " ---");
							System.out.println();

							options.alignmentPpm = alignmentMz;
							options.alignmentRtWindow = alignmentRt;
							options.alpha = alphas[j];
							options.groupingRtWindow = groupingRts[k];
							MultiAlign multiAlign = new MultiAlign(options, data);
							EvaluationResult evalRes = multiAlign.runExperiment();	
							if (evalRes != null) {
								evalRes.setTh(options.alpha);
								String note = options.alpha + ", " + options.groupingRtWindow + ", " + i;
								evalRes.setNote(note);
								tempResult.addResult(evalRes);	
							}		
					
						}
					}

				}
			}
			
			super.printTrainingResult(tempResult);
			
			// report the result on another set of random n files
			System.out.println();
			EvaluationResult bestResult = tempResult.getResultBestF1();	
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
				options.alpha = bestAlpha;
				options.groupingRtWindow = bestGroupingRtWindow;
				System.out.println();
				System.out.println("##################  TESTING PHASE ################## ");
				System.out.println("--- alignmentMz = " + bestMz + " alignmentRt = " + bestRt + 
						" groupingRt = " + bestGroupingRtWindow + " alpha = " + bestAlpha + " ---");
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
