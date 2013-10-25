package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class MyStableMarriageAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentList> featureList;
	private ExtendedLibrary library;
	private double alpha;
	
	public MyStableMarriageAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param, 
			boolean useWeightedScore) {

		super(dataList, param);
		
		featureList = new ArrayList<AlignmentList>();
		for (AlignmentFile data : dataList) {
			// construct list of features in rows, based on data
			AlignmentList newList = new AlignmentList(data);
			featureList.add(newList);
		}
		alpha = param.getAlpha();
		
	}
	
	public AlignmentList matchFeatures() {

		AlignmentList masterList = new AlignmentList("");

		for (int i = 0; i < featureList.size(); i++) {

			AlignmentList peakList = featureList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + peakList);

			FeatureMatching matcher = new StableMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
					library, massTolerance, rtTolerance, alpha);
			masterList = matcher.getMatchedList();			
            
		}
		
		return masterList;
		
	}
		
}
