package com.joewandy.alignmentResearch.grouping;

import java.util.List;

import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.FeatureGroup;

import no.uib.cipr.matrix.Matrix;

public abstract class BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	protected Matrix ZZprob;
	
	public Matrix getClusteringMatrix() {
		return ZZprob;
	}

	public abstract List<FeatureGroup> group(List<AlignmentFile> dataList);

	public abstract List<FeatureGroup> group(AlignmentFile data);
		
	public void close() { }
	
}
