package com.joewandy.alignmentResearch.model;

import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPAlignmentResults;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPSingleSample;


public interface HDPClustering {

	void runClustering();

	int getSamplesTaken();
	
	public HDPAlignmentResults getAlignmentResults();
	
	public HDPAnnotation<Feature> getIonisationProductFeatureAnnotations();

	public HDPAnnotation<Feature> getMetaboliteFeatureAnnotations();

	public HDPAnnotation<HDPMetabolite> getMetaboliteAnnotations();
	
	public HDPAnnotation<Feature> getIsotopeFeatureAnnotations();
	
	public HDPSingleSample getLastSample();
	
}
