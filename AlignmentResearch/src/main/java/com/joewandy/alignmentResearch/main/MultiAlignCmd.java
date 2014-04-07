package com.joewandy.alignmentResearch.main;

import java.awt.Toolkit;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import mzmatch.util.Tool;
import cmdline.CmdLineException;
import cmdline.CmdLineParser;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
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
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;

public class MultiAlignCmd {

//	private static final double[] ALL_ALPHA = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1 };
//	private static final double[] ALL_GROUPING_RT = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

	private static final int RANDOM_FILES_SELECTED = 2;
	private static final double[] ALL_ALPHA = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0  };
	private static final double[] ALL_GROUPING_RT = { 
		0.25, 0.5, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
		11, 12, 13, 14, 15, 16, 17, 18, 19, 20,
		21, 22, 23, 24, 25, 26, 27, 28, 29, 30
	};
	private static final double[] ALL_ALIGNMENT_RT = { 20, 40, 60, 80, 100 };
	
	private static final String EXPERIMENT_TYPE_MISSING_PEAKS = "missingPeaks";
	private static final String EXPERIMENT_TYPE_CONTAMINANT_PEAKS = "contaminantPeaks";
	private static final String EXPERIMENT_TYPE_MEASUREMENT_NOISE = "measurementNoise";
	private static final String EXPERIMENT_TYPE_GLOBAL_NOISE = "globalNoise";
	private static final String EXPERIMENT_TYPE_LOCAL_NOISE = "localNoise";
	private static final String EXPERIMENT_TYPE_POLY_NOISE = "polyNoise";	
	private static final String EXPERIMENT_TYPE_MIXED = "mixed";	
	private static final String EXPERIMENT_TYPE_WITHIN_SET_SINGLE = "withinSetSingle";	
	private static final String EXPERIMENT_TYPE_WITHIN_SET_SPLIT = "withinSetSplit";	
	
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
		LocalNoiseLevel.NONE,
		LocalNoiseLevel.LOW,
		LocalNoiseLevel.MEDIUM,
		LocalNoiseLevel.HIGH
	};
	private static final PolynomialNoiseLevel [] NOISE_POLY_RT_DRIFTS = { 
		PolynomialNoiseLevel.NONE,
		PolynomialNoiseLevel.LOW,
		PolynomialNoiseLevel.MEDIUM,
		PolynomialNoiseLevel.HIGH,
//		PolynomialNoiseLevel.SUPER_HIGH
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
						AlignmentData data = getData(options, false, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
						EvaluationResult evalRes = runExperiment(options, data);							
						if (evalRes != null) {
							expResult.addResult(evalRes);							
						}
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
							AlignmentData data = getData(options, false, 
									0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
									PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
							EvaluationResult evalRes = runExperiment(options, data);										
							if (evalRes != null) {
								expResult.addResult(evalRes);							
							}
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
						AlignmentData data = getData(options, false, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
						EvaluationResult evalRes = runExperiment(options, data);										
						if (evalRes != null) {
							expResult.addResult(evalRes);							
						}
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
						AlignmentData data = getData(options, false, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
						EvaluationResult evalRes = runExperiment(options, data);											
						if (evalRes != null) {
							expResult.addResult(evalRes);							
						}
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
						AlignmentData data = getData(options, false, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
						EvaluationResult evalRes = runExperiment(options, data);							
						if (evalRes != null) {
							expResult.addResult(evalRes);							
						}
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
						AlignmentData data = getData(options, false, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
						EvaluationResult evalRes = runExperiment(options, data);							
						if (evalRes != null) {
							expResult.addResult(evalRes);							
						}
					}
					results.add(expResult);					

					System.out.println("==================================================");
					
				}

			} else if (MultiAlignCmd.EXPERIMENT_TYPE_MIXED.equals(options.experimentType)) {

				for (PolynomialNoiseLevel noiseParam : MultiAlignCmd.NOISE_POLY_RT_DRIFTS) {

					System.out.println("============ noiseParam: " + noiseParam + "============");

					String msg = "" + noiseParam;			
					String replaced = msg.replaceAll("\\.", "");
					String label = options.method + "_poly_drift_" + replaced;
					MultiAlignExpResult expResult = new MultiAlignExpResult(label);
					
					for (int i = 0; i < iteration; i++) {
						AlignmentData data = getData(options, false, 
								0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
								PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
						EvaluationResult evalRes = runExperiment(options, data);				
						if (evalRes != null) {
							expResult.addResult(evalRes);							
						}
					}
					results.add(expResult);					

					System.out.println("==================================================");
					
				}

			} else if (MultiAlignCmd.EXPERIMENT_TYPE_WITHIN_SET_SINGLE.equals(options.experimentType)) {

				MultiAlignExpResult expResult = new MultiAlignExpResult("no_noise");
				
				int i = 0;
				do {
										
					System.out.println();
					System.out.println("################## TRAINING PHASE iter " + (i+1) + " ################## ");
					
					// pick n files randomly to 'train'
					MultiAlignExpResult tempResult = new MultiAlignExpResult("test");	
					AlignmentData data = getData(options, false, 
							0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
							PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);	
					for (double alignmentRt : MultiAlignCmd.ALL_ALIGNMENT_RT) {
						
						System.out.println();
						System.out.println("--- alignmentRt = " + alignmentRt + " ---");
						System.out.println();

						options.alignmentRtWindow = alignmentRt;
						EvaluationResult evalRes = runExperiment(options, data);	
						if (evalRes != null) {
							evalRes.setTh(options.alpha);
							String note = options.alpha + ", " + options.groupingRtWindow;
							evalRes.setNote(note);
							tempResult.addResult(evalRes);	
						}						
						
					}
					
					System.out.println();
					EvaluationResult bestResult = tempResult.getResultBestF1();	
					if (bestResult != null) {

						expResult.addResult(bestResult);							
						i++;
						
					} else {
						
						System.err.println("NO BEST RESULT FOUND ?! ...");
						
					}
																		
				} while (i < iteration);

				results.add(expResult);										
				System.out.println("==================================================");
				
			} else if (MultiAlignCmd.EXPERIMENT_TYPE_WITHIN_SET_SPLIT.equals(options.experimentType)) {

				MultiAlignExpResult expResult = new MultiAlignExpResult("no_noise");
				
				// quick hack: obtained from randsample(44, 2)
				// only works for M1 !!
				int[][] trainingIndices = { 
						{36, 40}, 	{5, 28}, 	{7, 43}, 	{22, 36}, 	{35, 41},
						{2, 38}, 	{33, 34}, 	{8, 32}, 	{3, 5}, 	{14, 42},
						{17, 34},	{20, 22}, 	{13, 34}, 	{6, 8}, 	{15, 26},
						{12, 23},	{25, 43}, 	{12, 37}, 	{11, 41},	{12, 28},
						{26, 37},	{13, 34},	{4, 25},	{35, 42},	{1, 21},
						{11, 18}, 	{6, 42},	{3, 11},	{1, 2}, 	{29, 33}
				};
				int[][] testingIndices= {
						{14, 35},	{12, 27}, 	{20, 33},	{7, 41},	{4, 44},
						{1, 43},	{4, 39}, 	{19, 36}, 	{7, 12}, 	{25, 26},
						{16, 28}, 	{4, 11},	{11, 19}, 	{22, 42},	{17, 40},
						{14, 33},	{9, 17},	{4, 41},	{14, 20}, 	{35, 36},
						{24, 36},	{25, 39},	{10, 14},	{9, 38},	{11, 20},
						{9, 19},	{5, 20},	{12, 27},	{6, 10}, 	{19, 23}						
				};
				
				assert(trainingIndices.length == testingIndices.length);
				for (int i = 0; i < trainingIndices.length; i++) {
										
					System.out.println();
					System.out.println("################## TRAINING PHASE iter " + (i+1) + " ################## ");
					
					int[] trainingSet = trainingIndices[i];
					int[] testingSet = testingIndices[i];
					
					// pick n files randomly to 'train'
					MultiAlignExpResult tempResult = new MultiAlignExpResult("test");	
					AlignmentData data = getData(options, false, 
							0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
							PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise,
							trainingSet);	
					for (double alignmentRt : MultiAlignCmd.ALL_ALIGNMENT_RT) {
						
						System.out.println();
						System.out.println("--- alignmentRt = " + alignmentRt + " ---");
						System.out.println();

						options.alignmentRtWindow = alignmentRt;
						EvaluationResult evalRes = runExperiment(options, data);	
						if (evalRes != null) {
							evalRes.setTh(options.alpha);
							String note = options.alpha + ", " + options.groupingRtWindow;
							evalRes.setNote(note);
							tempResult.addResult(evalRes);	
						}						
						
					}
					
					// report the result on another set of random n files
					System.out.println();
					EvaluationResult bestResult = tempResult.getResultBestF1();	
					data = getData(options, false, 
							0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
							PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise,
							testingSet);	
					if (bestResult != null) {

						double bestRt = bestResult.getDrt();
						options.alignmentRtWindow = bestRt;
						System.out.println();
						System.out.println("##################  TESTING PHASE ################## ");
						System.out.println("################## bestRt = " + bestRt + " ##################");
						System.out.println();
						EvaluationResult evalRes = runExperiment(options, data);	
						if (evalRes != null) {
							evalRes.setTh(options.alpha);
							String note = options.alpha + ", " + options.groupingRtWindow;
							evalRes.setNote(note);
							expResult.addResult(evalRes);	
						}
						
					} else {
						
						System.err.println("NO BEST RESULT FOUND ?! ");
						
					}
																		
				} 

				results.add(expResult);										
				System.out.println("==================================================");
				
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
				
				AlignmentData data = getData(options, false, 
						0.0, 0.0, GlobalNoiseLevel.NONE, LocalNoiseLevel.NONE, 
						PolynomialNoiseLevel.NONE, MeasurementNoiseLevel.NONE, options.randomise, null);
				String suffix = "";
				if (options.inputDirectory.contains("P1")) {
					suffix = "P1";
				} else if (options.inputDirectory.contains("P2")) {
					suffix = "P2";					
				}
//				saveGtToMatlab(data, suffix);
				
				final long startTime = System.currentTimeMillis();
				for (int k = 0; k < groupingRts.length; k++) {
					for (int j = 0; j < alphas.length; j++) {
						for (int i = 0; i < iteration; i++) {
							options.alpha = alphas[j];
							options.groupingRtWindow = groupingRts[k];
							EvaluationResult evalRes = runExperiment(options, data);	
							if (evalRes != null) {
								evalRes.setTh(options.alpha);
								String note = options.alpha + ", " + options.groupingRtWindow;
								evalRes.setNote(note);
								expResult.addResult(evalRes);	
							}
						}
					}
				}									
				final long endTime = System.currentTimeMillis();
				results.add(expResult);										
				System.out.println("==================================================");
				double totalTime = (endTime - startTime);
				double averageTime = totalTime / iteration;
				System.out.println("Total execution time: " + totalTime/1000  + " seconds");
				System.out.println("Average execution time: " + averageTime/1000 + " seconds");
				
			}
						
			 printForMatlab(results);			 

			 
			
		} catch (Exception e) {
			Tool.unexpectedError(e, MultiAlignCmdOptions.APPLICATION);
		}

		// notify with beep
		Toolkit.getDefaultToolkit().beep();
		System.exit(0);

	}
	
	private static void saveGtToMatlab(AlignmentData data, String suffix) {

		GroundTruth gt = data.getGroundTruth();
		int numFiles = data.getNoOfFiles();
		String[] fileNames = data.getFileNamesNoExt();
		Feature[][] featureArr = gt.getGroundTruthByFilenames(fileNames);
		double[][] gtArr = new double[featureArr.length][numFiles];
		for (int i = 0; i < featureArr.length; i++) {
			for (int j = 0; j < numFiles; j++) {
				Feature f = featureArr[i][j];
				if (f != null) {
					int peakID = f.getPeakID();
					gtArr[i][j] = peakID + 1;					
				} else {
					gtArr[i][j] = Double.NaN;					
				}
			}
		}
		
		System.out.println("Saving gt to matlab");
		MLDouble gtMat = new MLDouble("gt", gtArr);
		final Collection<MLArray> output = new ArrayList<MLArray>();
		output.add(gtMat);
		final MatFileWriter writer = new MatFileWriter();
		try {
			String fullPath = "/home/joewandy/Dropbox/Project/real_datasets/P2/source_features_080/debug/gt_" + suffix + ".mat";
			writer.write(fullPath, output);
			System.out.println("Written to " + fullPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	
	private static AlignmentData getData(MultiAlignCmdOptions options,
			boolean addNoise,
			final double missingPeakFrac,
			final double contaminantPeakFrac,
			final com.joewandy.alignmentResearch.noiseModel.GlobalRetentionShiftNoise.GlobalNoiseLevel globalNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.LocalRetentionShiftNoise.LocalNoiseLevel localNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.PolynomialLocalRetentionShiftNoise.PolynomialNoiseLevel polyNoiseLevel,
			final com.joewandy.alignmentResearch.noiseModel.MeasurementNoise.MeasurementNoiseLevel measurementNoiseLevel,
			boolean random, int[] fileIndices) 
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

		AlignmentData data;
		boolean silent = false;
		if (!random) {
			data = dataGenerator.generate();			
		} else {
			if (fileIndices == null) {
				data = dataGenerator.generateRandomly(RANDOM_FILES_SELECTED);
				for (AlignmentFile file : data.getAlignmentDataList()) {
					System.out.println("test on " + file.getFilename());
				}			
				silent = true;				
			} else {
				// pick files according to file indices
				data = dataGenerator.generateByIndices(fileIndices);
				for (AlignmentFile file : data.getAlignmentDataList()) {
					System.out.println("test on " + file.getFilename());
				}			
				silent = true;								
			}
		}
		return data;
		
	}
	
	private static EvaluationResult runExperiment(MultiAlignCmdOptions options, AlignmentData data) throws FileNotFoundException {
		
		MultiAlign multiAlign = new MultiAlign(data, options);
		AlignmentList result = multiAlign.align(false);
		if (result != null) {
			writeAlignmentResult(result, options.output);
			EvaluationResult evalRes = multiAlign.evaluate(result, options.useGroup, false);
			return evalRes;			
		} else {
			return null;
		}
		
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
