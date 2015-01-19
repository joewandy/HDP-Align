package mzmatch.ipeak.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import java.util.Vector;

import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.Pair;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

import org.apache.commons.math3.special.Beta;

import com.google.common.base.Joiner;

import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeak.AnnotationAscending;
import peakml.IPeakSet;

public abstract class Clusterer<D extends Data, T extends AbstractClustering<D>> {
	
	protected final int numSamples;
	protected final int burnIn;
	protected final D data;
	protected final Parameters parameters;
	private final List<Integer> order = new ArrayList<Integer>();
	protected final Random random;
	protected final PeakPosteriorScorer<T> scorer;
	protected final boolean verbose;

	public static <D1 extends Data, T1 extends AbstractClustering<D1>> Vector<IPeak> findRelatedPeaks(IPeakSet<IPeak> peakset, final Clusterer<D1,T1> clusterer,
			final Random random, final List<SampleHandler<D1,T1>> handlers) {
		final int[] clustering = clusterer.bestSample(handlers);
		assert clustering != null;
		
		final List<IPeak> peaks = new ArrayList<IPeak>(peakset.getPeaks());
		for (IPeak peak : peaks)
			peak.removeAnnotation(IPeak.relationid);
		
		int i = 0;
		for (IPeak peak : peakset) {
			final int id = clustering[i++];
			peak.addAnnotation(IPeak.relationid, Integer.toString(id), Annotation.ValueType.INTEGER);
		}
		
		Collections.sort(peaks, IPeak.sort_intensity_descending);
		Collections.sort(peaks, new AnnotationAscending(IPeak.relationid));
		Vector<IPeak> basepeaks = new Vector<IPeak>();
		
		String currentRelationId = "";
		for (IPeak peak : peaks) {
			final String thisRelationId = peak.getAnnotation(IPeak.relationid).getValueAsString();
			if ( ! thisRelationId.equals(currentRelationId) ) {
				basepeaks.add(peak);
				currentRelationId = thisRelationId;
			}
		}
		
		return basepeaks;
	}
	
	public Clusterer(final int numSamples, final int burnIn, final D data, final Parameters parameters, final Random random,
			final PeakPosteriorScorer<T> scorer, final boolean verbose) {
		this.numSamples = numSamples;
		this.burnIn = burnIn;
		this.data = data;
		this.parameters = parameters;
		this.random = random;
		for (int i = 0; i < data.numPeaksets; ++i) {
			order.add(i);
		}
		this.scorer = scorer;
		this.verbose = verbose;
	}
	
	public abstract T createClustering();
	
	public List<Pair<int[],Double>> sample(final List<SampleHandler<D,T>> handlers) {
		final List<Pair<int[],Double>> samples = new ArrayList<Pair<int[],Double>>(numSamples);
		T currentSample = createClustering();
		if ( parameters.debug ) System.err.println(currentSample);
		if ( verbose) System.err.println("Starting point. Number of clusters: " + currentSample.numberOfClusters());
		for (int i = 0; i < burnIn; ++i) {
			singleSample(currentSample);
			if ( verbose ) System.err.println("Burn-in number: " + i + " Number of clusters: " + currentSample.numberOfClusters());
		}
		if ( parameters.debug ) {
			parameters.out.println(currentSample.columnNames());
		}
		for (int i = 0; i < numSamples; ++i) {
			final double currentScore = singleSample(currentSample);
			if ( verbose ) System.err.println("Draw number: " + i + " Number of clusters: " + currentSample.numberOfClusters());
			if ( parameters.debug ) {
				parameters.out.println(currentSample.toCSVString(i));
				//System.err.println("Number of clusters: " + currentSample.numberOfClusters());
			}
			for ( SampleHandler<D,T> h : handlers ) {
				h.handleSample(currentSample);
			}
			samples.add(new Pair<int[],Double>(currentSample.peakClusteringCopy(),currentScore));
		}
		return samples;
	}
	
