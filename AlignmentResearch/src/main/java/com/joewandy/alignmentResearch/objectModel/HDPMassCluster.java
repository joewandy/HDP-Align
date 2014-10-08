package com.joewandy.alignmentResearch.objectModel;

import java.util.HashSet;
import java.util.Set;

public class HDPMassCluster {

	private int id;
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
		sumPeaks = f.getMassLog();					
	}	
	
}
