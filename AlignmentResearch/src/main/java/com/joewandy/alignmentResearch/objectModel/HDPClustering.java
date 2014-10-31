package com.joewandy.alignmentResearch.objectModel;

import no.uib.cipr.matrix.Matrix;

public interface HDPClustering {

	void runClustering();

	int getSamplesTaken();
	
	Matrix getSimilarityResult();
	
	public HDPAnnotation getIonisationProductAnnotations();

	public HDPAnnotation getMetaboliteAnnotations();
	
}
