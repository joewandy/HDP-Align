package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.apache.commons.math3.distribution.BetaDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter.ExperimentType;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.GenerativeFeatureClusterer;
import com.joewandy.alignmentResearch.objectModel.GenerativeFeatureGroup;
import com.joewandy.alignmentResearch.objectModel.GenerativeMolecule;
import com.joewandy.alignmentResearch.objectModel.GenerativeMoleculeDB;
import com.joewandy.alignmentResearch.objectModel.GroundTruth;
import com.joewandy.alignmentResearch.objectModel.GroundTruthFeatureGroup;
import com.joewandy.alignmentResearch.rtPrediction.RTPredictor;

import domsax.XmlParserException;

public class GenerativeModelDataGenerator extends BaseDataGenerator implements AlignmentDataGenerator {
	
	// path to molecule XML file
	private String molPath;
	
	// path to substitute molecule XML file
	private String subsPath;
	
	// parameters for generative models
	private GenerativeModelParameter params;
	
	// RT predictor
	private RTPredictor predictor;
	
	private Random r;
		
	public GenerativeModelDataGenerator(String molPath, GenerativeModelParameter params) {
		super();
		this.molPath = molPath;
		this.subsPath = params.getReplacementMolsPath();
		this.params = params;
		this.predictor = new RTPredictor(params);
		this.r = new Random();
	}
	
	@Override
	protected List<AlignmentFile> getAlignmentFiles() {

		System.out.println("Loading molecules");
		List<Molecule> molecules = loadMolecules(molPath);

		System.out.println("Loading replacement");
		List<Molecule> replacement = loadMolecules(subsPath);
		
		int numFiles = params.getS();
		GenerativeMoleculeDB database = createDatabase(molecules, replacement,
				numFiles);
						
		// generate the theoretical features for all metabolites
		System.out.println("Generating theoretical features");
		database.initializeTheoreticalPeaks();
		
		List<AlignmentFile> alignmentDataList = new ArrayList<AlignmentFile>();
		String filePrefix = "synthdata_";
		String fileSuffix = ".xml";
		
		// generating observed features for all replicates
		for (int i = 0; i < numFiles; i++) {

			// select the molecules in each replicate
			List<GenerativeMolecule> selectedMols = database.getAllMoleculeInfo(i);
			int N = database.countTheoreticalPeaks(selectedMols);
			System.out.println("Total " + selectedMols.size() + " molecules selected with " + N + " theoretical features");

			// collect all the theoretical features for each metabolite
			List<Feature> theoFeatures = new ArrayList<Feature>();
			for (GenerativeMolecule mol : selectedMols) {
				initialiseClusters(mol, i); // initialise RT clustering
				List<Feature> molFeatures = mol.getTheoFeatures();
				theoFeatures.addAll(molFeatures);
			}
			
			// generate observed features from theoretical featues
			System.out.println("Generating observed features ..");
			List<Feature> observedFeatures = generateObservedFeatures(theoFeatures);
									
			String fileName = filePrefix + i + fileSuffix;
			AlignmentFile file = new AlignmentFile(i, fileName, observedFeatures);
			System.out.println(file.getFilename() + " contains " + file.getFeaturesCount() + " observed features");

			alignmentDataList.add(file);
			
		}
		
		this.alignmentFiles = alignmentDataList;		
		return alignmentDataList;
		
	}
	
