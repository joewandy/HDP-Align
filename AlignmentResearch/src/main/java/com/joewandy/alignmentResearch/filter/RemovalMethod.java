package com.joewandy.alignmentResearch.filter;

import java.util.List;
import java.util.Set;

import com.joewandy.alignmentResearch.objectModel.AlignmentExpParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpResult;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public interface RemovalMethod {

	public Set<Feature> findFeatures(String filterMethod, double threshold);
	
	public void printStatistics(AlignmentExpParam parameter,
			AlignmentExpResult experimentResult, Set<Feature> allFeatures);
	
	public RemovalResult filterRows(AlignmentList listResult);
	
}
