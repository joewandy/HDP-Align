package com.joewandy.alignmentResearch.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import com.joewandy.alignmentResearch.main.MultiAlignConstants;

public class HDPMassCluster implements Serializable {

	private static final long serialVersionUID = -4499614245912320150L;

	private int id;
	private double theta;
	private int countPeaks;
	private double sumPeaks;
	private Set<Feature> peakData;
	private HDPPrecursorMass precursorMass;
	private Set<String> messages;
	
	// dummy constructor for jackson
	public HDPMassCluster() {
	}
	
	public HDPMassCluster(int id) {
		this.id = id;
		this.peakData = new HashSet<Feature>();
		this.messages = new HashSet<String>();
	}

	// copy constructor
	public HDPMassCluster(HDPMassCluster another) {
		this.id = another.id;
		this.theta = another.theta;
		this.countPeaks = another.countPeaks;
		this.sumPeaks = another.sumPeaks;
		this.peakData = new HashSet<Feature>(another.peakData);
		if (another.precursorMass != null) {
			this.precursorMass = new HDPPrecursorMass(another.precursorMass);			
		}
		this.messages = new HashSet<String>(another.messages);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public void setCountPeaks(int countPeaks) {
		this.countPeaks = countPeaks;
	}
	
	public double getSumPeaks() {
		return sumPeaks;
	}

	public void setSumPeaks(double sumPeaks) {
		this.sumPeaks = sumPeaks;
	}
	
	public Set<Feature> getPeakData() {
		return peakData;
	}

	public void setPeakData(Set<Feature> peakData) {
		this.peakData = peakData;
	}

	public Set<String> getMessages() {
		return messages;
	}

	public void setMessages(Set<String> messages) {
		this.messages = messages;
	}
	
	public void addMessage(String message) {
		this.messages.add(message);
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
		double mass = Math.exp(theta);
		StringBuilder builder = new StringBuilder();
		builder.append("HDPMassCluster [id=");
		builder.append(id);
		builder.append(", mass=");
		builder.append(String.format(MultiAlignConstants.MASS_FORMAT, mass));
		builder.append(", countPeaks=");
		builder.append(countPeaks);
		builder.append(", messages=");
		builder.append(messages);
		if (precursorMass != null) {
			builder.append(", precursorMass=");
			builder.append(String.format(MultiAlignConstants.MASS_FORMAT, precursorMass.getMass()));
			builder.append(", precursorMassMessages=");
			builder.append(precursorMass.getMessages());
		}
		builder.append("]");
		return builder.toString();
	}
	
}