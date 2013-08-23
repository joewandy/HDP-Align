package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.collections15.Bag;
import org.apache.commons.collections15.bag.TreeBag;


public class AlignmentExpResult {

	// tired of manually keeping track of the counts using map
	private Bag<Integer> degreeDistribution;
	private Bag<Double> edgeWeightDistribution;
	private Map<Double, Double> intensityByEdgeWeight;
	private Map<Double, Integer> groupSizeByEdgeWeight;
	private List<AlignmentPair> alignmentPairs;
	private List<AlignmentEdge> alignmentEdges;
	private List<AlignmentEdge> removedEdges;
	private Set<Feature> features;
	
	public AlignmentExpResult() {
		this.degreeDistribution = new TreeBag<Integer>();
		this.edgeWeightDistribution = new TreeBag<Double>();
		this.intensityByEdgeWeight = new HashMap<Double, Double>();
		this.groupSizeByEdgeWeight = new HashMap<Double, Integer>();
		this.alignmentPairs = new ArrayList<AlignmentPair>();
		this.alignmentEdges = new ArrayList<AlignmentEdge>();
		this.removedEdges = new ArrayList<AlignmentEdge>();
		this.features = new HashSet<Feature>();
	}

	public void increaseDegree(Integer degree) {
		this.degreeDistribution.add(degree, 1);
	}

	public void increaseEdgeWeight(Double weight) {
		this.edgeWeightDistribution.add(weight, 1);
	}
	
	public void increaseIntensity(double edgeWeight, double intensity) {
		Double currentIntensity = this.intensityByEdgeWeight.get(edgeWeight);
		if (currentIntensity != null) {
			double newIntensity = currentIntensity + intensity;
			this.intensityByEdgeWeight.put(edgeWeight, newIntensity);
		} else {
			this.intensityByEdgeWeight.put(edgeWeight, intensity);
		}
	}

	public void increaseGroupSize(double edgeWeight, int groupSize) {
		Integer currentGroupSize = this.groupSizeByEdgeWeight.get(edgeWeight);
		if (currentGroupSize != null) {
			int newGroupSize = currentGroupSize + groupSize;
			this.groupSizeByEdgeWeight.put(edgeWeight, newGroupSize);
		} else {
			this.groupSizeByEdgeWeight.put(edgeWeight, groupSize);
		}
	}
	
	public Bag<Integer> getDegreeDistribution() {
		return degreeDistribution;
	}
	
	public Bag<Double> getEdgeWeightDistribution() {
		return edgeWeightDistribution;
	}

	public Map<Double, Double> getIntensityByEdgeWeight() {
		return intensityByEdgeWeight;
	}	

	public Map<Double, Integer> getGroupSizeByEdgeWeight() {
		return groupSizeByEdgeWeight;
	}
	
	public List<AlignmentPair> getAlignmentPairs() {
		return alignmentPairs;
	}

	public List<AlignmentEdge> getAlignmentEdges() {
		return alignmentEdges;
	}
	
	public List<AlignmentEdge> getRemovedEdges() {
		return removedEdges;
	}	

	public List<AlignmentPair> getRemovedEdgesAlignmentPairs() {
		List<AlignmentPair> pairs = new ArrayList<AlignmentPair>();
		for (AlignmentEdge e : removedEdges) {
			pairs.addAll(e.getAlignmentPairs());
		}
		return pairs;
	}	
	
	public Set<Feature> getFeatures() {
		return features;
	}
	
	public void addDegreeDistribution(Bag<Integer> anotherBag) {
		this.degreeDistribution.addAll(anotherBag);
	}

	public void addEdgeWeightDistribution(Bag<Double> anotherBag) {
		this.edgeWeightDistribution.addAll(anotherBag);
	}
	
	public void addIntensityByEdgeWeight(Map<Double, Double> anotherMap) {
		for (Entry<Double, Double> entry : anotherMap.entrySet()) {
			increaseIntensity(entry.getKey(), entry.getValue());
		}
	}

	public void addGroupSizeByEdgeWeight(Map<Double, Integer> anotherMap) {
		for (Entry<Double, Integer> entry : anotherMap.entrySet()) {
			increaseGroupSize(entry.getKey(), entry.getValue());
		}
	}
	
	public void addAlignmentPairs(List<AlignmentPair> pairs) {
		this.alignmentPairs.addAll(pairs);
	}

	public void addAlignmentEdges(List<AlignmentEdge> edges) {
		this.alignmentEdges.addAll(edges);
	}

	public void addAlignmentEdge(AlignmentEdge edge) {
		this.alignmentEdges.add(edge);
	}

	public void addRemovedEdges(List<AlignmentEdge> edges) {
		this.removedEdges.addAll(edges);
	}

	public void addRemovedEdge(AlignmentEdge edge) {
		this.removedEdges.add(edge);
	}
	
	public void addFeatures(Set<Feature> features) {
		this.features.addAll(features);
	}
	
	public void collect(AlignmentExpResult iterResult) {
		this.addDegreeDistribution(iterResult.getDegreeDistribution());
		this.addEdgeWeightDistribution(iterResult.getEdgeWeightDistribution());
		this.addIntensityByEdgeWeight(iterResult.getIntensityByEdgeWeight());
		this.addGroupSizeByEdgeWeight(iterResult.getGroupSizeByEdgeWeight());
		this.addAlignmentPairs(iterResult.getAlignmentPairs());
		this.addAlignmentEdges(iterResult.getAlignmentEdges());
		this.addRemovedEdges(iterResult.getRemovedEdges());
		this.addFeatures(iterResult.getFeatures());
	}

	@Override
	public String toString() {
		return "AlignmentExpResult [degreeDistribution=" + degreeDistribution
				+ ", edgeWeightDistribution=" + edgeWeightDistribution + "]";
	}
	
}
