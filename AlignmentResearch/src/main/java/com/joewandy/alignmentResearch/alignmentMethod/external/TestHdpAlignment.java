package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.FeatureMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpProbabilityMatching;
import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpResult;
import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpSimpleMatching;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.HDPClustering;
import com.joewandy.alignmentResearch.objectModel.HDPMassRTClustering;

public class TestHdpAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;
	private Map<HdpResult, HdpResult> resultMap;
	private String scoringMethod;
	private boolean exactMatch;
	
	public TestHdpAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;
		this.scoringMethod = param.getScoringMethod();
		this.exactMatch = param.isExactMatch();

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

				// use the java HDP RT+mass clustering 
				HDPClustering clustering = new HDPMassRTClustering(dataList, param);
				clustering.run();
				resultMap = clustering.getSimilarityResult();
				
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
