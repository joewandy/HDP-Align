package com.joewandy.alignmentResearch.objectModel;

public class GroundTruthPair {

	private Feature f1;
	private Feature f2;
	
	public GroundTruthPair(Feature f1, Feature f2) {
		this.f1 = f1;
		this.f2 = f2;
	}

	public Feature getF1() {
		return f1;
	}

	public Feature getF2() {
		return f2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((f1 == null) ? 0 : f1.hashCode());
		result = prime * result + ((f2 == null) ? 0 : f2.hashCode());
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
		GroundTruthPair other = (GroundTruthPair) obj;
		if (f1 == null) {
			if (other.f1 != null)
				return false;
		} else if (!f1.equals(other.f1))
			return false;
		if (f2 == null) {
			if (other.f2 != null)
				return false;
		} else if (!f2.equals(other.f2))
			return false;
		return true;
	}
	
}
