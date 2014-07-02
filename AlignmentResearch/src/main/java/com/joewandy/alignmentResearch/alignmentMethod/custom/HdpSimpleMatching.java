package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class HdpSimpleMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private String listId;
	private double massTol;
	private double rtTol;	
	private Map<HdpResult, HdpResult> hdpResults;
	private String scoringMethod;
	private boolean exactMatch;
		
	public HdpSimpleMatching(String listId, AlignmentList masterList, AlignmentList childList,
			double massTol, double rtTol, Map<HdpResult, HdpResult> resultMap, String scoringMethod,
			boolean exactMatch) {			

		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.massTol = massTol;
		this.rtTol = rtTol;		
		this.hdpResults = resultMap;
		this.scoringMethod = scoringMethod;
		this.exactMatch = exactMatch;
		
	}

	public AlignmentList getMatchedList() {
		
		// nothing in masterlist to match against, just return the childlist directly
		if (masterList.getRowsCount() == 0) {
			return childList;
		}
		
		// do max weight matching here ...
		
		System.out.println("Running maximum weight matching on " + listId);
		List<MatchResult> matchResults = match(masterList, childList);
		
		// construct a new list and merge the matched entries together
		System.out.println("\tMerging matched results = " + matchResults.size() + " entries");
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
			
			// assign score
			double score = match.getScore();
			merged.setScore(score);						
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
		
    private List<MatchResult> match(AlignmentList masterList, AlignmentList childList) {

		System.out.println("\tmasterList " + masterList.getId());
		System.out.println("\tchildList " + childList.getId());		
		List<AlignmentRow> men = getMen(masterList, childList);
		List<AlignmentRow> women = getWomen(masterList, childList);
    	
        List<MatchResult> matches = new ArrayList<MatchResult>();        
		Matrix scoreArr = computeScores(men, women);  
		if (!exactMatch) {
	    	matches = approxMaxMatching(scoreArr, men, women);				
		} else {
			matches = hungarianMatching(scoreArr, men, women);				
		}
        return matches;
        
    }
    
	private List<MatchResult> approxMaxMatching(Matrix scoreArr, 
			List<AlignmentRow> men, 
			List<AlignmentRow> women) {
		
		// running matching
		System.out.println("\tRunning approximately maximum greedy matching ");
		PathGrowing algo = new PathGrowing(scoreArr, men, women, massTol, rtTol);
		List<MatchResult> matches = algo.executeGreedy();
		return matches;
		
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
		System.out.print("\tRunning maximum weighted matching ");    		
		HungarianAlgorithm algo = new HungarianAlgorithm(toArray(scoreArr));
		int[] res = algo.execute();
		System.out.println();
		
		// store the result
		List<MatchResult> matches = new ArrayList<MatchResult>();
		for (int i=0; i<men.size(); i++) {
			int matchIndex = res[i];
			// if there's a match
			if (matchIndex != -1) {
				AlignmentRow row1 = men.get(i);
				AlignmentRow row2 = women.get(matchIndex);
				// and they are within tolerance to each other
				if (row1.rowInRange(row2, massTol, -1, MultiAlignConstants.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
					MatchResult matchRes = new MatchResult(row1, row2, scoreArr.get(i, matchIndex));
					matches.add(matchRes);
				}
			}
		}
		return matches;

	}

	private Matrix computeScores(List<AlignmentRow> men, List<AlignmentRow> women) {

		int m = men.size();
		int n = women.size();
		Matrix scoreArr = new DenseMatrix(m, n);
		
		System.out.println("Scoring method = " + this.scoringMethod);
		
		if (scoringMethod.equals(MultiAlignConstants.SCORING_METHOD_DIST)) {
			
	    	System.out.print("\tComputing distances ");
	    	double maxDist = 0;
			Matrix distArr = new DenseMatrix(m, n);
			for (int i = 0; i < m; i++) {
				
				for (int j = 0; j < n; j++) {
					AlignmentRow man = men.get(i);
					AlignmentRow woman = women.get(j);
					if (man.rowInRange(woman, massTol, rtTol, MultiAlignConstants.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
						double dist = computeDist(man, woman);
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
						
		} else if (scoringMethod.equals(MultiAlignConstants.SCORING_METHOD_HDP_RT)) {
			
			// matching by posterior 'score' -- RT only
	    	System.out.print("\tComputing scores ");
			scoreArr = new DenseMatrix(m, n);
			for (int i = 0; i < m; i++) {		
				
				for (int j = 0; j < n; j++) {
					AlignmentRow man = men.get(i);
					AlignmentRow woman = women.get(j);
					if (man.rowInRange(woman, massTol, rtTol, MultiAlignConstants.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
						double score = this.computePosteriorScore(man, woman);
						scoreArr.set(i, j, score);
					}
				}

	            if (i % 1000 == 0) {
	            	System.out.print('.');
	            }
			
			}
			System.out.println();			
				
		} else if (scoringMethod.equals(MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT)) {
			
			// matching by posterior 'score' -- RT + mass
	    	System.out.print("\tComputing scores ");
			scoreArr = new DenseMatrix(m, n);
			for (int i = 0; i < m; i++) {		
				
				for (int j = 0; j < n; j++) {
					AlignmentRow man = men.get(i);
					AlignmentRow woman = women.get(j);
					if (man.rowInRange(woman, massTol, rtTol, MultiAlignConstants.ALIGN_BY_RELATIVE_MASS_TOLERANCE)) {
						double score = this.computePosteriorScore(man, woman);
						scoreArr.set(i, j, score);
					}
				}

	            if (i % 1000 == 0) {
	            	System.out.print('.');
	            }
			
			}
			System.out.println();			
										
		}
		
		// normalise score to 0..1
		double maxScore = getMax(scoreArr);
		scoreArr.scale(1/maxScore);
				
		return scoreArr;
			
	}
		
	private double computeDist(AlignmentRow row1, AlignmentRow row2) {
		
		double mass1 = row1.getAverageMz();
		double mass2 = row2.getAverageMz();
		
		double rt1 = row1.getAverageRt();
		double rt2 = row2.getAverageRt();
		double rt = rt1 - rt2;
		double mz = mass1 - mass2;
        double dist = Math.sqrt((rt*rt)/(rtTol*rtTol) + (mz*mz)/(massTol*massTol));
				
        return dist;

	}

	private double computeShit(AlignmentRow row1, AlignmentRow row2) {
		
		double mass1 = row1.getAverageMz();
		double mass2 = row2.getAverageMz();

		// this won't work if aligning more than 2 files ?
		Feature feature1 = row1.getFirstFeature();
		Feature feature2 = row2.getFirstFeature();
		HdpResult example = new HdpResult(feature1, feature2);
		HdpResult searched = hdpResults.get(example);

		// assign dist
		double rt = 0;
		if (searched != null) {
			rt = searched.getDistance();
		}			
		
//		double rt1 = row1.getAverageRt();
//		double rt2 = row2.getAverageRt();
//		double rt = rt1 - rt2;
		double mz = mass1 - mass2;
        double dist = Math.sqrt((rt*rt) + (mz*mz));
				
        return dist;

	}
	
	private double computePosteriorScore(AlignmentRow row1, AlignmentRow row2) {

		double score = 0;

		// this won't work if aligning more than 2 files ?
		Feature feature1 = row1.getFirstFeature();
		Feature feature2 = row2.getFirstFeature();
		HdpResult example = new HdpResult(feature1, feature2);
		HdpResult searched = hdpResults.get(example);

		// assign dist
		if (searched != null) {
			score = searched.getSimilarity();
		}			
		
        return score;

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
