package com.joewandy.alignmentResearch.noise;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;

public class LocalRetentionShiftNoise implements AlignmentNoise {

	private static final double NOISE_MEAN = 0.0f;
	public enum LocalNoiseLevel {

		NONE		(0),
		LOW 		(10),
		MEDIUM		(20),
		HIGH		(50),
		SUPER_HIGH	(100);
		
		private final double rtStdev;
		
		LocalNoiseLevel(double stdev) {
			this.rtStdev = stdev;
		}

		/** mean of Gaussian noise generated **/
		public double getNoiseMean() {
			return NOISE_MEAN;
		}

		/** standard deviation of Gaussian noise generated for retention time **/
		public double getRtStdev() {
			return this.rtStdev;
		}
		
	}

	private LocalNoiseLevel noiseLevel;
	
	/** random noise generator **/
	private final RandomData randomData;	
	
	public LocalRetentionShiftNoise(LocalNoiseLevel noiseStdev) {
		System.out.println("GlocalRetentionShiftNoise initialised - noiseStdev=" + noiseStdev);
		this.noiseLevel = noiseStdev;
		this.randomData = new RandomDataImpl();
	}	
	
	public void addNoise(AlignmentData data) {

		System.out.println("--- LocalRetentionShiftNoise---");
		System.out.println("Adding local drift noise " + this.noiseLevel + " to features");
		System.out.println("\tRT - mean=" + this.noiseLevel.getNoiseMean() + ", stdev=" + this.noiseLevel.getRtStdev());
		
		if (this.noiseLevel == LocalNoiseLevel.NONE) {
			return;
		}
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		for (AlignmentFile file : alignmentDataList) {
			for (Feature feature : file.getFeatures()) {
				double newRt = noisyRt(feature.getRt());
				feature.setRt(newRt);
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

		// then add additive noise as well
		double additiveNoise = randomData.nextGaussian(noiseLevel.getNoiseMean(), 
				noiseLevel.getRtStdev());
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
	public static LocalNoiseLevel randomNoiseLevel() {
	    int pick = new Random().nextInt(LocalNoiseLevel.values().length);
	    return LocalNoiseLevel.values()[pick];
	}
	
}
