package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uci.ics.jung.graph.Graph;

public class AlignmentLibrary {

	private int libraryID;
	private AlignmentFile data1;
	private AlignmentFile data2;

	private Map<AlignmentEdge, AlignmentEdge> entries;
	private double avgWeight;
	private List<ExtendedLibraryEntry> aligned;
	private Graph<AlignmentVertex, AlignmentEdge> graph;
	
	public AlignmentLibrary(int id, AlignmentFile data1, AlignmentFile data2) {
		
		this.libraryID = id;
		this.data1 = data1;
		this.data2 = data2;
		aligned = new ArrayList<ExtendedLibraryEntry>();
	
	}	
	
	public AlignmentLibrary(int id, AlignmentFile data1, AlignmentFile data2, List<AlignmentEdge> entryList, Graph<AlignmentVertex, AlignmentEdge> graph) {
		
		this.libraryID = id;
		this.data1 = data1;
		this.data2 = data2;
		this.graph = graph;
		
		entries = new HashMap<AlignmentEdge, AlignmentEdge>();
		aligned = new ArrayList<ExtendedLibraryEntry>();

		double total = 0;
		for (AlignmentEdge entry : entryList) {
			entries.put(entry, entry);			
			total += entry.getWeight();
		}
		avgWeight = total / entryList.size();
		
	}
	
	public void addEntry(AlignmentEdge entry) {
		entries.put(entry, entry);
	}
	
	public void addEntries(Map<AlignmentEdge, AlignmentEdge> newEntries) {
		entries.putAll(newEntries);
	}
	
	public AlignmentEdge getEntry(AlignmentEdge entry) {
		return entries.get(entry);
	}
	
	public boolean containsEntry(AlignmentEdge entry) {
		return entries.containsKey(entry);
	}
	
	public Map<AlignmentEdge, AlignmentEdge> getEntries() {
		return entries;
	}
	
	public Graph<AlignmentVertex, AlignmentEdge> getGraph() {
		return graph;
	}

	public void addAlignedPair(Feature f1, Feature f2, double score, double weight) {
		ExtendedLibraryEntry entry = new ExtendedLibraryEntry(f1, f2, score, weight);
		aligned.add(entry);
	}
		
	public int getAlignedPairCount() {
		return aligned.size();
	}
	
	public List<ExtendedLibraryEntry> getAlignedFeatures() {
		return this.aligned;
	}
	
	public List<ExtendedLibraryEntry> getAlignedFeaturesByFiles(AlignmentFile file1, AlignmentFile file2) {
		List<ExtendedLibraryEntry> result = new ArrayList<ExtendedLibraryEntry>();
		for (ExtendedLibraryEntry entry : this.aligned) {
			if (entry.getFeature1().getData() == file1 && entry.getFeature2().getData() == file2) {
				result.add(entry);
			}
		}
		return result;
	}

	public double getAvgWeight() {
		return avgWeight;
	}
	
	public int getLibraryID() {
		return libraryID;
	}

	public AlignmentFile getData1() {
		return data1;
	}

	public AlignmentFile getData2() {
		return data2;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + libraryID;
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
		AlignmentLibrary other = (AlignmentLibrary) obj;
		if (libraryID != other.libraryID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "[ID=" + libraryID + ", data1=" + data1.getFilenameWithoutExtension()
				+ ", data2=" + data2.getFilenameWithoutExtension() + "]";
	}
		
}