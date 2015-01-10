package com.joewandy.alignmentResearch.noise;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.GroundTruth;
import com.joewandy.alignmentResearch.noise.MeasurementNoise.MeasurementNoiseLevel;

public class ContaminantPeaksNoise implements AlignmentNoise {

	// fraction to add from data (0.0 .. 1.0)
	private double addFrac;	
	
	private static final double NOISE_MEAN = 0.0f;
	public enum MassIntensityNoiseLevel {

		LOW 		(0.005),
		MEDIUM		(0.010),
		HIGH		(0.050);
		
		private final double massStdev;
		private final double intensityStdev;
		private final double rtStdev;
		
		MassIntensityNoiseLevel(double stdev) {
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
	
	public enum RtNoiseLevel {

		LOW 		(30),
		MEDIUM		(60),
		HIGH		(90);
		
		private final double rtStdev;
		
		RtNoiseLevel(double stdev) {
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

	private MassIntensityNoiseLevel massIntensityNoiseLevel;
	private RtNoiseLevel rtNoiseLevel;
	
	/** random noise generator **/
	private final RandomData randomData;	
		
	public ContaminantPeaksNoise(double addFrac) {
		System.out.println("ContaminantPeaksNoise initialised - addFrac = " + addFrac);
		this.randomData = new RandomDataImpl();
		this.addFrac = addFrac;
	}	
	
	public void addNoise(AlignmentData data) {
		
		System.out.println("--- ContaminantPeaksNoise ---");
		System.out.println("Adding " + addFrac + " contaminant peaks to all features ...");
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();
		for (AlignmentFile file : alignmentDataList) {
			
			// shuffle and determine how many features to remove
			List<Feature> shuffledFeatures = new ArrayList<Feature>(file.getFeatures());
			final int N = (int) (shuffledFeatures.size() * addFrac);
			Collections.shuffle(shuffledFeatures);
			
			final int maxID = file.getMaxFeatureID();
			
			// loop through features and duplicate the bottom N features
			List<Feature> contaminants = new ArrayList<Feature>();
			Iterator<Feature> it = shuffledFeatures.iterator();
			int counter = 0;
			while (it.hasNext() && counter < N) {
				Feature feature = it.next();
				this.massIntensityNoiseLevel = ContaminantPeaksNoise.randomMassIntensityNoiseLevel();
				this.rtNoiseLevel = ContaminantPeaksNoise.randomRtNoiseLevel();
				double newMass = noisyMass(feature.getMass());
				double newIntensity = noisyIntensity(feature.getIntensity());
				double newRt = noisyRt(feature.getRt());
				int featureID = maxID + 1;
				Feature contaminant = new Feature(featureID, newMass, newRt, newIntensity);
				contaminants.add(contaminant);
				counter++;
			}

			// add into file
			System.out.println("Added " + contaminants.size() 
					+ " new contaminant features into " + file.getFilenameWithoutExtension());
			file.addFeatures(contaminants);
			
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
		double additiveNoise = randomData.nextGaussian(massIntensityNoiseLevel.getNoiseMean(), 
				massIntensityNoiseLevel.getMassStdev());
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
		double multiplicativeNoise = randomData.nextGaussian(massIntensityNoiseLevel.getNoiseMean(), 
				massIntensityNoiseLevel.getIntensityStdev());
		logValue += multiplicativeNoise;
		value = Math.exp(logValue);

		// then add additive noise as well
		double additiveNoise = randomData.nextGaussian(massIntensityNoiseLevel.getNoiseMean(), 
				massIntensityNoiseLevel.getIntensityStdev());
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
		double additiveNoise = randomData.nextGaussian(rtNoiseLevel.getNoiseMean(), 
				rtNoiseLevel.getRtStdev());
		value += additiveNoise;
		
		// value must always be >= 0
		if (value < 0) {
			value = 0;
		}
		return value;

	}	
	
	/**
	 * Pick a noise level randomly for mass and intensity
	 * @return
	 */
	public static MassIntensityNoiseLevel randomMassIntensityNoiseLevel() {
	    int pick = new Random().nextInt(MassIntensityNoiseLevel.values().length);
	    return MassIntensityNoiseLevel.values()[pick];
	}

	/**
	 * Pick a noise level randomly for retention time
	 * @return
	 */
	public static RtNoiseLevel randomRtNoiseLevel() {
	    int pick = new Random().nextInt(RtNoiseLevel.values().length);
	    return RtNoiseLevel.values()[pick];
	}

}
