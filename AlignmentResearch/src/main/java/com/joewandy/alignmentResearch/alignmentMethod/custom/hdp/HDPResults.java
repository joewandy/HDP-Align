package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class HDPResults {
	
	private Map<HDPResultItem, Integer> counts;
	
	public HDPResults() {
		this.counts = new HashMap<HDPResultItem, Integer>();
	}
	
	public int getCount(HDPResultItem item) {
		Integer featuresCount = counts.get(item);
		if (featuresCount == null) {
			return 0;
		}
		return featuresCount;
	}
		
	public void store(HDPResultItem item) {
		Integer currentCount = counts.get(item);
		if (currentCount == null) {
			counts.put(item, 1);
		} else {
			int newCount = currentCount+1;
			counts.put(item, newCount);
		}
	}
	
	public Set<Entry<HDPResultItem, Integer>> getEntries() {
		return counts.entrySet();
	}
	
}
