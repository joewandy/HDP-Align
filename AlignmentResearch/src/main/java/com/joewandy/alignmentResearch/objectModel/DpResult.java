package com.joewandy.alignmentResearch.objectModel;

public class DpResult {

	private double[] logPrior;
	private double[] logLikelihood;

	public DpResult(double[] logPrior, double[] logLikelihood) {
		this.logPrior = logPrior;
		this.logLikelihood = logLikelihood;
	}
	
	public double[] getLogPrior() {
		return logPrior;
	}
	
	public double[] getLogLikelihood() {
		return logLikelihood;
	}
	
}
