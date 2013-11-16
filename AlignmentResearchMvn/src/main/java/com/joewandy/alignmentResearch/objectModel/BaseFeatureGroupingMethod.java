package com.joewandy.alignmentResearch.objectModel;

import java.util.List;

public abstract class BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	public abstract List<FeatureGroup> group(List<AlignmentFile> dataList);

	public abstract List<FeatureGroup> group(AlignmentFile data);
		
	public void close() { }
	
}
