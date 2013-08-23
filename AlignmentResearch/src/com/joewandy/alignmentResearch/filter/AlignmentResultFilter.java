package com.joewandy.alignmentResearch.filter;

import java.util.List;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public interface AlignmentResultFilter {

	public List<AlignmentRow> filter(List<AlignmentRow> rows);
	public String getLabel();
	
}
