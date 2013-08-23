package com.joewandy.bioinfoapp.model.core;

public class PlainText implements Sequence {

	private String plainText;

	public PlainText(String sequenceString) {
		this.plainText = sequenceString;
	}

	public String getSequenceString() {
		return this.plainText;
	}

	public void setSequenceString(String sequenceString) {
		this.plainText = sequenceString;
	}

	public String getId() {
		return "";
	}

	public void setId(String id) {

	}

	@Override
	public String toString() {
		return this.getSequenceString();
	}

}
