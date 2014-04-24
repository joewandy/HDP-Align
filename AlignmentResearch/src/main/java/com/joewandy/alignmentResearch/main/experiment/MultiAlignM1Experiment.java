package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmd;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class MultiAlignM1Experiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");
		
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
			for (double alignmentRt : MultiAlignExperiment.ALL_ALIGNMENT_RT) {
				
				System.out.println();
				System.out.println("--- alignmentRt = " + alignmentRt + " ---");
				System.out.println();

				options.alignmentRtWindow = alignmentRt;
				MultiAlign multiAlign = new MultiAlign(options, data);
				EvaluationResult evalRes = multiAlign.runExperiment();	
				if (evalRes != null) {
					evalRes.setTh(options.alpha);
					String note = options.alpha + ", " + options.groupingRtWindow;
					evalRes.setNote(note);
					tempResult.addResult(evalRes);	
				}						
				
			}
			
			// report the result on another set of random n files
			System.out.println();
			EvaluationResult bestResult = tempResult.getResultBestF1();	
			data = getData(options,	testingSet);	
			if (bestResult != null) {

				double bestRt = bestResult.getDrt();
				options.alignmentRtWindow = bestRt;
				System.out.println();
				System.out.println("##################  TESTING PHASE ################## ");
				System.out.println("################## bestRt = " + bestRt + " ##################");
				System.out.println();
				MultiAlign multiAlign = new MultiAlign(options, data);
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
