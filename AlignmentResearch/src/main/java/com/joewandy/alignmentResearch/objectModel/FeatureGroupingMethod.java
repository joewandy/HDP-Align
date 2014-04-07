package com.joewandy.alignmentResearch.objectModel;

import java.util.List;

import no.uib.cipr.matrix.Matrix;

public interface FeatureGroupingMethod {

	public Matrix getClusteringMatrix();
	
	public List<FeatureGroup> group(List<AlignmentFile> dataList);

	public List<FeatureGroup> group(AlignmentFile data);
		
	public void close();
	
}
