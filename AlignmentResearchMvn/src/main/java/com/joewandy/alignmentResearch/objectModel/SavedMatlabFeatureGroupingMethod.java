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

public class SavedMatlabFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	public SavedMatlabFeatureGroupingMethod() { }
	
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

		int groupId = 1;
		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();

		System.out.println("Grouping " + data.getFilename() + " ");

		String filename = data.getFilenameWithoutExtension() + ".csv.Z.mat";
		final String dataPath = data.getParentPath() + "/mat/";
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

//			DoubleMatrix dz = new DoubleMatrix(((MLDouble)mfr.getMLArray("Z")).getArray());
//			data.setZ(dz);
//			int m = dz.rows;
//			int n = dz.columns;

			Matrix dz = new DenseMatrix(((MLDouble)mfr.getMLArray("Z")).getArray());		
			data.setZ(new DenseMatrix(dz));
			int m = dz.numRows();
			int n = dz.numColumns();
			
			int[][] Z = new int[m][n];
			for (int i = 0; i < m; i++) {
				for (int j = 0; j < n; j++) {
					Z[i][j] = (int) dz.get(i, j);
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
				int k = findClusterIndex(Z[i]);
				FeatureGroup group = groupMap.get(k);
				group.addFeature(feature);
			}
			
		}			
		
		filename = data.getFilenameWithoutExtension() + ".csv.ZZprob.mat";
		System.out.println("Loading " + dataPath + filename);				
		mfr = null;
		try {
			mfr = new MatFileReader(dataPath + filename);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (mfr != null) {
//			DoubleMatrix ZZprob = new DoubleMatrix(((MLDouble)mfr.getMLArray("ZZprob")).getArray());
//			data.setZZProb(ZZprob);
			Matrix ZZprob = new DenseMatrix(((MLDouble)mfr.getMLArray("ZZprob")).getArray());		
			data.setZZProb(new DenseMatrix(ZZprob));		
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
