package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.apache.commons.collections15.Bag;

import cern.colt.Arrays;

import com.joewandy.alignmentResearch.alignmentMethod.custom.CombineGraphView;
import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpResult;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.util.GraphEdgeConstructor;

public class GraphAlignmentResultFilter implements AlignmentResultFilter {

	private List<AlignmentFile> alignmentDataList;
	private String graphFilter;
	private double threshold;
	private GraphEdgeConstructor edgeConstructor;
	
	/**
	 * Constructs a new graph alignment result filter
	 * @param alignmentDataList The aligment input files
	 * @param graphFilter Which filtering method to use to remove edges, for experiments only
	 * @param threshold Percentile above which we will retain the edges, goes from 0 .. 1
	 */
	public GraphAlignmentResultFilter(
			List<AlignmentFile> alignmentDataList,
			String graphFilter, 
			double threshold) {
		this.alignmentDataList = alignmentDataList;
		this.graphFilter = graphFilter;
		this.threshold = threshold;
		this.edgeConstructor = new GraphEdgeConstructor();
	}

	@Override
	public List<AlignmentRow> filter(List<AlignmentRow> rows) {

		/* 
		 * edge list also contains the vertices etc.
		 * Inside constructEdgeList(), members of rows will be modified to contain their corresponding alignment pairs
		 */
		System.out.println("Constructing edge list");
		List<AlignmentEdge> edgeList = edgeConstructor.constructEdgeList(rows);

		/* 
		 * construct the actual graph here, based on the complete edgeList of all edges
		 * when the graph is constructed and scores (edge weight) assigned to alignmentPairs inside each edges,
		 * the same aligmentPair in each row would also have their corresponding scores
		 */
		int dataFileCount = alignmentDataList.size();
		System.out.println("Creating graph view");
		CombineGraphView combineGraphView = new CombineGraphView(edgeList, false, dataFileCount, this.threshold);
		
		System.out.println("Computing graph result");
		AlignmentExpResult result = combineGraphView.computeStatistics();

		String label = "mygraph";
		final String myLayout = CombineGraphView.LAYOUT_SPRING;
		final String msg = "graph filtering";
		// combineGraphView.visualiseGraph(label, msg, 1000, 700, combineGraphView.getAlignmentGraph(), myLayout);
		
		// sort all alignmentPairs by the specified graph filter method

		 List<AlignmentPair> allAlignmentPairs = result.getAlignmentPairs();			
		 List<AlignmentPair> removedAlignmentPairs = result.getRemovedEdgesAlignmentPairs();
		
		// delete by pairs
		// markPairForDelete(allAlignmentPairs, removedAlignmentPairs);		

		// delete by row score
		// markRowForDelete(rows);		

		// delete by feature score
		Set<Feature> allFeatures = markFeatureForDelete(allAlignmentPairs, removedAlignmentPairs);

		// generate matlab-friendly output
		AlignmentExpParam parameter = new AlignmentExpParam(label);
		printForMatlab(parameter, result, allFeatures);
		
		/* 
		 * filter rows based on graph representation
		 * create new alignment rows containing elements not filtered from the original row
		 */
		System.out.println("Filtering alignment pairs by groups - current rows " + rows.size());
		int rowId = 0;
		List<AlignmentRow> filteredRows = new ArrayList<AlignmentRow>();		
		for (AlignmentRow row : rows) {

			/*
			 * MARK PAIRS FOR DELETE
			 */
			
			/* 
			 * if an existing row contains some alignment pairs to delete, then keep
			 * them in the new row
			 */
			
//			SimpleAlignmentRow newRow = new SimpleAlignmentRow(rowId);
//			boolean createNewRow = false;			
//			for (AlignmentPair pair : row.getPairs()) {
//				if (!pair.isDelete()) {
//					newRow.addFeature(pair.getFeature1());
//					newRow.addFeature(pair.getFeature2());
//					newRow.addPair(pair);
//					// at least 1 alignment pair in this new row
//					createNewRow = true; 
//				}
//			}
//			
//			if (createNewRow) {
//				rowId++;
//				filteredRows.add(newRow);									
//			}
//
//			if (rowId % 1000 == 0) {
//				System.out.print(".");
//			}
	
			/*
			 * MARK ROW FOR DELETE
			 */
			
//			if (!row.isDelete()) {
//				undeletedCount++;
//				filteredRows.add(row);
//			} else {
//				deletedCount++;
//			}
			
			/*
			 * MARK FEATURE FOR DELETE
			 */

			AlignmentRow newRow = new AlignmentRow(rowId);
			boolean createNewRow = false;			
			for (Feature feature : row.getFeatures()) {
				if (!feature.isDelete()) {
					newRow.addFeature(feature);
					// alignment pair information is discard from old to new row
					// newRow.addPair(pair);
					createNewRow = true; 
				}
			}
			
			if (createNewRow) {
				rowId++;
				filteredRows.add(newRow);									
			}

			if (rowId % 1000 == 0) {
				System.out.print(".");
			}
			
		}

		System.out.println();
		int deletedCount = 0;
		int undeletedCount = 0;
		for (AlignmentRow row : rows) {
			for (Feature feature : row.getFeatures()) {
				if (feature.isDelete()) {
					deletedCount++;
				} else {
					undeletedCount++;
				}
			}
		}
		
		System.out.println("initial rows = " + rows.size());
		System.out.println("filtered rows = " + filteredRows.size());
		System.out.println("deletedCount = " + deletedCount);
		System.out.println("undeletedCount = " + undeletedCount);
		
		return filteredRows;

	}

