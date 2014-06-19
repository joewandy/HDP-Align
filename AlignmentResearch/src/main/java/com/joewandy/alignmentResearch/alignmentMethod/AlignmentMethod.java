package com.joewandy.alignmentResearch.alignmentMethod;

import java.util.List;

import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.FeatureGroupingMethod;

public interface AlignmentMethod {
		
	public AlignmentList align();

	public void addFilter(AlignmentResultFilter sizeFilter);

	public List<AlignmentRow> getAlignmentResult();
				
}
