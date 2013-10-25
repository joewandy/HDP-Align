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

import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jblas.DoubleMatrix;

import com.jmatio.io.MatFileReader;
import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.MatchingScorer;

public class StableMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private String listId;
	private double massTol;
	private double rtTol;	
	private MatchingScorer scorer;
	private DoubleMatrix distArr;
	private DoubleMatrix scoreArr;
	private DoubleMatrix binaryScoreArr;
	private double alpha;
	
	public StableMatching(String listId, AlignmentList masterList, AlignmentList childList,
			ExtendedLibrary library,
			double massTol, double rtTol, double alpha) {			
		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.massTol = massTol;
		this.rtTol = rtTol;		
		this.scorer = new MatchingScorer(this.massTol, this.rtTol);
		this.scoreArr = null;
		this.distArr = null;
		this.binaryScoreArr = null;
		this.alpha = alpha;
	}

	public AlignmentList getMatchedList() {
			
		// nothing in masterlist to match against, just return the childlist directly
		if (masterList.getRowsCount() == 0) {
			return childList;
		}
		
		// do stable matching here ...
		System.out.println("Running stable matching on " + listId);
		Map<AlignmentRow, AlignmentRow> stableMatch = match(masterList, childList);
		
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
		for (AlignmentRow row : getMen(masterList, childList)) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				menUnmatchedCount++;
			} else {
				menMatchedCount++;
			}
		}
		int womenMatchedCount = 0;
		int womenUnmatchedCount = 0;
		for (AlignmentRow row : getWomen(masterList, childList)) {
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
	
	private List<AlignmentRow> getMen(AlignmentList masterList, AlignmentList childList) {
		List<AlignmentRow> men = masterList.getRows();
		return men;
	}

	private List<AlignmentRow> getWomen(AlignmentList masterList, AlignmentList childList) {
		List<AlignmentRow> women = childList.getRows();			
		return women;
	}

	private double[][] getMenClustering(AlignmentList masterList, AlignmentList childList) {
		if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
			if (FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
				return masterList.getData().getZZProb();
			} else {				
				return masterList.getData().getZ();						
			}
		} else {
			AlignmentFile data = masterList.getData();
			if (data != null) {
				int n = data.getFeaturesCount();
				return new double[n][n];				
			} else {
				return null;
			}
		}
	}

	private double[][] getWomenClustering(AlignmentList masterList, AlignmentList childList) {
		if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
			if (FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
				return childList.getData().getZZProb();
			} else {				
				return childList.getData().getZ();						
			}
		} else {
			AlignmentFile data = childList.getData();
			if (data != null) {
				int n = data.getFeaturesCount();
				return new double[n][n];				
			} else {
				return null;
			}
		}
	}
	
    private Map<AlignmentRow, AlignmentRow> match(AlignmentList masterList, AlignmentList childList) {

		System.out.println("\tmasterList " + masterList.getId());
		System.out.println("\tchildList " + childList.getId());		
    	
		// compute score here
		List<AlignmentRow> men = getMen(masterList, childList);
		List<AlignmentRow> women = getWomen(masterList, childList);
		double[][] clusteringMen = getMenClustering(masterList, childList);
		double[][] clusteringWomen = getWomenClustering(masterList, childList);
        computeScores(men, women, clusteringMen, clusteringWomen);
//        saveToMatlab(masterList.getData().getParentPath());

        if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
			combineScoreJBlas(clusteringMen, clusteringWomen);				
//        	loadScore();
		}
        
        Map<AlignmentRow, AlignmentRow> matches = glMatching(men, women);        
