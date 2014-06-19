package com.joewandy.alignmentResearch.objectModel;

import java.util.HashSet;
import java.util.Set;

import peakml.chemistry.PeriodicTable;


public class FeatureGroup {

	protected int groupId;
	protected Set<Feature> members;
	private boolean deleted;
	private boolean matched;
	private double score;
	
	public FeatureGroup(int groupId) {
		this.groupId = groupId;
		this.members = new HashSet<Feature>();
		this.deleted = false;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public void addFeature(Feature feature) {
		feature.addGroup(this);			
		members.add(feature);
	}

	public void addFeatures(Set<Feature> features) {
		for (Feature feature : features) {
			feature.addGroup(this);
		}
		members.addAll(features);
	}
	
	public Set<Feature> getFeatures() {
		return members;
	}
	
	public int getFeatureCount() {
		return members.size();
	}

	public int getAlignedFeatureCount() {
		int count = 0;
		for (Feature feature : members) {
			if (feature.isAligned()) {
				count++;
			}
		}
		return count;
	}
	
	public void clearFeatures() {
		for (Feature feature : this.members) {
			feature.clearGroup(this.groupId);
		}
		this.members.clear();
	}
	
	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
	
	public boolean isMatched() {
		return matched;
	}

	public void setMatched(boolean matched) {
		this.matched = matched;
	}

	public double getScore() {
		return score;
	}

	public void setScore(double score) {
		this.score = score;
	}

	public double getAverageMz() {
		double sum = 0;
		for (Feature f : this.getFeatures()) {
			sum += f.getMass();
		}
		return sum / this.getFeatureCount();
	}

	public double getAverageRt() {
		double sum = 0;
		for (Feature f : this.getFeatures()) {
			sum += f.getRt();
		}
		return sum / this.getFeatureCount();
	}
	
	public boolean groupInRange(FeatureGroup another, double massTol, double rtTol, 
			boolean usePpm) {

		double delta = 0;
		if (usePpm) {
			delta = PeriodicTable.PPM(this.getAverageMz(), massTol);			
		} else {
			delta = massTol;			
		}

		double massLower = this.getAverageMz() - delta/2;
		double massUpper = this.getAverageMz() + delta/2;
		double rtLower = this.getAverageRt() - rtTol/2;
		double rtUpper = this.getAverageRt() + rtTol/2;	
		
		double massToCheck = another.getAverageMz();
		double rtToCheck = another.getAverageRt();
		if (inRange(massToCheck, massLower, massUpper)) {

			// in the mass range
			if (rtTol != -1) {
				
				// and in retention time range
				if (inRange(rtToCheck, rtLower, rtUpper)) {
					return true;
				}
				 
			} else {

				// not using retention time check
				return true;
			
			}
			
		}
		
		return false;

	}
	
	private boolean inRange(double toCheck, double lowerRange, double upperRange) {
		// TODO: double comparison ?
		if (toCheck > lowerRange && toCheck < upperRange) {
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String toString() {
		return "FeatureGroup [groupId=" + groupId 
				+ ", alignedCount=" + this.getAlignedFeatureCount() 
				+ ", features=" + this.getFeatureIDs() 
				+ ", score=" + this.getScore()
				+ "]";
	}
	
	private String getFeatureIDs() {
		String output = "";
		for (Feature f : this.getFeatures()) {
			output += f.getPeakID() + ", ";
		}
		return output;
	}
	
}
