package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.FeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.GreedyFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.MatlabFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.SavedMatlabFeatureGroupingMethod;

public class MyStableMarriageAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentFile> dataList;
	private ExtendedLibrary library;
	private boolean useGroup;
	private boolean usePeakShape;
	private String groupingMethod;
	private double groupingRtWindow;
	private double alpha;
	
	public MyStableMarriageAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		
		// do grouping before aligning ?
		useGroup = param.isUseGroup();
		useGroup = param.isUsePeakShape();
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
			FeatureMatching matcher = new StableMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
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
			if (MultiAlign.GROUPING_METHOD_GREEDY.equals(groupingMethod)) {
				grouping = new GreedyFeatureGroupingMethod(groupingRtWindow, usePeakShape);	
			} else {
//				grouping = new MatlabFeatureGroupingMethod(groupingMethod, groupingRtWindow, 
//						MultiAlign.GROUPING_METHOD_ALPHA, MultiAlign.GROUPING_METHOD_NUM_SAMPLES);															
				grouping = new SavedMatlabFeatureGroupingMethod(groupingMethod);															
			}
		}
		return grouping;
	}
	
	private void savePairProbs(Map<FeaturePairKey, Double> pairProbs,
			AlignmentFile data) {
		
		System.out.println("BEFORE pairProbs.size() = " + pairProbs.size());
//		DoubleMatrix zzProb = data.getZZProb();
//		for (int i = 0; i < zzProb.rows; i++) {
//			for (int j = 0; j < zzProb.columns; j++) {
		Matrix zzProb = data.getZZProb();
		for (int i = 0; i < zzProb.numRows(); i++) {
			for (int j = 0; j < zzProb.numColumns(); j++) {				
				double prob = zzProb.get(i, j);
				if (prob > 0) {
					Feature f1 = data.getFeatureByIndex(i);
					Feature f2 = data.getFeatureByIndex(j);
					FeaturePairKey pair = new FeaturePairKey(f1, f2);
					pairProbs.put(pair, prob);
				}
			}
		}
		System.out.println("AFTER pairProbs.size() = " + pairProbs.size());
	}
		
}
