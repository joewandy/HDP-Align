package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class RowRemovalMethod extends BaseRemovalMethod implements
		RemovalMethod {

	List<AlignmentRow> rows;

	public RowRemovalMethod(List<AlignmentRow> rows) {
		this.rows = rows;
	}

	public Set<Feature> findFeatures(String filterMethod, double threshold) {

		Set<Feature> allFeatures = new HashSet<Feature>();
		if ("graph".equals(filterMethod)) {

			double max = Double.MIN_VALUE;
			for (AlignmentRow row : rows) {
				if (row.getPairGraphScore() > max) {
					max = row.getPairGraphScore();
				}
			}
			final double largestScore = max;

			// construct a priority queue of alignment pairs, ordered by scores
			// ascending
			PriorityQueue<AlignmentRow> scoreQueue = new PriorityQueue<AlignmentRow>(
					rows.size(), new Comparator<AlignmentRow>() {
						public int compare(AlignmentRow arg0, AlignmentRow arg1) {
							return Double.compare(
									arg0.getNormalizedPairGraphScore(largestScore),
									arg1.getNormalizedPairGraphScore(largestScore));
						}
					});
			scoreQueue.addAll(rows);

			// remove the bottom results, up to threshold
			int n = (int) (threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = scoreQueue.poll();
				row.setDelete(true);
				counter++;
			}
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent
					+ "%) rows marked for deletion by scores");

		} else if ("intensity".equals(filterMethod)) {

			// construct a priority queue of alignment pairs, ordered by scores
			// ascending
			PriorityQueue<AlignmentRow> scoreQueue = new PriorityQueue<AlignmentRow>(
					rows.size(), new Comparator<AlignmentRow>() {
						public int compare(AlignmentRow arg0, AlignmentRow arg1) {
							return Double.compare(arg0.getPairIntensityScore(),
									arg1.getPairIntensityScore());
						}
					});
			scoreQueue.addAll(rows);

			// remove the bottom results, up to threshold
			int n = (int) (threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = scoreQueue.poll();
				row.setDelete(true);
				counter++;
			}
			final String percent = String.format("%.2f", (threshold * 100));
			System.out
					.println(counter
							+ " ("
							+ percent
							+ "%) rows marked for deletion by relative intensity error");

		} else if ("random".equals(filterMethod)) {

			// shuffle list
			List<AlignmentRow> shuffledList = new ArrayList<AlignmentRow>(rows);
			Collections.shuffle(shuffledList);

			// remove the bottom results, up to threshold
			int n = (int) (threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = shuffledList.get(counter);
				row.setDelete(true);
				counter++;
			}
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent
					+ " %) rows marked for deletion randomly");

		}

		return allFeatures;

	}

	public RemovalResult filterRows(AlignmentList listResult) {

		List<AlignmentRow> rows = listResult.getRows();
		RemovalResult result = new RemovalResult();

		/*
		 * filter rows based on graph representation create new alignment rows
		 * containing elements not filtered from the original row
		 */
		System.out
				.println("Filtering alignment pairs by groups - current rows "
						+ rows.size());
		List<AlignmentRow> accepted = new ArrayList<AlignmentRow>();
		List<AlignmentRow> rejected = new ArrayList<AlignmentRow>();
		int rowId = 0;
		for (AlignmentRow row : rows) {

			if (row.isDelete()) {
				for (Feature f : row.getFeatures()) {
					AlignmentRow rejectedRow = new AlignmentRow(listResult, rowId);
					rowId++;
					rejectedRow.addFeature(f);
					rejected.add(rejectedRow);
				}
			} else {
				accepted.add(row);
			}
			
			if (rowId % 1000 == 0) {
				System.out.print(".");
			}

		}

		printCounts(rows, accepted, rejected);
		result.setAccepted(accepted);
		result.setRejected(rejected);
		return result;

	}

}
