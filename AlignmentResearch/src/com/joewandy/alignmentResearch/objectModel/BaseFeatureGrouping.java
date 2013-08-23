package com.joewandy.alignmentResearch.objectModel;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.util.PrettyPrintGroupSize;

public abstract class BaseFeatureGrouping {

	public abstract List<FeatureGroup> group(List<AlignmentFile> dataList);
	
	public void filterGroups(List<FeatureGroup> groups) {

		Map<Integer, Integer> groupSize = new TreeMap<Integer, Integer>();
		Iterator<FeatureGroup> it = groups.iterator();

		int removed = 0;
		while (it.hasNext()) {
			
			FeatureGroup group = it.next();

			// create a map to keep track of how many groups of certain sizes
			int memberCount = group.getFeatureCount();
			if (groupSize.get(memberCount) != null) {
				int newSize = groupSize.get(memberCount) + 1;
				groupSize.put(memberCount, newSize);
			} else {
				groupSize.put(memberCount, 1);
			}

			// remove groups below threshold
			if (group.getFeatureCount() <= FeatureXMLAlignment.GROUP_SIZE_THRESHOLD) {
				group.clearFeatures();
				it.remove();
				removed++;
			}
			
		}
		System.out.println("Group size");		
		System.out.println(new PrettyPrintGroupSize<Integer, Integer>(groupSize));
		System.out.println("Removed " + removed + " groups");
		System.out.println("Remaining " + groups.size() + " groups");
	}

	
}
