package com.joewandy.alignmentResearch.objectModel;

public class HDPFeature {

	private HDPFile parent;
	private int id;
	private double mass;
	private double rt;
	private double intensity;
	private Object data;

	public HDPFeature(HDPFile parent, int id, double mass, double rt,
			double intensity, Object data) {
		super();
		this.parent = parent;
		this.id = id;
		this.mass = mass;
		this.rt = rt;
		this.intensity = intensity;
		this.data = data;
	}

	public HDPFile getParent() {
		return parent;
	}

	public int getId() {
		return id;
	}

	public double getMass() {
		return mass;
	}

	public double getRt() {
		return rt;
	}

	public double getIntensity() {
		return intensity;
	}

	public Object getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
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
		HDPFeature other = (HDPFeature) obj;
		if (id != other.id)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HDPFeature [parent=" + parent + ", id=" + id + ", mass=" + mass
				+ ", rt=" + rt + ", intensity=" + intensity + "]";
	}	
	
}
