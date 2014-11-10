package mzmatch.ipeak.sort;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.jmatio.io.MatFileWriter;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;


public class PeakClusteringSamplerHandler implements SampleHandler<Data, SimpleClustering> {

	private int numSamples;
	private int numPeaks;
	
	// last sample
	private Matrix lastZ;

	// peaks vs peaks probability to be in the same cluster
	private Matrix ZZall;
		
	public PeakClusteringSamplerHandler(int numPeaks) {
		System.err.println("-- PeakClusteringSamplerHandler --");
		this.numPeaks = numPeaks;
		this.ZZall = new DenseMatrix(numPeaks, numPeaks); 
	}
	
	public void handleSample(SimpleClustering clustering) {

		numSamples++;
		
		int numPeaks = clustering.numberOfPeaks();
		int numClusters = clustering.numberOfClusters();
		assert(numPeaks == this.numPeaks);

		System.err.println("\thandleSample iter=" + numSamples + " numClusters=" + numClusters);		
		
		lastZ = new DenseMatrix(numPeaks, numClusters);
		for (int k = 0; k < numClusters; k++) {
			List<Integer> clusterPeaks = clustering.getClusterPeaks(k);
			for (int n : clusterPeaks) {
				lastZ.set(n, k, 1);
			}
		}

		/*
		 * http://www.inf.uni-konstanz.de/algo/lehre/ws05/pp/mtj/mt/AbstractMatrix.html#transBmultAdd(double, mt.Matrix, double, mt.Matrix)
		 * A = lastZ, B = lastZ, C = ZZall
		 * C = A*BT + C
		 */
		ZZall = lastZ.transBmultAdd(lastZ, ZZall);
		
	}
	
	public int getNumSamples() {
		return numSamples;
	}

	public Matrix getLastZ() {
		return lastZ;
	}
	
	public Matrix getLastZZ() {
		Matrix ZZ = new DenseMatrix(numPeaks, numPeaks);
		ZZ = lastZ.transBmultAdd(lastZ, ZZ);
		return ZZ;
	}

	public Matrix getZZall() {
		return ZZall;
	}
	
	public Matrix getZZprob() {
		Matrix ZZprob = this.getZZall().copy();
		final double numSamples = this.getNumSamples();
		ZZprob.scale(1/numSamples);
		return ZZprob;
	}
	
	public void saveResult(String matOut) {

		// extract peak vs cluster membership 
		Matrix Z = this.getLastZ();
		
		// extract peak vs. peak probabilities
		Matrix ZZprob = this.getZZprob();
		
		// quick hack: save the resulting output to matlab files
		System.err.println("Saving clustering output");
		MLDouble ZMat = new MLDouble("Z", toArray(Z));
		MLDouble ZZProbMat = new MLDouble("ZZprob", toArray(ZZprob));
		final Collection<MLArray> output1 = new ArrayList<MLArray>();
		final Collection<MLArray> output2 = new ArrayList<MLArray>();
		output1.add(ZMat);
		output2.add(ZZProbMat);
		final String matFile1 = matOut + ".Z.mat";
		final String matFile2 = matOut + ".ZZprob.mat";
		final MatFileWriter writer = new MatFileWriter();
		try {
			writer.write(matFile1, output1);
			System.err.println("Written to " + matFile1);
			writer.write(matFile2, output2);
			System.err.println("Written to " + matFile2);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private double[][] toArray(Matrix matrix) {
		double[][] arr = new double[matrix.numRows()][matrix.numColumns()];
		for (MatrixEntry e : matrix) {
			int i = e.row();
			int j = e.column();
			double val = e.get();
			arr[i][j] = val;
		}
		return arr;
	}
	
}
