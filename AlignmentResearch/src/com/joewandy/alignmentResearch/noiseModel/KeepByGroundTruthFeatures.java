package com.joewandy.alignmentResearch.noiseModel;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class KeepByGroundTruthFeatures implements AlignmentNoise {

	@Override
	public void addNoise(AlignmentData data) {
		
		System.out.println("Retain only features present in ground truth data ...");
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();
		for (AlignmentFile file : alignmentDataList) {
			file.retainFeatures(gt.getAllUniqueFeatures());
		}

	}

}
