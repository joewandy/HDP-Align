package com.joewandy.bioinfoapp.main;

import com.joewandy.bioinfoapp.model.stringDistance.EditDistance;
import com.joewandy.bioinfoapp.model.stringDistance.HammingDistance;
import com.joewandy.bioinfoapp.model.stringDistance.LevenshteinDistance;

public class TestEditDistance {

	public static void main(String[] args) {

		String sequence1 = "ATGCGGACTAATGGCAT";
		String sequence2 = "ATGGGACTATGGCATTT";	// 2 deletions, 2 insertions

		System.out.println("sequence1 = " + sequence1);
		System.out.println("sequence2 = " + sequence2);

		EditDistance hammingDistance = new HammingDistance(sequence1, sequence2);
		System.out.println("The Hamming distance is "
				+ hammingDistance.getDistance());

		EditDistance levenshteinDistance = new LevenshteinDistance(sequence1,
				sequence2);
		System.out.println("The Levenshtein distance is "
				+ levenshteinDistance.getDistance());
		System.out.printf("The Levenshtein edit similarity is %.3f\n",
				levenshteinDistance.getDistanceSimilarity());

	}

}
