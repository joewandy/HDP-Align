package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.custom.FilePairKey;

public class ExtendedLibrary {

	private Map<FilePairKey, Set<ExtendedLibraryEntry>> filePairEntries;
	
	public ExtendedLibrary(double dmz, double drt) {
		filePairEntries = new HashMap<FilePairKey, Set<ExtendedLibraryEntry>>();
	}
				
	public void putEntry(Feature feature1, Feature feature2, double score, double weight) {
		ExtendedLibraryEntry newEntry = new ExtendedLibraryEntry(feature1, feature2, score, weight);
		AlignmentFile data1 = feature1.getData();
		AlignmentFile data2 = feature2.getData();
		FilePairKey pair = new FilePairKey(data1, data2);
		if (!filePairEntries.containsKey(pair)) {
			Set<ExtendedLibraryEntry> entries = new HashSet<ExtendedLibraryEntry>();
			entries.add(newEntry);
			filePairEntries.put(pair, entries);
		} else {
			Set<ExtendedLibraryEntry> entries = filePairEntries.get(pair);
			entries.add(newEntry);
		}
	}

	public double getScoresByFiles(AlignmentFile data1, AlignmentFile data2) {
		FilePairKey pair = new FilePairKey(data1, data2);
		Set<ExtendedLibraryEntry> entries = filePairEntries.get(pair);
		if (entries == null) {
			return 0;
		}
		double score = 0;
		for (ExtendedLibraryEntry entry : entries) {
			score += entry.getScore();
		}
//		score = score / entries.size();
		return score;
	}
			
}
