package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import peakml.chemistry.Molecule;

import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPKeggQuery;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPMassClusterFeatures;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPResults;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPResultsSample;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.precursorPrediction.AdductTransformComputer;
import com.joewandy.mzmatch.query.CompoundQuery;

public class HDPSamplerHandler {
	
	private List<HDPMetabolite> hdpMetabolites;
	private int totalPeaks;
	private HDPResults results;
	private double ppm;
	private int samplesTaken;
	
	private String mode;
	private AdductTransformComputer adductCalc;
	private List<String> adductList;
	private CompoundQuery idDatabase;
	private Map<HDPMetabolite, List<HDPPrecursorMass>> metabolitePrecursors;
	
	private HDPAnnotation<HDPPrecursorMass> isotopePrecursorMassAnnots;
	private HDPAnnotation<Feature> isotopeFeatureAnnots;
	private HDPAnnotation<Feature> ionisationProductFeatureAnnotations;
	private HDPAnnotation<Feature> metaboliteFeatureAnnotations;
	
	public HDPSamplerHandler(List<HDPMetabolite> hdpMetabolites, 
			int totalPeaks, double ppm, String dbPath, String mode) {
	
		this.hdpMetabolites = hdpMetabolites;
		this.totalPeaks = totalPeaks;
		this.results = new HDPResults();
		this.ppm = ppm;

		if (dbPath != null) {
			this.idDatabase = new HDPKeggQuery(dbPath);			
			this.metaboliteFeatureAnnotations = new HDPAnnotation<Feature>();
		}
		
		if (mode != null) {
			this.mode = mode;
			if (mode.toLowerCase().equals(MultiAlignConstants.IONISATION_MODE_POSITIVE)) {
				this.adductList = MultiAlignConstants.adductListPositive;				
			} else if (mode.toLowerCase().equals(MultiAlignConstants.IONISATION_MODE_NEGATIVE)) {
				this.adductList = MultiAlignConstants.adductListNegative;								
			}
			this.adductCalc = new AdductTransformComputer(this.adductList);
			this.adductCalc.makeLists();
			
			this.metabolitePrecursors = new HashMap<HDPMetabolite, List<HDPPrecursorMass>>(); 		
			this.ionisationProductFeatureAnnotations = new HDPAnnotation<Feature>();
			this.isotopePrecursorMassAnnots = new HDPAnnotation<HDPPrecursorMass>();
			this.isotopeFeatureAnnots = new HDPAnnotation<Feature>();

		}

				
	}

