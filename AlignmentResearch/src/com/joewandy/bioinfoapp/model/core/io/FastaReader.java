package com.joewandy.bioinfoapp.model.core.io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.bioinfoapp.model.core.Dna;
import com.joewandy.bioinfoapp.model.core.Protein;
import com.joewandy.bioinfoapp.model.core.Sequence;

/**
 * A quick and dirty FASTA reader
 * 
 * @author joewandy
 * 
 */
public class FastaReader {

	// TODO: validate acceptable symbols in file parsed based on content type
	public enum FastaFileContent {
		DNA_SEQUENCE, PROTEIN_SEQUENCE
	};

	private FastaFileContent contentType;
	private String inputFile;
	private List<Sequence> sequenceList;

	public FastaReader(FastaFileContent contentType, String inputFile) {
		this.contentType = contentType;
		this.inputFile = inputFile;
		this.sequenceList = new ArrayList<Sequence>();
	}

	public Sequence getFirstSequence() {
		if (sequenceList.size() < 1) {
			return null;
		}
		return sequenceList.get(0);
	}

	public List<Sequence> processFile() {

		BufferedReader fileReader = null;
		String line = null;
		String id = "";
		StringBuffer contentBuffer = new StringBuffer();

		try {

			fileReader = new BufferedReader(new FileReader(inputFile));
			do {

				line = fileReader.readLine();
				if (line != null) {

					line = line.trim();
					if (line.isEmpty()) {
						continue;
					}
					char firstChar = line.charAt(0);
					if (firstChar == '>') {

						// save the previous sequence read
						addToSequenceList(id, contentBuffer);

						// now can get the new id > ..
						id = line.substring(1).trim();

						// start a new content buffer
						contentBuffer = new StringBuffer();

					} else if (firstChar == ';') {

						// comment line, skip it

					} else {

						// carry on reading sequence content
						contentBuffer.append(line.trim());

					}

				} else {

					// save the final sequence content
					addToSequenceList(id, contentBuffer);

				}

			} while (line != null);

		} catch (FileNotFoundException e) {
			System.out.println("File " + inputFile + " is not found");
			System.exit(1);
		} catch (IOException e) {
			System.out.println("An IO error has occured: " + e.getMessage());
			System.exit(1);
		} finally {
			if (fileReader != null) {
				try {
					fileReader.close();
				} catch (IOException e) {
					// do nothing
				}
			}
		}

		return sequenceList;

	}

	private void addToSequenceList(String id, StringBuffer sb) {
		if (sb.length() != 0) {
			Sequence s = null;
			String content = sb.toString();
			if (this.contentType == FastaFileContent.DNA_SEQUENCE) {
				s = new Dna(id, content);
			} else if (this.contentType == FastaFileContent.PROTEIN_SEQUENCE) {
				s = new Protein(id, content);
			}
			this.sequenceList.add(s);
		}
	}

}
