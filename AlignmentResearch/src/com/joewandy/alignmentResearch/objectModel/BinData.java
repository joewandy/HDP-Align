package com.joewandy.alignmentResearch.objectModel;

import peakml.IPeak;

public class BinData {

	private IPeak peak;
	private int sourcePeakSet;
	
	public BinData(IPeak peak, int sourcePeakSet) {
		super();
		this.peak = peak;
		this.sourcePeakSet = sourcePeakSet;
	}

	public IPeak getPeak() {
		return peak;
	}

	public int getSourcePeakSet() {
		return sourcePeakSet;
	}

	@Override
	public String toString() {
		return "BinData [peak=" + peak + ", sourcePeakSet=" + sourcePeakSet
				+ "]";
	}
	
}
