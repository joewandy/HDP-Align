package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public abstract class MultiAlignBaseExp implements MultiAlignExperiment {

	public void printResult(List<MultiAlignExpResult> results) {
		for (MultiAlignExpResult result : results) {
			result.printResult();
			System.out.println();			
			for (EvaluationResult evalRes : result.getEvaluationResults()) {
				if (evalRes == null) {
					continue;
				}
				String precision = String.format("%.3f", evalRes.getPrecision());
				String recall = String.format("%.3f", evalRes.getRecall());
				String f1 = String.format("%.3f", evalRes.getF1());
				String tp = String.format("%.1f", evalRes.getTotalTp());
				String fp = String.format("%.1f", evalRes.getTotalFp());
				String fn = String.format("%.1f", evalRes.getTotalFn());			
				String note = evalRes.getNote();
				System.out.println("!OUTPUT, " + 
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
		}		 
	}	
	
	protected AlignmentData getData(MultiAlignCmdOptions options, int[] fileIndices)
			throws FileNotFoundException {

		AlignmentDataGenerator dataGenerator = AlignmentDataGeneratorFactory
				.getAlignmentDataGenerator(options);
		AlignmentData data;
		if (fileIndices == null) {
			data = dataGenerator.generate();
			for (AlignmentFile file : data.getAlignmentDataList()) {
				System.out.println("test on " + file.getFilename());
			}
		} else {
			// pick files according to file indices
			data = dataGenerator.generateByIndices(fileIndices);
			for (AlignmentFile file : data.getAlignmentDataList()) {
				System.out.println("test on " + file.getFilename());
			}
		}
		return data;

	}

	public abstract List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws Exception;

}
