package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.DistanceCalculator;
import com.joewandy.alignmentResearch.objectModel.FeatureGroup;
import com.joewandy.alignmentResearch.objectModel.MahalanobisDistanceCalculator;
import com.joewandy.alignmentResearch.objectModel.MatchingScorer;
import com.joewandy.alignmentResearch.objectModel.SocialGraph;
import com.joewandy.alignmentResearch.objectModel.SocialGraphEdge;
import com.joewandy.alignmentResearch.objectModel.WeightedRowVsRowScore;

public class MyGroupMatchingAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentList> featureList;
	private double threshold;
	
	public MyGroupMatchingAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

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
			
			Map<AlignmentRow, AlignmentRow> allMapping = new HashMap<AlignmentRow, AlignmentRow>();
			Map<FeatureGroup, FeatureGroup> seenBefore = new HashMap<FeatureGroup, FeatureGroup>();
			List<FeatureGroup> masterGroups = masterList.getGroups();
			List<FeatureGroup> alignedGroups = alignedList.getGroups();
			int unmatchedMasterGroupsCount = masterList.getUnmatchedGroupsCount();
			int unmatchedAlignedGroupsCount = alignedList.getUnmatchedGroupsCount();

			if (unmatchedMasterGroupsCount == 0 || unmatchedAlignedGroupsCount == 0) {
				break;
			}
			
			// match group vs group				
			Map<FeatureGroup, FeatureGroup> result = matchGroups(masterGroups, alignedGroups, seenBefore);
			seenBefore.putAll(result);

			// create acceptable pairs within matched groups
//		        List<WeightedRowVsRowScore> acceptablePairsList = createAcceptablePairsList(masterList, alignedList, result);
//		        SocialGraph socialGraph = new SocialGraph();
            
            // call 3/2-approximately maximum social stable matching	            
