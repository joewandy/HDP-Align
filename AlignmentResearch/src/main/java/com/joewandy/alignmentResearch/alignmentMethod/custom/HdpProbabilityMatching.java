package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class HdpProbabilityMatching implements FeatureMatching {

	private static final double PROBABILITY_THRESHOLD = 0.9;
	private Map<HdpResult, HdpResult> hdpResults;
	private List<AlignmentFile> dataList;
	
	public HdpProbabilityMatching(Map<HdpResult, HdpResult> resultMap, List<AlignmentFile> dataList) {			
		this.hdpResults = resultMap;
		this.dataList = dataList;
	}

	public AlignmentList getMatchedList() {

		Set<FeaturePairKey> unique = new HashSet<FeaturePairKey>();
		for (Entry<HdpResult, HdpResult> match : hdpResults.entrySet()) {
			HdpResult hdpResult = match.getKey();
			double score = hdpResult.getSimilarity();
//			if (score >= PROBABILITY_THRESHOLD) {
				Feature f1 = hdpResult.getFeature1();
				Feature f2 = hdpResult.getFeature2();
				if (f1.getData().equals(f2.getData())) {
					continue;
				}
				FeaturePairKey uniquePair = this.getFeaturePair(f1, f2, score);
				System.out.println(uniquePair);
				unique.add(uniquePair);				
//			}
		}
		System.out.println("unique.size() = " + unique.size());
		
		// construct a new list and merge the matched entries together
		Map<Feature, List<Feature>> pairing = new HashMap<Feature, List<Feature>>();
		for (FeaturePairKey uniquePair : unique) {
			Feature f1 = uniquePair.getF1();
			Feature f2 = uniquePair.getF2();
			if (pairing.containsKey(f1)) {
				pairing.get(f1).add(f2);
			} else {
				List partners = new ArrayList();
				f2.setScore(uniquePair.getScore());
				partners.add(f2);
				pairing.put(f1, partners);				
			}
		}
		AlignmentList matchedList = new AlignmentList("matched_list");
		int rowId = 0;		
		for (Entry<Feature, List<Feature>> match : pairing.entrySet()) {
			Feature f1 = match.getKey();
			List<Feature> candidates = match.getValue();
			System.out.print("Feature " + match.getKey() + " has " + candidates.size() + " candidates, pairings = [");
			Feature f2 = null;
			if (candidates.size() > 1) {
//				// choose the highest score
//				double massDiff = Double.MAX_VALUE;
//				for (Feature can : candidates) {
//					System.out.println("\t" + can.getScore() + " " + can);
//					double diff = Math.abs(f1.getMass() - can.getMass());
//					if (diff < massDiff) {
//						massDiff = diff;
//						f2 = can;
//					}
//				}
//				AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
//				merged.addAlignedFeature(f1);
//				merged.addAlignedFeature(f2);
//				merged.setScore(f2.getScore());			
//				matchedList.addRow(merged);					
//				System.out.print(f2 + ", ");
				for (Feature can : candidates) {
					f2 = can;
					AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
					merged.addAlignedFeature(f1);
					merged.addAlignedFeature(f2);
					merged.setScore(f2.getScore());			
					matchedList.addRow(merged);					
					System.out.print(f2 + ", ");
				}
			} else {
				f2 = candidates.get(0);
				AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
				merged.addAlignedFeature(f1);
				merged.addAlignedFeature(f2);
				merged.setScore(f2.getScore());			
				matchedList.addRow(merged);
				System.out.print(f2 + ", ");
			}			
			System.out.println();
		}
		
		// add everything else that's unmatched
		for (AlignmentFile data : dataList) {
			List<Feature> features = data.getFeatures();
			for (Feature f : features) {
				if (!f.isAligned()) {
					AlignmentRow newRow = new AlignmentRow(matchedList, rowId++);
					matchedList.addRow(newRow);
					newRow.addAlignedFeature(f);
				}
			}
		}
		return matchedList;

	}	
		
	private FeaturePairKey getFeaturePair(Feature f1, Feature f2, double score) {
		FeaturePairKey pairwise = null;
		if (f1.getData().getId() < f2.getData().getId()) {
			pairwise = new FeaturePairKey(f1, f2, score);
		} else {
			pairwise = new FeaturePairKey(f2, f1, score);					
		}
		return pairwise;
	}

	
}
