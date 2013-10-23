package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import peakml.chemistry.PeriodicTable;

import com.joewandy.alignmentResearch.alignmentMethod.custom.FeaturePairKey;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class ExtendedLibrary {

	private Map<ExtendedLibraryEntry, ExtendedLibraryEntry> entries;
	private Graph<AlignmentVertex, AlignmentEdge> graph;
	private MatchingScorer scorer;
	
	public ExtendedLibrary(double dmz, double drt) {
		this.entries = new HashMap<ExtendedLibraryEntry, ExtendedLibraryEntry>();
		this.graph = new UndirectedSparseGraph<AlignmentVertex, AlignmentEdge>();
		this.scorer =  new MatchingScorer(dmz, drt);
	}
	
	// copy constructor
	public ExtendedLibrary(ExtendedLibrary library, double dmz, double drt) {
		this.entries = new HashMap<ExtendedLibraryEntry, ExtendedLibraryEntry>();
		this.graph = new UndirectedSparseGraph<AlignmentVertex, AlignmentEdge>();
		this.scorer =  new MatchingScorer(dmz, drt);		
		add(library);
	}
	
	public void add(ExtendedLibrary library) {
		Set<ExtendedLibraryEntry> entries = library.getEntries();
		for (ExtendedLibraryEntry entry : entries) {
			this.putEntry(entry.getFeature1(), entry.getFeature2(), entry.getScore(), entry.getWeight());
		}		
	}
	
	public void add(Graph<AlignmentVertex, AlignmentEdge> newGraph) {
		List<AlignmentVertex> allVertices = new ArrayList<AlignmentVertex>(newGraph.getVertices());
		List<AlignmentEdge> allEdges = new ArrayList<AlignmentEdge>(newGraph.getEdges());
		for (AlignmentVertex v : allVertices) {
			this.graph.addVertex(v);
		}
		for (AlignmentEdge e : allEdges) {
			this.graph.addEdge(e, e.getLeft(), e.getRight());
		}
	}
		
//	public AlignmentVertex findVertex(Feature f) {
//		for (AlignmentVertex v : this.graph.getVertices()) {
//			if (v.getFeatures().contains(f)) {
//				return v;
//			}
//		}
//		return null;
//	}

	public void putEntry(Feature feature1, Feature feature2, double score, double weight) {
		ExtendedLibraryEntry existing = this.getEntry(feature1, feature2);
		if (existing == null) {
			// put as new entry if it doesn't exist yet
			ExtendedLibraryEntry newEntry = new ExtendedLibraryEntry(feature1, feature2, score, weight);
			entries.put(newEntry, newEntry);
		} else {
			// otherwise just add the score & weight to existing entry
			existing.increaseScore(score);
			existing.increaseWeight(weight);
		}
	}
	
	public ExtendedLibraryEntry getEntry(Feature feature1, Feature feature2) {
		ExtendedLibraryEntry entry1 = new ExtendedLibraryEntry(feature1, feature2);
		ExtendedLibraryEntry entry2 = new ExtendedLibraryEntry(feature2, feature1);
		if (entries.get(entry1) != null) {
			return entries.get(entry1);
		} if (entries.get(entry2) != null) { 
			return entries.get(entry2);
		} else {
			return null;
		}		
	}
	
	public double getMaxWeight() {
		double maxWeight = 0;
		for (Entry<ExtendedLibraryEntry, ExtendedLibraryEntry> entry : this.entries.entrySet()) {
			ExtendedLibraryEntry value = entry.getValue();
			if (value.getWeight() > maxWeight) {
				maxWeight = value.getWeight();
			}
		}
		return maxWeight;
	}

	public double getMinWeight() {
		double minWeight = Double.MAX_VALUE;
		for (Entry<ExtendedLibraryEntry, ExtendedLibraryEntry> entry : this.entries.entrySet()) {
			ExtendedLibraryEntry value = entry.getValue();
			if (value.getWeight() < minWeight) {
				minWeight = value.getWeight();
			}
		}
		return minWeight;
	}

//	public Set<ExtendedLibraryEntry> getNearbyPairs(Feature f1, Feature f2, double dmz, double drt) {
//		Set<ExtendedLibraryEntry> result = new HashSet<ExtendedLibraryEntry>();
//		Set<ExtendedLibraryEntry> entries = this.getEntriesByFiles(f1.getData(), f2.getData());
//		for (ExtendedLibraryEntry entry : entries) {
//			Feature c1 = entry.getFeature1();
//			Feature c2 = entry.getFeature2();
//			boolean inRange = false;
//			if (checkInMassRange(f1, c1, dmz) && checkInMassRange(f2, c2, dmz)) {
//				inRange = true;
//			} else if (checkInMassRange(f1, c2, dmz) && checkInMassRange(f2, c1, dmz)) {
//				inRange = true;
//			}
//			if (inRange) {
//				result.add(entry);
//			}
//		}
//		return result;
//	}	
	
	public boolean exist(Feature feature1, Feature feature2) {
		if (getEntry(feature1, feature2) != null) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean exist(AlignmentRow row1, AlignmentRow row2) {
		
		Set<Feature> features1 = row1.getFeatures();
		Set<Feature> features2 = row2.getFeatures();
		
		for (Feature f1 : features1) {
			for (Feature f2 : features2) {
				if (exist(f1, f2)) {
					return true;
				}
			}
		}
		
		return false;
		
	}
	
	public double getEntryScore(Feature feature1, Feature feature2) {
		ExtendedLibraryEntry entry = this.getEntry(feature1, feature2);
		if (entry != null) {
			return entry.getScore();
		} else {
			return 0;
		}
	}

	public double getEntryWeight(Feature feature1, Feature feature2) {
		ExtendedLibraryEntry entry = this.getEntry(feature1, feature2);
		if (entry != null) {
			return entry.getWeight();
		} else {
			return 0;
		}
	}
		
	public Set<ExtendedLibraryEntry> getEntriesByFiles(AlignmentFile file1, AlignmentFile file2) {
		Set<ExtendedLibraryEntry> result = new HashSet<ExtendedLibraryEntry>();
		for (ExtendedLibraryEntry entry : entries.values()) {
			AlignmentFile check1 = entry.getFeature1().getData();
			AlignmentFile check2 = entry.getFeature2().getData();
			if (check1 == file1 && check2 == file2) {
				result.add(entry);
			} else if (check1 == file2 && check1 == file1) {
				result.add(entry);
			}
		}
		return result;
	}	
	
	public double getScoresByFiles(AlignmentFile file1, AlignmentFile file2) {
		Set<ExtendedLibraryEntry> entries = this.getEntriesByFiles(file1, file2);
		double score = 0;
		for (ExtendedLibraryEntry entry : entries) {
			score += entry.getScore();
		}
		score /= entries.size();
		return score;
	}
		
	public int getEntrySize() {
		return this.entries.size();
	}
	
	public Set<ExtendedLibraryEntry> getEntries() {
		return this.entries.keySet();
	}

	public double computeRowScore(AlignmentRow row1, AlignmentRow row2) {
		return scorer.computeScore(row1, row2);
	}
		
	public double computeWeightedRowScore(AlignmentRow row1, AlignmentRow row2) {

//		Set<Feature> features1 = row1.getFeatures();
//		Set<Feature> features2 = row2.getFeatures();
//		double allScore = 0;
//		int counter = 0;
//		for (Feature fi : features1) {
//			for (Feature fj : features2) {
//				if (!FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
//					double weightedScore = scorer.computeGraphScore(fi, fj, graph);
//					allScore += weightedScore;					
//				} else {
//					double weightedScore = scorer.computeProbScore(fi, fj);
//					allScore += weightedScore;					
//				}
//				counter++;
//			}
//		}
//		return allScore / counter;				

		return scorer.computeScore(row1, row2);
		
	}

	public Graph<AlignmentVertex, AlignmentEdge> getGraph() {
		return graph;
	}

	public MatchingScorer getScorer() {
		return scorer;
	}	
			
}
