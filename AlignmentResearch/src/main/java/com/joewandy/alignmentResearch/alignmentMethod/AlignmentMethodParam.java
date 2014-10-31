package com.joewandy.alignmentResearch.alignmentMethod;

import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;

public class AlignmentMethodParam {
		
	private boolean usePpm;
	private double massTolerance;
	private double rtTolerance;
	private boolean useGroup;
	private boolean exactMatch;
	private boolean usePeakShape;

	// for ransac alignment
	private double ransacRtToleranceBeforeMinute;
	private double ransacRtToleranceAfterMinute;
	private int ransacIteration;
	private double ransacNMinPoints;
	private double ransacThreshold;
	private boolean ransacLinearModel;
	private boolean ransacSameChargeRequired;
	
	// for openms
	private double openMsMzPairMaxDistance;	
	
	// for clustering methods
	private int groupingNSamples;
	private int groupingBurnIn;	
	private String scoringMethod;	
	
	// for MWG, MWM methods
	private double alpha;	// the score-combining ratio
	private String groupingMethod;
	private double groupingRtTolerance;
	private double groupingDpAlpha;

	// for HDP alignment
	private double hdpAlphaRt;
	private double hdpAlphaMass;
	private double hdpTopAlpha;
	private double hdpGlobalRtClusterStdev;
	private double hdpLocalRtClusterStdev;
	private double hdpMassTol;
	private boolean hdpSpeedUp;
	private int hdpSpeedUpNumSample;
	private int hdpRefFileIdx;
	
	private String identificationDatabase;
	private String groundTruthDatabase;
	private String mode;

	public AlignmentMethodParam() {
	
	}
	
	public AlignmentMethodParam(MultiAlignCmdOptions options) {
		this.usePpm = options.usePpm;
		this.massTolerance = options.alignmentPpm;
		this.rtTolerance = options.alignmentRtWindow;
		this.useGroup = options.useGroup;
		this.exactMatch = options.exactMatch;
		this.usePeakShape = options.usePeakShape;
		this.ransacRtToleranceBeforeMinute = options.ransacRtToleranceBeforeCorrection;
		this.ransacRtToleranceAfterMinute = options.alignmentRtWindow;
		this.ransacIteration = options.ransacIteration;
		this.ransacNMinPoints = options.ransacNMinPoints;
		this.ransacThreshold = options.ransacThreshold;
		this.ransacLinearModel = options.ransacLinearModel;
		this.ransacSameChargeRequired = options.ransacSameChargeRequired;
		this.openMsMzPairMaxDistance = options.openMsMzPairMaxDistance;
		this.groupingNSamples = options.groupingNSamples;
		this.groupingBurnIn = options.groupingBurnIn;
		this.alpha = options.alpha;
		this.groupingMethod = options.groupingMethod;
		this.groupingRtTolerance = options.groupingRtWindow;
		this.groupingDpAlpha = options.groupingDpAlpha;	
		this.hdpAlphaRt = options.hdpAlphaRt;
		this.hdpAlphaMass = options.hdpAlphaMass;
		this.hdpTopAlpha = options.hdpTopAlpha;
		this.hdpGlobalRtClusterStdev = options.hdpGlobalRtClusterStdev;
		this.hdpLocalRtClusterStdev = options.hdpLocalRtClusterStdev;
		this.hdpMassTol = options.hdpMassTol;
		this.hdpSpeedUp = options.hdpSpeedUp;
		this.hdpSpeedUpNumSample = options.hdpSpeedUpNumSample;
		this.hdpRefFileIdx = options.hdpRefFileIdx;
		this.scoringMethod = options.scoringMethod;
		this.identificationDatabase = options.idDatabase;
		this.groundTruthDatabase = options.gtDatabase;
		this.mode = options.mode;
	}

	public boolean isUsePpm() {
		return usePpm;
	}

	public void setUsePpm(boolean usePpm) {
		this.usePpm = usePpm;
	}

	public double getMassTolerance() {
		return massTolerance;
	}

	public void setMassTolerance(double massTolerance) {
		this.massTolerance = massTolerance;
	}

	public double getRtTolerance() {
		return rtTolerance;
	}

	public void setRtTolerance(double rtTolerance) {
		this.rtTolerance = rtTolerance;
	}

	public boolean isUseGroup() {
		return useGroup;
	}

	public void setUseGroup(boolean useGroup) {
		this.useGroup = useGroup;
	}

	public boolean isExactMatch() {
		return exactMatch;
	}

	public void setExactMatch(boolean exactMatch) {
		this.exactMatch = exactMatch;
	}

	public boolean isUsePeakShape() {
		return usePeakShape;
	}

	public void setUsePeakShape(boolean usePeakShape) {
		this.usePeakShape = usePeakShape;
	}

	public double getRansacRtToleranceBeforeMinute() {
		return ransacRtToleranceBeforeMinute;
	}

	public void setRansacRtToleranceBeforeMinute(
			double ransacRtToleranceBeforeMinute) {
		this.ransacRtToleranceBeforeMinute = ransacRtToleranceBeforeMinute;
	}

	public double getRansacRtToleranceAfterMinute() {
		return ransacRtToleranceAfterMinute;
	}

	public void setRansacRtToleranceAfterMinute(double ransacRtToleranceAfterMinute) {
		this.ransacRtToleranceAfterMinute = ransacRtToleranceAfterMinute;
	}

