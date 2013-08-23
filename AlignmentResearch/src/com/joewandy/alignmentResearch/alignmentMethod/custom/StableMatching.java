package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class StableMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private ExtendedLibrary library;
	private String listId;
	private double massTol;
	private double rtTol;	
	
	public StableMatching(String listId, AlignmentList masterList, AlignmentList childList,
			ExtendedLibrary library,
			double massTol, double rtTol) {			
		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.library = library;
		this.massTol = massTol;
		this.rtTol = rtTol;		
	}

	public AlignmentList getMatchedList() {
			
		// nothing in masterlist to match against, just return the childlist directly
		if (masterList.getRowsCount() == 0) {
			return childList;
		}
		
		// do stable matching here ...
		System.out.println("Running stable matching on " + listId);
		List<AlignmentRow> men = null;
		List<AlignmentRow> women = null;
		if (masterList.getRowsCount() > childList.getRowsCount()) {
			men = masterList.getRows();
			women = childList.getRows();			
			System.out.println("\tmasterList " + masterList.getId() + " = " + masterList.getRowsCount() + " (men)");
			System.out.println("\tchildList " + childList.getId() + " = " + childList.getRowsCount() + " (women)");		
		} else {
			men = childList.getRows();
			women = masterList.getRows();						
			System.out.println("\tmasterList " + masterList.getId() + " = " + masterList.getRowsCount() + " (women)");
			System.out.println("\tchildList " + childList.getId() + " = " + childList.getRowsCount() + " (men)");		
		}

		Map<AlignmentRow, AlignmentRow> stableMatch = match(men, women);
		
		// construct a new list and merge the matched entries together
		System.out.println("\tMerging matched results = " + stableMatch.size() + " entries");
		AlignmentList matchedList = new AlignmentList(listId);
		int rowId = 0;
		int rejectedCount = 0;		
		for (Entry<AlignmentRow, AlignmentRow> match : stableMatch.entrySet()) {

			AlignmentRow row1 = match.getKey();
			AlignmentRow row2 = match.getValue();
			
			// TODO: quick hack. if the matched rows average mass or RT is more than tolerance, ignore this matching 
//			double massDiff = Math.abs(row1.getAverageMz() - row2.getAverageMz());
//			double rtDiff = Math.abs(row1.getAverageRt() - row2.getAverageRt());
//			if (massDiff > massTol) {
//				rejectedCount++;
//				continue;
//			}
			
			row1.setAligned(true);
			row2.setAligned(true);
			
			AlignmentRow merged = new AlignmentRow(rowId++);
			merged.addAlignedFeatures(row1.getFeatures());
			merged.addAlignedFeatures(row2.getFeatures());
			
			matchedList.addRow(merged);

		}
		
		// add everything else that's unmatched
		int menMatchedCount = 0;
		int menUnmatchedCount = 0;
		for (AlignmentRow row : men) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				menUnmatchedCount++;
			} else {
				menMatchedCount++;
			}
		}
		int womenMatchedCount = 0;
		int womenUnmatchedCount = 0;
		for (AlignmentRow row : women) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				womenUnmatchedCount++;
			} else {
				womenMatchedCount++;
			}
		}
		System.out.println("\t\tmen matched rows = " + menMatchedCount);
		System.out.println("\t\tmen unmatched rows = " + menUnmatchedCount);		
		System.out.println("\t\twomen matched rows = " + womenMatchedCount);
		System.out.println("\t\twomen unmatched rows = " + womenUnmatchedCount);
		System.out.println("\tRejected rows = " + rejectedCount);
		return matchedList;

	}
	
    private Map<AlignmentRow, AlignmentRow> match(List<AlignmentRow> men, List<AlignmentRow> women) {

        Queue<RowPreference> freemen = new LinkedList<RowPreference>();

        // Create a free list of men (and use it to store their proposals)
    	System.out.print("\tCreating prefs ");
        int counter = 0;

        for (AlignmentRow man : men) {
            Queue<PreferenceItem> prefs = getPrefs(man, women);
            freemen.add(new RowPreference(man, prefs));
            prefs = null;
            counter++;
            if (counter % 1000 == 0) {
            	System.out.print('.');
            }
        }
        System.out.println();

        // Create an initially empty map of engagements
        Map<AlignmentRow, RowPreference> engagements = new HashMap<AlignmentRow, RowPreference>();

        // As per wikipedia algorithm
        System.out.println("\tStart algorithm");
        int prevSize = 0;
        while (!freemen.isEmpty()) {

        	int size = freemen.size();
        	if (size % 1000 == 0 && size != prevSize) {
        		prevSize = size;
            	System.out.println("\t\tRemaining free men = " + size);        		
        	}
        	
            // The next free man who has a woman to propose to
            RowPreference m = freemen.poll();

            // m's highest ranked woman whom he has not proposed to yet
            PreferenceItem preferredWoman = m.prefs.poll();
            
            // for unequal m and w size. no more w to propose to ?
            if (preferredWoman != null) {

            	AlignmentRow w = preferredWoman.entry;
            	
                // if w is free
                if (!engagements.containsKey(w)) {

                	// (m, w) become engaged
                    engagements.put(w, m);
                
                } else {
                	
                    // some pair (m', w) already exists
                    RowPreference mPrime = engagements.get(w);
                    AlignmentRow mPrimeRow = mPrime.row;
                    AlignmentRow mRow = m.row;
                    WomanPreferenceComparator womanPreference = new WomanPreferenceComparator(w);
                    int compareRes = womanPreference.compare(mPrimeRow, mRow);
                    
                    // w prefers m to m'
                    if (compareRes < 0) {

                        // (m, w) become engaged
                        engagements.put(w, m);

                        // m' becomes free
                        assert(!freemen.contains(mPrime));
                        freemen.add(mPrime);                        	

                    } else {
                    	
                        // (m', w) remain engaged, m remains free
                        assert(!freemen.contains(m));
                    	freemen.add(m);

                    }

                }
            	
            } else {

            	// this man has been refused by all women in his preferences
            	// so will remain unmatched
            	
            }
            
        }
        
        freemen = null;

        // Convert internal data structure to mapping
        Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();
        for (Map.Entry<AlignmentRow, RowPreference> entry : engagements.entrySet())
            matches.put(entry.getValue().row, entry.getKey());
        return matches;
        
    }

	private Queue<PreferenceItem> getPrefs(AlignmentRow man, final List<AlignmentRow> women) {
		Queue<PreferenceItem> prefs = new PriorityQueue<PreferenceItem>(women.size(), new ManPreferenceComparator());
		for (AlignmentRow woman : women) {
//			if (library.exist(man, woman)) {
//				double score = computeLibraryScore(man, woman);
//				PreferenceItem pref = new PreferenceItem(woman, score);
//				prefs.add(pref);				
//			}
			double mass1 = man.getAverageMz();
			double mass2 = woman.getAverageMz();
			if (Math.abs(mass1-mass2) > this.massTol) {
				continue;
			}
			double rt1 = man.getAverageRt();
			double rt2 = woman.getAverageRt();
			double score = computeSimilarity(mass1, mass2, rt1, rt2);
			PreferenceItem pref = new PreferenceItem(woman, score);
			prefs.add(pref);							
		}
		return prefs;
//		return new LinkedList<AlignmentRow>(women);
	}

	private double computeLibraryScore(AlignmentRow row1, AlignmentRow row2) {
		double score = 0;
		if (library.exist(row1, row2)) {
			score = library.computeWeightedRowScore(row1, row2);
//			score = library.computeRowScore(row1, row2);
		}
		return score;
	}
		
	private double computeSimilarity(double mass1, double mass2, double rt1, double rt2) {

		double massDist = Math.pow(mass1-mass2, 2);
		double rtDist = Math.pow(rt1-rt2, 2);
		double euclideanDist = Math.sqrt(massDist + rtDist);
		double similarity = 1/(1+euclideanDist);
		return similarity;
	
	}
    	
    private class RowPreference {
        
    	private final AlignmentRow row;
        private final Queue<PreferenceItem> prefs;

        public RowPreference(AlignmentRow s, Queue<PreferenceItem> p) {
            this.row = s;
            this.prefs = p;
        }

		@Override
		public String toString() {
			return "RowPreference [row=" + row + ", prefs=" + prefs + "]";
		}
        
    }
    
    private class PreferenceItem {
    	
    	private final AlignmentRow entry;
    	private final double score;
    	
    	public PreferenceItem(AlignmentRow preferredRow, double score) {
    		this.entry = preferredRow;
    		this.score = score;
    	}

		@Override
		public String toString() {
			return "PreferenceItem [entry=" + entry + ", score=" + score + "]";
		}
    	
    }
    
    private class ManPreferenceComparator implements Comparator<PreferenceItem>{

    	@Override
    	public int compare(PreferenceItem item1, PreferenceItem item2) {
    		// higher score is preferred, so don't forget to invert the sign
    		return - Double.compare(item1.score, item2.score);
    	}
    	
    }    
    
    private class WomanPreferenceComparator implements Comparator<AlignmentRow>{

    	private AlignmentRow reference;
    	
    	public WomanPreferenceComparator(AlignmentRow reference) {
    		this.reference = reference;
    	}

    	@Override
    	public int compare(AlignmentRow row1, AlignmentRow row2) {
    		double score1 = computeLibraryScore(reference, row1);
    		double score2 = computeLibraryScore(reference, row2);
    		return Double.compare(score1, score2);
    	}
    	
    }    

}
