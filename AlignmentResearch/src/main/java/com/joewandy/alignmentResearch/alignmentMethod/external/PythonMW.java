package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;

/**
 * Calls a MW aligment in python
 * 
 * @author joewandy
 */
public class PythonMW extends BaseAlignment implements AlignmentMethod {
	
	// the executable name of the script
	private static final String SCRIPT_EXEC = System.getProperty("user.home") + "/scripts/MW.py";
	
	// the output to read after running SCRIPT EXEC
	private static final String SCRIPT_OUTPUT = "result.txt";

	private String groupingMethod;
	private double groupingRtTolerance;
	private double minCorrSignal;
	private boolean useGroup;
	private boolean usePeakShape;
	private boolean exactMatch;
	private double alpha;	
	private int groupingNSamples;
	private int groupingBurnIn;
	private double groupingDpAlpha;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public PythonMW(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
		this.groupingMethod = param.getGroupingMethod();
		this.groupingRtTolerance = param.getGroupingRtTolerance();
		this.minCorrSignal = param.getMinCorrSignal();
		this.useGroup = param.isUseGroup();
		this.usePeakShape = param.isUsePeakShape();
		this.exactMatch = param.isExactMatch();
		this.alpha = param.getAlpha();
		this.groupingNSamples = param.getGroupingNSamples();
		this.groupingBurnIn = param.getGroupingBurnIn();
		this.groupingDpAlpha = param.getGroupingDpAlpha();
	}
	
	@Override
	protected AlignmentList matchFeatures() {

		AlignmentList alignedList = null;
		try {

			// create temporary input files, and execute the script on them
			Path tempDirPath = writeTempFiles();			
			String outputPath = tempDirPath.toString() + "/" + PythonMW.SCRIPT_OUTPUT;
			runScript(tempDirPath.toString(), outputPath);
			
			// read back the output
			alignedList = new AlignmentList(outputPath, dataList, "");

			// clean all files inside directory
			FileUtils.cleanDirectory(tempDirPath.toFile());
			tempDirPath.toFile().delete();
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		} finally {
			
		}

		return alignedList;
				
	}

	private Path writeTempFiles() throws IOException, FileNotFoundException {
	
		// create temporary directory to hold our intermediate input files
        Path tempPath = Files.createTempDirectory(parentPath, "MW_INPUT_");
        System.out.println("tempPath = " + tempPath);
		
		// put all spectra files inside TEMP_INPUT_DIR
		for (AlignmentFile data : dataList) {
			String out = tempPath.toString() + "/" + 
					data.getFilenameWithoutExtension() + ".csv";
			data.saveSimaFeatures(out);
			System.out.println("Written to " + out);
		}
		return tempPath;
	
	}

	private void runScript(String tempDirPath, String outputPath) throws ExecuteException,
		IOException {

		// run the script on the input files, first set some parameters for calling
		Map<String, String> map = new HashMap<String, String>();
		map.put("inputDir", tempDirPath);
		map.put("outputFile", outputPath);		
		map.put("dmz", String.valueOf(this.massTolerance));		    
		map.put("drt", String.valueOf(this.rtTolerance));
		map.put("method", "mw");
		map.put("groupingMethod", this.groupingMethod);	
		map.put("alpha", String.valueOf(this.alpha));
		map.put("groupingRtTolerance", String.valueOf(this.groupingRtTolerance));
		map.put("minCorrSignal", String.valueOf(this.minCorrSignal));		
		map.put("dpAlpha", String.valueOf(this.groupingDpAlpha));
		map.put("numSamples", String.valueOf(this.groupingNSamples));
		map.put("burnIn", String.valueOf(this.groupingBurnIn));
		System.out.println(map);
		
		// for non-blocking version, see http://commons.apache.org/proper/commons-exec/tutorial.html
		final String execPath = PythonMW.SCRIPT_EXEC;
		CommandLine cmdLine = new CommandLine(execPath);

		// basic parameters
		cmdLine.addArgument("-i");
		cmdLine.addArgument("${inputDir}");
		cmdLine.addArgument("-o");
		cmdLine.addArgument("${outputFile}");
		cmdLine.addArgument("-dmz");
		cmdLine.addArgument("${dmz}");
		cmdLine.addArgument("-drt");
		cmdLine.addArgument("${drt}");
		cmdLine.addArgument("-method");
		cmdLine.addArgument("${method}");
		if (this.exactMatch) {
			cmdLine.addArgument("-exact_match");			
		}
		
		// grouping parameters
		if (this.useGroup) {
			cmdLine.addArgument("-g");			
		}
		if (this.usePeakShape) {
			cmdLine.addArgument("-p");			
			cmdLine.addArgument("-mcs");
			cmdLine.addArgument("${minCorrSignal}");					
		}
		cmdLine.addArgument("-gm");
		cmdLine.addArgument("${groupingMethod}");		
		cmdLine.addArgument("-alpha");
		cmdLine.addArgument("${alpha}");		
		cmdLine.addArgument("-grt");
		cmdLine.addArgument("${groupingRtTolerance}");		

		// mixture model parameters
		cmdLine.addArgument("-dp_alpha");
		cmdLine.addArgument("${dpAlpha}");		
		cmdLine.addArgument("-num_samples");
		cmdLine.addArgument("${numSamples}");		
		cmdLine.addArgument("-burn_in");
		cmdLine.addArgument("${burnIn}");		

		// for debugging
		cmdLine.addArgument("-always_recluster");		
		
		cmdLine.setSubstitutionMap(map);
		DefaultExecutor executor = new DefaultExecutor();
		
		// handle the exitcode properly ..
		System.out.println("cmdLine=" + cmdLine);
		int exitCode = executor.execute(cmdLine); 
		System.out.println("exitCode=" + exitCode);
		
	}
	
}
