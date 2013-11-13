package com.joewandy.alignmentResearch.objectModel;

import com.joewandy.alignmentResearch.main.MultiAlign;

import peakml.Annotation;
import peakml.IPeak;


public class AlignmentPair {

	private int sourcePeakSet1;
	private int groupId1;
	private int peakId1;
	private double mass1;
	private double rt1;
	private double intensity1;

	private int sourcePeakSet2;
	private int groupId2;
	private int peakId2;
	private double mass2;
	private double rt2;
	private double intensity2;

	private IPeak peak1;
	private IPeak peak2;
	private Feature feature1;
	private Feature feature2;
	
	private boolean delete;
	private double dmz;
	private double drt;

	private AlignmentEdge parent;
	
	public AlignmentPair(IPeak peak1, IPeak peak2, double dmz, double drt, AlignmentEdge parent) {

		this.peak1 = peak1;
		this.peak2 = peak2;
			
		this.sourcePeakSet1 = peak1.getAnnotation(Annotation.sourcePeakset).getValueAsInteger();
		this.groupId1 = peak1.getAnnotation(Annotation.relationid).getValueAsInteger();
		this.peakId1 = peak1.getAnnotation(Annotation.relationid).getValueAsInteger();
		this.mass1 = peak1.getMass();
		this.intensity1 = peak1.getIntensity();
		this.rt1 = peak1.getRetentionTime();		

		this.sourcePeakSet2 = peak2.getAnnotation(Annotation.sourcePeakset).getValueAsInteger();
		this.groupId2 = peak2.getAnnotation(Annotation.relationid).getValueAsInteger();
		this.peakId2 = peak2.getAnnotation(Annotation.relationid).getValueAsInteger();
		this.mass2 = peak2.getMass();
		this.intensity2 = peak2.getIntensity();
		this.rt2 = peak2.getRetentionTime();	
		
		this.delete = false;
		this.dmz = dmz;
		this.drt = drt;
		this.parent = parent;
		
	}

	public AlignmentPair(Feature feature1, Feature feature2, double dmz, double drt, AlignmentEdge parent) {

		this.feature1 = feature1;
		this.feature2 = feature2;
			
		this.sourcePeakSet1 = feature1.getData().getId();
		this.groupId1 = feature1.getFirstGroup().getGroupId();
		this.peakId1 = feature1.getPeakID();
		this.mass1 = feature1.getMass();
		this.intensity1 = feature1.getIntensity();
		this.rt1 = feature1.getRt();

		this.sourcePeakSet2 = feature2.getData().getId();
		this.groupId2 = feature2.getFirstGroup().getGroupId();
		this.peakId2 = feature2.getPeakID();
		this.mass2 = feature2.getMass();
		this.intensity2 = feature2.getIntensity();
		this.rt2 = feature2.getRt();

		this.delete = false;
		this.dmz = dmz;
		this.drt = drt;

		this.parent = parent;

	}
	
	public int getSourcePeakSet1() {
		return sourcePeakSet1;
	}

	public int getGroupId1() {
		return groupId1;
	}

	public int getPeakId1() {
		return peakId1;
	}

	public double getMass1() {
		return mass1;
	}

	public double getRt1() {
		return rt1;
	}

	public double getIntensity1() {
		return intensity1;
	}

	public int getSourcePeakSet2() {
		return sourcePeakSet2;
	}

	public int getGroupId2() {
		return groupId2;
	}

	public int getPeakId2() {
		return peakId2;
	}

	public double getMass2() {
		return mass2;
	}

	public double getRt2() {
		return rt2;
	}

	public double getIntensity2() {
		return intensity2;
	}

	public IPeak getPeak1() {
		return peak1;
	}

	public IPeak getPeak2() {
		return peak2;
	}

	public Feature getFeature1() {
		return feature1;
	}

	public Feature getFeature2() {
		return feature2;
	}

