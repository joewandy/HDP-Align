package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

import com.joewandy.alignmentResearch.precursorPrediction.AdductTransformComputer;

public class HDPSamplerHandler {

	private static final double MASS_EPSILON = 0.005;
	
	private List<HDPFile> hdpFiles;
	private List<HDPMetabolite> hdpMetabolites;
	private Matrix resultMap;
	private int samplesTaken;
	private int totalPeaks;
	
	private AdductTransformComputer adductCalc;
	private List<String> adductList;
	private Map<Feature, Map<String, Integer>> ipMap;
	
	public HDPSamplerHandler(List<HDPFile> hdpFiles, List<HDPMetabolite> hdpMetabolites, int totalPeaks) {
	
		this.hdpFiles = hdpFiles;
		this.hdpMetabolites = hdpMetabolites;
		this.totalPeaks = totalPeaks;
		this.resultMap = new FlexCompRowMatrix(totalPeaks, totalPeaks);
		this.ipMap = new HashMap<Feature, Map<String, Integer>>();
				
		adductList = new ArrayList<String>();
		adductList.add("M+3H");
		adductList.add("M+2H+Na");
		adductList.add("M+H+2Na");
		adductList.add("M+3Na");
		adductList.add("M+2H");
		adductList.add("M+H+NH4");
		adductList.add("M+H+Na");
		adductList.add("M+H+K");
		adductList.add("M+ACN+2H");
		adductList.add("M+2Na");
		adductList.add("M+2ACN+2H");
		adductList.add("M+3ACN+2H");
		adductList.add("M+H");
		adductList.add("M+NH4");
		adductList.add("M+Na");
		adductList.add("M+CH3OH+H");
		adductList.add("M+K");
		adductList.add("M+ACN+H");
		adductList.add("M+2Na-H");
		adductList.add("M+IsoProp+H");
		adductList.add("M+ACN+Na");
		adductList.add("M+2K-H");
		adductList.add("M+DMSO+H");
		adductList.add("M+2ACN+H");
		adductList.add("M+IsoProp+Na+H");
		adductList.add("2M+H");
		adductList.add("2M+NH4");
		adductList.add("2M+Na");
		adductList.add("2M+3H2O+2H");
		adductList.add("2M+K");
		adductList.add("2M+ACN+H");
		adductList.add("2M+ACN+Na");
		this.adductCalc = new AdductTransformComputer(adductList);
		this.adductCalc.makeLists();

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
		
		// print whole bunch of extra stuff in the last sample
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
	
	public Map<Feature, Map<String, Integer>> getIpMap() {
		return ipMap;
	}

	public int getSamplesTaken() {
		return samplesTaken;
	}

	private void doProcess() {
		
		// track alignment probabilities
		updateResultMap();
	
		// annotate ionisation products
		annotateIP();
		
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
	
		// for all metabolite
		for (int i = 0; i < hdpMetabolites.size(); i++) {

			// for all mass clusters inside
			HDPMetabolite met = hdpMetabolites.get(i);
			for (HDPMassCluster mc1 : met.getMassClusters()) {
				
//				if (mc1.getCountPeaks() == 2) {
//					System.out.println(mc1.getCountPeaks());
//				}

				/* 
				 * a mass cluster should correspond to an ion mass, so if this
				 * comes from an adduct, we work out the expected precursor mass
				 */
				double ionMass = Math.exp(mc1.getTheta());
				List<Double> predictedMasses = adductCalc.getPrecursorMass(ionMass);
				assert(adductList.size() == predictedMasses.size());
				for (HDPMassCluster mc2 : met.getMassClusters()) {
					if (mc1.equals(mc2)) {
						continue;
					} else {
						// check the mass of mc2 to see if it matches our list
						double toCheck = Math.exp(mc2.getTheta());
						for (int c = 0; c < adductList.size(); c++) {
							double predictedMass = predictedMasses.get(c);
							if (massWithinTolerance(toCheck, predictedMass)) {
//								System.out.println("\t" + mc1 + " " + adductList.get(c) + " matches " + mc2 + " due to " + predictedMass);	
								for (Feature f1 : mc1.getPeakData()) {
									annotate(adductList.get(c), f1);
									for (Feature f2 : mc2.getPeakData()) {
										annotate("PRECURSOR", f2);
									}
								}
							}
						}
						
					}
				}
				
			}
			
		}
				
	}

	private void annotate(String msg, Feature f) {
		Map<String, Integer> annots = this.ipMap.get(f);
		if (annots == null) {
			annots = new HashMap<String, Integer>();
			annots.put(msg, 1);
		} else {
			assert(annots != null);
			Integer count = annots.get(msg);
			if (count != null) {
				annots.put(msg, count+1); 				
			} else {
				annots.put(msg, 1);
			}
		}
		this.ipMap.put(f, annots);
	}
	
	private boolean massWithinTolerance(double mass1, double mass2) {
		double upper = mass1 + HDPSamplerHandler.MASS_EPSILON;
		double lower = mass1 - HDPSamplerHandler.MASS_EPSILON;
		if (lower < mass2 && mass2 < upper) {
			return true;
		} else {
			return false;
		}		
	}
	
}