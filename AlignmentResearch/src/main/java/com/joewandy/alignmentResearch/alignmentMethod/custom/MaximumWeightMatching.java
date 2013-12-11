package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.MatchingScorer;

public class MaximumWeightMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private String listId;
	private double massTol;
	private double rtTol;	
	private MatchingScorer scorer;
	
	private boolean useGroup;
	private double alpha;
	
	public MaximumWeightMatching(String listId, AlignmentList masterList, AlignmentList childList,
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
		
		// do max weight matching here ...
		
		System.out.println("Running maximum weight matching on " + listId);
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
        
        if (useGroup) {
        	Matrix clusteringMen = getClustering(masterList);
    		Matrix clusteringWomen = getClustering(childList);        	
    		Matrix scoreArrInitial = scoreArr.copy();
    		scoreArr = combineScoreMtj(scoreArr, clusteringMen, clusteringWomen);
//    		saveToMatlab(masterList.getData().getParentPath(), scoreArrInitial, scoreArr);
		}
    	
    	matches = approxMaxMatching(scoreArr, men, women);        	
//    	matches = hungarianMatching(scoreArr, men, women);        	
        
        return matches;
        
    }
    
	private void saveToMatlab(String path, Matrix scoreInitial, Matrix scoreNew) {
		System.out.println("Saving scores to matlab");
		MLDouble scoreInitialMat = new MLDouble("W", toArray(scoreInitial));
		MLDouble scoreNewMat = new MLDouble("Wp", toArray(scoreNew));
		final Collection<MLArray> output = new ArrayList<MLArray>();
		output.add(scoreInitialMat);
		output.add(scoreNewMat);
		final MatFileWriter writer = new MatFileWriter();
		try {
			String fullPath = path + "/mat/W.mat";
			writer.write(fullPath, output);
			System.out.println("Written to " + fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    	
	private Map<AlignmentRow, AlignmentRow> hungarianMatching(Matrix scoreArr,
			List<AlignmentRow> men, List<AlignmentRow> women) {
		
        double maxScore = getMax(scoreArr);
        
    	// normalise 
    	for (int i = 0; i < men.size(); i++) {
    		for (int j = 0; j < women.size(); j++) {
    			scoreArr.set(i, j, maxScore - scoreArr.get(i, j));
    		}
    	}
		// running matching
		System.out.print("\tRunning maximum weighted matching ");
		HungarianAlgorithm algo = new HungarianAlgorithm(toArray(scoreArr));
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
				// and they are within tolerance to each other
				if (row1.rowInRange(row2, massTol, rtTol, MultiAlign.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
					matches.put(row1, row2);
				}
			}
		}
		return matches;

	}

	private Map<AlignmentRow, AlignmentRow> approxMaxMatching(Matrix scoreArr, 
			List<AlignmentRow> men, 
			List<AlignmentRow> women) {
		
		// running matching
		System.out.println("\tRunning approximately maximum greedy matching ");
		PathGrowing algo = new PathGrowing(scoreArr, men, women, massTol, rtTol);
		Map<AlignmentRow, AlignmentRow> matches = algo.executeGreedy();
//		Map<AlignmentRow, AlignmentRow> matches = algo.execute();
		return matches;
		
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
				if (man.rowInRange(woman, massTol, rtTol, MultiAlign.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
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
//		scoreArr = new DoubleMatrix(m, n);
		scoreArr = new DenseMatrix(m, n);
		for (int i = 0; i < m; i++) {		
			
			for (int j = 0; j < n; j++) {
				double dist = distArr.get(i, j);
				if (dist > 0) {
					double score = 1-(dist/maxDist);
//					scoreArr.put(i, j, score);
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

		Matrix A = clusteringMen.copy();
		for (int i = 0; i < A.numRows(); i++) {
			A.set(i, i, 0);
		}
		System.out.println("\t\tA = " + A.numRows() + "x" + A.numColumns());
		 
		Matrix B = clusteringWomen.copy();
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
	
	private double[][] toArray(Matrix matrix) {
		double[][] arr = new double[matrix.numRows()][matrix.numColumns()];
		for (MatrixEntry e : matrix) {
			int i = e.row();
			int j = e.column();
			double val = e.get();
			arr[i][j] = val;
		}
		return arr;
	}

}
