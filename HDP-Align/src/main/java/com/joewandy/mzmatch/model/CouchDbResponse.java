package com.joewandy.mzmatch.model;

import java.util.Arrays;


import com.fasterxml.jackson.annotation.JsonProperty;

public class CouchDbResponse {

	private int totalRows;
	private int offset;
	private CouchDbRow[] rows;

	@JsonProperty("total_rows")
	public int getTotalRows() {
		return totalRows;
	}

	@JsonProperty("total_rows")
	public void setTotalRows(int totalRows) {
		this.totalRows = totalRows;
	}

	@JsonProperty("offset")
	public int getOffset() {
		return offset;
	}

	@JsonProperty("offset")
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@JsonProperty("rows")
	public CouchDbRow[] getRows() {
		return rows;
	}

	@JsonProperty("rows")
	public void setRows(CouchDbRow[] rows) {
		this.rows = rows;
	}

	@Override
	public String toString() {
		return "CouchDbResponse [totalRows=" + totalRows + ", offset=" + offset
				+ ", rows=" + Arrays.toString(rows) + "]";
	}
	
}