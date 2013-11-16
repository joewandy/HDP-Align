package com.joewandy.alignmentResearch.main;

import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class MultiAlignExpResult {

	private List<EvaluationResult> results;
	private String label;
	
	public MultiAlignExpResult(String label) {
		this.label = label;
		this.results = new ArrayList<EvaluationResult>();
	}
	
	public void addResult(EvaluationResult result) {
		this.results.add(result);
	}
	
	public void printResult() {
		
		System.out.print(label + "_prec = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getPrecision()));
		}
		System.out.println(" ]';");

		System.out.print(label + "_recall = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getRecall()));
		}
		System.out.println(" ]';");
		
		System.out.print(label + "_f1 = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getF1()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_total_tp = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%d,", (int) result.getTotalTp()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_total_fp = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%d,", (int) result.getTotalFp()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_total_fn = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%d,", (int) result.getTotalFn()));
		}
		System.out.println(" ]';");		
		
		System.out.print(label + "_medSdrt = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getMedSdrt()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_meanSdrt = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getMeanSdrt()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_medMdrt = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getMedMdrt()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_meanMdrt = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getMeanMdrt()));
		}
		System.out.println(" ]';");		

		System.out.print(label + "_coverage = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getCoverage()));
		}
		System.out.println(" ]';");		
		
	}
	
}
