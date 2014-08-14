package com.joewandy.alignmentResearch.alignmentMethod.custom;

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
				unique.add(uniquePair);				
//			}
		}
		
		// construct a new list and merge the matched entries together
		AlignmentList matchedList = new AlignmentList("matched_list");
		int rowId = 0;		
		for (FeaturePairKey uniquePair : unique) {
			AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
			merged.addAlignedFeature(uniquePair.getF1());
			merged.addAlignedFeature(uniquePair.getF2());
			merged.setScore(uniquePair.getScore());			
			matchedList.addRow(merged);
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
