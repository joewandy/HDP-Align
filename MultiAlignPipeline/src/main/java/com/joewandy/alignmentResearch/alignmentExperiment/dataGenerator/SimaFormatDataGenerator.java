package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.joewandy.alignmentResearch.comparator.NaturalOrderFilenameComparator;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.FeatureGroup;
import com.joewandy.alignmentResearch.model.GroundTruth;

public class SimaFormatDataGenerator extends BaseDataGenerator implements AlignmentDataGenerator {

	private String inputDirectory;
	private String gtPath;
	
	public SimaFormatDataGenerator(String inputDirectory, String gtPath, int gtCombinationSize, boolean verbose) {
		super();
		this.inputDirectory = inputDirectory;
		this.gtPath = gtPath;
		this.verbose = verbose;
		this.gtCombinationSize = gtCombinationSize;
	}
	
	@Override
	protected List<AlignmentFile> getAlignmentFiles(int currentIter) {
		
		// sort input file alphabetically to look nicer
		File inputDirectory = new File(this.inputDirectory);
		File[] listOfFiles = inputDirectory.listFiles();
		Arrays.sort(listOfFiles, new NaturalOrderFilenameComparator()); 			
		List<AlignmentFile> alignmentDataList = new ArrayList<AlignmentFile>();
		List<Feature> allFeatures = new ArrayList<Feature>();

		// load feature data
		for (int i = 0; i < listOfFiles.length; i++) {

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
					features = this.loadFeatures(path);
				} catch (ValidityException e) {
					e.printStackTrace();
				} catch (ParsingException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				AlignmentFile alignmentData = new AlignmentFile(i, myFile, features);
				if (verbose) {
					System.out.println("Load " + filename + " with " + alignmentData.getFeaturesCount() + " features");
				}
				alignmentDataList.add(alignmentData);
				allFeatures.addAll(alignmentData.getFeatures());
			
			}
			
		}
		
		this.alignmentFiles = alignmentDataList;		
		return alignmentDataList;
		
	}
	
	@Override
	protected GroundTruth getGroundTruth() {
		
		if (this.gtPath == null) {
			return null;
		}
		
		// store only features present in files that we're aligning
		List<FeatureGroup> groundTruthEntries = new ArrayList<FeatureGroup>();

		Scanner in = null;
		try {
					
			// build a map from filename -> the actual feature data
			Map<String, AlignmentFile> alignmentDataMap = new HashMap<String, AlignmentFile>();
			for (AlignmentFile data : this.alignmentFiles) {
				alignmentDataMap.put(data.getFilenameWithoutExtension(), data);											
			}
			
			Map<Integer, String> tempMap = new HashMap<Integer, String>();
			File inputFile = new File(this.gtPath);
			in = new Scanner(inputFile);
			
			int groupID = 1;			
			while (in.hasNextLine()) {
				
				String line = in.nextLine();
				Scanner lineSplitter = new Scanner(line);
				
				while (lineSplitter.hasNext()) {
					
					String firstToken = lineSplitter.next();
					if (">".equals(firstToken)) {
						
						int fileIdx = lineSplitter.nextInt();
						String filename = lineSplitter.next();
						tempMap.put(fileIdx, filename);
						
					} else if ("#".equals(firstToken)) {

						FeatureGroup gtFeatures = new FeatureGroup(groupID);
						while (lineSplitter.hasNext()) {

							int fileIdx = lineSplitter.nextInt();
							int peakIdx = lineSplitter.nextInt();
							String filename = tempMap.get(fileIdx);
							AlignmentFile data = alignmentDataMap.get(filename);
							if (data != null) {
								Feature featureFromData = data.getFeatureByPeakID(peakIdx);
								assert featureFromData != null : "fileIdx " + fileIdx + " peakIdx " + peakIdx;
								gtFeatures.addFeature(featureFromData);
							}
							
						}						
						groupID++; // new groupID for every line in ground truth
						
						// don't need this .. ?
						// if (gtFeatures.getFeatureCount() > 1) {
							groundTruthEntries.add(gtFeatures);	
						//	}
						
					}
															
				}
				lineSplitter.close();
	
			} // close main while loop
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();				
			}
		}
		System.out.println("Load ground truth = " + groundTruthEntries.size() + " rows");

//		System.out.println("Retaining only entries size >= 2 = " + groundTruthEntries.size() + " rows");
		Iterator<FeatureGroup> it = groundTruthEntries.iterator();
		while (it.hasNext()) {
			FeatureGroup gg = it.next();
			if (gg.getFeatureCount() < 2) {
				it.remove();
			}
		}
		
		GroundTruth groundTruth = new GroundTruth(groundTruthEntries, gtCombinationSize, verbose);
		return groundTruth;
		
	}

	private List<Feature> loadFeatures(String path) throws ValidityException, ParsingException, IOException {
		
		InputStream input = new FileInputStream(path);		
	    BufferedReader br = new BufferedReader(new InputStreamReader(input));
		List<Feature> features = new ArrayList<Feature>();
	    try {
	    	
	        String line = br.readLine();

	        // the peakID is also the position index, so it should start from 0
			int peakId = 0;
	        
	        while (line != null) {
	        	String[] tokens = line.split("\t");
	        	if (tokens.length == 4) {
		        	double mass = Double.valueOf(tokens[0]);
		        	double charge = Double.valueOf(tokens[1]);
		        	double intensity = Double.valueOf(tokens[2]);
		        	double rt = Double.valueOf(tokens[3]);
		        	Feature feature = new Feature(peakId, mass/charge, rt, intensity);
		        	features.add(feature);
		        	line = br.readLine();
		        	peakId++;	        		
	        	} else if (tokens.length == 7){
		        	double mass = Double.valueOf(tokens[0]);
		        	double charge = Double.valueOf(tokens[1]);
		        	double intensity = Double.valueOf(tokens[2]);
		        	double rt = Double.valueOf(tokens[3]);
		        	int theoPeakId = Integer.valueOf(tokens[4]);
		        	String metId = tokens[5];
		        	String adduct = tokens[6];
		        	Feature feature = new Feature(peakId, mass/charge, rt, intensity);
		        	feature.setTheoPeakID(theoPeakId);
		        	feature.setMetaboliteID(metId);
		        	feature.setTheoAdductType(adduct);
		        	features.add(feature);
		        	line = br.readLine();
		        	peakId++;
	        	}
	        }


	    } finally {
	        br.close();
	    }
	    
		return features;
		
	}
	
}
