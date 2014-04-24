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
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;


public class MultiAlign {
		
	private MultiAlignCmdOptions options;
	private AlignmentData data;
	private String method;
	private AlignmentMethodParam.Builder paramBuilder;
	private List<AlignmentResultFilter> filters;
	
	private double massTolerance;
	private double rtTolerance;

	private double groupingRtWindow;
	private double alpha;
		
	public MultiAlign(MultiAlignCmdOptions options, AlignmentData data) {

		this.options = options;
		this.data = data;
		this.method = options.method;
		this.massTolerance = options.alignmentPpm;
		this.rtTolerance = options.alignmentRtWindow;
		this.groupingRtWindow = options.groupingRtWindow;
		this.alpha = options.alpha;
		
		this.paramBuilder = new AlignmentMethodParam.Builder(
				options.alignmentPpm, options.alignmentRtWindow);
		setToolParams(options);
		
		// add whatever you want here to filter the alignment results
		this.filters = new ArrayList<AlignmentResultFilter>();
		
	}
	
	public EvaluationResult runExperiment() throws FileNotFoundException {
		
		AlignmentList result = align(false);
		if (result != null) {
			writeAlignmentResult(result, options.output);
			EvaluationResult evalRes = evaluate(result);
			return evalRes;			
		} else {
			return null;
		}
		
	}
	
	private AlignmentList align(boolean silent) {

		// set filters if necessary
		AlignmentMethod aligner = AlignmentMethodFactory.getAlignmentMethod(method, paramBuilder, data);
		for (AlignmentResultFilter filter : filters) {
			aligner.addFilter(filter);							
		}
		aligner.setSilentMode(silent);
		
		// actually do the alignment now, filtering of alignment results also happen inside align()
		AlignmentList result = aligner.align();
		if (result != null) {
			System.out.println("Total " + result.getRowsCount() + " rows aligned");			
		}
		return result;
				
	}
		
	private EvaluationResult evaluate(AlignmentList result) {
		
		// do performance evaluation - OLD
		EvaluationResult evalRes = null;
		if (data.getGroundTruth() != null) {			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();
			evalRes = gt.evaluateOld(Collections.unmodifiableList(result.getRows()), noOfFiles, 
					massTolerance, rtTolerance);				
		}		
		evalRes.setTh(alpha);
		String note = alpha + ", " + groupingRtWindow;
		evalRes.setNote(note);
		
		// do performance evaluation - NEW
		evalRes = null;
		if (data.getGroundTruth() != null) {			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();
			evalRes = gt.evaluateNew(Collections.unmodifiableList(result.getRows()), noOfFiles, 
					massTolerance, rtTolerance);				
		}		
		evalRes.setTh(alpha);
		note = alpha + ", " + groupingRtWindow;
		evalRes.setNote(note);
		
		System.out.println("evalRes RT=" + evalRes.getDrt() + " F1=" + evalRes.getF1());
					
		// RetentionTimePrinter rtp = new RetentionTimePrinter();
		// rtp.printRt1(alignmentDataList.get(0), alignmentDataList.get(1));
		// rtp.printRt2(alignmentDataList.get(0), alignmentDataList.get(1), result);
				
		return evalRes;
		
	}		
		
	private void setToolParams(MultiAlignCmdOptions options) {
		this.paramBuilder.usePpm(MultiAlignConstants.ALIGN_BY_RELATIVE_MASS_TOLERANCE);
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