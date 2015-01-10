package com.joewandy.alignmentResearch.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class HDPMetabolite implements Serializable {

	private static final long serialVersionUID = -6776646651530842805L;

	private int id;									// id of this metabolite
	private List<Feature> peakData;					// all the peaks under this metabolite

	private int massClusterSeqId;
	private List<HDPMassCluster> massClusters;		// list of mass clusters objects
	private Map<Feature, HDPMassCluster> V;			// which peak assigned to which mass clusters
	private List<Double> metaboliteMasses;
	
	// dummy constructor for jackson
	public HDPMetabolite() {
		
	}
	
	public HDPMetabolite(int id) {
		this.id = id;
		this.V = new HashMap<Feature, HDPMassCluster>();
		this.peakData = new ArrayList<Feature>();
		this.massClusters = new ArrayList<HDPMassCluster>();
		this.metaboliteMasses = new ArrayList<Double>();
	}
	
	/*
	 * Getters and setters
	 */

	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getMassClusterSeqId() {
		return massClusterSeqId;
	}

	public void setMassClusterSeqId(int massClusterSeqId) {
		this.massClusterSeqId = massClusterSeqId;
	}

	public Map<Feature, HDPMassCluster> getV() {
		return V;
	}

	public void setV(Map<Feature, HDPMassCluster> v) {
		V = v;
	}
	
	public List<Double> getMetaboliteMasses() {
		return this.metaboliteMasses;
	}
	
	public void setMetaboliteMasses(List<Double> metaboliteMasses) {
		this.metaboliteMasses = metaboliteMasses;
	}

	public List<HDPMassCluster> getMassClusters() {
		return massClusters;
	}
	
	public void setMassClusters(List<HDPMassCluster> massClusters) {
		this.massClusters = massClusters;
	}
	
	public List<Feature> getPeakData() {
		return peakData;
	}

	public void setPeakData(List<Feature> peakData) {
		this.peakData = peakData;
	}
	
	/*
	 * Additional methods
	 */
	
	@JsonIgnore
	public int getA() {
		return massClusters.size();
	}
		
	@JsonIgnore
	public Set<HDPPrecursorMass> getPrecursorMasses() {
		Set<HDPPrecursorMass> pcs = new HashSet<HDPPrecursorMass>();
		for (HDPMassCluster mc : this.getMassClusters()) {
			HDPPrecursorMass pc = mc.getPrecursorMass();
			if (pc != null) {
				pcs.add(mc.getPrecursorMass());				
			}
		}
		return pcs;
	}

	@JsonIgnore
	public int addMassCluster() {
		HDPMassCluster newCluster = new HDPMassCluster(massClusterSeqId);
		massClusterSeqId++;
		massClusters.add(newCluster);
		int a = massClusters.size()-1;
		return a;
	}

	@JsonIgnore
	public void removeMassCluster(HDPMassCluster mc) {
		massClusters.remove(mc);
	}
	
	@JsonIgnore
	public List<HDPMassCluster> getEmptyMassClusters() {
		List<HDPMassCluster> emptyList = new ArrayList<HDPMassCluster>();
		for (HDPMassCluster mc : massClusters) {
			if (mc.getCountPeaks() == 0) {
				emptyList.add(mc);
			}
		}
		return emptyList;
	}

	@JsonIgnore
	public void addPeak(Feature f, int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		massCluster.addFeature(f);
		V.put(f, massCluster);
		peakData.add(f);
	}

	@JsonIgnore
	public HDPMassCluster removePeak(Feature f) {
		HDPMassCluster massCluster = V.remove(f);
		peakData.remove(f);
		massCluster.removeFeature(f);
		return massCluster;
	}

	@JsonIgnore
	public HDPMassCluster getMassClusterOfPeak(Feature f) {
		HDPMassCluster massCluster = V.get(f);
		return massCluster;
	}

	@JsonIgnore
	public List<Feature> getPeaksInMassCluster(int a) {
		Set<Feature> massClusterData = massClusters.get(a).getPeakData();
		return new ArrayList<Feature>(massClusterData);
	}

	@JsonIgnore
	public int fa(int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		return massCluster.getCountPeaks();
	}

	@JsonIgnore
	public int[] faArray() {
		int[] temp = new int[massClusters.size()];
		for (int a = 0; a < massClusters.size(); a++) {
			temp[a] = fa(a);
		}
		return temp;
	}

	@JsonIgnore
	public double sa(int a) {
		HDPMassCluster massCluster = massClusters.get(a);
		return massCluster.getSumPeaks();
	}

	@JsonIgnore
	public double[] saArray() {
		double[] temp = new double[massClusters.size()];
		for (int a = 0; a < massClusters.size(); a++) {
			temp[a] = sa(a);
		}
		return temp;
	}

	@JsonIgnore
	public void setTheta(int a, double theta) {
		HDPMassCluster massCluster = massClusters.get(a);
		massCluster.setTheta(theta);
	}

	@JsonIgnore
	public int vSize() {
		return V.size();
	}

	@JsonIgnore
	public int findPeakPos(Feature toFind) {
		for (int peakPos = 0; peakPos < peakData.size(); peakPos++) {
			Feature f = peakData.get(peakPos);
			if (f.equals(toFind)) {
				return peakPos;
			}
		}
		return -1;
	}

	@JsonIgnore
	public Feature getPeakData(int peakPos) {
		return peakData.get(peakPos);
	}

	@JsonIgnore
	public int peakDataSize() {
		return peakData.size();
	}

	@JsonIgnore
	public void addPeakData(Feature peak) {
		this.peakData.add(peak);
	}

	@JsonIgnore
	public void addPeakData(List<Feature> peaks) {
		this.peakData.addAll(peaks);
	}

	@JsonIgnore
	public void removePeakData(int peakPos) {
		this.peakData.remove(peakPos);
	}

	@JsonIgnore
	public void addMetaboliteMass(double mass) {
		this.metaboliteMasses.add(mass);
	}

	@Override
	public String toString() {
		return "HDPMetabolite [id=" + id + ", massClusters.size()=" + massClusters.size() + ", peakData.size()=" + peakData.size() + "]";
	}

	@JsonIgnore
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
			if (inside.getFileID().equals(toFind.getFileID())) {
				return true;
			}
		}
		return false;
	}
	
}
