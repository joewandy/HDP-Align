package mzmatch.ipeak.sort;

public interface PeakLikelihoodScorer<C extends Clustering> {
	public double[] calculatePeakLikelihood(final C currentClustering, final int peak);
}
