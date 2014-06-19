package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.EvaluationResult;

public class P1P2Experiment extends MultiAlignBaseExp implements MultiAlignExperiment {

	public static final double[] ALL_ALPHA = { 0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0  };
	public static final double[] ALL_GROUPING_RT = { 
		1, 2, 3, 4, 5, 6, 7, 8, 9, 10
	};
	
	public List<MultiAlignExpResult> performExperiment(
			MultiAlignCmdOptions options) throws FileNotFoundException {
		
		List<MultiAlignExpResult> results = new ArrayList<MultiAlignExpResult>();
		MultiAlignExpResult expResult = new MultiAlignExpResult("");		
		final int iteration = options.experimentIter;
		
		double[] alphas = new double[] { options.alpha };	
		double[] groupingRts = new double[] { options.groupingRtWindow };														
		if (options.autoAlpha) {
			alphas = ALL_ALPHA;
		}
		if (options.autoOptimiseGreedy && options.useGroup && MultiAlignConstants.GROUPING_METHOD_GREEDY.equals(options.groupingMethod)) {
			groupingRts = ALL_GROUPING_RT;
		}
		
		AlignmentData data = getData(options, null);
//		String suffix = "";
//		if (options.inputDirectory.contains("P1")) {
//			suffix = "P1";
//		} else if (options.inputDirectory.contains("P2")) {
//			suffix = "P2";					
//		}
//		saveGtToMatlab(data, suffix);

		final long startTime = System.currentTimeMillis();
		for (int k = 0; k < groupingRts.length; k++) {
			for (int j = 0; j < alphas.length; j++) {
				for (int i = 0; i < iteration; i++) {
					options.alpha = alphas[j];
					options.groupingRtWindow = groupingRts[k];
					System.out.println("EXPERIMENT alpha = " + options.alpha + " groupingRtWindow = " + options.groupingRtWindow);
					// run experiment
					MultiAlign multiAlign = new MultiAlign(options, data);
					EvaluationResult evalRes = multiAlign.runExperiment();	
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
		double totalTime = (endTime - startTime);
		double averageTime = totalTime / iteration;
		System.out.println("==================================================");
		System.out.println("Total execution time: " + totalTime/1000  + " seconds");
		System.out.println("Average execution time: " + averageTime/1000 + " seconds");
		System.out.println("==================================================");
		
		return results;
		
	}
		
//	private static void saveGtToMatlab(AlignmentData data, String suffix) {
//
//		GroundTruth gt = data.getGroundTruth();
//		int numFiles = data.getNoOfFiles();
//		String[] fileNames = data.getFileNamesNoExt();
//		Feature[][] featureArr = gt.getGroundTruthByFilenames(fileNames);
//		double[][] gtArr = new double[featureArr.length][numFiles];
//		for (int i = 0; i < featureArr.length; i++) {
//			for (int j = 0; j < numFiles; j++) {
//				Feature f = featureArr[i][j];
//				if (f != null) {
//					int peakID = f.getPeakID();
//					gtArr[i][j] = peakID + 1;					
//				} else {
//					gtArr[i][j] = Double.NaN;					
//				}
//			}
//		}
//		
//		System.out.println("Saving gt to matlab");
//		MLDouble gtMat = new MLDouble("gt", gtArr);
//		final Collection<MLArray> output = new ArrayList<MLArray>();
//		output.add(gtMat);
//		final MatFileWriter writer = new MatFileWriter();
//		try {
//			String fullPath = "/home/joewandy/Dropbox/Project/real_datasets/P2/source_features_080/debug/gt_" + suffix + ".mat";
//			writer.write(fullPath, output);
//			System.out.println("Written to " + fullPath);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		
//	}

}
