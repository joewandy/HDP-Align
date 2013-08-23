package com.joewandy.bioinfoapp.model.partialDigest;

public class RestrictionSite implements Comparable<RestrictionSite> {

	private Integer location;

	public RestrictionSite(int location) {
		this.location = new Integer(location);
	}

	public Integer getLocation() {
		return location;
	}

	public void setLocation(Integer location) {
		this.location = location;
	}

	@Override
	public int compareTo(RestrictionSite arg0) {
		return this.getLocation().compareTo(arg0.getLocation());
	}

	@Override
	public String toString() {
		return this.location.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((location == null) ? 0 : location.hashCode());
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
		RestrictionSite other = (RestrictionSite) obj;
		if (location == null) {
			if (other.location != null)
				return false;
		} else if (!location.equals(other.location))
			return false;
		return true;
	}

}
