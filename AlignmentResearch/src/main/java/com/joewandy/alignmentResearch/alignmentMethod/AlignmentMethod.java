package com.joewandy.alignmentResearch.alignmentMethod;

import java.util.List;

import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.grouping.FeatureGroupingMethod;
import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;

public interface AlignmentMethod {
		
	public AlignmentList align();

	public void addFilter(AlignmentResultFilter sizeFilter);

	public List<AlignmentRow> getAlignmentResult();
				
}
