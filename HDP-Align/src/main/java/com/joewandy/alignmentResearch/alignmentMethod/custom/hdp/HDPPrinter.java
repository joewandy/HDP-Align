package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.HDPAnnotation;
import com.joewandy.alignmentResearch.model.HDPAnnotationItem;
import com.joewandy.alignmentResearch.model.HDPClustering;
import com.joewandy.alignmentResearch.model.HDPMassCluster;
import com.joewandy.alignmentResearch.model.HDPMetabolite;

public class HDPPrinter {
	
	private List<Feature> allFeatures;
	private Map<String, String> compoundGroundTruthDatabase;

	public HDPPrinter(List<Feature> allFeatures, Map<String, String> compoundGroundTruthDatabase) {
		this.allFeatures = allFeatures;
		this.compoundGroundTruthDatabase = compoundGroundTruthDatabase;
	}
	
	public void printHeader() {
		System.out.println();
		System.out.println("=============================================");
		System.out.println("HDP-ALIGN RESULTS");
		System.out.println("=============================================");
		System.out.println();
	}
	
	public Map<Feature, String> initialiseFeatureAnnotations(HDPClustering clustering) {
		
		System.out.println("INITIALISING FEATURES ANNOTATIONS");
		Map<Feature, String> messages = new HashMap<Feature, String>();
		
		HDPAnnotation<Feature> ipAnnotations = clustering
				.getIonisationProductFeatureAnnotations();
		HDPAnnotation<Feature> isotopeAnnotations = clustering
				.getIsotopeFeatureAnnotations();
		HDPAnnotation<Feature> metaboliteFeatureAnnotations = clustering
				.getMetaboliteFeatureAnnotations();

		if (ipAnnotations != null) {
			System.out.println("Ionisation product annotations size = " + ipAnnotations.size());			
		} else if (isotopeAnnotations != null) {
			System.out.println("Isotope annotations size = " + isotopeAnnotations.size());			
		} else if (metaboliteFeatureAnnotations != null) {
			System.out.println("Metabolite annotations size = " + metaboliteFeatureAnnotations.size());			
		} else {
			return messages;
		}
		
		int correctIPCount = 0;
		int nonAmbiguousIPCount = 0;
		int ambiguousIPCount = 0;
		
		Set<String> metaboliteFoundInDB = new HashSet<String>();
		Set<String> metaboliteNotFoundInDB = new HashSet<String>();

		// for all features in all data files
		for (Feature feature : allFeatures) {

			StringBuilder msgBuilder = new StringBuilder();
			
			// do isotope annotations
			if (isotopeAnnotations != null) {

				// first find the isotope annotations for this peak ...
				HDPAnnotationItem featureIsotopes = isotopeAnnotations.get(feature);

				// if there's any ..
				if (featureIsotopes != null) {
					
					// computes the total frequencies of all annotations
					double sum = 0;
					for (Entry<String, Integer> e2 : featureIsotopes.entrySet()) {
						sum += e2.getValue();
					}
					
					// normalise frequency by sum
					for (Entry<String, Integer> e2 : featureIsotopes.entrySet()) {
						int count = e2.getValue();
						double prob = (count) / sum;
						msgBuilder.append("\t\t\tISOTOPE " + e2.getKey() + "="
								+ String.format("%.2f", prob));
						msgBuilder.append("\n");
					}
										
				}				
								
			}
			
			// do metabolite annotations
			if (metaboliteFeatureAnnotations != null) {

				// first find the metabolite annotations for this feature
				HDPAnnotationItem featureMets = metaboliteFeatureAnnotations.get(feature);

				// if there's any ..
				if (featureMets != null) {
					
					// computes the total frequencies of all annotations
					double sum = 0;
					for (Entry<String, Integer> e2 : featureMets.entrySet()) {
						sum += e2.getValue();
					}
					
					// normalise frequency by sum
					for (Entry<String, Integer> e2 : featureMets.entrySet()) {
						String key = e2.getKey();
						int count = e2.getValue();
						double prob = (count) / sum;
						msgBuilder.append("\t\t\tMETABOLITE " + e2.getKey() + "="
								+ String.format("%.2f", prob));
						msgBuilder.append("\n");						
						// for debugging only, compare against ground truth
						if (compoundGroundTruthDatabase != null && 
								compoundGroundTruthDatabase.containsKey(key)) {
							metaboliteFoundInDB.add(key);
						} else {
							metaboliteNotFoundInDB.add(key);
						}
					}
										
				}
	
			}
			
			// do ionisation product annotations
			if (ipAnnotations != null) {
				
				// first find the adduct annotations for this peak ...
				HDPAnnotationItem featureIPs = ipAnnotations.get(feature);

				// if there's any ..
				if (featureIPs != null) {
				
					// computes the total frequencies of all annotations
					double sum = 0;
					for (Entry<String, Integer> e2 : featureIPs.entrySet()) {
						sum += e2.getValue();
					}
					
					// normalise frequency by sum
					double maxProb = 0;
					String msg = null;
					for (Entry<String, Integer> e2 : featureIPs.entrySet()) {
						int count = e2.getValue();
						double prob = (count) / sum;
						msgBuilder.append("\t\t\tADDUCT " + e2.getKey() + "="
								+ String.format("%.2f", prob));
						msgBuilder.append("\n");						
						if (prob > maxProb) {
							maxProb = prob;
							msg = e2.getKey();
						}
					}
					
					// for debugging only, compare against ground truth
					if (msg != null
							&& msg.equals(feature.getTheoAdductType())) {
						correctIPCount++;						
						if (featureIPs.entrySet().size() == 1) {
							nonAmbiguousIPCount++;
						} else {
							ambiguousIPCount++;
						}
					}
					
				}				
				
			}
			
			if (msgBuilder.length() != 0) {
				String msg = msgBuilder.toString();
				messages.put(feature, msg);				
			}
						
		} // end feature loop

		// print some overall statistics

		if (ipAnnotations != null) {
		
			System.out.println("Total IP annotations = " + ipAnnotations.size()
					+ "/" + allFeatures.size());
			System.out.println("Total correct IP annotations = "
					+ correctIPCount + "/" + ipAnnotations.size());
			System.out.println("Total nonambiguous correct IP annotations = "
					+ nonAmbiguousIPCount + "/" + correctIPCount);
			System.out.println("Total ambiguous correct IP annotations = "
					+ ambiguousIPCount + "/" + correctIPCount);
			
		}
		
		if (metaboliteFeatureAnnotations != null && compoundGroundTruthDatabase != null) {
		
			System.out.println("Metabolite DB size = " + compoundGroundTruthDatabase.size());			

			double ratio = ((double)metaboliteFoundInDB.size()) / metaboliteNotFoundInDB.size();
			System.out.println("Metabolites matching vs. non-matching = " + metaboliteFoundInDB.size()
					+ "/" + metaboliteNotFoundInDB.size() + " ratio = " + ratio);
			System.out.println("Matching found = ");
			for (String met : metaboliteFoundInDB) {
				System.out.println("\t- " + met);
			}
						
		}
		
		return messages;
				
	}
		
