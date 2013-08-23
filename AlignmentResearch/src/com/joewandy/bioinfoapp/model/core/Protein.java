package com.joewandy.bioinfoapp.model.core;

public class Protein implements Sequence {

	private String sequenceString;
	private String id;

	public Protein(String id, String sequenceString) {
		this(sequenceString);
		this.id = id;
	}

	public Protein(String sequenceString) {
		this.sequenceString = sequenceString.replaceAll("\\s", "");
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
