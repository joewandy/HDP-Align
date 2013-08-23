package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.FeatureGroup;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class GroundTruthResultFilter implements AlignmentResultFilter {

	private List<FeatureGroup> groundTruth;
	
	public GroundTruthResultFilter(List<FeatureGroup> groundTruth) {
		this.groundTruth = groundTruth;
	}

	@Override
	public List<AlignmentRow> filter(List<AlignmentRow> rows) {
		List<AlignmentRow> filteredRows = new ArrayList<AlignmentRow>();
		for (AlignmentRow row : rows) {
			Set<Feature> alignedFeatures = row.getFeatures();
			FeatureGroup toolConsensus = new FeatureGroup(-1);
			toolConsensus.addFeatures(alignedFeatures);
			for (FeatureGroup gtConsensus : groundTruth) {
				int count = GroundTruth.getIntersectCount(toolConsensus, gtConsensus);
				if (count > 0) {
					filteredRows.add(row);
					break;
				}
			}
		}
		return filteredRows;
	}

	@Override
	public String getLabel() {
		return "alignment result filtering by ground truth overlap";
	}

}
