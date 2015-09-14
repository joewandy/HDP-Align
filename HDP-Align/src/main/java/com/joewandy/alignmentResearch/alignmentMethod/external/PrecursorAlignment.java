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
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.AlignmentList;

/**
 * Calls the precursor aligment in python
 * 
 * @author joewandy
 */
public class PrecursorAlignment extends BaseAlignment implements AlignmentMethod {
		
	private static final String SCRIPT_EXEC = "/precursor_alignment.py";

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
	private boolean alwaysRecluster;

	private String trans;
	private String db;
	private double withinFileBinningMassTol;
	private double withinFileBinningRtTol;
	private double withinFileRtSd;
	private double acrossFileBinningMassTol;
	private double acrossFileRtSd;
	private double alphaMass;
	private double alphaRt;
	private double t;
	private int massClusteringNoIters;
	private int rtClusteringNsamps;
	private int rtClusteringBurnIn;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public PrecursorAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
		this.trans = param.getTrans();
		this.db = param.getIdentificationDatabase();
		this.withinFileBinningMassTol = param.getWithinFileBinningMassTol();
		this.withinFileBinningRtTol = param.getWithinFileBinningRtTol();
		this.withinFileRtSd = param.getWithinFileRtSd();
		this.acrossFileBinningMassTol = param.getAcrossFileBinningMassTol();
		this.acrossFileRtSd = param.getAcrossFileRtSd();
		this.alphaMass = param.getAlphaMass();
		this.alphaRt = param.getAlphaRt();
		this.t = param.getT();
		this.massClusteringNoIters = param.getMassClusteringNoIters();
		this.rtClusteringNsamps = param.getRtClusteringNsamps();
		this.rtClusteringBurnIn = param.getRtClusteringBurnIn();
	}
	
	@Override
	protected AlignmentList matchFeatures() {

		AlignmentList alignedList = null;
		try {

			// create temporary input files, and execute the script on them
			Path tempDirPath = writeTempFiles();			
			String outputPath = tempDirPath.toString() + "/" + PrecursorAlignment.SCRIPT_OUTPUT;
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
        Path tempPath = Files.createTempDirectory(parentPath, "PRECURSOR_INPUT_");
        if (verbose) {
            System.out.println("tempPath = " + tempPath);        	
        }
		
		// put all spectra files inside TEMP_INPUT_DIR
		for (AlignmentFile data : dataList) {
			String out = tempPath.toString() + "/" + 
					data.getFilenameWithoutExtension() + ".txt";
			data.saveSimaFeatures(out);
			if (verbose) {
				System.out.println("Written to " + out);				
			}
		}
		return tempPath;
	
	}

	private void runScript(String tempDirPath, String outputPath) throws ExecuteException,
		IOException {

		// run the script on the input files, first set some parameters for calling
		Map<String, String> map = new HashMap<String, String>();
		map.put("inputDir", tempDirPath);
		map.put("outputFile", outputPath);		
		map.put("trans", String.valueOf(this.trans));		    
		map.put("db", String.valueOf(this.db));
		map.put("within_file_binning_mass_tol", String.valueOf(this.withinFileBinningMassTol));	
		map.put("within_file_binning_rt_tol", String.valueOf(this.withinFileBinningRtTol));
		map.put("across_file_binning_mass_tol", String.valueOf(this.acrossFileBinningMassTol));	
		map.put("within_file_rt_sd", String.valueOf(this.withinFileRtSd));
		map.put("across_file_rt_sd", String.valueOf(this.acrossFileRtSd));		
		map.put("alpha_mass", String.valueOf(this.alphaMass));
		map.put("alpha_rt", String.valueOf(this.alphaRt));
		map.put("t", String.valueOf(this.t));
		map.put("mass_clustering_n_iterations", String.valueOf(this.massClusteringNoIters));
		map.put("rt_clustering_nsamps", String.valueOf(this.rtClusteringNsamps));
		map.put("rt_clustering_burnin", String.valueOf(this.rtClusteringBurnIn));
		if (verbose) {
			System.out.println(map);
		}
		map.put("seed", String.valueOf(this.seed));		
		
		// for non-blocking version, see http://commons.apache.org/proper/commons-exec/tutorial.html
		final String execPath = this.getExecutablePath() + SCRIPT_EXEC;
		CommandLine cmdLine = new CommandLine(execPath);

		// basic parameters
		cmdLine.addArgument("-i");
		cmdLine.addArgument("${inputDir}");
		cmdLine.addArgument("-o");
		cmdLine.addArgument("${outputFile}");
		cmdLine.addArgument("-trans");
		cmdLine.addArgument("${trans}");
		if (this.db != null) {
			cmdLine.addArgument("-db");
			cmdLine.addArgument("${db}");			
		}
		if (this.verbose) {
			cmdLine.addArgument("-v");			
		}
		cmdLine.addArgument("-seed");
		cmdLine.addArgument("${seed}");
		
		// alignment parameters
		cmdLine.addArgument("-within_file_binning_mass_tol");
		cmdLine.addArgument("${within_file_binning_mass_tol}");
		cmdLine.addArgument("-within_file_binning_rt_tol");
		cmdLine.addArgument("${within_file_binning_rt_tol}");
		cmdLine.addArgument("-within_file_rt_sd");
		cmdLine.addArgument("${within_file_rt_sd}");
		cmdLine.addArgument("-across_file_binning_mass_tol");
		cmdLine.addArgument("${across_file_binning_mass_tol}");
		cmdLine.addArgument("-across_file_rt_sd");
		cmdLine.addArgument("${across_file_rt_sd}");
		cmdLine.addArgument("-alpha_mass");
		cmdLine.addArgument("${alpha_mass}");
		cmdLine.addArgument("-alpha_rt");
		cmdLine.addArgument("${alpha_rt}");
		cmdLine.addArgument("-t");
		cmdLine.addArgument("${t}");
		cmdLine.addArgument("-mass_clustering_n_iterations");
		cmdLine.addArgument("${mass_clustering_n_iterations}");
		cmdLine.addArgument("-rt_clustering_nsamps");
		cmdLine.addArgument("${rt_clustering_nsamps}");
		cmdLine.addArgument("-rt_clustering_burnin");
		cmdLine.addArgument("${rt_clustering_burnin}");
		
		cmdLine.setSubstitutionMap(map);
		DefaultExecutor executor = new DefaultExecutor();
		
		// handle the exitcode properly ..
		if (verbose) {
			System.out.println("cmdLine=" + cmdLine);
		}
		int exitCode = executor.execute(cmdLine); 
		if (verbose) {
			System.out.println("exitCode=" + exitCode);			
		}
		
	}
	
}
