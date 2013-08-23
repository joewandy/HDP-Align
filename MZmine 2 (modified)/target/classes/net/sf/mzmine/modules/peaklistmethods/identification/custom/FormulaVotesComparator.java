package net.sf.mzmine.modules.peaklistmethods.identification.custom;

import java.util.Comparator;

/**
 * Compares which formula has the most votes
 * @author joewandy
 *
 */
public class FormulaVotesComparator implements Comparator<FormulaVotes> {

	@Override
	public int compare(FormulaVotes o1, FormulaVotes o2) {
		// the normal double comparison, but reverse the sign to get descending order
		return - (o1.getVotes().compareTo(o2.getVotes()));
	}
	
}
