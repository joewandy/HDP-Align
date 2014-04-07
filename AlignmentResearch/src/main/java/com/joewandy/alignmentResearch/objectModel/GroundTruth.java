package com.joewandy.alignmentResearch.objectModel;

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


public class GroundTruth {

	private static final String GROUND_TRUTH_NEW = "new";
	private static final String GROUND_TRUTH_OLD = "old";
	private final static double EPSILON = 0.0001;
	private List<GroundTruthFeatureGroup> groundTruth;
	private List<GroundTruthPair> pairwiseGroundTruth;	
	private Set<Feature> G;
	
	public GroundTruth(List<GroundTruthFeatureGroup> groundTruthEntries) {		
		
		this.groundTruth = groundTruthEntries;
		Map<Integer, Integer> sizeMap = new HashMap<Integer, Integer>();
		
		this.G = new HashSet<Feature>();
		G.addAll(this.getAllUniqueFeatures());
				
		// convert ground truth entries into a pairwise of aligned features
		this.pairwiseGroundTruth = new ArrayList<GroundTruthPair>();
		int groupID = 1;
		System.out.print("Generating all positive pairwise combinations ");
		for (GroundTruthFeatureGroup g : this.groundTruth) {

			// skip single entry ground truth
			int size = g.getFeatureCount();
			if (size < 2) {
				continue;
			}
			
			if (sizeMap.containsKey(size)) {
				int count = sizeMap.get(size);
				sizeMap.put(size, count+1);
			} else {
				sizeMap.put(size, 1);
			}
			
			// Create a simple combination generator to generate 2-combinations of
			// the initial vector
			Set<Feature> features = g.getFeatures();			
			Generator<Feature> gen = nChoose2(features);
			for (ICombinatoricsVector<Feature> combination : gen) {
				Feature f1 = combination.getValue(0);
				Feature f2 = combination.getValue(1);	
				GroundTruthPair pairwise = getGroundTruthPair(f1, f2);
				pairwiseGroundTruth.add(pairwise);
				groupID++;
			}
						
		}		
		System.out.println();
		System.out.println("Total pairwise ground truth combinations = " + pairwiseGroundTruth.size());
		System.out.println(sizeMap);
		
	}

	private GroundTruthPair getGroundTruthPair(Feature f1, Feature f2) {
		GroundTruthPair pairwise = null;
		if (f1.getPeakID() == f2.getPeakID()) {
			if (f1.getData().getId() < f2.getData().getId()) {
				pairwise = new GroundTruthPair(f1, f2);
			} else {
				pairwise = new GroundTruthPair(f2, f1);					
			}
		} else {
			if (f1.getPeakID() < f2.getPeakID()) {
				pairwise = new GroundTruthPair(f1, f2);
			} else {
				pairwise = new GroundTruthPair(f2, f1);					
			}			
		}
		return pairwise;
	}
		
	public Set<Feature> getAllUniqueFeatures() {
		Set<Feature> allGtFeatures = new HashSet<Feature>();
		for (GroundTruthFeatureGroup g : this.groundTruth) {
			allGtFeatures.addAll(g.getFeatures());
		}
		return allGtFeatures;
	}
	
	/**
	 * Repeating (non-unique) features may be counted more than once here ...
	 * Although it shouldn't happen
	 * @return The feature count
	 */
	public int getFeatureCount() {
		int count = 0;
		for (GroundTruthFeatureGroup entry : this.groundTruth) {
			count += entry.getFeatureCount();
		}		
		return count;
	}
		
	public Feature[][] getGroundTruthByFilenames(String[] filenames) {

		// collect in list
		List<Feature[]> featureRows = new ArrayList<Feature[]>();
		for (GroundTruthFeatureGroup group : this.groundTruth) {
			Feature[] arr = group.asArray(filenames);
			featureRows.add(arr);
		}
		
		// convert to array
		Feature[][] featureArr = new Feature[featureRows.size()][];
		for (int i = 0; i < featureRows.size(); i++) {
			featureArr[i] = featureRows.get(i);
		}

		return featureArr;
	
	}
	
	public int getGroundTruthGroupsCount() {
		return groundTruth.size();
	}

	public void clearFeature(Feature example) {
		Iterator<GroundTruthFeatureGroup> it = this.groundTruth.iterator();
		while (it.hasNext()) {
			GroundTruthFeatureGroup group = it.next();
			// find if any group contains this feature
			if (group.clearFeature(example)) {
				// remove group if nothing else remains inside 
				if (group.getFeatureCount() == 0) {
					it.remove();
				}
			}
		}
	}

	public EvaluationResult evaluateOld(List<AlignmentRow> alignmentResult, int noOfFiles, double dmz, double drt) {
		
		List<FeatureGroup> tool = convertToFeatureGroup(alignmentResult);
		
//		System.out.println("Calculating ");
		
		// for every consensus feature in ground truth
		int N = this.groundTruth.size();
		int M = 0; 
		double precision = 0;
		double recall = 0;
		int totalTp = 0;
		int totalFp = 0;
		int totalPositives = 0;
		for (int i = 0; i < N; i++) {
			
			if (i % 100 == 0) {
//				System.out.print('.');
			}
						
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
//			if (gtConsensus.getFeatureCount() == 1) {
//				System.out.println("skip ?");
//			}
			
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

		// NOTE: slightly different from the paper here, divide precision by M, not N ?
		// precision = precision / M;
		
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
				f1, f05, totalTpRatio, totalFpRatio, totalPositiveRatio, dmz, drt, GROUND_TRUTH_OLD);
		
		return evalRes;

	}

