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
 * Performs SIMA alignment
 * SIMA: Simultaneous Multiple Alignment of LC/MS Peak Lists, Voss, et al. (2011)
 * http://bioinformatics.oxfordjournals.org/content/early/2011/02/03/bioinformatics.btr051
 * 
 * @author joewandy
 */
public class SimaAlignment extends BaseAlignment implements AlignmentMethod {

	// base directory that contains sima
	private static final String SIMA_BASE = "/home/joewandy/Dropbox/Project/real_datasets/SIMA_full";

	// the executable name inside SIMA_BASE
	private static final String SIMA_EXEC = "SIMA";

	// temporary directory inside SIMA_BASE used to put our input files
	private static final String SIMA_INPUT_DIR = "/temp";
	
	// the output to read after running SIMA_EXEC
	private static final String SIMA_OUTPUT = "/temp/results/result.txt";
	
	// Enable MTS correction using PARAMETER linepoints. Recommendation: 50-100 linepoints.
	private double mtsCorrection; // any other options to include here ?

	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public SimaAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
	}
	
	@Override
	protected AlignmentList matchFeatures() {

		AlignmentList alignedList = null;
		try {

			// create temporary input files, and execute SIMA on them
			final String tempDirPath = writeTempFiles();			
			runSima(tempDirPath);
			
			// read back the output from simaDir/results/result.csv
			final String outputPath = SimaAlignment.SIMA_BASE + SimaAlignment.SIMA_OUTPUT;
			alignedList = new AlignmentList(outputPath, dataList, "");
						
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		return alignedList;
				
	}

	private String writeTempFiles() throws IOException, FileNotFoundException {
	
		// create temporary directory to hold our intermediate input files
		final String tempDirPath = SimaAlignment.SIMA_BASE + SimaAlignment.SIMA_INPUT_DIR;
		final File tempDir = new File(tempDirPath);
		if (!tempDir.exists() && !tempDir.mkdirs()) {
		    throw new IOException("Unable to create " + tempDir.getAbsolutePath());
		}
		
		// clean all files inside directory
		FileUtils.cleanDirectory(tempDir);

		// put all spectra files inside SIMA_INPUT_DIR
		for (AlignmentFile data : dataList) {
			String out = SimaAlignment.SIMA_BASE + SimaAlignment.SIMA_INPUT_DIR + "/" + 
					data.getFilenameWithoutExtension() + ".csv";
			PrintWriter pw = new PrintWriter(new FileOutputStream(out));
			for (Feature feature : data.getFeatures()) {
				pw.println(feature.csvFormForSima());						
			}
			pw.close();
			System.out.println("Written to " + out);
		}
		return tempDirPath;
	
	}

	private void runSima(final String tempDirPath) throws ExecuteException,
		IOException {

		// run sima on the input files, first set some parameters for calling
		Map<String, String> map = new HashMap<String, String>();
		map.put("rt", String.valueOf(this.rtTolerance));
		map.put("mz", String.valueOf(this.massTolerance));		    
		map.put("tempDirPath", tempDirPath);
		System.out.println(map);
		
		// for non-blocking version, see http://commons.apache.org/proper/commons-exec/tutorial.html
		final String execPath = SimaAlignment.SIMA_BASE + "/" + SimaAlignment.SIMA_EXEC;
		// final String execPath = "/usr/local/bin/examples/" + SimaAlignment.SIMA_EXEC;
		CommandLine cmdLine = new CommandLine(execPath);
		cmdLine.addArgument("-R");
		cmdLine.addArgument("${rt}");
		cmdLine.addArgument("-M");
		cmdLine.addArgument("${mz}");
		cmdLine.addArgument("-i");
		cmdLine.addArgument("${tempDirPath}");
		cmdLine.setSubstitutionMap(map);
		DefaultExecutor executor = new DefaultExecutor();
		
		// exitCode is useless here, the SIMA program returns 0 even if error occurs ?!
		System.out.println("cmdLine=" + cmdLine);
		int exitCode = executor.execute(cmdLine); 
		
	}
	
}
