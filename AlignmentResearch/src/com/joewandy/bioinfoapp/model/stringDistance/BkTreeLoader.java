package com.joewandy.bioinfoapp.model.stringDistance;

import java.util.List;

import com.joewandy.bioinfoapp.model.core.Sequence;
import com.joewandy.bioinfoapp.model.core.io.FastaReader;

/**
 * Loads a BK-Tree with the contents specified in inputFile. The input file
 * should be a FASTA-format file.
 * 
 * @author joewandy
 * 
 */
public class BkTreeLoader {

	private BkTree bkTree;
	private String inputFile;
	private List<Sequence> loadedSequence;

	public BkTreeLoader(BkTree bkTree, String inputFile) {
		this.bkTree = bkTree;
		this.inputFile = inputFile;
		this.loadedSequence = null;
	}

	/**
	 * Loads DNA sequences from the FASTA file specified by inputFile
	 * 
	 * @param inputFile
	 *            The FASTA file containing input sequences
	 * @param fileContent
	 *            Determines whether the file is a DNA or Protein Sequence
	 * @return A BK-Tree containing the sequences loaded from inputFile
	 */
	public BkTree loadTree(FastaReader.FastaFileContent fileContent) {

		// quick and dirty fasta reader
		FastaReader reader = new FastaReader(fileContent, inputFile);

		// actual loading done here
		loadedSequence = reader.processFile();
		bkTree.addItems(loadedSequence);

		return bkTree;

	}

	/**
	 * Returns the loaded sequence
	 * 
	 * @return
	 */
	public List<Sequence> getLoadedSequence() {
		return loadedSequence;
	}

}
