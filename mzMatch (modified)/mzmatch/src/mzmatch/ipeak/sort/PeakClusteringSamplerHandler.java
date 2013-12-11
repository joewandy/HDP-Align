package mzmatch.ipeak.sort;

import java.util.List;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.Matrix;


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
	
	@Override
	public void handleSample(SimpleClustering clustering) {

		System.err.println("\thandleSample iter=" + numSamples);		
		numSamples++;
		
		int numPeaks = clustering.numberOfPeaks();
		int numClusters = clustering.numberOfClusters();
		assert(numPeaks == this.numPeaks);
		
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

	public Matrix getZZall() {
		return ZZall;
	}
	
}
