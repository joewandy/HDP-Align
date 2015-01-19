package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;

public class SizeAlignmentResultFilter implements AlignmentResultFilter {

	private int threshold;
	private List<AlignmentRow> accepted;
	private List<AlignmentRow> rejected;
	
	public SizeAlignmentResultFilter(int threshold) {
		this.threshold = threshold;
		this.accepted = new ArrayList<AlignmentRow>();
		this.rejected = new ArrayList<AlignmentRow>();
	}
	
	public void process(AlignmentList result) {
		for (AlignmentRow row : result.getRows()) {
			if (row.getFeatures().size() >= threshold) {
				accepted.add(row);
			} else {
				rejected.add(row);
			}
		}
	}

	public List<AlignmentRow> getAccepted() {
		return accepted;
	}

	public List<AlignmentRow> getRejected() {
		return rejected;
	}
	
	public String getLabel() {
		return "alignment result filtering by consensus map size " + this.threshold;
	}

}
