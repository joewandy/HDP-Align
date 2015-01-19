package com.joewandy.alignmentResearch.filter;

import java.util.List;

import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;

public interface AlignmentResultFilter {

	public void process(AlignmentList result);
	public List<AlignmentRow> getAccepted();
	public List<AlignmentRow> getRejected();	
	public String getLabel();
	
}
