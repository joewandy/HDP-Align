package com.joewandy.alignmentResearch.main.experiment;

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
	
	public EvaluationResult getResultBestF1() {
		double lowest = 0;
		EvaluationResult best = null;
		System.out.println("Finding best result");
		for (EvaluationResult res : results) {
			System.out.println("\tmz=" + res.getDmz() + " rt=" + res.getDrt() + " note=" + res.getNote() + " f1=" + res.getF1());
			if (res.getF1() > lowest) {
				best = res;
				lowest = best.getF1();
			}
		}
		return best;
	}
	
	public List<EvaluationResult> getEvaluationResults() {
		return this.results;
	}
	
	public void printResult() {
				
		if (results.isEmpty()) {
			return;
		}
		
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

		System.out.print(label + "_f05 = [ ");
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			System.out.print(String.format("%.3f,", result.getF05()));
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

		System.out.println();
		for (EvaluationResult result : results) {
			if (result == null) {
				continue;
			}
			String precision = String.format("%.3f", result.getPrecision());
			String recall = String.format("%.3f", result.getRecall());
			String f1 = String.format("%.3f", result.getF1());
			String tp = String.format("%.1f", result.getTotalTp());
			String fp = String.format("%.1f", result.getTotalFp());
			String fn = String.format("%.1f", result.getTotalFn());			
			String note = result.getNote();
			System.out.println(precision + "\t" + recall + "\t" + f1 + "\t" + tp + "\t" + fp + "\t" + fn + "\t" + note);
		}
		
	}
	
}
