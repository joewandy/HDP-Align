package com.joewandy.mzmatch.old;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PugListKeyToCid {

	private String[] cid;
	
	@JsonProperty("CID")
	public String[] getCid() {
		return cid;
	}

	@JsonProperty("CID")
	public void setCid(String[] cid) {
		this.cid = cid;
	}
	
}
