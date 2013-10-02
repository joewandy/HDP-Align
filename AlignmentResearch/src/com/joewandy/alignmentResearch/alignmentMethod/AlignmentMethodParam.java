package com.joewandy.alignmentResearch.alignmentMethod;

import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;

public class AlignmentMethodParam {

	// show setup dialog or not during MZMine alignment ?
	public static final boolean SHOW_PARAM_SETUP_DIALOG = true;
	
	/** 
	 * This value sets the range, in terms of retention time, to create the model 
	 * using RANSAC and non-linear regression algorithm. 
	 * Maximum allowed retention time difference
	 */
	public static final int PARAM_RT_TOLERANCE_BEFORE_CORRECTION = 1000;

	/**
	 * Maximum number of iterations allowed in the algorithm to find the right model 
	 * consistent in all the pairs of aligned peaks. When its value is 0, the number 
	 * of iterations (k) will be estimate automatically.
	 */
	public static final int PARAM_RANSAC_ITERATION = 50000;
//	public static final int PARAM_RANSAC_ITERATION = 0;

	/**
	 * % of points required to consider the model valid (d).
	 */
	public static final double PARAM_MINIMUM_NO_OF_POINTS = 0.10;

	/**
	 * Threshold value (minutes) for determining when a data 
	 * point fits a model (t)
	 */
	public static final int PARAM_THRESHOLD_VALUE = 15;
	
	/**
	 * Switch between polynomial model or lineal model
	 */
	public static final boolean PARAM_LINEAR_MODEL = false;
	
	/**
	 * If checked, only rows having same charge state can be aligned
	 */
	public static final boolean PARAM_REQUIRE_SAME_CHARGE_STATE = false;	
	
	/**
	 * Threshold for picking 'friends' for social stable matching
	 */
	public static final double PARAM_FRIENDLY_THRESHOLD = 1;
	
	private double massTolerance;
	private double rtTolerance;
	private boolean usePpm;
	private int rtWindowMultiply;
	
	// for ransac alignment
	private double ransacRtToleranceBeforeMinute;
	private double ransacRtToleranceAfterMinute;
	private int ransacIteration;
	private double ransacNMinPoints;
	private double ransacThreshold;
	private boolean ransacLinearModel;
	private boolean ransacSameChargeRequired;
	
	// for grouping alignment
	private double friendlyThreshold;

	// variant of Builder pattern, as described in Effective Java 2nd Ed.
	public static class Builder {

		// required parameters
		private double massTolerance;
		private double rtTolerance;

		// optional parameters
		private boolean usePpm;
		private int rtWindowMultiply;

		// for ransac alignment
		private double ransacRtToleranceBeforeMinute;
		private double ransacRtToleranceAfterMinute;
		private int ransacIteration;
		private double ransacNMinPoints;
		private double ransacThreshold;
		private boolean ransacLinearModel;
		private boolean ransacSameChargeRequired;

		// for grouping alignment
		private double friendlyThreshold;
		
		public Builder(double massTolerance, double rtTolerance) {

			this.massTolerance = massTolerance;
			this.rtTolerance = rtTolerance;
		
			// set whole loads of default value
			
			this.usePpm = FeatureXMLAlignment.ALIGN_BY_RELATIVE_MASS_TOLERANCE;
			this.rtWindowMultiply = FeatureXMLAlignment.RTWINDOW_MULTIPLY;

			// set whole loads of default value for ransac
			this.ransacRtToleranceBeforeMinute = AlignmentMethodParam.PARAM_RT_TOLERANCE_BEFORE_CORRECTION / 60.0;
			this.ransacRtToleranceAfterMinute = this.rtTolerance / 60.0;
			this.ransacIteration = AlignmentMethodParam.PARAM_RANSAC_ITERATION;
			this.ransacNMinPoints = AlignmentMethodParam.PARAM_MINIMUM_NO_OF_POINTS / 100.0;
			this.ransacThreshold = AlignmentMethodParam.PARAM_THRESHOLD_VALUE;
			this.ransacLinearModel = AlignmentMethodParam.PARAM_LINEAR_MODEL;
			this.ransacSameChargeRequired = AlignmentMethodParam.PARAM_REQUIRE_SAME_CHARGE_STATE;
			
			// set parameter for other alignment methods
			this.friendlyThreshold = AlignmentMethodParam.PARAM_FRIENDLY_THRESHOLD;
			
		}

