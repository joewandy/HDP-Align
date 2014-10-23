package com.joewandy.alignmentResearch.alignmentMethod.custom.maxWeight;

import java.util.List;
import java.util.Map;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.FeatureGroupingMethod;

public class HierarchicalAlignment extends MyMaximumMatchingAlignment implements AlignmentMethod {
	
	public HierarchicalAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param, ExtendedLibrary extendedLibrary) {		
		super(dataList, param);
		this.library = extendedLibrary;
	}
	
	@Override
	public AlignmentList matchFeatures() {
		
		System.out.println("ALIGNMENT");
		AlignmentList alignedList = new AlignmentList("");
		
		// using guide tree and maximum matching
		DendogramBuilder builder = new DendogramBuilder(dataList, 
				library, massTolerance, rtTolerance, useGroup, exactMatch, alpha);
		if (!dataList.isEmpty()) {
			alignedList = builder.align();			
		}
				
		return alignedList;
				
	}
		
}