package com.joewandy.alignmentResearch.objectModel;

public class MahalanobisDistanceCalculator implements DistanceCalculator {

	private double dmz;
	private double drt;
	
	public MahalanobisDistanceCalculator(double dmz, double drt) {
		this.dmz = dmz;
		this.drt = drt;
	}
	
	@Override
	public double compute(double mass1, double mass2, double rt1, double rt2) {
		double rt = rt1 - rt2;
		double mz = mass1 - mass2;
        double dist = Math.sqrt((rt*rt)/(drt*drt) + (mz*mz)/(dmz*dmz));
        return dist;
	}

}