	@Override
	protected GroundTruth getGroundTruth() {
		
		List<GroundTruthFeatureGroup> groundTruthEntries = new ArrayList<GroundTruthFeatureGroup>();
		
		int groupID = 1;
		for (int i = 0; i < alignmentFiles.size(); i++) {
			
			AlignmentFile file1 = alignmentFiles.get(i);
			List<Feature> features1 = file1.getFeatures();

			for (Feature feature1 : features1) {
			
				if (feature1.isAligned()) {
					continue;
				}
				
				// add ourselves
				GroundTruthFeatureGroup gt = new GroundTruthFeatureGroup(groupID);
				gt.addFeature(feature1);
				feature1.setAligned(true);

				// find matching in other files
				if (alignmentFiles.size() > 1) {

					// just do greedy search for matching theoretical peak ID
					for (int j = 0; j < alignmentFiles.size(); j++) {
						
						if (i == j) {
							continue;
						}
						
						AlignmentFile file2 = alignmentFiles.get(j);
						List<Feature> features2 = file2.getFeatures();
						for (Feature feature2 : features2) {
							if (feature2.isAligned()) {
								continue;
							}
							if (feature1.getTheoPeakID() == feature2.getTheoPeakID()) {
								gt.addFeature(feature2);
								feature2.setAligned(true);
								break;
							}
						}
						
					}					
					
				}
				
				groundTruthEntries.add(gt);
				
			}
			
		}
		
		// clean up
		for (AlignmentFile file : alignmentFiles) {
			List<Feature> features = file.getFeatures();
			for (Feature feature : features) {
				feature.setAligned(false);
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
	
	private GenerativeMoleculeDB createDatabase(List<Molecule> molecules,
			List<Molecule> replacement, int numFiles) {
		
		GenerativeMoleculeDB database = new GenerativeMoleculeDB(params.getAdducts(), params);
		int subsIndex = 0;
		for (int i = 0; i < numFiles; i++) {
			
			List<Molecule> selected = new ArrayList<Molecule>();
			int originalCount = 0;
			int substituteCount = 0;
			
			if (params.getExpType() == GenerativeModelParameter.ExperimentType.TECHNICAL) {

				System.out.println("Technical replicates #" + i);
				selected.addAll(molecules);
				originalCount += selected.size();
			
			} else if (params.getExpType() == GenerativeModelParameter.ExperimentType.BIOLOGICAL) {

				System.out.println("Biological replicate #" + i);
				BetaDistribution beta = new BetaDistribution(params.getAlpha_a(), params.getAlpha_b());
				double a = beta.sample();
				if (params.getA() > 0) {
					a = params.getA();
				}
				System.out.println("a = " + a);
				originalCount = (int) (molecules.size() * a);
				substituteCount = molecules.size() - originalCount;
				
				// take originalCount amount of molecules from the original
				for (int j = 0; j < originalCount; j++) {
					selected.add(molecules.get(j));
				}
				
				// take substituteCount amount of molecules from the replacement
				int taken = 0;
				while (taken < substituteCount) {
					selected.add(replacement.get(subsIndex));
					subsIndex++;
					taken++;
				}
				
				// shuffle just to mess up the ordering
				Collections.shuffle(selected);
				
			}
			
			System.out.println("\tOriginal molecules = " + originalCount);
			System.out.println("\tSubstitute molecules = " + substituteCount);
			System.out.println("\t" + selected.size() + " inserted into database");

			for (Molecule molecule : selected) {
				database.insert(i, molecule); // also predict molecule's retention time inside
			}
			
		}
		
		return database;

	}

	private List<Molecule> loadMolecules(String path) {
		List<Molecule> molecules = new ArrayList<Molecule>();
		try {
			
			InputStream input = new FileInputStream(path);
			Map<String, Molecule> temp = new HashMap<String, Molecule>();
			temp.putAll(MoleculeIO.parseXml(input));
				
			for (Entry<String, Molecule> e : temp.entrySet()) {
				molecules.add(e.getValue());
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlParserException e) {
			e.printStackTrace();
		}
		return molecules;
	}
				
	private void initialiseClusters(GenerativeMolecule mol,
			int replicateIndex) {
		
		GenerativeFeatureClusterer clusterer = new GenerativeFeatureClusterer(params);
			
		// divide these features into clusters
		List<Feature> theoFeatures = mol.getTheoFeatures();
		assert(theoFeatures.size() != 0) : "Zero theoretical features for " + mol;
		List<GenerativeFeatureGroup> groups = clusterer.cluster(theoFeatures);
		
		// set parent metabolite & apply the warped RT to clusters based on parent's predicted RT
		for (GenerativeFeatureGroup group: groups) {
			group.setParent(mol);			
			double warpedRT = predictor.predict(group, replicateIndex);
			group.setWarpedRT(warpedRT);
		}

	}		
	
	/**
	 * Generate observed features from theoretical features
	 * @param theoFeatures The theoretical features
	 * @return The observed features
	 */
	private List<Feature> generateObservedFeatures(List<Feature> theoFeatures) {

		List<Feature> observedFeatures = new ArrayList<Feature>();
		int observedPeakID = 0;
//		Map<GenerativeMolecule, List<Feature>> molFeatures = new HashMap<GenerativeMolecule, List<Feature>>();
		for (Feature theoFeature : theoFeatures) {
			
			// features are observed only if it's below the intensity threshold and by its probability
			if (theoFeature.getIntensity() > params.getThreshold_q() && 
					Math.random() < params.getProbP()) {

				Feature observedFeature = new Feature(observedPeakID);
				observedPeakID++;
				
				// get the observed mass, intensity and RT
				int id = theoFeature.getPeakID();
				double mass = theoFeature.getMass();
				double intensity = theoFeature.getIntensity();

				NormalDistribution massDist = new NormalDistribution(mass, params.getSigma_m());
				NormalDistribution intensityDist = new NormalDistribution(intensity, params.getSigma_q());
				double observedMass = massDist.sample();
				double observedIntensity = intensityDist.sample();
				double observedRT = predictor.predict(theoFeature);
				
				if (observedRT < 0) {
					continue;
				}
				
				observedFeature.setMass(observedMass);
				observedFeature.setIntensity(observedIntensity);
				observedFeature.setRt(observedRT);
				observedFeature.setTheoPeakID(id);
				observedFeatures.add(observedFeature);
				
				GenerativeFeatureGroup group = (GenerativeFeatureGroup) theoFeature.getFirstGroup();
				GenerativeMolecule mol = group.getParent();
//				if (molFeatures.containsKey(mol)) {
//					molFeatures.get(mol).add(observedFeature);
//				} else {
//					List<Feature> newList = new ArrayList<Feature>();
//					newList.add(observedFeature);
//					molFeatures.put(mol, newList);
//				}

			}
				
		}
		
//		for (Entry<GenerativeMolecule, List<Feature>> e : molFeatures.entrySet()) {
//			System.out.println(e.getKey());
//			for (Feature f : e.getValue()) {
//				System.out.println("\t" + f);				
//			}
//		}
		
		return observedFeatures;

	}
	
}