package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import peakml.chemistry.PeriodicTable;

import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.RuleG;

public class AlignmentRow {

	private AlignmentList parent;
	private int rowId;
	private Set<Feature> features;
	private List<AlignmentPair> pairs;
	private boolean aligned;
	private boolean delete;
	private double normalisedScore;
	private List<RuleG> satisfiedRules;
	private double avgMz;
	private double avgRt;
	private double absoluteRtDiff;
	private double minRt;
	
	public AlignmentRow(AlignmentList parent, int rowId) {
		this.parent = parent;
		this.rowId = rowId;
		this.features = new HashSet<Feature>();
		this.pairs = new ArrayList<AlignmentPair>();
		this.aligned = false;
		this.delete = false;
		this.satisfiedRules = new ArrayList<RuleG>();
		recomputeStats();
	}
	
	public int getRowId() {
		return rowId;
	}

	public void addAlignedFeature(Feature feature) {
		feature.setAligned(true);
		this.features.add(feature);
		recomputeStats();
	}

	public void addAlignedFeatures(Set<Feature> features) {
		for (Feature f : features) {
			f.setAligned(true);
		}
		this.features.addAll(features);
		recomputeStats();
	}
	
	public void addFeature(Feature feature) {
		this.features.add(feature);
		recomputeStats();
	}

	public void addFeatures(Set<Feature> features) {
		this.features.addAll(features);
		recomputeStats();
	}

	public Set<Feature> getFeatures() {
		return features;
	}
	
	public boolean contains(Feature f) {
		if (features.contains(f)) {
			return true;
		}
		return false;
	}

	public Set<Integer> getGroupIds() {
		Set<Integer> groupIds = new HashSet<Integer>();
		for (Feature f : features) {
			groupIds.add(f.getFirstGroupID());
		}
		return groupIds;
	}
	
	public Feature getFirstFeature() {
		Iterator<Feature> iter = features.iterator();
		return iter.next();
	}
	
	public int getFeaturesCount() {
		return features.size();
	}
	
	public double[] getFeatureRts() {
		double[] rts = new double[features.size()];
		int counter = 0;
		for (Feature f : features) {
			rts[counter] = f.getRt();
			counter++;
		}
		return rts;
	}
	
	public Feature getFeaturesFromFile(String fileName) {
		Feature feature = null;
		for (Feature f : this.features) {
			if (fileName.equals(f.getData().getFilenameWithoutExtension())) {
				feature = f;
				break;
			}
		}
		return feature;
	}
		
	public double getPairGraphScore() {

		double sum = 0;
		for (AlignmentPair pair : pairs) {
			sum += pair.getScore();
		}
		return sum / pairs.size();
		
	}

	public double getNormalizedPairGraphScore(double max) {
		double score = this.getPairGraphScore();
		normalisedScore = score / max;
		return normalisedScore;
	}
	
	public double getPairIntensityScore() {
		double sum = 0;
		for (AlignmentPair pair : pairs) {
			sum += pair.getRelativeIntensityErrorScore();
		}
		return sum / pairs.size();
	}
	
	@Deprecated
	public Feature getFeatureByKey(String key) {
		for (Feature f : features) {
			// the key is actually the data file name, without extension
			String featureKey = f.getData().getFilenameWithoutExtension();
			if (key.equals(featureKey)) {
				return f;
			}
		}
		return null;
	}
	
	public void addPair(AlignmentPair pair) {
		this.pairs.add(pair);
	}

	public List<AlignmentPair> getPairs() {
		return pairs;
	}

	public boolean isAligned() {
		return aligned;
	}

	public void setAligned(boolean aligned) {
		this.aligned = aligned;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
		for (Feature f : this.features) {
			f.setDelete(true);
		}
	}

	public double getAverageMz() {
		return avgMz;
	}

	public double getAverageRt() {
		return avgRt;
	}

	public double getAbsoluteRtDiff() {
		return absoluteRtDiff;
	}

	public double getMinRt() {
		return minRt;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + rowId;
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
		AlignmentRow other = (AlignmentRow) obj;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (rowId != other.rowId)
			return false;
		return true;
	}

	@Override
	public String toString() {
		String output = "SimpleAlignmentRow [parent=" + parent + ", rowId=" + rowId + ", size=" + features.size() + ", featureIDs=";
		for (Feature f : this.features) {
			output += f.getPeakID() + ",";
		}
		output = output.substring(0, output.length()-1);
		output += "]";
		return output;
	}

	public List<RuleG> checkRules(List<RuleG> rulesToCheck) {
		for (RuleG rule : rulesToCheck) {
			Set<Integer> initialSet = rule.getGroupIds();
			int initialCount = initialSet.size();
			Set<Integer> intersection = new HashSet<Integer>(initialSet);
			Set<Integer> mine = this.getGroupIds();
			intersection.retainAll(mine);
			if (initialCount == intersection.size()) {
				satisfiedRules.add(rule);
			}
		}
		return satisfiedRules;
	}
	
	public boolean rowInRange(AlignmentRow another, double massTol, double rtTol, 
			boolean usePpm) {

		double delta = 0;
		if (usePpm) {
			delta = PeriodicTable.PPM(this.getAverageMz(), massTol);			
		} else {
			delta = massTol;			
		}

		double massLower = this.getAverageMz() - delta/2;
		double massUpper = this.getAverageMz() + delta/2;
		double rtLower = this.getAverageRt() - rtTol/2;
		double rtUpper = this.getAverageRt() + rtTol/2;	
		
		double massToCheck = another.getAverageMz();
		double rtToCheck = another.getAverageRt();
		if (inRange(massToCheck, massLower, massUpper)) {

			// in the mass range
			if (rtTol != -1) {
				
				// and in retention time range
				if (inRange(rtToCheck, rtLower, rtUpper)) {
					return true;
				}
				 
			} else {

				// not using retention time check
				return true;
			
			}
			
		}
		
		return false;

	}
	
	private boolean inRange(double toCheck, double lowerRange, double upperRange) {
		// TODO: double comparison ?
		if (toCheck > lowerRange && toCheck < upperRange) {
			return true;
		} else {
			return false;
		}
	}
	
	private void recomputeStats() {
		this.avgMz = this.computeAverageMz();
		this.avgRt = this.computeAverageRt();
		this.absoluteRtDiff = this.computeAbsoluteRtDiff();
		this.minRt = this.computeMinRt();		
	}

	private double computeAverageMz() {
		double sum = 0;
		for (Feature f : features) {
			sum += f.getMass();
		}
		return sum / getFeaturesCount();
	}
	
	private double computeAverageRt() {
		double sum = 0;
		for (Feature f : features) {
			sum += f.getRt();
		}
		return sum / getFeaturesCount();
	}
	
	private double computeMinRt() {
		double min = 0;
		for (Feature f : features) {
			if (f.getRt() > min) {
				min = f.getRt();
			}
		}
		return min;
	}
	
	private double computeAbsoluteRtDiff() {
		if (features.size() < 2) {
			return 0;
		} else {
			double diff = 0;
			double mean = computeAverageRt();
			for (Feature f : features) {
				diff += Math.abs(f.getRt()-mean);
			}
			return diff;
		}
	}
		
}
