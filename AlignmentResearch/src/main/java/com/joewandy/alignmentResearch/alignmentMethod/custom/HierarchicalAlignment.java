package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.List;
import java.util.Map;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.util.GraphEdgeConstructor;

public class HierarchicalAlignment extends MyMaximumMatchingAlignment implements AlignmentMethod {

	private boolean useGroup;
	private double alpha;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public HierarchicalAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
		this.useGroup = param.isUseGroup();
		this.alpha = param.getAlpha();
	}
	
	@Override
	public AlignmentList matchFeatures() {

		if (useGroup) {
			groupFeatures();
		}	
		
		ExtendedLibraryBuilder builder = new ExtendedLibraryBuilder(dataList, massTolerance, rtTolerance);		
		Map<Double, List<AlignmentLibrary>> metaLibraries = builder.buildPrimaryLibrary();
		ExtendedLibrary extendedLibrary = builder.combineLibraries(metaLibraries);
//		ExtendedLibrary extendedLibrary = builder.extendLibrary(builder.combineLibraries(metaLibraries));

		System.out.println("ALIGNMENT");
		AlignmentList alignedList = align(extendedLibrary);

		return alignedList;
				
	}

	private AlignmentList align(ExtendedLibrary extendedLibrary) {

		AlignmentList alignedList = new AlignmentList("");
		
		// using guide tree and maximum matching
		DendogramBuilder builder = new DendogramBuilder(dataList, 
				extendedLibrary, massTolerance, rtTolerance, useGroup, alpha);
		alignedList = builder.align();
				
		return alignedList;

	}
		
}