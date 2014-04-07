package com.joewandy.alignmentResearch.noiseModel;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class MeasurementNoise implements AlignmentNoise {

	private static final double NOISE_MEAN = 0.0f;
	public enum MeasurementNoiseLevel {

		NONE		(0),
		LOW 		(0.005),
		MEDIUM		(0.010),
		HIGH		(0.020),
		SUPER_HIGH	(1.0);
		
		private final double massStdev;
		private final double intensityStdev;
		private final double rtStdev;
		
		MeasurementNoiseLevel(double stdev) {
			this.massStdev = stdev;
			this.intensityStdev = stdev;
			this.rtStdev = stdev;
		}

		/** mean of Gaussian noise generated **/
		public double getNoiseMean() {
			return NOISE_MEAN;
		}

		/** standard deviation of Gaussian noise generated for mass **/
		public double getMassStdev() {
			return this.massStdev;
		}
		
		/** standard deviation of Gaussian noise generated for intensity **/
		public double getIntensityStdev() {
			return this.intensityStdev;
		}

		/** standard deviation of Gaussian noise generated for retention time **/
		public double getRtStdev() {
			return this.rtStdev;
		}
		
	}

	private MeasurementNoiseLevel noiseLevel;
	
	/** random noise generator **/
	private final RandomData randomData;	
	
	public MeasurementNoise(MeasurementNoiseLevel noiseStdev) {
		System.out.println("MeasurementNoise initialised");
		this.noiseLevel = noiseStdev;
		this.randomData = new RandomDataImpl();
	}	
	
	public void addNoise(AlignmentData data) {

		System.out.println("--- MeasurementNoise ---");
		System.out.println("Adding measurement noise " + this.noiseLevel + " to features");
		System.out.println("\tmass - mean=" + this.noiseLevel.getNoiseMean() + ", stdev=" + this.noiseLevel.getMassStdev());
		System.out.println("\tintensity - mean=" + this.noiseLevel.getNoiseMean() + ", stdev=" + this.noiseLevel.getIntensityStdev());
		System.out.println("\tRT - mean=" + this.noiseLevel.getNoiseMean() + ", stdev=" + this.noiseLevel.getRtStdev());
		
		if (this.noiseLevel == MeasurementNoiseLevel.NONE) {
			return;
		}
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		for (AlignmentFile file : alignmentDataList) {
			for (Feature feature : file.getFeatures()) {
				double newMass = noisyMass(feature.getMass());
				double newIntensity = noisyIntensity(feature.getIntensity());
				double newRt = noisyRt(feature.getRt());
				feature.setMass(newMass);
				feature.setIntensity(newIntensity);
				feature.setRt(newRt);
			}
		}

	}
	
	/**
	 * Adds random mass noise to value
	 * 
	 * @param value
	 *            The initial value
	 * @return The initial value, plus a random Gaussian noise ~ N(noiseMean,
	 *         noiseStdev). The return value is always truncated to above 0.
	 */
	private double noisyMass(double value) {

		// then add additive noise as well
		double additiveNoise = randomData.nextGaussian(noiseLevel.getNoiseMean(), 
				noiseLevel.getMassStdev());
		value += additiveNoise;
		
		// value must always be >= 0
		if (value < 0) {
			value = 0;
		}
		return value;

	}

	/**
	 * Adds random intensity noise to value
	 * 
	 * @param value
	 *            The initial value
	 * @return The initial value, plus a random Gaussian noise ~ N(noiseMean,
	 *         noiseStdev). The return value is always truncated to above 0.
	 */
	private double noisyIntensity(double value) {

		// add multiplicative noise: log the value, add noise then take the exp
		double logValue = Math.log(value);
		double multiplicativeNoise = randomData.nextGaussian(noiseLevel.getNoiseMean(), 
				noiseLevel.getIntensityStdev());
		logValue += multiplicativeNoise;
		value = Math.exp(logValue);

		// then add additive noise as well
		double additiveNoise = randomData.nextGaussian(noiseLevel.getNoiseMean(), 
				noiseLevel.getIntensityStdev());
		value += additiveNoise;
		
		// value must always be >= 0
		if (value < 0) {
			value = 0;
		}
		return value;

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
	public static MeasurementNoiseLevel randomNoiseLevel() {
	    int pick = new Random().nextInt(MeasurementNoiseLevel.values().length);
	    return MeasurementNoiseLevel.values()[pick];
	}
	
}
