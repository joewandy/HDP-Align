package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.matrix.LinkedSparseMatrix;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.MatchingScorer;

public class MaximumWeightMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private String listId;
	private double massTol;
	private double rtTol;	
	private MatchingScorer scorer;
	
	private boolean useGroup;
	private boolean exactMatch;
	private double alpha;
	
	private boolean quiet;
	
	public MaximumWeightMatching(String listId, AlignmentList masterList, AlignmentList childList,
			double massTol, double rtTol, boolean useGroup, boolean exactMatch, double alpha, 
			boolean quiet) {			
		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.massTol = massTol;
		this.rtTol = rtTol;		
		this.scorer = new MatchingScorer(this.massTol, this.rtTol);
		this.useGroup = useGroup;
		this.exactMatch = exactMatch;
		this.alpha = alpha;
		this.quiet = quiet;
	}

	public AlignmentList getMatchedList() {
			
		// nothing in masterlist to match against, just return the childlist directly
		if (masterList.getRowsCount() == 0) {
			return childList;
		}
		
		// do max weight matching here ...
		
		if (!quiet) {
			System.out.println("Running maximum weight matching on " + listId);
		}
		List<MatchResult> matchResults = match(masterList, childList);
		
		// construct a new list and merge the matched entries together
		if (!quiet) {
			System.out.println("\tMerging matched results = " + matchResults.size() + " entries");
		}
		AlignmentList matchedList = new AlignmentList(listId);
		int rowId = 0;
		int rejectedCount = 0;		
		for (MatchResult match : matchResults) {

			AlignmentRow row1 = match.getRow1();
			AlignmentRow row2 = match.getRow2();
						
			row1.setAligned(true);
			row2.setAligned(true);
			
			AlignmentRow merged = new AlignmentRow(masterList, rowId++);
			merged.addAlignedFeatures(row1.getFeatures());
			merged.addAlignedFeatures(row2.getFeatures());
			merged.setScore(match.getScore());
			
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
		if (!quiet) {
			System.out.println("\t\tmen matched rows = " + menMatchedCount);
			System.out.println("\t\tmen unmatched rows = " + menUnmatchedCount);		
			System.out.println("\t\twomen matched rows = " + womenMatchedCount);
			System.out.println("\t\twomen unmatched rows = " + womenUnmatchedCount);
			System.out.println("\tRejected rows = " + rejectedCount);
		}
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
				return getClusteringMatrix(data);				
			} else {

				Matrix mat = null;
				
				// for intermediate list produced from merging files, recluster the peaks
//				if (MultiAlignConstants.GROUPING_METHOD_GREEDY.equals(groupingMethod)) {
//					System.out.println("Grouping newData");
//					AlignmentFile newFile = dataList.getRowsAsFile();
//					List<AlignmentFile> files = new ArrayList<AlignmentFile>();
//					files.add(newFile);
//					groupingMethod.group(files);			
//					mat = groupingMethod.getClusteringMatrix();
//				}			
				
				return mat;
				
			}
		} else {
			// for the case when we don't use the weight
			List<AlignmentRow> rows = dataList.getRows();
			int n = rows.size();
			if (n < 10000) {
				return new DenseMatrix(n, n);								
			} else {
				return new LinkedSparseMatrix(n, n);				
				
			}
		}
	}
	
	private Matrix getClusteringMatrix(AlignmentFile data) {
		return data.getZZProb();
	}
		
    private List<MatchResult> match(AlignmentList masterList, AlignmentList childList) {

    	if (!quiet) {
    		System.out.println("\tmasterList " + masterList.getId());
    		System.out.println("\tchildList " + childList.getId());		    		
    	}
		List<AlignmentRow> men = getMen(masterList, childList);
		List<AlignmentRow> women = getWomen(masterList, childList);
    	
        List<MatchResult> matches = new ArrayList<MatchResult>();
		if (useGroup) {
			Matrix scoreArr = computeScores(men, women);
			Matrix clusteringMen = getClustering(masterList);
			Matrix clusteringWomen = getClustering(childList);        	
			if (clusteringMen != null && clusteringWomen != null) {
				scoreArr = combineScoreMtj(scoreArr, clusteringMen, clusteringWomen);
//				saveToMatlab("/home/joewandy/Dropbox/Project/real_datasets/P2/source_features_080/debug/", masterList.getId(), 
//						scoreArrInitial, scoreArr);				
			}
			if (!exactMatch) {
		    	matches = approxMaxMatching(scoreArr, men, women);				
			} else {
				matches = hungarianMatching(scoreArr, men, women);				
			}
		} else {
			Matrix scoreArr = computeScores(men, women);  
			if (!exactMatch) {
				matches = approxMaxMatching(scoreArr, men, women);        								
			} else {
				matches = hungarianMatching(scoreArr, men, women);				
			}
		}
        
        return matches;
        
    }
    
	private void saveToMatlab(String path, String id, Matrix scoreInitial, Matrix scoreNew) {
		System.out.println("Saving scores to matlab");
		MLDouble scoreInitialMat = new MLDouble("W", toArray(scoreInitial));
		MLDouble scoreNewMat = new MLDouble("S", toArray(scoreNew));
		final Collection<MLArray> output = new ArrayList<MLArray>();
		output.add(scoreInitialMat);
		output.add(scoreNewMat);
		final MatFileWriter writer = new MatFileWriter();
		try {
			String fullPath = path + id + ".W.mat";
			writer.write(fullPath, output);
			System.out.println("Written to " + fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    	
	private List<MatchResult> hungarianMatching(Matrix scoreArr,
			List<AlignmentRow> men, List<AlignmentRow> women) {
		
        double maxScore = getMax(scoreArr);
        
    	// normalise 
    	for (int i = 0; i < men.size(); i++) {
    		for (int j = 0; j < women.size(); j++) {
    			scoreArr.set(i, j, maxScore - scoreArr.get(i, j));
    		}
    	}
		// running matching
    	if (!quiet) {
    		System.out.print("\tRunning maximum weighted matching ");    		
    	}
		HungarianAlgorithm algo = new HungarianAlgorithm(toArray(scoreArr));
		int[] res = algo.execute();
    	if (!quiet) {
    		System.out.println();
    	}
		
		// store the result
		List<MatchResult> matches = new ArrayList<MatchResult>();
		for (int i=0; i<men.size(); i++) {
			int matchIndex = res[i];
			// if there's a match
			if (matchIndex != -1) {
				AlignmentRow row1 = men.get(i);
				AlignmentRow row2 = women.get(matchIndex);
				// and they are within tolerance to each other
				if (row1.rowInRange(row2, massTol, rtTol, MultiAlignConstants.USE_PPM)) {
					MatchResult matchRes = new MatchResult(row1, row2, scoreArr.get(i, matchIndex));
					matches.add(matchRes);
				}
			}
		}
		return matches;

	}

	private List<MatchResult> approxMaxMatching(Matrix scoreArr, 
			List<AlignmentRow> men, 
			List<AlignmentRow> women) {
		
		// running matching
		if (!quiet) {
			System.out.println("\tRunning approximately maximum greedy matching ");			
		}
		PathGrowing algo = new PathGrowing(scoreArr, men, women, massTol, rtTol);
		List<MatchResult> matches = algo.executeGreedy();
//		List<MatchResult> matches = algo.execute();
		return matches;
		
	}

	private Matrix computeScores(List<AlignmentRow> men, List<AlignmentRow> women) {

		int m = men.size();
		int n = women.size();

		if (!quiet) {
	    	System.out.print("\tComputing distances ");			
		}
    	double maxDist = 0;
    	Matrix distArr = null;
    	if (m < 10000 & n < 10000) {
    		distArr = new DenseMatrix(m, n);    		
    	} else {
    		distArr = new LinkedSparseMatrix(m, n);    		
    	}
		for (int i = 0; i < m; i++) {
			
			for (int j = 0; j < n; j++) {
				AlignmentRow man = men.get(i);
				AlignmentRow woman = women.get(j);
				if (man.rowInRange(woman, massTol, rtTol, MultiAlignConstants.USE_PPM)) {
					double dist = scorer.computeDist(man, woman);
					if (dist > maxDist) {
						maxDist = dist;
					}
					distArr.set(i, j, dist);				
				}
			}
			
            if (i % 1000 == 0 & !quiet) {
            	System.out.print('.');
            }
			
		}
		if (!quiet) {
			System.out.println();			
	    	System.out.print("\tComputing scores ");
		}

    	Matrix scoreArr = null;
    	if (m < 10000 & n < 10000) {
    		scoreArr = new DenseMatrix(m, n);    		
    	} else {
    		scoreArr = new LinkedSparseMatrix(m, n);    		
    	}
		for (int i = 0; i < m; i++) {		
			
			for (int j = 0; j < n; j++) {
				double dist = distArr.get(i, j);
				if (dist > 0) {
					double score = 1-(dist/maxDist);
					scoreArr.set(i, j, score);
				}
			}

            if (i % 1000 == 0 & !quiet) {
            	System.out.print('.');
            }
		
		}
		if (!quiet) {
			System.out.println();			
		}
		distArr = null;
		
		// normalise score to 0..1
		double maxScore = getMax(scoreArr);
		scoreArr.scale(1/maxScore);
		
		return scoreArr;
			
	}
	
	private Matrix combineScoreMtj(Matrix scoreArr, Matrix clusteringMen,
			Matrix clusteringWomen) {

		if (!quiet) {
	    	System.out.println("\tCombining scores ");			
		}
		long startTime = System.nanoTime();

		Matrix W = scoreArr;
		double maxScore = getMax(W);
		W.scale(1/maxScore);
		if (!quiet) {
			System.out.println("\t\tW = " + W.numRows() + "x" + W.numColumns());
		}
		
		Matrix A = clusteringMen.copy();
		for (int i = 0; i < A.numRows(); i++) {
			A.set(i, i, 0);
		}
		if (!quiet) {
			System.out.println("\t\tA = " + A.numRows() + "x" + A.numColumns());
		}
		
		Matrix B = clusteringWomen.copy();
		for (int i = 0; i < B.numRows(); i++) {
			B.set(i, i, 0);
		}
		if (!quiet) {
			System.out.println("\t\tB = " + B.numRows() + "x" + B.numColumns());
		}
		
		// D = (A*W)*B;
		if (!quiet) {
			System.out.println("\t\tComputing D=(AW)");
		}
		Matrix AW = null;
    	if (A.numRows() < 10000 & W.numColumns() < 10000) {
    		AW = new DenseMatrix(A.numRows(), W.numColumns());
    	} else {
    		AW = new LinkedSparseMatrix(A.numRows(), W.numColumns());
    	}		
		A.mult(W, AW);
		if (!quiet) {
			System.out.println("\t\tComputing D=(AW)B");			
		}
		Matrix D = null;
    	if (AW.numRows() < 10000 & B.numColumns() < 10000) {
    		D = new DenseMatrix(AW.numRows(), B.numColumns());
    	} else {
    		D = new LinkedSparseMatrix(AW.numRows(), B.numColumns());
    	}		
		AW.mult(B, D);

		// D = Q .* D;
		if (!quiet) {
			System.out.println("\t\tComputing D.*Q");			
		}
		for (MatrixEntry e : W) {
			if (e.get() > 0) {
				// leave as it is
			} else {
				int row = e.row();
				int col = e.column();
				D.set(row, col, 0);
			}
		}
		
//		 D = D ./ max(max(D));
		 maxScore = getMax(D);
		 D.scale(1/maxScore);

		// Wp = (alpha .* W) + ((1-alpha) .* D);
		if (!quiet) {
			System.out.println("\t\tComputing W'=(alpha.*W)+((1-alpha).*D)");			
		}
		W.scale(alpha);
		D.scale(1-alpha);
		scoreArr = W.add(D);
		maxScore = getMax(scoreArr);
		scoreArr.scale(1/maxScore);

		// alternatively, Wp = W .* D
//		System.out.println("\t\tComputing W'=W.*D");
//		for (MatrixEntry e : W) {
//			int row = e.row();
//			int col = e.column();
//			double dCurr = D.get(row, col);
//			double wCurr = e.get();
//			double wNew = dCurr * wCurr;
//			e.set(wNew);
//		}
//		scoreArr = W;
		
		long elapsedTime = (System.nanoTime()-startTime)/1000000000;
		if (!quiet) {
			System.out.println("\tElapsed time = " + elapsedTime + "s");			
		}
		
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
	
	private void normalise(Matrix matrix) {
		double maxScore = this.getMax(matrix);
		for (MatrixEntry e : matrix) {
			double val = e.get();
			val = val / maxScore;
			e.set(val);
		}
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