	public int[] bestSample(final List<SampleHandler<D,T>> handlers)  {
		final List<Pair<int[],Double>> samples = sample(handlers);
		int[] bestSamp = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (Pair<int[],Double> p : samples) {
			if ( p.v2 >= bestScore ) {
				bestSamp = p.v1;
				bestScore = p.v2;
			}
		}
		if ( bestSamp == null ) {
			throw new RuntimeException("No clustering was better than NEGATIVE_INFINITY");
		}
		return bestSamp;
	}
	
	public static int samplePosition(final double[] distribution, final Random random) {
		assert distribution.length != 0;
		assert distribution[distribution.length - 1] <= 1.0f : distribution[distribution.length - 1];
		final double[] cs = cumsumNormalise(distribution);
		assert cs[cs.length - 1] == 1.0f;
		final double rn = random.nextDouble();
		int selectedPosition = -1;
		
		for (int i = 0; i < distribution.length; ++i) {		
			if ( rn < cs[i] ) {
				selectedPosition = i;
				break;
			}
		}
		return selectedPosition;
	}
	
	public double singleSample(final T currentClustering) {
		// Clustering must be from 0 to numClusters - 1
		//assert clusteringOK(currentClustering);
		Collections.shuffle(order, random);
		
		Map<Integer,String> output = new TreeMap<Integer,String>();
		Map<Integer,String> move = new TreeMap<Integer,String>();
		
		for (int i = 0; i < data.numPeaksets; ++i ) {
			final int peak = order.get(i);
			final int oldCluster = currentClustering.getCluster(peak);
			final double[] clusterLogPosterior = scorer.calculatePeakLogPosterior(currentClustering, peak);
			final List<String> moves = new ArrayList<String>();
			final String peakId = data.ids[peak];
			if ( currentClustering instanceof MoleculeClustering && parameters.options.peakLikelihoodTrace.contains(peakId) ) {
				final MoleculeClustering mClustering = (MoleculeClustering)currentClustering;
				final List<String> posteriors = new ArrayList<String>();
				
				final int currentCluster = mClustering.getCluster(peak);
				final int currentMolecule =  mClustering.getMolecule(currentCluster);
				
				moves.add(formatLikelihoods(currentClustering, peak, currentCluster, mClustering.data.theoreticalSpectrums.getName(currentMolecule), clusterLogPosterior));

				for ( String m : parameters.options.moleculeLikelihoodTrace ) {
					final int mIndex = mClustering.data.theoreticalSpectrums.getIndex(m);
					final List<Integer> mClusters = mClustering.getClustersFromMolecule(mIndex);
					
					for ( int c : mClusters ) {
						posteriors.add(formatLikelihoods(currentClustering, peak, c, m, clusterLogPosterior));
					}
				}
				final int newCluster = clusterLogPosterior.length - 1;
				posteriors.add(formatLikelihoodsNewCluster(currentClustering, peak, newCluster, "newCluster", clusterLogPosterior));
				output.put(peak, Joiner.on(",").join(posteriors));
			}
			
			assert clusterLogPosterior.length != 0;
			final double[] clusterPosterior;
			if ( currentClustering.clusterSize(oldCluster) == 1 ) {
				clusterPosterior = normaliseDistribution(clusterLogPosterior, oldCluster);
			} else {
				clusterPosterior = normaliseDistribution(clusterLogPosterior, -1);
			}
			final int selectedCluster = samplePosition(clusterPosterior, random);
			currentClustering.setCluster(peak, selectedCluster);
			if ( currentClustering instanceof MoleculeClustering && parameters.options.peakLikelihoodTrace.contains(peakId) ) {
				final MoleculeClustering mClustering = (MoleculeClustering)currentClustering;
				final int newCluster = currentClustering.getCluster(peak);
				final int currentMolecule = mClustering.getMolecule(newCluster);
				moves.add(formatLikelihoods(currentClustering, peak, newCluster, mClustering.data.theoreticalSpectrums.getName(currentMolecule), clusterLogPosterior));
				move.put(peak, Joiner.on("->").join(moves));
			}
		}
		

		for ( Map.Entry<Integer,String> e : output.entrySet() ) {
			final int peak = e.getKey();
			final double[] vals = new double[data.numReplicates];
			for ( int i = 0; i < data.numReplicates; ++i ) {
				vals[i] = data.retentionTimes[i][peak];
			}
			
			System.err.println(String.format("Peak: %s(%.0f) %s %s", data.ids[peak], Common.mean(vals, true), move.get(peak), e.getValue()));
		}

		final double l = scorer.calculateLikelihood(currentClustering);
		return l;
	}
	
