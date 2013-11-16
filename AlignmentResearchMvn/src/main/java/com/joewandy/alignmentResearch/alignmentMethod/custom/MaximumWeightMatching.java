package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.DistanceCalculator;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.MahalanobisDistanceCalculator;

public class MaximumWeightMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private ExtendedLibrary library;
	private String listId;
	private double massTol;
	private double rtTol;
	private boolean useGroup;

	public MaximumWeightMatching(String listId, AlignmentList masterList,
			AlignmentList childList, ExtendedLibrary library, 
			double massTol, double rtTol, boolean useGroup) {
		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.library = library;
		this.massTol = massTol;
		this.rtTol = rtTol;
		this.useGroup = useGroup;
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
			
			AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
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

	private Map<AlignmentRow, AlignmentRow> match(
			List<AlignmentRow> rows1, List<AlignmentRow> rows2) {

		System.out.println("Matching " + listId);

		// build matrix of row vs row scores
		System.out.print("\tConstructing score matrix ");
		int m = rows1.size();
		int n = rows2.size();
		double[][] scoreMatrix = new double[m][n];
		double maxScore = Double.MIN_VALUE;
		for (int i=0; i<m; i++) {
			if (i % 1000 == 0) {
				System.out.print('.');
			}
			for (int j=0; j<n; j++) {
				AlignmentRow row1 = rows1.get(i);
				AlignmentRow row2 = rows2.get(j);
				double score = 0;
				if (useGroup) {
					double mass1 = row1.getAverageMz();
					double mass2 = row2.getAverageMz();
					double rt1 = row1.getAverageRt();
					double rt2 = row2.getAverageRt();
					DistanceCalculator calc = new MahalanobisDistanceCalculator(massTol, rtTol);
					double dist = calc.compute(mass1, mass2, rt1, rt2);						
					 score = library.computeWeightedRowScore(row1, row2);
				} else {
					score = library.computeRowScore(row1, row2);										
				}
				scoreMatrix[i][j] = score;
				if (score > maxScore) {
					maxScore = score;
				}
			}
		}
		System.out.println();

		/* 
		 * this is needed because the matching algorithm tries to find the MINIMUM weight matching
		 * so we have to invert the scores in the matrix by substracting it from the max score
		 */
		for (int i=0; i<m; i++) {
			for (int j=0; j<n; j++) {
				scoreMatrix[i][j] = maxScore - scoreMatrix[i][j];
			}
		}

		// running matching
		System.out.print("\tRunning matching ");
		HungarianAlgorithm algo = new HungarianAlgorithm(scoreMatrix);
		int[] res = algo.execute();
		System.out.println();
		
		// store the result
		Map<AlignmentRow, AlignmentRow> result = new HashMap<AlignmentRow, AlignmentRow>();
		for (int i=0; i<m; i++) {
			int matchIndex = res[i];
			// if there's a match
			if (matchIndex != -1) {
				AlignmentRow row1 = rows1.get(i);
				AlignmentRow row2 = rows2.get(matchIndex);
				result.put(row1, row2);
			}
		}

		return result;

	}

}
