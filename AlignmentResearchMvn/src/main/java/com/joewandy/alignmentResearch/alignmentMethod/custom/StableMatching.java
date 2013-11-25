package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import org.jblas.DoubleMatrix;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.MatchingScorer;

public class StableMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private String listId;
	private double massTol;
	private double rtTol;	
	private MatchingScorer scorer;
	
	private boolean useGroup;
	private double alpha;
	
	public StableMatching(String listId, AlignmentList masterList, AlignmentList childList,
			ExtendedLibrary library,
			double massTol, double rtTol, boolean useGroup, double alpha) {			
		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.massTol = massTol;
		this.rtTol = rtTol;		
		this.scorer = new MatchingScorer(this.massTol, this.rtTol);
		this.useGroup = useGroup;
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

//	private DoubleMatrix getClustering(AlignmentList dataList) {
	private Matrix getClustering(AlignmentList dataList) {
		if (useGroup) {
			AlignmentFile data = dataList.getData();
			if (data != null) {
				// for list produced from initial file
				return getClusteringProbs(data);				
			} else {
				// for list produced from merging files
				List<AlignmentRow> rows = dataList.getRows();
				int n = rows.size();
				DenseMatrix mat = new DenseMatrix(n, n);
				for (int i = 0; i < n; i++) {
					for (int j = 0; j < n; j++) {
						AlignmentRow row1 = rows.get(i);
						AlignmentRow row2 = rows.get(j);
						double score = scoreRow(row1, row2);
//						mat.put(i, j, score);
						mat.set(i, j, score);
					}
				}
				return mat;
			}
		} else {
			// for the case when we don't use the weight
			List<AlignmentRow> rows = dataList.getRows();
			int n = rows.size();
			return new DenseMatrix(n, n);				
		}
	}
	
//	private DoubleMatrix getClusteringProbs(AlignmentFile data) {
//		return data.getZZProb();
//	}

	private Matrix getClusteringProbs(AlignmentFile data) {
		return data.getZZProb();
	}
	
	private double scoreRow(AlignmentRow row1, AlignmentRow row2) {
		
		double total = 0;
		int counter = 0;
		for (Feature f1 : row1.getFeatures()) {
			for (Feature f2 : row2.getFeatures()) {
				if (f1.getData().equals(f2.getData())) {
					int idx1 = f1.getPeakID();
					int idx2 = f2.getPeakID();
					if (idx1 == idx2) {
						total += 1;
					} else {
						Matrix probs = f1.getData().getZZProb();
						double prob = probs.get(idx1, idx2);
						total += prob;					
					}
					counter++;
				}
			}
		}
		double avg = total / counter;		
		return avg;
		
	}
	
    private Map<AlignmentRow, AlignmentRow> match(AlignmentList masterList, AlignmentList childList) {

		System.out.println("\tmasterList " + masterList.getId());
		System.out.println("\tchildList " + childList.getId());		
		List<AlignmentRow> men = getMen(masterList, childList);
		List<AlignmentRow> women = getWomen(masterList, childList);
    	
        Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();

        Matrix scoreArr = computeScores(men, women);
        
//      saveToMatlab(masterList.getData().getParentPath());

        if (useGroup) {        	
        	Matrix clusteringMen = getClustering(masterList);
    		Matrix clusteringWomen = getClustering(childList);        	
    		scoreArr = combineScoreMtj(scoreArr, clusteringMen, clusteringWomen);
//        	scoreArr = loadScore();
		}
    	
    	matches = glMatching(scoreArr, men, women);        	
        
        return matches;
        
    }
    
	
//	private void saveToMatlab(String path) {
//		System.out.println("Saving scores to matlab");
//		MLDouble scoreMat = new MLDouble("W", scoreArr.toArray2());
//		MLDouble inRangeMat = new MLDouble("Q", binaryScoreArr.toArray2());
//		final Collection<MLArray> output = new ArrayList<MLArray>();
//		output.add(scoreMat);
//		output.add(inRangeMat);
//		final MatFileWriter writer = new MatFileWriter();
//		try {
//			String fullPath = path + "/mat/WQ.mat";
//			writer.write(fullPath, output);
//			System.out.println("Written to " + fullPath);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private Map<AlignmentRow, AlignmentRow> glMatching(Matrix scoreArr, 
			List<AlignmentRow> men, 
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
        Map<AlignmentRow, RowPreference> engagements = galeShapley(scoreArr, freeMen, women);

        // Convert internal data structure to mapping
        Map<AlignmentRow, AlignmentRow> matches = new HashMap<AlignmentRow, AlignmentRow>();
        for (Map.Entry<AlignmentRow, RowPreference> entry : engagements.entrySet()) {
            matches.put(entry.getValue().row, entry.getKey());
        }
        
        return matches;

	}

	private Map<AlignmentRow, RowPreference> galeShapley(
			Matrix scoreArr, 
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

            	int pos = preferredWoman.pos;
            	AlignmentRow w = women.get(pos);
            	
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
	                    WomanPreferenceComparator womanPreference = new WomanPreferenceComparator(w, scoreArr);
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
			Matrix scoreMatrix, int i) {
		
		List<PreferenceItem> prefs = new ArrayList<PreferenceItem>();
		for (int j = 0; j < scoreMatrix.numColumns(); j++) {
			AlignmentRow woman = women.get(j);
			if (man.rowInRange(woman, massTol, rtTol, MultiAlign.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
				double score = scoreMatrix.get(i, j);
				PreferenceItem pref = new PreferenceItem(j, score);
				prefs.add(pref);				
			}
		}
		Queue<PreferenceItem> sorted = new PriorityQueue<PreferenceItem>(11, new ManPreferenceComparator());
		sorted.addAll(prefs);
		return sorted;

	}

	private Matrix computeScores(List<AlignmentRow> men, List<AlignmentRow> women) {

		int m = men.size();
		int n = women.size();
		Matrix scoreArr = new DenseMatrix(m, n);
		
    	System.out.print("\tComputing distances ");
    	double maxDist = 0;
		Matrix distArr = new DenseMatrix(m, n);
		for (int i = 0; i < m; i++) {
			
			for (int j = 0; j < n; j++) {
				AlignmentRow man = men.get(i);
				AlignmentRow woman = women.get(j);
				if (man.rowInRange(woman, massTol, -1, MultiAlign.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
					double dist = scorer.computeDist(man, woman);
					if (dist > maxDist) {
						maxDist = dist;
					}
					distArr.set(i, j, dist);				
				}
			}
			
            if (i % 1000 == 0) {
            	System.out.print('.');
            }
			
		}
		System.out.println();

    	System.out.print("\tComputing scores ");
		scoreArr = new DenseMatrix(m, n);
		for (int i = 0; i < m; i++) {		
			
			for (int j = 0; j < n; j++) {
				double dist = distArr.get(i, j);
				if (dist > 0) {
					double score = 1-(dist/maxDist);
					scoreArr.set(i, j, score);
				}
			}

            if (i % 1000 == 0) {
            	System.out.print('.');
            }
		
		}
		System.out.println();
		distArr = null;
		
		// normalise score to 0..1
		double maxScore = getMax(scoreArr);
		scoreArr.scale(1/maxScore);
		
		return scoreArr;
			
	}

	private Matrix combineScoreMtj(Matrix scoreArr, Matrix clusteringMen,
			Matrix clusteringWomen) {

    	System.out.println("\tCombining scores ");
		long startTime = System.nanoTime();

		Matrix W = scoreArr;
		double maxScore = getMax(W);
		W.scale(1/maxScore);
		System.out.println("\t\tW = " + W.numRows() + "x" + W.numColumns());

		Matrix A = clusteringMen;
		for (int i = 0; i < A.numRows(); i++) {
			A.set(i, i, 0);
		}
		System.out.println("\t\tA = " + A.numRows() + "x" + A.numColumns());
		 
		Matrix B = clusteringWomen;
		for (int i = 0; i < B.numRows(); i++) {
			B.set(i, i, 0);
		}
		System.out.println("\t\tB = " + B.numRows() + "x" + B.numColumns());

		// D = (A*W)*B;
		System.out.println("\t\tComputing D=(AW)");
		Matrix AW = new DenseMatrix(A.numRows(), W.numColumns());
		A.mult(W, AW);
		System.out.println("\t\tComputing D=(AW)B");
		Matrix D = new DenseMatrix(AW.numRows(), B.numColumns());
		AW.mult(B, D);

		// D = Q .* D;
		System.out.println("\t\tComputing D.*Q");
		for (MatrixEntry e : W) {
			if (e.get() > 0) {
				// leave as it is
			} else {
				int row = e.row();
				int col = e.column();
				D.set(row, col, 0);
			}
		}
		
		// D = D ./ max(max(D));
		maxScore = getMax(D);
		D.scale(1/maxScore);

		// Wp = (alpha .* W) + ((1-alpha) .* D);
		System.out.println("\t\tComputing W'=(alpha.*W)+((1-alpha).*D)");
		W.scale(alpha);
		D.scale(1-alpha);
		scoreArr = W.add(D);
		
		long elapsedTime = (System.nanoTime()-startTime)/1000000000;
		System.out.println("\tElapsed time = " + elapsedTime + "s");
		
		return scoreArr;
		
	}	
	
	private double getMax(Matrix matrix) {
		double maxScore = 0;
		for (MatrixEntry e : matrix) {
			double val = e.get();
			if (val > maxScore) {
				maxScore = val;
			}
		}
		return maxScore;
	}
	
	private DoubleMatrix loadScore() {
		System.out.println("\tLoading precomputed matlab scores");
		// load from matlab
		MatFileReader mfr = null;
		try {
			mfr = new MatFileReader("/home/joewandy/Dropbox/Project/real_datasets/P2/source_features_000_test/mat/Wp.mat");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		DoubleMatrix scoreArr = new DoubleMatrix(((MLDouble)mfr.getMLArray("Wp")).getArray());
		return scoreArr;
	}
	
    private class RowPreference {
        
    	private final AlignmentRow row;
        private final Queue<PreferenceItem> prefs;

        public RowPreference(AlignmentRow s, Queue<PreferenceItem> p) {
            this.row = s;
            this.prefs = p;
        }

		
		public String toString() {
			return "RowPreference [row=" + row + ", prefs=" + prefs + "]";
		}
        
    }
    
    private class PreferenceItem {

    	private int pos;
    	private final double score;
    	
    	public PreferenceItem(int idx, double score) {
    		this.pos = idx;
    		this.score = score;
    	}

		
		public String toString() {
			return "PreferenceItem [entry=" + pos + ", score=" + score + "]";
		}
    	
    }
    
    private class ManPreferenceComparator implements Comparator<PreferenceItem>{

    	
    	public int compare(PreferenceItem item1, PreferenceItem item2) {
    		// higher score is preferred, so don't forget to invert the sign
    		return - Double.compare(item1.score, item2.score);
    	}
    	
    }    
    
    private class WomanPreferenceComparator implements Comparator<AlignmentRow>{

    	private AlignmentRow woman;
    	private Matrix scoreArr;
  	
    	public WomanPreferenceComparator(AlignmentRow woman, Matrix scoreArr) {
    		this.woman = woman;
    		this.scoreArr = scoreArr;
    	}

    	
    	public int compare(AlignmentRow candidate1, AlignmentRow candidate2) {
    		int myIndex = woman.getPos();
    		int can1Index = candidate1.getPos();
    		int can2Index = candidate2.getPos();
    		double score1 = scoreArr.get(can1Index, myIndex);
    		double score2 = scoreArr.get(can2Index, myIndex);
    		return Double.compare(score1, score2);
    	}
    	
    }   
    
    private class MatchesResult {
    	
        private List<AlignmentMatch> matches;

        public MatchesResult() {
        	this.matches = new ArrayList<AlignmentMatch>();
        }
        
        public void addMatch(Map<AlignmentRow, AlignmentRow> mapping) {
        	for (Entry<AlignmentRow, AlignmentRow> entry : mapping.entrySet()) {
        		AlignmentRow from = entry.getKey();
        		AlignmentRow to = entry.getValue();
        		if (from == null || to == null) {
        			continue;
        		}
        		boolean found = false;
            	for (AlignmentMatch match : matches) {
            		if (match.exist(from, to)) {
            			match.increaseFreq();
            			found = true;
            		}
            	}
            	if (!found) {
            		AlignmentMatch newMatch = new AlignmentMatch(from, to);
            		matches.add(newMatch);
            	}
        	}
        }
        
        public Map<AlignmentRow, AlignmentRow> getConsensus() {
        	Map<AlignmentRow, AlignmentRow> consensus = new HashMap<AlignmentRow, AlignmentRow>();
        	Queue<AlignmentMatch> matchQueue = new PriorityQueue<AlignmentMatch>(11, new AlignmentMatchComparator());
        	matchQueue.addAll(matches);
        	int counter = 0;
        	while (!matchQueue.isEmpty()) {
        		AlignmentMatch match = matchQueue.poll();
        		if (match.isDeleted()) {
        			continue;
        		}
        		if (counter < 20) {
        			System.out.println(match);
        		}
        		counter++;
        		List<AlignmentMatch> remaining = findNotDeleted(match);
        		Collections.sort(remaining, new AlignmentMatchComparator());
        		AlignmentMatch best = remaining.get(0);
        		consensus.put(best.from, best.to);
        		for (int i = 0; i < remaining.size(); i++) {
    				AlignmentMatch rem = remaining.get(i);
    				rem.setDeleted(true);
        		}
        	}
        	return consensus;
        }

		private List<AlignmentMatch> findNotDeleted(
				AlignmentMatch match) {
			List<AlignmentMatch> result = new ArrayList<AlignmentMatch>();
			for (AlignmentMatch other : matches) {
				if (other.isDeleted()) {
					continue;
				}
				Set<Feature> intersection = match.intersect(other);
				if (!intersection.isEmpty()) {
					result.add(other);
				}
			}
			return result;
		}
            	
    }
    
    private class AlignmentMatch {

    	private AlignmentRow from;
    	private AlignmentRow to;
    	private Set<Feature> aligned;
    	private int freq;
    	private boolean deleted;
    	
		public AlignmentMatch(AlignmentRow from, AlignmentRow to) {
			this.deleted = false;
			this.aligned = new HashSet<Feature>();
			add(from, to);
		}
    	
		public void add(AlignmentRow from, AlignmentRow to) {
			this.from = from;
			this.to = to;
			Set<Feature> fromFeatures = from.getFeatures();
			Set<Feature> toFeatures = to.getFeatures();
			this.aligned.addAll(fromFeatures);
			this.aligned.addAll(toFeatures);			
			this.freq = 1;
		}
		
		public boolean exist(AlignmentRow from, AlignmentRow to) {
			for (Feature f : from.getFeatures()) {
				if (!aligned.contains(f)) {
					return false;
				}
			}
			for (Feature f : to.getFeatures()) {
				if (!aligned.contains(f)) {
					return false;
				}
			}
			return true;
		}
		
		public void increaseFreq() {
			this.freq++;
		}
		
		public boolean isDeleted() {
			return deleted;
		}

		public void setDeleted(boolean deleted) {
			this.deleted = deleted;
		}
		
		public Set<Feature> intersect(AlignmentMatch another) {
			Set<Feature> anotherFeatures = another.aligned;
			Set<Feature> intersection = new HashSet<Feature>(this.aligned);
			intersection.retainAll(anotherFeatures);
			return intersection;
		}

		
		public String toString() {
			return "AlignmentMatch [freq=" + freq + ", aligned=" + aligned
					+ "]";
		}
    	
    }
    
    private class AlignmentMatchComparator implements Comparator<AlignmentMatch>{

    	
    	public int compare(AlignmentMatch item1, AlignmentMatch item2) {
    	
    		// higher score is now preferred, so invert the sign
    		return - Double.compare(item1.freq, item2.freq);
   	
    	}
    	
    }    


}
