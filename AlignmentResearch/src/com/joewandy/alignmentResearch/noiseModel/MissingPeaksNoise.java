package com.joewandy.alignmentResearch.noiseModel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class MissingPeaksNoise implements AlignmentNoise {

	// fraction to remove from data (0.0 .. 1.0)
	private double removeFrac;	
	
	public MissingPeaksNoise(double removeFrac) {
		System.out.println("MissingPeaksNoise initialised - removeFrac = " + removeFrac);
		this.removeFrac = removeFrac;
	}
	
	@Override
	public void addNoise(AlignmentData data) {
		
		System.out.println("--- MissingPeaksNoise ---");
		System.out.println("Removing " + removeFrac + " of all features ...");
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();
		for (AlignmentFile file : alignmentDataList) {

			// shuffle and determine how many features to remove
			List<Feature> features = file.getFeatures();
			final int N = (int) (features.size() * removeFrac);
			Collections.shuffle(features);
			
			// loop through features and remove the bottom N features
			Iterator<Feature> it = features.iterator();
			int counter = 0;
			while (it.hasNext() && counter < N) {
				Feature example = it.next();
				it.remove();
				gt.clearFeature(example);
				counter++;
			}
			// System.out.println("Removed " + counter + " features");
			// System.out.println(file.getFilenameWithoutExtension() + " has " + file.getFeaturesCount() + " features");
			
		}

	}

}
