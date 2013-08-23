package com.joewandy.alignmentResearch.objectModel;

public class EvaluationResult {

	private double precision;
	private double recall;
	private double f1;
	private double f05;
	private double totalTp;
	private double totalFp;
	private double totalPositives;
	private double totalTpRatio;
	private double totalFpRatio;
	private double totalPositivesRatio;

	public EvaluationResult(double precision, double recall, double f1,
			double f05, double totalTp, double totalFp, double totalPositives,
			double totalTpRatio, double totalFpRatio, double totalPositivesRatio) {
		super();
		this.precision = precision;
		this.recall = recall;
		this.f1 = f1;
		this.f05 = f05;
		this.totalTp = totalTp;
		this.totalFp = totalFp;
		this.totalPositives = totalPositives;
		this.totalTpRatio = totalTpRatio;
		this.totalFpRatio = totalFpRatio;
		this.totalPositivesRatio = totalPositivesRatio;
	}

	public double getPrecision() {
		return precision;
	}

	public double getRecall() {
		return recall;
	}

	public double getF1() {
		return f1;
	}

	public double getF05() {
		return f05;
	}

	public double getTotalTp() {
		return totalTp;
	}

	public double getTotalFp() {
		return totalFp;
	}

	public double getTotalPositives() {
		return totalPositives;
	}

	public double getTotalTpRatio() {
		return totalTpRatio;
	}

	public double getTotalFpRatio() {
		return totalFpRatio;
	}

	public double getTotalPositivesRatio() {
		return totalPositivesRatio;
	}

	@Override
	public String toString() {
		
		String precStr = String.format("%.3f", precision);
		String recallStr = String.format("%.3f", recall);
		String f1Str = String.format("%.3f", f1);
		String f05Str = String.format("%.3f", f05);
		String totalTpRatioStr = String.format("%.3f", totalTpRatio);
		String totalFpRatioStr = String.format("%.3f", totalFpRatio);
		String totalPositivesRatioStr = String.format("%.3f", totalPositivesRatio);
		
		// human readable
		String output = "";
		output += "Precision = " + precStr + "\n";
		output += "Recall = " + recallStr + "\n";
		output += "F1 = " + f1Str + "\n";
		output += "F0.5 = " + f05Str + "\n";
		output += "Total TP = " + totalTp + " (" + totalTpRatioStr + ")" + "\n";
		output += "Total FP = " + totalFp + " (" + totalFpRatioStr + ")" + "\n";
		output += "Total positives = " + totalPositives + " (" + totalPositivesRatioStr + ")"  + "\n";

		// for parsing in CSV format
		output += "!OUTPUT," + precStr + ", " + recallStr + ", " + f1Str + ", " + f05Str + ", " + 
				totalTp + ", " + totalTpRatioStr + ", " + 
				totalFp + ", " + totalFpRatioStr + ", " +
				totalPositives + ", " + totalPositivesRatioStr;

		return output;
		
	}
	
}
