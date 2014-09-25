package com.joewandy.alignmentResearch.noiseModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class RandomPeakNoise implements AlignmentNoise {

	// fraction to add from data (0.0 .. 1.0)
	private double addFrac;	
	
	/** random noise generator **/
	private final RandomData randomData;	
		
	public RandomPeakNoise(double addFrac) {
		System.out.println("RandomPeaksNoise initialised - addFrac = " + addFrac);
		this.randomData = new RandomDataImpl();
		this.addFrac = addFrac;
	}	
	
	public void addNoise(AlignmentData data) {
		
		System.out.println("--- RandomPeaksNoise ---");
		System.out.println("Adding " + addFrac + " random peaks to data ...");
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		for (AlignmentFile file : alignmentDataList) {

			// get min max ranges
			double minMass = file.getMinMass();
			double maxMass = file.getMaxMass();
			double massRange = maxMass - minMass;
			double massMean = massRange / 2;
			double massStdev = massRange / 6;			

			double minRt = file.getMinRt();
			double maxRt = file.getMaxRt();
			double rtRange = maxRt - minRt;
			double rtMean = rtRange / 2;
			double rtStdev = rtRange / 6;			
			
			// determine how many features to add
			final int N = (int) (file.getFeaturesCount() * addFrac);			
			final int maxID = file.getMaxFeatureID();
			
			// loop through features and duplicate the bottom N features
			List<Feature> contaminants = new ArrayList<Feature>();
			int counter = 0;
			while (counter < N) {
				double newMass = randomData.nextGaussian(massMean, massStdev); 
				double newIntensity = 1000;
				double newRt = randomData.nextGaussian(rtMean, rtStdev); 
				int featureID = maxID + 1;
				Feature contaminant = new Feature(featureID, newMass, newRt, newIntensity);
				contaminants.add(contaminant);
				counter++;
			}

			// add into file
			System.out.println("Added " + contaminants.size() 
					+ " new noisy features into " + file.getFilenameWithoutExtension());
			file.addFeatures(contaminants);
			
		}

	}

}
