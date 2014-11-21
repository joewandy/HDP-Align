package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class GlycoExperiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public static final double[] ALL_ALPHA = { 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0  };
	public static final double[] ALL_GROUPING_RT = { 
		2, 4, 6, 8, 10
	};
	public static final double[] ALL_ALIGNMENT_MZ = { 0.05, 0.1, 0.25 };
	public static final double[] ALL_ALIGNMENT_RT = { 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95, 100 };
	
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
		
		// quick hack: obtained from randsample(12, 2)
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
