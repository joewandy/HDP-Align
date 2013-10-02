package com.joewandy.alignmentResearch.objectModel;

import java.util.Set;


public class WeightedRowVsRowScore implements Comparable<WeightedRowVsRowScore> {

	private AlignmentRow aligned;
	private AlignmentRow masterListCandidate;
	private double dist;
	private double weight;
	private boolean probWeight;
	private double dmz;
	private double drt;

	public WeightedRowVsRowScore(AlignmentRow aligned, AlignmentRow masterListCandidate, double dmz, double drt) {
		this(aligned, masterListCandidate, false, null, dmz, drt);
	}
	
	public WeightedRowVsRowScore(AlignmentRow aligned, AlignmentRow masterListCandidate, 
			boolean probWeight, ExtendedLibrary library, double dmz, double drt) {
		
		this.aligned = aligned;
		this.masterListCandidate = masterListCandidate;
		this.probWeight = probWeight;
		
		double mass1 = aligned.getAverageMz();
		double mass2 = masterListCandidate.getAverageMz();
		double rt1 = aligned.getAverageRt();
		double rt2 = masterListCandidate.getAverageRt();
		
		this.dist = this.computeDist(mass1, mass2, rt1, rt2);				
		this.weight = this.computeWeight(aligned, masterListCandidate, library);
		this.dmz = dmz;
		this.drt = drt;
		
	}
	
	@Override
	public int compareTo(WeightedRowVsRowScore object) {
		return Double.compare(this.getScore(), object.getScore());
	}

	@Override
	public String toString() {
		return "RowVsRowScore [\n\taligned=" + aligned + "\n\tmasterListCandidate=" + masterListCandidate
				+ "\n\tscore=" + this.getScore() + "\n]";
	}
	
	public AlignmentRow getAligned() {
		return aligned;
	}
	
	public AlignmentRow getMasterListCandidate() {
		return masterListCandidate;
	}

	public double getScore() {
		double inverseDist = 1/dist;
		double score = inverseDist * weight;
		return score;
	}
			
	private double computeDist(double mass1, double mass2, double rt1, double rt2) {

		// DistanceCalculator calc = new EuclideanDistanceCalculator();
		DistanceCalculator calc = new MahalanobisDistanceCalculator(dmz, drt);
		double dist = calc.compute(mass1, mass2, rt1, rt2);		
		return dist;

		// no good ?!
//		double similarity = 1/(1+euclideanDist);
//		return similarity;

		// from mzmine
//		double mzDiff = Math.abs(mass1-mass2);
//		double rtDiff = Math.abs(rt1-rt2);
//		double mzRatio = mzDiff/mzTolerance;
//		double rtRatio = rtDiff/rtTolerance; 
//		// lower is better
//		double score = mzRatio + rtRatio;				
//		return score;
		
	}

	private double computeWeight(AlignmentRow aligned,
			AlignmentRow masterListCandidate, ExtendedLibrary library) {
		
		if (library == null) {
			return 1;
		} else {
			Set<Feature> features1 = aligned.getFeatures();
			Set<Feature> features2 = masterListCandidate.getFeatures();
			double weight = 0;
			int counter = 0;
			for (Feature f1 : features1) {
				for (Feature f2 : features2) {
					if (library.exist(f1, f2)) {
						weight += library.getEntryWeight(f1, f2);
						counter++;
					}
				}
			}
			if (counter == 0) {
				return 1;
			} else {
				return weight;				
			}			
		}
		
	}

}
