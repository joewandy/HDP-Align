package mzmatch.ipeak.sort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import mzmatch.ipeak.util.Common;

public abstract class AbstractClustering<D extends Data> implements Clustering {
	protected final int[] peakClustering;
	protected final List<List<Integer>> clusterPeaks = new ArrayList<List<Integer>>();
	//private final List<Integer> allClusterSizes;
	protected int[] allClusterSizes;
	protected final D data;
	private final CorrelationMeasure measure;
	
	//public abstract double[] calculatePeakLogPosterior(final int peak);
	//public abstract double calculateLikelihood();
	
	
	
	public AbstractClustering(final int[] initialClustering, final D data, final CorrelationMeasure measure) {
		this.peakClustering = initialClustering;
		this.data = data;
		this.measure = measure;
		initialiseClusterPeaks();
		generateAllClusterSizes();
	}
	
	public void initialiseClusterPeaks() {
		final int K = numClusters(peakClustering);
		for (int i = 0; i < K; ++i) {
			clusterPeaks.add(new ArrayList<Integer>());
		}
		
		for (int i = 0; i < peakClustering.length; ++i) {
			clusterPeaks.get(peakClustering[i]).add(i);
		}
	}
	
	public int getCluster(final int peak) {
		return peakClustering[peak];
	}
	
	public void setCluster(final int peak, final int cluster) {
		assert clusteringOK();
		final int oldCluster = peakClustering[peak];

		
		//System.err.println("Peak: " + peak + " Cluster: " + cluster + " oldCluster: " + oldCluster + " oldSize: " + clusterSize(oldCluster));
		//System.err.println(numberOfClusters());
		final int oldNumberOfClusters = numberOfClusters();
		assert cluster >=0 && cluster <= oldNumberOfClusters : cluster;
		if ( cluster == oldCluster ) {
			// Do nothing
		} else if ( cluster == oldNumberOfClusters && clusterSize(oldCluster) == 1 ) {
			// Do nothing
		} else  {
			if ( cluster == oldNumberOfClusters ) {
				assert clusterPeaks.size() <= data.numPeaksets;
				clusterPeaks.add(new ArrayList<Integer>());
				assert clusterPeaks.size() <= data.numPeaksets;
			}
			peakClustering[peak] = cluster;
			clusterPeaks.get(oldCluster).remove(Integer.valueOf(peak));
			assert ! clusterPeaks.get(cluster).contains(peak);
			clusterPeaks.get(cluster).add(peak);

			if ( clusterPeaks.get(oldCluster).size() == 0 ) {
				for (int i = 0; i < peakClustering.length; ++i) {
					if ( peakClustering[i] > oldCluster ) {
						peakClustering[i]--;
					}
				}
				clusterPeaks.remove(oldCluster);
			}
			generateAllClusterSizes();
		}
		assert clusteringOK() : "Peak: " + peak + " Cluster: " + cluster + " size: " + clusterSize(cluster) +
			" oldSize: " + clusterSize(oldCluster) + " oldNumberOfClusters: " + oldNumberOfClusters +
			" oldCluster: " + oldCluster;
	}

	public List<Integer> getClusterPeaks(final int cluster) {
		return clusterPeaks.get(cluster);
	}
	
	public int clusterSize(final int cluster) {
		return allClusterSizes[cluster];
//		return clusterPeaks.get(cluster).size();
	}
	
	public int numberOfClusters() {
		assert clusterPeaks.size() <= data.numPeaksets;
		return clusterPeaks.size();
	}
	
	private void generateAllClusterSizes() {
		allClusterSizes = new int[numberOfClusters()];
		for (int i = 0; i < allClusterSizes.length; ++i) {
			allClusterSizes[i] = clusterPeaks.get(i).size();
		}
	}
	
	public int[] getAllClusterSizes() {
		return allClusterSizes;
	}
	
	public int[] peakClusteringCopy() {
		return peakClustering.clone();
	}
	
	public int[] getPeakClustering() {
		return peakClustering;
	}
	
	private boolean clusteringOK() {
		final int K = numberOfClusters();
		if ( K != numClusters(peakClustering) ) {
			assert false : "K: " + K + " numClusters(peakClustering): " + numClusters(peakClustering);
		}
		for (int c : peakClustering) {
			if ( c < 0 || c >= K ) {
				assert false;
			}
		}
		for (int i = 0; i < K; ++i) {
			final List<Integer> c = clusterPeaks.get(i);
			for (int peak : c) {
				if ( peakClustering[peak] != i ) {
					assert false;
				}
			}
		}
		int size = 0;
		for (int i = 0; i < K; ++i) {
			size += clusterPeaks.get(i).size();
		}
		if ( size != peakClustering.length ) {
			assert false;
		}
		
		return true;
	}
	
	private static int numClusters(int[] clustering) {
		final Set<Integer> set = new HashSet<Integer>();
		for (int i : clustering) set.add(i);
		return set.size();
	}
	
	public int numberOfPeaks() {
		return peakClustering.length;
	}
	
	public void postProcess() {}
	
	public String averageRt(final int peak) {
		final double[] rts = new double[data.numReplicates];
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			rts[rep] = data.retentionTimes[rep][peak];
		}
		return Double.toString(Common.mean(rts, true));
	}
	
	public String rtDiff(final int peak) {
		final StringBuilder builder = new StringBuilder();
		final List<Integer> otherPeaks = getClusterPeaks(getCluster(peak));
		for ( int peak2 : otherPeaks ) {
			final double corr = averageRtDiff(peak, peak2);
			builder.append(String.format("%d/%.2f:", peak2, corr));
		}
		return builder.toString();
	}
	
	public double averageRtDiff(final int peak1, final int peak2) {
		final double[] rtDiffs = new double[data.numReplicates];
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			rtDiffs[rep] = data.retentionTimes[rep][peak1] - data.retentionTimes[rep][peak2];
		}
		return Common.mean(rtDiffs, true);
	}
	
	public String correlations(final int peak) {
		final StringBuilder builder = new StringBuilder();
		final List<Integer> otherPeaks = getClusterPeaks(getCluster(peak));
		for ( int peak2 : otherPeaks ) {
			final double corr = averageCorrelation(peak, peak2);
			builder.append(String.format("%d/%.2f:", peak2, corr));
		}
		return builder.toString();
	}
	
	private double averageCorrelation(final int peak1, final int peak2) {
		final double[] corrs = new double[data.numReplicates];
		for ( int rep = 0; rep < data.numReplicates; ++rep ) {
			final double corr = measure.correlation(data.signals[rep][peak1], data.signals[rep][peak2]);
			if ( data.signals[rep][peak1] == null || data.signals[rep][peak2] == null ) {
				corrs[rep] = Double.NaN;
			} else {
				corrs[rep] = corr;
			}
		}
		return Common.mean(corrs, true);
	}
	
	public abstract String toString();
	public abstract String toCSVString(final int sampleNumber);
	public abstract String columnNames();
 }