/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.joewandy.alignmentResearch.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.filter.GraphAlignmentResultFilter;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;
import com.joewandy.alignmentResearch.objectModel.FeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.GreedyFeatureGroupingMethod;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;
import com.joewandy.alignmentResearch.objectModel.SavedMatlabFeatureGroupingMethod;


public class MultiAlign {
		
	public static final boolean ALIGN_BY_RELATIVE_MASS_TOLERANCE = false;
	public static final boolean PARALLEL_LIBRARY_BUILD = false;
	
	public static final String GROUPING_METHOD_GREEDY = "greedy";
	public static final String GROUPING_METHOD_MIXTURE = "mixture";
	public static final String GROUPING_METHOD_POSTERIOR = "posterior";
	public static final double GROUPING_METHOD_RT_TOLERANCE = 5;
	public static final double GROUPING_METHOD_ALPHA = 1;
	public static final int GROUPING_METHOD_NUM_SAMPLES = 10;

	private AlignmentData data;
	private String method;
	private AlignmentMethodParam.Builder paramBuilder;
	private List<AlignmentResultFilter> filters;
	
	private double massTolerance;
	private double rtTolerance;
	private double ransacRtToleranceBeforeCorrection;

	private boolean useGroup;
	private String groupingMethod;
	private double groupingRtWindow;
	private double alpha;
		
	public MultiAlign(AlignmentData data, MultiAlignCmdOptions options) {

		this.data = data;
		this.method = options.method;
		this.massTolerance = options.alignmentPpm;
		this.rtTolerance = options.alignmentRtWindow;
		this.ransacRtToleranceBeforeCorrection = options.ransacRtToleranceBeforeCorrection;

		this.useGroup = options.useGroup;
		this.groupingMethod = options.groupingMethod;
		this.groupingRtWindow = options.groupingRtWindow;
		this.alpha = options.alpha;
		
		this.paramBuilder = new AlignmentMethodParam.Builder(
				options.alignmentPpm, options.alignmentRtWindow);
		this.paramBuilder.usePpm(MultiAlign.ALIGN_BY_RELATIVE_MASS_TOLERANCE);
		this.paramBuilder.ransacRtToleranceBefore(options.ransacRtToleranceBeforeCorrection);
		this.paramBuilder.ransacRtToleranceAfter(options.alignmentRtWindow);
		this.paramBuilder.ransacIteration(options.ransacIteration);
		this.paramBuilder.ransacNMinPoints(options.ransacNMinPoints);
		this.paramBuilder.ransacThreshold(options.ransacThreshold);
		this.paramBuilder.ransacLinearModel(options.ransacLinearModel);
		this.paramBuilder.ransacSameChargeRequired(options.ransacSameChargeRequired);
		this.paramBuilder.openMsMzPairMaxDistance(options.openMsMzPairMaxDistance);
		this.paramBuilder.useGroup(options.useGroup);
		this.paramBuilder.usePeakShape(options.usePeakShape);
		this.paramBuilder.groupingMethod(options.groupingMethod);
		this.paramBuilder.groupingRtTolerance(options.groupingRtWindow);
		this.paramBuilder.alpha(options.alpha);
		
		this.filters = new ArrayList<AlignmentResultFilter>();
		if (options.graphFilter != null) {
			// FIXME: maybe not correct to directly use alignmentPpm for mahalanobis distance calculation. Here we assume that it's an absolute value !
			AlignmentResultFilter graphFilter = new GraphAlignmentResultFilter(data.getAlignmentDataList(), 
					options.graphFilter, options.th, options.alignmentPpm, options.alignmentRtWindow);
			this.filters.add(graphFilter);
		}
		
	}
	
	public AlignmentList align() {

		// set filters if necessary
		AlignmentMethod aligner = AlignmentMethodFactory.getAlignmentMethod(method, paramBuilder, data);
		for (AlignmentResultFilter filter : filters) {
			aligner.addFilter(filter);							
		}
		
		// actually do the alignment now, filtering of alignment results also happen inside align()
		AlignmentList result = aligner.align();
		System.out.println("Total " + result.getRowsCount() + " rows aligned");
		return result;
				
	}
		
	public EvaluationResult evaluate(AlignmentList result) {

		// do performance evaluation
		EvaluationResult evalRes = null;
		if (data.getGroundTruth() != null) {			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();
			evalRes = gt.evaluateOld(Collections.unmodifiableList(result.getRows()), noOfFiles, 
					massTolerance, rtTolerance);				
		}		
		evalRes.setTh(alpha);
		evalRes.setDrtBefore(ransacRtToleranceBeforeCorrection);
		System.out.println(evalRes);
		
		// do performance evaluation
		evalRes = null;
		if (data.getGroundTruth() != null) {			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();
			evalRes = gt.evaluateNew(Collections.unmodifiableList(result.getRows()), noOfFiles, 
					massTolerance, rtTolerance);				
		}		
		evalRes.setTh(alpha);
		evalRes.setDrtBefore(ransacRtToleranceBeforeCorrection);
		System.out.println(evalRes);
					
		// RetentionTimePrinter rtp = new RetentionTimePrinter();
		// rtp.printRt1(alignmentDataList.get(0), alignmentDataList.get(1));
		// rtp.printRt2(alignmentDataList.get(0), alignmentDataList.get(1), result);
		
		return evalRes;
		
	}
		
	
}