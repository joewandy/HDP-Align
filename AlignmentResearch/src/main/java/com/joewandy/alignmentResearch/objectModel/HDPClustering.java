package com.joewandy.alignmentResearch.objectModel;

import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPResults;


public interface HDPClustering {

	void runClustering();

	int getSamplesTaken();
	
	HDPResults getResults();
	
	public HDPAnnotation<Feature> getIonisationProductAnnotations();

	public HDPAnnotation<Feature> getMetaboliteAnnotations();

	public HDPAnnotation<Feature> getIsotopeAnnotations();
	
}
