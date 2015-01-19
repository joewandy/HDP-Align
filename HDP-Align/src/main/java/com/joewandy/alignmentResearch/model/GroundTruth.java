package com.joewandy.alignmentResearch.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.joewandy.alignmentResearch.main.MultiAlignConstants;


public class GroundTruth {

	private List<FeatureGroup> groundTruth;
	private List<AlignedPeakset> groundTruthPeaksets;	
	private Set<Feature> G;
	private boolean verbose;
	private int gtCombinationSize;
	
	public GroundTruth(List<FeatureGroup> groundTruthEntries, int gtCombinationSize, boolean verbose) {		
		
		this.groundTruth = groundTruthEntries;		
		this.verbose = verbose;
		this.gtCombinationSize = gtCombinationSize;
		buildKSizeCombination();
		
	}

	public void buildKSizeCombination() {
		
		Map<Integer, Integer> sizeMap = new HashMap<Integer, Integer>();		
		this.G = new HashSet<Feature>();
		G.addAll(this.getAllUniqueFeatures());
				
		// convert ground truth entries into a pairwise of aligned features
		this.groundTruthPeaksets = new ArrayList<AlignedPeakset>();
//		System.out.print("Generating all positive pairwise combinations ");
		for (FeatureGroup g : this.groundTruth) {

			// skip single entry ground truth
			int size = g.getFeatureCount();
			if (size < this.gtCombinationSize) {
				continue;
			}
			
			if (sizeMap.containsKey(size)) {
				int count = sizeMap.get(size);
				sizeMap.put(size, count+1);
			} else {
				sizeMap.put(size, 1);
			}
			
			// Create a simple combination generator to generate k-combinations of
			// the initial vector
			Set<Feature> features = g.getFeatures();			
			Generator<Feature> gen = nChoosek(features, this.gtCombinationSize);
//			System.out.println(features);
			for (ICombinatoricsVector<Feature> combination : gen) {		
				List<Feature> combList = combination.getVector();
//				System.out.println("\t" + combList);
				AlignedPeakset peakset = new AlignedPeakset(combList);
				groundTruthPeaksets.add(peakset);
			}
						
		}		

		// print debug message
		if (verbose) {

			System.out.println();
			System.out.println("Initial ground truth sizes = " + sizeMap);
			System.out.println("Total " + this.gtCombinationSize + "-combinations from ground truth = " + groundTruthPeaksets.size());
			
//			double avgRt = 0;
//			double avgMass = 0;
//			for (GroundTruthPeakset pairwise : pairwiseGroundTruth) {
//				avgRt += pairwise.getAbsRtDiff();
//				avgMass += pairwise.getAbsMassDiff();
//			}
//			avgRt = avgRt / pairwiseGroundTruth.size();
//			avgMass = avgMass / pairwiseGroundTruth.size();		
//			System.out.println("Average abs RT diff = " + avgRt);
//			System.out.println("Average abs mass diff = " + avgMass);
			
		}
		
	}
		
	public Set<Feature> getAllUniqueFeatures() {
		Set<Feature> allGtFeatures = new HashSet<Feature>();
		for (FeatureGroup g : this.groundTruth) {
			allGtFeatures.addAll(g.getFeatures());
		}
		return allGtFeatures;
	}

	// Repeating (non-unique) features may be counted more than once here ...
	// Although it shouldn't happen
	public int getFeatureCount() {
		int count = 0;
		for (FeatureGroup entry : this.groundTruth) {
			count += entry.getFeatureCount();
		}		
		return count;
	}
			
	public int getGroundTruthGroupsCount() {
		return groundTruth.size();
	}
	
	public List<FeatureGroup> getGroundTruthFeatureGroups() {
		return this.groundTruth;
	}

	public void clearFeature(Feature example) {
		Iterator<FeatureGroup> it = this.groundTruth.iterator();
		while (it.hasNext()) {
			FeatureGroup group = it.next();
			// find if any group contains this feature
			if (group.clearFeature(example)) {
				// remove group if nothing else remains inside 
				if (group.getFeatureCount() == 0) {
					it.remove();
				}
			}
		}
	}

	public void removeFeature(Feature example) {
		boolean found = false;
		for (FeatureGroup gtg : this.groundTruth) {
			found = gtg.clearFeature(example);
			if (found) {
				break;
			}
		}
	}

