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

import org.apache.commons.math3.distribution.NormalDistribution;

import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;

import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.FeatureGroup;
import com.joewandy.alignmentResearch.model.GenerativeFeatureClusterer;
import com.joewandy.alignmentResearch.model.GenerativeFeatureGroup;
import com.joewandy.alignmentResearch.model.GenerativeMolecule;
import com.joewandy.alignmentResearch.model.GenerativeMoleculeDB;
import com.joewandy.alignmentResearch.model.GroundTruth;
import com.joewandy.alignmentResearch.rtPrediction.RTPredictor;
import com.rits.cloning.Cloner;

import domsax.XmlParserException;

public class GenerativeModelDataGenerator extends BaseDataGenerator implements AlignmentDataGenerator {
	
	// path to molecule XML file
	private String molPath;
	
	// path to substitute molecule XML file
	private String subsPath;
	
	// parameters for generative models
	private GenerativeModelParameter params;
	private GenerativeFeatureClusterer clusterer;

	// RT predictor
	private RTPredictor predictor;
		
	private Random r;
	
	private List<Molecule> molecules;
	private List<Molecule> replacement;
	
	private Cloner cloner;

	
	public GenerativeModelDataGenerator(String molPath, GenerativeModelParameter params, int gtCombinationSize, boolean verbose) {

		super();
		
		this.molPath = molPath;
		this.subsPath = params.getReplacementMolsPath();
		this.params = params;
		this.predictor = new RTPredictor(params);
		this.r = new Random();
		this.verbose = verbose;
		this.clusterer = new GenerativeFeatureClusterer(params);

		if (verbose) {
			System.out.println("Loading molecules");			
		}
		this.molecules = loadMolecules(molPath);

		if (verbose) {
			System.out.println("Loading replacement");			
		}
		this.replacement = loadMolecules(subsPath);
		
		this.cloner = new Cloner();

	}
	
	@Override
	protected List<AlignmentFile> getAlignmentFiles(int currentIter) {
		
		int numFiles = params.getS();
		double a = params.getAs(currentIter);
		GenerativeMoleculeDB database = createDatabase(molecules, replacement,
				numFiles, a);
						
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
			if (verbose) {
				System.out.println("Total " + selectedMols.size() + " molecules selected with " + N + " theoretical features");				
			}

			// map between group ID and it's parent molecule ID
			Map<Integer, GenerativeFeatureGroup> groupIdMap = new HashMap<Integer, GenerativeFeatureGroup>();
						
			// collect all the theoretical features for each metabolite
			List<Feature> theoFeatures = new ArrayList<Feature>();
			for (GenerativeMolecule mol : selectedMols) {
				List<GenerativeFeatureGroup> groups = initialiseClusters(mol, i); // initialise RT clustering
				for (GenerativeFeatureGroup group : groups) {
					Integer groupID = group.getGroupID();
					groupIdMap.put(groupID, group);
				}
				List<Feature> molFeatures = mol.getTheoFeatures();
				theoFeatures.addAll(molFeatures);
			}
			
			// generate observed features from theoretical featues
			if (verbose) {
				System.out.println("Generating observed features ..");				
			}
			List<Feature> observedFeatures = generateObservedFeatures(theoFeatures, groupIdMap);
									
			String fileName = filePrefix + i + fileSuffix;
			AlignmentFile file = new AlignmentFile(i, fileName, observedFeatures);
			if (verbose) {
				System.out.println(file.getFilename() + " contains " + file.getFeaturesCount() + " observed features");				
			}

			alignmentDataList.add(file);
			
		}
		
