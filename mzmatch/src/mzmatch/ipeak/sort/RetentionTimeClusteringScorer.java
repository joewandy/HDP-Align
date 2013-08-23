package mzmatch.ipeak.sort;

import java.util.Arrays;

import com.google.common.primitives.Doubles;

import mzmatch.ipeak.util.Common;

public class RetentionTimeClusteringScorer<C extends Clustering> implements PeakLikelihoodScorer<C> {
	private final double kappa0;
	private final double mu0;
	private final double kappa;
	private final Data data;
	private final Parameters parameters;
	
	public RetentionTimeClusteringScorer(final Data data, final Parameters parameters) {
		this.data = data;
		this.parameters = parameters;
		this.mu0 = (Common.max(data.retentionTimes, true) - Common.min(data.retentionTimes, true)) / 2;
		// 2 * sd = 1 s, therefore, sd = 1 s / 2
		//final double[] flatRetentionTimes = Doubles.concat(data.retentionTimes);
		//final double variance = Common.variance(flatRetentionTimes, true);
		//kappa0 = 1 / variance;
		this.kappa0 = 1e-10;
		//final double standardDeviation = 5.0 / 2.0;
		this.kappa = 1 / (parameters.retentionTimeSD * parameters.retentionTimeSD);
		//System.out.println("rt_kappa0: " + kappa0);
		//System.out.println("rt_mu0: " + mu0);
	}
	
	
	public double[] calculatePeakLikelihood(final C currentClustering, final int peak) {
		final int K = currentClustering.numberOfClusters();
		final double[] clusterLikelihood = new double[K + 1];
		final double[] kappaK = calculateKappaK(currentClustering, peak);
		for (int rep = 0; rep < data.numReplicates; ++rep) {
			final double retentionTime = data.retentionTimes[rep][peak];
			if ( Double.isNaN(retentionTime) ) {
				continue;
			}
			final double[] muK = calculateMuK(currentClustering, peak, kappaK, rep);
			for ( int k = 0; k < clusterLikelihood.length; ++k) {
				final double mu_w = muK[k];
				final double kappa_w = 1 / (1/kappa + 1/kappaK[k]);
				//if ( Math.abs(retentionTime - mu_w) > parameters.rtWindow ) {
				//	clusterLikelihood[k] = -Double.MAX_VALUE;
				//} else {
					clusterLikelihood[k] += Common.normalDensity(retentionTime, mu_w, kappa_w, true);
				//}
			}
		}
		return clusterLikelihood;
	}
	
	private double[] calculateMuK(final Clustering currentClustering, final int peak, double[] kappaK, int rep) {
		final double[] retval = new double[currentClustering.numberOfClusters() + 1];
		final int[] clusters = currentClustering.getPeakClustering();

		for ( int i = 0; i < data.numPeaksets; ++i ) {
			if ( i == peak ) continue;
			retval[clusters[i]] += data.retentionTimes[rep][i];
		}
		for (int i = 0; i < retval.length; ++i) {
			retval[i] = (kappa0 * mu0 + retval[i] * kappa) / kappaK[i];
		}
		return retval;
	}

	private double[] calculateKappaK(final Clustering currentClustering, final int peak) {
		final double[] retval = new double[currentClustering.numberOfClusters() + 1];
		Arrays.fill(retval, kappa0);
		final int[] clusters = currentClustering.getPeakClustering();
		for ( int i = 0; i < data.numPeaksets; ++i ) {
			if ( i == peak ) continue;
			retval[clusters[i]] += kappa;
		}
		return retval;
	}
}
