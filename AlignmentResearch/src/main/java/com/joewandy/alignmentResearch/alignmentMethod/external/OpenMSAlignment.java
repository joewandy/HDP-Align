package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteException;
import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
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

	private static final String MAP_ALIGNER_EXEC = "/usr/bin/MapAlignerPoseClustering";
	private static final String FEATURE_LINKER_EXEC = "/usr/bin/FeatureLinkerUnlabeled";

	private static final String CONFIG_TEMPLATE_MAP_ALIGNER = "MapAlignerPoseClustering.ini.vm";
	private static final String CONFIG_TEMPLATE_FEATURE_LINKER = "FeatureLinkerUnlabeled.ini.vm";
	
	Map<Integer, AlignmentFile> dataMap;
	private double mzPairMaxDistance;
	
	public OpenMSAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
		this.dataMap = new HashMap<Integer, AlignmentFile>();
		for (int i = 0; i < dataList.size(); i++) {
			dataMap.put(i, dataList.get(i));
		}
		mzPairMaxDistance = AlignmentMethodParam.PARAM_MZ_PAIR_MAX_DISTANCE;
	}
	
	@Override
	protected AlignmentList matchFeatures() {

		AlignmentList alignedList = null;
		try {

			// create temporary input files
			writeTempFiles();			
			
			// execute OpenMS' MapAligner and FeatureLinker on the MapAligner output
			runAlignerLinker();
						
			// read back the resulting consensusXML
			final String inputConsensusXML = OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH + "/" + ALIGNED_XML;
			alignedList = readResult(inputConsensusXML);
						
		} catch (IOException e) {
			System.out.println(e.getMessage());
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

			String destPath = OpenMSAlignment.TEMP_INPUT_DIR + "/" + 
					data.getFilename();
			
//			File source = data.getFile();
//			File dest = new File(destPath);
//			FileUtils.copyFile(source, dest);
			
			data.saveFeatures(destPath);			
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
		
		// run MapAligner on the input files using the specified config file
		String[] inputFiles = getFilePaths(OpenMSAlignment.TEMP_INPUT_DIR);
		String[] outputFiles = getFilePaths(OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH);
		writeConfigTemplate(OpenMSAlignment.CONFIG_TEMPLATE_MAP_ALIGNER, OpenMSAlignment.TEMP_INPUT_DIR);
		writeConfigTemplate(OpenMSAlignment.CONFIG_TEMPLATE_FEATURE_LINKER, OpenMSAlignment.TEMP_INPUT_DIR);
		
		// for non-blocking version, see http://commons.apache.org/proper/commons-exec/tutorial.html
		CommandLine cmdLine = new CommandLine(OpenMSAlignment.MAP_ALIGNER_EXEC);
		cmdLine.addArgument("-in");
		cmdLine.addArguments(inputFiles);
		cmdLine.addArgument("-out");
		cmdLine.addArguments(outputFiles);
		cmdLine.addArgument("-ini");
		cmdLine.addArgument(OpenMSAlignment.TEMP_INPUT_DIR + "/" + OpenMSAlignment.CONFIG_TEMPLATE_MAP_ALIGNER);
		DefaultExecutor executor = new DefaultExecutor();
		System.out.println("cmdLine=" + cmdLine);
		executor.execute(cmdLine); 

		// run FeatureLinker on the output of MapAligner
		cmdLine = new CommandLine(OpenMSAlignment.FEATURE_LINKER_EXEC);
		cmdLine.addArgument("-in");
		cmdLine.addArguments(outputFiles);
		cmdLine.addArgument("-out");
		cmdLine.addArguments(OpenMSAlignment.TEMP_INPUT_DIR + OpenMSAlignment.ALIGNED_OUT_PATH + "/" + OpenMSAlignment.ALIGNED_XML);
		cmdLine.addArgument("-ini");
		cmdLine.addArgument(OpenMSAlignment.TEMP_INPUT_DIR + "/" + OpenMSAlignment.CONFIG_TEMPLATE_FEATURE_LINKER);
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
	
	private AlignmentList readResult(String inputConsensusXML) {		
		
		AlignmentList result = null;
		try {
			result = loadConsensusFeatures(inputConsensusXML);
		} catch (ValidityException e) {
			e.printStackTrace();
		} catch (ParsingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
				
		return result;

	}
	
	private AlignmentList loadConsensusFeatures(String path) throws ValidityException, ParsingException, IOException {
		
		AlignmentList alignmentList = new AlignmentList("");
		
		InputStream input = new FileInputStream(path);
		Builder parser = new Builder();
		Document doc = parser.build(input);
		Element root = doc.getRootElement();
		Element consensusFeatureList = root
				.getFirstChildElement("consensusElementList");
	
		// the peakID is also the position index, so it should start from 0
		int id = 0;
		if (consensusFeatureList != null) {
			Elements consensusElems = consensusFeatureList.getChildElements("consensusElement");
			for (int j = 0; j < consensusElems.size(); j++) {
				Element consensusElem = consensusElems.get(j);
				Element groupedElem = consensusElem.getFirstChildElement("groupedElementList");
				AlignmentRow consensus = getConsensusRow(groupedElem, 
						alignmentList, id);
				alignmentList.addRow(consensus);						
				id++;
			}

		}
		
		return alignmentList;
		
	}
	
	private AlignmentRow getConsensusRow(Element featureElem, AlignmentList parent, int id) {
		AlignmentRow consensus = new AlignmentRow(parent, id);
		Elements children = featureElem.getChildElements("element");
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			Attribute mapAttr = child.getAttribute("map");
			Attribute idAttr = child.getAttribute("id");
			int mapIdx = Integer.parseInt(mapAttr.getValue());
			
			int peakId = Integer.parseInt(idAttr.getValue()) - 1; // id goes from 1 ... N in the consensusXML file

			// TODO: WHAT A HACK !! for P1 & P2 only.
			String path = dataList.get(0).getParentPath();
			if (path.contains("P1") || path.contains("P2")) {
				peakId = Integer.parseInt(idAttr.getValue()) - 0; // id goes from 0 ... N in the consensusXML file				
			}

			AlignmentFile data = dataMap.get(mapIdx);
			Feature f = data.getFeatureByPeakID(peakId);
			assert f != null : "peakID " + peakId + " not found";
			consensus.addAlignedFeature(f);
		}
		return consensus;
	}	
	
	private void writeConfigTemplate(String templateName, String tempPath) throws FileNotFoundException {
		
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        ve.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        ve.init();

        Template t = ve.getTemplate("templates/" + templateName);
        VelocityContext context = new VelocityContext();
        context.put("mz_pair_max_distance", this.mzPairMaxDistance);
        context.put("distance_MZ_max_difference", this.massTolerance);
        context.put("distance_RT_max_difference", this.rtTolerance);
        
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        
        File tempFile = new File(tempPath + "/" + templateName);
        PrintWriter pw = new PrintWriter(tempFile);
        pw.print(writer.toString());     
        pw.close();

	}
		
}
