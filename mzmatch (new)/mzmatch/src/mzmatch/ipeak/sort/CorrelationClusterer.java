package mzmatch.ipeak.sort;

import java.util.Arrays;
import java.util.Random;

import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import peakml.math.Signal;

public class CorrelationClusterer extends Clusterer<Data,SimpleClustering> {
	//private final Data data;
	//private final IPeakSet<IPeak> peakset;
	//private final Header header;
	private final CorrelationParameters parameters;
	private final LikelihoodScorer inScorer;
	private final LikelihoodScorer outScorer;
	private final CorrelationMeasure measure;

	public CorrelationClusterer(final Data data, final CorrelationParameters parameters, final Random random,
			final LikelihoodScorer inScorer, final LikelihoodScorer outScorer, final CorrelationMeasure measure,
			final PeakPosteriorScorer<SimpleClustering> scorer, final boolean verbose) {
		super(parameters.numSamples, parameters.burnIn, data, parameters, random, scorer, verbose);
		this.parameters = parameters;
		this.inScorer = inScorer;
		this.outScorer = outScorer;
		this.measure = measure;
	}

	@Override
	public SimpleClustering createClustering() {
		return SimpleClustering.createSimpleClustering(parameters, random, data, inScorer, outScorer, measure);
	}
	/*
	public static class SimpleClustering extends AbstractClustering {
		private final ClusteringScorer<SimpleClustering> scorer;
		
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
			super(initialClustering);
			scorer = new ClusteringScorer<SimpleClustering>(data, parameters, inScorer, outScorer, measure);
			//scorer = new ClusteringScorer(peakset, likelihood, baseLikelihood, alpha, defaultLikelihood);
		}

		@Override
		public double[] calculatePeakLogPosterior(int peak) {
			return scorer.calculatePeakLogPosterior(this, peak);
		}

		@Override
		public double calculateLikelihood() {
			return scorer.calculateLikelihood(this);
		}
	
		@Override
		public String toString() {
			final StringBuilder builder = new StringBuilder();
			for ( int peak = 0; peak < peakClustering.length; ++peak) {
				final int cluster = peakClustering[peak];
				builder.append("Peak: " + peak);
				builder.append(" cluster: " + cluster);
				builder.append("\n");
			}
			return builder.toString();
		}

	}
	*/
	
	public static class ClusteringScorer<C extends Clustering> implements PeakLikelihoodScorer<C> {
		private final Parameters parameters;
		private final CorrelationMeasure measure;
		
		private FlexCompRowMatrix correlationLikelihood;
		private double[] baseLikelihood;
		private double defaultLikelihood;
		
		public ClusteringScorer(final Data data, final Parameters parameters, final LikelihoodScorer inScorer,
				final LikelihoodScorer outScorer, final CorrelationMeasure measure) {
			this.parameters = parameters;
			this.measure = measure;
			calculateCorrelationLikelihoods(data, inScorer, outScorer);
		}
		
