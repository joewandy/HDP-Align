package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.util.PrettyPrintGroupSize;

public class GreedyFeatureGrouping extends BaseFeatureGrouping implements FeatureGrouping {

	private double rtTolerance;
	
	/**
	 * Creates a simple grouper
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public GreedyFeatureGrouping(double rtTolerance) {
		
		this.rtTolerance = rtTolerance;
				
	}
	
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {

		System.out.println("============ Grouping = " + dataList.size() + " files ============");
				
		// the group ids must be unique across all input files 
		int groupId = 1;
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		for (AlignmentFile data : dataList) {

			System.out.print("Grouping " + data.getFilename() + " ");
			for (Feature feature : data.getFeatures()) {

				// process ungrouped features
				if (!feature.isGrouped()) {
					FeatureGroup group = new FeatureGroup(groupId);
					Set<Feature> nearbyFeatures = findNearbyFeatures(data, feature);
					if (!nearbyFeatures.isEmpty()) {
						group.addFeatures(nearbyFeatures);
					} 
					groupId++;
					groups.add(group);
				}
				
				if (groupId % 1000 == 0) {
					System.out.print(".");
				}
				
			}				
			System.out.println();
			
			int groupedCount = 0;
			int ungroupedCount = 0;
			for (Feature feature : data.getFeatures()) {
				// System.out.println(feature.getPeakID() + "\t" + feature.getGroup().getGroupId());
				if (feature.isGrouped()) {
					groupedCount++;
				} else {
					ungroupedCount++;
				}
			}			
			System.out.println("groupedCount = " + groupedCount);
//			System.out.println("ungroupedCount = " + ungroupedCount);

		}
		
		return groups;
				
	}
		
	private Set<Feature> findNearbyFeatures(AlignmentFile data, Feature referenceFeature) {
		Set<Feature> nearbyFeatures = new HashSet<Feature>();
		// find matching feature
		Set<Feature> unmatched = data.getNextUngroupedFeatures(referenceFeature, this.rtTolerance);
		nearbyFeatures.addAll(unmatched);
		return nearbyFeatures;
	}
	
}
