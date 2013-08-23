package com.joewandy.alignmentResearch.objectModel;

import java.util.Set;


public class WeightedRowVsRowScore implements Comparable<WeightedRowVsRowScore> {

	private AlignmentRow reference;
	private AlignmentRow masterListCandidate;
	private double dist;
	private double weight;
	private double mzTolerance;
	private double rtTolerance;
	private boolean probWeight;
	
	public WeightedRowVsRowScore(AlignmentRow reference, AlignmentRow masterListCandidate, 
			double mzTolerance, double rtTolerance, boolean probWeight, ExtendedLibrary library) {
		
		this.reference = reference;
		this.masterListCandidate = masterListCandidate;
		this.mzTolerance = mzTolerance;
		this.rtTolerance = rtTolerance;
		this.probWeight = probWeight;
		
		double mass1 = reference.getAverageMz();
		double mass2 = masterListCandidate.getAverageMz();
		double rt1 = reference.getAverageRt();
		double rt2 = masterListCandidate.getAverageRt();
		
		this.dist = this.computeDist(mass1, mass2, rt1, rt2, mzTolerance, rtTolerance);				
		this.weight = this.computeWeight(reference, masterListCandidate, library);
		
	}
	
	@Override
	public int compareTo(WeightedRowVsRowScore object) {
		return Double.compare(this.getScore(), object.getScore());
	}

	@Override
	public String toString() {
		return "RowVsRowScore [\n\treference=" + reference + "\n\tmasterListCandidate=" + masterListCandidate
				+ "\n\tscore=" + this.getScore() + "\n]";
	}
	
	public AlignmentRow getReference() {
		return reference;
	}
	
	public AlignmentRow getMasterListCandidate() {
		return masterListCandidate;
	}

	public double getMzTolerance() {
		return mzTolerance;
	}

	public double getRtTolerance() {
		return rtTolerance;
	}
	
	public double getScore() {
		double inverseDist = 1/dist;
		double score = inverseDist * weight;
		return score;
	}
			
	private double computeDist(double mass1, double mass2, double rt1, double rt2,
			double mzTolerance, double rtTolerance) {

		double massDist = Math.pow(mass1-mass2, 2);
		double rtDist = Math.pow(rt1-rt2, 2);
		double euclideanDist = Math.sqrt(massDist + rtDist);
		return euclideanDist;

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

	private double computeWeight(AlignmentRow reference,
			AlignmentRow masterListCandidate, ExtendedLibrary library) {
		
		if (library == null) {
			return 1;
		} else if (!probWeight) {
			return 1;
		} else {
			// compute probability weight thingie
			Set<Feature> features1 = reference.getFeatures();
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
	
//	@Deprecated
//	private double computeWeight(AlignmentRow reference,
//			AlignmentRow masterListCandidate, List<WeightedRowVsRowScore> scoreList) {
//		
//		double weightTotal = 0;
//		for (Feature fi : reference.getFeatures()) {
//			
//			for (Feature fj : masterListCandidate.getFeatures()) {
//				
//				int fiIdx = fi.getPeakID();
//				int fjIdx = fj.getPeakID();
//				double weight = 0;
//				double[][] ZZProb1 = fi.getZZProb();
//				double[][] ZZProb2 = fj.getZZProb();
//				
//				List<WeightedRowVsRowScore> others = getOtherEntries(fi, fj, scoreList);
//				for (WeightedRowVsRowScore other : others) {
//
//					AlignmentRow otherRef = other.getReference();
//					AlignmentRow otherCan = other.getMasterListCandidate();
//					Feature fm = otherRef.getFirstFeature();
//					Feature fn = otherCan.getFirstFeature();
//					
//					int fmIdx = fm.getPeakID();
//					int fnIdx = fn.getPeakID();
//					double prob1 = ZZProb1[fiIdx][fmIdx];
//					double prob2 = ZZProb2[fjIdx][fnIdx];
//					double pairWeight = prob1 * prob2;
//					
//					weight += pairWeight;
//				
//				}
//				
//				weightTotal += weight;
//				
//			}
//			
//		}
//		
//		return weightTotal;
//
//	}
//	
//	@Deprecated
//	private List<WeightedRowVsRowScore> getOtherEntries(Feature referenceFeature, Feature candidateFeature, 
//			List<WeightedRowVsRowScore> scoreList) {
//
//		List<WeightedRowVsRowScore> others = new ArrayList<WeightedRowVsRowScore>();
//		for (WeightedRowVsRowScore score : scoreList) {
//			
//			AlignmentRow ref = score.getReference();
//			AlignmentRow can = score.getMasterListCandidate();
//			Feature refRetrieved = ref.getFeaturesFromFile(referenceFeature.getData().getFilenameWithoutExtension());
//			Feature canRetrieved = can.getFeaturesFromFile(candidateFeature.getData().getFilenameWithoutExtension());
//
//			AlignmentRow newRef = new AlignmentRow(ref.getRowId());
//			AlignmentRow newCan = new AlignmentRow(can.getRowId());
//			newRef.addFeature(refRetrieved);
//			newCan.addFeature(canRetrieved);
//			
//			if (refRetrieved != null && canRetrieved != null) {
//				WeightedRowVsRowScore filtered = new WeightedRowVsRowScore(newRef, newCan, mzTolerance, rtTolerance);
//				others.add(filtered);
//			}
//			
//		}
//		
//		return others;
//
//	}

}
