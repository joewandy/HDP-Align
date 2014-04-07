package com.joewandy.alignmentResearch.objectModel;

import java.util.List;

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
