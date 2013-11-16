package com.joewandy.alignmentResearch.objectModel;

import java.util.List;

public interface FeatureGroupingMethod {

	public List<FeatureGroup> group(List<AlignmentFile> dataList);

	public List<FeatureGroup> group(AlignmentFile data);
	
	public void close();
		
}
