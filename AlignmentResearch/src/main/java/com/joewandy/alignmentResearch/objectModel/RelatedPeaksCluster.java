package com.joewandy.alignmentResearch.objectModel;

import peakml.IPeak;
import peakml.IPeakSet;

public class RelatedPeaksCluster {

	private int clusterId;
	private int sourcePeakSet;
	private IPeakSet<IPeak> peaks;

	public RelatedPeaksCluster(int clusterId, int sourcePeakSet, IPeakSet<IPeak> peaks) {
		super();
		this.clusterId = clusterId;
		this.sourcePeakSet = sourcePeakSet;
		this.peaks = peaks;
	}

	public int getSourcePeakSet() {
		return sourcePeakSet;
	}

	public int getClusterId() {
		return clusterId;
	}

	public IPeakSet<IPeak> getPeaks() {
		return peaks;
	}

	@Override
	public String toString() {
		return "RelatedPeaksCluster [clusterId=" + clusterId + ",\tsourcePeakSet="
				+ sourcePeakSet + ",\tpeaks=" + peaks.getMinMass() + "-" + peaks.getMaxMass() + "]";
	}

}
