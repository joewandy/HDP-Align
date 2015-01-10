package com.joewandy.alignmentResearch.comparator;

import java.util.Comparator;

import com.joewandy.alignmentResearch.model.Feature;

public class FeatureIntensityComparator implements Comparator<Feature> {

	/*
	 * Sorts feature by intensity order, descending
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	
	public int compare(Feature arg0, Feature arg1) {
		return -Double.compare(arg0.getIntensity(), arg1.getIntensity());
	}

}
