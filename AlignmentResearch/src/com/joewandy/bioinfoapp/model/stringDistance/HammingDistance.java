package com.joewandy.bioinfoapp.model.stringDistance;

public class HammingDistance implements EditDistance {

	private String s1;
	private String s2;

	public HammingDistance(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
	}

	/**
	 * Compute the Hamming distance between the two strings <code>s1</code> and
	 * <code>s2</code>. The two strings to be computed must be of equal length
	 * and the Hamming distance is defined to be the number of positions where
	 * the characters are different.
	 * 
	 * @return The Hamming distance
	 */
	@Override
	public int getDistance() {

		// check preconditions
		if (s1 == null || s2 == null || s1.length() != s2.length()) {
			throw new IllegalArgumentException();
		}

		// compute hamming distance
		int distance = 0;
		for (int i = 0; i < s1.length(); i++) {
			if (s1.charAt(i) != s2.charAt(i)) {
				distance++;
			}
		}
		return distance;

	}

	@Override
	public double getDistanceSimilarity() {
		// TODO Not sure how to define this ?
		return 0;
	}

}