	@Override
	public String getLabel() {
		return "alignment result filtering by peak group information";
	}	

	private void printForMatlab(AlignmentExpParam parameter,
			AlignmentExpResult experimentResult, Set<Feature> allFeatures) {
	
		Bag<Integer> degreeDistribution = experimentResult.getDegreeDistribution();
		List<Integer> uniqueDegrees = new ArrayList<Integer>(degreeDistribution.uniqueSet());

		int[] vertexDegrees = new int[uniqueDegrees.size()];
		int[] vertexDegreesCounts = new int[uniqueDegrees.size()];
		for (int i = 0; i < uniqueDegrees.size(); i++) {
			int degree = uniqueDegrees.get(i);
			vertexDegrees[i] = degree;
			vertexDegreesCounts[i] = degreeDistribution.getCount(vertexDegrees[i]);
		}
		
		Bag<Double> edgeDistribution = experimentResult.getEdgeWeightDistribution();
		Map<Double, Double> intensityByEdgeWeight = experimentResult.getIntensityByEdgeWeight();
		Map<Double, Integer> groupSizeByEdgeWeight = experimentResult.getGroupSizeByEdgeWeight();
		List<Double> uniqueEdges = new ArrayList<Double>(edgeDistribution.uniqueSet());

		double[] edgeWeights = new double[uniqueEdges.size()];
		int[] edgeWeightsCounts = new int[uniqueEdges.size()];
		double[] intensities = new double[uniqueEdges.size()];
		int[] groupSizes = new int[uniqueEdges.size()];
		for (int i = 0; i < uniqueEdges.size(); i++) {
			edgeWeights[i] = uniqueEdges.get(i);
			edgeWeightsCounts[i] = edgeDistribution.getCount(edgeWeights[i]);
			double sumIntensity = intensityByEdgeWeight.get(edgeWeights[i]);
			double intense = sumIntensity / edgeWeightsCounts[i];
			intensities[i] = intense;
			groupSizes[i] = groupSizeByEdgeWeight.get(edgeWeights[i]);
		}
		
		System.out.println("vertex_degrees_" + parameter.getLabel() + " = " + Arrays.toString(vertexDegrees) + ";");
		System.out.println("vertex_degrees_counts_" + parameter.getLabel() + " = " + Arrays.toString(vertexDegreesCounts) + ";");				
		System.out.println("edge_weights_" + parameter.getLabel() + " = " + Arrays.toString(edgeWeights) + ";");
		System.out.println("edge_weights_counts_" + parameter.getLabel() + " = " + Arrays.toString(edgeWeightsCounts) + ";");				
		
//		List<AlignmentPair> allAlignments = experimentResult.getAlignmentPairs();
//		double[] alignmentPairErr = new double[allAlignments.size()];
//		double[] alignmentPairScore = new double[allAlignments.size()];
//		int i = 0;
//		for (AlignmentPair align : allAlignments) {
//			alignmentPairErr[i] = align.getRelativeIntensityErrorScore();
//			alignmentPairScore[i] = align.getScore();
//			i++;
//		}
//		System.out.println("intensities_" + parameter.getLabel() + " = " + Arrays.toString(intensities) + ";");				
//		System.out.println("group_size_" + parameter.getLabel() + " = " + Arrays.toString(groupSizes) + ";");				
//		System.out.println("alignment_intensity_" + parameter.getLabel() + " = " + Arrays.toString(alignmentPairErr) + ";");				
//		System.out.println("alignment_score_" + parameter.getLabel() + " = " + Arrays.toString(alignmentPairScore) + ";");				

//		List<Double> featureIntensityErr = new ArrayList<Double>();
//		List<Double> featureScore = new ArrayList<Double>();
//		for (Feature feature : allFeatures) {
//			if (!feature.isDelete()) {
//				featureIntensityErr.add(feature.getAverageIntensityError());
//				featureScore.add(feature.getAverageScore());				
//			}
//		}
//		System.out.println("feature_intensity_err_" + parameter.getLabel() + " = " + Arrays.toString(featureIntensityErr.toArray()) + ";");				
//		System.out.println("feature_score_" + parameter.getLabel() + " = " + Arrays.toString(featureScore.toArray()) + ";");						
//		System.out.println();

	}
	
