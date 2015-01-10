package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.noise.AlignmentNoise;


public interface AlignmentDataGenerator {
	public void addNoise(AlignmentNoise noiseModel);
	
	public AlignmentData generate();
		
	public AlignmentData generateByIndices(int[] indices);

	public AlignmentData generateByIteration(int currentIter);
	
}
