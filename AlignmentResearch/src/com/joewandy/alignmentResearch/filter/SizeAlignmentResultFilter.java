package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class SizeAlignmentResultFilter implements AlignmentResultFilter {

	private int threshold;
	
	public SizeAlignmentResultFilter(int threshold) {
		this.threshold = threshold;
	}

	@Override
	public List<AlignmentRow> filter(List<AlignmentRow> rows) {
		List<AlignmentRow> filteredRows = new ArrayList<AlignmentRow>();
		for (AlignmentRow row : rows) {
			if (row.getFeatures().size() >= threshold) {
				filteredRows.add(row);
			}
		}
		return filteredRows;
	}

	@Override
	public String getLabel() {
		return "alignment result filtering by consensus map size " + this.threshold;
	}

}
