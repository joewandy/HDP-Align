package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.model.EvaluationResult;

public class MultiAlignNormalRun extends MultiAlignBaseExp implements MultiAlignExperiment {

	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");		
		AlignmentData data = getData(options, null);

		final long startTime = System.currentTimeMillis();
		// cluster peaks within files
		MultiAlign multiAlign = new MultiAlign(options, data);
		EvaluationResult evalRes = multiAlign.runExperiment();	
		if (evalRes != null) {
			evalRes.setTh(options.alpha);
			String note = options.alpha + ", " + options.groupingRtWindow;
			evalRes.setNote(note);
			expResult.addResult(evalRes);	
		}
		final long endTime = System.currentTimeMillis();
		results.add(expResult);										
		System.out.println("==================================================");
		double totalTime = (endTime - startTime);
		System.out.println("Total execution time: " + totalTime/1000  + " seconds");
		
		return results;
		
	}

}
