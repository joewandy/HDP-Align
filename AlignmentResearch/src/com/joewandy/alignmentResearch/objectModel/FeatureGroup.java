package com.joewandy.alignmentResearch.objectModel;

import java.util.HashSet;
import java.util.Set;


public class FeatureGroup {

	protected int groupId;
	protected Set<Feature> members;
	private boolean deleted;
	
	public FeatureGroup(int groupId) {
		this.groupId = groupId;
		this.members = new HashSet<Feature>();
		this.deleted = false;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public void addFeature(Feature feature) {
		feature.addGroup(this);			
		members.add(feature);
	}

	public void addFeatures(Set<Feature> features) {
		for (Feature feature : features) {
			feature.addGroup(this);
		}
		members.addAll(features);
	}
	
	public Set<Feature> getFeatures() {
		return members;
	}
	
	public int getFeatureCount() {
		return members.size();
	}

	public void clearFeatures() {
		for (Feature feature : this.members) {
			feature.clearGroup(this.groupId);
		}
		this.members.clear();
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	@Override
	public String toString() {
		return "FeatureGroup [groupId=" + groupId + ", features=" + this.getFeatureIDs()
				+ "]";
	}
	
	private String getFeatureIDs() {
		String output = "";
		for (Feature f : this.getFeatures()) {
			output += f.getPeakID() + ", ";
		}
		return output;
	}
	
}
