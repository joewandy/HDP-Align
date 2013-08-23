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

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cmdline.CmdLineException;
import cmdline.CmdLineParser;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.filter.GraphAlignmentResultFilter;
import com.joewandy.alignmentResearch.filter.SizeAlignmentResultFilter;
import com.joewandy.alignmentResearch.noiseModel.ContaminantPeaksNoise;
import com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise;
import com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel;
import com.joewandy.alignmentResearch.noiseModel.KeepByGroundTruthFeatures;
import com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise;
import com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel;
import com.joewandy.alignmentResearch.noiseModel.MeasurementNoise;
import com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel;
import com.joewandy.alignmentResearch.noiseModel.MissingPeaksNoise;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;
import com.joewandy.alignmentResearch.objectModel.FeatureGroup;
import com.joewandy.alignmentResearch.objectModel.FeatureGrouping;
import com.joewandy.alignmentResearch.objectModel.GreedyFeatureGrouping;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;
import com.joewandy.alignmentResearch.objectModel.MatlabFeatureGrouping;
import com.joewandy.util.Tool;

public class FeatureXMLAlignment {
		
	public static final int ALIGNMENT_SIZE_THRESHOLD = 2;
	public static final int GROUP_SIZE_THRESHOLD = 0;
	
	public static final int RTWINDOW_MULTIPLY = 1;
	public static final boolean PARALLEL_LIBRARY_BUILD = false;

	public static final boolean WEIGHT_USE_WEIGHTED_SCORE = true;
	public static final boolean WEIGHT_USE_PROB_CLUSTERING_WEIGHT = false;
	public static final boolean WEIGHT_USE_ALL_PEAKS = false;
	
	// public static final int ALIGNMENT_SCORE_THRESHOLD = 20;
	
	/*
	 * PARAMETERS FOR NOISE & EXPERIMENT
	 */
	
	private static final String EXPERIMENT_TYPE_MISSING_PEAKS = "missingPeaks";
	private static final String EXPERIMENT_TYPE_CONTAMINANT_PEAKS = "contaminantPeaks";
	private static final String EXPERIMENT_TYPE_MEASUREMENT_NOISE = "measurementNoise";
	private static final String EXPERIMENT_TYPE_GLOBAL_NOISE = "globalNoise";
	private static final String EXPERIMENT_TYPE_LOCAL_NOISE = "localNoise";
	private static final int EXPERIMENT_ITERATION = 1;
	
	private static final double [] NOISE_MISSING_PEAKS_FRACTIONS = { 0.0, 0.2, 0.4, 0.6 };
	private static final double [] NOISE_CONTAMINANT_PEAKS_FRACTIONS = { 0.0, 0.2, 0.4, 0.6 };
	private static final MeasurementNoiseLevel [] NOISE_MEASUREMENT= {
		MeasurementNoiseLevel.NONE, 
		MeasurementNoiseLevel.LOW, 
		MeasurementNoiseLevel.MEDIUM, 
		MeasurementNoiseLevel.HIGH 
	};	
	private static final GlobalNoiseLevel [] NOISE_GLOBAL_RT_DRIFTS = {
			GlobalNoiseLevel.NONE, 
			GlobalNoiseLevel.LOW, 
			GlobalNoiseLevel.MEDIUM, 
			GlobalNoiseLevel.HIGH 
	};
	private static final LocalNoiseLevel [] NOISE_LOCAL_RT_DRIFTS = { 
			LocalNoiseLevel.SUPER_HIGH
	};
	
