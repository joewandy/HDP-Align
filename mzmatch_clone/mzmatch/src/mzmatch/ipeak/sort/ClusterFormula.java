package mzmatch.ipeak.sort;

import java.util.List;

import mzmatch.ipeak.sort.FormulaClusterer.MassIntensityClusteringScorer;

public class ClusterFormula {
	private final List<Integer> clusterFormulas;
	private List<Integer>[] formulaClusters;
	
	private final Data data;
	private final Clustering clustering;
	private final MassIntensityClusteringScorer scorer;
	
	public ClusterFormula(final Clustering clustering, final Data data, final MassIntensityClusteringScorer scorer) {
		this.data = data;
		this.clustering = clustering;
		this.scorer = scorer;
		this.formulaClusters = null;
		clusterFormulas = null;
	}
	
	private void initialise() {
		for ( int cluster = 0; cluster < clustering.numberOfClusters(); ++cluster ) {
			update(cluster, cluster);
		}
	}
	
	public void update(final int oldCluster, final int newCluster) {
		if ( clustering.numberOfClusters() < clusterFormulas.size() ) {
			clusterFormulas.remove(oldCluster);
			//recalculate(newCluster);
		} else if ( clustering.numberOfClusters() > clusterFormulas.size() ) {
			clusterFormulas.add(-1);
			//recalculateTime(oldCluster);
			recalculate(newCluster);
		}
	}
	
	public int getFormula(final int cluster) {
		assert cluster >= 0 && cluster < clustering.numberOfClusters() : "cluster: " + cluster + " numberOfClusters: " + clustering.numberOfClusters();
		return clusterFormulas.get(cluster);
	}
	
	private void recalculate(final int cluster) {
		final List<Integer> peaks = clustering.getClusterPeaks(cluster);
		assert peaks.size() == 1 : peaks.size();
		final int peak = peaks.get(0);
		final int newFormula = scorer.pickFormula(peak);
		clusterFormulas.set(cluster, newFormula);
	}
	
	
}
