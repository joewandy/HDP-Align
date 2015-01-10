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
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureGrouper;
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.main.experiment.MultiAlignExpResult;
import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;
import com.joewandy.alignmentResearch.model.EvaluationResult;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.GroundTruth;


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
		
	public MultiAlign(MultiAlignCmdOptions options, AlignmentData data) {

		this.options = options;
		this.data = data;
		this.method = options.method;
		this.massTolerance = options.alignmentMzTol;
		this.rtTolerance = options.alignmentRtTol;
		this.groupingRtWindow = options.groupingRtWindow;
		this.alpha = options.alpha;		
		this.param = new AlignmentMethodParam(options);
				
		if (AlignmentMethodFactory.ALIGNMENT_METHOD_GROUP_ONLY.equals(method)) {

			// cluster peaks within files
			if (param.isUseGroup()) {
				FeatureGrouper grouper = new FeatureGrouper(data.getAlignmentDataList(), param);
				grouper.groupFeatures();				
			}

		} else {

			this.aligner = AlignmentMethodFactory.getAlignmentMethod(method, param, data);			
					
		}
				
	}
			
	public EvaluationResult runExperiment() throws FileNotFoundException {
		
		if (aligner == null) {
			return null;
		}
		
		// actually do the alignment now, filtering of alignment results also happen inside align()
		AlignmentList result = aligner.align();
		if (result != null && options.verbose) {
			System.out.println("Total " + result.getRowsCount() + " rows aligned");			
		}
		if (result != null) {
			System.out.println("Writing alignment results to " + options.output);
			writeAlignmentResult(result, options.output);
			System.out.println("Computing performance evaluation");
			EvaluationResult evalRes = evaluate(result, options.measureType);
			return evalRes;			
		} else {
			return null;
		}
		
	}
	
	public MultiAlignExpResult runPRExperiment() throws FileNotFoundException {

		MultiAlignExpResult expResult = new MultiAlignExpResult("");						
		AlignmentList result = aligner.align();
		if (result != null && options.verbose) {
			System.out.println("Total " + result.getRowsCount() + " rows aligned");			
		}
		if (result != null) {
			System.out.println("Computing performance evaluation");
			EvaluationResult evalRes = null;
			double lastThreshold = 0;
			do {
				evalRes = evaluatePR(result, options.measureType, lastThreshold);
				if (evalRes != null) {
					expResult.addResult(evalRes);
					lastThreshold = evalRes.getTh();					
				}
			} while (evalRes != null);
			
		}
		return expResult;
		
	}
	
	public void addResultFilter(AlignmentResultFilter filter) {
		aligner.addFilter(filter);							
	}
				
	private EvaluationResult evaluate(AlignmentList result, String measureType) {
		
		EvaluationResult evalRes = null;
		if (data.getGroundTruth() != null) {			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();
			if (measureType.equals(MultiAlignConstants.PERFORMANCE_MEASURE_LANGE)) {
				evalRes = gt.evaluateLange(Collections.unmodifiableList(result.getRows()), noOfFiles, 
						massTolerance, rtTolerance);								
			} else if (measureType.equals(MultiAlignConstants.PERFORMANCE_MEASURE_COMBINATION)) {
				evalRes = gt.evaluatePairwise(Collections.unmodifiableList(result.getRows()), noOfFiles, 
						massTolerance, rtTolerance);												
			}
		}		
		if (evalRes == null) {
			return null;
		}
		evalRes.setTh(alpha);
		String note = alpha + ", " + groupingRtWindow;
		evalRes.setNote(note);
		
//		System.out.println();
//		System.out.println("******************************************************");
//		System.out.println("evalRes method=" + method + " mz=" + evalRes.getDmz() + " RT=" + evalRes.getDrt() + " F1=" + evalRes.getF1());
//		System.out.println("******************************************************");
//		System.out.println();
					
		// RetentionTimePrinter rtp = new RetentionTimePrinter();
		// rtp.printRt1(alignmentDataList.get(0), alignmentDataList.get(1));
		// rtp.printRt2(alignmentDataList.get(0), alignmentDataList.get(1), result);
				
		return evalRes;
		
	}		

	private EvaluationResult evaluatePR(AlignmentList result, String measureType, double threshold) {
		
		EvaluationResult evalRes = null;
		if (data.getGroundTruth() != null) {			
			
			int noOfFiles = data.getNoOfFiles();
			GroundTruth gt = data.getGroundTruth();

			Queue<AlignmentRow> pq = new PriorityQueue<AlignmentRow>(10, new AlignmentScoreComparator());
			for (AlignmentRow row : result.getRows()) {
				pq.add(row);
			}
			double lastScore = 0;
			// filter out stuff lower than or equal to threshold
			while (!pq.isEmpty()) {
				AlignmentRow row = pq.peek();
				if (row.getScore() > threshold) {
					lastScore = row.getScore();
					break;
				} else {
					AlignmentRow removed = pq.remove();
				}
			}
				
			if (pq.isEmpty()) {
				return null;
			}
			
			List<AlignmentRow> filtered = new ArrayList<AlignmentRow>(pq);
			if (measureType.equals(MultiAlignConstants.PERFORMANCE_MEASURE_LANGE)) {
				evalRes = gt.evaluateLange(filtered, noOfFiles, massTolerance, rtTolerance);								
			} else if (measureType.equals(MultiAlignConstants.PERFORMANCE_MEASURE_COMBINATION)) {
				evalRes = gt.evaluatePairwise(filtered, noOfFiles, massTolerance, rtTolerance);												
			}
			evalRes.setTh(lastScore);
			String note = alpha + ", " + groupingRtWindow;
			evalRes.setNote(note);
			
		}		
		
		String precStr = String.format("%.3f", evalRes.getPrecision());
		String recStr = String.format("%.3f", evalRes.getRecall());
		String f1Str = String.format("%.3f", evalRes.getF1());
		
		System.out.println("!PR, " + method + ", " + evalRes.getDmz() + ", " + evalRes.getDrt() + 
				", " + precStr + ", " + recStr + ", " + f1Str + 
				", " + evalRes.getTh());
					
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
	
	class AlignmentScoreComparator implements Comparator<AlignmentRow> {
	    public int compare(AlignmentRow x, AlignmentRow y) {
	        return Double.compare(x.getScore(), y.getScore());
	    }
	}
	
}