package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpResult;

public class HDPRTClustering implements HDPClustering {

	private List<AlignmentFile> dataList;
	private HDPClusteringParam param;
	
	public HDPRTClustering(List<AlignmentFile> dataList) {
		this.dataList = dataList;
	}

	@Override
	public Map<HdpResult, HdpResult> getSimilarityResult() {
		Map<HdpResult, HdpResult> resultMap = new HashMap<HdpResult, HdpResult>();
		return resultMap;
	}

	@Override
	public void run() {

		
		
	}

}