	public double getScore() {
		DistanceCalculator calc = new MahalanobisDistanceCalculator(dmz, drt);
		double dist = calc.compute(mass1, mass2, rt1, rt2);		
		double inverseDist = 1/dist;
		double weight = 1;
		double score = weight * inverseDist;
		return score;
	}

	public double getScore(double maxWeight, double maxDist, double weightCoeff, double distCoeff) {
		DistanceCalculator calc = new MahalanobisDistanceCalculator(dmz, drt);
		double dist = calc.compute(mass1, mass2, rt1, rt2);		
		dist = dist / maxDist;
		double weight = 1;
		weight = weight / maxWeight;
		double score = weightCoeff*weight + distCoeff*dist;
		score = 1/score;
		return score;
	}
	
	public double getWeight() {
		if (parent != null) {
			return parent.getWeight();			
		} else {
			return 1;
		}
	}

	public double getProbWeight() {
		if (parent != null) {
			return parent.getProbWeight(feature1, feature2);			
		} else {
			return 1;
		}
	}
	
	public double getDist() {
		DistanceCalculator calc = new MahalanobisDistanceCalculator(dmz, drt);
		double dist = calc.compute(mass1, mass2, rt1, rt2);		
		return dist;
	}
	
	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
		this.feature1.setDelete(delete);
		this.feature2.setDelete(delete);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + groupId1;
		result = prime * result + groupId2;
		result = prime * result + peakId1;
		result = prime * result + peakId2;
		result = prime * result + sourcePeakSet1;
		result = prime * result + sourcePeakSet2;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlignmentPair other = (AlignmentPair) obj;
		if (groupId1 != other.groupId1)
			return false;
		if (groupId2 != other.groupId2)
			return false;
		if (peakId1 != other.peakId1)
			return false;
		if (peakId2 != other.peakId2)
			return false;
		if (sourcePeakSet1 != other.sourcePeakSet1)
			return false;
		if (sourcePeakSet2 != other.sourcePeakSet2)
			return false;
		return true;
	}

	public double getIntensitySquareError() {
		double error = this.intensity1 - this.intensity2;
		return Math.pow(error, 2);	
	}
	
	public double getRelativeIntensityErrorScore() {
//		double error = (Math.log(this.intensity1) - Math.log(this.intensity2));
//		error = Math.pow(error, 2);
//		error = error / Math.log(Math.max(this.intensity1, this.intensity2));
//		return error;
		double error = this.getRelativeIntensityError();
		return 1 - error;
	}

	public double getRelativeIntensityError() {
		double error = Math.abs(this.intensity1 - this.intensity2);
		error = error / Math.max(this.intensity1, this.intensity2);
		return error;
	}

	public void print() {
		String output = this.feature1.getRt() + ", " + this.feature1.getFirstGroup().getFeatureCount() + ", " +
				this.feature2.getRt() + ", " + this.feature2.getFirstGroup().getFeatureCount();
		System.out.println(output);
	}
	
	@Override
	public String toString() {
		String output = "==============================================================\n";
		output += "sourcePeakSet1=" + sourcePeakSet1 + ", groupId1=" + groupId1 + ", peakId1=" + peakId1 + "\n";
		output += "mass1=" + String.format("%.6f", mass1) + ", rt1=" + String.format("%4.2f", rt1) + ", intensity1=" + String.format("%10.2f", intensity1) + "\n";
		output += " -- aligned to -- \n";
		output += "sourcePeakSet2=" + sourcePeakSet2 + ", groupId2=" + groupId2 + ", peakId2=" + peakId2 + "\n";
		output += "mass2=" + String.format("%.6f", mass2) + ", rt2=" + String.format("%4.2f", rt2) + ", intensity2=" + String.format("%10.2f", intensity2) + "\n";
		output += "intensity square error = " + String.format("%10.2f", this.getRelativeIntensityError()) + "\n";
		output += "weight = " + String.format("%3.2f", this.getWeight()) + " score = " + String.format("%3.2f", this.getScore()) + "\n";
		return output;
	}
	
}