//        Map<AlignmentRow, AlignmentRow> matches = hungarianMatching(men, women);
//        Map<AlignmentRow, AlignmentRow> matches = approxMaxMatching(men, women);
    	
        return matches;
        
    }
    
	
	private void saveToMatlab(String path) {
		System.out.println("Saving scores to matlab");
		MLDouble scoreMat = new MLDouble("W", scoreArr.toArray2());
		MLDouble inRangeMat = new MLDouble("Q", binaryScoreArr.toArray2());
		final Collection<MLArray> output = new ArrayList<MLArray>();
		output.add(scoreMat);
		output.add(inRangeMat);
		final MatFileWriter writer = new MatFileWriter();
		try {
			String fullPath = path + "/mat/WQ.mat";
			writer.write(fullPath, output);
			System.out.println("Written to " + fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Map<AlignmentRow, AlignmentRow> hungarianMatching(
			List<AlignmentRow> men, List<AlignmentRow> women) {
		
        double maxScore = scoreArr.max();
        
    	// normalise 
    	for (int i = 0; i < men.size(); i++) {
    		for (int j = 0; j < women.size(); j++) {
    			scoreArr.put(i, j, maxScore - scoreArr.get(i, j));
    		}
    	}
		// running matching
		System.out.print("\tRunning matching ");
		HungarianAlgorithm algo = new HungarianAlgorithm(scoreArr.toArray2());
		int[] res = algo.execute();
		System.out.println();
		
		// store the result
		Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();
		for (int i=0; i<men.size(); i++) {
			int matchIndex = res[i];
			// if there's a match
			if (matchIndex != -1) {
				AlignmentRow row1 = men.get(i);
				AlignmentRow row2 = women.get(matchIndex);
				matches.put(row1, row2);
			}
		}
		return matches;

	}

	private Map<AlignmentRow, AlignmentRow> approxMaxMatching(List<AlignmentRow> men, 
			List<AlignmentRow> women) {
		
		// running matching
		System.out.print("\tRunning matching ");
		GreedyApprox algo = new GreedyApprox(scoreArr.toArray2());
		int[] res = algo.execute();
		System.out.println();
		
		// store the result
		Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();
		for (int i=0; i<men.size(); i++) {
			int matchIndex = res[i];
			// if there's a match
			if (matchIndex != -1) {
				AlignmentRow row1 = men.get(i);
				AlignmentRow row2 = women.get(matchIndex);
				matches.put(row1, row2);
			}
		}
		return matches;
		
	}
	
	private Map<AlignmentRow, AlignmentRow> glMatching(List<AlignmentRow> men, 
			List<AlignmentRow> women) {

        // Create a free list of men (and use it to store their proposals)
    	System.out.print("\tCreating prefs ");
        Queue<RowPreference> freeMen = new LinkedList<RowPreference>();
    	for (int i = 0; i < men.size(); i++) {

        	AlignmentRow man = men.get(i);
        	Queue<PreferenceItem> sorted = getSortedPrefs(man, women, scoreArr, i);
        	freeMen.add(new RowPreference(man, sorted));
            
        	if (i % 1000 == 0) {
            	System.out.print('.');
            }
        	
        }
        System.out.println();
		
		// call GS algorithm
        Map<AlignmentRow, RowPreference> engagements = galeShapley(freeMen, women);

        // Convert internal data structure to mapping
        Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();
        for (Map.Entry<AlignmentRow, RowPreference> entry : engagements.entrySet()) {
            matches.put(entry.getValue().row, entry.getKey());
        }
        
        return matches;

	}

	private Map<AlignmentRow, RowPreference> galeShapley(
			Queue<RowPreference> freemen, List<AlignmentRow> women) {
		
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
            AlignmentRow mRow = m.row;

            // m's highest ranked woman whom he has not proposed to yet
            PreferenceItem preferredWoman = m.prefs.poll();
            
            // for unequal m and w size. no more w to propose to ?
            if (preferredWoman != null) {

            	int rowIdx = preferredWoman.rowIdx;
            	AlignmentRow w = women.get(rowIdx);
            	
            	// FIXME: hard cut-off here improves precision & recall quite a lot
            	// can we do better than this ?
//            	double score = preferredWoman.score;
//            	if (score < 0.5) {
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
		return engagements;
	}

	private Queue<PreferenceItem> getSortedPrefs(AlignmentRow man, List<AlignmentRow> women,
			DoubleMatrix scoreMatrix, int i) {
		
		DoubleMatrix womenScore = scoreMatrix.getRow(i);
		List<PreferenceItem> prefs = new ArrayList<PreferenceItem>();
		for (int j = 0; j < womenScore.length; j++) {
			AlignmentRow woman = women.get(j);
			if (man.rowInRange(woman, massTol, rtTol, FeatureXMLAlignment.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
				double score = womenScore.get(j);
				PreferenceItem pref = new PreferenceItem(j, score);
				prefs.add(pref);				
			}
		}
		Queue<PreferenceItem> sorted = new PriorityQueue<PreferenceItem>(11, new ManPreferenceComparator());
		sorted.addAll(prefs);
		return sorted;

	}

	private void computeScores(List<AlignmentRow> men,
			List<AlignmentRow> women, double[][] clusteringMen, double[][] clusteringWomen) {

		int m = men.size();
		int n = women.size();
		
    	System.out.print("\tComputing scores ");
    	double maxDist = 0;
		distArr = new DoubleMatrix(m, n);
		binaryScoreArr = new DoubleMatrix(m, n);
		for (int i = 0; i < m; i++) {
			
			for (int j = 0; j < n; j++) {
				AlignmentRow man = men.get(i);
				AlignmentRow woman = women.get(j);
				if (man.rowInRange(woman, massTol, -1, FeatureXMLAlignment.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
					double dist = scorer.computeDist(man, woman);
					if (dist > maxDist) {
						maxDist = dist;
					}
					distArr.put(i, j, dist);				
					binaryScoreArr.put(i, j, 1);
				}
			}
			
            if (i % 1000 == 0) {
            	System.out.print('.');
            }
			
		}
		System.out.println();

    	// score = 1-(dist/maxDist)
		distArr.divi(maxDist);
		scoreArr = DoubleMatrix.ones(men.size(), women.size());
		scoreArr.subi(distArr);
		
		// get rid of those score values not within mass tolerance
		scoreArr.muli(binaryScoreArr);

		// normalise score to 0..1
		double maxScore = scoreArr.max();
		scoreArr.divi(maxScore);
			
	}

	private void combineScoreJBlas(double[][] clusteringMen,
			double[][] clusteringWomen) {

    	System.out.println("\tCombining scores ");
		long startTime = System.nanoTime();

		DoubleMatrix W = scoreArr;
		double maxScore = W.max();
		W.divi(maxScore);
		System.out.println("\t\tW = " + W.rows + "x" + W.columns);
		
		DoubleMatrix A = new DoubleMatrix(clusteringMen);
		if (!FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
			A = A.mmul(A.transpose());			
		}
		A.subi(DoubleMatrix.diag(A.diag())); // A = A - diag(diag(A));
		System.out.println("\t\tA = " + A.rows + "x" + A.columns);
		 
		DoubleMatrix B = new DoubleMatrix(clusteringWomen);
		if (!FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
			B = B.mmul(B.transpose());
		}
		B.subi(DoubleMatrix.diag(B.diag())); // B = B - diag(diag(B));
		System.out.println("\t\tB = " + B.rows + "x" + B.columns);

		// D = (A*W)*B;
		System.out.println("\t\tComputing D=(AW)B");
		DoubleMatrix D = A.mmul(W);

		D.mmuli(B);

		// D = Q .* D;
		System.out.println("\t\tComputing D.*Q");
		DoubleMatrix Q = binaryScoreArr;
		D.muli(Q);
		
		// D = D ./ max(max(D));
		maxScore = D.max();
		D.divi(maxScore);

		// Wp = (alpha .* W) + ((1-alpha) .* D);
		System.out.println("\t\tComputing W'=(alpha.*W)+((1-alpha).*D)");
		W.muli(alpha);
		D.muli(1-alpha);
		DoubleMatrix Wp = W.add(D);
		
		long elapsedTime = (System.nanoTime()-startTime)/1000000000;
		System.out.println("\tElapsed time = " + elapsedTime + "s");
		
	}
	
	private void loadScore() {
		System.out.println("\tLoading precomputed matlab scores");
		// load from matlab
		MatFileReader mfr = null;
		try {
			mfr = new MatFileReader("/home/joewandy/Dropbox/Project/real_datasets/P2/source_features_000_test/mat/Wp.mat");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		scoreArr = new DoubleMatrix(((MLDouble)mfr.getMLArray("Wp")).getArray());
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

    	private AlignmentRow woman;
    	
    	public WomanPreferenceComparator(AlignmentRow woman) {
    		this.woman = woman;
    	}

    	@Override
    	public int compare(AlignmentRow candidate1, AlignmentRow candidate2) {
    		int myIndex = woman.getRowId();
    		int can1Index = candidate1.getRowId();
    		int can2Index = candidate2.getRowId();
    		double score1 = scoreArr.get(can1Index, myIndex);
    		double score2 = scoreArr.get(can2Index, myIndex);
    		return Double.compare(score1, score2);
    	}
    	
    }        

}
