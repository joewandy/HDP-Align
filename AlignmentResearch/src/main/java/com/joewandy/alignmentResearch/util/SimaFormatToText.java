/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.joewandy.alignmentResearch.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import mzmatch.util.Tool;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.FeatureXMLDataGenerator;
import com.joewandy.alignmentResearch.comparator.NaturalOrderFilenameComparator;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class SimaFormatToText {

	final static String version = "1.0.1";
	final static String application = "ConvertToText";

	@OptionsClass(name = application, version = version, author = "RA Scheltema (r.a.scheltema@rug.nl)", description = "Converts the contents of a FeatureML file to a tab-separated text file.")
	public static class Options {

		@Option(name = "d", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The directory of input files in the FeatureML file format.")
		public String inputDirectory = null;

		@Option(name = "gt", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The ground truth file for these data.")
		public String gt = null;
		
		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the help is shown.")
		public boolean help = false;

		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
		
	}

	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
		try {

			Tool.init();

			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);

			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help) {
				Tool.printHeader(System.out, application, version);
				cmdline.printUsage(System.out, "");
				return;
			}

			if (options.verbose) {
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}

			AlignmentDataGenerator generator = new FeatureXMLDataGenerator(options.inputDirectory, options.gt);
			AlignmentData data = generator.generate();
			
			// sort input file alphabetically to look nicer
			File inputDirectory = new File(options.inputDirectory);
			File[] listOfFiles = inputDirectory.listFiles();
			Arrays.sort(listOfFiles, new NaturalOrderFilenameComparator()); 	
			for (int i = 0; i < listOfFiles.length; i++) {

				// load & parse the actual featureXML file here
				if (listOfFiles[i].isFile()) {
				
					File myFile = listOfFiles[i];
					String filename = myFile.getName();
					String path = myFile.getPath();
					String extension = filename.substring(filename.lastIndexOf('.')+1);
					
					// ignore other crap files in the directory
					if (!"txt".equals(extension)) {
						continue;
					}
										
					List<Feature> features = new ArrayList<Feature>();
					try {
						features = loadFeatures(path);
					} catch (ValidityException e) {
						e.printStackTrace();
					} catch (ParsingException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					AlignmentFile alignmentData = new AlignmentFile(i, myFile, features);
					
					// write each data to output
					String currentDir = myFile.getParent();
					String out = currentDir + "/" + alignmentData.getFilenameWithoutExtension() + ".csv";
					PrintWriter pw = new PrintWriter(new FileOutputStream(out));
					pw.println(Feature.csvHeader());
					for (Feature feature : alignmentData.getFeatures()) {
						pw.println(feature.csvForm());
					}
					pw.close();
					System.out.println("Written to " + out);
					
				}
				
			}
			
		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}

	}
	
	private static List<Feature> loadFeatures(String path) throws ValidityException, ParsingException, IOException {
		
		InputStream input = new FileInputStream(path);		
	    BufferedReader br = new BufferedReader(new InputStreamReader(input));
		List<Feature> features = new ArrayList<Feature>();
	    try {
	    	
	        String line = br.readLine();

	        // the peakID is also the position index, so it should start from 0
			int peakId = 0;
	        
	        while (line != null) {
	        	String[] tokens = line.split("\t");
	        	double mass = Double.valueOf(tokens[0]);
	        	double charge = Double.valueOf(tokens[1]);
	        	double intensity = Double.valueOf(tokens[2]);
	        	double rt = Double.valueOf(tokens[3]);
	        	Feature feature = new Feature(peakId, mass/charge, rt, intensity);
	        	features.add(feature);
	        	line = br.readLine();
	        	peakId++;
	        }


	    } finally {
	        br.close();
	    }
	    
		return features;
		
	}


}