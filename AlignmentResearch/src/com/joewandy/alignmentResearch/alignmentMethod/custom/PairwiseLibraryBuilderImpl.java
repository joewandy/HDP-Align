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
import com.joewandy.alignmentResearch.main.MultiAlign;
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

		AlignmentLibrary library = new AlignmentLibrary(libraryID, data1, data2);
		for (AlignmentRow row : rows) {

			// TODO: hack .. mark all features as unaligned, necessary when doing the final alignment later
			// since we're not processing any features that have been aligned
			for (Feature f : row.getFeatures()) {
				f.setAligned(false);
			}
			
			if (row.getFeaturesCount() == 2) {
				Feature[] features = row.getFeatures().toArray(new Feature[0]);
				Feature f1 = features[0];
				Feature f2 = features[1];
				double score = computeSimilarity(f1, f2);
				double weight = 1;
				library.addAlignedPair(f1, f2, score, weight);					
			}
			
		}
		
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

}
