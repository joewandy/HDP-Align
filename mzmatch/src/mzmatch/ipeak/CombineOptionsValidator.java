package mzmatch.ipeak;

import mzmatch.util.Tool;
import cmdline.CmdLineParser;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;

public class CombineOptionsValidator {

	private CombineOptions options;
	private CmdLineParser cmdLine;
	
	public CombineOptionsValidator(final CombineOptions options,
			CmdLineParser cmdLine) {
		this.options = options;
		this.cmdLine = cmdLine;
	}
	
	public void validateOptions() {
		
		// check whether we need to show the help
		if (options.help) {
			Tool.printHeader(System.out, CombineOptions.APPLICATION, CombineOptions.VERSION);
			cmdLine.printUsage(System.out, "");
			System.exit(0);
		}

		if (options.verbose) {
			Tool.printHeader(System.out, CombineOptions.APPLICATION, CombineOptions.VERSION);
			cmdLine.printOptions();
		}

		// check the command-line parameters

		// are the required options set
		if (options.ppm == -1) {
			System.err
					.println("[ERROR]: the ppm value needs to be set");
			System.exit(0);
		}
		if (options.rtwindow == -1) {
			System.err
					.println("[ERROR]: the rtwindow value needs to be set");
			System.exit(0);
		}			
		// are there enough labels?
		if (options.labels.size() > 0
				&& (options.labels.size() != options.input.size())) {
			System.err
					.println("[ERROR]: the number of labels does not equal the number of input files");
			System.exit(0);
		}
		
		if (!AlignmentMethodFactory.ALIGNMENT_METHOD_GREEDY.equals(options.method)
				&& !AlignmentMethodFactory.ALIGNMENT_METHOD_CUSTOM_JOIN.equals(options.method)
				&& !AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.equals(options.method)
				&& !AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.equals(options.method)
				&& !AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.equals(options.method)
				&& !AlignmentMethodFactory.ALIGNMENT_METHOD_GROUPING_INFO.equals(options.method)) {
			System.err
					.println("[ERROR]: valid options for method are 'greedy', 'join', 'ransac', 'sima' or 'groupingInfo'.");
			System.exit(1);
		}

		if (!"pearson".equals(options.measure)
				&& !"cosine".equals(options.measure)) {
			System.err
					.println("[ERROR]: valid options for method are 'pearson' and 'cosine'.");
			System.exit(1);
		}

	}
	
}
