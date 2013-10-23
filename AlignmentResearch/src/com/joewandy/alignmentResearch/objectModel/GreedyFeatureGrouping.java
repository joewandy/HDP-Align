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

			List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
			
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
					fileGroups.add(group);
				}
				
				if (groupId % 1000 == 0) {
					System.out.print(".");
				}
				
			}				
			System.out.println();
			System.out.println("fileGroups.size() = " + fileGroups.size());

			groups.addAll(fileGroups);
			
			// create assignment matrix
			double[][] Z = new double[data.getFeaturesCount()][fileGroups.size()];
			for (int j = 0; j < fileGroups.size(); j++) {
				FeatureGroup group = fileGroups.get(j);
				for (Feature f : group.getFeatures()) {
					int i = f.getPeakID(); // starts from 0
					Z[i][j] = 1;
				}
			}
			data.setZ(Z);
			
			int groupedCount = 0;
			for (Feature feature : data.getFeatures()) {
				if (feature.isGrouped()) {
					groupedCount++;
				}
			}			
			System.out.println("groupedCount = " + groupedCount);
			
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
