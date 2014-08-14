package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.filter.ScoreResultFilter;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class HdpExperiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		AlignmentData data = getData(options, null);

		final long startTime = System.currentTimeMillis();
		MultiAlign multiAlign = new MultiAlign(options, data);
		if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_HDP_ALIGNMENT.equals(options.method)) {
			MultiAlignExpResult expResult = multiAlign.runPRExperiment();				
			results.add(expResult);										
		} else {
			MultiAlignExpResult expResult = new MultiAlignExpResult("");
			EvaluationResult evalRes = multiAlign.runExperiment();
			expResult.addResult(evalRes);
			results.add(expResult);													
		}
		final long endTime = System.currentTimeMillis();
		double totalTime = (endTime - startTime);
		double averageTime = totalTime;
		System.out.println("==================================================");
		System.out.println("Total execution time: " + totalTime/1000  + " seconds");
		System.out.println("Average execution time: " + averageTime/1000 + " seconds");
		System.out.println("==================================================");
		
		return results;
		
	}
	
}
