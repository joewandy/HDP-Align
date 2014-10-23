package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class HdpProbabilityMatching implements FeatureMatching {

	private Map<HdpResult, HdpResult> hdpResults;
	private List<AlignmentFile> dataList;
	
	public HdpProbabilityMatching(Map<HdpResult, HdpResult> resultMap, List<AlignmentFile> dataList) {			
		this.hdpResults = resultMap;
		this.dataList = dataList;
	}

	public AlignmentList getMatchedList() {

		// add matched entries
		AlignmentList matchedList = new AlignmentList("matched_list");
		int rowId = 0;		
		for (Entry<HdpResult, HdpResult> match : hdpResults.entrySet()) {
			HdpResult hdpResult = match.getKey();
			Feature f1 = hdpResult.getFeature1();
			Feature f2 = hdpResult.getFeature2();
			double score = hdpResult.getSimilarity();
			AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
			merged.addAlignedFeature(f1);
			merged.addAlignedFeature(f2);
			merged.setScore(score);
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
	
}
