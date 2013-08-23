package com.joewandy.bioinfoapp.model.core;

/**
 * An interface that represents biological sequences. Typically either
 * nucleotide or amino acid sequences. A typical sequence has an identifier id
 * and the content sequence string.
 * 
 * @author joewandy
 * 
 */
public interface Sequence {

	/** Get the id of this sequence. Could be the accession number, or FASTA id. */
	public String getId();

	/** Set the id of this sequence */
	public void setId(String id);

	/** Get the sequence content */
	public String getSequenceString();

	/** Set the sequence content */
	public void setSequenceString(String sequence);

}
