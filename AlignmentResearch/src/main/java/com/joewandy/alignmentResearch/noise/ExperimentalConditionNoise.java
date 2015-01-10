package com.joewandy.alignmentResearch.noise;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.GroundTruth;

public class ExperimentalConditionNoise implements AlignmentNoise {

	public ExperimentalConditionNoise() {
		System.out.println("ExperimentalConditionNoise");
	}
	
	public void addNoise(AlignmentData data) {

		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();

	}

}
