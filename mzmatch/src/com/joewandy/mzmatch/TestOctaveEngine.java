package com.joewandy.mzmatch;

import java.io.IOException;
import java.util.Arrays;

import mzmatch.ipeak.combineMethod.CombineMethod;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.matrix.DoubleMatrix;

public class TestOctaveEngine {

	public static final String MATLAB_OUTPUT_FILENAME = "test-3-mini.mat";
	public static final String MATLAB_SCRIPT_PATH = "/home/joewandy/Dropbox/workspace/AlignmentModel";
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		test2();
		
	}

	private static void test1() throws IOException {
		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
		
		int numAlignmentClusters = 50;
		octave.put("K", Octave.scalar(numAlignmentClusters));
		
		octave.eval("cd " + TestOctaveEngine.MATLAB_SCRIPT_PATH);
		octave.eval("load " + TestOctaveEngine.MATLAB_OUTPUT_FILENAME);
		octave.eval("pkg load general;");
		octave.eval("q = par_em(X, K);");
		DoubleMatrix octaveMat = octave.get(OctaveDouble.class, "q");

		System.out.println("Press Enter to continue ...");
		System.in.read();
		octave.close();

		int[] sizes = octaveMat.getSize();
		int n = sizes[0];
		int k = sizes[1];

		System.out.println();
		System.out.println("=======================");
		System.out.println("EM result");
		System.out.println("=======================");
		System.out.println("sizes = " + Arrays.toString(sizes));

		// reshape 1d array of size 1 by (n*k) into a 2d array of size n by k
		double[] data = octaveMat.getData();
		double[][] qs = new double[n][k];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < k; j++) {
				int idx = k*i + j;
				qs[i][j] = data[idx];
			}
		}

		System.out.println("data = " + Arrays.toString(data));
		for (double[] q : qs) {
			System.out.println("q = " + Arrays.toString(q));			
		}
	}

	private static void test2() throws IOException {
		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();

		double rtWindow = 50;
		double alpha = 1;
		int nSamples = 10;
		
		octave.put("rtWindow", Octave.scalar(rtWindow));
		octave.put("alpha", Octave.scalar(alpha));
		octave.put("nSamples", Octave.scalar(nSamples));

		double[] dataPoints = { 1, 2, 3, 4, 5 , 6, 7, 8, 9, 10 };
		OctaveDouble octaveData = new OctaveDouble(dataPoints, dataPoints.length, 1);
		octave.put("data", octaveData);
		
		octave.eval("cd " + TestOctaveEngine.MATLAB_SCRIPT_PATH);
		octave.eval("[Z, ZZall, ZZprob] = gmm_dp_sampler(data, rtWindow, alpha, nSamples);");
		DoubleMatrix zMat = octave.get(OctaveDouble.class, "Z");
		DoubleMatrix zzProbMat = octave.get(OctaveDouble.class, "ZZprob");

		System.out.println("Press Enter to continue ...");
		System.in.read();
		octave.close();

		double[][] zs = convertMatrix(zMat);
		for (double[] z : zs) {
			System.out.println("Z = " + Arrays.toString(z));			
		}
		
		double[][] zzProbs = convertMatrix(zzProbMat);
		for (double[] zzProb : zzProbs) {
			System.out.println("ZZprob = " + Arrays.toString(zzProb));			
		}

	}

	private static double[][] convertMatrix(DoubleMatrix matlabMatrix) {

		int[] sizes = matlabMatrix.getSize();
		int n = sizes[0];
		int k = sizes[1];

		System.out.println();
		System.out.println("=======================");
		System.out.println("Clustering result");
		System.out.println("=======================");
		System.out.println("sizes = " + Arrays.toString(sizes));

		// reshape 1d array of size 1 by (n*k) into a 2d array of size n by k
		double[] data = matlabMatrix.getData();
		double[][] javaMatrix = new double[n][k];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < k; j++) {
				int idx = k*i + j;
				javaMatrix[i][j] = data[idx];
			}
		}
		System.out.println("data = " + Arrays.toString(data));
		return javaMatrix;

	}
	
}
