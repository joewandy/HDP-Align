package com.joewandy.alignmentResearch.alignmentMethod.custom;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class MatchResult {

	private AlignmentRow row1;
	private AlignmentRow row2;
	private double score;

	public MatchResult(AlignmentRow row1, AlignmentRow row2, double score) {
		super();
		this.row1 = row1;
		this.row2 = row2;
		this.score = score;
	}
	
	public AlignmentRow getRow1() {
		return row1;
	}

	public AlignmentRow getRow2() {
		return row2;
	}
	
	public double getScore() {
		return score;
	}
	
}
