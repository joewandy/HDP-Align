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

public class HDPAllSamples implements Serializable {
	
	private static final long serialVersionUID = 398275171032368664L;

	private List<HDPSingleSample> samplingResults;
	
	public HDPAllSamples() {
		this.samplingResults = new ArrayList<HDPSingleSample>();
	}

	public void store(List<HDPMetabolite> metabolites) {
		HDPSingleSample resultsSample = new HDPSingleSample(metabolites);
		samplingResults.add(resultsSample);
	}
	
	public List<HDPSingleSample> getSamplingResults() {
		return samplingResults;
	}

	public void setSamplingResults(List<HDPSingleSample> samplingResults) {
		this.samplingResults = samplingResults;
	}
	
}
