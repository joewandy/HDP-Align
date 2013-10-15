package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.collections15.Transformer;

import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class PairRemovalMethod extends BaseRemovalMethod implements RemovalMethod {

	List<AlignmentPair> allAlignmentPairs;
	List<AlignmentPair> removedAlignmentPairs;
	double maxWeight;
	
	public PairRemovalMethod(List<AlignmentPair> allAlignmentPairs,
			List<AlignmentPair> removedAlignmentPairs, double maxWeight) {
		this.allAlignmentPairs = allAlignmentPairs;
		this.removedAlignmentPairs = removedAlignmentPairs;
		this.maxWeight = maxWeight;
	}

	@Override
	public Set<Feature> findFeatures(String filterMethod, double threshold) {
		
		Set<Feature> allFeatures = new HashSet<Feature>();
		System.out.println("Total alignment pairs = " + allAlignmentPairs.size());
		if ("graph".equals(filterMethod)) {

			double temp = 0;
			for (AlignmentPair pair : allAlignmentPairs) {
				if (pair.getDist() > temp) {
					temp = pair.getDist();
				}
			}
			final double maxDist = temp;
			
			// construct a priority queue of alignment pairs, ordered by scores ascending
			PriorityQueue<AlignmentPair> scoreQueue = new PriorityQueue<AlignmentPair>(11, 
					new Comparator<AlignmentPair>() {
						@Override
						public int compare(AlignmentPair arg0,
								AlignmentPair arg1) {
							return Double.compare(arg0.getScore(), arg1.getScore());
						}
			});
			
			for (AlignmentPair pair : allAlignmentPairs) {
				scoreQueue.add(pair);												
			}
			int n = (int) (threshold * allAlignmentPairs.size());

			// remove the bottom results, up to threshold
			int counter = 0;
			while (counter < n) {
				AlignmentPair pair = scoreQueue.poll();
				double score = pair.getScore();
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent + "%) alignment pairs marked for deletion by scores");

		} else if ("intensity".equals(filterMethod)) {

			// construct a priority queue of alignment pairs, ordered by scores ascending
			PriorityQueue<AlignmentPair> intenseQueue = new PriorityQueue<AlignmentPair>(allAlignmentPairs.size(), 
					new Comparator<AlignmentPair>() {
						@Override
						public int compare(AlignmentPair arg0,
								AlignmentPair arg1) {
							return Double.compare(arg0.getRelativeIntensityErrorScore(), 
									arg1.getRelativeIntensityErrorScore());
						}
			});

			for (AlignmentPair pair : allAlignmentPairs) {
				intenseQueue.add(pair);												
			}
			int n = (int) (threshold * allAlignmentPairs.size());
			
			// remove the bottom results, up to threshold
			int counter = 0;
			while (counter < n) {
				AlignmentPair pair = intenseQueue.poll();
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent + "%) alignment pairs marked for deletion by relative intensity squared error");
			
		} else if ("random".equals(filterMethod)) {

			List<AlignmentPair> shuffledList = new ArrayList<AlignmentPair>();

			for (AlignmentPair pair : allAlignmentPairs) {
				shuffledList.add(pair);
			}
			int n = (int) (threshold * allAlignmentPairs.size());
			
			Collections.shuffle(shuffledList);
			
			// remove the bottom results, up to threshold
			int counter = 0;
			while (counter < n) {
				AlignmentPair pair = shuffledList.get(counter);
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent+ " %) alignment pairs marked for deletion randomly");
			
		} else if ("between".equals(filterMethod)) {

			// remove everything in removedAlignmentPairs
			for (AlignmentPair pair : removedAlignmentPairs) {
				pair.setDelete(true);				
			}
			
			final String percent = String.format("%.2f", ((double) removedAlignmentPairs.size()) / allAlignmentPairs.size() * 100);
			System.out.println(removedAlignmentPairs.size() + " (" + percent+ 
					" %) alignment pairs marked for deletion by edge betweenness clustering");			

		}
		
		return allFeatures;
		
	}
	
	public RemovalResult filterRows(AlignmentList alignmentList) {
		
		List<AlignmentRow> rows = alignmentList.getRows();
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
			
			// skip singleton alignment
			if (row.getFeaturesCount() == 1) {
				continue;
			}
			
			// find the accepted
//			AlignmentRow acceptedRow = new AlignmentRow(alignmentList, rowId);
//			for (AlignmentPair pair : row.getPairs()) {
//				if (!pair.isDelete()) {
//					acceptedRow.addFeature(pair.getFeature1());
//					acceptedRow.addFeature(pair.getFeature2());
//					acceptedRow.addPair(pair);
//				}
//			}
//			if (acceptedRow.getFeaturesCount() != 0) {
//				rowId++;
//				accepted.add(acceptedRow);									
//			}

			// find the rejected
//			AlignmentRow rejectedRow = new AlignmentRow(alignmentList, rowId);			
//			for (Feature f : row.getFeatures()) {
//				if (!acceptedRow.contains(f)) {
//					rejectedRow.addFeature(f);
//				}
//			}
//			if (rejectedRow.getFeaturesCount() != 0) {
//				rowId++;
//				rejected.add(acceptedRow);									
//			}			

			boolean hasDeleted = false;
			for (AlignmentPair pair : row.getPairs()) {
				if (pair.isDelete()) {
					hasDeleted = true;
				}
			}
			if (!hasDeleted) {
				accepted.add(row);
			} else {
				// we need to find out whether our peakset has been split into several components
				Graph<Feature, AlignmentPair> graph = new UndirectedSparseGraph<Feature, AlignmentPair>();
				for (Feature f : row.getFeatures()) {
					graph.addVertex(f);
				}
				int edgeCount = 0;
				for (AlignmentPair pair : row.getPairs()) {
					if (!pair.isDelete()) {
						graph.addEdge(pair, pair.getFeature1(), pair.getFeature2());						
						edgeCount++;
					}
				}
				assert(edgeCount == graph.getEdgeCount());
				Transformer<Graph<Feature, AlignmentPair>, Set<Set<Feature>>> trns = new WeakComponentClusterer<Feature, AlignmentPair>();
				Set<Set<Feature>> clusters = trns.transform(graph);	
				for (Set<Feature> features : clusters) {
					AlignmentRow acceptedRow = new AlignmentRow(alignmentList, rowId);
					acceptedRow.addFeatures(features);
					rowId++;
					accepted.add(acceptedRow);
				}
			}
			
			if (rowId != 0 && rowId % 1000 == 0) {
				System.out.print(".");
			}
						
		}

		printCounts(rows, accepted, rejected);
		result.setAccepted(accepted);
		result.setRejected(rejected);
		return result;
		
	}

}
