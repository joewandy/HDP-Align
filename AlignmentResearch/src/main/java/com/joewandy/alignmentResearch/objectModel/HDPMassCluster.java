package com.joewandy.alignmentResearch.objectModel;

import java.util.HashSet;
import java.util.Set;

public class HDPMassCluster {

	private int id;
	private double theta;
	private int countPeaks;
	private double sumPeaks;
	private Set<Feature> peakData;
	
	public HDPMassCluster(int id) {
		this.id = id;
		this.peakData = new HashSet<Feature>();
	}

	public int getId() {
		return id;
	}

	public double getTheta() {
		return theta;
	}

	public void setTheta(double theta) {
		this.theta = theta;
	}

	public int getCountPeaks() {
		return countPeaks;
	}

	public double getSumPeaks() {
		return sumPeaks;
	}

	public Set<Feature> getPeakData() {
		return peakData;
	}
	
	public void addFeature(Feature f) {
		countPeaks++;
		sumPeaks += f.getMassLog();
		peakData.add(f);
	}	
	
	public void removeFeature(Feature f) {
		countPeaks--;
		sumPeaks -= f.getMassLog();
		peakData.remove(f);
	}
	
	public boolean contains(Feature f) {
		return peakData.contains(f);
	}

	@Override
	public String toString() {
		return "HDPMassCluster [id=" + id + ", mass=" + Math.exp(theta)
				+ ", countPeaks=" + countPeaks + "]";
	}
	
}
