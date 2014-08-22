package mzmatch.ipeak.sort;

import java.util.Arrays;

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
		this.mu0 = Common.mean(data.retentionTimes, true);
		//System.err.format("Retention prior mean (mu0): %.0f\n", this.mu0);
		this.kappa0 = parameters.options.kappa0;
		this.kappa = 1 / (parameters.retentionTimeSD * parameters.retentionTimeSD);
	}
	
	public double[] getClusterMeans(final C currentClustering, final int rep) {
		final int K = currentClustering.numberOfClusters();
		final double[] clusterMeans = new double[K];
		final double[] kappaK = calculateKappaK(currentClustering, -1, rep);
		final double[] muK = calculateMuK(currentClustering, -1, kappaK, rep);
		for ( int k = 0; k < K; ++k) {
			clusterMeans[k] = muK[k];
		}
		return clusterMeans;
	}
	
	public double[] getClusterMeans(final C currentClustering) {
		final int K = currentClustering.numberOfClusters();
		final double[] clusterMeans = new double[K];
		final double[] kappaK = calculateKappaK(currentClustering, -1);
		final double[] muK = calculateMuK(currentClustering, -1, kappaK);
		for ( int k = 0; k < K; ++k) {
			clusterMeans[k] = muK[k];
		}
		return clusterMeans;
	}

	public double[] calculatePeakLikelihood(final C currentClustering, final int peak) {
		final int K = currentClustering.numberOfClusters();
		final double[] clusterLikelihood = new double[K + 1];
		for (int rep = 0; rep < data.numReplicates; ++rep) {
			final double[] kappaK = calculateKappaK(currentClustering, peak, rep);
			final double retentionTime = data.retentionTimes[rep][peak];
			if ( Double.isNaN(retentionTime) ) {
				continue;
			}
			final double[] muK = calculateMuK(currentClustering, peak, kappaK, rep);
			for ( int k = 0; k < clusterLikelihood.length; ++k) {
				final double mu_w = muK[k];
				final double kappa_w = 1 / (1/kappa + 1/kappaK[k]);
				clusterLikelihood[k] += Common.normalDensity(retentionTime, mu_w, kappa_w, true);
				assert ! Double.isNaN(clusterLikelihood[k]) && ! Double.isInfinite(clusterLikelihood[k]) :
					"peakLikelihood[k]: " + clusterLikelihood[k] + " retentionTime: " + retentionTime + " mu_w: " + mu_w +
					" kappa_w: " + kappa_w;
			}
		}
		return clusterLikelihood;
	}
	
	private double[] calculateMuK(final Clustering currentClustering, final int peak, double[] kappaK, int rep) {
		final double[] retval = new double[currentClustering.numberOfClusters() + 1];
		final int[] clusters = currentClustering.getPeakClustering();

		for ( int i = 0; i < data.numPeaksets; ++i ) {
			if ( i == peak || Double.isNaN(data.retentionTimes[rep][i]) ) continue;
			retval[clusters[i]] += data.retentionTimes[rep][i];
		}
		for (int i = 0; i < retval.length; ++i) {
			retval[i] = (kappa0 * mu0 + retval[i] * kappa) / kappaK[i];
		}
		return retval;
	}

	private double[] calculateKappaK(final Clustering currentClustering, final int peak, final int rep) {
		final double[] retval = new double[currentClustering.numberOfClusters() + 1];
		Arrays.fill(retval, kappa0);
		final int[] clusters = currentClustering.getPeakClustering();
		for ( int i = 0; i < data.numPeaksets; ++i ) {
			if ( i == peak || Double.isNaN(data.retentionTimes[rep][i]) ) continue;
			retval[clusters[i]] += kappa;
		}
		return retval;
	}
	
	private double[] calculateMuK(final Clustering currentClustering, final int peak, double[] kappaK) {
		final double[] retval = new double[currentClustering.numberOfClusters() + 1];
		final int[] clusters = currentClustering.getPeakClustering();

		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			for ( int i = 0; i < data.numPeaksets; ++i ) {
				if ( i == peak || Double.isNaN(data.retentionTimes[rep][i]) ) continue;
				retval[clusters[i]] += data.retentionTimes[rep][i];
			}
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
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			for ( int i = 0; i < data.numPeaksets; ++i ) {
				if ( i == peak || Double.isNaN(data.retentionTimes[rep][i]) ) continue;
				retval[clusters[i]] += kappa;
			}
		}
		return retval;
	}
}
