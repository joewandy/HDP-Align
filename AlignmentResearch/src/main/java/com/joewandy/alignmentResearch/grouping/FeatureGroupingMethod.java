package com.joewandy.alignmentResearch.grouping;

import java.util.List;

import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.FeatureGroup;

import no.uib.cipr.matrix.Matrix;

public interface FeatureGroupingMethod {

	public Matrix getClusteringMatrix();
	
	public List<FeatureGroup> group(List<AlignmentFile> dataList);

	public List<FeatureGroup> group(AlignmentFile data);
		
	public void close();
	
}