	private void markPairForDelete(List<AlignmentPair> allAlignmentPairs, List<AlignmentPair> removedAlignmentPairs) {
		
		System.out.println("Total alignment pairs = " + allAlignmentPairs.size());
		if ("graph".equals(this.graphFilter)) {
		
			// construct a priority queue of alignment pairs, ordered by scores ascending
			PriorityQueue<AlignmentPair> scoreQueue = new PriorityQueue<AlignmentPair>(11, 
					new Comparator<AlignmentPair>() {
						@Override
						public int compare(AlignmentPair arg0,
								AlignmentPair arg1) {
							return Double.compare(arg0.getScore(), arg1.getScore());
						}
			});
			
			// skip very low-scoring pair
			int removed = 0;
			for (AlignmentPair pair : allAlignmentPairs) {
				if (pair.getScore() == 1 || pair.getScore() == 2) {
					pair.setDelete(true);
					removed++;
				} else {
					scoreQueue.add(pair);												
				}
			}

			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * (allAlignmentPairs.size()-removed));
			int counter = 0;
			while (counter < n) {
				AlignmentPair pair = scoreQueue.poll();
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) alignment pairs marked for deletion by scores");

		} else if ("graphAbsolute".equals(this.graphFilter)) {

			// construct a priority queue of alignment pairs, ordered by scores ascending
			PriorityQueue<AlignmentPair> scoreQueue = new PriorityQueue<AlignmentPair>(allAlignmentPairs.size(), 
					new Comparator<AlignmentPair>() {
						@Override
						public int compare(AlignmentPair arg0,
								AlignmentPair arg1) {
							return Double.compare(arg0.getScore(), arg1.getScore());
						}
			});

			// skip very low-scoring pair
			int removed = 0;
			for (AlignmentPair pair : allAlignmentPairs) {
				if (pair.getScore() == 1 || pair.getScore() == 2) {
					pair.setDelete(true);
					removed++;
				} else {
					scoreQueue.add(pair);												
				}
			}

			// remove the bottom results, up to threshold
			int counter = 0;
			double total = scoreQueue.size();
			while (!scoreQueue.isEmpty()) {
				AlignmentPair pair = scoreQueue.poll();
				if (pair.getScore() > this.threshold) {
					break;
				}
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (counter / total * 100));
			System.out.println(counter + " (" + percent + "%) alignment pairs marked for deletion by absolute scores");
			
		} else if ("intensity".equals(graphFilter)) {

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

			// skip very low-scoring pair
			int removed = 0;
			for (AlignmentPair pair : allAlignmentPairs) {
				if (pair.getScore() == 1 || pair.getScore() == 2) {
					pair.setDelete(true);
					removed++;
				} else {
					intenseQueue.add(pair);												
				}
			}

			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * (allAlignmentPairs.size()-removed));
			int counter = 0;
			while (counter < n) {
				AlignmentPair pair = intenseQueue.poll();
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) alignment pairs marked for deletion by relative intensity squared error");
			
		} else if ("random".equals(graphFilter)) {

			// shuffle list
			int removed = 0;
			List<AlignmentPair> shuffledList = new ArrayList<AlignmentPair>();
			for (AlignmentPair pair : allAlignmentPairs) {
				if (pair.getScore() == 1 || pair.getScore() == 2) {
					pair.setDelete(true);
					removed++;
				} else {
					shuffledList.add(pair);
				}
			}
			Collections.shuffle(shuffledList);
			
			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * (allAlignmentPairs.size()-removed));
			int counter = 0;
			while (counter < n) {
				AlignmentPair pair = shuffledList.get(counter);
				pair.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent+ " %) alignment pairs marked for deletion randomly");
			
		} else if ("between".equals(graphFilter)) {

			// remove everything in removedAlignmentPairs
			for (AlignmentPair pair : removedAlignmentPairs) {
				pair.setDelete(true);				
			}
			
			final String percent = String.format("%.2f", ((double) removedAlignmentPairs.size()) / allAlignmentPairs.size() * 100);
			System.out.println(removedAlignmentPairs.size() + " (" + percent+ 
					" %) alignment pairs marked for deletion by edge betweenness clustering");			

		}
	}
	
	private void markRowForDelete(List<AlignmentRow> rows) {

		if ("graph".equals(this.graphFilter)) {

			double max = Double.MIN_VALUE;
			for (AlignmentRow row : rows) {
				if (row.getPairGraphScore() > max) {
					max = row.getPairGraphScore();
				}
			}
			final double largestScore = max;
			
			// construct a priority queue of alignment pairs, ordered by scores ascending
			PriorityQueue<AlignmentRow> scoreQueue = new PriorityQueue<AlignmentRow>(rows.size(), 
					new Comparator<AlignmentRow>() {
						@Override
						public int compare(AlignmentRow arg0,
								AlignmentRow arg1) {
							return Double.compare(arg0.getNormalizedPairGraphScore(largestScore), arg1.getNormalizedPairGraphScore(largestScore));
						}
			});
			scoreQueue.addAll(rows);			

			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = scoreQueue.poll();
				row.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) rows marked for deletion by scores");

		} else if ("intensity".equals(graphFilter)) {
			
			// construct a priority queue of alignment pairs, ordered by scores ascending
			PriorityQueue<AlignmentRow> scoreQueue = new PriorityQueue<AlignmentRow>(rows.size(), 
					new Comparator<AlignmentRow>() {
						@Override
						public int compare(AlignmentRow arg0,
								AlignmentRow arg1) {
							return Double.compare(arg0.getPairIntensityScore(), arg1.getPairIntensityScore());
						}
			});
			scoreQueue.addAll(rows);			

			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = scoreQueue.poll();
				row.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) rows marked for deletion by relative intensity error");
			
		} else if ("random".equals(graphFilter)) {

			// shuffle list
			List<AlignmentRow> shuffledList = new ArrayList<AlignmentRow>(rows);
			Collections.shuffle(shuffledList);
			
			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = shuffledList.get(counter);
				row.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent+ " %) rows marked for deletion randomly");						

		}
		
	}
	
	private Set<Feature> markFeatureForDelete(List<AlignmentPair> allAlignmentPairs, List<AlignmentPair> removedAlignmentPairs) {
		
		System.out.println("Total alignment pairs = " + allAlignmentPairs.size());

		Set<Feature> allFeatures = new HashSet<Feature>();
		if ("graph".equals(this.graphFilter)) {

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
						@Override
						public int compare(Feature arg0,
								Feature arg1) {
							return Double.compare(arg0.getAverageScore(), arg1.getAverageScore());
						}
			});
			scoreQueue.addAll(allFeatures);
			
			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * scoreQueue.size());
			int counter = 0;
			while (counter < n) {
				Feature feature = scoreQueue.poll();
				feature.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) features marked for deletion by graph scores");
						
		} else if ("intensity".equals(graphFilter)) {

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
						@Override
						public int compare(Feature arg0,
								Feature arg1) {
							return Double.compare(arg0.getAverageScore(), arg1.getAverageScore());
						}
			});
			scoreQueue.addAll(allFeatures);
			
			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * scoreQueue.size());
			int counter = 0;
			while (counter < n) {
				Feature feature = scoreQueue.poll();
				feature.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) features marked for deletion by intensity scores");

		} else if ("random".equals(graphFilter)) {

			for (AlignmentPair pair : allAlignmentPairs) {
				Feature f1 = pair.getFeature1();
				allFeatures.add(f1);
				Feature f2 = pair.getFeature2();
				allFeatures.add(f2);
			}
			List<Feature> shuffledList = new ArrayList<Feature>(allFeatures);
			Collections.shuffle(shuffledList);
			
			// remove the bottom results, up to threshold
			int n = (int) (this.threshold * shuffledList.size());
			int counter = 0;
			while (counter < n) {
				Feature feature = shuffledList.get(counter);
				feature.setDelete(true);
				counter++;
			}			
			final String percent = String.format("%.2f", (this.threshold * 100));
			System.out.println(counter + " (" + percent + "%) features marked for deletion randomly");			

		}
		return allFeatures;

	}
				
}
