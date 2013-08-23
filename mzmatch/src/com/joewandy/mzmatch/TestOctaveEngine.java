package com.joewandy.mzmatch;

import java.io.IOException;
import java.util.Arrays;

import mzmatch.ipeak.CombineTask;
import dk.ange.octave.OctaveEngine;
import dk.ange.octave.OctaveEngineFactory;
import dk.ange.octave.type.Octave;
import dk.ange.octave.type.OctaveDouble;
import dk.ange.octave.type.matrix.DoubleMatrix;

public class TestOctaveEngine {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {

		OctaveEngine octave = new OctaveEngineFactory().getScriptEngine();
		
		int numAlignmentClusters = 50;
		octave.put("K", Octave.scalar(numAlignmentClusters));
		
		octave.eval("cd " + CombineTask.MATLAB_OUTPUT_PATH);
		octave.eval("load " + CombineTask.MATLAB_OUTPUT_FILENAME);
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

}
