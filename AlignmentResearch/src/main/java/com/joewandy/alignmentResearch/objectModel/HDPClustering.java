package com.joewandy.alignmentResearch.objectModel;

import no.uib.cipr.matrix.Matrix;

public interface HDPClustering {

	Matrix getSimilarityResult();
	
	int getSamplesTaken();

	void run();

}
