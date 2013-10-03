package com.joewandy.alignmentResearch.objectModel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.matrix.DoubleMatrix;

public class MatlabFeatureGrouping extends BaseFeatureGrouping implements FeatureGrouping {

	public static final String MATLAB_SCRIPT_PATH = "/home/joewandy/Dropbox/workspace/AlignmentModel";
	
	private double rtWindow;
	private double alpha;
	private int nSamples;
	
	public MatlabFeatureGrouping(double rtTolerance, double alpha, int nSamples) {
		
		this.rtWindow = rtTolerance;
		this.alpha = alpha;
		this.nSamples = nSamples;
				
	}
	
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {

		System.out.println("============ Grouping = " + dataList.size() + " files ============");
				
		// the group ids must be unique across all input files 
		int groupId = 1;
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		//Create a proxy, which we will use to control MATLAB
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
			.setUsePreviouslyControlledSession(true)
			.setHidden(true)
			.setMatlabLocation(null).build(); 
	    MatlabProxyFactory factory = new MatlabProxyFactory(options);
	    MatlabProxy proxy = null;
		try {
			proxy = factory.getProxy();
		} catch (MatlabConnectionException e1) {
			e1.printStackTrace();
		}

		for (AlignmentFile data : dataList) {

			if (proxy == null) {
				System.out.println("Cannot group !!");
				System.exit(1);
			}
			
			System.out.print("Grouping " + data.getFilename() + " ");
		    try {
				double[] dataPoints = getDataPoints(data);
				String dataStr = asString(dataPoints);
				proxy.eval(dataStr);
				proxy.eval("rtWindow = " + rtWindow + ";");
				proxy.eval("alpha = " + alpha + ";");
				proxy.eval("nSamples = " + nSamples + ";");
				proxy.eval("cd " + MatlabFeatureGrouping.MATLAB_SCRIPT_PATH);
				proxy.eval("gmm_dp_sampler(data', rtWindow, alpha, nSamples);");
				proxy.eval("clear all; clc; close all;");
				
			} catch (MatlabInvocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 

			// load from matlab
			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(MATLAB_SCRIPT_PATH + "/temp.Z.mat");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			if (mfr != null) {

				double[][] dz = ((MLDouble)mfr.getMLArray("Z")).getArray();
				int m = dz.length;
				int n = dz[0].length;
				int[][] Z = new int[m][n];
				for (int i = 0; i < m; i++) {
					for (int j = 0; j < n; j++) {
						Z[i][j] = (int) dz[i][j];
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
				System.out.println(groupMap.size() + " groups created");
				
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
			System.out.println("groupedCount = " + groupedCount);
			System.out.println("ungroupedCount = " + ungroupedCount);

			if (FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
				System.out.print("Getting cluster co-ocurrence probabilities of peaks for " + data.getFilename() + " ");
				mfr = null;
				try {
					mfr = new MatFileReader(MATLAB_SCRIPT_PATH + "/temp.ZZprob.mat");
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				if (mfr != null) {

					final double[][] ZZprob = ((MLDouble)mfr.getMLArray("ZZprob")).getArray();
					System.out.println("ZZprob = " + ZZprob.length + "x" + ZZprob[0].length);
					data.setZZProb(ZZprob);
					
				}	
			}
			
		}
		
		if (proxy != null) {
		    //Disconnect the proxy from MATLAB
		    proxy.disconnect();					
		}
		
		return groups;
				
	}
	
	public List<FeatureGroup> group2(List<AlignmentFile> dataList) {

		System.out.println("============ Grouping = " + dataList.size() + " files ============");
				
		// the group ids must be unique across all input files 
		int groupId = 1;
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		for (AlignmentFile data : dataList) {

			System.out.print("Grouping " + data.getFilename() + " ");

			OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

			octave.put("rtWindow", Octave.scalar(rtWindow));
			octave.put("alpha", Octave.scalar(alpha));
			octave.put("nSamples", Octave.scalar(nSamples));

			double[] dataPoints = getDataPoints(data);
			OctaveDouble octaveData = new OctaveDouble(dataPoints, dataPoints.length, 1);
			octave.put("data", octaveData);
			
			octave.eval("cd " + MatlabFeatureGrouping.MATLAB_SCRIPT_PATH);
			octave.eval("gmm_dp_sampler(data, rtWindow, alpha, nSamples);");
			System.out.println("Press Enter to continue ...");
			try {
				System.in.read();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			octave.close();

			// load from matlab
			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(MATLAB_SCRIPT_PATH + data.getFilenameWithoutExtension() + ".csv.Z.mat");
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(1);
			}

			if (mfr != null) {

				double[][] dz = ((MLDouble)mfr.getMLArray("Z")).getArray();
				int m = dz.length;
				int n = dz[0].length;
				int[][] Z = new int[m][n];
				for (int i = 0; i < m; i++) {
					for (int j = 0; j < n; j++) {
						Z[i][j] = (int) dz[i][j];
					}
				}
				Map<Integer, FeatureGroup> groupMap = new HashMap<Integer, FeatureGroup>();
				for (int k = 0; k < n; k++) {
					FeatureGroup group = new FeatureGroup(groupId);
					groupId++;
					groups.add(group);
					groupMap.put(k, group);
				}
				System.out.println(groupMap.size() + " groups created");
				for (int i = 0; i < m; i++) {
					Feature feature = data.getFeatureByIndex(i);
					int k = findClusterIndex(Z[i]);
					FeatureGroup group = groupMap.get(k);
					group.addFeature(feature);
					if (i % 1000 == 0) {
						System.out.print(".");
					}
				}
				
			}			
			System.out.println();
			
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
			System.out.println("groupedCount = " + groupedCount);
			System.out.println("ungroupedCount = " + ungroupedCount);

			if (FeatureXMLAlignment.WEIGHT_USE_ALL_PEAKS) {
				System.out.print("Getting cluster co-ocurrence probabilities of peaks for " + data.getFilename() + " ");
				mfr = null;
				try {
					mfr = new MatFileReader(MATLAB_SCRIPT_PATH + data.getFilenameWithoutExtension() + ".csv.ZZprob.mat");
				} catch (IOException e) {
					e.printStackTrace();
					System.exit(1);
				}
				
				if (mfr != null) {

					final double[][] ZZprob = ((MLDouble)mfr.getMLArray("ZZprob")).getArray();
					System.out.println("ZZprob = " + ZZprob.length + "x" + ZZprob[0].length);
					data.setZZProb(ZZprob);
					
				}				
			}
			
		}
		
		return groups;
				
	}

	private double[] getDataPoints(AlignmentFile data) {
		double[] retentionTimes = new double[data.getFeaturesCount()];
		for (int i = 0; i < data.getFeaturesCount(); i++) {
			Feature feature = data.getFeatureByIndex(i);
			double rt = feature.getRt();
			retentionTimes[i] = rt;
		}
		return retentionTimes;
	}
	
	private String asString(double[] dataPoints) {
		String output = "data = [";
		for (double rt : dataPoints) {
			output += rt + ", ";
		}
		output += "];";
		return output;
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
	
	private static double[][] convertMatrix(DoubleMatrix matlabMatrix) {

		int[] sizes = matlabMatrix.getSize();
		int n = sizes[0];
		int k = sizes[1];

//		System.out.println();
//		System.out.println("=======================");
//		System.out.println("Clustering result");
//		System.out.println("=======================");
//		System.out.println("sizes = " + Arrays.toString(sizes));

		// reshape 1d array of size 1 by (n*k) into a 2d array of size n by k
		double[] data = matlabMatrix.getData();
		double[][] javaMatrix = new double[n][k];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < k; j++) {
				int idx = k*i + j;
				javaMatrix[i][j] = data[idx];
			}
		}
//		System.out.println("data = " + Arrays.toString(data));
		return javaMatrix;

	}
	
}
