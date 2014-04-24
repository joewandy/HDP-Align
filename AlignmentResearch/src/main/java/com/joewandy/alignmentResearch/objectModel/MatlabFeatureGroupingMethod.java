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
import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;

public class MatlabFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	public static final String MATLAB_SCRIPT_PATH = "/home/joewandy/Dropbox/workspace/AlignmentModel";
	
	private MatlabProxy proxy;
	private String groupingMethod;
	private double rtWindow;
	private double alpha;
	private int nSamples;
	private int burnIn;
	
	public MatlabFeatureGroupingMethod(String groupingMethod, double rtTolerance, double alpha, int nSamples, int burnIn) {
		
		this.groupingMethod = groupingMethod;
		this.rtWindow = rtTolerance;
		this.alpha = alpha;
		this.nSamples = nSamples;
		this.burnIn = burnIn;
				
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

		System.out.println("Grouping " + data.getFilename() + " rtwindow " + rtWindow);
		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		
		if (proxy == null) {
			System.out.println("Cannot find Matlab !! Proxy is null");
			System.exit(1);
		}

		// do clustering with matlab
		try {
			double[] dataPoints = getDataPoints(data);
			String dataStr = asString(dataPoints);
			proxy.eval(dataStr);
			proxy.eval("rtWindow = " + rtWindow + ";");
			proxy.eval("alpha = " + alpha + ";");
			proxy.eval("nSamples = " + nSamples + ";");
			proxy.eval("burnIn = " + burnIn + ";");
			proxy.eval("cd " + MatlabFeatureGroupingMethod.MATLAB_SCRIPT_PATH);
			proxy.eval("gmm_dp_sampler(data', rtWindow, alpha, nSamples, burnIn);");
			proxy.eval("clear all; clc; close all;");
			
		} catch (MatlabInvocationException e) {
			e.printStackTrace();
		} 

		int groupId = 1;
		if (MultiAlignConstants.GROUPING_METHOD_MIXTURE.equals(groupingMethod)) {
						
			// load from matlab
			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(MATLAB_SCRIPT_PATH + "/temp.Z.mat");
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
			
		} else if (MultiAlignConstants.GROUPING_METHOD_POSTERIOR.equals(groupingMethod)) {

			MatFileReader mfr = null;
			try {
				mfr = new MatFileReader(MATLAB_SCRIPT_PATH + "/temp.ZZprob.mat");
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
