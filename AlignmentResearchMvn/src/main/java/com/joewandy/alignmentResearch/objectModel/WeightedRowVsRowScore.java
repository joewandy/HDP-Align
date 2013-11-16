package com.joewandy.alignmentResearch.objectModel;



public class WeightedRowVsRowScore implements Comparable<WeightedRowVsRowScore> {

	private AlignmentRow aligned;
	private AlignmentRow masterListCandidate;
	private double dmz;
	private double drt;
	private ExtendedLibrary library;

	public WeightedRowVsRowScore(AlignmentRow aligned, AlignmentRow masterListCandidate, 
			double dmz, double drt) {
		this(aligned, masterListCandidate, null, dmz, drt);
	}
	
	public WeightedRowVsRowScore(AlignmentRow aligned, AlignmentRow masterListCandidate, 
			ExtendedLibrary library, double dmz, double drt) {
		
		this.aligned = aligned;
		this.masterListCandidate = masterListCandidate;
		this.dmz = dmz;
		this.drt = drt;		
		this.library = library;
		
	}
	
	
	public int compareTo(WeightedRowVsRowScore object) {
		return Double.compare(this.getScore(), object.getScore());
	}

	
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
		if (library != null) {
			double score = library.computeWeightedRowScore(masterListCandidate, aligned);
			return score;			
		} else {
			MatchingScorer scorer = new MatchingScorer(dmz, drt);
			return scorer.computeScore(masterListCandidate, aligned);
		}
	}
	
	public double getDist() {
		MatchingScorer scorer = new MatchingScorer(dmz, drt);
		return scorer.computeDist(masterListCandidate, aligned);
	}

}