	public EvaluationResult evaluateNew(List<AlignmentRow> alignmentResult, int noOfFiles, double dmz, double drt) {
				
		// construct G+, the set of positive pairwise ground truth ==> things that should be aligned together
		Set<GroundTruthPair> gPlus = new HashSet<GroundTruthPair>(this.pairwiseGroundTruth);				
		
		// convert tool output into t = a set of pairwise alignments as well
		Set<GroundTruthPair> t = new HashSet<GroundTruthPair>(convertToPairwiseFeatureGroup(alignmentResult));		
	
		// TP = should be aligned & are aligned = G+ intersect t
//		System.out.println("Computing TP");		
		Set<GroundTruthPair> intersect = new HashSet<GroundTruthPair>(gPlus);
		intersect.retainAll(t);
		int TP = intersect.size();
		
		// FN = should be aligned & aren't aligned = G+ \ t
//		System.out.println("Computing FN");		
		Set<GroundTruthPair> diff1 = new HashSet<GroundTruthPair>(gPlus);
		diff1.removeAll(t);
		int FN = diff1.size();
						
		// FP = shouldn't be aligned & are aligned = t \ G+
//		System.out.println("Computing FP");
		Set<GroundTruthPair> diff2 = new HashSet<GroundTruthPair>(t);
		diff2.removeAll(gPlus);
		int FP = diff2.size();
		
		int totalPositives = t.size();
//		System.out.println("TP = " + TP);
//		System.out.println("FP = " + FP);
//		System.out.println("totalPositives = " + totalPositives);
//		System.out.println("totalPositives-TP = " + (totalPositives-TP));
		assert(FP == (totalPositives-TP));
		
		// how many ground truth entries are aligned ?
//		System.out.println("Ground truth entries = ");
//		System.out.print('[');
//		for (GroundTruthPair entry : gPlus) {
//			if (t.contains(entry)) {
//				System.out.print("1, ");
//			} else {
//				System.out.print("0, ");
//			}
//		}
//		System.out.println("];");

		// TN = big number, don't compute
		
		double precision = (double)TP/(TP+FP);
		double recall = (double)TP/(TP+FN);
		
		double f1 = (2*precision*recall) / (precision + recall);
		double f05 = (1.25*precision*recall) / ((0.25*precision) + recall);
		
		double totalTpRatio = (double)TP / totalPositives;
		double totalFpRatio = (double)FP / totalPositives;		
		double totalPositiveRatio = (double)totalPositives / totalPositives;				
				
		EvaluationResult evalRes = computeAdditional(alignmentResult,
				noOfFiles, precision, recall, TP, FP, FN, totalPositives,
				f1, f05, totalTpRatio, totalFpRatio, totalPositiveRatio, dmz, drt, GROUND_TRUTH_NEW);
		
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
				feature.clearGroups();
			}
			
			FeatureGroup group = new FeatureGroup(groupID);
			groupID++;
			group.addFeatures(alignedFeatures);
			tool.add(group);

		}
		return tool;
	
	}
	
	private List<GroundTruthPair> convertToPairwiseFeatureGroup(
			List<AlignmentRow> alignmentResult) {
	
		List<GroundTruthPair> tool = new ArrayList<GroundTruthPair>();
		for (AlignmentRow row : alignmentResult) {

			// skip single entry row
			if (row.getFeaturesCount() < 2) {
				continue;
			}
			
			Set<Feature> alignedFeatures = row.getFeatures();
			for (Feature feature : alignedFeatures) {
				feature.clearGroups();
			}
			
			// Create a simple combination generator to generate 2-combinations of
			// the initial vector
			Set<Feature> features = row.getFeatures();			
			Generator<Feature> gen = nChoose2(features);
			for (ICombinatoricsVector<Feature> combination : gen) {
				Feature f1 = combination.getValue(0);
				Feature f2 = combination.getValue(1);
				
				// only add into tool if either feature is present in G ?
				if (G.contains(f1) || G.contains(f2)) {
					GroundTruthPair pairwise = getGroundTruthPair(f1, f2);
					tool.add(pairwise);					
				}
								
			}
		
		}
		return tool;
	
	}
	
	private Generator<Feature> nChoose2(Set<Feature> features) {
		ICombinatoricsVector<Feature> initialVector = Factory.createVector(features);
		Generator<Feature> gen = Factory.createSimpleCombinationGenerator(
				initialVector, 2);
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
		
//		System.out.println();		
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
				boolean same = compareFeature(f1, f2);
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
	
	public static boolean compareFeature(Feature f1, Feature f2) {

		double mass1 = f1.getMass();
		double rt1 = f1.getRt();
		double intense1 = f1.getIntensity();
		
		double mass2 = f2.getMass();
		double rt2 = f2.getRt();
		double intense2 = f2.getIntensity();
	
		boolean massOk = compareDouble(mass1, mass2);
		boolean rtOk = compareDouble(rt1, rt2);
		boolean intenseOk = compareDouble(intense1, intense2);
		
		return (massOk && rtOk && intenseOk);
	
	}
		
	public static boolean compareDouble(double a, double b){
	    return a == b ? true : Math.abs(a - b) < EPSILON;
	}	
	
}
