package mzmatch.ipeak.sort;

import java.util.List;

public class PeakPosteriorScorer<C extends Clustering> {
	private final List<PeakLikelihoodScorer<C>> scorers;
	private final Parameters parameters;
	private final int numScorers;
	
	public PeakPosteriorScorer(final List<PeakLikelihoodScorer<C>> scorers, final Parameters parameters) {
		this.scorers = scorers;
		this.parameters = parameters;
		this.numScorers = scorers.size();
	}
	
	public double[] calculatePeakLogPosterior(final C clustering, int peak) {
		//System.err.println("Calculating posterior for peak: " + peak);
		final int K = clustering.numberOfClusters();
		final double[] clusterLikelihood = calculatePeakLikelihood(clustering, peak);
		//System.err.println("Likelihood: " + Arrays.toString(clusterLikelihood));
		final int[] clusterSizes = clustering.getAllClusterSizes();
		for (int i = 0; i < K; ++i) {
			assert ! Double.isNaN(clusterLikelihood[i]);
			assert clusterSizes[i] > 0;
			clusterLikelihood[i] += Math.log(clusterSizes[i]);
			assert ! Double.isNaN(clusterLikelihood[i]);
		}
		clusterLikelihood[K] += Math.log(parameters.alpha);
		//System.err.println("Posterior: " + Arrays.toString(clusterLikelihood));
		return clusterLikelihood;
	}
	
	public double[] calculatePeakLikelihood(final C clustering, final int peak) {
		final int K = clustering.numberOfClusters();
		final double[] clusterLikelihood = new double[K + 1];
		for ( int j = 0; j < numScorers; ++j ) {
			final PeakLikelihoodScorer<C> scorer = scorers.get(j);
			final double[] likelihood = scorer.calculatePeakLikelihood(clustering, peak);
			for ( int i = 0; i < K + 1; ++i ) {
				clusterLikelihood[i] += likelihood[i];
			}
		}
		return clusterLikelihood;
	}
	
	public String likelihoods(final C clustering, final int peak, final int cluster) {
		final StringBuilder builder = new StringBuilder();
		for ( PeakLikelihoodScorer<C> scorer : scorers ) {
			final double[] likelihood = scorer.calculatePeakLikelihood(clustering, peak);
			builder.append(likelihood[cluster] + "/");
		}
		if ( cluster == clustering.numberOfClusters() ) {
			builder.append(Math.log(parameters.alpha));
		} else {
			builder.append(Math.log(clustering.getAllClusterSizes()[cluster]));
		}
		return builder.toString();
	}
	
	public double calculateLikelihood(final C currentClustering) {
		return 0.0;
	}
}
