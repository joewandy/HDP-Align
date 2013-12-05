package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ComparisonChain;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class DynamicProgrammingMatching implements FeatureMatching {

	public enum BACKTRACK {
		UP, LEFT, UP_LEFT
	};
	
	private final static double EPSILON = 0.00001;
	
	private AlignmentList masterList;
	private AlignmentList childList;
	private ExtendedLibrary library;
	private String listId;
	private double massTol;
	private double rtTol;

	public DynamicProgrammingMatching(String listId, AlignmentList masterList,
			AlignmentList childList, ExtendedLibrary library, 
			double massTol, double rtTol) {
		this.listId = listId;
		this.masterList = masterList;
		this.childList = childList;
		this.library = library;
		this.massTol = massTol;
		this.rtTol = rtTol;
	}

	public AlignmentList getMatchedList() {

		// nothing in masterlist to match against, just return the childlist
		// directly
		if (masterList.getRowsCount() == 0) {
			return childList;
		}

		// do maximum bipartite matching here ...
		List<AlignmentRow> suitors = masterList.getRows();
		List<AlignmentRow> reviewers = childList.getRows();
		System.out.println("Running dynamic programming matching on " + listId);
		Map<AlignmentRow, AlignmentRow> maximumMatch = match(suitors, reviewers);

		// construct a new list and merge the matched entries together
		AlignmentList matchedList = new AlignmentList(listId);
		int rowId = 0;
		int rejectedCount = 0;
		for (Entry<AlignmentRow, AlignmentRow> match : maximumMatch.entrySet()) {

			AlignmentRow row1 = match.getKey();
			AlignmentRow row2 = match.getValue();
			
			// TODO: quick hack. if the matched rows average mass or RT is more than tolerance, ignore this matching 
//			double massDiff = Math.abs(row1.getAverageMz() - row2.getAverageMz());
//			double rtDiff = Math.abs(row1.getAverageRt() - row2.getAverageRt());
//			if (rtDiff > rtTol) {
//				rejectedCount++;
//				continue;
//			}
			
			AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
			merged.addAlignedFeatures(row1.getFeatures());
			merged.addAlignedFeatures(row2.getFeatures());
			matchedList.addRow(merged);
			row1.setAligned(true);
			row2.setAligned(true);

		}

		// add everything else that's unmatched
		int matchedCount = 0;
		int unmatchedCount = 0;
		for (AlignmentRow row : suitors) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				unmatchedCount++;
			} else {
				matchedCount++;
			}
		}
		for (AlignmentRow row : reviewers) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				unmatchedCount++;
			} else {
				matchedCount++;
			}
		}
		System.out.println("\tMatched rows = " + matchedCount);
		System.out.println("\tUnmatched rows = " + unmatchedCount);
		System.out.println("\tRejected rows = " + rejectedCount);
		return matchedList;

	}

	private Map<AlignmentRow, AlignmentRow> match(
			List<AlignmentRow> rows1, List<AlignmentRow> rows2) {

		System.out.println("Matching " + listId);

		// order the rows by retention time
		Collections.sort(rows1, new AlignmentRowComparator());
		Collections.sort(rows2, new AlignmentRowComparator());
		
		System.out.print("\tConstructing score matrix ");
		int m = rows1.size();
		int n = rows2.size();
		double[][] scoreMatrix = new double[m+1][n+1];
		BACKTRACK[][] backtrackMatrix = new BACKTRACK[m + 1][n + 1];
		final double penalty = -1;
		
		for (int i = 0; i < m + 1; i++) {
			scoreMatrix[i][0] = i * penalty;
			backtrackMatrix[i][0] = BACKTRACK.UP;
		}
		for (int j = 0; j < n + 1; j++) {
			scoreMatrix[0][j] = j * penalty;
			backtrackMatrix[0][j] = BACKTRACK.LEFT;
		}
		scoreMatrix[0][0] = 0;
				
		// for each row in the distance matrix (s1)
		for (int i = 1; i <= m; i++) {

			if (i % 1000 == 0) {
				System.out.print('.');
			}

			AlignmentRow row1 = rows1.get(i-1);
			
			// for each column in the distance matrix (s2)
			for (int j = 1; j <= n; j++) {

				AlignmentRow row2 = rows2.get(j-1);

				double score = library.computeRowScore(row1, row2);
				
				double costWithDeletion = scoreMatrix[i - 1][j] + penalty;
				double costWithInsertion = scoreMatrix[i][j - 1] + penalty;
				double costWithSubtitution = scoreMatrix[i - 1][j - 1] + score;

				// update alignment matrix
				double curCost = maxOf(costWithDeletion, costWithInsertion,
						costWithSubtitution);
				scoreMatrix[i][j] = curCost;

				// update backtrack pointer
				if (equalWithinEpsilon(curCost, costWithDeletion)) {
					backtrackMatrix[i][j] = BACKTRACK.UP;
				} else if (equalWithinEpsilon(curCost, costWithInsertion)) {
					backtrackMatrix[i][j] = BACKTRACK.LEFT;
				} else if (equalWithinEpsilon(curCost, costWithSubtitution)) {
					backtrackMatrix[i][j] = BACKTRACK.UP_LEFT;
				}
				
			}
			
		}
		System.out.println();

		// store the result
		System.out.println("\tBacktracking");		
		Map<AlignmentRow, AlignmentRow> result = new HashMap<AlignmentRow, AlignmentRow>();
		printLcs(backtrackMatrix, rows1, rows2, result);

		return result;

	}
	
	/**
	 * Returns the maximum of three numbers
	 * 
	 * @param num1
	 *            The first number
	 * @param num2
	 *            The second number
	 * @param num3
	 *            The third number
	 * @return The minimum of first, second and third numbers
	 */
	private double maxOf(double num1, double num2, double num3) {
		return Math.max(num1, Math.max(num2, num3));
	}
	
	private boolean equalWithinEpsilon(double value1, double value2) {
		if (Math.abs(value1 - value2) < DynamicProgrammingMatching.EPSILON) {
			return true;
		} else {
			return false;
		}
	}
	
	private void printLcs(BACKTRACK[][] b, 
			List<AlignmentRow> rows1, List<AlignmentRow> rows2, 
			Map<AlignmentRow, AlignmentRow> result) {

		int i = rows1.size();
		int j = rows2.size();
		while (i != 0 && j != 0) {

			AlignmentRow row1 = rows1.get(i-1);
			AlignmentRow row2 = rows2.get(j-1);
			result.put(row1, row2);

			if (b[i][j] == BACKTRACK.UP_LEFT) {
				i = i - 1;
				j = j - 1;
			} else if (b[i][j] == BACKTRACK.UP) {
				i = i - 1;
			} else if (b[i][j] == BACKTRACK.LEFT) {
				j = j - 1;
			}			
			
		}
				
	}
	
	private class AlignmentRowComparator implements Comparator<AlignmentRow> {

		
		public int compare(AlignmentRow arg0, AlignmentRow arg1) {
			return Double.compare(arg0.getMinRt(), arg1.getMinRt());
		}
		
	}

}
