package com.joewandy.bioinfoapp.model.partialDigest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestrictionSiteAnalysis {

	public static void main(String[] args) {

		DigestedDna dna = new DigestedDna(
				"cgcgtggcgt ggttttcagg tttacgcctg gtagaacgtt gcgagctgaa tcgcttaacc"
						+ "tggtgatttc taaaagaagt tttttgcatg gtattttcag agattatgaa ttgccgcatt"
						+ "atagcctaat aacgcgcatc tttcatgacg gcaaacaata gggtagtatt gacaagccaa"
						+ "ttacaaatca ttaacaaaaa attgctctaa agcatccgta tcgcaggacg caaacgcata"
						+ "tgcaacgtgg tggcagacga gcaaaccagt agcgctcgaa ggagaggtga atggaaagta"
						+ "aagtagttgt tccggcacaa ggcaagaaga tcaccctgca aaacggcaaa ctcaacgttc"
						+ "ctgaaaatcc gattatccct tacattgaag gtgatggaat cggtgtagat gtaaccccag"
						+ "ccatgctgaa agtggtcgac gctgcagtcg agaaagccta taaaggcgag cgtaaaatct"
						+ "cctggatgga aatttacacc ggtgaaaaat ccacacaggt ttatggtcag gacgtctggc"
						+ "tgcctgctga aactcttgat ctgattcgtg aatatcgcgt tgccattaaa ggtccgctga"
						+ "ccactccggt tggtggcggt attcgctctc tgaacgttgc cctgcgccag gaactggatc"
						+ "tctacatctg cctgcgtccg gtacgttact atcagggcac tccaagcccg gttaaacacc"
						+ "ctgaactgac cgatatggtt atcttccgtg aaaactcgga agacatttat gcgggtatcg"
						+ "aatggaaagc agactctgcc gacgccgaga aagtgattaa attcctgcgt gaagagatgg"
						+ "gggtgaagaa aattcgcttc ccggaacatt gtggtatcgg tattaagccg tgttcggaag"
						+ "aaggcaccaa acgtctggtt cgtgcagcga tcgaatacgc aattgctaac gatcgtgact"
						+ "ctgtgactct ggtgcacaaa ggcaacatca tgaagttcac cgaaggagcg tttaaagact"
						+ "ggggctacca gctggcgcgt gaagagtttg gcggtgaact gatcgacggt ggcccgtggc"
						+ "tgaaagttaa aaacccgaac actggcaaag agatcgtcat taaagacgtg attgctgatg"
						+ "cattcctgca acagatcctg ctgcgtccgg ctgaatatga tgttatcgcc tgtatgaacc"
						+ "tgaacggtga ctacatttct gacgccctgg cagcgcaggt tggcggtatc ggtatcgccc"
						+ "ctggtgcaaa catcggtgac gaatgcgccc tgtttgaagc cacccacggt actgcgccga"
						+ "aatatgccgg tcaggacaaa gtaaatcctg gctctattat tctctccgct gagatgatgc"
						+ "tgcgccacat gggttggacc gaagcggctg acttaattgt taaaggtatg gaaggcgcaa"
						+ "tcaacgcgaa aaccgtaacc tatgacttcg agcgtctgat ggatggcgct aaactgctga"
						+ "aatgttcaga gtttggtgac gcgatcatcg aaaacatgta atgccgtagt ttgttaaatt"
						+ "tattaacg");

		dna.addRestrictionSite(0);
		dna.addRestrictionSite(100);
		dna.addRestrictionSite(302);
		dna.addRestrictionSite(539);
		dna.addRestrictionSite(899);
		dna.addRestrictionSite(1211);
		dna.addRestrictionSite(1400);

		// Compute the partial digest fragments
		List<RestrictionFragment> fragmentList = dna
				.getPartialDigestFragments();

		System.out.println(dna);
		System.out.println("\nRestriction sites: " + dna.getRestrictionSites());
		System.out.println("Restriction site fragments: " + fragmentList);

		// Solve the Partial Digest Problem using brute force method
		// to reconstruct the restriction site mapping back
		System.out.println("\nPerforming restriction site analysis");
		RestrictionSiteAnalysis rm = new RestrictionSiteAnalysis();

		List<List<RestrictionSite>> allSolutions = new ArrayList<List<RestrictionSite>>();
		rm.partialDigestProblem(fragmentList, allSolutions);
		System.out.println("All possible restriction maps: " + allSolutions);

	}

	/**
	 * Brute-force partial digest problem Jones & Pevzner, 2004, p. 90
	 * 
	 * PARTIALDIGEST(L) 1 width <- Maximum element in L 2 DELETE(width, L) 3 X
	 * <- {0, width} 4 PLACE(L, X)
	 * 
	 * @param L
	 *            The multiset of pairwise distance L
	 * @param solutions
	 *            The set of all possible solutions, each element denoted by X,
	 *            such that Delta X = L
	 */
	public void partialDigestProblem(List<RestrictionFragment> L,
			List<List<RestrictionSite>> solutions) {

		RestrictionFragment width = this.findMax(L); // 1

		L.remove(width); // 2

		List<RestrictionSite> X = new ArrayList<RestrictionSite>(); // 3
		X.add(new RestrictionSite(0));
		X.add(new RestrictionSite(width.getDistance()));

		place(L, X, width, solutions); // 4

	}

	/**
	 * Brute-force partial digest problem Jones & Pevzner, 2004, p. 90
	 * 
	 * PLACE(L, X) 1 if L is empty 2 output X 3 return 4 y <- Maximum element in
	 * L 5 if Delta(y, X) subset of L 6 Add y to X and remove lengths Delta(y,
	 * X) from L 7 PLACE(L, X) 8 Remove y from X and add lengths Delta(y, X) to
	 * L 9 if Delta(width-y, X) subset of L 10 Add width-y to X and remove
	 * lengths Delta(width-y, X) from L 11 PLACE(L, X) 12 Remove width-y from X
	 * and add lengths Delta(width-y, X) 13 return
	 * 
	 * @param L
	 *            The multiset of pairwise distance L
	 * @return A set X, of n integers, such that Delta X = L
	 */
	private void place(List<RestrictionFragment> L, List<RestrictionSite> X,
			RestrictionFragment width, List<List<RestrictionSite>> solutions) {

		if (L.isEmpty()) { // 1 - 3

			Collections.sort(X);

			// necessary to perform shallow copy of X first before adding to
			// solutions
			if (!solutions.contains(X)) {
				List<RestrictionSite> solution = new ArrayList<RestrictionSite>(
						X);
				solutions.add(solution);
			}

			return;
		}

		RestrictionFragment y = this.findMax(L); // 4

		Integer partialDigestDistance = y.getDistance(); // 5
		List<Integer> deltaList = this.getDelta(partialDigestDistance, X);
		if (this.isDistanceSubsetOf(deltaList, L)) {
			recurse(L, X, width, partialDigestDistance, deltaList, solutions);
		}

		partialDigestDistance = width.getDistance() - y.getDistance(); // 9
		deltaList = this.getDelta(partialDigestDistance, X);
		if (this.isDistanceSubsetOf(deltaList, L)) {
			recurse(L, X, width, partialDigestDistance, deltaList, solutions);
		}

	}

	/**
	 * Add every partial digest distance as restriction site and remove them
	 * from the partial digest list. Recurse using PLACE to check that a
	 * solution has been found. Upon backtracking, restore the values inside X
	 * and L to be used in the next recursive call.
	 * 
	 * @param L
	 * @param X
	 * @param width
	 * @param partialDigestDistance
	 * @param deltaList
	 */
	private void recurse(List<RestrictionFragment> L, List<RestrictionSite> X,
			RestrictionFragment width, Integer partialDigestDistance,
			List<Integer> deltaList, List<List<RestrictionSite>> solutions) {

		// Add computed distances as restriction site and remove them
		// from partial digest segments
		X.add(new RestrictionSite(partialDigestDistance));
		List<RestrictionFragment> removed = this.removePartialDigests(
				deltaList, L);

		this.place(L, X, width, solutions);

		// Restore back for the next recursive call
		X.remove(new RestrictionSite(partialDigestDistance));
		L.addAll(removed);

	}

	/**
	 * Find the partial digest element from List L that has the largest distance
	 * 
	 * @param L
	 *            A list of partial digest segments
	 * @return The partial digest element from List L that has the largest
	 *         distance
	 */
	private RestrictionFragment findMax(List<RestrictionFragment> L) {

		RestrictionFragment maxDigest = new RestrictionFragment(0);
		for (int i = 0; i < L.size(); i++) {
			if (L.get(i).getDistance() > maxDigest.getDistance()) {
				maxDigest = L.get(i);
			}
		}
		return maxDigest;

	}

	/**
	 * Calculate the distance from startPoint to every element in X
	 * 
	 * @param partialDigestDistance
	 *            The distance of a partial digest segment to the start of the
	 *            DNA sequence
	 * @param X
	 *            The list of restriction sites
	 * @return A list of distances from startPoint to every element in X
	 */
	private List<Integer> getDelta(Integer partialDigestDistance,
			List<RestrictionSite> X) {

		List<Integer> resultList = new ArrayList<Integer>();

		for (RestrictionSite rs : X) {
			Integer distance = java.lang.Math.abs(rs.getLocation()
					- partialDigestDistance);
			resultList.add(distance);
		}

		return resultList;

	}

	/**
	 * Check that all distances in distanceList correspond to each restriction
	 * site in X
	 * 
	 * @param distanceList
	 *            The list of distances
	 * @param L
	 *            The list of partial digest segments
	 * @return
	 */
	private boolean isDistanceSubsetOf(List<Integer> distanceList,
			List<RestrictionFragment> L) {

		boolean result = true;

		for (Integer distance : distanceList) {
			if (!L.contains(new RestrictionFragment(distance))) {
				result = false;
				break;
			}
		}

		return result;

	}

	/**
	 * Remove every partial digest segments in L that has the distance specified
	 * by delta
	 * 
	 * @param delta
	 *            The list of distances
	 * @param L
	 *            The list of partial digest segments
	 * @return
	 */
	private List<RestrictionFragment> removePartialDigests(List<Integer> delta,
			List<RestrictionFragment> L) {

		List<RestrictionFragment> result = new ArrayList<RestrictionFragment>();

		for (Integer distance : delta) {
			int indexToRemove = L.indexOf(new RestrictionFragment(distance));
			result.add(L.remove(indexToRemove));
		}

		return result;

	}

}
