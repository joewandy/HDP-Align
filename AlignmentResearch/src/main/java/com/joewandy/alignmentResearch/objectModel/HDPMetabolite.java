package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HDPMetabolite {

	private int id;									// id of this metabolite
	private List<Feature> peakData;					// all the peaks under this metabolite

	private int massClusterSeqId;
	private List<HDPMassCluster> massClusters;		// list of mass clusters objects
	private Map<Feature, HDPMassCluster> V;			// which peak assigned to which mass clusters
	private List<Double> metaboliteMasses;
	
	public HDPMetabolite(int id) {
		this.id = id;
		this.V = new HashMap<Feature, HDPMassCluster>();
		this.peakData = new ArrayList<Feature>();
		this.massClusters = new ArrayList<HDPMassCluster>();
		this.metaboliteMasses = new ArrayList<Double>();
	}

	public int getId() {
		return id;
	}
	
	public int getA() {
		return massClusters.size();
	}
		
	public List<HDPMassCluster> getMassClusters() {
		return massClusters;
	}

	public int addMassCluster() {
		HDPMassCluster newCluster = new HDPMassCluster(massClusterSeqId);
		massClusterSeqId++;
		massClusters.add(newCluster);
		int a = massClusters.size()-1;
		return a;
	}
		
	public void removeMassCluster(HDPMassCluster mc) {
		massClusters.remove(mc);
	}
	
	public List<HDPMassCluster> getEmptyMassClusters() {
		List<HDPMassCluster> emptyList = new ArrayList<HDPMassCluster>();
		for (HDPMassCluster mc : massClusters) {
			if (mc.getCountPeaks() == 0) {
				emptyList.add(mc);
			}
		}
		return emptyList;
	}
	
	public void addPeak(Feature f, int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		massCluster.addFeature(f);
		V.put(f, massCluster);
		peakData.add(f);
	}
	
	public HDPMassCluster removePeak(Feature f) {
		HDPMassCluster massCluster = V.remove(f);
		peakData.remove(f);
		massCluster.removeFeature(f);
		return massCluster;
	}
	
	public HDPMassCluster getMassClusterOfPeak(Feature f) {
		HDPMassCluster massCluster = V.get(f);
		return massCluster;
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
			
	public void setTheta(int a, double theta) {
		HDPMassCluster massCluster = massClusters.get(a);
		massCluster.setTheta(theta);
	}
	
	public int vSize() {
		return V.size();
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
	
	public List<Feature> getPeakData() {
		return peakData;
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
	
	public List<Double> getMetaboliteMasses() {
		return this.metaboliteMasses;
	}
	
	public void addMetaboliteMass(double mass) {
		this.metaboliteMasses.add(mass);
	}

	@Override
	public String toString() {
		return "HDPMetabolite [id=" + id + ", massClusters.size()=" + massClusters.size() + ", peakData.size()=" + peakData.size() + "]";
	}

	public int[] getMassClusterIndicator(Feature thisPeak) {

		int[] results = new int[getA()+1];
		for (int a = 0; a < getA(); a++) {
			List<Feature> peaksInside = getPeaksInMassCluster(a);
			if (containsSameOrigin(peaksInside, thisPeak)) {
				results[a] = 0; // do not allow peaks from the same file to be put together in the same mass cluster
			} else {
				results[a] = 1;
			}
		}		
		
		results[getA()] = 1; // the last infinite part is always 1
		
		return results;

	}
	
	private boolean containsSameOrigin(List<Feature> features, Feature toFind) {
		for (Feature inside : features) {
			if (inside.getData().equals(toFind.getData())) {
				return true;
			}
		}
		return false;
	}
	
}
