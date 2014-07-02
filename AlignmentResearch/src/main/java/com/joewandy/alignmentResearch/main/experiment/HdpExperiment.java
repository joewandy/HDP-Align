package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.filter.ScoreResultFilter;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class HdpExperiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public static final double[] ALL_THRESHOLD = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.99 };
	
	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");						
		AlignmentData data = getData(options, null);

		final long startTime = System.currentTimeMillis();
		for (int i = 0; i < ALL_THRESHOLD.length; i++) {
			options.alpha = ALL_THRESHOLD[i];
			System.out.println("EXPERIMENT alpha = " + options.alpha);
			// run experiment
			MultiAlign multiAlign = new MultiAlign(options, data);
			AlignmentResultFilter scoreFilter = new ScoreResultFilter(options.alpha, data);
			multiAlign.addResultFilter(scoreFilter);
			EvaluationResult evalRes = multiAlign.runExperiment();	
			if (evalRes != null) {
				evalRes.setTh(options.alpha);
				String note = options.alpha + "";
				evalRes.setNote(note);
				expResult.addResult(evalRes);	
			}
		}
		final long endTime = System.currentTimeMillis();
		results.add(expResult);										
		double totalTime = (endTime - startTime);
		double averageTime = totalTime;
		System.out.println("==================================================");
		System.out.println("Total execution time: " + totalTime/1000  + " seconds");
		System.out.println("Average execution time: " + averageTime/1000 + " seconds");
		System.out.println("==================================================");
		
		return results;
		
	}
	
}
