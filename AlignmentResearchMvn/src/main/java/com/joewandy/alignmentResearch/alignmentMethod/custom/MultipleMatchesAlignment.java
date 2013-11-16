package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class MultipleMatchesAlignment extends BaseAlignment implements AlignmentMethod {

	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public MultipleMatchesAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
	}
	
	@Override
	protected AlignmentList matchFeatures() {
			
		AlignmentList alignedList = new AlignmentList("");
		
		int rowId = 0;
		for (int i = 0; i < dataList.size(); i++) {

			AlignmentFile data = dataList.get(i);

			for (Feature feature : data.getFeatures()) {

				// process features
				AlignmentRow row = new AlignmentRow(alignedList, rowId);
				Set<Feature> nearbyFeatures = findMatchingFeatures(i, feature);
				nearbyFeatures.add(feature); // remember to add this current feature too
				row.addAlignedFeatures(nearbyFeatures);
				rowId++;
				alignedList.addRow(row);
				
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
			// find ALL features within tolerance
			AlignmentFile data = dataList.get(j);
			Set<Feature> featuresInRange = data.getNextFeatures(referenceFeature, 
					this.massTolerance, this.rtTolerance, this.usePpm);
			nearbyFeatures.addAll(featuresInRange);
		}
		return nearbyFeatures;
	}
	
}
