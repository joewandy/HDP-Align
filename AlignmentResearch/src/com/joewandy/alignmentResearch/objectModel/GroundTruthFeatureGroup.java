package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;


public class GroundTruthFeatureGroup extends FeatureGroup {
	
	private Map<String, Feature> filenameToFeatureMap;
	
	public GroundTruthFeatureGroup(int groupId) {
		super(groupId);
		this.filenameToFeatureMap = new HashMap<String, Feature>();
	}
	
	@Override
	public void addFeature(Feature feature) {
		this.filenameToFeatureMap.put(feature.getData().getFilenameWithoutExtension(), feature);
	}
	
	@Override
	public void addFeatures(Set<Feature> features) {
		for (Feature feature : features) {
			this.addFeature(feature);
		}
	}

	@Override
	public Set<Feature> getFeatures() {
		return new HashSet<Feature>(this.filenameToFeatureMap.values());
	}

	
	public Feature getFeature(String filename) {
		return this.filenameToFeatureMap.get(filename);
	}
		
	public boolean hasFeature(String filename) {
		return this.filenameToFeatureMap.containsKey(filename);
	}
	
	public Feature[] asArray(String[] filenames) {
		Feature[] result = new Feature[filenames.length];
		for (int i = 0; i < filenames.length; i++) {
			String filename = filenames[i];
			if (this.hasFeature(filename)) {
				result[i] = this.getFeature(filename);
			}
		}
		return result;
	}		

	@Override
	public int getFeatureCount() {
		return this.filenameToFeatureMap.size();
	}
	
	@Override
	public void clearFeatures() {
		this.filenameToFeatureMap.clear();
	}
	
	public boolean clearFeature(Feature example) {
		String key = null;
		for (Entry<String, Feature> entry : filenameToFeatureMap.entrySet()) {
			if (GroundTruth.compareFeature(entry.getValue(), example)) {
				key = entry.getKey();
				break;
			}
		}
		if (key != null) {
			filenameToFeatureMap.remove(key);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "GroundTruthFeatureGroup [groupId=" + groupId + ", features=" + this.getFeatureCount()
				+ "]";
	}
	
}
