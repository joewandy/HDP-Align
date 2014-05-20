package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;

public class GenerativeFeatureClusterer {

	private ChineseRestaurant crp;
	private double alpha;
	
	public GenerativeFeatureClusterer(GenerativeModelParameter params) {
		this.alpha = params.getAlpha();
		this.crp = new ChineseRestaurant();
	}
	
	public List<GenerativeFeatureGroup> cluster(List<Feature> features) {
		
		Map<Integer, GenerativeFeatureGroup> groupMap = new HashMap<Integer, GenerativeFeatureGroup>();
		int N = features.size();
		int[] assignments = crp.sample(alpha, N);
		for (int i = 0; i < assignments.length; i++) {
			Feature feature = features.get(i);
			int groupId = assignments[i];
			if (groupMap.containsKey(groupId)) {
				// existing group
				GenerativeFeatureGroup group = groupMap.get(groupId);
				group.addFeature(feature);
			} else {
				// new group
				GenerativeFeatureGroup group = new GenerativeFeatureGroup(groupId);
				group.addFeature(feature);
				groupMap.put(groupId, group);
			}
		}
		
		List<GenerativeFeatureGroup> groups = new ArrayList<GenerativeFeatureGroup>(groupMap.values());
		return groups;
		
	}
			
}
