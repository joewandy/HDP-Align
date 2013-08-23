package com.joewandy.alignmentResearch.objectModel;

import java.util.List;

public interface FeatureGrouping {

	public List<FeatureGroup> group(List<AlignmentFile> dataList);
	
	public void filterGroups(List<FeatureGroup> groups);
	
}
