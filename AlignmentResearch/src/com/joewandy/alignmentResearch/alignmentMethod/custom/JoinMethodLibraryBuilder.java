package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.AlignmentVertex;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.util.GraphEdgeConstructor;

import edu.uci.ics.jung.graph.Graph;

public class JoinMethodLibraryBuilder implements Runnable, PairwiseLibraryBuilder {

	private BlockingQueue<AlignmentLibrary> queue;

	private GraphEdgeConstructor edgeConstructor;
	private int libraryID;
	private double massTolerance;
	private double rtTolerance;
	private AlignmentFile data1;
	private AlignmentFile data2;

	public JoinMethodLibraryBuilder(BlockingQueue<AlignmentLibrary> queue,
			int libraryID, double massTolerance, double rtTolerance, AlignmentFile data1, AlignmentFile data2) {
		this.queue = queue;
		this.edgeConstructor = new GraphEdgeConstructor();
		this.libraryID = libraryID;
		this.massTolerance = massTolerance;
		this.rtTolerance = rtTolerance;
		this.data1 = data1;
		this.data2 = data2;
	}

	@Override
	public void run() {
		try {
			queue.put(producePairwiseLibrary());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public AlignmentLibrary producePairwiseLibrary() {
		
		System.out.println("PairwiseLibraryBuilder #" + libraryID + " running");
		
		List<AlignmentFile> files = new ArrayList<AlignmentFile>();
		files.add(data1);
		files.add(data2);

		AlignmentMethodParam.Builder paramBuilder = new AlignmentMethodParam.Builder(massTolerance, rtTolerance);
		AlignmentMethodParam param = paramBuilder.build();
		AlignmentMethod pairwiseAligner = new CustomJoinAlignment(files, param);
		List<AlignmentRow> rows = pairwiseAligner.align();

		// TODO: hack .. mark all features as unaligned, necessary when doing the final alignment later
		// since we're not processing any features that have been aligned
		for (AlignmentRow row : rows) {
			for (Feature f : row.getFeatures()) {
				f.setAligned(false);
			}
		}

		/*
		 * score the pairwise alignment produced
		 */
		
		// add the edges to library
		List<AlignmentEdge> edgeList = edgeConstructor.constructEdgeList(rows);

		final boolean multiGraph = false;
		final int dataFileCount = 2;
		final double clusterThreshold = 0;
		
		// TODO: poor design of code !!
		// must run the next two lines in this sequence because 
		// things happen to edgeList when constructing the graph view
		CombineGraphView graphView = new CombineGraphView(edgeList, multiGraph, dataFileCount, clusterThreshold);
		AlignmentLibrary library = new AlignmentLibrary(libraryID, data1, data2, edgeList);
		
		// add pairwise alignment of features to library
		Graph<AlignmentVertex, AlignmentEdge> graph = graphView.getAlignmentGraph();

		// calculate max weight for normalising later
		double maxWeight = Double.MIN_VALUE;
		for (AlignmentEdge e : graph.getEdges()) {
			if (e.getWeight() > maxWeight) {
				maxWeight = e.getWeight();
			}
		}
		
		// calculate mean & stdev for file 1
		SummaryStatistics massStats = new SummaryStatistics();
		SummaryStatistics rtStats = new SummaryStatistics();
		for (Feature f : data1.getFeatures()) {
			massStats.addValue(f.getMass());
			rtStats.addValue(f.getRt());
		}
		double massMean1 = massStats.getMean();
		double massStd1 = massStats.getStandardDeviation();
		double rtMean1 = rtStats.getMean();
		double rtStd1 = rtStats.getStandardDeviation();		

		// calculate mean & stdev for file 2
		SummaryStatistics massStats2 = new SummaryStatistics();
		SummaryStatistics rtStats2 = new SummaryStatistics();
		for (Feature f : data2.getFeatures()) {
			massStats2.addValue(f.getMass());
			rtStats2.addValue(f.getRt());
		}
		double massMean2 = massStats2.getMean();
		double massStd2 = massStats2.getStandardDeviation();
		double rtMean2 = rtStats2.getMean();
		double rtStd2 = rtStats2.getStandardDeviation();		
		
		Map<Key, Key> mappedKeys = new HashMap<Key, Key>();
		for (AlignmentEdge e : graph.getEdges()) {
			List<AlignmentPair> pairs = e.getAlignmentPairs();
			for (AlignmentPair pair : pairs) {

				Feature f1 = pair.getFeature1();
				Feature f2 = pair.getFeature2();
				
				double similarity = computeSimilarity(massMean1, massStd1,
						rtMean1, rtStd1, massMean2, massStd2, rtMean2, rtStd2,
						f1, f2);

				double clusterPenalty = e.getWeight() / maxWeight;				

				// NOTE: changed from * to + here !!
				double score = similarity;

				library.addAlignedPair(pair.getFeature1(), pair.getFeature2(), score, clusterPenalty);					
				Key key = new Key(f1, f2);
				mappedKeys.put(key, key);
				
			}
		}
		
		int unmappedKeysCount = 0;
		for (Feature f1 : data1.getFeatures()) {
			for (Feature f2 : data2.getFeatures()) {
				if (Math.abs(f1.getMass() - f2.getMass()) > massTolerance || 
					Math.abs(f1.getRt() - f2.getRt()) > rtTolerance) {
					continue;
				}
				Key key = new Key(f1, f2);
				if (!mappedKeys.containsValue(key)) {
					double similarity = computeSimilarity(massMean1, massStd1,
							rtMean1, rtStd1, massMean2, massStd2, rtMean2, rtStd2,
							f1, f2);
					double score = similarity;
					library.addAlignedPair(f1, f2, score, 0);
					unmappedKeysCount++;
				} else {
					System.out.println("In");
				}
			}
		}

		System.out.println("#" + String.format("%04d ", libraryID) + 
				"(" + data1.getFilenameWithoutExtension() + ", " + data2.getFilenameWithoutExtension() + ")" +  
				" pairwise alignments = " + library.getAlignedPairCount() + 
				" average library weight = " + String.format("%.3f", library.getAvgWeight()));
		System.out.println("\tmappedKeys = " + mappedKeys.size());
		System.out.println("\tunmappedKeysCount = " + unmappedKeysCount);
		return library;
		
	}

	private double computeSimilarity(double massMean1, double massStd1,
			double rtMean1, double rtStd1, double massMean2, double massStd2,
			double rtMean2, double rtStd2, Feature f1, Feature f2) {

		double f1MassZScore = (f1.getMass() - massMean1) / massStd1;
		double f2MassZScore = (f2.getMass() - massMean2) / massStd2;
		double f1RtZScore = (f1.getRt() - rtMean1) / rtStd1;
		double f2RtZScore = (f2.getRt() - rtMean2) / rtStd2;				
		
		double massDist = Math.pow(f1MassZScore-f2MassZScore, 2);
		double rtDist = Math.pow(f1RtZScore-f2RtZScore, 2);
		double euclideanDist = Math.sqrt(massDist + rtDist);
		double similarity = 1/(1+euclideanDist);
		return similarity;
	
	}
	
	private class Key {
		private Feature f1;
		private Feature f2;
		public Key(Feature f1, Feature f2) {
			this.f1 = f1;
			this.f2 = f2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((f1 == null) ? 0 : f1.hashCode());
			result = prime * result + ((f2 == null) ? 0 : f2.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (f1 == null) {
				if (other.f1 != null)
					return false;
			} else if (!f1.equals(other.f1))
				return false;
			if (f2 == null) {
				if (other.f2 != null)
					return false;
			} else if (!f2.equals(other.f2))
				return false;
			return true;
		}
		private JoinMethodLibraryBuilder getOuterType() {
			return JoinMethodLibraryBuilder.this;
		}
	}

}
