package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class M1Experiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public static final double[] ALL_ALPHA = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0  };
	public static final double[] ALL_GROUPING_RT = { 
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10
	};
	public static final double[] ALL_ALIGNMENT_MZ = { 0.05, 0.1, 0.25 };
	public static final double[] ALL_ALIGNMENT_RT = { 10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
	
	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");
		
		double[] alphas = new double[] { options.alpha };	
		double[] groupingRts = new double[] { options.groupingRtWindow };														
		if (options.autoAlpha) {
			alphas = ALL_ALPHA;
		}
		if (options.autoOptimiseGreedy && options.useGroup && MultiAlignConstants.GROUPING_METHOD_GREEDY.equals(options.groupingMethod)) {
			groupingRts = ALL_GROUPING_RT;
		}		
		
		// quick hack: obtained from randsample(44, 2)
		// only works for M1 !!
		int[][] trainingIndices = { 
				{36, 40}, 	{5, 28}, 	{7, 43}, 	{22, 36}, 	{35, 41},
				{2, 38}, 	{33, 34}, 	{8, 32}, 	{3, 5}, 	{14, 42},
				{17, 34},	{20, 22}, 	{13, 34}, 	{6, 8}, 	{15, 26},
				{12, 23},	{25, 43}, 	{12, 37}, 	{11, 41},	{12, 28},
				{26, 37},	{13, 34},	{4, 25},	{35, 42},	{1, 21},
				{11, 18}, 	{6, 42},	{3, 11},	{1, 2}, 	{29, 33}
		};
		int[][] testingIndices= {
				{14, 35},	{12, 27}, 	{20, 33},	{7, 41},	{4, 44},
				{1, 43},	{4, 39}, 	{19, 36}, 	{7, 12}, 	{25, 26},
				{16, 28}, 	{4, 11},	{11, 19}, 	{22, 42},	{17, 40},
				{14, 33},	{9, 17},	{4, 41},	{14, 20}, 	{35, 36},
				{24, 36},	{25, 39},	{10, 14},	{9, 38},	{11, 20},
				{9, 19},	{5, 20},	{12, 27},	{6, 10}, 	{19, 23}						
		};
		
		assert(trainingIndices.length == testingIndices.length);
		
		for (int i = 0; i < trainingIndices.length; i++) {
			
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
								String note = options.alpha + ", " + options.groupingRtWindow;
								evalRes.setNote(note);
								tempResult.addResult(evalRes);	
							}		
					
						}
					}

				}
			}
			
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
					String note = options.alpha + ", " + options.groupingRtWindow;
					evalRes.setNote(note);
					expResult.addResult(evalRes);	
				}
				
			} else {
				
				System.err.println("NO BEST RESULT FOUND ?! ");
				
			}
																
		} 
		
		return results;
		
	}

}
