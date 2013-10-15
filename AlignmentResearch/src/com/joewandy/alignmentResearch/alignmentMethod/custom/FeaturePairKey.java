package com.joewandy.alignmentResearch.alignmentMethod.custom;

import com.joewandy.alignmentResearch.objectModel.Feature;

public class FeaturePairKey {
	private Feature f1;
	private Feature f2;
	public FeaturePairKey(Feature f1, Feature f2) {
		this.f1 = f1;
		this.f2 = f2;
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
		FeaturePairKey other = (FeaturePairKey) obj;
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

	@Override
	public String toString() {
		return "FeaturePairKey [f1=" + f1 + ", f2=" + f2 + "]";
	}
}