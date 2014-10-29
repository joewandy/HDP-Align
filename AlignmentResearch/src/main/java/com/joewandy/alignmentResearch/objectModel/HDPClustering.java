package com.joewandy.alignmentResearch.objectModel;

import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;

public interface HDPClustering {

	void runClustering();

	int getSamplesTaken();
	
	Matrix getSimilarityResult();
	
	public Map<Feature, Map<String, Integer>> getIpMap();

	public Map<HDPMetabolite, List<HDPPrecursorMass>> getMetabolitePrecursors();
	
	public List<HDPMetabolite> getMetabolitesInLastSample();

}