	public void storeSample(int s, int peaksProcessed, double timeTaken, HDPClusteringParam hdpParam, boolean lastSample) {

		int I = hdpMetabolites.size();
		
		boolean printMsg = true;	
		if ((s+1) > hdpParam.getBurnIn()) {
			if (printMsg) {
				System.out.print(String.format("Sample S#%05d ", (s+1)));		
				saveResults();
				samplesTaken++;
			}
		} else {
			// ignore the burn-in samples
			if (printMsg) {
				System.out.print(String.format("Sample B#%05d ", (s+1)));					
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if ((s+1) > hdpParam.getBurnIn()) {
			sb.append(String.format("(%5.2fs) peaks=%d/%d I=%d ", timeTaken, peaksProcessed, totalPeaks, I));
		} else {
			sb.append(String.format("(%5.2fs) peaks=%d/%d I=%d ", timeTaken, peaksProcessed, totalPeaks, I));			
		}
		
		sb.append("all_A = [");
		for (int i = 0; i < I; i++) {
			HDPMetabolite met = hdpMetabolites.get(i);
			int A = met.getA();
			String formatted = String.format(" %3d", A);
			sb.append(formatted);
		}
		sb.append(" ]");

		if (printMsg) {
			System.out.println(sb.toString());				
		}
				
	}
		
	public HDPResults getResultMap() {
		return results;
	}
	
	public HDPAnnotation<Feature> getIonisationProductAnnotations() {
		return ionisationProductFeatureAnnotations;
	}

	public HDPAnnotation<Feature> getMetaboliteAnnotations() {
		return metaboliteFeatureAnnotations;
	}
	
	public HDPAnnotation<Feature> getIsotopeAnnotations() {
		return isotopeFeatureAnnots;
	}

	public void setIsotopeFeatureAnnots(HDPAnnotation<Feature> isotopeFeatureAnnots) {
		this.isotopeFeatureAnnots = isotopeFeatureAnnots;
	}

	public int getSamplesTaken() {
		return samplesTaken;
	}

	public Map<HDPMetabolite, List<HDPPrecursorMass>> getMetabolitePrecursors() {
		return metabolitePrecursors;
	}
	
	public void processSample() {
		
		int counter = 1;
		for (HDPResultsSample sampleResult : results.getResultsSamples()) {		
			System.out.println("Processing sample " + counter + "/" + results.getResultsSamplesCount());
			doProcess(sampleResult);
			counter++;
		}
		
	}	
	
	private void saveResults() {		
		results.store(hdpMetabolites);
	}
	
	private void doProcess(HDPResultsSample resultsSample) {
				
		List<HDPMetabolite> metabolites = resultsSample.getMetabolites();
		
		// track alignment probabilities
		updateResultMap(metabolites);
	
		// annotate ionisation products
		if (mode != null) {
			annotateIP(metabolites);			
		}
		
		// annotate metabolites
		if (idDatabase != null) {
			annotateMetabolites(metabolites);			
		}
		
	}
		
	private void updateResultMap(List<HDPMetabolite> metabolites) {
		
		// for all metabolite
		for (int i = 0; i < metabolites.size(); i++) {

			HDPMetabolite met = metabolites.get(i);

			// for all mass clusters
			for (int a = 0; a < met.getA(); a++) {
				// accumulate the frequencies of the set of peaks inside
				List <Feature> peaksInside = met.getPeaksInMassCluster(a);
				Set<Feature> features = new HashSet<Feature>(peaksInside);
				HDPMassClusterFeatures item = new HDPMassClusterFeatures(features);
				results.store(item);					
			}
			
		}
		
	}
	
	private void annotateIP(List<HDPMetabolite> metabolites) {

		// precompute precursor masses for each mass cluster
		Map<HDPMassCluster, List<Double>> precursorMap = new HashMap<HDPMassCluster, List<Double>>();
		for (int i = 0; i < metabolites.size(); i++) {
			
			// for every mass cluster
			HDPMetabolite met = metabolites.get(i);
			for (int j = 0; j < met.getMassClusters().size(); j++) {				

				// use the calculator to compute precursor masses under possible adduct transformations
				HDPMassCluster mc = met.getMassClusters().get(j);				
				double ionMass = Math.exp(mc.getTheta());
				List<Double> precursorMasses = adductCalc.getPrecursorMass(ionMass);
				assert(adductList.size() == precursorMasses.size());
				
				// store this for later use
				precursorMap.put(mc, precursorMasses);

			}
			
		}
		
		// then build the 'consensus' counts of precursor masses
		for (int i = 0; i < metabolites.size(); i++) {
			
			// initialise the precursor map for each metablite
			HDPMetabolite met = metabolites.get(i);
			List<HDPPrecursorMass> precursorList = metabolitePrecursors.get(met);
			if (precursorList == null) {
				precursorList = new ArrayList<HDPPrecursorMass>();
				metabolitePrecursors.put(met, precursorList);
			}

			// for all mass clusters inside this metabolite
			for (int j = 0; j < met.getMassClusters().size(); j++) {
				
				// first determine all the possible precursor masses for this mass cluster
				HDPMassCluster mc1 = met.getMassClusters().get(j);				
				List<Double> precursorMasses1 = precursorMap.get(mc1);
				
				// then check if there's any other mass cluster sharing the same precursor mass
				boolean found = false;
				for (int k = j+1; k < met.getMassClusters().size(); k++) {
					
					HDPMassCluster mc2 = met.getMassClusters().get(k);				
					List<Double> precursorMasses2 = precursorMap.get(mc2);

					// if yes, then annotate both mass clusters
					for (int c1 = 0; c1 < precursorMasses1.size(); c1++) {
						// get the precomputed value
						double precursorMass1 = precursorMasses1.get(c1);
						if (precursorMass1 > 0) {
						
							// find the precursor mass object first
							HDPPrecursorMass precursor = null;
							for (HDPPrecursorMass pc : precursorList) {
								if (pc.withinTolerance(precursorMass1)) {
									precursor = pc;
									break;
								}
							}
							// make a new precursor mass if necessary
							boolean newPc = false;
							if (precursor == null) {
								precursor = new HDPPrecursorMass(precursorMass1, this.ppm, this.idDatabase);
								newPc = true;
							}
							// search for matching results in precursorMasses2 and annotate features if found
							found = findAndAnnotateIP(precursor, precursorMasses2, mc1,
									mc2, c1);
							if (found) {
								if (newPc) {
									mc1.setPrecursorMass(precursor);
									mc2.setPrecursorMass(precursor);
									precursorList.add(precursor);								
								} else {
									precursor.incrementCount();
								}
								break;
							}

							
						}						
					}

				}
				
			}
						
		}
		
		/*
		 * if there's any metabolite without any consensus precursor mass, then
		 * take the highest intensity peak and use that to annotate with M+H / M-H
		 */
		for (int i = 0; i < metabolites.size(); i++) {

			HDPMetabolite met = metabolites.get(i);
			List<HDPPrecursorMass> precursorList = metabolitePrecursors.get(met);
			assert(precursorList != null); // cannot be null
			
			// if no precursors
			if (precursorList.isEmpty()) {
				
				// find the most intense peak and its parent mass cluster
				double maxIntensity = 0;
				Feature selectedPeak = null;
				for (Feature f : met.getPeakData()) {
					if (f.getIntensity() > maxIntensity) {
						maxIntensity = f.getIntensity();
						selectedPeak = f;
					}
				}
				HDPMassCluster mc = met.getMassClusterOfPeak(selectedPeak);

				// get the precursor mass under M+H / M-H, and use this
				String defaultAdduct = "";
				if (mode.toLowerCase().equals(MultiAlignConstants.IONISATION_MODE_POSITIVE)) {
					defaultAdduct = "M+H";				
				} else if (mode.toLowerCase().equals(MultiAlignConstants.IONISATION_MODE_NEGATIVE)) {
					defaultAdduct = "M-H";				
				}				
				double precursorMass = adductCalc.getPrecursorMass(mc.getTheta(), defaultAdduct);
				HDPPrecursorMass precursor = new HDPPrecursorMass(precursorMass, this.ppm, this.idDatabase);

				mc.setPrecursorMass(precursor);
				precursorList.add(precursor);
				
			}
			
		}
		
				
	}

	private boolean findAndAnnotateIP(HDPPrecursorMass precursorMass1,
			List<Double> precursorMasses2, HDPMassCluster mc1,
			HDPMassCluster mc2, int c1) {
		
		for (int c2 = 0; c2 < precursorMasses2.size(); c2++) {
	
			double precursorMass2 = precursorMasses2.get(c2);
			if (precursorMass2 < 0) {
				continue;
			}
			
			// check mass tolerance to determine if precursorMass2 is an adduct type
			if (precursorMass1.withinTolerance(precursorMass2)) {
				// if yes, then annotate peaks in both mass clusters with the respective adduct types
				for (Feature f1 : mc1.getPeakData()) {
					ionisationProductFeatureAnnotations.annotate(f1, adductList.get(c1));
				}
				for (Feature f2 : mc2.getPeakData()) {
					ionisationProductFeatureAnnotations.annotate(f2, adductList.get(c2));
				}				
				return true;
			}

		}
		return false;

	}
	
	private void annotateMetabolites(List<HDPMetabolite> metabolites) {

		List<HDPMetabolite> inferredMetabolites = metabolites;
		
		for (HDPMetabolite met : inferredMetabolites) {
			
			// first annotate each precursorMass by isotope, if any
			annotateIsotopes(met);
			
			// for every mass cluster inside met, annotate features by molecule identity
			for (HDPMassCluster mc : met.getMassClusters()) {

				HDPPrecursorMass pc = mc.getPrecursorMass();
				
				// if no precursor mass, skip
				if (pc == null) {
					continue;
				}
				
				// if isotope mass cluster, skip too
				HDPAnnotationItem isoAnnot = isotopePrecursorMassAnnots.get(pc);
				if (isoAnnot != null) {
					// but annotate all peaks inside by this isotope type first
					for (Feature f : mc.getPeakData()) {
						for (Entry<String, Integer> e : isoAnnot.entrySet()) {
							String msg = e.getKey();
							isotopeFeatureAnnots.annotate(f, msg);
						}
					}
					continue;
				}
				
				// initialise the molecule identity for this mass cluster
				Set<Molecule> mols = pc.initMolecules();
				if (mols.isEmpty()) {
					continue;
				}
				
				// annotate all the peaks inside with mol
				for (Molecule mol : mols) {
					String msg = mol.getPlainFormula();
					for (Feature f : mc.getPeakData()) {
						metaboliteFeatureAnnotations.annotate(f, msg);
					}					
				}

			}

		}
				
	}

	private void annotateIsotopes(HDPMetabolite met) {

		// for all precursor masses
		Set<HDPPrecursorMass> pcs = met.getPrecursorMasses();

		// compare against each other
		for (HDPPrecursorMass pc1 : pcs) {
			for (HDPPrecursorMass pc2 : pcs) {
				
				if (pc1.equals(pc2)) {
					continue;
				}

				// check using various heuristics to find which masses are the isotopes
				double[] isotopeDiffs = new double[] {
						13.00335483780 - 12.00000000000, 	// carbon
						15.00010889840 - 14.00307400524, 	// nitrogen
						17.99916040000 - 15.99491462210, 	// oxygen
						33.96786683000 - 31.97207069000}; 	// sulfur
				String[] isotopeLabels = new String[] {
						"C13", "N15", "O18", "S34" 
				};
				assert(isotopeDiffs.length == isotopeLabels.length);
				
				// check the masses to annotate isotopes
				for (int k = 0; k < isotopeDiffs.length; k++) {
					double diff = isotopeDiffs[k];
					String label = isotopeLabels[k];
					if (pc2.withinTolerance(pc1.getMass()+diff)) {
						// pc2 is probably an isotope of pc1 if within tolerance
						String annotation = label + " of " + pc1.getMass();
						isotopePrecursorMassAnnots.annotate(pc2, annotation);
					}
				}

			}
		}
	}
		
}