	public int getRansacIteration() {
		return ransacIteration;
	}

	public void setRansacIteration(int ransacIteration) {
		this.ransacIteration = ransacIteration;
	}

	public double getRansacNMinPoints() {
		return ransacNMinPoints;
	}

	public void setRansacNMinPoints(double ransacNMinPoints) {
		this.ransacNMinPoints = ransacNMinPoints;
	}

	public double getRansacThreshold() {
		return ransacThreshold;
	}

	public void setRansacThreshold(double ransacThreshold) {
		this.ransacThreshold = ransacThreshold;
	}

	public boolean isRansacLinearModel() {
		return ransacLinearModel;
	}

	public void setRansacLinearModel(boolean ransacLinearModel) {
		this.ransacLinearModel = ransacLinearModel;
	}

	public boolean isRansacSameChargeRequired() {
		return ransacSameChargeRequired;
	}

	public void setRansacSameChargeRequired(boolean ransacSameChargeRequired) {
		this.ransacSameChargeRequired = ransacSameChargeRequired;
	}

	public double getOpenMsMzPairMaxDistance() {
		return openMsMzPairMaxDistance;
	}

	public void setOpenMsMzPairMaxDistance(double openMsMzPairMaxDistance) {
		this.openMsMzPairMaxDistance = openMsMzPairMaxDistance;
	}

	public int getGroupingNSamples() {
		return groupingNSamples;
	}

	public void setGroupingNSamples(int groupingNSamples) {
		this.groupingNSamples = groupingNSamples;
	}

	public int getGroupingBurnIn() {
		return groupingBurnIn;
	}

	public void setGroupingBurnIn(int groupingBurnIn) {
		this.groupingBurnIn = groupingBurnIn;
	}

	public String getScoringMethod() {
		return scoringMethod;
	}

	public void setScoringMethod(String scoringMethod) {
		this.scoringMethod = scoringMethod;
	}

	public double getAlpha() {
		return alpha;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}

	public String getGroupingMethod() {
		return groupingMethod;
	}

	public void setGroupingMethod(String groupingMethod) {
		this.groupingMethod = groupingMethod;
	}

	public double getGroupingRtTolerance() {
		return groupingRtTolerance;
	}

	public void setGroupingRtTolerance(double groupingRtTolerance) {
		this.groupingRtTolerance = groupingRtTolerance;
	}

	public double getGroupingDpAlpha() {
		return groupingDpAlpha;
	}

	public void setGroupingDpAlpha(double groupingDpAlpha) {
		this.groupingDpAlpha = groupingDpAlpha;
	}

	public double getHdpAlphaRt() {
		return hdpAlphaRt;
	}

	public void setHdpAlphaRt(double hdpAlphaRt) {
		this.hdpAlphaRt = hdpAlphaRt;
	}

	public double getHdpAlphaMass() {
		return hdpAlphaMass;
	}

	public void setHdpAlphaMass(double hdpAlphaMass) {
		this.hdpAlphaMass = hdpAlphaMass;
	}

	public double getHdpTopAlpha() {
		return hdpTopAlpha;
	}

	public void setHdpTopAlpha(double hdpTopAlpha) {
		this.hdpTopAlpha = hdpTopAlpha;
	}

	public double getHdpGlobalRtClusterStdev() {
		return hdpGlobalRtClusterStdev;
	}

	public void setHdpGlobalRtClusterStdev(double hdpGlobalRtClusterStdev) {
		this.hdpGlobalRtClusterStdev = hdpGlobalRtClusterStdev;
	}

	public double getHdpLocalRtClusterStdev() {
		return hdpLocalRtClusterStdev;
	}

	public void setHdpLocalRtClusterStdev(double hdpLocalRtClusterStdev) {
		this.hdpLocalRtClusterStdev = hdpLocalRtClusterStdev;
	}

	public double getHdpMassTol() {
		return hdpMassTol;
	}

	public void setHdpMassTol(double hdpMassTol) {
		this.hdpMassTol = hdpMassTol;
	}

	public boolean isHdpSpeedUp() {
		return hdpSpeedUp;
	}

	public void setHdpSpeedUp(boolean hdpSpeedUp) {
		this.hdpSpeedUp = hdpSpeedUp;
	}

	public int getHdpSpeedUpNumSample() {
		return hdpSpeedUpNumSample;
	}

	public void setHdpSpeedUpNumSample(int hdpSpeedUpNumSample) {
		this.hdpSpeedUpNumSample = hdpSpeedUpNumSample;
	}

	public int getHdpRefFileIdx() {
		return hdpRefFileIdx;
	}

	public void setHdpRefFileIdx(int hdpRefFileIdx) {
		this.hdpRefFileIdx = hdpRefFileIdx;
	}
	
	public String getIdentificationDatabase() {
		return identificationDatabase;
	}

	public void setIdentificationDatabase(String identificationDatabase) {
		this.identificationDatabase = identificationDatabase;
	}

	public String getGroundTruthDatabase() {
		return groundTruthDatabase;
	}

	public void setGroundTruthDatabase(String groundTruthDatabase) {
		this.groundTruthDatabase = groundTruthDatabase;
	}

	public String getMode() {
		return mode;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

}
