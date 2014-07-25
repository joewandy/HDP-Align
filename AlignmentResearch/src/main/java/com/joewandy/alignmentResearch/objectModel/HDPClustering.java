package com.joewandy.alignmentResearch.objectModel;

import java.util.Map;

import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpResult;

public interface HDPClustering {

	Map<HdpResult, HdpResult> getSimilarityResult();

	void run();

}
