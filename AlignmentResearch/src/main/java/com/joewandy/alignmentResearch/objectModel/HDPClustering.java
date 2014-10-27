package com.joewandy.alignmentResearch.objectModel;

import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;

public interface HDPClustering {

	Matrix getSimilarityResult();
	
	public Map<Feature, Map<String, Integer>> getIpMap();

	public Map<HDPMetabolite, List<HDPPrecursorMass>> getMetabolitePrecursors();
	
	int getSamplesTaken();

	void run();

}