	public void printLastSample(HDPClustering clustering, Map<Feature, String> messages) {

		System.out.println("LAST SAMPLE");
		HDPSingleSample lastSample = clustering.getLastSample();

		HDPAnnotation<HDPMetabolite> metaboliteAnnotations = clustering
				.getMetaboliteAnnotations();
		if (metaboliteAnnotations == null) {
			return;
		}
		System.out.println("Metabolite annotations size = " + metaboliteAnnotations.size());			

		List<HDPMetabolite> metabolites = lastSample.getMetabolites();
		for (HDPMetabolite met : metabolites) {

			// print putative identities
			System.out.println(met);
			HDPAnnotationItem annot = metaboliteAnnotations.get(met);
			System.out.println("Putative identities");
			if (annot != null) {
				System.out.println(annot);
			} else {
				System.out.println("\t- no metabolite annotations -");
			}
			
			// print mass clusters and the features inside
			System.out.println("Mass clusters");
			List<HDPMassCluster> massClusters = met.getMassClusters();
			for (HDPMassCluster mc : massClusters) {
				System.out.println("\t" + mc);
				for (Feature f : mc.getPeakData()) {
					System.out.println("\t\t" + f);
					String msg = messages.get(f);
					if (msg != null) {
						System.out.print(msg);											
					}
				}
			}
		
			System.out.println();
			
		}
			
	}	
	
}