package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import mzmatch.ipeak.util.Common;

public class ClusterRetentionTime {
	private final double kappa0;
	private final double mu0;
	private final double kappa;
	
	private final List<Double>[] muK;
	private final List<Double>[] sumRT;
	private final List<Double> kappaK;
	
	private final Data data;
	private final Clustering clustering;
	
	@SuppressWarnings("unchecked")
	public ClusterRetentionTime(final Clustering currentClustering, final Data data, final Parameters parameters) {
		this.mu0 = (Common.max(data.retentionTimes, true) - Common.min(data.retentionTimes, true)) / 2;
		this.kappa0 = 1e-10;
		this.kappa = 1 / (parameters.retentionTimeSD * parameters.retentionTimeSD);
		muK = (List<Double>[])Array.newInstance(List.class, data.numReplicates);
		sumRT = (List<Double>[])Array.newInstance(List.class, data.numReplicates);
		kappaK = new ArrayList<Double>();
		this.data = data;
		this.clustering = currentClustering;
		initialise();
	}
	
	private void initialise() {
		//kappaK = new ArrayList<Double>();
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			muK[rep] = new ArrayList<Double>();
			sumRT[rep] = new ArrayList<Double>();
		}
		for ( int cluster = 0; cluster < clustering.numberOfClusters(); ++cluster ) {
			update(cluster, cluster);
		}
	}
	
	public double[] calculatePeakLikelihood(final int peak) {
		final int K = clustering.numberOfClusters();
		final double[] clusterLikelihood = new double[K + 1];
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			if ( data.isMissing(rep, peak) ) {
				continue;
			}
			final double retentionTime = data.retentionTimes[rep][peak];
			for ( int k = 0; k < clusterLikelihood.length; ++k ) {
				final double kappa_k = getKappaK(k, peak);
				final double mu_w = getMuK(rep, k, peak, kappa_k);
				
				//final double mu_w = muK[rep].get(k);
				final double kappa_w = 1 / (1/kappa + 1/kappa_k);
				clusterLikelihood[k] += Common.normalDensity(retentionTime, mu_w, kappa_w, true);
			}
		}
		return clusterLikelihood;
	}
	
	public void update(final int oldCluster, final int newCluster) {
		if ( clustering.numberOfClusters() < kappaK.size() ) {
			kappaK.remove(oldCluster);
			for ( int rep = 0; rep < data.numReplicates; ++rep ) {
				muK[rep].remove(oldCluster);
				sumRT[rep].remove(oldCluster);
			}
			recalculateTime(newCluster);
		} else if ( clustering.numberOfClusters() > kappaK.size() ) {
			kappaK.add(Double.NaN);
			for ( int rep = 0; rep < data.numReplicates; ++rep ) {
				muK[rep].add(Double.NaN);
				sumRT[rep].add(Double.NaN);
			}
			recalculateTime(oldCluster);
			recalculateTime(newCluster);
		} else if ( clustering.clusterSize(newCluster) > 1 ){
			recalculateTime(oldCluster);
			recalculateTime(newCluster);
		}
	}
	
	private void recalculateTime(final int cluster) {
		updateKappaK(cluster);
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			updateMuK(cluster, rep);
		}
	}
	
	private void updateMuK(final int cluster, final int rep) {
		final List<Integer> clusterPeaks = clustering.getClusterPeaks(cluster);
		double value = 0.0;
		for ( int i = 0; i < clusterPeaks.size(); ++i ) {
			value += data.retentionTimes[rep][i];
		}
		sumRT[rep].set(cluster, value);
		value = ( kappa0 * mu0 + value * kappa ) / kappaK.get(cluster);
		muK[rep].set(cluster, value);
	}
	
	private double getMuK(final int rep, final int cluster, final int peak,
			final double kappa_k) {
		if ( clustering.getCluster(peak) == cluster ) {
			final double rt = sumRT[rep].get(cluster) - data.retentionTimes[rep][peak];
			return ( kappa0 * mu0 + rt * kappa ) / kappa_k;
		} else if ( cluster == clustering.numberOfClusters() ) {
			return kappa0 * mu0 / kappa_k;
		}
		return muK[rep].get(cluster);
	}
	
	private void updateKappaK(final int cluster) {
		assert cluster >= 0 && cluster <= clustering.numberOfClusters();
		double value = kappa0 + kappa * clustering.clusterSize(cluster);
		kappaK.set(cluster, value);
	}
	
	private double getKappaK(final int cluster, final int peak) {
		if ( clustering.getCluster(peak) == cluster ) {
			return kappa0 + kappa * ( clustering.clusterSize(cluster) - 1 );
		} else if ( cluster == clustering.numberOfClusters() ) {
			return kappa0;
		}
		return kappaK.get(cluster);
	}
}
