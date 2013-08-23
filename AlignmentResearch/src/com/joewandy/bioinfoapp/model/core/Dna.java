package com.joewandy.bioinfoapp.model.core;

public class Dna implements Sequence {

	private String sequenceString;
	private String id;

	public Dna(String id, String dnaString) {
		this(dnaString);
		this.id = id;
	}

	public Dna(String sequenceString) {
		this.sequenceString = sequenceString.replaceAll("\\s", "")
				.toUpperCase();
	}

	public String getSequenceString() {
		return this.sequenceString;
	}

	public void setSequenceString(String sequenceString) {
		this.sequenceString = sequenceString;
	}

	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return "Dna [id=" + this.id + ", sequenceString=" + this.sequenceString
				+ "]";
	}

}
