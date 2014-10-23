package com.joewandy.alignmentResearch.alignmentMethod.custom.maxWeight;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.FeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.GreedyFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.MetAssignFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.SavedMatlabFeatureGroupingMethod;

public class MyMaximumMatchingAlignment extends BaseAlignment implements AlignmentMethod {

	protected ExtendedLibrary library;	
	protected boolean useGroup;
	protected boolean exactMatch;
	protected double alpha;	
	
	public MyMaximumMatchingAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		this.useGroup = param.isUseGroup();
		this.exactMatch = param.isExactMatch();
		this.alpha = param.getAlpha();		

	}
	
	public AlignmentList matchFeatures() {
		
		AlignmentList masterList = new AlignmentList("");	
		boolean quiet = true;
		for (AlignmentFile data : dataList) {			
			AlignmentList peakList = new AlignmentList(data);
//			System.out.println("Aligning #" + (counter+1) + ": " + peakList);
			FeatureMatching matcher = new MaximumWeightMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
					massTolerance, rtTolerance, useGroup, exactMatch, alpha, quiet);
			masterList = matcher.getMatchedList();			            
		}
		
		return masterList;
		
	}
				
}
