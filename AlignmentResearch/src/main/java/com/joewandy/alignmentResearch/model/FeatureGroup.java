package com.joewandy.alignmentResearch.model;

import java.util.HashSet;
import java.util.Set;

import peakml.chemistry.PeriodicTable;


public class FeatureGroup {

	protected int groupID;
	protected Set<Feature> members;
	
	public FeatureGroup(int groupId) {
		this.groupID = groupId;
		this.members = new HashSet<Feature>();
	}
	
	public int getGroupID() {
		return groupID;
	}

	public void setGroupID(int groupID) {
		this.groupID = groupID;
	}

	public void addFeature(Feature feature) {
		feature.setGroupID(this.getGroupID());			
		members.add(feature);
	}

	public void addFeatures(Set<Feature> features) {
		for (Feature feature : features) {
			feature.setGroupID(this.getGroupID());
		}
		members.addAll(features);
	}
	
	public Set<Feature> getFeatures() {
		return members;
	}
	
	public int getFeatureCount() {
		return members.size();
	}

	public int getAlignedFeatureCount() {
		int count = 0;
		for (Feature feature : members) {
			if (feature.isAligned()) {
				count++;
			}
		}
		return count;
	}
	
	public boolean clearFeature(Feature example) {
		for (Feature feature : this.members) {
			if (feature.equals(example)) {
				feature.clearGroupID();				
			}
		}
		return this.members.remove(example);
	}
	
	public void clearFeatures() {
		for (Feature feature : this.members) {
			feature.clearGroupID();
		}
		this.members.clear();
	}
		
}
