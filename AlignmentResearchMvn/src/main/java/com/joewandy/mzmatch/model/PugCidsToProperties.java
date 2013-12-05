package com.joewandy.mzmatch.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PugCidsToProperties {
	
	private PubChemMolecule[] properties;

	@JsonProperty("Properties")
	public PubChemMolecule[] getProperties() {
		return properties;
	}

	@JsonProperty("Properties")
	public void setProperties(PubChemMolecule[] properties) {
		this.properties = properties;
	}
	
}
