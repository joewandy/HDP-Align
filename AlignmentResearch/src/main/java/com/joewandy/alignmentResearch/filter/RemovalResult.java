package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class RemovalResult {

	private List<AlignmentRow> accepted;
	private List<AlignmentRow> rejected;

	public RemovalResult() {
		this.accepted = new ArrayList<AlignmentRow>();
		this.rejected = new ArrayList<AlignmentRow>();
	}

	public List<AlignmentRow> getAccepted() {
		return accepted;
	}

	public void setAccepted(List<AlignmentRow> accepted) {
		this.accepted = accepted;
	}

	public List<AlignmentRow> getRejected() {
		return rejected;
	}

	public void setRejected(List<AlignmentRow> rejected) {
		this.rejected = rejected;
	}	
	
}