		private void calculateCorrelationLikelihoods(final Data data,
				final LikelihoodScorer inScorer, final LikelihoodScorer outScorer) {
			//final SignalsAndRetentionTimes sart = ShapeCorrelations.getSignals(header, peakset);
			assert inScorer != null;
			assert outScorer != null;
			final double inTerm = inScorer.constantTerm();
			final double outTerm = outScorer.constantTerm();
			
			final double outWindowIn = parameters.p1 == 0.0 ? Double.NEGATIVE_INFINITY : Math.log(parameters.p1);
			final double outWindowOut = parameters.p0 == 0.0 ? Double.NEGATIVE_INFINITY : Math.log(parameters.p0);

			final FlexCompRowMatrix likelihood = new FlexCompRowMatrix(data.numPeaksets, data.numPeaksets);
			final double[] baseLikelihood = new double[data.numPeaksets];
			
			for (int rep = 0; rep < data.numReplicates; ++rep) {
				for (int i = 0; i < data.numPeaksets - 1; ++i) {
					final double signal1rt = data.retentionTimes[rep][i];
					//System.err.println("rttime: " + signal1rt);
					final Signal signal1 = data.signals[rep][i];
					for (int j = i + 1; j < data.numPeaksets; ++j) {
						final double signal2rt = data.retentionTimes[rep][j];
						final Signal signal2 = data.signals[rep][j];
						double sameCluster;
						double differentCluster;
						if ( Math.abs(signal1rt - signal2rt) < parameters.rtWindow ) {
							final double correlation;
							if ( signal1 == null || signal2 == null ) {
								correlation = 0.0;
							} else {
								correlation = measure.correlation(signal1, signal2);
							}
							sameCluster = inTerm + inScorer.correlationTerm(correlation);
							differentCluster = outTerm + outScorer.correlationTerm(correlation);
							assert ! Double.isNaN(sameCluster) : correlation;
							likelihood.add(i,j,sameCluster - differentCluster);
							likelihood.set(j, i, likelihood.get(i,j));
						} else {
							sameCluster = outWindowIn;
							differentCluster = outWindowOut;
						}
						assert ! Double.isNaN(sameCluster);
						assert ! Double.isNaN(differentCluster);
						assert ! Double.isNaN(likelihood.get(i,j)) : " " + i + " " + j;
						baseLikelihood[i] += differentCluster;
						baseLikelihood[j] += differentCluster;
					}
				}
			}
			
			this.correlationLikelihood = likelihood;
			this.baseLikelihood = baseLikelihood;
			this.defaultLikelihood = data.numReplicates * (outWindowIn - outWindowOut);
		}
		
		public double[] calculatePeakLikelihood(final Clustering clustering, final int peak) {
			final int K = clustering.numberOfClusters();
			final double[] clusterLikelihood = new double[K + 1];
			Arrays.fill(clusterLikelihood, baseLikelihood[peak]);
			
			final int[] clusterSizes = clustering.getAllClusterSizes().clone();
			final double[] like = correlationLikelihood.getRow(peak).getData();
			final int[] indices = correlationLikelihood.getRow(peak).getIndex();

			final int[] c = clustering.getPeakClustering();
			for (int i = 0; i < indices.length; ++i) {
				final int cluster = c[indices[i]];
				clusterLikelihood[cluster] += like[i];
				clusterSizes[cluster]--;
			}
			clusterSizes[c[peak]]--;
			
			for (int cluster = 0; cluster < clusterSizes.length; ++cluster) {
				clusterLikelihood[cluster] += clusterSizes[cluster] * this.defaultLikelihood;
			}
			/*
			for (int cluster = 0; cluster < K + 1; ++cluster) {
				clusterLikelihood[cluster] /= (clustering.numberOfPeaks() - 1);
			}
			*/
			return clusterLikelihood;
		}
		/*
		public double[] calculatePeakLogPosterior(final AbstractClustering clustering, int peak) {
			//System.err.println("Calculating posterior for peak: " + peak);
			final int K = clustering.numberOfClusters();
			final double[] clusterLikelihood = calculatePeakLikelihood(clustering, peak);
			final int[] clusterSizes = clustering.getAllClusterSizes();
			for (int i = 0; i < K; ++i) {
				assert clusterSizes[i] != 0;
				clusterLikelihood[i] +=  Math.log(clusterSizes[i]);
				assert ! Double.isNaN(clusterLikelihood[i]);
			}
			clusterLikelihood[K] += Math.log(parameters.alpha);
			return clusterLikelihood;
		}
		*/
		/*
		private double calculateLikelihood(final AbstractClustering clustering) {
			double totalLikelihood = 0.0f;

			for (int i = 0; i < clustering.numberOfPeaks(); ++i) {
				final double[] like = calculatePeakLikelihood(clustering, i);
				for (int j = 0; j < like.length - 1; ++j) {
					totalLikelihood += like[j];
				}
			}
			return totalLikelihood;
		}
		*/
	}
}
