package com.joewandy.alignmentResearch.util;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.FeatureXMLDataGenerator;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;

public class FeatureXMLToText {
	
	public static void main(String args[]) throws Exception {

		String inputDirectory = "/home/joewandy/Dropbox/Project/documents/new_measure_experiment/input_data/M2";
		FeatureXMLDataGenerator dataGen = new FeatureXMLDataGenerator(inputDirectory, null, 2, true);
		AlignmentData data = dataGen.generate();
		for (AlignmentFile file : data.getAlignmentDataList()) {
			List<String> lines = new ArrayList<String>();
			for (Feature f : file.getFeatures()) {
				lines.add(f.csvFormForSima());
			}
			String output = file.getParentPath() + "/" + file.getFilenameWithoutExtension() + ".txt";
	        Path path = Paths.get(output);
	        Files.write(path, lines, StandardCharsets.UTF_8);
	        System.out.println("Written " + lines.size() + " lines to " + output);
		}		
		
	}

}
