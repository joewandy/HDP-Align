package com.joewandy.alignmentResearch.objectModel;


public class RowVsRowScore implements Comparable<RowVsRowScore> {

	private AlignmentRow reference;
	private AlignmentRow masterListCandidate;
	private double score;
	
	public RowVsRowScore(AlignmentRow reference, AlignmentRow masterListCandidate, 
			double mzTolerance, double rtTolerance) {
		
		this.reference = reference;
		this.masterListCandidate = masterListCandidate;

		double mzDiff = Math.abs(reference.getAverageMz() - masterListCandidate.getAverageMz());
		double rtDiff = Math.abs(reference.getAverageRt() - masterListCandidate.getAverageRt());
		
		double mzRatio = mzDiff/mzTolerance;
		double rtRatio = rtDiff/rtTolerance; 

		// lower is better
		this.score = mzRatio + rtRatio;
		
	}
	
	public AlignmentRow getReference() {
		return reference;
	}
	
	public AlignmentRow getMasterListCandidate() {
		return masterListCandidate;
	}

	public double getScore() {
		return score;
	}

	@Override
	public int compareTo(RowVsRowScore object) {
		return Double.compare(this.score, object.getScore());
	}

	@Override
	public String toString() {
		return "RowVsRowScore [\n\treference=" + reference + "\n\tmasterListCandidate=" + masterListCandidate
				+ "\n\tscore=" + score + "\n]";
	}

}
