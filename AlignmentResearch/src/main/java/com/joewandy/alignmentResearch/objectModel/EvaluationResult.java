package com.joewandy.alignmentResearch.objectModel;

public class EvaluationResult {

	private double dmz;
	private double drt;
	private double precision;
	private double recall;
	private double f1;
	private double f05;
	private double totalTp;
	private double totalFp;
	private double totalFn;
	private double totalPositives;
	private double totalTpRatio;
	private double totalFpRatio;
	private double totalPositivesRatio;
	private double medSdrt;
	private double meanSdrt;
	private double medMdrt;
	private double meanMdrt;
	private double coverage;
	private double th;
	private double drtBefore;
	private String version;

	public EvaluationResult(double dmz, double drt, double precision, double recall, double f1,
			double f05, double totalTp, double totalFp, double totalFn, double totalPositives,
			double totalTpRatio, double totalFpRatio, double totalPositivesRatio,
			double medSdrt, double meanSdrt, double medMdrt, double meanMdrt, double coverage, String version) {
		super();
		this.dmz = dmz;
		this.drt = drt;
		this.precision = precision;
		this.recall = recall;
		this.f1 = f1;
		this.f05 = f05;
		this.totalTp = totalTp;
		this.totalFp = totalFp;
		this.totalFn = totalFn;
		this.totalPositives = totalPositives;
		this.totalTpRatio = totalTpRatio;
		this.totalFpRatio = totalFpRatio;
		this.totalPositivesRatio = totalPositivesRatio;
		this.medSdrt = medSdrt;
		this.meanSdrt = meanSdrt;
		this.medMdrt = medMdrt;
		this.meanMdrt = meanMdrt;
		this.coverage = coverage;
		this.version = version;
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
	
	public double getTotalFn() {
		return totalFn;
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

	public double getMedSdrt() {
		return medSdrt;
	}

	public double getMeanSdrt() {
		return meanSdrt;
	}

	public double getMedMdrt() {
		return medMdrt;
	}

	public double getMeanMdrt() {
		return meanMdrt;
	}

	public double getCoverage() {
		return coverage;
	}

	public double getTh() {
		return th;
	}

	public void setTh(double th) {
		this.th = th;
	}

	public double getDmz() {
		return dmz;
	}

	public void setDmz(double dmz) {
		this.dmz = dmz;
	}

	public double getDrt() {
		return drt;
	}

	public void setDrt(double drt) {
		this.drt = drt;
	}

	public double getDrtBefore() {
		return drtBefore;
	}

	public void setDrtBefore(double drtBefore) {
		this.drtBefore = drtBefore;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
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
		String medSdrtStr = String.format("%.3f", medSdrt);
		String meanSdrtStr = String.format("%.3f", meanSdrt);
		String medMdrtStr = String.format("%.3f", medMdrt);
		String meanMdrtStr = String.format("%.3f", meanMdrt);
		String coverageStr = String.format("%.3f", coverage);
		
		// human readable
		String output = "";
//		output += "Precision = " + precStr + "\n";
//		output += "Recall = " + recallStr + "\n";
//		output += "F1 = " + f1Str + "\n";
//		output += "F0.5 = " + f05Str + "\n";
//		output += "Total TP = " + totalTp + " (" + totalTpRatioStr + ")" + "\n";
//		output += "Total FP = " + totalFp + " (" + totalFpRatioStr + ")" + "\n";
//		output += "Total FN = " + totalFn + "\n";		
//		output += "Total positives = " + totalPositives + " (" + totalPositivesRatioStr + ")"  + "\n";
//		output += "Median SDRT = " + medSdrtStr + "\n";
//		output += "Mean SDRT = " + meanSdrtStr + "\n";
//		output += "Median MDRT = " + medMdrtStr + "\n";
//		output += "Mean MDRT = " + meanMdrtStr + "\n";
//		output += "Coverage = " + coverageStr + "\n";
//		output += "Version = " + version + "\n";

		// for parsing in CSV format
		output += "!OUTPUT," + dmz + ", " + drt + ", " + th + ", " + precStr + ", " + recallStr + ", " + f1Str + ", " + f05Str + ", " + 
				totalTp + ", " + totalTpRatioStr + ", " + 
				totalFp + ", " + totalFpRatioStr + ", " +
				totalFn + ", " + 
				totalPositives + ", " + totalPositivesRatioStr + ", " + 
				medSdrtStr + ", " + meanSdrtStr + ", " + 
				medMdrtStr + ", " + meanMdrtStr + ", " +
				coverageStr + ", " + drtBefore + ", " +
				version;

		return output;
		
	}
	
}
