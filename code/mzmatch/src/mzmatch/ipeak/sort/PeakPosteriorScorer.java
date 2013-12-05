package mzmatch.ipeak.sort;

import java.util.ArrayList;
import java.util.List;

import mzmatch.ipeak.sort.FormulaClusterer.MassIntensityClusteringScorer;

import com.google.common.base.Joiner;

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
		final int K = clustering.numberOfClusters();
		final double[] clusterLikelihood = calculatePeakLikelihood(clustering, peak);
		final int[] clusterSizes = clustering.getAllClusterSizes();
		for (int i = 0; i < K; ++i) {
			assert ! Double.isNaN(clusterLikelihood[i]);
			assert clusterSizes[i] > 0;
			clusterLikelihood[i] += Math.log(clusterSizes[i]);
			assert ! Double.isNaN(clusterLikelihood[i]);
		}
		clusterLikelihood[K] += Math.log(parameters.alpha);
		return clusterLikelihood;
	}
	
	public double[] calculatePeakLikelihood(final C clustering, final int peak) {
		final int K = clustering.numberOfClusters();
		final double[] clusterLikelihood = new double[K + 1];
		for ( int j = 0; j < numScorers; ++j ) {
			final PeakLikelihoodScorer<C> scorer = scorers.get(j);
			final double[] likelihood = scorer.calculatePeakLikelihood(clustering, peak);
			for ( int i = 0; i < K + 1; ++i ) {
				assert ! Double.isNaN(likelihood[i]) : scorers.get(j);
				clusterLikelihood[i] += likelihood[i];
			}
		}
		return clusterLikelihood;
	}
	
	public String likelihoods(final C clustering, final int peak, final int cluster) {
		final List<String> likes = new ArrayList<String>();
		for ( PeakLikelihoodScorer<C> scorer : scorers ) {
			final double[] likelihood = scorer.calculatePeakLikelihood(clustering, peak);
			String like = String.format("%.1f", likelihood[cluster]);
			if ( scorer instanceof MassIntensityClusteringScorer ) {
				final MoleculeClustering mClustering = (MoleculeClustering)clustering;
				like = like + "{" + ((MassIntensityClusteringScorer)scorer).likelihoods(mClustering, peak, cluster) + "}";
			}
			likes.add(like);
		}
		if ( cluster == clustering.numberOfClusters() ) {
			likes.add(String.format("%.1f", Math.log(parameters.alpha)));
		} else {
			likes.add(String.format("%.1f", Math.log(clustering.getAllClusterSizes()[cluster])));
		}
		return Joiner.on(" ").join(likes);
	}
	
	public double calculateLikelihood(final C currentClustering) {
		return 0.0;
	}
}
