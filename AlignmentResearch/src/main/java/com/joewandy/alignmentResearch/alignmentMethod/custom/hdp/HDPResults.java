package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.joewandy.alignmentResearch.model.HDPMetabolite;
import com.rits.cloning.Cloner;

public class HDPResults implements Serializable {
	
	private static final long serialVersionUID = 398275171032368664L;

	private List<HDPResultsSample> allResults;
	private Map<HDPMassClusterFeatures, Integer> counts;
	
	public HDPResults() {
		this.allResults = new ArrayList<HDPResultsSample>();
		this.counts = new HashMap<HDPMassClusterFeatures, Integer>();
	}

	public void store(List<HDPMetabolite> metabolites) {
		HDPResultsSample resultsSample = new HDPResultsSample(metabolites);
		allResults.add(resultsSample);
	}
	
	public List<HDPResultsSample> getResultsSamples() {
		return allResults;
	}
	
	@JsonIgnore
	public int getResultsSamplesCount() {
		return allResults.size();
	}

	public int getCount(HDPMassClusterFeatures item) {
		Integer featuresCount = counts.get(item);
		if (featuresCount == null) {
			return 0;
		}
		return featuresCount;
	}
		
	public void store(HDPMassClusterFeatures item) {
		Integer currentCount = counts.get(item);
		if (currentCount == null) {
			counts.put(item, 1);
		} else {
			int newCount = currentCount+1;
			counts.put(item, newCount);
		}
	}
	
	public Set<Entry<HDPMassClusterFeatures, Integer>> getEntries() {
		return counts.entrySet();
	}
	
}
