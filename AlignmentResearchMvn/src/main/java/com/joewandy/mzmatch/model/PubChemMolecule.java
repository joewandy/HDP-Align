package com.joewandy.mzmatch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PubChemMolecule {

	private String cid;
	private String molecularFormula;
	private String molecularWeight;
	private String monoisotopicMass;
	private String charge;
	private String inChi;
	private String inChiKey;
	private String iupacName;

	@JsonProperty("CID")
	public String getCid() {
		return cid;
	}

	@JsonProperty("CID")
	public void setCid(String cid) {
		this.cid = cid;
	}

	@JsonProperty("MolecularFormula")
	public String getMolecularFormula() {
		return molecularFormula;
	}

	@JsonProperty("MolecularFormula")
	public void setMolecularFormula(String molecularFormula) {
		this.molecularFormula = molecularFormula;
	}

	@JsonProperty("MolecularWeight")
	public String getMolecularWeight() {
		return molecularWeight;
	}

	@JsonProperty("MolecularWeight")
	public void setMolecularWeight(String molecularWeight) {
		this.molecularWeight = molecularWeight;
	}

	@JsonProperty("MonoisotopicMass")
	public String getMonoisotopicMass() {
		return monoisotopicMass;
	}

	@JsonProperty("MonoisotopicMass")
	public void setMonoisotopicMass(String monoisotopicMass) {
		this.monoisotopicMass = monoisotopicMass;
	}

	@JsonProperty("Charge")
	public String getCharge() {
		return charge;
	}

	@JsonProperty("Charge")
	public void setCharge(String charge) {
		this.charge = charge;
	}

	@JsonProperty("InChIKey")
	public String getInChiKey() {
		return inChiKey;
	}

	@JsonProperty("InChIKey")
	public void setInChiKey(String inChiKey) {
		this.inChiKey = inChiKey;
	}
		
	@JsonProperty("InChI")
	public String getInChi() {
		return inChi;
	}

	@JsonProperty("InChI")
	public void setInChi(String inChi) {
		this.inChi = inChi;
	}

	@JsonProperty("IUPACName")
	public String getIupacName() {
		return iupacName;
	}

	@JsonProperty("IUPACName")
	public void setIupacName(String iupacName) {
		this.iupacName = iupacName;
	}

	// for the purpose of identification, we just check the formula
	
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
		PubChemMolecule other = (PubChemMolecule) obj;
		if (molecularFormula == null) {
			if (other.molecularFormula != null)
				return false;
		} else if (!molecularFormula.equals(other.molecularFormula))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PubChemMolecule [cid=" + cid + ", molecularFormula="
				+ molecularFormula + ", molecularWeight=" + molecularWeight
				+ ", monoisotopicMass=" + monoisotopicMass + ", charge="
				+ charge + ", inChi=" + inChi + ", inChiKey=" + inChiKey
				+ ", iupacName=" + iupacName + "]";
	}

}