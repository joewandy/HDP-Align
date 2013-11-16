package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections15.Bag;

import cern.colt.Arrays;

import com.joewandy.alignmentResearch.objectModel.AlignmentExpParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpResult;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public abstract class BaseRemovalMethod implements RemovalMethod {
	
	public void printStatistics(AlignmentExpParam parameter,
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
	
	protected void printCounts(List<AlignmentRow> rows,
			List<AlignmentRow> accepted, List<AlignmentRow> rejected) {

		int onePeak = 0;
		int moreThanOnePeak = 0;
		System.out.println();
		int deletedCount = 0;
		int undeletedCount = 0;
		for (AlignmentRow row : rows) {
			if (row.getFeaturesCount() == 1) {
				onePeak++;
			} else {
				moreThanOnePeak++;
			}
			for (Feature feature : row.getFeatures()) {
				if (feature.isDelete()) {
					deletedCount++;
				} else {
					undeletedCount++;
				}
			}
		}		
		System.out.println("one peak = " + onePeak);
		System.out.println("more than one peak = " + moreThanOnePeak);
		System.out.println("initial rows = " + rows.size());
		System.out.println("accepted rows = " + accepted.size());
		System.out.println("rejected rows = " + rejected.size());
		System.out.println("deletedCount = " + deletedCount);
		System.out.println("undeletedCount = " + undeletedCount);

	}
	
}