	private String formatLikelihoodsNewCluster(final T c, final int peak, final int cluster, final String molecule, final double[] posterior) {
		final String mv = String.format("%d(%s)%.1f", cluster, molecule, posterior[cluster]);
		final String l = scorer.likelihoods(c, peak, cluster);
		return mv + "(" + l + ")";
	}
	
	private String formatLikelihoods(final T c, final int peak, final int cluster, final String molecule, final double[] posterior) {
		final List<Integer> peaks = c.getClusterPeaks(cluster);
		final double[][] rts = new double[data.numReplicates][peaks.size()];
		for ( int j = 0; j < data.numReplicates; ++j ) {
			for ( int i = 0; i < peaks.size(); ++i ) {
				rts[j][i] = data.retentionTimes[j][peaks.get(i)];
			}
		}
		final double meanRt = Common.mean(rts, true);
		final double varRt = Common.variance(rts, true);
		final String mv = String.format("%d[%.0f(%.0f)](%s)%.1f", cluster, meanRt, Math.sqrt(varRt), molecule, posterior[cluster]);
		final String l = scorer.likelihoods(c, peak, cluster);
		return mv + "(" + l + ")";
	}
	
	public static double[] normaliseDistribution(double[] distribution, final int pos) {
		return normaliseDistribution(distribution, pos, distribution.length);
	}
	
	public static double[] normaliseDistribution(double[] distribution, final int pos, final int length) {
		assert distribution.length != 0 && length != 0;
		final double[] normalisedDistribution = new double[length];
		final double maxDistribution = max(distribution, length);
		assert ! Double.isNaN(maxDistribution) && ! Double.isInfinite(maxDistribution) : "maxDistribution: " + maxDistribution +
			" distribution: " + Arrays.toString(distribution) + " length: " + length;
		double innerSum = 0.0f;
		for (int i = 0; i < normalisedDistribution.length; ++i) {
			if ( i == pos ) continue;
			normalisedDistribution[i] = distribution[i];
			assert ! Double.isNaN(normalisedDistribution[i]) && ! Double.isInfinite(normalisedDistribution[i]);
			innerSum += Math.exp(normalisedDistribution[i] - maxDistribution);
		}
		assert innerSum > 0.0 : "normalisedDistribution: " + Common.arrayToString(normalisedDistribution);
		final double logSumDistribution = maxDistribution + Math.log(innerSum);
		assert ! Double.isNaN(logSumDistribution);
		
		for (int i = 0; i < normalisedDistribution.length; ++i) {
			if ( i == pos ) continue;
			normalisedDistribution[i] = Math.exp(normalisedDistribution[i] - logSumDistribution);
			assert ! Double.isNaN(normalisedDistribution[i]);
		}
		return normalisedDistribution;
	}

	public static double max(final double[] array) {
		return max(array, array.length);
	}
	
	public static double max(final double[] array, final int length) {
		double m = Double.NEGATIVE_INFINITY;
		for ( int i = 0; i < length; ++i ) {
			final double v = array[i];
			if (v > m) {
				m = v;
			}
		}
		return m;
	}

	private static double[] cumsum(final double[] array) {
		final double[] cs = new double[array.length];
		cs[0] = array[0];
		for (int i = 1; i < array.length; ++i) {
			cs[i] = cs[i - 1] + array[i];
		}
		return cs;
	}
	
	private static double[] cumsumNormalise(final double[] array) {
		final double[] cs = cumsum(array);
		final double total = cs[cs.length - 1];
		for (int i = 0; i < cs.length - 1; ++i) {
			cs[i] = cs[i] / total;
		}
		cs[cs.length - 1] = 1.0f;
		return cs;
	}

	public static double calculateMassPrecision(final double massPrecisionPPM) {
		final double deviation = Common.onePPM * massPrecisionPPM;
		// This will give us 2 * sd = 3 * PPM. Hence 3PPM on both sides will cover
		// ~95% of the distribution
		final double standardDeviation = deviation / 2;
		final double precision = 1.0 / (standardDeviation * standardDeviation);
		return precision;
	}

