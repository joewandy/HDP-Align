package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.EvaluationResult;

public abstract class MultiAlignBaseExp implements MultiAlignExperiment {

	public void printTrainingResult(MultiAlignExpResult result) {
		result.printResult();
		System.out.println();			
		for (EvaluationResult evalRes : result.getEvaluationResults()) {
			if (evalRes == null) {
				continue;
			}
			printRes("!TRAINING, ", evalRes);
		}			
	}	
	
	public void printTestingResult(List<MultiAlignExpResult> results) {
		for (MultiAlignExpResult result : results) {
			System.out.println("TESTING RESULTS");
			result.printResult();
			System.out.println();			
			for (EvaluationResult evalRes : result.getEvaluationResults()) {
				if (evalRes == null) {
					continue;
				}
				printRes("!OUTPUT, ", evalRes);
			}			
		}		 
	}	
	
	protected AlignmentData getData(MultiAlignCmdOptions options, int[] fileIndices)
			throws FileNotFoundException {

		AlignmentDataGenerator dataGenerator = AlignmentDataGeneratorFactory
				.getAlignmentDataGenerator(options);
		AlignmentData data;
		if (fileIndices != null) {
			// pick files according to file indices
			data = dataGenerator.generateByIndices(fileIndices);
			for (AlignmentFile file : data.getAlignmentDataList()) {
				System.out.println("Processing " + file.getFilename());
			}
		} else {
			data = dataGenerator.generate();
			for (AlignmentFile file : data.getAlignmentDataList()) {
				System.out.println("Processing " + file.getFilename());
			}
		}
		return data;

	}

	protected void printRes(String msg, EvaluationResult evalRes) {
		String precision = String.format("%.3f", evalRes.getPrecision());
		String recall = String.format("%.3f", evalRes.getRecall());
		String f1 = String.format("%.3f", evalRes.getF1());
		String tp = String.format("%.1f", evalRes.getTotalTp());
		String fp = String.format("%.1f", evalRes.getTotalFp());
		String fn = String.format("%.1f", evalRes.getTotalFn());			
		String note = evalRes.getNote();
		System.out.println(msg + 
				evalRes.getDmz() + ", " + 
				evalRes.getDrt() + ", " + 
				precision + ", " + 
				recall + ", " + 
				f1 + ", " + 
				tp + ", " + 
				fp + ", " + 
				fn + ", " + 
				note);
	}
	
	public abstract List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws Exception;
	
}
