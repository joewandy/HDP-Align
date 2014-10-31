package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import peakml.chemistry.Molecule;

import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPKeggQuery;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.precursorPrediction.AdductTransformComputer;
import com.joewandy.mzmatch.query.CompoundQuery;

public class HDPSamplerHandler {
	
	private List<HDPFile> hdpFiles;
	private List<HDPMetabolite> hdpMetabolites;
	private int totalPeaks;
	private Matrix resultMap;
	private double ppm;
	private int samplesTaken;
	
	private String mode;
	private AdductTransformComputer adductCalc;
	private List<String> adductList;
	private CompoundQuery idDatabase;
	private Map<HDPMetabolite, List<HDPPrecursorMass>> metabolitePrecursors;
	private HDPAnnotation<Feature> ionisationProductAnnotations;
	private HDPAnnotation<Feature> metaboliteAnnotations;
	
	public HDPSamplerHandler(List<HDPFile> hdpFiles, List<HDPMetabolite> hdpMetabolites, int totalPeaks, double ppm, String dbPath, String mode) {
	
		this.hdpFiles = hdpFiles;
		this.hdpMetabolites = hdpMetabolites;
		this.totalPeaks = totalPeaks;
		this.resultMap = new FlexCompRowMatrix(totalPeaks, totalPeaks); // TODO: don't use a matrix!
		this.ppm = ppm;

		if (dbPath != null) {
			this.idDatabase = new HDPKeggQuery(dbPath);			
			this.metaboliteAnnotations = new HDPAnnotation<Feature>();
		}
		
		if (mode != null) {
			this.mode = mode;
			if (mode.equals(MultiAlignConstants.IONISATION_MODE_POSITIVE)) {
				this.adductList = MultiAlignConstants.adductListPositive;				
			} else if (mode.equals(MultiAlignConstants.IONISATION_MODE_NEGATIVE)) {
				this.adductList = MultiAlignConstants.adductListNegative;								
			}
			this.adductCalc = new AdductTransformComputer(this.adductList);
			this.adductCalc.makeLists();
			this.metabolitePrecursors = new HashMap<HDPMetabolite, List<HDPPrecursorMass>>(); 		
			this.ionisationProductAnnotations = new HDPAnnotation<Feature>();
		}

				
	}

	public void handleSample(int s, int peaksProcessed, double timeTaken, HDPClusteringParam hdpParam, boolean lastSample) {

		int I = hdpMetabolites.size();
		
		boolean printMsg = true;	
		if ((s+1) > hdpParam.getBurnIn()) {
			if (printMsg) {
				System.out.print(String.format("Sample S#%05d ", (s+1)));					
			}
		} else {
			if (printMsg) {
				System.out.print(String.format("Sample B#%05d ", (s+1)));					
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if ((s+1) > hdpParam.getBurnIn()) {
			// store the actual samples
			sb.append(String.format("(%5.2fs) peaks=%d/%d I=%d ", timeTaken, peaksProcessed, totalPeaks, I));
			doProcess();
			samplesTaken++;
		} else {
			// discard the burn-in samples
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
		
		// extra stuff in the last sample
		if (lastSample) {
			
			// print peak RT vs. local cluster RT if reference file is enabled
			if (hdpParam.getRefFileIdx() != -1) {
				System.out.println("peakrt, tjk");
				for (HDPFile hdpFile : hdpFiles) {					
					for (int n = 0; n < hdpFile.N(); n++) {
						Feature f = hdpFile.getFeature(n);
						int k = hdpFile.Z(n);
						if (k != -1) {
							double tjk = hdpFile.tjk(k);
							System.out.println(f.getRt() + ", " + tjk);							
						}
					}
				}
			}
			
		}
		
	}
	
	public Matrix getResultMap() {
		return resultMap;
	}
	
	public HDPAnnotation<Feature> getIonisationProductAnnotations() {
		return ionisationProductAnnotations;
	}

	public HDPAnnotation<Feature> getMetaboliteAnnotations() {
		return metaboliteAnnotations;
	}
	
	public int getSamplesTaken() {
		return samplesTaken;
	}

	public Map<HDPMetabolite, List<HDPPrecursorMass>> getMetabolitePrecursors() {
		return metabolitePrecursors;
	}

	private void doProcess() {
		
		// track alignment probabilities
		updateResultMap();
	
		// annotate ionisation products
		if (mode != null) {
			annotateIP();			
		}
		
		// annotate metabolites
		if (idDatabase != null) {
			annotateMetabolites();			
		}
		
	}
	
	private void updateResultMap() {
		
		// for all metabolite
		for (int i = 0; i < hdpMetabolites.size(); i++) {

			HDPMetabolite met = hdpMetabolites.get(i);

			// for all mass clusters
			for (int a = 0; a < met.getA(); a++) {
				List <Feature> peaksInside = met.getPeaksInMassCluster(a);
				for (Feature f1 : peaksInside) {
					for (Feature f2 : peaksInside) {
						int m = f1.getSequenceID();
						int n = f2.getSequenceID();
						double currentValue = resultMap.get(m, n);
						double newValue = currentValue+1;
						resultMap.set(m, n, newValue);
					}
				}
			}
			
		}
		
	}
	
	private void annotateIP() {

		// precompute precursor masses for each mass cluster
		Map<HDPMassCluster, List<Double>> precursorMap = new HashMap<HDPMassCluster, List<Double>>();
		for (int i = 0; i < hdpMetabolites.size(); i++) {
			
			// for every mass cluster
			HDPMetabolite met = hdpMetabolites.get(i);
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
		for (int i = 0; i < hdpMetabolites.size(); i++) {
			
			// initialise the precursor map for each metablite
			HDPMetabolite met = hdpMetabolites.get(i);
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
		for (int i = 0; i < hdpMetabolites.size(); i++) {

			HDPMetabolite met = hdpMetabolites.get(i);
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
				double precursorMass = adductCalc.getPrecursorMass(mc.getTheta(), "M+H");
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
			
			if (precursorMass1.withinTolerance(precursorMass2)) {
				// if yes, then annotate peaks in both mass clusters with the adduct types
				for (Feature f1 : mc1.getPeakData()) {
					ionisationProductAnnotations.annotate(f1, adductList.get(c1));
				}
				for (Feature f2 : mc2.getPeakData()) {
					ionisationProductAnnotations.annotate(f2, adductList.get(c2));
				}
				return true;
			}

		}
		return false;

	}
	
	private void annotateMetabolites() {

		List<HDPMetabolite> inferredMetabolites = this.hdpMetabolites;
		
		for (HDPMetabolite met : inferredMetabolites) {
			
			// annotate all features inside with the mols
			for (HDPMassCluster mc : met.getMassClusters()) {
				HDPPrecursorMass pc = mc.getPrecursorMass();
				if (pc == null) {
					continue;
				}
				Set<Molecule> mols = pc.initMolecules();
				if (mols.isEmpty()) {
					continue;
				}
				for (Molecule mol : mols) {
					String msg = mol.getPlainFormula();
					for (Feature f : mc.getPeakData()) {
						metaboliteAnnotations.annotate(f, msg);
					}					
				}
			}

		}
				
	}	
		
}