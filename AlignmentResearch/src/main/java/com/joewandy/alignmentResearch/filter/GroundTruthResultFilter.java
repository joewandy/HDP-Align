package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.FeatureGroup;
import com.joewandy.alignmentResearch.model.GroundTruth;

public class GroundTruthResultFilter implements AlignmentResultFilter {

	private List<FeatureGroup> groundTruth;
	private List<AlignmentRow> accepted;
	private List<AlignmentRow> rejected;
	
	public GroundTruthResultFilter(List<FeatureGroup> groundTruth) {
		this.groundTruth = groundTruth;
		this.accepted = new ArrayList<AlignmentRow>();
		this.rejected = new ArrayList<AlignmentRow>();
	}

	
	public void process(AlignmentList result) {
		List<AlignmentRow> filteredRows = new ArrayList<AlignmentRow>();
		for (AlignmentRow row : result.getRows()) {
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
	}

	
	public String getLabel() {
		return "alignment result filtering by ground truth overlap";
	}
	
	public List<AlignmentRow> getAccepted() {
		return accepted;
	}

	public List<AlignmentRow> getRejected() {
		return rejected;
	}

}
