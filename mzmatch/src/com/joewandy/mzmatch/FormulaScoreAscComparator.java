package com.joewandy.mzmatch;

import java.util.Comparator;


/**
 * Compares which formula has the most support
 * @author joewandy
 *
 */
public class FormulaScoreAscComparator implements Comparator<FormulaScore> {

	@Override
	public int compare(FormulaScore o1, FormulaScore o2) {
		return o1.getScore().compareTo(o2.getScore());
	}
	
}
