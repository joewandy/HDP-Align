package com.joewandy.alignmentResearch.objectModel;

public class AlignmentEntry {

	private int clusterId;
	private int binId;
	private int sourcePeakSet;
	private int groupId;
	private int peakId;
	private double mass;
	private double rt;
	private double intensity;
	
	public AlignmentEntry(int clusterId, int binId, int sourcePeakSet, int peakId, 
			int groupId, double mass, double rt, double intensity) {
		super();
		this.clusterId = clusterId;
		this.binId = binId;
		this.sourcePeakSet = sourcePeakSet;
		this.groupId = groupId;
		this.peakId = peakId;
		this.mass = mass;
		this.rt = rt;
		this.intensity = intensity;
	}

	public int getClusterId() {
		return clusterId;
	}

	public int getBinId() {
		return binId;
	}

	public int getSourcePeakSet() {
		return sourcePeakSet;
	}
	
	public int getGroupId() {
		return groupId;
	}

	public int getPeakId() {
		return peakId;
	}

	public double getMass() {
		return mass;
	}

	public double getRt() {
		return rt;
	}

	public double getIntensity() {
		return intensity;
	}

	@Override
	public String toString() {
		if (this.getBinId() == 0) {
			return 
					"\t\tsourcePeakSet " + this.getSourcePeakSet() + 
					"\t\tgroupIdId " + String.format("%5d", this.getGroupId()) + 
					"\t\tpeakId " + String.format("%5d", this.getPeakId()) + 
					"\t\tmass " + String.format("%4.8f", this.getMass()) +  
					"\t\trt " + String.format("%5.5f", this.getRt()) +
					"\t\tintensity " + String.format("%10.3f", this.getIntensity());	
		} else {
			return 
					"\t\tclusterId " + this.getClusterId() +				
					"\t\tbinId " + this.getBinId() +								
					"\t\t\tsourcePeakSet " + this.getSourcePeakSet() + 
					"\t\tgroupId " + String.format("%5d", this.getGroupId()) + 
					"\t\tpeakId " + String.format("%5d", this.getPeakId()) + 
					"\t\tmass " + String.format("%4.8f", this.getMass()) +  
					"\t\tlog mass " + String.format("%4.8f", Math.log(this.getMass())) +  								
					"\t\trt " + String.format("%5.5f", this.getRt()) +
					"\t\tintensity " + String.format("%10.3f", this.getIntensity());				
		}
	}
	
}
