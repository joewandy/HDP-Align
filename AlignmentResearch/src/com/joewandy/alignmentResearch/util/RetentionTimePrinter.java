package com.joewandy.alignmentResearch.util;

import java.util.List;

import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class RetentionTimePrinter {

	public void printRt1(AlignmentFile firstFile, AlignmentFile secondFile) {

		System.out.println("Printing retention times for file 1...");
		System.out.println("rt, group");
		List<Feature> features1 = firstFile.getFeatures();
		for (Feature f : features1) {
			System.out.println(f.getRt() + ", " + f.getFirstGroup().getFeatureCount());
		}

		System.out.println();
		System.out.println("Printing retention times for file 2...");
		System.out.println("rt, group");
		List<Feature> features2 = secondFile.getFeatures();
		for (Feature f : features2) {
			System.out.println(f.getRt() + ", " + f.getFirstGroup().getFeatureCount());
		}

	}

	public void printRt2(AlignmentFile firstFile, AlignmentFile secondFile, List<AlignmentRow> result, double dmz, double drt) {

		System.out.println("Printing retention times ...");
		System.out.println("rt1, group1, rt2, group2");
		for (AlignmentRow row : result) {
			Feature feature1 = row.getFeaturesFromFile(firstFile.getFilenameWithoutExtension());
			Feature feature2 = row.getFeaturesFromFile(secondFile.getFilenameWithoutExtension());
			if (feature1 != null && feature2 != null) {
				AlignmentPair aligned = new AlignmentPair(feature1, feature2, dmz, drt, null);
				aligned.print();
			}
		}

	}
	
}
