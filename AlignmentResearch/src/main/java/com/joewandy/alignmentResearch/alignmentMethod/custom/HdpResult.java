package com.joewandy.alignmentResearch.alignmentMethod.custom;

import com.joewandy.alignmentResearch.objectModel.Feature;

public class HdpResult {
	
	private Feature feature1;
	private Feature feature2;
	private double similarity;
	
	public HdpResult(Feature feature1, Feature feature2) {
		super();
		this.feature1 = feature1;
		this.feature2 = feature2;
	}
	
	public Feature getFeature1() {
		return feature1;
	}

	public Feature getFeature2() {
		return feature2;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public double getDistance() {
		return 1 - similarity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((feature1 == null) ? 0 : feature1.hashCode());
		result = prime * result
				+ ((feature2 == null) ? 0 : feature2.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HdpResult other = (HdpResult) obj;
		if (feature1 == null) {
			if (other.feature1 != null)
				return false;
		} else if (!feature1.equals(other.feature1))
			return false;
		if (feature2 == null) {
			if (other.feature2 != null)
				return false;
		} else if (!feature2.equals(other.feature2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HdpResult [feature1=" + feature1 + ", feature2=" + feature2
				+ ", similarity=" + similarity + "]";
	}	
	
}
