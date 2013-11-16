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

import org.jblas.DoubleMatrix;

import cern.colt.matrix.tdouble.DoubleMatrix2D;
import cern.colt.matrix.tdouble.impl.SparseDoubleMatrix2D;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlign;

import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;

public class MatlabFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	public static final String MATLAB_SCRIPT_PATH = "/home/joewandy/Dropbox/workspace/AlignmentModel";
	
	private MatlabProxy proxy;
	private double rtWindow;
	private double alpha;
	private int nSamples;
	
	public MatlabFeatureGroupingMethod(double rtTolerance, double alpha, int nSamples) {
		
		this.rtWindow = rtTolerance;
		this.alpha = alpha;
		this.nSamples = nSamples;
				
		//Create a proxy, which we will use to control MATLAB
		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
			.setUsePreviouslyControlledSession(true)
			.setHidden(true)
			.setMatlabLocation(null).build(); 
	    MatlabProxyFactory factory = new MatlabProxyFactory(options);
		try {
			this.proxy = factory.getProxy();
		} catch (MatlabConnectionException e1) {
			e1.printStackTrace();
		}
		
	}
	
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {

		System.out.println("============ Grouping = " + dataList.size() + " files ============");
				
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();

		for (AlignmentFile data : dataList) {
			List<FeatureGroup> fileGroups = group(data);
			groups.addAll(fileGroups);			
		}
		
		return groups;
				
	}
	
	@Override
	public List<FeatureGroup> group(AlignmentFile data) {
				
		if (proxy == null) {
			System.out.println("Cannot find Matlab !! Proxy is null");
			System.exit(1);
		}
		
		int groupId = 1;
		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();

		System.out.print("Grouping " + data.getFilename() + " ");
	    try {
			double[] dataPoints = getDataPoints(data);
			String dataStr = asString(dataPoints);
			proxy.eval(dataStr);
			proxy.eval("rtWindow = " + rtWindow + ";");
			proxy.eval("alpha = " + alpha + ";");
			proxy.eval("nSamples = " + nSamples + ";");
			proxy.eval("cd " + MatlabFeatureGroupingMethod.MATLAB_SCRIPT_PATH);
			proxy.eval("gmm_dp_sampler(data', rtWindow, alpha, nSamples);");
			proxy.eval("clear all; clc; close all;");
			
		} catch (MatlabInvocationException e) {
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
				fileGroups.add(group);
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
//			 System.out.println(feature.getPeakID() + "\t" + feature.getGroup().getGroupId());
			if (feature.isGrouped()) {
				groupedCount++;
			} else {
				ungroupedCount++;
			}
		}			
		System.out.println("groupedCount = " + groupedCount);
		System.out.println("ungroupedCount = " + ungroupedCount);

		System.out.print("Getting cluster co-ocurrence probabilities of peaks for " + data.getFilename() + " ");
		mfr = null;
		try {
			mfr = new MatFileReader(MATLAB_SCRIPT_PATH + "/temp.ZZprob.mat");
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		if (mfr != null) {
			DoubleMatrix ZZprob = new DoubleMatrix(((MLDouble)mfr.getMLArray("ZZprob")).getArray());
//			System.out.println("ZZprob = " + ZZprob.length + "x" + ZZprob[0].length);
			data.setZZProb(ZZprob);				
		}	
		
		return fileGroups;
		
	}
	
	@Override
	public void close() {
		if (proxy != null) {
		    //Disconnect the proxy from MATLAB
		    proxy.disconnect();					
		}		
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
	
}
