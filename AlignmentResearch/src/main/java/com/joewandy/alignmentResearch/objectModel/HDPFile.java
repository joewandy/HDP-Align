package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class HDPFile {

	private int id;
	private List<Feature> features;
	private int K;
	private List<Integer> Z;		// peak to RT clusters assignment
	private List<Integer> topZ; 	// RT clusters to metabolite assignment
	private List<Double> tij;		// RT clusters' time
	private List<Integer> countZ;	// no. of peaks under each RT cluster
	private List<Double> sumZ;		// sum of peak RT under each RT cluster
	
	public HDPFile(int id) {
		this.id = id;
		this.features = new ArrayList<Feature>();
		this.Z = new ArrayList<Integer>();
		this.topZ = new ArrayList<Integer>();
		this.tij = new ArrayList<Double>();
		this.countZ = new ArrayList<Integer>();
		this.sumZ = new ArrayList<Double>();
	}

	public int getId() {
		return id;
	}

	public List<Feature> getFeatures() {
		return features;
	}
	
	public void addFeatures(List<Feature> newFeatures) {
		features.addAll(newFeatures);
	}
	
	public double getMassSum(boolean usePpm) {
		double mass = 0;
		for (Feature f : features) {
			if (usePpm) {
				mass += f.getMassLog();				
			} else {
				mass += f.getMass();
			}
		}
		return mass;
	}

	public double getRtSum() {
		double rt = 0;
		for (Feature f : features) {
			rt += f.getRt();
		}
		return rt;
	}
	
	public int N() {
		return features.size();
	}

	public int K() {
		return K;
	}	
	
	public void setK(int k) {
		K = k;
	}

	public void increaseK() {
		K++;
	}
	
	public void decreaseK() {
		K--;
	}
	
	public int Z(int n) {
		return Z.get(n);
	}
	
	public int Zsize() {
		return Z.size();
	}
	
	public void setZ(int n, int k) {
		Z.set(n, k);
	}
	
	public void appendZ(int k) {
		Z.add(k);
	}
	
	public void reindexZ(int k) {
		for (int n = 0; n < Z.size(); n++) {
			int currentVal = Z(n);
			if (currentVal > k) {
				Z.set(n, currentVal-1);
			}
		}
	}	
			
	public int topZ(int k) {
		return topZ.get(k);
	}
	
	public void setTopZ(int k, int i) {
		topZ.set(k, i);
	}
	
	public void appendTopZ(int i) {
		topZ.add(i);
	}	
	
	public void removeTopZ(int k) {
		topZ.remove(k);
	}
	
	public void reindexTopZ(int i) {
		for (int k = 0; k < topZ.size(); k++) {
			int currentVal = topZ(k);
			if (currentVal > i) {
				topZ.set(k, currentVal-1);
			}
		}
	}		
	
	public double tij(int k) {
		return tij.get(k);
	}
	
	public double[] tijArray() {
		Double[] temp = tij.toArray(new Double[tij.size()]);
		return ArrayUtils.toPrimitive(temp);
	}
	
	public void setTij(int k, double ti) {
		this.tij.set(k, ti);
	}

	public void appendTij(double ti) {
		this.tij.add(ti);
	}
	
	public void removeTij(int k) {
		this.tij.remove(k);
	}
		
	public int countZ(int k) {
		return countZ.get(k);
	}
	
	public void setCountZ(int k, int count) {
		countZ.set(k, count);
	}	
	
	public void increaseCountZ(int k) {
		int currCount = countZ(k);
		int newCount = currCount + 1;
		setCountZ(k, newCount);
	}		

	public void decreaseCountZ(int k) {
		int currCount = countZ(k);
		int newCount = currCount - 1;
		setCountZ(k, newCount);
	}		
	
	public void appendCountZ(int count) {
		countZ.add(count);
	}
	
	public void removeCountZ(int k) {
		countZ.remove(k);
	}

	public int[] countZArray() {
		Integer[] temp = countZ.toArray(new Integer[countZ.size()]);
		return ArrayUtils.toPrimitive(temp);
	}
	
	public double sumZ(int k) {
		return sumZ.get(k);
	}
	
	public void setSumZ(int k, double sum) {
		sumZ.set(k, sum);
	}	

	public void addSumZ(int k, double sum) {
		double currSum = sumZ(k);
		double newSum = currSum + sum;
		setSumZ(k, newSum);
	}		

	public void subsSumZ(int k, double sum) {
		double currSum = sumZ(k);
		double newSum = currSum - sum;
		setSumZ(k, newSum);
	}		

	public void appendSumZ(double sum) {
		sumZ.add(sum);
	}
	
	public void removeSumZ(int k) {
		sumZ.remove(k);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
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
		HDPFile other = (HDPFile) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "HDPFile [id=" + id + ", countZ="
				+ countZ + "]";
	}
	
}
