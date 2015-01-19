package com.joewandy.alignmentResearch.noise;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;

public class GlobalRetentionShiftNoise implements AlignmentNoise {

	public enum GlobalNoiseLevel {

		NONE		(0, 0),
		LOW 		(20, 10),
		MEDIUM		(40, 20),
		HIGH		(60, 30);

		private final double rtMean;
		private final double rtStdev;
		
		GlobalNoiseLevel(double mean, double stdev) {
			this.rtMean = mean;
			this.rtStdev = stdev;
		}

		/** mean of Gaussian noise generated **/
		public double getNoiseMean() {
			return this.rtMean;
		}

		/** standard deviation of Gaussian noise generated for retention time **/
		public double getNoiseStdev() {
			return this.rtStdev;
		}
		
	}

	private GlobalNoiseLevel noiseLevel;
	
	/** random noise generator **/
	private final RandomData randomData;	
	
	public GlobalRetentionShiftNoise(GlobalNoiseLevel noiseStdev) {
		System.out.println("GlocalRetentionShiftNoise initialised - noiseStdev=" + noiseStdev);
		this.noiseLevel = noiseStdev;
		this.randomData = new RandomDataImpl();
	}	
	
	public void addNoise(AlignmentData data) {
		
		System.out.println("--- GlobalRetentionShiftNoise ---");
		System.out.println("Adding measurement noise " + this.noiseLevel + " to features");
		System.out.println("\tmean=" + this.noiseLevel.getNoiseMean() + ", stdev=" + this.noiseLevel.getNoiseStdev());
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		for (AlignmentFile file : alignmentDataList) {
			Iterator<Feature> it = file.getFeatures().iterator();
			while (it.hasNext()) {
				Feature feature = it.next();
				double newRt = noisyRt(feature.getRt());
				if (newRt == 0) {
//					// the feature never elutes ... so we remove it from the data file
//					it.remove();
//					// also remove from ground truth
//					data.getGroundTruth().clearFeature(feature);
					newRt = 10;
				} else {
					feature.setRt(newRt);					
				}
			}
		}

	}
	
	/**
	 * Adds random retention noise to value
	 * 
	 * @param value
	 *            The initial value
	 * @return The initial value, plus a random Gaussian noise ~ N(noiseMean,
	 *         noiseStdev). The return value is always truncated to above 0.
	 */
	private double noisyRt(double value) {

		if (this.noiseLevel == GlobalNoiseLevel.NONE) {
			return value;
		}
		
		// then add additive noise as well
		double additiveNoise = randomData.nextGaussian(noiseLevel.getNoiseMean(), 
				noiseLevel.getNoiseStdev());
		value += additiveNoise;
		
		// value must always be >= 0
		if (value < 0) {
			value = 0;
		}
		return value;

	}
	
	/**
	 * Pick a noise level randomly
	 * @return
	 */
	public static GlobalNoiseLevel randomNoiseLevel() {
	    int pick = new Random().nextInt(GlobalNoiseLevel.values().length);
	    return GlobalNoiseLevel.values()[pick];
	}
	
}
