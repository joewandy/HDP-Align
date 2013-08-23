package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExtendedLibrary {

	Map<ExtendedLibraryEntry, ExtendedLibraryEntry> entries;
	
	public ExtendedLibrary() {
		this.entries = new HashMap<ExtendedLibraryEntry, ExtendedLibraryEntry>();
	}
	
	// copy constructor
	public ExtendedLibrary(ExtendedLibrary library) {
		this.entries = new HashMap<ExtendedLibraryEntry, ExtendedLibraryEntry>();
		add(library);
	}
	
	public void add(ExtendedLibrary library) {
		Set<ExtendedLibraryEntry> entries = library.getEntries();
		for (ExtendedLibraryEntry entry : entries) {
			this.putEntry(entry.getFeature1(), entry.getFeature2(), entry.getScore(), entry.getWeight());
		}		
	}

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
	
	public double getScoreByFiles(AlignmentFile file1, AlignmentFile file2) {
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

		Set<Feature> features1 = row1.getFeatures();
		Set<Feature> features2 = row2.getFeatures();
		
		int counter = 0;
		double score = 0;
		for (Feature f1 : features1) {
			for (Feature f2 : features2) {
				if (exist(f1, f2)) {
					score += getEntryScore(f1, f2);
					counter++;
				}
			}
		}

		if (counter != 0) {
			score = score / counter;
			return score;			
		} else {
			return 0;
		}
	
	}
	
	public double computeWeightedRowScore(AlignmentRow row1, AlignmentRow row2) {

		Set<Feature> features1 = row1.getFeatures();
		Set<Feature> features2 = row2.getFeatures();
		
		int counter = 0;
		double score = 0;
		for (Feature f1 : features1) {
			for (Feature f2 : features2) {
				if (exist(f1, f2)) {
					double weight = getEntryWeight(f1, f2);
					// TODO: swap between addition and multiplication here
					score += getEntryScore(f1, f2) * weight;
					counter++;
				}
			}
		}

		if (counter != 0) {
			score = score / counter;
			return score;			
		} else {
			return 0;
		}
	
	}	

}
