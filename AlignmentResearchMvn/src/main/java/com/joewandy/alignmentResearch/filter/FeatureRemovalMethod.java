package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class FeatureRemovalMethod extends BaseRemovalMethod implements RemovalMethod {

	List<AlignmentPair> allAlignmentPairs;
	
	public FeatureRemovalMethod(List<AlignmentPair> allAlignmentPairs) {
		this.allAlignmentPairs = allAlignmentPairs;
	}

	
	public Set<Feature> findFeatures(String filterMethod, double threshold) {
		
		System.out.println("Total alignment pairs = " + allAlignmentPairs.size());

		Set<Feature> allFeatures = new HashSet<Feature>();
		if ("graph".equals(filterMethod)) {

			for (AlignmentPair pair : allAlignmentPairs) {
				Feature f1 = pair.getFeature1();
				f1.graphScore(pair);
				allFeatures.add(f1);
				Feature f2 = pair.getFeature2();
				f2.graphScore(pair);
				allFeatures.add(f2);
			}
			
			// construct a priority queue of features, ordered by scores ascending
			PriorityQueue<Feature> scoreQueue = new PriorityQueue<Feature>(11, 
					new Comparator<Feature>() {
						
						public int compare(Feature arg0,
								Feature arg1) {
							return Double.compare(arg0.getAverageScore(), arg1.getAverageScore());
						}
			});
			scoreQueue.addAll(allFeatures);
			
			// remove the bottom results, up to threshold
			int n = (int) (threshold * scoreQueue.size());
			int counter = 0;
			while (counter < n) {
				Feature feature = scoreQueue.poll();
				feature.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent + "%) features marked for deletion by graph scores");
						
		} else if ("intensity".equals(filterMethod)) {

			for (AlignmentPair pair : allAlignmentPairs) {
				Feature f1 = pair.getFeature1();
				f1.intensityScore(pair);
				allFeatures.add(f1);
				Feature f2 = pair.getFeature2();
				f2.intensityScore(pair);
				allFeatures.add(f2);
			}
			
			// construct a priority queue of features, ordered by scores ascending
			PriorityQueue<Feature> scoreQueue = new PriorityQueue<Feature>(11, 
					new Comparator<Feature>() {
						
						public int compare(Feature arg0,
								Feature arg1) {
							return Double.compare(arg0.getAverageScore(), arg1.getAverageScore());
						}
			});
			scoreQueue.addAll(allFeatures);
			
			// remove the bottom results, up to threshold
			int n = (int) (threshold * scoreQueue.size());
			int counter = 0;
			while (counter < n) {
				Feature feature = scoreQueue.poll();
				feature.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent + "%) features marked for deletion by intensity scores");

		} else if ("random".equals(filterMethod)) {

			for (AlignmentPair pair : allAlignmentPairs) {
				Feature f1 = pair.getFeature1();
				allFeatures.add(f1);
				Feature f2 = pair.getFeature2();
				allFeatures.add(f2);
			}
			List<Feature> shuffledList = new ArrayList<Feature>(allFeatures);
			Collections.shuffle(shuffledList);
			
			// remove the bottom results, up to threshold
			int n = (int) (threshold * shuffledList.size());
			int counter = 0;
			while (counter < n) {
				Feature feature = shuffledList.get(counter);
				feature.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent + "%) features marked for deletion randomly");			

		}
		return allFeatures;
		
	}
	
	public RemovalResult filterRows(AlignmentList listResult) {
		
		List<AlignmentRow> rows = listResult.getRows();
		RemovalResult result = new RemovalResult();
		
		/* 
		 * filter rows based on graph representation
		 * create new alignment rows containing elements not filtered from the original row
		 */
		System.out.println("Filtering alignment pairs by groups - current rows " + rows.size());
		List<AlignmentRow> accepted = new ArrayList<AlignmentRow>();
		List<AlignmentRow> rejected = new ArrayList<AlignmentRow>();
		int rowId = 0;
		for (AlignmentRow row : rows) {

			AlignmentRow acceptedRow = new AlignmentRow(listResult, rowId);
			rowId++;
			for (Feature feature : row.getFeatures()) {
				if (!feature.isDelete()) {
					acceptedRow.addFeature(feature);
				} else {
					AlignmentRow rejectedRow = new AlignmentRow(listResult, rowId);
					rejectedRow.addFeature(feature);
					rejected.add(rejectedRow);
					rowId++;
				}
			}
			if (acceptedRow.getFeaturesCount() != 0) {
				accepted.add(acceptedRow);													
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