	public EvaluationResult evaluateLange(List<AlignmentRow> alignmentResult, int noOfFiles, double dmz, double drt) {
		
		List<FeatureGroup> tool = convertToFeatureGroup(alignmentResult);
		
		// for every consensus feature in ground truth
		int N = this.groundTruth.size();
		int M = 0; 
		double precision = 0;
		double recall = 0;
		int totalTp = 0;
		int totalFp = 0;
		int totalPositives = 0;
		for (int i = 0; i < N; i++) {
									
			/* 
			 * Lange, et al. (2008):
			 * 
			 * We consider the set of consensus features from the tool that contain at
			 * least two features and intersect with a given consensus feature from 
			 * the ground truth.
			 * 
			 * Note: 'consensus feature' --> FeatureGroup here
			 */
			
			FeatureGroup gtConsensus = this.groundTruth.get(i);
			
			// -1 since ID means nothing for our purpose later
			FeatureGroup toolAll = new FeatureGroup(-1); 
			int m = 0;
			for (FeatureGroup toolConsensus : tool) {
				
				// ignore singleton alignments
				if (toolConsensus.getFeatureCount() < 2) {
					continue;
				}								
				int count = getIntersectCount(toolConsensus, gtConsensus);
				if (count > 0) {
					toolAll.addFeatures(toolConsensus.getFeatures());
					m++;
				}
				
			}
			int toolAllCount = toolAll.getFeatureCount();
			if (toolAllCount == 0) {
				// skip if no matches at all
				continue;
			}				

			// get intersection between tool and gold
			Set<Feature> toolIntersect = getIntersection(toolAll, gtConsensus);			
			int toolMatchingCount = toolIntersect.size();
			
			int fp = toolAllCount - toolMatchingCount;
			int tp = toolMatchingCount;
			precision += ((double) toolMatchingCount / toolAllCount);
			recall += ( ((double)toolMatchingCount) / (m*gtConsensus.getFeatureCount()) );

			totalTp += tp;
			totalFp += fp;
			totalPositives += toolAllCount;
			
			// total number of groups that are aligned from tool for all gold_i
			M += m;
			
		}
				
		precision = precision / N;		
		recall = recall / N;

		/*
		 * Calculations here
		 */
		
		double f1 = (2*precision*recall) / (precision + recall);
		double f05 = (1.25*precision*recall) / ((0.25*precision) + recall);
		
		double totalTpRatio = ((double) totalTp) / totalPositives;
		double totalFpRatio = ((double) totalFp) / totalPositives;		
		double totalPositiveRatio = ((double) totalPositives) / totalPositives;				
		
		/*
		 * More calculations here
		 */
		
		EvaluationResult evalRes = computeAdditional(alignmentResult,
				noOfFiles, precision, recall, totalTp, totalFp, 0, totalPositives,
				f1, f05, totalTpRatio, totalFpRatio, totalPositiveRatio, dmz, drt, MultiAlignConstants.PERFORMANCE_MEASURE_LANGE);
		
		return evalRes;

	}

	public EvaluationResult evaluatePairwise(List<AlignmentRow> alignmentResult, int noOfFiles, double dmz, double drt) {
				
		// construct G+, the set of positive pairwise ground truth ==> things that should be aligned together
		Set<AlignedPeakset> gPlus = new HashSet<AlignedPeakset>(this.groundTruthPeaksets);				
		
		// convert tool output into t = a set of pairwise alignments as well
		Set<AlignedPeakset> t = new HashSet<AlignedPeakset>(getAlignedPeaksetCombinations(alignmentResult, this.gtCombinationSize));		
	
		// TP = should be aligned & are aligned = G+ intersect t
		Set<AlignedPeakset> intersect = new HashSet<AlignedPeakset>(gPlus);
		intersect.retainAll(t);
		int TP = intersect.size();
		
		// FN = should be aligned & aren't aligned = G+ \ t
		Set<AlignedPeakset> diff1 = new HashSet<AlignedPeakset>(gPlus);
		diff1.removeAll(t);
		int FN = diff1.size();
						
		// FP = shouldn't be aligned & are aligned = t \ G+
		Set<AlignedPeakset> diff2 = new HashSet<AlignedPeakset>(t);
		diff2.removeAll(gPlus);
		int FP = diff2.size();
		
		int totalPositives = t.size();
		assert(FP == (totalPositives-TP));
		
		// TN = big number, no need to compute
		
		double precision = (double)TP/(TP+FP);
		double recall = (double)TP/(TP+FN);
		
		double f1 = (2*precision*recall) / (precision + recall);
		double f05 = (1.25*precision*recall) / ((0.25*precision) + recall);
		
		double totalTpRatio = (double)TP / totalPositives;
		double totalFpRatio = (double)FP / totalPositives;		
		double totalPositiveRatio = (double)totalPositives / totalPositives;				
				
		EvaluationResult evalRes = computeAdditional(alignmentResult,
				noOfFiles, precision, recall, TP, FP, FN, totalPositives,
				f1, f05, totalTpRatio, totalFpRatio, totalPositiveRatio, dmz, drt, MultiAlignConstants.PERFORMANCE_MEASURE_COMBINATION);
		
		return evalRes;

	}
	
	@Override
	public String toString() {
		return "SimpleGroundTruth [groundTruth=" + groundTruth.size() + " alignments]";
	}
	
	private List<FeatureGroup> convertToFeatureGroup(
			List<AlignmentRow> alignmentResult) {
		
		List<FeatureGroup> tool = new ArrayList<FeatureGroup>();
		int groupID = 1;
		for (AlignmentRow row : alignmentResult) {

			Set<Feature> alignedFeatures = row.getFeatures();
			for (Feature feature : alignedFeatures) {
				feature.clearGroupID();
			}
			
			FeatureGroup group = new FeatureGroup(groupID);
			groupID++;
			group.addFeatures(alignedFeatures);
			tool.add(group);

		}
		return tool;
	
	}
	
