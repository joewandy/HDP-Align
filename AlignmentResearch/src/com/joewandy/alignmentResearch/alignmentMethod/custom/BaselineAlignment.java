package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.AlignmentVertex;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.MatchingScorer;

import edu.uci.ics.jung.graph.Graph;

public class BaselineAlignment extends BaseAlignment implements AlignmentMethod {

	private ExtendedLibrary library;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public BaselineAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
		ExtendedLibraryBuilder builder = new ExtendedLibraryBuilder(dataList, massTolerance, rtTolerance, 1);		
		Map<Double, List<AlignmentLibrary>> metaLibraries = builder.buildPrimaryLibrary();
		this.library = builder.combineLibraries(metaLibraries);			
	}
	
	@Override
	protected AlignmentList matchFeatures() {
		
//		// save to csv file for debugging
//		for (AlignmentFile data : dataList) {
//			PrintWriter out = null;
//			try {
//				out = new PrintWriter("/home/joewandy/temp/"+ 
//						data.getFilenameWithoutExtension() + ".csv");
//				out.println(Feature.csvHeader());
//				for (Feature feature : data.getFeatures()) {
//					out.println(feature.csvForm());
//				}
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} finally {
//				if (out != null) {
//					out.close();
//				}
//			}
//		}
		
		AlignmentList alignedList = new AlignmentList("");
		
		int rowId = 0;
		for (int i = 0; i < dataList.size(); i++) {

			AlignmentFile data = dataList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + data);

			for (Feature feature : data.getSortedFeatures()) {

				// process unaligned features
				if (!feature.isAligned()) {
					AlignmentRow row = new AlignmentRow(alignedList, rowId);
					Set<Feature> nearbyFeatures = findMatchingFeatures(i, feature);
					nearbyFeatures.add(feature); // remember to add this current feature too
					row.addAlignedFeatures(nearbyFeatures);
					rowId++;
					alignedList.addRow(row);
				}
				
			}			
		}
		
		return alignedList;
				
	}
	
	protected Set<Feature> findMatchingFeatures(int i, Feature referenceFeature) {
		Set<Feature> nearbyFeatures = new HashSet<Feature>();
		for (int j = 0; j < dataList.size(); j++) {
			// match against every other file except ourselves
			if (i == j) {
				continue;
			}
			// find unmatched features within tolerance
			AlignmentFile data = dataList.get(j);
			Set<Feature> unmatched = data.getNextUnalignedFeatures(referenceFeature, 
					this.massTolerance, this.rtTolerance, this.usePpm);
			if (!unmatched.isEmpty()) {
				Feature closest = findHighestClusterSim(referenceFeature, unmatched);
				if (closest == null) {
					continue;
				}
				nearbyFeatures.add(closest);
			} 
		}
		return nearbyFeatures;
	}

	protected Feature findClosestFeature(Feature feature,
			Set<Feature> nearbyFeatures) {
		
		double minDiff = Double.MAX_VALUE;
		Feature closest = null;
		for (Feature neighbour : nearbyFeatures) {
			double featureMz = feature.getMass();
			double neighbourMz = neighbour.getMass();
			double diff = Math.abs(featureMz - neighbourMz);
			if (diff < minDiff) {
				closest = neighbour;
				minDiff = diff;
			}
		}
		return closest;
		
	}

	protected Feature findHighestClusterSim(Feature feature,
			Set<Feature> nearbyFeatures) {

		Graph<AlignmentVertex, AlignmentEdge> graph = library.getGraph();
		MatchingScorer scorer = library.getScorer();
		
		double maxScore = 0;
		Feature closest = null;
		for (Feature neighbour : nearbyFeatures) {
			double score = 0;
			if (FeatureXMLAlignment.WEIGHT_USE_PROB_CLUSTERING_WEIGHT) {
				score = scorer.computeGraphScore(feature, neighbour, graph);
			} else if (FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
				score = scorer.computeProbScore(feature, neighbour);
			} else {
				score = scorer.computeScore(feature, neighbour);
			}
			if (score > maxScore) {
				maxScore = score;
				closest = neighbour;
			}
		}
		return closest;
		
	}
	
}
