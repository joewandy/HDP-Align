package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
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

public class BaselineAlignment extends BaseAlignment implements AlignmentMethod {

	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public BaselineAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
	}
	
	@Override
	protected AlignmentList matchFeatures() {
		
		// save to csv file for debugging
		for (AlignmentFile data : dataList) {
			PrintWriter out = null;
			try {
				out = new PrintWriter("/home/joewandy/"+ 
						data.getFilenameWithoutExtension() + ".csv");
				out.println(Feature.csvHeader());
				for (Feature feature : data.getFeatures()) {
					out.println(feature.csvForm());
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}
		}
		
		AlignmentList alignedList = new AlignmentList("");
		
		int rowId = 0;
		for (int i = 0; i < dataList.size(); i++) {

			AlignmentFile data = dataList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + data);

			for (Feature feature : data.getSortedFeatures()) {

				// process unaligned features
				if (!feature.isAligned()) {
					AlignmentRow row = new AlignmentRow(alignedList, rowId);
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
					this.massTolerance, this.rtTolerance, this.usePpm);
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
		
		double minDiff = Double.MIN_VALUE;
		Feature closest = null;
		for (Feature neighbour : nearbyFeatures) {
			double featureMz = feature.getMass();
			double neighbourMz = neighbour.getMass();
			double diff = Math.abs(featureMz - neighbourMz);
			if (diff > minDiff) {
				closest = neighbour;
				minDiff = diff;
			}
		}
		return closest;
		
	}
	
}
