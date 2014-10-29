package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;
import peakml.chemistry.Molecule;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpProbabilityMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpResult;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.HDPClustering;
import com.joewandy.alignmentResearch.objectModel.HDPMassRTClustering;
import com.joewandy.alignmentResearch.objectModel.HDPMetabolite;
import com.joewandy.alignmentResearch.objectModel.HDPPrecursorMass;

/**
 * An alignment method using Hierarchical Dirichlet Process mixture model
 * @author joewandy
 *
 */
public class HdpAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;			// The input files to be aligned
	private int totalPeaks;							// Count of total number of peaks
	private AlignmentMethodParam param;				// Alignment parameters	
	private Map<String, String> database;			// Identification database for evaluation	

	private Map<Integer, Feature> sequenceMap;		// Map of sequence ID to peak feature
	private Map<HdpResult, HdpResult> resultMap;	// Map of the results
	
	/**
	 * Constructs an instance of HDPAlignment object
	 * @param dataList The input files
	 * @param param Alignment parameters
	 */
	public HdpAlignment(List<AlignmentFile> dataList,
			AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		for (AlignmentFile file : dataList) {
			this.totalPeaks += file.getFeaturesCount();
		}
		this.param = param;

		// TODO: this should be a configurable parameter
		String databaseFile = "/home/joewandy/Dropbox/Project/documents/new_measure_experiment/input_data/standard_hdp/std1.csv";
		this.database = loadIdentificationDB(databaseFile);
		
		this.sequenceMap = new HashMap<Integer, Feature>();
		this.resultMap = new HashMap<HdpResult, HdpResult>();

	}

	@Override
	public AlignmentList matchFeatures() {
		
		AlignmentList masterList = new AlignmentList("");
		MatFileReader mfr = getMfr(dataList);
		if (mfr != null) {

			// load from matlab
			resultMap = getMatlabResultMap(dataList, mfr);

		} else {

			// use the java HDP Mass-RT clustering			
			if (MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT_JAVA
					.equals(this.param.getScoringMethod())) {

				// assign a sequential ID to all peaks to store the result later
				assignSequenceID(dataList);

				// run the HDP RT+mass clustering
				HDPClustering clustering = new HDPMassRTClustering(dataList,
						param);
				clustering.runClustering();

				// process the matching results
				int samplesTaken = clustering.getSamplesTaken();
				System.out.println("Samples taken = " + samplesTaken);
				resultMap = getHDPMassRTResultMap(clustering, samplesTaken);

				// process the ionisation product annotations
				annotateIP(clustering);

				// process metabolite annotations
				System.out.println();
				annotateMetabolites(clustering);

			} else if (MultiAlignConstants.SCORING_METHOD_HDP_RT_JAVA
					.equals(this.param.getScoringMethod())) {

				// use the java HDP RT clustering

			}

		}
		
		// construct the actual feature matching here 
		FeatureMatching matcher = new HdpProbabilityMatching(resultMap,
				dataList);
		masterList = matcher.getMatchedList();	
		return masterList;
		
	}
	
	/**
	 * Load identification database for evaluation 
	 * @param databaseFile the standards database file
	 * @return Map of database formula to the line
	 */
	private Map<String, String> loadIdentificationDB(String databaseFile) {

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

		System.out.println("database=" + database);
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
	 * Imports HDP alignment results from matlab prototype
	 * @param dataList The list of files
	 * @param mfr The matlab file reader
	 * @return The resultMap object
	 */
	private Map<HdpResult, HdpResult> getMatlabResultMap(
			List<AlignmentFile> dataList, MatFileReader mfr) {

		Map<HdpResult, HdpResult> resultMap = new HashMap<HdpResult, HdpResult>();

		double[][] result = ((MLDouble) mfr.getMLArray("sorted_res"))
				.getArray();
		for (int i = 0; i < result.length; i++) {

			double[] row = result[i];
			int peakID1 = (int) row[0];
			int fileID1 = (int) row[1] - 1;
			int peakID2 = (int) row[2];
			int fileID2 = (int) row[3] - 1;
			double similarity = row[4];

			// find features
			AlignmentFile file1 = dataList.get(fileID1);
			Feature feature1 = file1.getFeatureByPeakID(peakID1);
			AlignmentFile file2 = dataList.get(fileID2);
			Feature feature2 = file2.getFeatureByPeakID(peakID2);

			HdpResult hdpRes = new HdpResult(feature1, feature2);
			hdpRes.setSimilarity(similarity);

		}
		
		return resultMap;

	}
	
	/**
	 * Assigns a sequential ID numbers to all features across all input files
	 * @param dataList The input files
	 * @return A map from ID to feature
	 */
	private void assignSequenceID(List<AlignmentFile> dataList) {

		int sequenceID = 0;
		for (int j = 0; j < dataList.size(); j++) {

			AlignmentFile alignmentFile = dataList.get(j);
			for (Feature f : alignmentFile.getFeatures()) {
				f.setSequenceID(sequenceID);
				sequenceMap.put(sequenceID, f);
				sequenceID++;
			}

		}
		
	}
	
	/**
	 * Constructs HDP alignment results
	 * @param sequenceMap Map of sequence ID of each peak
	 * @param clustering
	 * @param samplesTaken
	 * @return
	 */
	private Map<HdpResult, HdpResult> getHDPMassRTResultMap(HDPClustering clustering, int samplesTaken) {

		Map<HdpResult, HdpResult> resultMap = new HashMap<HdpResult, HdpResult>();
		Map<Feature, List<Feature>> pairings = new HashMap<Feature, List<Feature>>();
		Matrix simMatrix = clustering.getSimilarityResult();

		Iterator<MatrixEntry> it = simMatrix.iterator();
		while (it.hasNext()) {

			MatrixEntry entry = it.next();
			int m = entry.row();
			int n = entry.column();
			double similarity = entry.get() / samplesTaken;
			Feature feature1 = sequenceMap.get(m);
			Feature feature2 = sequenceMap.get(n);

			// skip alignment of a feature to itself
			if (feature1.equals(feature2)) {
				continue;
			}

			// skip alignment of features in the same file
			if (feature1.getData().getId() == feature2.getData()
					.getId()) {
				continue;
			}

			// track the partner peaks for debugging
			List<Feature> partners = null;
			if (pairings.containsKey(feature1)) {
				partners = pairings.get(feature1);
			} else {
				partners = new ArrayList<Feature>();
				pairings.put(feature1, partners);
			}
			feature2.setScore(similarity);
			partners.add(feature2);

			// HACK: ensure that f1 file id is always smaller than f2
			// file id
			if (feature1.getData().getId() > feature2.getData().getId()) {
				Feature temp = feature1;
				feature1 = feature2;
				feature2 = temp;
			}

			HdpResult hdpRes = new HdpResult(feature1, feature2);
			hdpRes.setSimilarity(similarity);
			resultMap.put(hdpRes, hdpRes);

		}

		// print out all the partners of each peak
		for (Entry<Feature, List<Feature>> entry : pairings.entrySet()) {
			Feature f = entry.getKey();
			List<Feature> partners = entry.getValue();
			System.out.println(f + " has " + partners.size()
					+ " partners = ");
			for (Feature partner : partners) {
				System.out.println("\t" + partner);
			}
		}
		
		return resultMap;
		
	}

	/**
	 * Annotates features by inferred ionisation products
	 * @param clustering The HDP clustering results
	 */
	private void annotateIP(HDPClustering clustering) {
		
		Map<Feature, Map<String, Integer>> ipMap = clustering
				.getIpMap();
		System.out.println("IPMAP SIZE = " + ipMap.size());

		int correctCount = 0;
		int nonAmbiguousCount = 0;
		int ambiguousCount = 0;
		
		for (Entry<Feature, Map<String, Integer>> e : ipMap.entrySet()) {

			Feature f = e.getKey();
			System.out.println(f.getPeakID() + " " + f.getMass() + " "
					+ f.getRt() + " " + f.getIntensity() + " "
					+ f.getTheoAdductType());
			
			Map<String, Integer> vals = e.getValue();
			if (vals != null) {
			
				double sum = 0;
				for (Entry<String, Integer> e2 : vals.entrySet()) {
					sum += e2.getValue();
				}
				
				double maxProb = 0;
				String annot = null;
				for (Entry<String, Integer> e2 : vals.entrySet()) {
					int count = e2.getValue();
					double prob = (count) / sum;
					System.out.println("\t" + e2.getKey() + "="
							+ String.format("%.2f", prob));
					if (prob > maxProb) {
						maxProb = prob;
						annot = e2.getKey();
					}
				}
				
				if (annot != null
						&& annot.equals(f.getTheoAdductType())) {
					System.out.println("\tCORRECT");
					correctCount++;
					if (vals.entrySet().size() == 1) {
						nonAmbiguousCount++;
					} else {
						ambiguousCount++;
					}
				}
				
			}
			
		}
		
		System.out.println("Total peaks annotated = " + ipMap.size()
				+ "/" + totalPeaks);
		System.out.println("Total correct peaks annotated = "
				+ correctCount + "/" + ipMap.size());
		System.out.println("Total nonambiguous correct annotated = "
				+ nonAmbiguousCount + "/" + correctCount);
		System.out.println("Total ambiguous correct annotated = "
				+ ambiguousCount + "/" + correctCount);
		
	}

	/**
	 * Annotates features by putative metabolite identities
	 * @param clustering The HDP clustering results
	 */
	private void annotateMetabolites(HDPClustering clustering) {
		
		Map<HDPMetabolite, List<HDPPrecursorMass>> metabolitePrecursors = clustering
				.getMetabolitePrecursors();
		List<HDPMetabolite> inferredMetabolites = clustering
				.getMetabolitesInLastSample();

		System.out.println("Total metabolites in database = "
				+ database.size());
		System.out.println("Total metabolites inferred = "
				+ inferredMetabolites.size());
		
		Set<String> uniqueFound = new HashSet<String>();
		for (HDPMetabolite met : inferredMetabolites) {

			List<HDPPrecursorMass> precursors = metabolitePrecursors
					.get(met);
			System.out.println("Metabolite " + met + " has "
					+ precursors.size() + " precursor masses ");
			if (precursors.isEmpty()) {
				// if no precursor mass in this metabolite, then within
				// each mass cluster, take the most intense peak and use
				// M+H adduct

			}

			Collections.sort(precursors);
			for (int i = 0; i < precursors.size(); i++) {
				HDPPrecursorMass pc = precursors.get(i);
				Set<Molecule> mols = pc.getMolecules();
				if (mols.isEmpty()) {
					continue;
				}
				System.out.println("\t" + pc);
				checkInDatabase(mols);
			}

		}
		
		System.out.println("Metabolites found = " + uniqueFound.size()
				+ "/" + database.size());

	}

	private Set<String> checkInDatabase(Set<Molecule> mols) {
		Set<String> metaboliteFound = new HashSet<String>(); 
		for (Molecule mol : mols) {
			String key = mol.getPlainFormula();
			if (database.containsKey(key)) {
				if (!metaboliteFound.contains(key)) {
					metaboliteFound.add(key);
					System.out.println("\t\tFOUND " + key + " in database");
				}
			}
		}
		return metaboliteFound;
	}

}
