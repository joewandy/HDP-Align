package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HDPMetabolite {

	private int id;									// id of this metabolite
	private List<Feature> peakData;					// all the peaks under this metabolite

	private int massClusterSeqId;
	private int A;									// count of mass clusters in this metabolite
	private List<HDPMassCluster> massClusters;		// list of mass clusters objects
	private List<Integer> V;						// membership of which peak in peakData to the mass clusters in the list above
	
	public HDPMetabolite(int id) {
		this.id = id;
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
	
	public void addMassCluster() {
		HDPMassCluster newCluster = new HDPMassCluster(massClusterSeqId);
		massClusterSeqId++;
		massClusters.add(newCluster);
	}
	
	public void deleteMassCluster(int a) {
		massClusters.remove(a);
	}
	
	public void addPeak(Feature f, int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		massCluster.addFeature(f);
		V.add(a);
		peakData.add(f);
	}

	public void removePeak(Feature f) {
		int peakPos = findPeakPos(f);
		int a = V(peakPos);
		V.remove(peakPos);
		peakData.remove(peakPos);
		HDPMassCluster massCluster = massClusters.get(a);
		massCluster.removeFeature(f);;
	}
	
	public List<Feature> getPeaksInMassCluster(int a) {
		Set<Feature> massClusterData = massClusters.get(a).getPeakData();
		return new ArrayList<Feature>(massClusterData);
	}
			
	public int fa(int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		return massCluster.getCountPeaks();
	}
	
	public int[] faArray() {
		int[] temp = new int[massClusters.size()];
		for (int a = 0; a < massClusters.size(); a++) {
			temp[a] = fa(a);
		}
		return temp;
	}
	
	public int getMassClustersSize() {
		return massClusters.size();
	}
	
	public double sa(int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		return massCluster.getSumPeaks();
	}
	
	public double[] saArray() {
		double[] temp = new double[massClusters.size()];
		for (int a = 0; a < massClusters.size(); a++) {
			temp[a] = sa(a);
		}
		return temp;
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

	@Override
	public String toString() {
		return "HDPMetabolite [id=" + id + ", peakData.size()=" + peakData.size() + ", peakData=" + peakData + "]";
	}

	public int[] getMassClusterIndicator(Feature thisPeak) {

		int[] results = new int[A+1];
		for (int a = 0; a < A; a++) {
			List<Feature> peaksInside = getPeaksInMassCluster(a);
			if (containsSameOrigin(peaksInside, thisPeak)) {
				results[a] = 0; // do not allow peaks from the same file to be put together in the same mass cluster
			} else {
				results[a] = 1;
			}
		}		
		
		results[A] = 1; // the last infinite part is always 1
		
		return results;

	}
	
	private boolean containsSameOrigin(List<Feature> features, Feature toFind) {
		for (Feature inside : features) {
			if (inside.equals(toFind)) {
				continue; // skip ourself
			}
			if (inside.getData().equals(toFind.getData())) {
				return true;
			}
		}
		return false;
	}
	
}
