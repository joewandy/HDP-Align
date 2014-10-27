package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.FeatureMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpProbabilityMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpResult;
import com.joewandy.alignmentResearch.alignmentMethod.custom.hdp.HdpSimpleMatching;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.HDPClustering;
import com.joewandy.alignmentResearch.objectModel.HDPMassRTClustering;
import com.joewandy.alignmentResearch.objectModel.HDPMetabolite;
import com.joewandy.alignmentResearch.objectModel.HDPPrecursorMass;

public class TestHdpAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;
	private Map<HdpResult, HdpResult> resultMap;
	private String scoringMethod;
	private boolean exactMatch;
	private int totalPeaks;
	
	public TestHdpAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		this.scoringMethod = param.getScoringMethod();
		this.exactMatch = param.isExactMatch();

		for (AlignmentFile file : dataList) {
			totalPeaks += file.getFeaturesCount();
		}
		
		// load from matlab
		AlignmentFile firstFile = dataList.get(0);
		String parentPath = firstFile.getParentPath();
		resultMap = new HashMap<HdpResult, HdpResult>();
		MatFileReader mfr = null;
		try {
			if (MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT.equals(this.scoringMethod)) {
				// clustering results by RT + mass
				mfr = new MatFileReader(parentPath + "/csv/hdp_result_rt_mass.mat");				
			} else if (MultiAlignConstants.SCORING_METHOD_HDP_RT.equals(this.scoringMethod)) {
				// clustering results by RT only
				mfr = new MatFileReader(parentPath + "/csv/hdp_result_rt.mat");				
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}

		if (mfr != null) {
			double[][] result = ((MLDouble)mfr.getMLArray("sorted_res")).getArray();		
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
				resultMap.put(hdpRes, hdpRes);
				
			}
		} else {
			
			if (MultiAlignConstants.SCORING_METHOD_HDP_MASS_RT_JAVA.equals(this.scoringMethod)) {

				// assign a sequential ID to all peaks to store the result later
				Map<Integer, Feature> sequenceMap = new HashMap<Integer, Feature>();
				int sequenceID = 0;
				for (int j=0; j < dataList.size(); j++) {
								
					AlignmentFile alignmentFile = dataList.get(j);
					for (Feature f : alignmentFile.getFeatures()) {
						f.setSequenceID(sequenceID);
						sequenceMap.put(sequenceID, f);
						sequenceID++;
					}
					
				}				
				
				// run the HDP RT+mass clustering 
				HDPClustering clustering = new HDPMassRTClustering(dataList, param);
				clustering.run();

				// process the result
				Map<Feature, List<Feature>> pairings = new HashMap<Feature, List<Feature>>();
				int samplesTaken = clustering.getSamplesTaken();		
				System.out.println("Samples taken = " + samplesTaken);
				Matrix simMatrix = clustering.getSimilarityResult();
				Iterator<MatrixEntry> it = simMatrix.iterator();
				while (it.hasNext()) {
					
					MatrixEntry entry = it.next();
					int m = entry.row();
					int n = entry.column();
					double similarity = entry.get()/samplesTaken;
					Feature feature1 = sequenceMap.get(m);
					Feature feature2 = sequenceMap.get(n);
					
					// skip alignment of a feature to itself					
					if (feature1.equals(feature2)) {
						continue; 
					}
					
					// skip alignment of features in the same file
					if (feature1.getData().getId() == feature2.getData().getId()) {
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
					
					// HACK: ensure that f1 file id is always smaller than f2 file id
					if (feature1.getData().getId() > feature2.getData().getId()) {
						Feature temp = feature1;
						feature1 = feature2;
						feature2 = temp;
					}
					
					HdpResult hdpRes = new HdpResult(feature1, feature2);
					hdpRes.setSimilarity(similarity);
					resultMap.put(hdpRes, hdpRes);
					
				}
				
				for (Entry<Feature, List<Feature>> entry : pairings.entrySet()) {
					Feature f = entry.getKey();
					List<Feature> partners = entry.getValue();
					System.out.println(f + " has " + 
							partners.size() + " partners = ");
					for (Feature partner : partners) {
						System.out.println("\t" + partner);
					}
				}
				
				// print IP map
				Map<Feature, Map<String, Integer>> ipMap = clustering.getIpMap();
				System.out.println("IPMAP SIZE = " + ipMap.size());
				int correctCount = 0;
				int nonAmbiguousCount = 0;
				int ambiguousCount = 0;
				for (Entry<Feature, Map<String, Integer>> e : ipMap.entrySet()) {
					Feature f = e.getKey();
					System.out.println(f.getPeakID() + " " + f.getMass() + " " + f.getRt() + " " + f.getIntensity() + " " + f.getTheoAdductType());
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
							double prob = ((double) count) / sum;
							System.out.println("\t" + e2.getKey() + "=" + String.format("%.2f", prob));
							if (prob > maxProb) {
								maxProb = prob;
								annot = e2.getKey();
							}
						}						
						if (annot != null && annot.equals(f.getTheoAdductType())) {
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
				System.out.println("Total peaks annotated = " + ipMap.size() + "/" + totalPeaks);
				System.out.println("Total correct peaks annotated = " + correctCount + "/" + ipMap.size());
				System.out.println("Total nonambiguous correct annotated = " + nonAmbiguousCount + "/" + correctCount);
				System.out.println("Total ambiguous correct annotated = " + ambiguousCount + "/" + correctCount);
				//				MapUtils.debugPrint(System.out, "IP Map", ipMap);
				
				// print metabolite precursor
				System.out.println();
				Map<HDPMetabolite, List<HDPPrecursorMass>> metabolitePrecursors = clustering.getMetabolitePrecursors();
				for (Entry<HDPMetabolite, List<HDPPrecursorMass>> e : metabolitePrecursors.entrySet()) {
					HDPMetabolite met = e.getKey();
					List<HDPPrecursorMass> common = e.getValue();
					System.out.println("Metabolite " + met + " has " + common.size() + " precursor masses ");
					Collections.sort(common);
					for (HDPPrecursorMass pc : common) {
						System.out.println("\t" + pc);
					}					
				}
								
			} else if (MultiAlignConstants.SCORING_METHOD_HDP_RT_JAVA.equals(this.scoringMethod)) {
			
				// use the java HDP RT clustering
				
			}			
			
		}
				
	}
	
	public AlignmentList matchFeatures() {
//		AlignmentList masterList = greedyMatch();
		AlignmentList masterList = probMatch();
		return masterList;
	}

	private AlignmentList greedyMatch() {
		AlignmentList masterList = new AlignmentList("");	
		int counter = 0;
		for (AlignmentFile data : dataList) {			
			AlignmentList peakList = new AlignmentList(data);
			System.out.println("Aligning #" + (counter+1) + ": " + peakList);
			FeatureMatching matcher = new HdpSimpleMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
					massTolerance, rtTolerance, resultMap, scoringMethod, exactMatch);
			masterList = matcher.getMatchedList();			            
			counter++;
		}
		return masterList;
	}

	private AlignmentList probMatch() {
		AlignmentList masterList = new AlignmentList("");	
		FeatureMatching matcher = new HdpProbabilityMatching(resultMap, dataList);
		masterList = matcher.getMatchedList();			            
		return masterList;
	}	
	
}