	public interface LikelihoodScorer {
		double constantTerm();
		double correlationTerm(double correlation);
	}
	
	public static class ExpLikelihoodScorer implements LikelihoodScorer {
		final double lambda;
		final double expTerm;
		
		public ExpLikelihoodScorer(final double p1, final double lambda) {
			this.lambda = lambda;
			expTerm = Math.log((1 - p1) * lambda);
		}
		
		public double constantTerm() {
			return expTerm;
		}
		
		public double correlationTerm(double correlation) {
			return -lambda * (1.0f - correlation);
		}
	}
	
	public static class NormLikelihoodScorer implements LikelihoodScorer {
		final double mu;
		final double sigmaSquared;
		final double normTerm;
		
		public NormLikelihoodScorer(final double p0, final double mu, final double sigmaSquared) {
			this.mu = mu;
			this.sigmaSquared = sigmaSquared;
			this.normTerm = Math.log((1 - p0) / Math.sqrt(2.0 * Math.PI * sigmaSquared));
		}
		
		public double constantTerm() {
			return this.normTerm;
		}
		
		public double correlationTerm(double correlation) {
			return -(correlation - mu) * (correlation - mu) / (2.0f * sigmaSquared);
		}
	}
	
	public static abstract class BetaLikelihoodScorer implements LikelihoodScorer {
		private static double lowerBound = 0.001;
		private static double upperBound = 0.999;
		private final double lower;
		private final double range;

		
		public BetaLikelihoodScorer(final double lower, final double range) {
			this.lower = lower;
			this.range = range;
		}
		
		public double normalisedCorrelation(double correlation) {
			final double c = (correlation - lower) / range;
			if ( c < lowerBound ) {
				return lowerBound;
			} else if ( c > upperBound ) {
				return upperBound;
			}
			return c;
		}
	}
	
	public static class BetaInLikelihoodScorer extends BetaLikelihoodScorer {
		final double alpha1;
		final double betaTerm;
		
		public BetaInLikelihoodScorer(final double p1, final double alpha1, final double lower, final double upper) {
			super(lower, upper - lower);
			this.alpha1 = alpha1;
			this.betaTerm = Math.log(1 - p1) - Beta.logBeta(alpha1,1);
		}
		
		public double constantTerm() {
			return this.betaTerm;
		}
		
		public double correlationTerm(final double correlation) {
			final double nCorrelation = normalisedCorrelation(correlation);
			return (alpha1 - 1) * Math.log(nCorrelation);
		}
	}
	
	public static class BetaOutLikelihoodScorer extends BetaLikelihoodScorer {
		final double alpha0;
		final double betaTerm;
		
		public BetaOutLikelihoodScorer(final double p0, final double alpha0, final double lower, final double upper) {
			super(lower, upper - lower);
			this.alpha0 = alpha0;
			this.betaTerm = Math.log(1 - p0) - Beta.logBeta(alpha0,alpha0);
		}
		
		public double constantTerm() {
			return this.betaTerm;
		}
		
		public double correlationTerm(final double correlation) {
			final double nCorrelation = normalisedCorrelation(correlation);
			return (alpha0 - 1) * Math.log(nCorrelation * (1 - nCorrelation));
		}
	}
	
	public static class ScoringCache {
		private final FlexCompRowMatrix[] cache;
				
		public ScoringCache(final int numPeaks, final int numFormulae, final int maxFormulaPeaks) {
			cache = new FlexCompRowMatrix[numPeaks];
			for (int i = 0; i < cache.length; ++i) {
				cache[i] = new FlexCompRowMatrix(numFormulae, maxFormulaPeaks);
			}
		}
		
		public FlexCompRowMatrix get(final int peak) {
			return cache[peak];
		}
		
		public double get(final int peak, final int cluster, final int clusterPeak) {
			return cache[peak].get(cluster, clusterPeak);
		}
		
		public void set(final int peak, final int cluster, final int clusterPeak, final double value) {
			cache[peak].set(cluster, clusterPeak, value);
		}
	}
}
