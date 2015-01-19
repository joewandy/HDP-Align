package mzmatch.ipeak.sort;

import java.util.Random;

import mzmatch.ipeak.sort.Clusterer.LikelihoodScorer;

public class SimpleClustering extends AbstractClustering<Data> {
//	private final ClusteringScorer<SimpleClustering> scorer;
	
	public static SimpleClustering createSimpleClustering(final CorrelationParameters parameters, final Random random,
			final Data data, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final CorrelationMeasure measure) {
		final int[] peakClustering = new int[data.numPeaksets];
		for (int i = 0; i < data.numPeaksets; ++i) {
			peakClustering[i] = random.nextInt(parameters.initialNumClusters);
		}
		return new SimpleClustering(peakClustering, data, parameters, inScorer, outScorer, measure);
	}

	private SimpleClustering(final int[] initialClustering, final Data data, final CorrelationParameters parameters,
			final LikelihoodScorer inScorer, final LikelihoodScorer outScorer, final CorrelationMeasure measure) {
		super(initialClustering, data, measure);
		//scorer = new ClusteringScorer<SimpleClustering>(data, parameters, inScorer, outScorer, measure);
		//scorer = new ClusteringScorer(peakset, likelihood, baseLikelihood, alpha, defaultLikelihood);
	}
/*
	@Override
	public double[] calculatePeakLogPosterior(int peak) {
		return scorer.calculatePeakLogPosterior(this, peak);
	}

	@Override
	public double calculateLikelihood() {
		return scorer.calculateLikelihood(this);
	}
*/	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for ( int peak = 0; peak < peakClustering.length; ++peak) {
			final int cluster = peakClustering[peak];
			builder.append("Peak: " + peak);
			builder.append(" cluster: " + cluster);
			//builder.append(" corrs: " + correlations(peak));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	@Override
	public String toCSVString(final int sampleNumber) {
		final StringBuilder builder = new StringBuilder();
		for ( int peak = 0; peak < peakClustering.length; ++peak) {
			final int cluster = peakClustering[peak];
			builder.append(sampleNumber + ",");
			builder.append(peak + ",");
			builder.append(cluster + ",");
			//builder.append(" corrs: " + correlations(peak));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	@Override
	public String columnNames() {
		return "sample,peak,cluster";
	}

}