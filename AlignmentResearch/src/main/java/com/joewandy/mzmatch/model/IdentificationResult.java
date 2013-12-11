package com.joewandy.mzmatch.model;

public class IdentificationResult {

	private int id;
	private double mass;
	private double rt;
	private double intensity;
	private String identification;
	private double prob;
	
	public IdentificationResult(int id, double mass, double rt, double intensity,
			String identification, double prob) {
		super();
		this.id = id;
		this.mass = mass;
		this.rt = rt;
		this.intensity = intensity;
		this.identification = identification;
		this.prob = prob;
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

	public String getIdentification() {
		return identification;
	}

	public double getProb() {
		return prob;
	}

	public String[] toArray() {
		String[] arr = new String[5];
		arr[0] = String.format("%f", mass);
		arr[1] = String.format("%f", rt);
		arr[2] = String.format("%f", intensity);
		arr[3] = identification;
		arr[4] = String.format("%f", prob);;
		return arr;
	}

	@Override
	public String toString() {
		return "IdentificationResult [mass=" + mass + "\trt=" + rt
				+ "\tintensity=" + intensity + "\t\tidentification="
				+ identification + "\t\tprob=" + prob + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		IdentificationResult other = (IdentificationResult) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
