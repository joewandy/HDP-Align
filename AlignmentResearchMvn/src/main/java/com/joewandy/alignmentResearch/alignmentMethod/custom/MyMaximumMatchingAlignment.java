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
import com.joewandy.alignmentResearch.objectModel.SavedMatlabFeatureGroupingMethod;

public class MyMaximumMatchingAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentFile> dataList;
	private ExtendedLibrary library;
	private boolean useGroup;
	private String groupingMethod;
	private double groupingRtWindow;
	private double alpha;
	
	public MyMaximumMatchingAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		
		// do grouping before aligning ?
		useGroup = param.isUseGroup();
		groupingMethod = param.getGroupingMethod();
		groupingRtWindow = param.getGroupingRtTolerance();
		alpha = param.getAlpha();
		
	}
	
	public AlignmentList matchFeatures() {

		AlignmentList masterList = new AlignmentList("");
		FeatureGroupingMethod groupingMethod = getFeatureGroupingMethod();
	
		int counter = 0;
		for (AlignmentFile data : dataList) {
			
			AlignmentList peakList = new AlignmentList(data);

			System.out.println("Grouping #" + (counter+1) + ": " + peakList);
			if (groupingMethod != null) {
				groupingMethod.group(data);				
			}
			
			System.out.println("Aligning #" + (counter+1) + ": " + peakList);
			FeatureMatching matcher = new MaximumWeightMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
					library, massTolerance, rtTolerance, useGroup, alpha);
			masterList = matcher.getMatchedList();			
            
			counter++;
			
		}
		if (groupingMethod != null) {
			groupingMethod.close();			
		}
		
		return masterList;
		
	}

	private FeatureGroupingMethod getFeatureGroupingMethod() {
		FeatureGroupingMethod grouping = null;
		if (useGroup) {
			if (MultiAlign.GROUPING_METHOD_GREEDY_RT.equals(groupingMethod)) {
				grouping = new GreedyFeatureGroupingMethod(groupingRtWindow);	
			} else {
	//			grouping = new MatlabFeatureGrouping(options.groupingRtWindow, 
	//					options.groupingAlpha, options.groupingNSamples);															
				grouping = new SavedMatlabFeatureGroupingMethod(groupingMethod);															
			}
		}
		return grouping;
	}
			
}
