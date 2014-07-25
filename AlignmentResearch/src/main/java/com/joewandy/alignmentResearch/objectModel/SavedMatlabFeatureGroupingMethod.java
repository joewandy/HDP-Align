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
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.matrix.LinkedSparseMatrix;

public class SavedMatlabFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	private String groupingMethod;
	private boolean usePeakShape;
	
	public SavedMatlabFeatureGroupingMethod(String groupingMethod, boolean usePeakShape) {
		this.groupingMethod = groupingMethod;
		this.usePeakShape = usePeakShape;
	}
	
	@Override
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {
		System.out.println("============ Grouping = " + dataList.size() + " files ============");
		// the group ids must be unique across all input files 
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		int counter = 0;
		for (AlignmentFile data : dataList) {
			List<FeatureGroup> fileGroups = group(data);
			groups.addAll(fileGroups);			
			counter++;
		}
		return groups;		
	}

	@Override
	public List<FeatureGroup> group(AlignmentFile data) {

		System.out.println("Grouping " + data.getFilename() + " ");
		final String dataPath = "/home/joewandy/mat/";
		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		
		int groupId = 1;
		if (MultiAlignConstants.GROUPING_METHOD_MIXTURE.equals(groupingMethod)) {
		
			String filename = data.getFilenameWithoutExtension() + ".csv.Z.mat";
			if (usePeakShape) {
				filename = data.getFilenameWithoutExtension() + ".csv.corr.Z.mat";
			}
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

				if (data.getFeaturesCount() < 10000) {
					ZZprob = new DenseMatrix(data.getFeaturesCount(), data.getFeaturesCount());
				} else {
					ZZprob = new LinkedSparseMatrix(data.getFeaturesCount(), data.getFeaturesCount());				
				}
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
			
		} else if (MultiAlignConstants.GROUPING_METHOD_POSTERIOR.equals(groupingMethod)) {

			String filename = data.getFilenameWithoutExtension() + ".dense.csv.mat";
			if (usePeakShape) {
				filename = data.getFilenameWithoutExtension() + ".dense.csv.corr.mat";
			}
			System.out.println("Loading " + dataPath + filename);				
			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(dataPath + filename);
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}
			
			if (mfr != null) {
				Matrix ZZprob = new DenseMatrix(((MLDouble)mfr.getMLArray("ZZ_prob")).getArray());		
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
