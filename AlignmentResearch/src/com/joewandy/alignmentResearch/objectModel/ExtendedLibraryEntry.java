package com.joewandy.alignmentResearch.objectModel;

public class ExtendedLibraryEntry implements Comparable<ExtendedLibraryEntry> {

	private Feature feature1;
	private Feature feature2;
	private double score;
	private double weight;
	
	public ExtendedLibraryEntry(Feature feature1, Feature feature2) {
		this.feature1 = feature1;
		this.feature2 = feature2;
		this.score = 0;
		this.weight = 0;
	}

	public ExtendedLibraryEntry(Feature feature1, Feature feature2, double score, double binaryWeight) {
		this.feature1 = feature1;
		this.feature2 = feature2;
		this.score = score;
		this.weight = binaryWeight;
	}
	
	public Feature getFeature1() {
		return feature1;
	}

	public Feature getFeature2() {
		return feature2;
	}
	
	public double getScore() {
		return score;
	}
	
	public double getDist() {
		return 1/score;
	}

	public double getWeight() {
		return weight;
	}
	
	public void setScore(double score) {
		this.score = score;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public void increaseScore(double add) {
		this.score += add;
	}
	
	public void increaseWeight(double add) {
		this.weight += add;
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
		ExtendedLibraryEntry other = (ExtendedLibraryEntry) obj;
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
	public int compareTo(ExtendedLibraryEntry other) {
		return Double.compare(score*weight, other.getScore()*other.getWeight());
	}
	
	public String toString() {
		return String.format("Score %.3f (%.3fx%.3f)", score*weight, score, weight)
				+ "\tFeature " + String.format("%5d", feature1.getPeakID()) 
				+ " m/z " + String.format("%.4f", feature1.getMass())
				+ " rt " + String.format("%.2f", feature1.getRt())
				+ " intensity " + String.format("%.2f", feature1.getIntensity())
				+ " (" + feature1.getData().getFilenameWithoutExtension() 
				+ ")-Feature " + String.format("%5d", feature2.getPeakID()) 
				+ " m/z " + String.format("%.4f", feature2.getMass())
				+ " rt " + String.format("%.2f", feature2.getRt())				
				+ " intensity " + String.format("%.2f", feature2.getIntensity())
				+ " (" + feature2.getData().getFilenameWithoutExtension() + ")";
	}
	
}
