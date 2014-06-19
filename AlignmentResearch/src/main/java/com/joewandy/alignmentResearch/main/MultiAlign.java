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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureGrouper;
import com.joewandy.alignmentResearch.alignmentMethod.custom.ExtendedLibraryBuilder;
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.filter.ScoreResultFilter;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;


public class MultiAlign {
		
	private MultiAlignCmdOptions options;
	private AlignmentData data;
	private String method;
	private AlignmentMethodParam param;
	private List<AlignmentResultFilter> filters;
	
	private double massTolerance;
	private double rtTolerance;
	private double groupingRtWindow;
	private double alpha;
	
	private AlignmentMethod aligner;
	private ExtendedLibrary extendedLibrary;
		
	public MultiAlign(MultiAlignCmdOptions options, AlignmentData data) {

		this.options = options;
		this.data = data;
		this.method = options.method;
		this.massTolerance = options.alignmentPpm;
		this.rtTolerance = options.alignmentRtWindow;
		this.groupingRtWindow = options.groupingRtWindow;
		this.alpha = options.alpha;
		
		AlignmentMethodParam.Builder paramBuilder = new AlignmentMethodParam.Builder(
				options.alignmentPpm, options.alignmentRtWindow);
		paramBuilder.usePpm(MultiAlignConstants.ALIGN_BY_RELATIVE_MASS_TOLERANCE);
		paramBuilder.ransacRtToleranceBefore(options.ransacRtToleranceBeforeCorrection);
		paramBuilder.ransacRtToleranceAfter(options.alignmentRtWindow);
		paramBuilder.ransacIteration(options.ransacIteration);
		paramBuilder.ransacNMinPoints(options.ransacNMinPoints);
		paramBuilder.ransacThreshold(options.ransacThreshold);
		paramBuilder.ransacLinearModel(options.ransacLinearModel);
		paramBuilder.ransacSameChargeRequired(options.ransacSameChargeRequired);
		paramBuilder.openMsMzPairMaxDistance(options.openMsMzPairMaxDistance);
		paramBuilder.useGroup(options.useGroup);
		paramBuilder.exactMatch(options.exactMatch);		
		paramBuilder.usePeakShape(options.usePeakShape);
		paramBuilder.groupingMethod(options.groupingMethod);
		paramBuilder.groupingRtTolerance(options.groupingRtWindow);
		paramBuilder.alpha(options.alpha);
		this.param = paramBuilder.build();
				
		if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_HIERARCHICAL.equals(method)) {

			// cluster peaks within files
			if (param.isUseGroup()) {
				FeatureGrouper grouper = new FeatureGrouper(data.getAlignmentDataList(), param);
				grouper.groupFeatures();				
			}
			
			// build pairwise library
			ExtendedLibraryBuilder builder = new ExtendedLibraryBuilder(data.getAlignmentDataList(), param);		
			List<AlignmentLibrary> allLibraries = builder.buildPrimaryLibrary();
			extendedLibrary = builder.combineLibraries(allLibraries);			
			this.aligner = AlignmentMethodFactory.getAlignmentMethod(method, param, data, extendedLibrary);			

		} else {

			this.aligner = AlignmentMethodFactory.getAlignmentMethod(method, param, data, null);			
					
		}
		
		// add whatever you want here to filter the alignment results
		this.filters = new ArrayList<AlignmentResultFilter>();		
//		this.filters.add(new ScoreResultFilter(0.9, data));
		for (AlignmentResultFilter filter : filters) {
			aligner.addFilter(filter);							
		}

				
	}
			
	public EvaluationResult runExperiment() throws FileNotFoundException {
		
		// actually do the alignment now, filtering of alignment results also happen inside align()
		AlignmentList result = aligner.align();
		if (result != null) {
			System.out.println("Total " + result.getRowsCount() + " rows aligned");			
		}
		if (result != null) {
			writeAlignmentResult(result, options.output);
			EvaluationResult evalRes = evaluate(result, options.measureType);
			return evalRes;			
		} else {
			return null;
		}
		
	}
				
	private EvaluationResult evaluate(AlignmentList result, String measureType) {
		
		EvaluationResult evalRes = null;
		if (data.getGroundTruth() != null) {			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();
			if (measureType.equals(MultiAlignConstants.PERFORMANCE_MEASURE_LANGE)) {
				evalRes = gt.evaluateOld(Collections.unmodifiableList(result.getRows()), noOfFiles, 
						massTolerance, rtTolerance);								
			} else if (measureType.equals(MultiAlignConstants.PERFORMANCE_MEASURE_JOE)) {
				evalRes = gt.evaluateNew(Collections.unmodifiableList(result.getRows()), noOfFiles, 
						massTolerance, rtTolerance);												
			}
		}		
		evalRes.setTh(alpha);
		String note = alpha + ", " + groupingRtWindow;
		evalRes.setNote(note);
		
		System.out.println();
		System.out.println("******************************************************");
		System.out.println("evalRes method=" + method + " RT=" + evalRes.getDrt() + " F1=" + evalRes.getF1());
		System.out.println("******************************************************");
		System.out.println();
					
		// RetentionTimePrinter rtp = new RetentionTimePrinter();
		// rtp.printRt1(alignmentDataList.get(0), alignmentDataList.get(1));
		// rtp.printRt2(alignmentDataList.get(0), alignmentDataList.get(1), result);
				
		return evalRes;
		
	}		
	
	private void writeAlignmentResult(AlignmentList result, 
			String outputPath) throws FileNotFoundException {
		PrintStream alignmentOutput = System.out;
		if (outputPath != null) {
			System.out.println("Writing output");
			alignmentOutput = new PrintStream(new FileOutputStream(outputPath));
			List<AlignmentRow> rows = result.getRows();
			for (AlignmentRow row : rows) {
				alignmentOutput.println(printRow(row));
			}
		}
	}	
	
	private String printRow(AlignmentRow row) {
		StringBuilder sb = new StringBuilder();
		for (Feature feature : row.getFeatures()) {
			sb.append(printFeature(feature));			
		}
		return sb.toString();		
	}
	
	private String printFeature(Feature feature) {
		return feature.getIntensity() + " " + feature.getRt() + " " + feature.getMass() + " ";
	}
	
}