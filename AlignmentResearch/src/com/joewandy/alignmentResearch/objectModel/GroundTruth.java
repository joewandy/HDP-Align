package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


public class GroundTruth {

	private final static double EPSILON = 0.0001;
	private List<GroundTruthFeatureGroup> groundTruth;
	
	public GroundTruth(List<GroundTruthFeatureGroup> groundTruthEntries) {
		
		this.groundTruth = groundTruthEntries;
		System.out.println("Ground truth loaded = " + this.groundTruth.size() + " rows");
		System.out.println("Ground truth features count = " + this.getFeatureCount() + " features");
		
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

	public EvaluationResult evaluate(List<AlignmentRow> alignmentResult) {
		
		// convert alignmentResult to feature groups
		List<FeatureGroup> tool = new ArrayList<FeatureGroup>();
		int groupID = 1;
		for (AlignmentRow row : alignmentResult) {
			FeatureGroup group = new FeatureGroup(groupID);
			groupID++;
			Set<Feature> alignedFeatures = row.getFeatures();
			for (Feature feature : alignedFeatures) {
				feature.clearGroups();
			}
			group.addFeatures(alignedFeatures);
			tool.add(group);
		}
		
		System.out.println("Calculating ");
		
		// for every consensus feature in ground truth
		int N = this.groundTruth.size();
		double precision = 0;
		double recall = 0;
		int totalTp = 0;
		int totalFp = 0;
		int totalPositives = 0;
		int M = 0; 
		for (int i = 0; i < this.groundTruth.size(); i++) {
			
			if (i % 100 == 0) {
				System.out.print('.');
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
		
		System.out.println();		
		EvaluationResult evalRes = new EvaluationResult(precision, recall, f1, f05, totalTp, totalFp, totalPositives, totalTpRatio, totalFpRatio, totalPositiveRatio);
		System.out.println(evalRes);
		return evalRes;

	}

	@Override
	public String toString() {
		return "SimpleGroundTruth [groundTruth=" + groundTruth.size() + " alignments]";
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
