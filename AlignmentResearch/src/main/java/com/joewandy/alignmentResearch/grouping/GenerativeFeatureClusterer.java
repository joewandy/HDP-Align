package com.joewandy.alignmentResearch.grouping;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;
import com.joewandy.alignmentResearch.model.ChineseRestaurant;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.GenerativeFeatureGroup;

public class GenerativeFeatureClusterer {

	private ChineseRestaurant crp;
	private double alpha;
	private int groupId;
	
	public GenerativeFeatureClusterer(GenerativeModelParameter params) {
		this.alpha = params.getAlpha();
		this.crp = new ChineseRestaurant();
		this.groupId = 0;
	}
	
	public List<GenerativeFeatureGroup> cluster(List<Feature> features) {
		
//		Map<Integer, GenerativeFeatureGroup> groupMap = new HashMap<Integer, GenerativeFeatureGroup>();
//		int N = features.size();
//		int[] assignments = crp.sample(alpha, N);
//		for (int i = 0; i < assignments.length; i++) {
//			Feature feature = features.get(i);
//			int groupId = assignments[i];
//			if (groupMap.containsKey(groupId)) {
//				// existing group
//				GenerativeFeatureGroup group = groupMap.get(groupId);
//				group.addFeature(feature);
//			} else {
//				// new group
//				GenerativeFeatureGroup group = new GenerativeFeatureGroup(groupId);
//				group.addFeature(feature);
//				groupMap.put(groupId, group);
//			}
//		}
//		
//		List<GenerativeFeatureGroup> groups = new ArrayList<GenerativeFeatureGroup>(groupMap.values());
		
		GenerativeFeatureGroup group = new GenerativeFeatureGroup(groupId);
		group.addFeatures(new HashSet<Feature>(features));
		List<GenerativeFeatureGroup> groups = new ArrayList<GenerativeFeatureGroup>();
		groups.add(group);
		
		groupId++;		
		return groups;
		
	}
			
}
