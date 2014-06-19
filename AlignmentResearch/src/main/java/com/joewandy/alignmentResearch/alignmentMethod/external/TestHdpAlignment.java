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
import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpResult;
import com.joewandy.alignmentResearch.alignmentMethod.custom.HdpSimpleMatching;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class TestHdpAlignment extends BaseAlignment implements AlignmentMethod {

	protected List<AlignmentFile> dataList;
	private Map<HdpResult, HdpResult> resultMap;
	
	public TestHdpAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		this.dataList = dataList;

		// load from matlab
		AlignmentFile firstFile = dataList.get(0);
		String parentPath = firstFile.getParentPath();
		resultMap = new HashMap<>();
		MatFileReader mfr = null;
		try {
			mfr = new MatFileReader(parentPath + "/csv/hdp_result_rt_mass.mat");
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
		}
				
	}
	
	public AlignmentList matchFeatures() {
		
		AlignmentList masterList = new AlignmentList("");	
		int counter = 0;
		for (AlignmentFile data : dataList) {			
			AlignmentList peakList = new AlignmentList(data);
			System.out.println("Aligning #" + (counter+1) + ": " + peakList);
			FeatureMatching matcher = new HdpSimpleMatching(masterList.getId() + ", " + peakList.getId(), masterList, peakList, 
					massTolerance, rtTolerance, resultMap);
			masterList = matcher.getMatchedList();			            
			counter++;
		}
		
		return masterList;
		
	}
				
}
