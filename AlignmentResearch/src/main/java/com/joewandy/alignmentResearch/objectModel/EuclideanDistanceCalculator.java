package com.joewandy.alignmentResearch.objectModel;

public class EuclideanDistanceCalculator implements DistanceCalculator {

	
	public double compute(double mass1, double mass2, double rt1, double rt2) {
		double massDist = Math.pow(mass1-mass2, 2);
		double rtDist = Math.pow(rt1-rt2, 2);
		double euclideanDist = Math.sqrt(massDist + rtDist);
		return euclideanDist;
	}

}