	public static void main(String args[]) throws Exception {
		try {

			// parse the commandline options
			Tool.init();
			FeatureXMLAlignmentOptions options = parseCommandLine(args);

			List<FeatureXMLAlignmentResult> results = new ArrayList<FeatureXMLAlignmentResult>();
			final int iteration = FeatureXMLAlignment.EXPERIMENT_ITERATION;

			if (FeatureXMLAlignment.EXPERIMENT_TYPE_MISSING_PEAKS.equals(options.experimentType)) {
			
				for (double noiseParam : FeatureXMLAlignment.NOISE_MISSING_PEAKS_FRACTIONS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");

					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_frac_" + replaced;
					FeatureXMLAlignmentResult expResult = new FeatureXMLAlignmentResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								noiseParam, 0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					
					
					System.out.println("==================================================");

				}

			} else if (FeatureXMLAlignment.EXPERIMENT_TYPE_CONTAMINANT_PEAKS.equals(options.experimentType)) {
					
					for (double noiseParam : FeatureXMLAlignment.NOISE_CONTAMINANT_PEAKS_FRACTIONS) {

						System.out.println("============ noiseParam: " + noiseParam + "============");

						String msg = "" + noiseParam;			
						String replaced = msg.replaceAll("\\.", "");
						String label = options.method + "_frac_" + replaced;
						FeatureXMLAlignmentResult expResult = new FeatureXMLAlignmentResult(label);
						
						for (int i = 0; i < iteration; i++) {
							EvaluationResult evalRes = runExperiment(options, true, 
									0, noiseParam, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
							expResult.addResult(evalRes);
						}
						results.add(expResult);					
						
						System.out.println("==================================================");

					}

			} else if (FeatureXMLAlignment.EXPERIMENT_TYPE_MEASUREMENT_NOISE.equals(options.experimentType)) {

				for (MeasurementNoiseLevel noiseParam : FeatureXMLAlignment.NOISE_MEASUREMENT) {

					System.out.println("============ noiseParam: " + noiseParam + "============");
					
					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_measurement_" + replaced;
					FeatureXMLAlignmentResult expResult = new FeatureXMLAlignmentResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, noiseParam);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");

				}
					
			} else if (FeatureXMLAlignment.EXPERIMENT_TYPE_GLOBAL_NOISE.equals(options.experimentType)) {

				for (GlobalNoiseLevel noiseParam : FeatureXMLAlignment.NOISE_GLOBAL_RT_DRIFTS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");
					
					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_global_drift_" + replaced;
					FeatureXMLAlignmentResult expResult = new FeatureXMLAlignmentResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, noiseParam, LocalNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");

				}
				
			} else if (FeatureXMLAlignment.EXPERIMENT_TYPE_LOCAL_NOISE.equals(options.experimentType)) {

				for (LocalNoiseLevel noiseParam : FeatureXMLAlignment.NOISE_LOCAL_RT_DRIFTS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");

					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_local_drift_" + replaced;
					FeatureXMLAlignmentResult expResult = new FeatureXMLAlignmentResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, GlobalNoiseLevel.NONE, noiseParam, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");
					
				}
				
			} else {
				
				// no noise

				System.out.println("============ noiseParam: NONE ============");

				FeatureXMLAlignmentResult expResult = new FeatureXMLAlignmentResult("no_noise");
				
				for (int i = 0; i < iteration; i++) {
					EvaluationResult evalRes = runExperiment(options, false, 
							0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
					expResult.addResult(evalRes);
				}
				results.add(expResult);					

				System.out.println("==================================================");
				
			}
						
			printForMatlab(results);
			
		} catch (Exception e) {
			Tool.unexpectedError(e, FeatureXMLAlignmentOptions.APPLICATION);
		}

		// notify with beep
		Toolkit.getDefaultToolkit().beep();
		System.exit(0);

	}
	
	private static FeatureXMLAlignmentOptions parseCommandLine(String[] args)
			throws CmdLineException {
		FeatureXMLAlignmentOptions options = new FeatureXMLAlignmentOptions();
		CmdLineParser cmdline = new CmdLineParser(options);

		// check whether we need to show the help
		cmdline.parse(args);
		if (options.help) {
			Tool.printHeader(System.out, FeatureXMLAlignmentOptions.APPLICATION, FeatureXMLAlignmentOptions.VERSION);
			cmdline.printUsage(System.out, "");
			System.exit(0);
		}
		if (options.verbose) {
			Tool.printHeader(System.out, FeatureXMLAlignmentOptions.APPLICATION, FeatureXMLAlignmentOptions.VERSION);
			cmdline.printOptions();
		}
		return options;

	}
	
	private static void addNoise(FeatureXMLAlignmentOptions options,
			AlignmentDataGenerator dataGenerator,
			final double missingPeakFrac,
			final double contaminantPeakFrac,
			final com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel globalNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel localNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel measurementNoiseLevel) {

		if (options.useGtFeaturesOnly) {
			// use only features present in ground truth 
			dataGenerator.addNoise(new KeepByGroundTruthFeatures());
		}
				
		// add missing peaks
		dataGenerator.addNoise(new MissingPeaksNoise(missingPeakFrac));
		
		// add contaminant peaks
		dataGenerator.addNoise(new ContaminantPeaksNoise(contaminantPeakFrac));
		
		// add global RT drift
		dataGenerator.addNoise(new GlobalRetentionShiftNoise(globalNoiseLevel));
		
		// add local RT drift
		dataGenerator.addNoise(new LocalRetentionShiftNoise(localNoiseLevel));
		
		// add measurement noises
		dataGenerator.addNoise(new MeasurementNoise(measurementNoiseLevel));

		// add systematic noises across runs
		// dataGenerator.addNoise(new ExperimentalConditionNoise());
		
	}	

	private static EvaluationResult runExperiment(FeatureXMLAlignmentOptions options,
			boolean addNoise,
			final double missingPeakFrac,
			final double contaminantPeakFrac,
			final com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel globalNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel localNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel measurementNoiseLevel) 
		throws FileNotFoundException {

		/*
		 * LOAD FEATURES & GROUND TRUTH DATA
		 */
		
		AlignmentDataGenerator dataGenerator = AlignmentDataGeneratorFactory.getAlignmentDataGenerator(options);
		if (addNoise) {
			FeatureXMLAlignment.addNoise(
					options, 
					dataGenerator, 
					missingPeakFrac, 
					contaminantPeakFrac, 
					globalNoiseLevel, 
					localNoiseLevel, 
					measurementNoiseLevel);						
		}
		
		AlignmentData data = dataGenerator.generate();
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		GroundTruth gt = data.getGroundTruth();
								
		/*
		 * GROUPING & ALIGNMENT
		 */

		// do grouping before aligning
		if (options.grouping) {
			FeatureGrouping grouping = null;
			if (!WEIGHT_USE_WEIGHTED_SCORE) {
				// even without weighting, we still want to group .. 
				// just to prevent null pointer exception
				grouping = new GreedyFeatureGrouping(options.groupingRtwindow);		
			} else {
				if (WEIGHT_USE_PROB_CLUSTERING_WEIGHT) {
					// use probability weight scores
					grouping = new MatlabFeatureGrouping(options.groupingRtwindow);									
				} else {
					// use greedy weight scores
					grouping = new MatlabFeatureGrouping(options.groupingRtwindow);					
				}
			}
			List<FeatureGroup> groups = grouping.group(alignmentDataList);
			grouping.filterGroups(groups); // remove groups that are too small ?
		}	

		// pick alignment method
		AlignmentMethod aligner = AlignmentMethodFactory.getAlignmentMethod(options, data);

		// setup some filters to prune alignment results later
		if (options.grouping) {
			AlignmentResultFilter sizeFilter = new SizeAlignmentResultFilter(FeatureXMLAlignment.ALIGNMENT_SIZE_THRESHOLD);
			aligner.addFilter(sizeFilter);
			AlignmentResultFilter graphFilter = new GraphAlignmentResultFilter(alignmentDataList, 
					options.graphFilter, options.th);
			aligner.addFilter(graphFilter);
		}
		
		// actually do the alignment now, filtering of alignment results also happen inside align()
		List<AlignmentRow> result = aligner.align();
		System.out.println("Total " + result.size() + " rows aligned");
		
		/*
		 * OUTPUT & EVALUATION
		 */
		
		PrintStream alignmentOutput = System.out;
		if (options.output != null) {
			System.out.println("Writing output");
			alignmentOutput = new PrintStream(new FileOutputStream(options.output));
			aligner.writeAlignmentResult(alignmentOutput);				
		}

		// do performance evaluation
		EvaluationResult evalRes = null;
		if (options.gt != null) {				
			evalRes = gt.evaluate(Collections.unmodifiableList(result));				
		}
					
		// RetentionTimePrinter rtp = new RetentionTimePrinter();
		// rtp.printRt1(alignmentDataList.get(0), alignmentDataList.get(1));
		// rtp.printRt2(alignmentDataList.get(0), alignmentDataList.get(1), result);
		
		return evalRes;
		
	}
	
	private static void printForMatlab(List<FeatureXMLAlignmentResult> results) {
		
		System.out.println();
		System.out.println("*****************************************************");
		System.out.println("MATLAB OUTPUT");
		System.out.println("*****************************************************");
		System.out.println();
		
		for (FeatureXMLAlignmentResult result : results) {
			result.printResult();
			System.out.println();
		}
			
	}
	
}