	private List<AlignedPeakset> getAlignedPeaksetCombinations(
			List<AlignmentRow> alignmentResult, int k) {
	
		List<AlignedPeakset> tool = new ArrayList<AlignedPeakset>();
		for (AlignmentRow row : alignmentResult) {

			// skip single entry row
			if (row.getFeaturesCount() < k) {
				continue;
			}
			
			Set<Feature> alignedFeatures = row.getFeatures();
			for (Feature feature : alignedFeatures) {
				feature.clearGroupID();
			}
			
			// Create a simple combination generator to generate k-combinations of
			// the initial vector
			Set<Feature> features = row.getFeatures();			
			Generator<Feature> gen = nChoosek(features, k);
			for (ICombinatoricsVector<Feature> combination : gen) {

				boolean valid = false;
				for (Feature f : combination) {
					if (G.contains(f)) {
						valid = true;
						break;						
					}
				}
			
				// only add into tool if any of the feature is present in G ?
				if (valid) {
					AlignedPeakset peakset = new AlignedPeakset(combination.getVector());
					tool.add(peakset);					
				}
				
			}
		
		}
		return tool;
	
	}
	
	private Generator<Feature> nChoosek(Set<Feature> features, int k) {
		ICombinatoricsVector<Feature> initialVector = Factory.createVector(features);
		Generator<Feature> gen = Factory.createSimpleCombinationGenerator(
				initialVector, k);
		return gen;
	}

	private EvaluationResult computeAdditional(
			List<AlignmentRow> alignmentResult, int noOfFiles,
			double precision, double recall, int totalTp, int totalFp, int totalFn,
			int totalPositives, double f1, double f05, double totalTpRatio,
			double totalFpRatio, double totalPositiveRatio, double dmz, double drt, String version) {
		
		int coverageCount = 0;		
		List<Double> sdrtList = new ArrayList<Double>();
		List<Double> mdrtList = new ArrayList<Double>();		
		for (AlignmentRow row : alignmentResult) {
			
			if (row.getFeaturesCount() > noOfFiles/2) {
				coverageCount++;
			}
			
			double[] rts = row.getFeatureRts();
			Variance variance = new Variance();
			double var = variance.evaluate(rts);
			double sdrt = Math.sqrt(var);
			sdrtList.add(sdrt);
			
			Min min = new Min();
			Max max = new Max();
			double minValue = min.evaluate(rts);
			double maxValue = max.evaluate(rts);
			double mdrt = maxValue - minValue;
			mdrtList.add(mdrt);
			
		}

		double[] sdrtArr = listToArray(sdrtList);
		double[] mdrtArr = listToArray(mdrtList);
		
		// compute median & mean SDRT
		Median med1 = new Median();
		Mean mean1 = new Mean();
		double medSdrt = med1.evaluate(sdrtArr);
		double meanSdrt = mean1.evaluate(sdrtArr);

		// compute median & mean MDRT
		Median med2 = new Median();
		Mean mean2 = new Mean();
		double medMdrt = med2.evaluate(mdrtArr);
		double meanMdrt = mean2.evaluate(mdrtArr);
		
		// compute coverage
		double coverage = (double)coverageCount / alignmentResult.size();
		
		EvaluationResult evalRes = new EvaluationResult(
				dmz, drt,
				precision, recall, f1, f05, 
				totalTp, totalFp, totalFn, totalPositives, totalTpRatio, totalFpRatio, totalPositiveRatio,
				medSdrt, meanSdrt, medMdrt, meanMdrt, coverage, version);
		return evalRes;

	}
	
	private double[] listToArray(List<Double> list) {
		double[] arr = new double[list.size()];
		int counter = 0;
		for (Double value : list) {
			arr[counter] = value;
			counter++;
		}
		return arr;
	}
	
	/**
	 * Intersect group1 and group 2
	 * @param group1 The first group
	 * @param group2 The second group
	 * @return The set of features in both group1 and group2
	 */
	public static Set<Feature> getIntersection(FeatureGroup group1, FeatureGroup group2) {
		Set<Feature> set1 = group1.getFeatures();
		Set<Feature> set2 = group2.getFeatures();
		Set<Feature> intersect = new HashSet<Feature>();
		for (Feature f1 : set1) {
			for (Feature f2 : set2) {
				boolean same = f1.equals(f2);
				if (same) {
					intersect.add(f2);
					break;
				}
			}
		}
		return intersect;
	}

	/**
	 * Gets the count of intersecting features between group 1 & group 2
	 * @param group1 The first group
	 * @param group2 The second group
	 * @return The count of features in both group1 and group2
	 */
	public static int getIntersectCount(FeatureGroup group1,
			FeatureGroup group2) {
		Set<Feature> common = getIntersection(group1, group2);
		return common.size();
	}
				
}
