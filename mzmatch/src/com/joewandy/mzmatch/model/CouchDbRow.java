package com.joewandy.mzmatch.model;


import com.fasterxml.jackson.annotation.JsonProperty;

public class CouchDbRow {
	
	private String id;
	private String key;
	private PubChemSnapshot doc;
	
	@JsonProperty("id")
	public String getId() {
		return id;
	}

	@JsonProperty("id")
	public void setId(String id) {
		this.id = id;
	}
	
	@JsonProperty("key")
	public String getKey() {
		return key;
	}
	
	@JsonProperty("key")
	public void setKey(String key) {
		this.key = key;
	}
	
	@JsonProperty("doc")
	public PubChemSnapshot getDoc() {
		return doc;
	}
	
	@JsonProperty("doc")
	public void setDoc(PubChemSnapshot doc) {
		this.doc = doc;
	}

	@Override
	public String toString() {
		return "CouchDbRow [id=" + id + ", key=" + key + ", doc=" + doc
				+ "]";
	}
	
}
