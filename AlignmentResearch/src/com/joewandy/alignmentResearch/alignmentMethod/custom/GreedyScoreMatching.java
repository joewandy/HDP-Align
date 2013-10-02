package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.ComparisonChain;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class GreedyScoreMatching implements FeatureMatching {

	private AlignmentList masterList;
	private AlignmentList childList;
	private ExtendedLibrary library;
	private String listId;
	private double massTol;
	private double rtTol;

	public GreedyScoreMatching(String listId, AlignmentList masterList,
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

		List<AlignmentRow> suitors = masterList.getRows();
		List<AlignmentRow> reviewers = childList.getRows();
		System.out.println("Running greedy score matching on " + listId);
		System.out.println("masterList = " + masterList.getRowsCount());
		System.out.println("childList = " + childList.getRowsCount());		
		
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
//			if (massDiff > massTol) {
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
		int suitorsMatchedCount = 0;
		int suitorsUnmatchedCount = 0;
		for (AlignmentRow row : suitors) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				suitorsUnmatchedCount++;
			} else {
				suitorsMatchedCount++;
			}
		}
		int reviewersMatchedCount = 0;
		int reviewersUnmatchedCount = 0;
		for (AlignmentRow row : reviewers) {
			if (!row.isAligned()) {
				matchedList.addRow(row);
				reviewersUnmatchedCount++;
			} else {
				reviewersMatchedCount++;
			}
		}
		System.out.println("\tSuitors matched rows = " + suitorsMatchedCount);
		System.out.println("\tSuitors unmatched rows = " + suitorsUnmatchedCount);		
		System.out.println("\tReviewers matched rows = " + reviewersMatchedCount);
		System.out.println("\tReviewers unmatched rows = " + reviewersUnmatchedCount);
		System.out.println("\tRejected rows = " + rejectedCount);
		return matchedList;

	}

	private Map<AlignmentRow, AlignmentRow> match(
			List<AlignmentRow> rows1, List<AlignmentRow> rows2) {

		Map<AlignmentRow, AlignmentRow> result = new HashMap<AlignmentRow, AlignmentRow>();
		System.out.println("Matching " + listId);
		for (int i = 0; i < rows1.size(); i++) {
			
			AlignmentRow row1 = rows1.get(i);
			double maxScore = Double.MIN_VALUE;
			int maxIndex = -1;
			
			for (int j = 0; j < rows2.size(); j++) {
				AlignmentRow row2 = rows2.get(j);
				if (row2.isAligned()) {
					continue;
				}
//				double massDiff = Math.abs(row1.getAverageMz() - row2.getAverageMz());
//				double rtDiff = Math.abs(row1.getAverageRt() - row2.getAverageRt());
//				if (massDiff > massTol) {
//					continue;
//				}
				double currScore = 0;
				if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
					currScore = library.computeWeightedRowScore(row1, row2);					
				} else {
					currScore = library.computeRowScore(row1, row2);										
				}
				if (currScore > maxScore) {
					maxScore = currScore;
					maxIndex = j;
				}
			}
			
			if (maxIndex != -1) {
				AlignmentRow bestMatch = rows2.get(maxIndex);
				row1.setAligned(true);
				bestMatch.setAligned(true);
				result.put(row1, bestMatch);				
			}
			
		}
		
		return result;

	}

}
