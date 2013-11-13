package com.joewandy.alignmentResearch.objectModel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.util.PrettyPrintGroupSize;

public abstract class BaseFeatureGrouping {

	public abstract List<FeatureGroup> group(List<AlignmentFile> dataList);
	
}
