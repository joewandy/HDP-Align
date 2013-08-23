package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.WeightedRowVsRowScore;

public class GroupingInfoAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentList> featureList;
	private ExtendedLibrary library;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public GroupingInfoAlignment(List<AlignmentFile> dataList, double massTolerance, double rtTolerance) {

		super(dataList, massTolerance, rtTolerance);
		
		featureList = new ArrayList<AlignmentList>();
		for (AlignmentFile data : dataList) {
			// construct list of features in rows, based on data
			AlignmentList newList = new AlignmentList(data);
			featureList.add(newList);
		}
		
        /*
         * 0. Construct libraries of possible pairwise alignments
         */
        if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
    		ExtendedLibraryBuilder builder = new ExtendedLibraryBuilder(dataList, massTolerance, rtTolerance, 
    				FeatureXMLAlignment.RTWINDOW_MULTIPLY);    		
    		Map<Double, List<AlignmentLibrary>> metaLibraries = builder.buildPrimaryLibrary();
    		ExtendedLibrary extendedLibrary = builder.combineLibraries(metaLibraries);
//    		ExtendedLibrary extendedLibrary = builder.extendLibrary(builder.combineLibraries(metaLibraries));
    		this.library = extendedLibrary;
        }
		
	}
	
	/**
	 * Clone of joinAlign implemented in mzMine
	 * @return
	 */
	public AlignmentList matchFeatures() {

		AlignmentList masterList = new AlignmentList("");
		int newRowId = 0;

		for (int i = 0; i < featureList.size(); i++) {

			AlignmentList peakList = featureList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + peakList);
			
			/*
			 * 1. Calculate scores for every entry in this peaklist against our master list.
			 */
			
            List<WeightedRowVsRowScore> scoreList = new ArrayList<WeightedRowVsRowScore>();
            List<AlignmentRow> allRows = peakList.getRows();
            for (AlignmentRow reference : allRows) {

            	/*
            	 * Get all rows of the aligned peaklist within parameter limits,
            	 * calculate scores and store them.
            	 * 
            	 * If this is the first iteration of the outermost loop (the first file aligned),
            	 * then candidateRows is always empty .. so effectively we're skipping the code
            	 * below this comment.
            	 * 
            	 * Remember that candidateRows come from the master list, so later 
            	 * we're going to successively merge entries (rows) in this featureList to the 
            	 * candidateRows in the master list.
            	 */
            	Set<AlignmentRow> candidateRows = masterList.getRowsInRange(reference, 
            			this.massTolerance, this.rtTolerance, this.usePpm);
            	for (AlignmentRow candidate : candidateRows) {
            		WeightedRowVsRowScore score = new WeightedRowVsRowScore(reference, candidate, this.massTolerance/2, this.rtTolerance/2,
            				FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE, this.library);
            		scoreList.add(score);
            	}
            	
            }

            /*
             * 2. Next, maps between rows and their scores. For the first iteration,
             * since nothing is inside scoreQueue, nothing will happen.
             */
            
            Map<AlignmentRow, AlignmentRow> alignmentMapping = mapping(scoreList);

            /*
             * 3. Here, we actually align using the mapping created above.
             * if we cannot find the mapping, then add this row 
             */
            
            // Align all rows using mapping
            for (AlignmentRow row : allRows) {
            	
            	AlignmentRow referenceRow = alignmentMapping.get(row);
            	
                // If we have no mapping for this row, add as new entry to masterList
                if (referenceRow == null) {
                    referenceRow = new AlignmentRow(newRowId);
                    newRowId++;
                    masterList.addRow(referenceRow);
                }
                
                // Add all peaks from the original row to the aligned row
                referenceRow.addFeatures(row.getFeatures());
            	
            }
            
		}
		
		return masterList;
		
	}
	
	protected Map<AlignmentRow, AlignmentRow> mapping(
			List<WeightedRowVsRowScore> scoreList) {
		
		// Create a table of mappings for best scores
		Map<AlignmentRow, AlignmentRow> stableMatch = new HashMap<AlignmentRow, AlignmentRow> ();
		if (scoreList.isEmpty()) {
			return stableMatch;			
		}
		
		// do stable matching
		System.out.println("Running stable matching");
		Set<AlignmentRow> men = new HashSet<AlignmentRow>();
		Set<AlignmentRow> women = new HashSet<AlignmentRow>();

		// Iterate scores by ascending order, lower score (lower difference) is better match
		for (WeightedRowVsRowScore score : scoreList) {
		    AlignmentRow man = score.getMasterListCandidate();
		    AlignmentRow woman = score.getReference();
		    // men is master list
		    men.add(man);
		    // women is aligned (reference) list
		    women.add(woman);
		}		
		System.out.println("\tmen size = " + men.size());
		System.out.println("\twomen size = " + women.size());		
		
		List<AlignmentRow> menList = new ArrayList<AlignmentRow>(men);
		List<AlignmentRow> womenList = new ArrayList<AlignmentRow>(women);
		stableMatch = match(menList, womenList, scoreList);
		
		return stableMatch;

	}
	
    private Map<AlignmentRow, AlignmentRow> match(List<AlignmentRow> men, List<AlignmentRow> women, 
    		List<WeightedRowVsRowScore> scoreList) {

        Queue<RowPreference> freemen = new LinkedList<RowPreference>();

        // Create a free list of men (and use it to store their proposals)
    	System.out.print("\tCreating prefs ");
        int counter = 0;

        for (AlignmentRow man : men) {
            Queue<PreferenceItem> prefs = getPrefs(man, scoreList);
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
        for (AlignmentRow key : engagements.keySet()) {
        	RowPreference rowPref = engagements.get(key);
            matches.put(key, rowPref.row);
        }
        return matches;
        
    }
    
	private Queue<PreferenceItem> getPrefs(AlignmentRow man, final List<WeightedRowVsRowScore> scoreList) {
		Queue<PreferenceItem> prefs = new PriorityQueue<PreferenceItem>(11, new ManPreferenceComparator());
		for (WeightedRowVsRowScore rowScore : scoreList) {
			if (rowScore.getMasterListCandidate().equals(man)) {
				double score = rowScore.getScore();
				AlignmentRow woman = rowScore.getReference();
				PreferenceItem pref = new PreferenceItem(woman, score);
				prefs.add(pref);				
			}
		}
		return prefs;
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
    		// higher score is now preferred, so invert the sign
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
    		WeightedRowVsRowScore rowScore1 = new WeightedRowVsRowScore(reference, candidate1, massTolerance/2, rtTolerance/2,
    				FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE, library);
    		WeightedRowVsRowScore rowScore2 = new WeightedRowVsRowScore(reference, candidate2, massTolerance/2, rtTolerance/2,
    				FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE, library);
    		double score1 = rowScore1.getScore();
    		double score2 = rowScore2.getScore();
    		return Double.compare(score1, score2);
    	}
    	
    }        
		
}