		this.alignmentFiles = alignmentDataList;		
		return alignmentDataList;
		
	}
	
	@Override
	protected GroundTruth getGroundTruth() {
		
		List<FeatureGroup> groundTruthEntries = new ArrayList<FeatureGroup>();
		
		int groupID = 1;
		for (int i = 0; i < alignmentFiles.size(); i++) {
			
			AlignmentFile file1 = alignmentFiles.get(i);
			List<Feature> features1 = file1.getFeatures();

			for (Feature feature1 : features1) {
			
				if (feature1.isAligned()) {
					continue;
				}
				
				// add ourselves
				FeatureGroup gt = new FeatureGroup(groupID);
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
		if (verbose) {			
			System.out.println("Load ground truth = " + groundTruthEntries.size() + " rows");
		}

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
	
	private GenerativeMoleculeDB createDatabase(List<Molecule> molecules,
			List<Molecule> replacement, int numFiles, double a) {
		
		GenerativeMoleculeDB database = new GenerativeMoleculeDB(params.getAdducts(), params);
		int subsIndex = 0;
		for (int i = 0; i < numFiles; i++) {
			
			List<Molecule> selected = new ArrayList<Molecule>();
			int originalCount = 0;
			int substituteCount = 0;
			
			if (params.getExpType() == GenerativeModelParameter.ExperimentType.TECHNICAL) {

				if (verbose) {
					System.out.println("Technical replicates #" + i);					
				}
				selected.addAll(molecules);
				originalCount += selected.size();
			
			} else if (params.getExpType() == GenerativeModelParameter.ExperimentType.BIOLOGICAL) {

				if (verbose) {
					System.out.println("Biological replicate #" + i);
					System.out.println("a = " + a);					
				}
				originalCount = (int) (molecules.size() * a);
				substituteCount = molecules.size() - originalCount;
				
				// take originalCount amount of molecules from the original
				for (int j = 0; j < originalCount; j++) {
					Molecule original = molecules.get(j);
					Molecule clone = cloner.deepClone(original);
					selected.add(clone);
				}
				
				// take substituteCount amount of molecules from the replacement
				int taken = 0;
				Collections.shuffle(replacement); // remember to shuffle this to get random pick
				while (taken < substituteCount) {
					Molecule original = replacement.get(subsIndex);
					if (selected.contains(original)) {
						System.out.println("\t" + original + " already exists ... retrying");
					} else {
						Molecule clone = cloner.deepClone(original);
						selected.add(clone);
						taken++;						
					}
					subsIndex++;
				}
				
				// shuffle again just to mess up the ordering
				Collections.shuffle(selected);
				
			}
			
			if (verbose) {
				System.out.println("\tOriginal molecules = " + originalCount);
				System.out.println("\tSubstitute molecules = " + substituteCount);
				System.out.println("\t" + selected.size() + " inserted into database");				
			}

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
				
	private List<GenerativeFeatureGroup> initialiseClusters(GenerativeMolecule mol,
			int replicateIndex) {
					
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
		
		return groups;

	}		
	
	/**
	 * Generate observed features from theoretical features
	 * @param theoFeatures The theoretical features
	 * @param groupIdMap 
	 * @return The observed features
	 */
	private List<Feature> generateObservedFeatures(List<Feature> theoFeatures, Map<Integer, GenerativeFeatureGroup> groupIdMap) {

		List<Feature> observedFeatures = new ArrayList<Feature>();
		int observedPeakID = 0;
//		Map<GenerativeMolecule, List<Feature>> molFeatures = new HashMap<GenerativeMolecule, List<Feature>>();
		
		for (Feature theoFeature : theoFeatures) {
										
			// get the observed mass, intensity and RT
			int id = theoFeature.getPeakID();
			String adduct = theoFeature.getTheoAdductType();
			double logMass = Math.log(theoFeature.getMass());
			double logIntensity = Math.log(theoFeature.getIntensity());

			NormalDistribution massDist = new NormalDistribution(logMass, params.getSigma_m());
			NormalDistribution intensityDist = new NormalDistribution(logIntensity, params.getSigma_q());
			double observedMass = Math.exp(massDist.sample());
			double observedIntensity = Math.exp(intensityDist.sample());
			double observedRT = predictor.predict(theoFeature, groupIdMap);
						
			// skip if feature has intensity below threshold 
			if (observedIntensity < params.getThreshold_q()) {
				continue;
			}
			
			// or negative elution time (never appears)
			if (observedRT < 0) {
				continue;
			}
			
			if (Math.random() < params.getA()) {
				continue;
			}
			
			Feature observedFeature = new Feature(observedPeakID);
			observedFeature.setMass(observedMass);
			observedFeature.setIntensity(observedIntensity);
			observedFeature.setRt(observedRT);
			observedFeature.setTheoPeakID(id);
			observedFeature.setTheoAdductType(adduct);
			
			Integer groupID = theoFeature.getGroupID();
			GenerativeFeatureGroup group = groupIdMap.get(groupID);
			GenerativeMolecule met = group.getParent();
			String metaboliteID = met.getId();
			observedFeature.setMetaboliteID(metaboliteID);
			observedFeature.setSynthetic(true);
			
			observedFeatures.add(observedFeature);
			observedPeakID++;
			
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