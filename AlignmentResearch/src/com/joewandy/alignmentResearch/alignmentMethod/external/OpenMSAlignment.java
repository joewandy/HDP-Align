package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
import com.joewandy.alignmentResearch.objectModel.Feature;

/**
 * Performs OpenMS alignment using MapAlignmentAlgorithmPoseClustering and FeatureGroupingAlgorithmUnlabeled
 * See http://bioinformatics.oxfordjournals.org/content/23/13/i273.abstract
 * 
 * @author joewandy
 */
public class OpenMSAlignment extends BaseAlignment implements AlignmentMethod {

	private static final String TEMP_INPUT_DIR = "/home/joewandy/temp/OpenMS";
	private static final String ALIGNED_OUT_PATH = "/aligned_out";
	private static final String ALIGNED_XML = "aligned.consensusXML";
	private static final String ALIGNED_TXT = "aligned.txt";

	private static final String MAP_ALIGNER_EXEC = "/usr/bin/MapAlignerPoseClustering";
	private static final String FEATURE_LINKER_EXEC = "/usr/bin/FeatureLinkerUnlabeled";
	
	public OpenMSAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
	}
	
	@Override
	protected AlignmentList matchFeatures() {

		AlignmentList alignedList = null;
		try {

			// create temporary input files
			writeTempFiles();			
			
			// execute OpenMS' MapAligner and FeatureLinker on the MapAligner output
			runAlignerLinker();
			
			// convert consensusXML into something we can understand
			String outputPath = convertResult();
			
			// read back the output from simaDir/results/result.txt
			alignedList = new AlignmentList(outputPath, dataList, "");
						
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return alignedList;
				
	}

	private String writeTempFiles() throws IOException, FileNotFoundException {
	
		// create temporary directory to hold our intermediate input files
		final String tempDirPath = OpenMSAlignment.TEMP_INPUT_DIR;
		final File tempDir = new File(tempDirPath);
		if (!tempDir.exists() && !tempDir.mkdirs()) {
		    throw new IOException("Unable to create " + tempDir.getAbsolutePath());
		}
		
		// clean all files inside directory
		FileUtils.cleanDirectory(tempDir);

		// put all spectra files inside TEMP_INPUT_DIR
		for (AlignmentFile data : dataList) {
			File source = data.getFile();
			String destPath = OpenMSAlignment.TEMP_INPUT_DIR + "/" + 
					data.getFilename();
			File dest = new File(destPath);
			FileUtils.copyFile(source, dest);
			System.out.println("Written to " + destPath);
		}
		return tempDirPath;
	
	}

	private void runAlignerLinker() throws ExecuteException,
		IOException {

		// create temporary directory to hold our intermediate output files
		final String tempOutPath = OpenMSAlignment.TEMP_INPUT_DIR + ALIGNED_OUT_PATH;
		final File tempDir = new File(tempOutPath);
		if (!tempDir.exists() && !tempDir.mkdirs()) {
		    throw new IOException("Unable to create " + tempDir.getAbsolutePath());
		}
		
		// clean all files inside directory
		FileUtils.cleanDirectory(tempDir);
		
		// run MapAligner on the input files
		String[] inputFiles = getFilePaths(OpenMSAlignment.TEMP_INPUT_DIR);
		String[] outputFiles = getFilePaths(OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH);
		
		// for non-blocking version, see http://commons.apache.org/proper/commons-exec/tutorial.html
		CommandLine cmdLine = new CommandLine(OpenMSAlignment.MAP_ALIGNER_EXEC);
		cmdLine.addArgument("-in");
		cmdLine.addArguments(inputFiles);
		cmdLine.addArgument("-out");
		cmdLine.addArguments(outputFiles);
//		cmdLine.addArgument("-ini");
//		cmdLine.addArgument("${ini}");
		DefaultExecutor executor = new DefaultExecutor();
		System.out.println("cmdLine=" + cmdLine);
		executor.execute(cmdLine); 

		// run FeatureLinker on the output of MapAligner
		cmdLine = new CommandLine(OpenMSAlignment.FEATURE_LINKER_EXEC);
		cmdLine.addArgument("-in");
		cmdLine.addArguments(outputFiles);
		cmdLine.addArgument("-out");
		cmdLine.addArguments(OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH + "/" + OpenMSAlignment.ALIGNED_XML);
//		cmdLine.addArgument("-ini");
//		cmdLine.addArgument("${ini}");
		System.out.println("cmdLine=" + cmdLine);
		executor.execute(cmdLine); 
				
	}

	private String[] getFilePaths(String path) {
		String[] result = new String[dataList.size()];
		for (int i = 0; i < dataList.size(); i++) {
			AlignmentFile file = dataList.get(i);
			result[i] = path + "/" + file.getFilename();
		}
		return result;
	}
	
	private String convertResult() {		
		
		final String inputConsensusXML = OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH + "/" + ALIGNED_XML;
		final String outputPath = OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH + "/" + ALIGNED_TXT;

		// read consensus XML
		
		// write text file
		
		return outputPath;

	}
	
}
