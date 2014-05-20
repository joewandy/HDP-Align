package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public abstract class GenerativeExperiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();

		// all the experimental-wide settings go here
		setCommonExperimentalSettings(options);
		
		for (int i = 0; i < options.experimentIter; i++) {

			AlignmentData data = getData(options, null);	
			
			// for every method to be tested
			List<String> allMethods = getAllMethods();
			MultiAlignExpResult tempResult = new MultiAlignExpResult("test_"+i);	
			for (String method : allMethods) {
				
				// do alignment using it
				options.method = method;
				MultiAlign multiAlign = new MultiAlign(options, data);
				EvaluationResult evalRes = multiAlign.runExperiment();	
				if (evalRes != null) {
					tempResult.addResult(evalRes);	
				}
				
				// and try the grouping approach too last
//				if (method.equals(AlignmentMethodFactory.ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_HIERARCHICAL)) {
//					setGroupingSettings(options);
//					multiAlign = new MultiAlign(options, data);
//					evalRes = multiAlign.runExperiment();	
//					if (evalRes != null) {
//						evalRes.setTh(options.alpha);
//						String note = options.alpha + ", " + options.groupingRtWindow;
//						evalRes.setNote(note);
//						tempResult.addResult(evalRes);	
//					}					
//				}
				
			}
			results.add(tempResult);
																			
		} 
		
		return results;
		
	}

	@Override
	public void printResult(List<MultiAlignExpResult> results) {

		StringBuilder f1Builder = new StringBuilder();
//		f1Builder.append("join,openms,sima,mw,mwg\n");
		for (MultiAlignExpResult result : results) {

			result.printResult();
			System.out.println();

			List<EvaluationResult> evalRes = result.getEvaluationResults();
			String[] toolF1 = new String[evalRes.size()];
			for (int i = 0; i < evalRes.size(); i++) {
				EvaluationResult eval = evalRes.get(i);
				String f1 = String.format("%.3f", eval.getF1());
				toolF1[i] = f1;
			}
			String joined = StringUtils.join(toolF1, ",");
			f1Builder.append(joined + "\n");

		}		 
		System.out.println("********* OVERALL *********");

		System.out.println(f1Builder);
		
	}

	protected abstract void setCommonExperimentalSettings(MultiAlignCmdOptions options);
	
	protected abstract void setGroupingSettings(MultiAlignCmdOptions options);

	protected abstract List<String> getAllMethods();

}
