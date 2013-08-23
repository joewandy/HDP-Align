package com.joewandy.mzmatch.old;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PugFormulaToListKey {

	private String listKey;
	private String message;
	
	@JsonProperty("ListKey")
	public String getListKey() {
		return listKey;
	}
	
	@JsonProperty("ListKey")
	public void setListKey(String listKey) {
		this.listKey = listKey;
	}
	
	@JsonProperty("Message")
	public String getMessage() {
		return message;
	}
	
	@JsonProperty("Message")
	public void setMessage(String message) {
		this.message = message;
	}
	
}
