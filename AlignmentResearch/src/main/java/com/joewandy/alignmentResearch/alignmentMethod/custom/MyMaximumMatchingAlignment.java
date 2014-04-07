package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.FeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.GreedyFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.MetAssignFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.SavedMatlabFeatureGroupingMethod;

public class MyMaximumMatchingAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;
	protected ExtendedLibrary library;
	protected boolean useGroup;
	protected boolean usePeakShape;
	protected String groupingMethod;
	protected double groupingRtWindow;
	protected double alpha;
	
	public MyMaximumMatchingAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		
		// do grouping before aligning ?
		useGroup = param.isUseGroup();
		usePeakShape = param.isUsePeakShape();
		groupingMethod = param.getGroupingMethod();
		groupingRtWindow = param.getGroupingRtTolerance();
		alpha = param.getAlpha();
		
	}
	
	public AlignmentList matchFeatures() {

		FeatureGroupingMethod groupingMethod = null;
		if (useGroup) {
			groupingMethod = groupFeatures();			
		}
		
		AlignmentList masterList = new AlignmentList("");	
		int counter = 0;
		for (AlignmentFile data : dataList) {			
			AlignmentList peakList = new AlignmentList(data);
			System.out.println("Aligning #" + (counter+1) + ": " + peakList);
			FeatureMatching matcher = new MaximumWeightMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
					library, massTolerance, rtTolerance, useGroup, alpha, groupingMethod);
			masterList = matcher.getMatchedList();			            
			counter++;
		}
		
		return masterList;
		
	}
	
	protected FeatureGroupingMethod groupFeatures() {
		
		FeatureGroupingMethod groupingMethod = getFeatureGroupingMethod();
	
		int counter = 0;
		for (AlignmentFile data : dataList) {
			AlignmentList peakList = new AlignmentList(data);
			if (groupingMethod != null) {
				System.out.println("Grouping #" + (counter+1) + ": " + peakList);
				groupingMethod.group(data);				
			}			
			counter++;
		}
		if (groupingMethod != null) {
			groupingMethod.close();			
		}
		
		return groupingMethod;
		
	}

	protected FeatureGroupingMethod getFeatureGroupingMethod() {
		FeatureGroupingMethod grouping = null;
		if (useGroup) {
			if (MultiAlign.GROUPING_METHOD_GREEDY.equals(groupingMethod)) {
				grouping = new GreedyFeatureGroupingMethod(groupingRtWindow, usePeakShape);	
			} else if (MultiAlign.GROUPING_METHOD_METASSIGN_MIXTURE.equals(groupingMethod) || 
					MultiAlign.GROUPING_METHOD_METASSIGN_POSTERIOR.equals(groupingMethod)) {
				grouping = new MetAssignFeatureGroupingMethod(groupingMethod, groupingRtWindow, usePeakShape);																			
			} else {
//				grouping = new MatlabFeatureGroupingMethod(groupingMethod, groupingRtWindow, 
//						MultiAlign.GROUPING_METHOD_ALPHA, MultiAlign.GROUPING_METHOD_NUM_SAMPLES, MultiAlign.GROUPING_METHOD_BURN_IN);															
				grouping = new SavedMatlabFeatureGroupingMethod(groupingMethod, usePeakShape);															
			}
		}
		return grouping;
	}
			
}
