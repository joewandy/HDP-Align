package com.joewandy.alignmentResearch.objectModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jblas.DoubleMatrix;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlign;

public class SavedMatlabFeatureGrouping extends BaseFeatureGrouping implements FeatureGrouping {

	public SavedMatlabFeatureGrouping() { }
	
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {

		System.out.println("============ Grouping = " + dataList.size() + " files ============");
		
		// the group ids must be unique across all input files 
		int groupId = 1;
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		for (AlignmentFile data : dataList) {

			System.out.println("Grouping " + data.getFilename() + " ");

//			final String dataPath = "/home/joewandy/workspace/AlignmentModel/mat/";
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

				DoubleMatrix dz = new DoubleMatrix(((MLDouble)mfr.getMLArray("Z")).getArray());
				data.setZ(dz);

				int m = dz.rows;
				int n = dz.columns;
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
					groups.add(group);
					groupMap.put(k, group);
				}
				for (int i = 0; i < m; i++) {
					Feature feature = data.getFeatureByIndex(i);
					int k = findClusterIndex(Z[i]);
					FeatureGroup group = groupMap.get(k);
					group.addFeature(feature);
				}
//				System.out.println(groupMap.size() + " groups created");
				
			}			
			
			int groupedCount = 0;
			int ungroupedCount = 0;
			for (Feature feature : data.getFeatures()) {
//				 System.out.println(feature.getPeakID() + "\t" + feature.getGroup().getGroupId());
				if (feature.isGrouped()) {
					groupedCount++;
				} else {
					ungroupedCount++;
				}
			}			
//			System.out.println("groupedCount = " + groupedCount);
//			System.out.println("ungroupedCount = " + ungroupedCount);

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

				DoubleMatrix ZZprob = new DoubleMatrix(((MLDouble)mfr.getMLArray("ZZprob")).getArray());
				data.setZZProb(ZZprob);
//					System.out.println("ZZprob = " + ZZprob.rows + "x" + ZZprob.columns);
//					System.out.println("ZZprob(4693, 4693) = " + ZZprob.get(4693, 4693));
				
			}				
			
		}
		
		return groups;
				
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
