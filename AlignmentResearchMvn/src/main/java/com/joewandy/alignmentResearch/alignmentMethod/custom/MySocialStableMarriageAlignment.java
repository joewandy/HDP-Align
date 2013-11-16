package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.Collections;
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

import no.uib.cipr.matrix.DenseMatrix;
import peakml.chemistry.PeriodicTable;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.SocialGraph;
import com.joewandy.alignmentResearch.objectModel.SocialGraphEdge;
import com.joewandy.alignmentResearch.objectModel.WeightedRowVsRowScore;

public class MySocialStableMarriageAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentList> featureList;
	private double threshold;
	
	public MySocialStableMarriageAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		
		featureList = new ArrayList<AlignmentList>();
		for (AlignmentFile data : dataList) {
			// construct list of features in rows, based on data
			AlignmentList newList = new AlignmentList(data);
			featureList.add(newList);
		}
		
		threshold = AlignmentMethodParam.PARAM_FRIENDLY_THRESHOLD;
				
	}
	
	public AlignmentList matchFeatures() {

		AlignmentList masterList = new AlignmentList("");
		int newRowId = 0;

		for (int i = 0; i < featureList.size(); i++) {

			AlignmentList alignedList = featureList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + alignedList);
			
			// first iteration, just set this list to be the masterlist
			if (masterList.getRowsCount() == 0) {
				masterList = alignedList;
				continue;
			}
						
			/*
			 * 1a. Calculate scores for every entry in this peaklist against our master list.
			 * Scores are calculated for the pairs that are within tolerance windows. Each entries
			 * form an acceptable pair of rows that can potentially be matched.
			 * 
             * 1b. Construct a social graph of pairs that are 'connected' across the two lists.
             * This forms the list of acquainted pairs, sharing some form of relationships outside the matching.
             * Acquainted pairs are given higher priority in the matching, and they can form 
             * socially stable blocking pairs.
			 */
	        List<WeightedRowVsRowScore> acceptablePairsList = createAcceptablePairsList(masterList, alignedList);
	        SocialGraph socialGraph = createSocialGraph(masterList, alignedList, acceptablePairsList);
            
            /*
             * 2. Finally, create the 3/2-approximately maximum social stable matching
             */
            
		    // men is master list
    		List<AlignmentRow> men = masterList.getRows();
		    // women is aligned list
    		List<AlignmentRow> women = alignedList.getRows();
    		
    		// do stable matching
    		System.out.println("Running maximum socially stable matching");
    		System.out.println("\tmen size = " + men.size());
    		System.out.println("\twomen size = " + women.size());
    		System.out.println("\tacceptable pairs = " + acceptablePairsList.size());
    		System.out.println("\tsocial graph = " + socialGraph.getEdgeCount() + " edges");
    		
    		Map<AlignmentRow, AlignmentRow> alignmentMapping = approxSmiss(men, women, 
    				acceptablePairsList, socialGraph);

            /*
             * 3. Here, we actually construct the row alignments using the mapping created above.
             * if we cannot find the mapping, then add this row 
             */
            
            // Align all rows using mapping
    		List<AlignmentRow> alignedRows = alignedList.getRows();
            for (AlignmentRow alignedRow : alignedRows) {
            	
            	AlignmentRow masterListRow = alignmentMapping.get(alignedRow);
            	
                // If we have no mapping for this row, add as new entry to masterList
                if (masterListRow == null) {
                    masterListRow = new AlignmentRow(masterList, newRowId);
                    newRowId++;
                    masterList.addRow(masterListRow);
                }
                
                // Add all peaks from the original row to the aligned row
                masterListRow.addFeatures(alignedRow.getFeatures());
            	
            }
            
		}
		
		return masterList;
		
	}

	private List<WeightedRowVsRowScore> createAcceptablePairsList(AlignmentList masterList,
			AlignmentList alignedList) {

        List<AlignmentRow> masterRows = masterList.getRows();
        List<WeightedRowVsRowScore> acceptablePairsList = new ArrayList<WeightedRowVsRowScore>();

        System.out.println("\tmasterRows.size() = " + masterRows.size());		
		System.out.print("\tCreating acceptable pairs list ");
		int counter = 0;
		for (AlignmentRow master : masterRows) {

			if (counter % 1000 == 0) {
				System.out.print('.');
			}
			counter++;
			
			// Get all rows of the aligned peaklist within parameter limits,
			Set<AlignmentRow> candidateRows = alignedList.getRowsInRange(master, 
					this.massTolerance, this.rtTolerance, this.usePpm);
			for (AlignmentRow candidate : candidateRows) {
				
				// calculate score and add them to the list of acceptable pairs
				WeightedRowVsRowScore score = new WeightedRowVsRowScore(candidate, master, massTolerance, this.rtTolerance);
				acceptablePairsList.add(score);
				
			}
			
		}
		System.out.println();
		
		return acceptablePairsList;
	
	}

	private SocialGraph createSocialGraph(AlignmentList masterList,
			AlignmentList alignedList, List<WeightedRowVsRowScore> acceptablePairsList) {

        SocialGraph socialGraph = new SocialGraph();

        System.out.println("\talignedList.getRowsCount() = " + alignedList.getRowsCount());		
        
        System.out.print("\tCreating social graph from aligned -> master ");
		int counter = 0;
		for (AlignmentRow alignedRow : alignedList.getRows()) {

			// find the top few nearest match in the other list
			List<WeightedRowVsRowScore> selectedSub = findNearestMasterRowMatch(alignedRow, acceptablePairsList, 
					AlignmentMethodParam.PARAM_TOP_K_FRIENDS);

			// find this candidate row's 'friends'
			Set<AlignmentRow> candidateFriends = new HashSet<AlignmentRow>();

			for (WeightedRowVsRowScore nearestMatch : selectedSub) {

				AlignmentRow candidate = nearestMatch.getMasterListCandidate();
				
				if (counter % 1000 == 0) {
					System.out.print('.');
				}
				counter++;
				
				// remember to add this current row too
				candidateFriends.add(candidate);
				
				// loop through all the features in this row
				for (Feature feature : candidate.getFeatures()) { 
					
					// find all friends above threshold and within mass tolerance
					Set<Feature> friends = getFriends(feature);            			
					for (Feature friend : friends) {
						
						// find all rows containing this friendly feature
		    			AlignmentRow friendRow = masterList.getRowContaining(friend);
		    			
		    			// store the rows
		    			candidateFriends.add(friendRow);            				
		    			
					}
					
				}
				
			}									
			
			// create social links in our graph from aligned row to candidate rows + friends
			for (AlignmentRow candidateFriend : candidateFriends) {
				SocialGraphEdge friendEdge = new SocialGraphEdge(candidateFriend, alignedRow);
				socialGraph.addEdge(friendEdge);
			}

		}				
		System.out.println();

        System.out.print("\tCreating social graph from aligned <- master ");
		counter = 0;
		for (AlignmentRow masterRow : masterList.getRows()) {

			// find the top few nearest match in the other list
			List<WeightedRowVsRowScore> selectedSub = findNearestAlignedRowMatch(masterRow, acceptablePairsList, 
					AlignmentMethodParam.PARAM_TOP_K_FRIENDS);

			// find this aligned row's 'friends'
			Set<AlignmentRow> alignedFriends = new HashSet<AlignmentRow>();

			for (WeightedRowVsRowScore nearestMatch : selectedSub) {

				AlignmentRow aligned = nearestMatch.getAligned();
				
				if (counter % 1000 == 0) {
					System.out.print('.');
				}
				counter++;
				
				// remember to add this current row too
				alignedFriends.add(aligned);
				
				// loop through all the features in this row
				for (Feature feature : aligned.getFeatures()) { 
					
					// find all friends above threshold and within mass tolerance
					Set<Feature> friends = getFriends(feature);            			
					for (Feature friend : friends) {
						
						// find all rows containing this friendly feature
		    			AlignmentRow friendRow = masterList.getRowContaining(friend);
		    			
		    			// store the rows
		    			alignedFriends.add(friendRow);            				
		    			
					}
					
				}
				
			}									
			
			// create social links in our graph from aligned row to candidate rows + friends
			for (AlignmentRow alignedFriend : alignedFriends) {
				SocialGraphEdge friendEdge = new SocialGraphEdge(masterRow, alignedFriend);
				socialGraph.addEdge(friendEdge);
			}

		}				
		System.out.println();
		
		return socialGraph;
		
	}
	
	private List<WeightedRowVsRowScore> findNearestMasterRowMatch(
			AlignmentRow alignedRow, List<WeightedRowVsRowScore> acceptablePairsList, int limit) {
	
		List<WeightedRowVsRowScore> selected = new ArrayList<WeightedRowVsRowScore>();
		for (WeightedRowVsRowScore acceptable : acceptablePairsList) {
			if (acceptable.getAligned().equals(alignedRow)) {
				selected.add(acceptable);
			}
		}
		
		Collections.sort(selected, Collections.reverseOrder());
		int min = Math.min(limit, selected.size());
		List<WeightedRowVsRowScore> selectedSub = selected.subList(0, min);
		
		return selectedSub;

	}

	private List<WeightedRowVsRowScore> findNearestAlignedRowMatch(
			AlignmentRow masterRow, List<WeightedRowVsRowScore> acceptablePairsList, int limit) {
	
		List<WeightedRowVsRowScore> selected = new ArrayList<WeightedRowVsRowScore>();
		for (WeightedRowVsRowScore acceptable : acceptablePairsList) {
			if (acceptable.getMasterListCandidate().equals(masterRow)) {
				selected.add(acceptable);
			}
		}
		
		Collections.sort(selected, Collections.reverseOrder());
		int min = Math.min(limit, selected.size());
		List<WeightedRowVsRowScore> selectedSub = selected.subList(0, min);
		
		return selectedSub;

	}
	
	private Set<Feature> getFriends(Feature feature) {

		Set<Feature> friends = new HashSet<Feature>();		
		AlignmentFile file = feature.getData();
//		DoubleMatrix zzProb = feature.getZZProb();
		DenseMatrix zzProb = feature.getZZProb();
		
		if (zzProb == null) {
			return friends;
		}
		
		int i = feature.getPeakID();
//		DoubleMatrix probArray = zzProb.getRow(i);			
//		for (int j = 0; j < probArray.length; j++) {
		
		for (int j = 0; j < zzProb.numColumns(); j++) {

			double prob = zzProb.get(i, j);
			if (prob >= threshold) {
				
				Feature friend = file.getFeatureByIndex(j);				
				boolean inRange = checkInMassRange(feature, friend);				
				if (inRange) {
					friends.add(friend);					
				}
				
			}
		}
		return friends;

	}

	private boolean checkInMassRange(Feature feature, Feature friend) {
		boolean inRange = false;
		double delta = 0;
		if (usePpm) {
			delta = PeriodicTable.PPM(feature.getMass(), massTolerance);			
		} else {
			delta = massTolerance;			
		}
		double massLower = feature.getMass() - delta/2;
		double massUpper = feature.getMass() + delta/2;
		if (friend.getMass() > massLower && friend.getMass() < massUpper) {
			inRange = true;
		}
		return inRange;
	}
		
	private Map<AlignmentRow, AlignmentRow> approxSmiss(
			List<AlignmentRow> men, List<AlignmentRow> women,
			List<WeightedRowVsRowScore> acceptablePairsList, 
			SocialGraph socialGraph) {
		
		Queue<PreferenceItem> freeMen = new LinkedList<PreferenceItem>();

        // Create a free list of men (and use it to store their proposals)
    	System.out.print("\tCreating prefs ");
        int counter = 0;
        for (AlignmentRow man : men) {
            Queue<ManPreference> prefs = getPrefs(man, acceptablePairsList);
            freeMen.add(new PreferenceItem(man, prefs));
            prefs = null;
            counter++;
            if (counter % 1000 == 0) {
            	System.out.print('.');
            }
        }
        System.out.println();

        // Create an initially empty map of engagements between woman -> man
        Map<AlignmentRow, PreferenceItem> engagements = new HashMap<AlignmentRow, PreferenceItem>();

        System.out.println("\tStart algorithm, initial freeMen = " + freeMen.size());
        int prevSize = 0;
        while (!freeMen.isEmpty()) {
        	
        	// print progress
    		int size = freeMen.size();
    		if (size % 1000 == 0 && size != prevSize) {
    			prevSize = size;
    			System.out.println("\t\tRemaining free men = " + size);        		
    		}
    		
    		PreferenceItem m = freeMen.peek();
    		if (m.emptyPrefs()) {
    			m.setDeleted(true);
    		}
    		if (m.isDeleted()) {
    			freeMen.poll();
    			continue;
    		}
        	    		
        	// call modified Gale Shapley algorithm
        	modExgs(freeMen, engagements, socialGraph);
        	
        	System.out.println("\t\tPromoting and deleting men ...");
        	for (PreferenceItem mi : freeMen) {
            	
        		// delete all men that have been promoted but still unmatched
        		if (mi.isPromoted() && !mi.isMatched()) {
        			mi.setDeleted(true);
        		}
        		
            	// promote all men that is unmatched, unpromoted and has a non-empty preference list
        		if (!mi.isMatched() && !mi.isPromoted() && !mi.emptyPrefs()) {
        			mi.setPromoted(true);
        		}
        			
        	}
        	        	
        }
        
        // Convert internal data structure to mapping
        System.out.println("engagements.size() = " + engagements.size());
        Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();
        for (AlignmentRow key : engagements.keySet()) {
        	PreferenceItem rowPref = engagements.get(key);
            matches.put(key, rowPref.getRow());
        }
        return matches;

	}
	
	private Queue<ManPreference> getPrefs(AlignmentRow man, final List<WeightedRowVsRowScore> acceptablePairsList) {
		Queue<ManPreference> prefs = new PriorityQueue<ManPreference>(11, new ManPreferenceComparator());
		for (WeightedRowVsRowScore acceptablePair : acceptablePairsList) {
			if (acceptablePair.getMasterListCandidate().equals(man)) {
				double score = acceptablePair.getScore();
				AlignmentRow woman = acceptablePair.getAligned();
				ManPreference pref = new ManPreference(woman, score);
				prefs.add(pref);				
			}
		}
		return prefs;
	}
	
    private void modExgs(Queue<PreferenceItem> freeMen, Map<AlignmentRow, 
    		PreferenceItem> engagements, SocialGraph socialGraph) {

    	System.out.println("\t\tmod-EXGS called");
		Queue<PreferenceItem> toProcess = new LinkedList<PreferenceItem>(freeMen);
        int prevSize = 0;
		while (!toProcess.isEmpty()) {

        	// print progress
    		int size = toProcess.size();
    		if (size % 1000 == 0 && size != prevSize) {
    			prevSize = size;
    			System.out.println("\t\t\tmod-EXGS = " + size);        		
    		}
			
            // the current man m
            PreferenceItem m = toProcess.poll();
            
            // m's highest ranked woman whom he has not proposed to yet
            ManPreference preferredWoman = m.getPrefs().poll();
                        
            // for unequal m and w size. no more w to propose to ?
            if (preferredWoman != null) {

            	AlignmentRow w = preferredWoman.entry;
                WomanPreferenceComparator womanPreference = new WomanPreferenceComparator(w);
                WomanSocialPreferenceComparator womanSocialPreference = new WomanSocialPreferenceComparator(w, socialGraph);

            	// if w is matched in engagement
                if (engagements.containsKey(w)) {

                	// and she socially prefers m to her current partner m'
                    PreferenceItem mPrime = engagements.get(w);
                    boolean preferNewPartner = womanSocialPreference.prefer(m, mPrime); 

                    // w prefers m to m'
                    if (preferNewPartner) {
                    	// then remove the matching between w and m'
                    	engagements.remove(w);
                    	// m' goes back to the pool of free men
                    	mPrime.setMatched(false);
                        // assert(!toProcess.contains(mPrime));
                        toProcess.add(mPrime);
                    }
                	
                }
                
                // if w is free
                if (!engagements.containsKey(w)) {
                	// (m, w) become engaged
                    engagements.put(w, m);
                    m.setMatched(true);
                } 
                
                // if m and w are in in acquainted pairs (in the social graph)
                if (isAcquainted(socialGraph, m, w)) {

                	// remove all mk in acceptable pairs list (that are currently acceptable to w)
        			// but less preferred (in the classical sense) than m
                    findPrefsToDelete(freeMen, m, w, womanPreference);
                
                }
                            	
            }
            
    	} // end while (!freeMen.isEmpty())
    	                            
    }

	private boolean isAcquainted(
			SocialGraph socialGraph, 
			PreferenceItem m, AlignmentRow w) {

		AlignmentRow mRow = m.row;
		SocialGraphEdge edge = new SocialGraphEdge(mRow, w);
		if (socialGraph.containsEdge(edge)) {
			return true;			
		}
		return false;
		
	}

	private void findPrefsToDelete(Queue<PreferenceItem> freeMen,
			PreferenceItem m, AlignmentRow w, WomanPreferenceComparator womanPreference) {
		Iterator<PreferenceItem> it = freeMen.iterator();
		while (it.hasNext()) {
			PreferenceItem mk = it.next();
			if (womanPreference.prefer(m, mk)) {
				mk.removePref(w);
			}
		}
	}
    		    	
    private class PreferenceItem {
        
    	private final AlignmentRow row;
        private Queue<ManPreference> prefs;
        private Queue<ManPreference> oldPrefs;
        private boolean matched;
        private boolean deleted;
        private boolean promoted;

        public PreferenceItem(AlignmentRow s, Queue<ManPreference> p) {
            this.row = s;
            this.prefs = p;
            this.oldPrefs = new PriorityQueue<ManPreference>(p);
            this.matched = false;
            this.deleted = false;
            this.promoted = false;
        }

		public AlignmentRow getRow() {
			return row;
		}

		public Queue<ManPreference> getPrefs() {
			return prefs;
		}
		
		public void removePref(AlignmentRow woman) {
			Iterator<ManPreference> it = prefs.iterator();
			while (it.hasNext()) {
				ManPreference mp = it.next();
				if (woman.equals(mp.entry)) {
					it.remove();
				}
			}
		}
		
		public boolean emptyPrefs() {
			return prefs.isEmpty();
		}
		
		public boolean isMatched() {
			return matched;
		}

		public void setMatched(boolean matched) {
			this.matched = matched;
		}

		public boolean isDeleted() {
			return deleted;
		}

		public void setDeleted(boolean deleted) {
			this.deleted = deleted;
		}

		public boolean isPromoted() {
			return promoted;
		}

		public void setPromoted(boolean promoted) {
			this.promoted = promoted;
			if (promoted) {
				prefs = oldPrefs;
			}
		}

		
		public String toString() {
			return "ManPreference [row=" + row + ", prefs=" + prefs
					+ ", deleted=" + deleted + ", promoted=" + promoted + "]";
		}
        
    }
    
    private class ManPreference {
    	
    	private final AlignmentRow entry;
    	private final double score;
    	
    	public ManPreference(AlignmentRow preferredRow, double score) {
    		this.entry = preferredRow;
    		this.score = score;
    	}
    	
		
		public String toString() {
			return "PreferenceItem [entry=" + entry + ", score=" + score + "]";
		}
    	
    }
    
    private class ManPreferenceComparator implements Comparator<ManPreference>{

    	
    	public int compare(ManPreference item1, ManPreference item2) {
    		// higher score is now preferred, so invert the sign
    		return - Double.compare(item1.score, item2.score);
    	}
    	
    }    
    
    private class WomanPreferenceComparator implements Comparator<PreferenceItem>{

    	protected AlignmentRow woman;
    	
    	public WomanPreferenceComparator(AlignmentRow woman) {
    		this.woman = woman;
    	}

    	/**
    	 * Compares the preference of woman towards two men
    	 * @param mp1 The first man
    	 * @param mp2 The second man
    	 * @return Returns true if woman prefers the first man, false if she prefers the second man
    	 */
    	public boolean prefer(PreferenceItem mp1, PreferenceItem mp2) {
    		int compareRes = compare(mp1, mp2);
    		if (compareRes > 0) {
    			return true;
    		} else {
    			return false;
    		}
    	}
    	
    	
    	public int compare(PreferenceItem mi, PreferenceItem mk) {
    		AlignmentRow candidate1 = mi.getRow();
    		AlignmentRow candidate2 = mk.getRow();
    		WeightedRowVsRowScore rowScore1 = new WeightedRowVsRowScore(woman, candidate1, massTolerance, rtTolerance);
    		WeightedRowVsRowScore rowScore2 = new WeightedRowVsRowScore(woman, candidate2, massTolerance, rtTolerance);
    		double score1 = rowScore1.getScore();
    		double score2 = rowScore2.getScore();
    		return Double.compare(score1, score2);
    	}
    	    	
    }      
    
    private class WomanSocialPreferenceComparator extends WomanPreferenceComparator implements Comparator<PreferenceItem>{

    	private SocialGraph socialGraph;
    	
    	public WomanSocialPreferenceComparator(AlignmentRow woman, SocialGraph socialGraph) {
    		super(woman);
    		this.socialGraph = socialGraph;
    	}
    	
    	
    	public int compare(PreferenceItem mi, PreferenceItem mk) {

    		boolean priorityRelationship = checkPriorityRelationship(mi, mk);
    		if (priorityRelationship) {
    			return 1;
    		}
    		
    		priorityRelationship = checkPriorityRelationship(mk, mi);
    		if (priorityRelationship) {
    			return -1;
    		}    		
    		
    		return super.compare(mi, mk);    		

    	}

		private boolean checkPriorityRelationship(PreferenceItem mi, PreferenceItem mk) {
			boolean priorityRelationship = false;
    		boolean miAcquainted = isAcquainted(socialGraph, mi, woman);
    		boolean mkAcquainted = isAcquainted(socialGraph, mk, woman);    		
    		if (!miAcquainted & !mkAcquainted & mi.isPromoted() && !mk.isPromoted()) {
    			priorityRelationship = true;
    		}
    		if (miAcquainted && !mkAcquainted && !mk.isPromoted()) {
    			priorityRelationship = true;
    		}
			return priorityRelationship;
		}
    	    	    	
    }
    
		
}
