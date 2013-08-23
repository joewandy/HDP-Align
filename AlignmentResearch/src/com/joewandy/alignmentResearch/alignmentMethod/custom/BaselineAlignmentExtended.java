package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class BaselineAlignmentExtended extends BaselineAlignment implements AlignmentMethod {

	private AlignmentList alignedList;
	private ExtendedLibrary extendedLibrary;
	private int windowMultiply;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public BaselineAlignmentExtended(List<AlignmentFile> dataList, double massTolerance, double rtTolerance, 
			ExtendedLibrary extendedLibrary, int windowMultiply) {		
		super(dataList, massTolerance, rtTolerance);
		this.extendedLibrary = extendedLibrary;
		this.windowMultiply = windowMultiply;
		this.alignedList = new AlignmentList("");
	}
	
	public AlignmentList getAlignedList() {
		return this.alignedList;
	}
	
	@Override
	protected AlignmentList matchFeatures() {
		
		for (AlignmentFile data : dataList) {
			// order features by intensity
			data.sortFeatures();
		}		
		
		int rowId = 0;
		for (int i = 0; i < dataList.size(); i++) {

			AlignmentFile data = dataList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + data);

			for (Feature feature : data.getFeatures()) {

				// process unaligned features
				if (!feature.isAligned()) {
					AlignmentRow row = new AlignmentRow(rowId);
					Set<Feature> nearbyFeatures = findMatchingFeatures(i, feature);
					nearbyFeatures.add(feature); // remember to add this current feature too
					row.addAlignedFeatures(nearbyFeatures);
					rowId++;
					alignedList.addRow(row);
				}
				
			}			
		}
		
		return alignedList;
				
	}
	
	protected Set<Feature> findMatchingFeatures(int i, Feature referenceFeature) {
		Set<Feature> nearbyFeatures = new HashSet<Feature>();
		for (int j = 0; j < dataList.size(); j++) {
			// match against every other file except ourselves
			if (i == j) {
				continue;
			}
			// find unmatched features within tolerance
			AlignmentFile data = dataList.get(j);
			Set<Feature> unmatched = data.getNextUnalignedFeatures(referenceFeature, 
					this.massTolerance, this.rtTolerance*windowMultiply, this.usePpm);
			if (!unmatched.isEmpty()) {
				// if we found several matches, take the one closest in mass to reference feature
				Feature closest = findClosestFeature(referenceFeature, unmatched);
				if (closest == null) {
					continue;
				}
				nearbyFeatures.add(closest);
			} 
		}
		return nearbyFeatures;
	}

	protected Feature findClosestFeature(Feature feature,
			Set<Feature> nearbyFeatures) {
		
		double minScore = Double.MIN_VALUE;
		Feature closest = null;
		for (Feature neighbour : nearbyFeatures) {
			double score = extendedLibrary.getEntryScore(feature, neighbour);
			if (score > minScore) {
				closest = neighbour;
				minScore = score;
			}
		}
		return closest;
		
	}
	
}
