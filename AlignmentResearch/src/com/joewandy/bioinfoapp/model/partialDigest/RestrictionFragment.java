package com.joewandy.bioinfoapp.model.partialDigest;

public class RestrictionFragment implements Comparable<RestrictionFragment> {

	private Integer distance;

	public RestrictionFragment(Integer distance) {
		this.distance = distance;
	}

	public Integer getDistance() {
		return distance;
	}

	public void setDistance(Integer distance) {
		this.distance = distance;
	}

	@Override
	public int compareTo(RestrictionFragment arg0) {
		return this.getDistance().compareTo(arg0.getDistance());
	}

	@Override
	public String toString() {
		return this.distance.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((distance == null) ? 0 : distance.hashCode());
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
		RestrictionFragment other = (RestrictionFragment) obj;
		if (distance == null) {
			if (other.distance != null)
				return false;
		} else if (!distance.equals(other.distance))
			return false;
		return true;
	}

}
