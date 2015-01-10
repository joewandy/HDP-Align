package com.joewandy.alignmentResearch.noise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.GroundTruth;

public class MissingPeaksNoise implements AlignmentNoise {

	// fraction to remove from data (0.0 .. 1.0)
	private double removeFrac;	
	
	public MissingPeaksNoise(double removeFrac) {
		System.out.println("MissingPeaksNoise initialised - removeFrac = " + removeFrac);
		this.removeFrac = removeFrac;
	}
	
	public void addNoise(AlignmentData data) {
		
		System.out.println("--- MissingPeaksNoise ---");
		System.out.println("Removing " + removeFrac + " of all features ...");
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();
		for (AlignmentFile file : alignmentDataList) {

			// shuffle and determine how many features to remove
			// TODO: don't change the original feature list !!!!
			List<Feature> shuffledFeatures = new ArrayList<Feature>(file.getFeatures());
			final int N = (int) (shuffledFeatures.size() * removeFrac);
			Collections.shuffle(shuffledFeatures);
			
			// loop through features and remove the bottom N features
			Iterator<Feature> it = shuffledFeatures.iterator();
			int counter = 0;
			while (it.hasNext() && counter < N) {
				Feature example = it.next();
				example.setDelete(true);
				it.remove();
				gt.clearFeature(example);
				counter++;
			}
			it = file.getFeatures().iterator();
			counter = 0;
			int peakID = 0;
			while (it.hasNext()) {
				Feature example = it.next();
				if (example.isDelete()) {
					it.remove();
					gt.clearFeature(example);
					counter++;					
				} else {
					// renumber peakID. Always start from 0.
//					example.setPeakID(peakID);
//					peakID++;
				}
			}
			// System.out.println("Removed " + counter + " features");
			// System.out.println(file.getFilenameWithoutExtension() + " has " + file.getFeaturesCount() + " features");
			
		}

	}

}
