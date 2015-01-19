package com.joewandy.mzmatch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PubChemSnapshot {

	private String id;
	private String rev;
	
	private double molecularWeight;
	private double charge;
	private double monoisotopicWeight;
	
	private String molecularFormula;
	private String iupacInchi;
	private String iupacInchiKey;
	private String inchi;
	
	private String traditionalName;

	@JsonProperty("_id")
	public String getId() {
		return id;
	}

	@JsonProperty("_id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("_rev")
	public String getRev() {
		return rev;
	}

	@JsonProperty("_rev")
	public void setRev(String rev) {
		this.rev = rev;
	}

	@JsonProperty("molecular_weight")
	public double getMolecularWeight() {
		return molecularWeight;
	}

	@JsonProperty("molecular_weight")
	public void setMolecularWeight(double molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	@JsonProperty("charge")
	public double getCharge() {
		return charge;
	}
	
	@JsonProperty("charge")
	public void setCharge(double charge) {
		this.charge = charge;
	}

	@JsonProperty("monoisotopic_weight")
	public double getMonoisotopicWeight() {
		return monoisotopicWeight;
	}

	@JsonProperty("monoisotopic_weight")
	public void setMonoisotopicWeight(double monoisotopicWeight) {
		this.monoisotopicWeight = monoisotopicWeight;
	}

	@JsonProperty("molecular_formula")
	public String getMolecularFormula() {
		return molecularFormula;
	}

	@JsonProperty("molecular_formula")
	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	@JsonProperty("iupac_inchi")
	public String getIupacInchi() {
		return iupacInchi;
	}

	@JsonProperty("iupac_inchi")
	public void setIupacInchi(String iupacInchi) {
		this.iupacInchi = iupacInchi;
	}

	@JsonProperty("iupac_inchi_key")
	public String getIupacInchiKey() {
		return iupacInchiKey;
	}

	@JsonProperty("iupac_inchi_key")
	public void setIupacInchiKey(String iupacInchiKey) {
		this.iupacInchiKey = iupacInchiKey;
	}

	@JsonProperty("traditional_name")
	public String getTraditionalName() {
		return traditionalName;
	}

	@JsonProperty("traditional_name")
	public void setTraditionalName(String traditionalName) {
		this.traditionalName = traditionalName;
	}

	@Override
	public String toString() {
		return "PubChemSnapshot [id=" + id + ", monoisotopicWeight="
				+ monoisotopicWeight + ", molecularFormula=" + molecularFormula
				+ ", traditionalName=" + traditionalName + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((molecularFormula == null) ? 0 : molecularFormula.hashCode());
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
		PubChemSnapshot other = (PubChemSnapshot) obj;
		if (molecularFormula == null) {
			if (other.molecularFormula != null)
				return false;
		} else if (!molecularFormula.equals(other.molecularFormula))
			return false;
		return true;
	}

}