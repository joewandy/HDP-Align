package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;
import com.joewandy.alignmentResearch.model.Feature;

public class HdpProbabilityMatching implements FeatureMatching {

	private HDPAlignmentResults alignmentResults;
	private List<AlignmentFile> dataList;
	private int samplesTaken;
	
	public HdpProbabilityMatching(HDPAlignmentResults alignmentResults, List<AlignmentFile> dataList, int samplesTaken) {			
		this.alignmentResults = alignmentResults;
		this.dataList = dataList;
		this.samplesTaken = samplesTaken;
	}

	public AlignmentList getMatchedList() {

		// add matched entries
		AlignmentList matchedList = new AlignmentList("matched_list");
		int rowId = 0;		
		for (Entry<HDPMassClusterFeatures, Integer> match : alignmentResults.getEntries()) {
			HDPMassClusterFeatures item = match.getKey();
			int count = alignmentResults.getCount(item);
			double prob = ((double)count) / samplesTaken;
			AlignmentRow merged = new AlignmentRow(matchedList, rowId++);
			Set<Feature> features = item.getFeatures();
			merged.addAlignedFeatures(features);
			merged.setScore(prob);
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
