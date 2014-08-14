package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;

public class HDPMetabolite {

	private int id;
	private int A;
	private List<Double> thetas;
	private List<Integer> fa;
	private List<Double> sa;
	
	private List<Integer> V;
	private List<Feature> peakData;
	
	public HDPMetabolite(int id) {
		this.id = id;
		this.thetas = new ArrayList<Double>();
		this.fa = new ArrayList<Integer>();
		this.sa = new ArrayList<Double>();
		this.V = new ArrayList<Integer>();
		this.peakData = new ArrayList<Feature>();
	}

	public int getId() {
		return id;
	}
	
	public int A() {
		return A;
	}
	
	public void setA(int a) {
		A = a;
	}

	public void increaseA() {
		A++;
	}
	
	public void decreaseA() {
		A--;
	}
	
	public List<Feature> getPeaksInMassCluster(int toFind) {
		assert(V.size() == peakData.size());
		List<Feature> results = new ArrayList<Feature>();
		for (int n = 0; n < peakData.size(); n++) {
			int a = V.get(n);
			if (a == toFind) {
				Feature f = peakData.get(n);
				results.add(f);
			}
		}
		return results;
	}
	
	public double theta(int a) {
		return thetas.get(a);
	}

	public double[] thetasArray() {
		Double[] temp = thetas.toArray(new Double[thetas.size()]);
		return ArrayUtils.toPrimitive(temp);
	}
	
	public void setTheta(int a, double newTheta) {
		thetas.set(a, newTheta);
	}

	public void appendTheta(double newTheta) {
		thetas.add(newTheta);
	}
	
	public void removeTheta(int a) {
		thetas.remove(a);
	}
	
	public int thetasSize() {
		return thetas.size();
	}
	
	public int fa(int a) {
		return fa.get(a);
	}
	
	public double[] faArray() {
		double[] temp = new double[fa.size()];
		for (int i = 0; i < fa.size(); i++) {
			temp[i] = fa.get(i).doubleValue();
		}
		return temp;
	}
	
	public void setFa(int a, int newFa) {
		fa.set(a, newFa);
	}
	
	public int faSize() {
		return fa.size();
	}
	
	public void increaseFa(int a) {
		fa.set(a, fa(a) + 1);
	}

	public void decreaseFa(int a) {
		fa.set(a, fa(a) - 1);
	}

	public void appendFa(int newFa) {
		fa.add(newFa);
	}
	
	public void removeFa(int a) {
		fa.remove(a);
	}
	
	public double sa(int a) {
		return sa.get(a);
	}
	
	public void setSa(int a, double newSa) {
		sa.set(a, newSa);
	}
	
	public int saSize() {
		return sa.size();
	}
	
	public void addSa(int a, double amount) {
		double currentSa = sa(a);
		setSa(a, currentSa + amount);
	}

	public void subsSa(int a, double amount) {
		double currentSa = sa(a);
		setSa(a, currentSa - amount);
	}

	public void appendSa(double newSa) {
		sa.add(newSa);
	}
	
	public void removeSa(int a) {
		sa.remove(a);
	}
	
	public int V(int peakPos) {
		return V.get(peakPos);
	}
	
	public void setV(int peakPos, int a) {
		V.set(peakPos, a);
	}
	
	public void appendV(int a) {
		V.add(a);
	}
	
	public void removeV(int peakPos) {
		V.remove(peakPos);
	}
	
	public int vSize() {
		return V.size();
	}
	
	public void reindexV(int a) {
		for (int n = 0; n < V.size(); n++) {
			int currentVal = V(n);
			if (currentVal > a) {
				V.set(n, currentVal-1);
			}
		}
	}
	
	public int findPeakPos(Feature toFind) {
		for (int peakPos = 0; peakPos < peakData.size(); peakPos++) {
			Feature f = peakData.get(peakPos);
			if (f.equals(toFind)) {
				return peakPos;
			}
		}
		return -1;
	}

	public Feature getPeakData(int peakPos) {
		return peakData.get(peakPos);
	}
	
	public int peakDataSize() {
		return peakData.size();
	}

	public void addPeakData(Feature peak) {
		this.peakData.add(peak);
	}
	
	public void addPeakData(List<Feature> peaks) {
		this.peakData.addAll(peaks);
	}
	
	public void removePeakData(int peakPos) {
		this.peakData.remove(peakPos);
	}
	
}
