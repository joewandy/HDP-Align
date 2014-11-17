package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPResultItem;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpProbabilityMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HDPResults;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.HDPAnnotation;
import com.joewandy.alignmentResearch.objectModel.HDPAnnotationItem;
import com.joewandy.alignmentResearch.objectModel.HDPClustering;
import com.joewandy.alignmentResearch.objectModel.HDPMassRTClustering;

/**
 * An alignment method using Hierarchical Dirichlet Process mixture model
 * @author joewandy
 *
 */
public class HdpAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;			// The input files to be aligned
	private List<Feature> allFeatures;				// All the features across all files
	private int totalPeaks;							// Count of total number of peaks
	private AlignmentMethodParam param;				// Alignment parameters	

	private HDPResults results;
	private Map<String, String> compoundGroundTruthDatabase;
	
	/**
	 * Constructs an instance of HDPAlignment object
	 * @param dataList The input files
	 * @param param Alignment parameters
	 */
	public HdpAlignment(List<AlignmentFile> dataList,
			AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		this.allFeatures = new ArrayList<Feature>();
		for (AlignmentFile file : dataList) {
			this.totalPeaks += file.getFeaturesCount();
			this.allFeatures.addAll(file.getFeatures());
		}
		this.param = param;
		
		this.results = new HDPResults();
		String databaseFile = param.getGroundTruthDatabase();
		this.compoundGroundTruthDatabase = loadGroundTruthDB(databaseFile);

	}

	@Override
	public AlignmentList matchFeatures() {
		
		AlignmentList masterList = new AlignmentList("");
		int samplesTaken = 0;
		
		// use the java HDP Mass-RT clustering			
		if (MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT_JAVA
				.equals(this.param.getScoringMethod())) {

			// run the HDP RT+mass clustering
			HDPClustering clustering = new HDPMassRTClustering(dataList,
					param);
			clustering.runClustering();

			// process the matching results
			samplesTaken = clustering.getSamplesTaken();
			System.out.println("Samples taken = " + samplesTaken);
			results = clustering.getResults();

			// print out all the partners of each peak
			for (Entry<HDPResultItem, Integer> entry : results.getEntries()) {
				HDPResultItem item = entry.getKey();
				int count = results.getCount(item);
				double prob = ((double)count) / samplesTaken;
				System.out.println(item + " has probability " + prob );
			}
						
			// do post-processing of annotations
			postProcessAnnotations(clustering);
			
			// process the ionisation product and metabolite annotations
			printAnnotations(clustering);

		} else if (MultiAlignConstants.SCORING_METHOD_HDP_RT_JAVA
				.equals(this.param.getScoringMethod())) {

			// use the java HDP RT clustering

		}
		
		// construct the actual feature matching here 
		FeatureMatching matcher = new HdpProbabilityMatching(results,
				dataList, samplesTaken);
		masterList = matcher.getMatchedList();	
		return masterList;
		
	}
	
	/**
	 * Load identification database for evaluation 
	 * @param databaseFile the standards database file
	 * @return Map of database formula to the line
	 */
	private Map<String, String> loadGroundTruthDB(String databaseFile) {

		if (databaseFile == null) {
			return null;
		}
		
		Map<String, String> database = new HashMap<String, String>();
		try {
			List<String> lines = Files.readAllLines(Paths.get(databaseFile),
					Charset.defaultCharset());
			for (String line : lines) {
				String[] tokens = line.split(",");
				String formula = tokens[2].trim();
				database.put(formula, line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Ground truth database=" + database);
		return database;
	
	}
		
	/**
	 * Gets the Matlab file reader for the results
	 * @param dataList The input files
	 * @return The matlab file reader
	 */
	private MatFileReader getMfr(List<AlignmentFile> dataList) {

		AlignmentFile firstFile = dataList.get(0);
		String parentPath = firstFile.getParentPath();
		MatFileReader mfr = null;

		try {
			if (MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT
					.equals(this.param.getScoringMethod())) {
				// clustering results by RT + mass
				mfr = new MatFileReader(parentPath
						+ "/csv/hdp_result_rt_mass.mat");
			} else if (MultiAlignConstants.SCORING_METHOD_HDP_RT
					.equals(this.param.getScoringMethod())) {
				// clustering results by RT only
				mfr = new MatFileReader(parentPath + "/csv/hdp_result_rt.mat");
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		return mfr;

	}

	/**
	 * Annotates features by inferred ionisation products
	 * @param clustering The HDP clustering results
	 */
	private void postProcessAnnotations(HDPClustering clustering) {
		
		HDPAnnotation<Feature> ipAnnotations = clustering
				.getIonisationProductAnnotations();
		HDPAnnotation<Feature> isotopeAnnotations = clustering
				.getIsotopeAnnotations();
		HDPAnnotation<Feature> metaboliteAnnotations = clustering
				.getMetaboliteAnnotations();
		
		if (ipAnnotations != null) {
			System.out.println("Ionisation product annotations size = " + ipAnnotations.size());			
		}		
		if (isotopeAnnotations != null) {
			System.out.println("Isotope annotations size = " + isotopeAnnotations.size());			
		}
		if (metaboliteAnnotations != null) {
			System.out.println("Metabolite annotations size = " + metaboliteAnnotations.size());			
		}
		
		// for all features in all data files
		int metAnnotCleared = 0;
		for (Feature feature : allFeatures) {

			// remove its metabolite annotations if the feature has a putative isotope annotation
			if (isotopeAnnotations.contains(feature)) {
				metaboliteAnnotations.clear(feature);
				metAnnotCleared++;
			}
			
		}
		System.out.println("Features with metabolite annotations removed = " + metAnnotCleared + "/" + allFeatures.size());

	}
	
	/**
	 * Annotates features by inferred ionisation products
	 * @param clustering The HDP clustering results
	 */
	private void printAnnotations(HDPClustering clustering) {
		
		HDPAnnotation<Feature> ipAnnotations = clustering
				.getIonisationProductAnnotations();
		HDPAnnotation<Feature> isotopeAnnotations = clustering
				.getIsotopeAnnotations();
		HDPAnnotation<Feature> metaboliteAnnotations = clustering
				.getMetaboliteAnnotations();
				
		int correctIPCount = 0;
		int nonAmbiguousIPCount = 0;
		int ambiguousIPCount = 0;
		
		Set<String> metaboliteFoundInDB = new HashSet<String>();
		Set<String> metaboliteNotFoundInDB = new HashSet<String>();

		// for all features in all data files
		for (Feature feature : allFeatures) {

			System.out.println(feature.getPeakID() + " " + feature.getMass() + " "
					+ feature.getRt() + " " + feature.getIntensity() + " "
					+ feature.getTheoAdductType());

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
						System.out.println("\tISOTOPE " + e2.getKey() + "="
								+ String.format("%.2f", prob));
					}
										
				}				
								
			}
			
			// do metabolite annotations
			if (metaboliteAnnotations != null) {

				// first find the metabolite annotations for this feature
				HDPAnnotationItem featureMets = metaboliteAnnotations.get(feature);

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
						System.out.println("\tMETABOLITE " + e2.getKey() + "="
								+ String.format("%.2f", prob));
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
						System.out.println("\tADDUCT " + e2.getKey() + "="
								+ String.format("%.2f", prob));
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
						
		} // end feature loop

		// print some overall statistics

		if (ipAnnotations != null) {
		
			System.out.println("Total IP annotations = " + ipAnnotations.size()
					+ "/" + totalPeaks);
			System.out.println("Total correct IP annotations = "
					+ correctIPCount + "/" + ipAnnotations.size());
			System.out.println("Total nonambiguous correct IP annotations = "
					+ nonAmbiguousIPCount + "/" + correctIPCount);
			System.out.println("Total ambiguous correct IP annotations = "
					+ ambiguousIPCount + "/" + correctIPCount);
			
		}
		
		if (metaboliteAnnotations != null && compoundGroundTruthDatabase != null) {
		
			System.out.println("Metabolite DB size = " + compoundGroundTruthDatabase.size());			

			double ratio = ((double)metaboliteFoundInDB.size()) / metaboliteNotFoundInDB.size();
			System.out.println("Metabolites matching vs. non-matching = " + metaboliteFoundInDB.size()
					+ "/" + metaboliteNotFoundInDB.size() + " ratio = " + ratio);
			System.out.println("Matching found = ");
			for (String met : metaboliteFoundInDB) {
				System.out.println("\t- " + met);
			}
						
		}
				
	}
	
}
