package com.joewandy.alignmentResearch.main;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import mzmatch.util.Tool;
import cmdline.CmdLineException;
import cmdline.CmdLineParser;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.noiseModel.ContaminantPeaksNoise;
import com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise;
import com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel;
import com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise;
import com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel;
import com.joewandy.alignmentResearch.noiseModel.MeasurementNoise;
import com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel;
import com.joewandy.alignmentResearch.noiseModel.MissingPeaksNoise;
import com.joewandy.alignmentResearch.noiseModel.PolynomialLocalRetentionShiftNoise;
import com.joewandy.alignmentResearch.noiseModel.PolynomialLocalRetentionShiftNoise.PolynomialNoiseLevel;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class MultiAlignCmd {

//	private static final double[] ALL_ALPHA = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
//	private static final double[] ALL_GROUPING_RT = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	private static final double[] ALL_ALPHA = { 0.2, 0.4, 0.6, 0.8  };
	private static final double[] ALL_GROUPING_RT = { 2, 4, 6, 8, 10 };
	
	private static final String EXPERIMENT_TYPE_MISSING_PEAKS = "missingPeaks";
	private static final String EXPERIMENT_TYPE_CONTAMINANT_PEAKS = "contaminantPeaks";
	private static final String EXPERIMENT_TYPE_MEASUREMENT_NOISE = "measurementNoise";
	private static final String EXPERIMENT_TYPE_GLOBAL_NOISE = "globalNoise";
	private static final String EXPERIMENT_TYPE_LOCAL_NOISE = "localNoise";
	private static final String EXPERIMENT_TYPE_POLY_NOISE = "polyNoise";	
	
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
	private static final PolynomialNoiseLevel [] NOISE_POLY_RT_DRIFTS = { 
		PolynomialNoiseLevel.HIGH
	};
	
	public static void main(String args[]) throws Exception {
		try {

			// parse the commandline options									
			Tool.init();
			MultiAlignCmdOptions options = parseCommandLine(args);

			List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
			final int iteration = options.experimentIter;

			if (MultiAlignCmd.EXPERIMENT_TYPE_MISSING_PEAKS.equals(options.experimentType)) {
			
				for (double noiseParam : MultiAlignCmd.NOISE_MISSING_PEAKS_FRACTIONS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");

					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_frac_" + replaced;
					MultiAlignExpResult expResult = new MultiAlignExpResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								noiseParam, 0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					
					
					System.out.println("==================================================");

				}

			} else if (MultiAlignCmd.EXPERIMENT_TYPE_CONTAMINANT_PEAKS.equals(options.experimentType)) {
					
					for (double noiseParam : MultiAlignCmd.NOISE_CONTAMINANT_PEAKS_FRACTIONS) {

						System.out.println("============ noiseParam: " + noiseParam + "============");

						String msg = "" + noiseParam;			
						String replaced = msg.replaceAll("\\.", "");
						String label = options.method + "_frac_" + replaced;
						MultiAlignExpResult expResult = new MultiAlignExpResult(label);
						
						for (int i = 0; i < iteration; i++) {
							EvaluationResult evalRes = runExperiment(options, true, 
									0, noiseParam, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
									PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
							expResult.addResult(evalRes);
						}
						results.add(expResult);					
						
						System.out.println("==================================================");

					}

			} else if (MultiAlignCmd.EXPERIMENT_TYPE_MEASUREMENT_NOISE.equals(options.experimentType)) {

				for (MeasurementNoiseLevel noiseParam : MultiAlignCmd.NOISE_MEASUREMENT) {

					System.out.println("============ noiseParam: " + noiseParam + "============");
					
					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_measurement_" + replaced;
					MultiAlignExpResult expResult = new MultiAlignExpResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, noiseParam);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");

				}
					
			} else if (MultiAlignCmd.EXPERIMENT_TYPE_GLOBAL_NOISE.equals(options.experimentType)) {

				for (GlobalNoiseLevel noiseParam : MultiAlignCmd.NOISE_GLOBAL_RT_DRIFTS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");
					
					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_global_drift_" + replaced;
					MultiAlignExpResult expResult = new MultiAlignExpResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, noiseParam, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");

				}
				
			} else if (MultiAlignCmd.EXPERIMENT_TYPE_LOCAL_NOISE.equals(options.experimentType)) {

				for (LocalNoiseLevel noiseParam : MultiAlignCmd.NOISE_LOCAL_RT_DRIFTS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");

					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_local_drift_" + replaced;
					MultiAlignExpResult expResult = new MultiAlignExpResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, GlobalNoiseLevel.NONE, noiseParam, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");
					
				}

			} else if (MultiAlignCmd.EXPERIMENT_TYPE_POLY_NOISE.equals(options.experimentType)) {

				for (PolynomialNoiseLevel noiseParam : MultiAlignCmd.NOISE_POLY_RT_DRIFTS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");

					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_poly_drift_" + replaced;
					MultiAlignExpResult expResult = new MultiAlignExpResult(label);
					
					for (int i = 0; i < iteration; i++) {
						EvaluationResult evalRes = runExperiment(options, true, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								noiseParam, MeasurementNoiseLevel.NONE);				
						expResult.addResult(evalRes);
					}
					results.add(expResult);					

					System.out.println("==================================================");
					
				}
				
			} else {
				
				// no noise

				System.out.println("============ noiseParam: NONE ============");

				MultiAlignExpResult expResult = new MultiAlignExpResult("no_noise");
				double[] alphas = new double[] { options.alpha };	
				double[] groupingRts = new double[] { options.groupingRtWindow };														
				if (options.autoAlpha) {
					alphas = MultiAlignCmd.ALL_ALPHA;
				}
				if (options.autoOptimiseGreedy && options.useGroup && MultiAlign.GROUPING_METHOD_GREEDY.equals(options.groupingMethod)) {
					groupingRts = MultiAlignCmd.ALL_GROUPING_RT;
				}
				for (int k = 0; k < groupingRts.length; k++) {
					for (int j = 0; j < alphas.length; j++) {
						for (int i = 0; i < iteration; i++) {
							options.alpha = alphas[j];
							options.groupingRtWindow = groupingRts[k];
							EvaluationResult evalRes = runExperiment(options, false, 
									0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
									PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE);	
							evalRes.setTh(options.alpha);
							String note = options.alpha + ", " + options.groupingRtWindow;
							evalRes.setNote(note);
							expResult.addResult(evalRes);	
						}
					}
				}
				results.add(expResult);										
				System.out.println("==================================================");
				
			}
						
			 printForMatlab(results);
			 
			
		} catch (Exception e) {
			Tool.unexpectedError(e, MultiAlignCmdOptions.APPLICATION);
		}

		// notify with beep
		Toolkit.getDefaultToolkit().beep();
		System.exit(0);

	}
	
	private static MultiAlignCmdOptions parseCommandLine(String[] args)
			throws CmdLineException {
		MultiAlignCmdOptions options = new MultiAlignCmdOptions();
		CmdLineParser cmdline = new CmdLineParser(options);

		// check whether we need to show the help
		cmdline.parse(args);
		if (options.help) {
			Tool.printHeader(System.out, MultiAlignCmdOptions.APPLICATION, MultiAlignCmdOptions.VERSION);
			cmdline.printUsage(System.out, "");
			System.exit(0);
		}
		if (options.verbose) {
			Tool.printHeader(System.out, MultiAlignCmdOptions.APPLICATION, MultiAlignCmdOptions.VERSION);
			cmdline.printOptions();
		}
		return options;

	}
	
	private static EvaluationResult runExperiment(MultiAlignCmdOptions options,
			boolean addNoise,
			final double missingPeakFrac,
			final double contaminantPeakFrac,
			final com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel globalNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel localNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.PolynomialLocalRetentionShiftNoise.PolynomialNoiseLevel polyNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel measurementNoiseLevel) 
		throws FileNotFoundException {

		/*
		 * LOAD FEATURES & GROUND TRUTH DATA
		 */
		
		AlignmentDataGenerator dataGenerator = AlignmentDataGeneratorFactory.getAlignmentDataGenerator(options);
		if (addNoise) {
			MultiAlignCmd.addNoise(
					options, 
					dataGenerator, 
					missingPeakFrac, 
					contaminantPeakFrac, 
					globalNoiseLevel, 
					localNoiseLevel, 
					polyNoiseLevel,
					measurementNoiseLevel);						
		}
		AlignmentData data = dataGenerator.generate();
		
		MultiAlign multiAlign = new MultiAlign(data, options);
		AlignmentList result = multiAlign.align();
		writeAlignmentResult(result, options.output);
		EvaluationResult evalRes = multiAlign.evaluate(result, options.useGroup);
		return evalRes;
		
	}
	
	private static void writeAlignmentResult(AlignmentList result, 
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
	
	private static String printRow(AlignmentRow row) {
		StringBuilder sb = new StringBuilder();
		for (Feature feature : row.getFeatures()) {
			sb.append(printFeature(feature));			
		}
		return sb.toString();		
	}
	
	private static String printFeature(Feature feature) {
		return feature.getIntensity() + " " + feature.getRt() + " " + feature.getMass() + " ";
	}
	
	private static void addNoise(MultiAlignCmdOptions options,
			AlignmentDataGenerator dataGenerator,
			final double missingPeakFrac,
			final double contaminantPeakFrac,
			final com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel globalNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel localNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.PolynomialLocalRetentionShiftNoise.PolynomialNoiseLevel polyNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel measurementNoiseLevel) {
				
		// add missing peaks
		dataGenerator.addNoise(new MissingPeaksNoise(missingPeakFrac));
		
		// add contaminant peaks
		dataGenerator.addNoise(new ContaminantPeaksNoise(contaminantPeakFrac));
		
		// add global RT drift
		dataGenerator.addNoise(new GlobalRetentionShiftNoise(globalNoiseLevel));
		
		// add local RT drift
		dataGenerator.addNoise(new LocalRetentionShiftNoise(localNoiseLevel));
		
		// add polynomial RT drift
		dataGenerator.addNoise(new PolynomialLocalRetentionShiftNoise(polyNoiseLevel));

		// add measurement noises
		dataGenerator.addNoise(new MeasurementNoise(measurementNoiseLevel));

		// add systematic noises across runs
		// dataGenerator.addNoise(new ExperimentalConditionNoise());
		
	}	
	
	private static void printForMatlab(List<MultiAlignExpResult> results) {
		
		System.out.println();
		System.out.println("*****************************************************");
		System.out.println("MATLAB OUTPUT");
		System.out.println("*****************************************************");
		System.out.println();
		
		for (MultiAlignExpResult result : results) {
			result.printResult();
			System.out.println();
		}
			
	}

}
