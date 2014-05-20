package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;


public class RetentionTimeWarping {

	private double[] g;
	private double[] h;
	
	public RetentionTimeWarping(GenerativeModelParameter params) {
		this.g = params.getG();
		this.h = params.getH();
	}
		
	public double getWarpedRT(double originalRT, int replicate) {
		double gs = g[replicate];
		double hs = h[replicate];
		double warpedRT = (gs * originalRT) + hs;
		return warpedRT;
	}

}