		public Builder usePpm(boolean usePpm) {
			this.usePpm = usePpm;
			return this;
		}
		
		public Builder rtWindowMultiply(int rtWindowMultiply) {
			this.rtWindowMultiply = rtWindowMultiply;
			return this;
		}

		public Builder ransacRtToleranceBefore(double rtToleranceBefore) {
			this.ransacRtToleranceBeforeMinute = rtToleranceBefore / 60.0;
			return this;
		}

		public Builder ransacRtToleranceAfter(double rtToleranceAfter) {
			this.ransacRtToleranceAfterMinute = rtToleranceAfter / 60.0;
			return this;
		}
		
		public Builder ransacIteration(int ransacIteration) {
			this.ransacIteration = ransacIteration;
			return this;
		}

		public Builder ransacNMinPoints(double nMinPoints) {
			this.ransacNMinPoints = nMinPoints / 100.0;
			return this;
		}

		public Builder ransacThreshold(double threshold) {
			this.ransacThreshold = threshold;
			return this;
		}

		public Builder ransacLinearModel(boolean linearModel) {
			this.ransacLinearModel = linearModel;
			return this;
		}

		public Builder ransacSameChargeRequired(boolean sameChargeRequired) {
			this.ransacSameChargeRequired = sameChargeRequired;
			return this;
		}
		
		public Builder friendlyThreshold(double threshold) {
			this.friendlyThreshold = threshold;
			return this;
		}		

		public AlignmentMethodParam build() {
			return new AlignmentMethodParam(this);
		}

	}
	
	public AlignmentMethodParam(Builder builder) {
		this.massTolerance = builder.massTolerance;
		this.rtTolerance = builder.rtTolerance;
		this.usePpm = builder.usePpm;
		this.rtWindowMultiply = builder.rtWindowMultiply;
		this.ransacRtToleranceBeforeMinute = builder.ransacRtToleranceBeforeMinute;
		this.ransacRtToleranceAfterMinute = builder.ransacRtToleranceAfterMinute;
		this.ransacIteration = builder.ransacIteration;
		this.ransacNMinPoints = builder.ransacNMinPoints;
		this.ransacThreshold = builder.ransacThreshold;
		this.ransacLinearModel = builder.ransacLinearModel;
		this.ransacSameChargeRequired = builder.ransacSameChargeRequired;
		this.friendlyThreshold = builder.friendlyThreshold;
	}

	public double getMassTolerance() {
		return massTolerance;
	}

	public double getRtTolerance() {
		return rtTolerance;
	}

	public boolean isUsePpm() {
		return usePpm;
	}

	public int getRtWindowMultiply() {
		return rtWindowMultiply;
	}

	public double getRansacRtToleranceBeforeMinute() {
		return ransacRtToleranceBeforeMinute;
	}

	public double getRansacRtToleranceAfterMinute() {
		return ransacRtToleranceAfterMinute;
	}

	public int getRansacIteration() {
		return ransacIteration;
	}

	public double getRansacNMinPoints() {
		return ransacNMinPoints;
	}

	public double getRansacThreshold() {
		return ransacThreshold;
	}

	public boolean isRansacLinearModel() {
		return ransacLinearModel;
	}

	public boolean isRansacSameChargeRequired() {
		return ransacSameChargeRequired;
	}

	public double getFriendlyThreshold() {
		return friendlyThreshold;
	}
	
}