//	    		List<AlignmentRow> men = masterList.getUnalignedRows();
//	    		List<AlignmentRow> women = alignedList.getUnalignedRows();	    		
//	    		Map<AlignmentRow, AlignmentRow> alignmentMapping = approxSmiss(men, women, 
//	    				acceptablePairsList, socialGraph);
	        
			Map<AlignmentRow, AlignmentRow> alignmentMapping = new HashMap<AlignmentRow, AlignmentRow>();
			List<AlignmentRow> masterRows = masterList.getUnalignedRows();
			List<AlignmentRow> alignedRows = alignedList.getUnalignedRows();
			for (AlignmentRow masterRow : masterRows) {
				FeatureGroup masterGroup = masterRow.getFirstFeature().getFirstGroup();
				FeatureGroup preferred = result.get(masterGroup);
				AlignmentRow bestMatch = findBestMatch(masterRow, alignedRows, preferred);
				if (bestMatch != null) {
					alignmentMapping.put(masterRow, bestMatch);						
				}
			}
			
			System.out.println("alignmentMapping.size() = " + alignmentMapping.size());
    		allMapping.putAll(alignmentMapping);
    		setFlag(allMapping);
			
            /*
             * 3. Here, we actually construct the row alignments using the mapping created above.
             * if we cannot find the mapping, then add this row 
             */
            
            // Align all rows using mapping
            for (AlignmentRow alignedRow : alignedRows) {
            	
            	AlignmentRow masterListRow = allMapping.get(alignedRow);
            	
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

	private AlignmentRow findBestMatch(AlignmentRow masterRow, List<AlignmentRow> alignedRows,
			FeatureGroup preferred) {

		// consider only unaligned rows inside preferred and within mass tolerance
		List<AlignmentRow> reduced = new ArrayList<AlignmentRow>(alignedRows);
		Iterator<AlignmentRow> it = reduced.iterator();
		while (it.hasNext()) {
			AlignmentRow candidate = it.next();
			Set<Integer> candidateIds = candidate.getGroupIds();
			if (candidate.isAligned()) {
				// has been aligned before ?
				it.remove();
			} else if (!candidateIds.contains(preferred.getGroupId())) {
				// not in matched group ?
				it.remove();
			} else if (!masterRow.rowInRange(candidate, massTolerance, -1, usePpm)) {
				// not within mass tolerance ?
				it.remove();
			}
		}
		
		// find the one with minimum mass distance
		MatchingScorer scorer = new MatchingScorer(massTolerance, Double.MAX_VALUE);
		double minDist = Double.MAX_VALUE;
		AlignmentRow closest = null;
		for (AlignmentRow candidate : reduced) {
			double dist = scorer.computeDist(masterRow, candidate);
			if (dist < minDist) {
				minDist = dist;
				closest = candidate;
			}
		}
		return closest;
		
	}

	private void setFlag(Map<AlignmentRow, AlignmentRow> allMapping) {

		// mark all rows & groups inside allMapping as aligned & matched
		for (Entry<AlignmentRow, AlignmentRow> entry : allMapping.entrySet()) {

			AlignmentRow masterRow = entry.getKey();
			masterRow.setAligned(true);
			
			AlignmentRow alignedRow = entry.getValue();
			alignedRow.setAligned(true);		
			
			FeatureGroup masterGroup = masterRow.getFirstFeature().getFirstGroup();
			masterGroup.setMatched(true);

			FeatureGroup alignedGroup = alignedRow.getFirstFeature().getFirstGroup();
			alignedGroup.setMatched(true);
			
		}
		
	}

	private Map<FeatureGroup, FeatureGroup> matchGroups(
			List<FeatureGroup> masterGroups, List<FeatureGroup> alignedGroups, Map<FeatureGroup, FeatureGroup> seenBefore) {
		
		DistanceCalculator calc = new MahalanobisDistanceCalculator(massTolerance, Double.MAX_VALUE);
		int unmatchedMaster = masterGroups.size();
		int unmatchedAligned = alignedGroups.size();

		double[][] distMatrix = new double[unmatchedMaster][unmatchedAligned];
		for (int m = 0; m < masterGroups.size(); m++) {
			for (int n = 0; n < alignedGroups.size(); n++) {
				
				FeatureGroup masterGroup = masterGroups.get(m);
				FeatureGroup alignedGroup = alignedGroups.get(n);
			
				if (seenBefore.get(masterGroup) != null && seenBefore.get(masterGroup).equals(alignedGroup)) {
					distMatrix[m][n] = Double.MAX_VALUE;
				} else {
					double mass1 = masterGroup.getAverageMz();
					double mass2 = alignedGroup.getAverageMz();
					double rt1 = masterGroup.getAverageRt();
					double rt2 = alignedGroup.getAverageRt();
					double mzDist = calc.compute(mass1, mass2, rt1, rt2);
					distMatrix[m][n] = mzDist;
//					double sizeDiff = Math.abs(masterGroup.getFeatureCount() - alignedGroup.getFeatureCount());
//					distMatrix[m][n] = sizeDiff * mzDist;	
				}
				
			}
		}
		
		// find the minimum weighted matching
		System.out.print("\tRunning matching ");
		HungarianAlgorithm algo = new HungarianAlgorithm(distMatrix);
		int[] res = algo.execute();
		System.out.println();
		
		// store the result
		Map<FeatureGroup, FeatureGroup> result = new HashMap<FeatureGroup, FeatureGroup>();
		for (int m=0; m<masterGroups.size(); m++) {
			int matchIndex = res[m];
			// if there's a match
			if (matchIndex != -1) {
				FeatureGroup masterGroup = masterGroups.get(m);
				FeatureGroup alignedGroup = alignedGroups.get(matchIndex);
				assert(masterGroup != null);
				assert(alignedGroup != null);
				result.put(masterGroup, alignedGroup);
			}
		}

		return result;
		
	}

	private List<WeightedRowVsRowScore> createAcceptablePairsList(AlignmentList masterList,
			AlignmentList alignedList, Map<FeatureGroup, FeatureGroup> result) {

        List<AlignmentRow> masterRows = masterList.getUnalignedRows();
        List<WeightedRowVsRowScore> acceptablePairsList = new ArrayList<WeightedRowVsRowScore>();

        System.out.println("\tmasterRows.size() = " + masterRows.size());		
		System.out.print("\tCreating acceptable pairs list ");
		int counter = 0;
		for (AlignmentRow masterRow : masterRows) {

			if (counter % 1000 == 0) {
				System.out.print('.');
			}
			counter++;
						
			// Get all rows of the aligned peaklist within parameter limits AND matched groups
			FeatureGroup preferredGroup = null;
			if (result != null) {
				FeatureGroup masterGroup = masterRow.getFirstFeature().getFirstGroup();
				preferredGroup = result.get(masterGroup);
				assert(masterGroup != null);
				if (preferredGroup == null) {
					continue;
				}				
			}
			
			Set<AlignmentRow> candidateRows = alignedList.getUnalignedRowsInRange(masterRow, 
					this.massTolerance, this.usePpm);
			for (AlignmentRow candidate : candidateRows) {

				if (candidate.isAligned()) {
					continue;
				}
				
				FeatureGroup alignedGroup = candidate.getFirstFeature().getFirstGroup();
				if (result != null && preferredGroup.getGroupId() != alignedGroup.getGroupId()) {
					continue;
				}
				
				// calculate score and add them to the list of acceptable pairs
				WeightedRowVsRowScore acceptable = new WeightedRowVsRowScore(candidate, masterRow, massTolerance, Double.MAX_VALUE);
				acceptablePairsList.add(acceptable);
				
			}
			
		}
		System.out.println();
		System.out.println("acceptablePairsList.size() = " + acceptablePairsList.size());
		
		return acceptablePairsList;
	
	}
			
	private Map<AlignmentRow, AlignmentRow> approxSmiss(
			List<AlignmentRow> men, List<AlignmentRow> women,
			List<WeightedRowVsRowScore> acceptablePairsList, 
			SocialGraph socialGraph) {
		
		// do stable matching
		System.out.println("Running maximum socially stable matching");
		System.out.println("\tmen size = " + men.size());
		System.out.println("\twomen size = " + women.size());
		System.out.println("\tacceptable pairs = " + acceptablePairsList.size());
		System.out.println("\tsocial graph = " + socialGraph.getEdgeCount() + " edges");
		
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
				double dist = acceptablePair.getDist();
				AlignmentRow woman = acceptablePair.getAligned();
				ManPreference pref = new ManPreference(woman, dist);
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

		@Override
		public String toString() {
			return "ManPreference [row=" + row + ", prefs=" + prefs
					+ ", deleted=" + deleted + ", promoted=" + promoted + "]";
		}
        
    }
    
    private class ManPreference {
    	
    	private final AlignmentRow entry;
    	private final double dist;
    	
    	public ManPreference(AlignmentRow preferredRow, double dist) {
    		this.entry = preferredRow;
    		this.dist = dist;
    	}
    	
		@Override
		public String toString() {
			return "PreferenceItem [entry=" + entry + ", dist=" + dist + "]";
		}
    	
    }
    
    private class ManPreferenceComparator implements Comparator<ManPreference>{

    	@Override
    	public int compare(ManPreference item1, ManPreference item2) {
    	
    		// higher score is now preferred, so invert the sign
//    		return - Double.compare(item1.score, item2.score);

    		// prefer lower distance
    		return Double.compare(item1.dist, item2.dist);
    	
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
    	
    	@Override
    	public int compare(PreferenceItem mi, PreferenceItem mk) {
    		AlignmentRow candidate1 = mi.getRow();
    		AlignmentRow candidate2 = mk.getRow();
    		WeightedRowVsRowScore rowScore1 = new WeightedRowVsRowScore(woman, candidate1, massTolerance, Double.MAX_VALUE);
    		WeightedRowVsRowScore rowScore2 = new WeightedRowVsRowScore(woman, candidate2, massTolerance, Double.MAX_VALUE);
    		double dist1 = rowScore1.getDist();
    		double dist2 = rowScore2.getDist();
    		return Double.compare(dist1, dist2);
    	}
    	    	
    }      
    
    private class WomanSocialPreferenceComparator extends WomanPreferenceComparator implements Comparator<PreferenceItem>{

    	private SocialGraph socialGraph;
    	
    	public WomanSocialPreferenceComparator(AlignmentRow woman, SocialGraph socialGraph) {
    		super(woman);
    		this.socialGraph = socialGraph;
    	}
    	
    	@Override
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
