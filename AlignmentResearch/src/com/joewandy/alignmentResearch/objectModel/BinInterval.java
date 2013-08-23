package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;

import peakml.IPeak;

public class BinInterval implements Comparable<BinInterval> {

	private int index;
	private double binStart;
	private double binEnd;
	private List<BinData> data;
	
	public BinInterval(int index, double binStart, double binEnd) {
		this.index = index;
		this.binStart = binStart;
		this.binEnd = binEnd;
		this.data = new ArrayList<BinData>();
	}
	
	/**
	 * Copy constructor
	 * @param interval The interval to copy
	 */
	public BinInterval(BinInterval interval) {
		this.index = interval.getIndex();
		this.binStart = interval.getBinStart();
		this.binEnd = interval.getBinEnd();
		this.data = new ArrayList<BinData>(interval.getData());
	}
	
	public double getBinStart() {
		return binStart;
	}

	public double getBinEnd() {
		return binEnd;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public List<BinData> getData() {
		return data;
	}

	public List<Integer> getDataIds() {
		List<Integer> ids = new ArrayList<Integer>();
		for (BinData d : data) {
			ids.add(d.getPeak().getPatternID());
		}
		return ids;
	}
	
	public boolean addPeak(IPeak peak, int sourcePeakSet) {
		double mz = peak.getMass();
		double logMz = Math.log(mz);
		if (isInInterval(logMz)) {
			BinData newData = new BinData(peak, sourcePeakSet);
			this.data.add(newData);
			return true;
		}
		return false;
	}
	
	public void mergePeaks(BinInterval other) {
		if (this.index != other.index) {
			throw new RuntimeException("Cannot merge " + this.index + " with " + other.index);
		} else {
			this.data.addAll(other.getData());
		}
	}
	
	public int getSize() {
		return this.getData().size();
	}
	
	public boolean isEmpty() {
		if (getSize() == 0) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean isInInterval(double mzLog) {
		boolean moreThanEqualStart = Double.compare(binStart, mzLog) < 0 || Double.compare(binStart,  mzLog) == 0;
		boolean lessThanEqualEnd = Double.compare(binEnd, mzLog) > 0 || Double.compare(binEnd,  mzLog) == 0;
		if (moreThanEqualStart && lessThanEqualEnd) {
			return true;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = Double.doubleToLongBits(binEnd);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		temp = Double.doubleToLongBits(binStart);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + index;
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
		BinInterval other = (BinInterval) obj;
		if (Double.doubleToLongBits(binEnd) != Double
				.doubleToLongBits(other.binEnd))
			return false;
		if (Double.doubleToLongBits(binStart) != Double
				.doubleToLongBits(other.binStart))
			return false;
		if (index != other.index)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "BinInterval [index=" + index + ", binStart=" + binStart
				+ ", binEnd=" + binEnd + ", interval=" + (binEnd-binStart) 
				+ ", data=" + data + "]";
	}

	@Override
	public int compareTo(BinInterval arg0) {
		// TODO Auto-generated method stub
		return 0;
	}
	
}
