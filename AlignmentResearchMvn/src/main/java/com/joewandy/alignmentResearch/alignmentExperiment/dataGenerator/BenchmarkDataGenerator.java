package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import com.joewandy.alignmentResearch.comparator.NaturalOrderFilenameComparator;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;
import com.joewandy.alignmentResearch.objectModel.GroundTruthFeatureGroup;

public class BenchmarkDataGenerator extends BaseDataGenerator implements AlignmentDataGenerator {

	private String inputDirectory;
	private String gtPath;
	private List<AlignmentFile> alignmentFiles;
	
	public BenchmarkDataGenerator(String inputDirectory, String gtPath) {
		super();
		this.inputDirectory = inputDirectory;
		this.gtPath = gtPath;
	}
	
	@Override
	protected List<AlignmentFile> getAlignmentFiles() {
		
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
				if (!"featureXML".equals(extension)) {
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
				System.out.println("Load " + filename + " with " + alignmentData.getFeaturesCount() + " features");
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
		List<GroundTruthFeatureGroup> groundTruthEntries = new ArrayList<GroundTruthFeatureGroup>();

		Scanner in = null;
		try {
					
			// build a map from filename -> the actual feature data
			Map<String, AlignmentFile> alignmentDataMap = new HashMap<String, AlignmentFile>();
			for (AlignmentFile data : this.alignmentFiles) {
				alignmentDataMap.put(data.getFilenameWithoutExtension(), data);											
			}
					
			// load ground truth file from Lange, et al. (2008)
			File inputFile = new File(this.gtPath);
			in = new Scanner(inputFile);
			
			int groupID = 1;			
			while (in.hasNextLine()) {
				String line = in.nextLine();
				Scanner lineSplitter = new Scanner(line);
				GroundTruthFeatureGroup gtFeatures = new GroundTruthFeatureGroup(groupID);
				
				// we can implement a finite-state machine, or we can just hack this ...
				while (lineSplitter.hasNext()) {
					
					// parse until the next non-number
					String nextString = lineSplitter.next();
					try {
						Double.parseDouble(nextString);
					} catch (NumberFormatException e) {
	
						String filename = nextString;
						double intensity;
						double rt;
						double mass;
						
						try {

							// next token is unknown purpose
							if (lineSplitter.hasNext()) {
								lineSplitter.nextDouble();								
							} else {
								break; // move on to next line
							}
							
							// next token is intensity
							if (lineSplitter.hasNext()) {
								intensity = lineSplitter.nextDouble();								
							} else {
								break; // move on to next line
							}
							
							// next token is rt
							if (lineSplitter.hasNext()) {
								rt = lineSplitter.nextDouble();			
							} else {
								break; // move on to next line
							}
							
							// next token is mass
							if (lineSplitter.hasNext()) {
								mass = lineSplitter.nextDouble();								
							} else {
								break; // move on to next line
							}
							
							// don't load ground truth data for the input files that we're not processing
							String noExt = AlignmentFile.removeExtension(nextString);
							AlignmentFile data = alignmentDataMap.get(noExt);
							if (data != null) {
								// make a new example Feature object
								Feature example = new Feature(-1, mass, rt, intensity);
								Feature featureFromData = data.getFeatureByExample(example);
								assert(featureFromData != null);
								gtFeatures.addFeature(featureFromData);
							}
	
						} catch (InputMismatchException ie) {
							break; // move on to the next line
						}
													
					}
					
				}
				lineSplitter.close();
				groupID++; // new groupID for every line in ground truth
								
				// don't need this .. ?
				// if (gtFeatures.getFeatureCount() > 1) {
					groundTruthEntries.add(gtFeatures);	
				//	}
	
			} // close main while loop
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			if (in != null) {
				in.close();				
			}
		}

		System.out.println("Ground truth loaded = " + groundTruthEntries.size() + " rows");

		Iterator<GroundTruthFeatureGroup> it = groundTruthEntries.iterator();
		while (it.hasNext()) {
			GroundTruthFeatureGroup gg = it.next();
			if (gg.getFeatureCount() < 2) {
				it.remove();
			}
		}
		System.out.println("Retaining only entries size >= 2 = " + groundTruthEntries.size() + " rows");
		
		GroundTruth groundTruth = new GroundTruth(groundTruthEntries);
		return groundTruth;
		
	}

	private List<Feature> loadFeatures(String path) throws ValidityException, ParsingException, IOException {
		
		InputStream input = new FileInputStream(path);
		List<Feature> features = new ArrayList<Feature>();
		Builder parser = new Builder();
		Document doc = parser.build(input);
		Element root = doc.getRootElement();
		Element featureList = root
				.getFirstChildElement("featureList");
	
		// the peakID is also the position index, so it should start from 0
		int peakId = 0;
		if (featureList != null) {
			Elements featureElems = featureList.getChildElements();
			for (int j = 0; j < featureElems.size(); j++) {
				Element featureElem = featureElems.get(j);
				if ("feature"
						.equals(featureElem.getQualifiedName())) {
					Feature feature = getFeatureFromElement(featureElem,
							peakId);
					features.add(feature);						
					peakId++;
				}
			}

		}
		
		return features;
		
	}
	
	private Feature getFeatureFromElement(Element featureElem, int peakId) {
		Elements children = featureElem.getChildElements();
		double mass = 0;
		double rt = 0;
		double intensity = 0;
		for (int i = 0; i < children.size(); i++) {
			Element child = children.get(i);
			String childName = child.getQualifiedName();
			if ("position".equals(childName)) {
				Attribute attr = child.getAttribute("dim");
				// Dimension 0 for retention time and 1 for m/z
				if ("0".equals(attr.getValue())) {
					rt = Double.parseDouble(child.getValue());
				} else if ("1".equals(attr.getValue())) {
					mass = Double.parseDouble(child.getValue());
				}
			} else if ("intensity".equals(childName)) {
				intensity = Double.parseDouble(child.getValue());
			}
		}
		Feature feature = new Feature(peakId, mass, rt, intensity);
		return feature;
	}	
	
}
