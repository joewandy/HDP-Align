package com.joewandy.alignmentResearch.objectModel;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class HDPMassCluster implements Serializable {

	private static final long serialVersionUID = -4499614245912320150L;

	private int id;
	private double theta;
	private int countPeaks;
	private double sumPeaks;
	private Set<Feature> peakData;
	private HDPPrecursorMass precursorMass;
	
	// dummy constructor for jackson
	public HDPMassCluster() {
		
	}
	
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

	public HDPPrecursorMass getPrecursorMass() {
		return precursorMass;
	}

	public void setPrecursorMass(HDPPrecursorMass pc) {
		this.precursorMass = pc;
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
