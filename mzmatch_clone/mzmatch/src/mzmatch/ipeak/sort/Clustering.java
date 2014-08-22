package mzmatch.ipeak.sort;

import java.util.List;

public interface Clustering {
	public int getCluster(final int peak);
	public void setCluster(final int peak, final int cluster);
	public List<Integer> getClusterPeaks(final int cluster);
	public int clusterSize(final int cluster);
	public int numberOfClusters();
	public int[] getAllClusterSizes();
	public int[] peakClusteringCopy();
	public int[] getPeakClustering();
	public int numberOfPeaks();
	public void postProcess();
}
