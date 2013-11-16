package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;

public class AlignmentResult {

	private int alignmentId;
	private List<RelatedPeaksCluster> relatedPeaksClusters;
	
	public AlignmentResult(int alignmentId) {
		this.alignmentId = alignmentId;
		this.relatedPeaksClusters = new ArrayList<RelatedPeaksCluster>();
	}
	
	public int getAlignmentId() {
		return alignmentId;
	}

	public List<RelatedPeaksCluster> getRelatedPeaksClusters() {
		return relatedPeaksClusters;
	}

	public boolean addMember(RelatedPeaksCluster rpc) {
		return addMember(rpc, 1);
	}
	
	public boolean addMember(RelatedPeaksCluster rpc, double prob) {
		
		boolean result = false;
		
		// TODO: double comparisons 
		if (prob >= 0.5) {
			relatedPeaksClusters.add(rpc);
			result = true;
		}
		
		return result;
		
	}

	@Override
	public String toString() {
		String s = "AlignmentCluster [alignmentId=" + alignmentId + "]\n";
		for (RelatedPeaksCluster rpc : relatedPeaksClusters) {
			s += "\t" + rpc + "\n";
		}
		return s;
	}

}
