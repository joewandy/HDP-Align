package com.joewandy.alignmentResearch.noiseModel;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class ExperimentalConditionNoise implements AlignmentNoise {

	public ExperimentalConditionNoise() {
		System.out.println("ExperimentalConditionNoise");
	}
	
	@Override
	public void addNoise(AlignmentData data) {

		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();

	}

}
