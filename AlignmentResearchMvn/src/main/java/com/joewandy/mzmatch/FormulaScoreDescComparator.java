package com.joewandy.mzmatch;

import java.util.Comparator;


/**
 * Compares which formula has the most support
 * @author joewandy
 *
 */
public class FormulaScoreDescComparator implements Comparator<FormulaScore> {

	public int compare(FormulaScore o1, FormulaScore o2) {
		// the normal double comparison, but reverse the sign to get descending order
		return - (o1.getScore().compareTo(o2.getScore()));
	}
	
}
