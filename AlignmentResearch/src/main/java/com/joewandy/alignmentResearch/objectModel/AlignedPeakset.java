package com.joewandy.alignmentResearch.objectModel;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlignedPeakset {

	private Set<Feature> features;
	
	public AlignedPeakset(List<Feature> fs) {
		this.features = new HashSet<Feature>(fs);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((features == null) ? 0 : features.hashCode());
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
		AlignedPeakset other = (AlignedPeakset) obj;
		if (features == null) {
			if (other.features != null)
				return false;
		} else if (!features.equals(other.features))
			return false;
		return true;
	}
	
}
