package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.noiseModel.AlignmentNoise;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;


public abstract class BaseDataGenerator implements AlignmentDataGenerator {

	private List<AlignmentNoise> noiseModels;
	
	public BaseDataGenerator() {
		this.noiseModels = new ArrayList<AlignmentNoise>();
	}
	
	public void addNoise(AlignmentNoise noiseModel) {
		this.noiseModels.add(noiseModel);
	}
	
	public AlignmentData generate() {
		
		//generate alignment data
		List<AlignmentFile> alignmentFiles = this.getAlignmentFiles();
		GroundTruth groundTruth = this.getGroundTruth();
		AlignmentData alignmentData = new AlignmentData(alignmentFiles, groundTruth);
		
		// introduce noise to the generated data
		for (AlignmentNoise noiseModel : noiseModels) {
			noiseModel.addNoise(alignmentData);
		}
		
		return alignmentData;
	}
	
	protected abstract List<AlignmentFile> getAlignmentFiles();
	
	protected abstract GroundTruth getGroundTruth();
	
}
