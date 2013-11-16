package com.joewandy.alignmentResearch.objectModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlign;

public class SavedMatlabFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	private String groupingMethod;
	
	public SavedMatlabFeatureGroupingMethod(String groupingMethod) {
		this.groupingMethod = groupingMethod;
	}
	
	@Override
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {
		System.out.println("============ Grouping = " + dataList.size() + " files ============");
		// the group ids must be unique across all input files 
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		for (AlignmentFile data : dataList) {
			List<FeatureGroup> fileGroups = group(data);
			groups.addAll(fileGroups);			
		}
		return groups;		
	}

	@Override
	public List<FeatureGroup> group(AlignmentFile data) {

		System.out.println("Grouping " + data.getFilename() + " ");
		final String dataPath = data.getParentPath() + "/mat/";
		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		
		int groupId = 1;
		if (MultiAlign.GROUPING_METHOD_MIXTURE_RT.equals(groupingMethod)) {
		
			String filename = data.getFilenameWithoutExtension() + ".csv.Z.mat";
			System.out.println("Loading " + dataPath + filename);
			
			// load from matlab
			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(dataPath + filename);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			if (mfr != null) {

				// load the best single clustering Z
				System.out.println("Loading Z");
				Matrix Z = new DenseMatrix(((MLDouble)mfr.getMLArray("Z")).getArray());		
				
				// get probabilities of peak vs peak to be together ZZprob
				System.out.println("Computing ZZprob");
				
				DenseMatrix ZZprob = new DenseMatrix(data.getFeaturesCount(), data.getFeaturesCount());
				Z.transBmult(Z, ZZprob);		
				data.setZZProb(ZZprob);
				
				// map this clustering results into FeatureGroups
				int m = Z.numRows();
				int n = Z.numColumns();
				int[][] zInt = new int[m][n];
				for (int i = 0; i < m; i++) {
					for (int j = 0; j < n; j++) {
						zInt[i][j] = (int) Z.get(i, j);
					}
				}
				Map<Integer, FeatureGroup> groupMap = new HashMap<Integer, FeatureGroup>();
				for (int k = 0; k < n; k++) {
					FeatureGroup group = new FeatureGroup(groupId);
					groupId++;
					fileGroups.add(group);
					groupMap.put(k, group);
				}
				for (int i = 0; i < m; i++) {
					Feature feature = data.getFeatureByIndex(i);
					int k = findClusterIndex(zInt[i]);
					FeatureGroup group = groupMap.get(k);
					group.addFeature(feature);
				}
				
			}
			
		} else if (MultiAlign.GROUPING_METHOD_POSTERIOR_RT.equals(groupingMethod)) {

			String filename = data.getFilenameWithoutExtension() + ".csv.ZZprob.mat";
			System.out.println("Loading " + dataPath + filename);				
			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(dataPath + filename);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			if (mfr != null) {
				Matrix ZZprob = new DenseMatrix(((MLDouble)mfr.getMLArray("ZZprob")).getArray());		
				data.setZZProb(new DenseMatrix(ZZprob));		
			}
			
		}
					
		return fileGroups;
				
	}
	
	private int findClusterIndex(int[] is) {
		for (int i = 0; i < is.length; i++) {
			if (is[i] == 1) {
				return i;
			}
		}
		// never happens
		return -1;
	}
	
}
