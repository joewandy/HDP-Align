package com.joewandy.alignmentResearch.main;

import java.util.List;

import mzmatch.util.Tool;
import cmdline.CmdLineException;
import cmdline.CmdLineParser;

import com.joewandy.alignmentResearch.main.experiment.GenerativeBiologicalExperiment;
import com.joewandy.alignmentResearch.main.experiment.GenerativeTechnicalExperiment;
import com.joewandy.alignmentResearch.main.experiment.M1Experiment;
import com.joewandy.alignmentResearch.main.experiment.MultiAlignExpResult;
import com.joewandy.alignmentResearch.main.experiment.MultiAlignExperiment;
import com.joewandy.alignmentResearch.main.experiment.MultiAlignNormalRun;
import com.joewandy.alignmentResearch.main.experiment.P1P2Experiment;

/**
 * Command-line to invoke various alignment tools
 * @author joewandy
 *
 */
public class MultiAlignCmd {
	
	public static void main(String args[]) throws Exception {
		
		try {

			Tool.init();
			
			// parse command line stuff
			MultiAlignCmdOptions options = parseCommandLine(args);

			// run alignment
			MultiAlignExperiment exp = pickExperimentType(options);

			// print out the results
			List<MultiAlignExpResult> results = exp.performExperiment(options);									
			exp.printResult(results);
			
		} catch (Exception e) {
			Tool.unexpectedError(e, MultiAlignCmdOptions.APPLICATION);
		}

		System.exit(0);

	}

	private static MultiAlignExperiment pickExperimentType(
			MultiAlignCmdOptions options) {
		
		MultiAlignExperiment exp = null;			
		if (options.experimentType != null) {

			if (MultiAlignExperiment.EXPERIMENT_TYPE_M1.equals(options.experimentType)) {
				// metabolomic experiment using the M1 dataset from Lange, et al. (2008)
				exp = new M1Experiment();
			} else if (MultiAlignExperiment.EXPERIMENT_TYPE_P1P2.equals(options.experimentType)) {
				// proteomic experiment using the P1 and P2 datasets from Lange, et al. (2008)
				exp = new P1P2Experiment();
			} else if (MultiAlignExperiment.EXPERIMENT_TYPE_GENERATIVE_TECHNICAL_REPLICATES.equals(options.experimentType)) {
				// generative model alignment without retention time warping
				exp = new GenerativeTechnicalExperiment();
			} else if (MultiAlignExperiment.EXPERIMENT_TYPE_GENERATIVE_BIOLOGICAL_REPLICATES.equals(options.experimentType)) {
				// generative model alignment with retention time warping
				exp = new GenerativeBiologicalExperiment();
			} 
			
		} 
		
		if (exp == null) {
			// nothing special, just align
			exp = new MultiAlignNormalRun();				
		}
		
		return exp;
		
	}
		
	private static MultiAlignCmdOptions parseCommandLine(String[] args)
			throws CmdLineException {
		
		MultiAlignCmdOptions options = new MultiAlignCmdOptions();
		CmdLineParser cmdline = new CmdLineParser(options);

		cmdline.parse(args);
		if (options.help) {
			// show help ?
			Tool.printHeader(System.out, MultiAlignCmdOptions.APPLICATION, MultiAlignCmdOptions.VERSION);
			cmdline.printUsage(System.out, "");
			System.exit(0);
		}
		if (options.verbose) {
			// be verbose ?
			Tool.printHeader(System.out, MultiAlignCmdOptions.APPLICATION, MultiAlignCmdOptions.VERSION);
			cmdline.printOptions();
		}
		return options;

	}
			
}
