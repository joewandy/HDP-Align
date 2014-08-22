package net.sf.mzmine.modules.peaklistmethods.identification.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.mzmine.data.PeakIdentity;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;

public class CountVotes {

    private Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static final String DB_SOURCE_STANDARD = "standard";
	public static final String DB_SOURCE_EXTRA = "extra";
	
	private static final double VOTES_THRESHOLD = 1.0;
	private PeakList peakList;
	private Map<FormulaVotes, FormulaVotes> votes;

	public CountVotes(PeakList peakList) {
		this.peakList = peakList;
		this.votes = new HashMap<FormulaVotes, FormulaVotes>();
	}
	
	public List<FormulaVotes> countVotes(final String databaseName, Map<String, String> ids) {
		
		for (PeakListRow peakRow : peakList.getRows()) {

			PeakIdentity[] identities = peakRow.getPeakIdentities();
			int length = identities.length;
			
			for (PeakIdentity identity : identities) {
				
				String molId = identity.getPropertyValue(PeakIdentity.PROPERTY_ID);
				String dbId = identity.getPropertyValue(PeakIdentity.PROPERTY_METHOD);
				String form = identity.getPropertyValue(PeakIdentity.PROPERTY_FORMULA);
				FormulaVotes lookup = new FormulaVotes(molId, dbId, form);

				if (votes.containsKey(lookup)) {
					// increase votes count of formula if it's already there
					votes.get(lookup).incrementVotes(length);
				} else {
					// otherwise, initialize new votes
					lookup.newVotes(length);
					votes.put(lookup, lookup);
				}
				
			}
			
		}
		
		List<FormulaVotes> sortedVotes = new ArrayList<FormulaVotes>(this.votes.values());
		Collections.sort(sortedVotes, new FormulaVotesComparator());

		List<FormulaVotes> positives = new ArrayList<FormulaVotes>();
		List<FormulaVotes> negatives = new ArrayList<FormulaVotes>();
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;
		for (FormulaVotes f : sortedVotes) {
			// System.out.println(f);
			if (f.getVotes().compareTo(VOTES_THRESHOLD) == 0 || f.getVotes().compareTo(VOTES_THRESHOLD) > 0) {

				// check formula votes >= threshold (1), then it's positive
				positives.add(f);

				if (databaseName.equals(f.getDbId())) {
					if (ids.containsKey(f.getMolId())) {
						// in the standard database, so it's true positive
						tp++;						
					}
				} else {
					// not in the standard database, so it's a false positive
					fp++;
				}
				
			} else {
				
				// otherwise below threshold, then it's negative
				negatives.add(f);
				if (databaseName.equals(f.getDbId())) {
					if (ids.containsKey(f.getMolId())) {
						// in the standard database, so it's false negative
						fn++;
					}
				} else {
					// not in the standard database, so it's a true negative
					tn++;
				}
				
			}	
		}
		
		double tpr = ((double) tp) / (tp + fn);
		double fpr = 1 - ((double) tn) / (tn + fp);
		double f1 = ((double) (2*tp)) / ( (2*tp) + fp + fn );
		String tprStr = String.format("%.4f", tpr);
		String fprStr = String.format("%.4f", fpr);
		String f1Str = String.format("%.4f", f1);
		logger.finest("CountVotes:" + peakList.getName() + "," + tp + "," + fp + "," + tn + "," + fn 
				+ "," + tprStr + "," + fprStr + "," + f1Str);		

		return sortedVotes;
		
	}
	
}
