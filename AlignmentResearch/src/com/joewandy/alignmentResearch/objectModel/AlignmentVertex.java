package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import peakml.IPeak;


public class AlignmentVertex {

	private int sourcePeakSet;
	private int groupId;
	private List<IPeak> peaks;
	private Set<Feature> features;

	public static String formatId(int sourcePeakSet, int groupId) {
		return sourcePeakSet + ":" + groupId;
	}
	
	public AlignmentVertex(int sourcePeakSet, int groupId) {
		this.sourcePeakSet = sourcePeakSet;
		this.groupId = groupId;
		this.peaks = new ArrayList<IPeak>();
		this.features = new HashSet<Feature>();
	}

	public String getId() {
		return AlignmentVertex.formatId(sourcePeakSet, groupId);
	}
	
	public int getGroupId() {
		return groupId;
	}

	public int getSourcePeakSet() {
		return sourcePeakSet;
	}
	
	public List<IPeak> getPeaks() {
		return peaks;
	}

	public void setPeaks(List<IPeak> peaks) {
		this.peaks = peaks;
	}
	
	public Set<Feature> getFeatures() {
		return features;
	}

	public void setFeatures(Set<Feature> features) {
		this.features = features;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupId;
		result = prime * result + sourcePeakSet;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlignmentVertex other = (AlignmentVertex) obj;
		if (groupId != other.groupId)
			return false;
		if (sourcePeakSet != other.sourcePeakSet)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "V[" + this.getId() + "]";
	}
	
}
