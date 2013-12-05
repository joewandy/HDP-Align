package com.joewandy.alignmentResearch.noiseModel;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class PolynomialLocalRetentionShiftNoise implements AlignmentNoise {

	public enum PolynomialNoiseLevel {

		NONE		(0),
		LOW 		(1),
		MEDIUM		(2),
		HIGH		(3),
		SUPER_HIGH	(4);
		
		private final double upper;
		
		PolynomialNoiseLevel(double upper) {
			this.upper = upper;
		}

		public double getUpper() {
			return this.upper;
		}
		
	}

	private PolynomialNoiseLevel noiseLevel;
	
	/** random noise generator **/
	private final RandomData randomData;	
	
	public PolynomialLocalRetentionShiftNoise(PolynomialNoiseLevel noiseLevel) {
		System.out.println("PolynomialLocalRetentionShiftNoise initialised - noiseLevel=" + noiseLevel);
		this.noiseLevel = noiseLevel;
		this.randomData = new RandomDataImpl();
	}	
	
	public void addNoise(AlignmentData data) {

		System.out.println("--- PolynomialLocalRetentionShiftNoise---");
		System.out.println("Adding polynomial local drift noise " + this.noiseLevel + " to features");
		
		if (this.noiseLevel == PolynomialNoiseLevel.NONE) {
			return;
		}
		
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		for (AlignmentFile file : alignmentDataList) {
//			double coeff1 = randomData.nextUniform(0, noiseLevel.upper);
//			double coeff2 = randomData.nextUniform(0, noiseLevel.upper);
//			double coeff3 = randomData.nextUniform(0, noiseLevel.upper);
//			double coeff4 = randomData.nextUniform(0, noiseLevel.upper);
			double coeff1 = 0;
			double coeff2 = 0;
			double coeff3 = randomData.nextUniform(0, noiseLevel.upper);
			double coeff4 = randomData.nextUniform(0, noiseLevel.upper);
			System.out.println("\tfile=" + file.getFilename() + 
					", coeff1=" + String.format("%.3f", coeff1) + 
					", coeff2=" + String.format("%.3f", coeff2) + 
					", coeff3=" + String.format("%.3f", coeff3) + 
					", coeff4=" + String.format("%.3f", coeff4)
			);
			for (Feature feature : file.getFeatures()) {
				double newRt = noisyRt(feature.getRt(), coeff1, coeff2, coeff3, coeff4);
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
	private double noisyRt(double x, double coeff1, double coeff2, double coeff3, double coeff4) {
		double value = coeff1 * (x*x*x);
		value += coeff2 * (x*x);
		value += coeff3 * (x);
		value += coeff4;
		return value;		
	}
		
	/**
	 * Pick a noise level randomly
	 * @return
	 */
	public static PolynomialNoiseLevel randomNoiseLevel() {
	    int pick = new Random().nextInt(PolynomialNoiseLevel.values().length);
	    return PolynomialNoiseLevel.values()[pick];
	}
	
}
