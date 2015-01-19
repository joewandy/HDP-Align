package com.joewandy.alignmentResearch.alignmentMethod;

import java.util.List;

import com.joewandy.alignmentResearch.grouping.FeatureGroupingMethod;
import com.joewandy.alignmentResearch.grouping.GreedyFeatureGroupingMethod;
import com.joewandy.alignmentResearch.grouping.MatlabFeatureGroupingMethod;
import com.joewandy.alignmentResearch.grouping.MetAssignFeatureGroupingMethod;
import com.joewandy.alignmentResearch.grouping.SavedMatlabFeatureGroupingMethod;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.AlignmentList;

public class FeatureGrouper  {

	protected List<AlignmentFile> dataList;	
	
	protected boolean usePeakShape;
	protected String groupingMethod;
	protected double groupingRtWindow;
	protected double minCorrSignal;
	
	public FeatureGrouper(List<AlignmentFile> dataList, AlignmentMethodParam param) {		

		this.dataList = dataList;			
		this.usePeakShape = param.isUsePeakShape();
		this.groupingMethod = param.getGroupingMethod();
		this.groupingRtWindow = param.getGroupingRtTolerance();
		this.minCorrSignal = param.getMinCorrSignal();
	
	}
		
	public FeatureGroupingMethod groupFeatures() {
		
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
		if (MultiAlignConstants.GROUPING_METHOD_GREEDY.equals(groupingMethod)) {
			grouping = new GreedyFeatureGroupingMethod(groupingRtWindow, usePeakShape, minCorrSignal);	
		} else if (MultiAlignConstants.GROUPING_METHOD_METASSIGN_MIXTURE.equals(groupingMethod) || 
				MultiAlignConstants.GROUPING_METHOD_METASSIGN_POSTERIOR.equals(groupingMethod)) {
			grouping = new MetAssignFeatureGroupingMethod(groupingMethod, groupingRtWindow, usePeakShape);																			
		} else if (MultiAlignConstants.GROUPING_METHOD_POSTERIOR.equals(groupingMethod)) {
			grouping = new SavedMatlabFeatureGroupingMethod(groupingMethod, usePeakShape);															
		} else if (MultiAlignConstants.GROUPING_METHOD_MATLAB_POSTERIOR.equals(groupingMethod)) {
			grouping = new MatlabFeatureGroupingMethod(groupingMethod, groupingRtWindow, 
					MultiAlignConstants.GROUPING_METHOD_ALPHA, MultiAlignConstants.GROUPING_METHOD_NUM_SAMPLES, 
					MultiAlignConstants.GROUPING_METHOD_BURN_IN);															
		}
		return grouping;
	}	
	
}
