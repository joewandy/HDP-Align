package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.WeightedRowVsRowScore;

public class StableMatching implements FeatureMatching {

	private static final int LIMIT = 1;
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
		List<AlignmentRow> men = masterList.getRows();
		List<AlignmentRow> women = childList.getRows();			
//		List<AlignmentRow> men = childList.getRows();
//		List<AlignmentRow> women = masterList.getRows();			
		System.out.println("\tmasterList " + masterList.getId() + " = " + masterList.getRowsCount() + " (men)");
		System.out.println("\tchildList " + childList.getId() + " = " + childList.getRowsCount() + " (women)");		
		Map<AlignmentRow, AlignmentRow> stableMatch = match(men, women);
		
		// construct a new list and merge the matched entries together
		System.out.println("\tMerging matched results = " + stableMatch.size() + " entries");
		AlignmentList matchedList = new AlignmentList(listId);
		int rowId = 0;
		int rejectedCount = 0;		
		for (Entry<AlignmentRow, AlignmentRow> match : stableMatch.entrySet()) {

			AlignmentRow row1 = match.getKey();
			AlignmentRow row2 = match.getValue();
						
			row1.setAligned(true);
			row2.setAligned(true);
			
			AlignmentRow merged = new AlignmentRow(masterList, rowId++);
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
	
    private Map<AlignmentRow, AlignmentRow> match(List<AlignmentRow> men, final List<AlignmentRow> women) {

        Queue<RowPreference> freemen = new LinkedList<RowPreference>();

        // Create a free list of men (and use it to store their proposals)
        double[][] scoreMatrix = computeScoreMatrix(men, women);
        
    	System.out.print("\tCreating prefs ");
        for (int i = 0; i < men.size(); i++) {
        	
        	Queue<PreferenceItem> sorted = getSortedPrefs(women, scoreMatrix, i);
        	AlignmentRow man = men.get(i);
        	freemen.add(new RowPreference(man, sorted));
            
        	if (i % 1000 == 0) {
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

            	int rowIdx = preferredWoman.rowIdx;
            	AlignmentRow w = women.get(rowIdx);
            	
            	// FIXME: hard cut-off here improves precision & recall quite a lot
            	// can we do better than this ?
//            	double dist = preferredWoman.dist;
//            	if (dist > LIMIT) {
//            		// no partner for him
//            	} else {            	
            	
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
	                
//            	}
            	
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

	private Queue<PreferenceItem> getSortedPrefs(List<AlignmentRow> women,
			double[][] scoreMatrix, int i) {
		
		double[] womenScore = scoreMatrix[i];
		List<PreferenceItem> prefs = new ArrayList<PreferenceItem>();
		for (int j = 0; j < womenScore.length; j++) {
			double score = womenScore[j];
			PreferenceItem pref = new PreferenceItem(j, score);
			prefs.add(pref);
		}
		Queue<PreferenceItem> sorted = new PriorityQueue<PreferenceItem>(prefs.size(), new ManPreferenceComparator());
		sorted.addAll(prefs);
		return sorted;

	}

	private double[][] computeScoreMatrix(List<AlignmentRow> men,
			List<AlignmentRow> women) {

    	System.out.print("\tComputing score matrix ");
		double[][] scoreMatrix = new double[men.size()][women.size()];
		for (int i = 0; i < men.size(); i++) {
			
			for (int j = 0; j < women.size(); j++) {
				AlignmentRow man = men.get(i);
				AlignmentRow woman = women.get(j);
				double score = 0;
				if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
					score = library.computeWeightedRowScore(man, woman);					
				} else {
					score = library.computeRowScore(man, woman);
				}
				scoreMatrix[i][j] = score;				
			}
			
            if (i % 1000 == 0) {
            	System.out.print('.');
            }
			
		}
		System.out.println();
		
		return scoreMatrix;
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

    	private int rowIdx;
    	private final double score;
    	
    	public PreferenceItem(int idx, double score) {
    		this.rowIdx = idx;
    		this.score = score;
    	}

		@Override
		public String toString() {
			return "PreferenceItem [entry=" + rowIdx + ", score=" + score + "]";
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
    	public int compare(AlignmentRow candidate1, AlignmentRow candidate2) {
    		WeightedRowVsRowScore rowScore1 = new WeightedRowVsRowScore(
    				reference, candidate1, library, massTol, rtTol);
    		WeightedRowVsRowScore rowScore2 = new WeightedRowVsRowScore(
    				reference, candidate2, library, massTol, rtTol);
    		double score1 = rowScore1.getScore();
    		double score2 = rowScore2.getScore();
    		return Double.compare(score1, score2);
    	}
    	
    }        

}
