package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import peakml.chemistry.PeriodicTable;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.AlignmentVertex;
import com.joewandy.alignmentResearch.objectModel.DistanceCalculator;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibraryEntry;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.MahalanobisDistanceCalculator;
import com.joewandy.alignmentResearch.util.GraphEdgeConstructor;

import edu.uci.ics.jung.graph.Graph;

public abstract class PairwiseLibraryBuilderImpl implements Runnable, PairwiseLibraryBuilder {

	protected BlockingQueue<AlignmentLibrary> queue;

	protected GraphEdgeConstructor edgeConstructor;
	protected int libraryID;
	protected double massTolerance;
	protected double rtTolerance;
	protected AlignmentFile data1;
	protected AlignmentFile data2;

	public PairwiseLibraryBuilderImpl(BlockingQueue<AlignmentLibrary> queue,
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

	@SuppressWarnings("unused")
	public AlignmentLibrary producePairwiseLibrary() {
		
		System.out.println("PairwiseLibraryBuilder #" + libraryID + " running");
		
		List<AlignmentFile> files = new ArrayList<AlignmentFile>();
		files.add(data1);
		files.add(data2);

		AlignmentMethodParam.Builder paramBuilder = new AlignmentMethodParam.Builder(massTolerance, rtTolerance);
		AlignmentMethodParam param = paramBuilder.build();
		AlignmentMethod pairwiseAligner = getAlignmentMethod(files, param);

		AlignmentList result = pairwiseAligner.align();
		List<AlignmentRow> rows = result.getRows();

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
		List<AlignmentEdge> edgeList = edgeConstructor.constructEdgeList(rows, massTolerance, rtTolerance);

		final boolean multiGraph = false;
		final int dataFileCount = 2;
		final double clusterThreshold = 0;
		
		// TODO: poor design of code !!
		// must run the next two lines in this sequence because 
		// things happen to edgeList when constructing the graph view
		CombineGraphView graphView = new CombineGraphView(edgeList, multiGraph, dataFileCount, clusterThreshold, "graph");
		// add pairwise alignment of features to library
		Graph<AlignmentVertex, AlignmentEdge> graph = graphView.getAlignmentGraph();
		AlignmentLibrary library = new AlignmentLibrary(libraryID, data1, data2, edgeList, graph);
		
		// calculate max weight for normalising later
		double maxWeight = Double.MIN_VALUE;
		for (AlignmentEdge e : graph.getEdges()) {
			if (e.getWeight() > maxWeight) {
				maxWeight = e.getWeight();
			}
		}
				
		Map<FeaturePairKey, FeaturePairKey> mappedKeys = new HashMap<FeaturePairKey, FeaturePairKey>();
		for (AlignmentEdge e : graph.getEdges()) {
			List<AlignmentPair> pairs = e.getAlignmentPairs();
			for (AlignmentPair pair : pairs) {

				Feature f1 = pair.getFeature1();
				Feature f2 = pair.getFeature2();
				
				double score = computeSimilarity(f1, f2);

				// TODO: hack to swap binary and probability weight
				double weight = 1;
				if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE) {
	 				weight = e.getWeight();					
				}
				library.addAlignedPair(pair.getFeature1(), pair.getFeature2(), score, weight);					

				FeaturePairKey key = new FeaturePairKey(f1, f2);
				mappedKeys.put(key, key);
				
			}
		}
		
//		if (FeatureXMLAlignment.WEIGHT_USE_WEIGHTED_SCORE && FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
//
//			// find all library entries for all pairwise files
//			Map<FilePairKey, Set<ExtendedLibraryEntry>> filePairMaps = new HashMap<FilePairKey, Set<ExtendedLibraryEntry>>();
//			for (ExtendedLibraryEntry entry : library.getAlignedFeatures()) {
//				AlignmentFile file1 = entry.getFeature1().getData();
//				AlignmentFile file2 = entry.getFeature2().getData();
//				FilePairKey key = new FilePairKey(file1, file2);
//				if (filePairMaps.get(key) != null) {
//					Set<ExtendedLibraryEntry> entries = filePairMaps.get(key);
//					entries.add(entry);
//				} else {
//					Set<ExtendedLibraryEntry> entries = new HashSet<ExtendedLibraryEntry>();
//					entries.add(entry);
//					filePairMaps.put(key, entries);					
//				}
//			}
//			
//			// loop through all library entries and recompute the weight
//			for (ExtendedLibraryEntry entry : library.getAlignedFeatures()) {		
//				
//				Feature fi = entry.getFeature1();
//				Feature fj = entry.getFeature2();
//				
//				int fiIdx = fi.getPeakID();
//				int fjIdx = fj.getPeakID();
//				double allWeight = 0;
//				double[][] ZZProb1 = fi.getZZProb();
//				double[][] ZZProb2 = fj.getZZProb();
//
//				AlignmentFile file1 = fi.getData();
//				AlignmentFile file2 = fj.getData();
//				FilePairKey key = new FilePairKey(file1, file2);
//				Set<ExtendedLibraryEntry> others = filePairMaps.get(key);
//				for (ExtendedLibraryEntry other : others) {
//
//					Feature fm = other.getFeature1();
//					Feature fn = other.getFeature2();
//				
//					// ignore entries outside mass tolerances					
//					if (!checkInMassRange(fi, fm) && !checkInMassRange(fj, fn)) {
//						continue;
//					}
//							
//					int fmIdx = fm.getPeakID();
//					int fnIdx = fn.getPeakID();
//					
//					double prob1 = ZZProb1[fiIdx][fmIdx];
//					double prob2 = ZZProb2[fjIdx][fnIdx];
//					double pairWeight = prob1 * prob2;
//					allWeight += pairWeight;
//				
//				}
//				entry.setWeight(allWeight);
//				
//			}
//
//		}
		
		System.out.println("#" + String.format("%04d ", libraryID) + 
				"(" + data1.getFilenameWithoutExtension() + ", " + data2.getFilenameWithoutExtension() + ")" +  
				" pairwise alignments = " + library.getAlignedPairCount() + 
				" average library weight = " + String.format("%.3f", library.getAvgWeight()));
		return library;
		
	}

	protected abstract AlignmentMethod getAlignmentMethod(List<AlignmentFile> files, AlignmentMethodParam param);

	protected double computeSimilarity(Feature f1, Feature f2) {

		DistanceCalculator calc = new MahalanobisDistanceCalculator(massTolerance, rtTolerance);
		double dist = calc.compute(f1.getMass(), f2.getMass(), f1.getRt(), f2.getRt());
		double similarity = 1/dist;
		return similarity;
				
	}
	
	private boolean checkInMassRange(Feature feature, Feature friend) {
		boolean inRange = false;
		double delta = 0;
		if (FeatureXMLAlignment.ALIGN_BY_RELATIVE_MASS_TOLERANCE) {
			delta = PeriodicTable.PPM(feature.getMass(), massTolerance);			
		} else {
			delta = massTolerance;			
		}
		double massLower = feature.getMass() - delta/2;
		double massUpper = feature.getMass() + delta/2;
		if (friend.getMass() > massLower && friend.getMass() < massUpper) {
			inRange = true;
		}
		return inRange;
	}

}
