package mzmatch.experimental;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.SortedMap;
import java.util.Vector;

import mzmatch.ipeak.sort.CorrelationMeasure;
import mzmatch.ipeak.sort.Data;
import mzmatch.ipeak.sort.ShapeCorrelations;
import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.Pair;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

import org.apache.commons.math3.special.Beta;

import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeak.AnnotationAscending;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.Peak;
import peakml.chemistry.Molecule;
import peakml.io.Header;
import peakml.math.Signal;

public class MixtureClusterer {
	private static double massCutOff = 1e-10; // The likelihood, below which we remove the value.
	
	private final double p1;
	private final double p0;
	private final double alpha;
	private final int numSamples;
	private final int initialNumClusters;
	
	//private final int numReplicates;
	//private final int numPeaks;
	private final Data data;
	private final int numFormulae;
	private final String[] adducts;
	private final double minDistributionValue;
	private final int maxValues;
	private final double massPrecision;
	
	private double[] baseLikelihood;
	private FlexCompRowMatrix correlationLikelihood;
	private double defaultLikelihood;
	
	private final List<Integer> order = new ArrayList<Integer>();
	
	private final Random random;
	private final CorrelationMeasure measure;

	public static Vector<IPeak> findRelatedPeaks(IPeakSet<IPeak> peakset, Header header, Data data, final double rtWindow,
			final Random random, final double p1, final double p0, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final double alpha, final int numSamples, final int initialNumClusters, final CorrelationMeasure measure,
			final SortedMap<String,Molecule> molecules, final String[] adducts, final double minDistributionValue,
			final int maxValues, final double massPrecisionPPM) {
		final MixtureClusterer c = new MixtureClusterer(data, rtWindow, random,
			p1, p0, inScorer, outScorer, alpha, numSamples, initialNumClusters, measure, molecules, adducts, minDistributionValue,
			maxValues, massPrecisionPPM);
		final int[] clustering = c.bestSample();
		assert clustering != null;
		
		final Vector<IPeak> peaks = peakset.getPeaks();
		for (IPeak peak : peaks)
			peak.removeAnnotation(IPeak.relationid);
		
		int i = 0;
		for (IPeak peak : peakset) {
			final int id = clustering[i++];
			//System.err.println("peak: " + peak.getAnnotation("id").getValue() + " cluster: " + id);
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

	public MixtureClusterer(final Data data, final double rtWindow, final Random random,
			final double p1, final double p0, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final double alpha, final int numSamples, final int initialNumClusters, final CorrelationMeasure measure,
			final SortedMap<String,Molecule> molecules, final String[] adducts, final double minDistributionValue,
			final int maxValues, final double massPrecisionPPM) {
		assert inScorer != null;
		assert outScorer != null;
		this.p1 = p1;
		this.p0 = p0;
		this.alpha = alpha;
		this.numSamples = numSamples;
		this.initialNumClusters = initialNumClusters;
		this.random = random;
		this.measure = measure;
		this.data = data;
		//this.numReplicates = header.getNrMeasurementInfos();
		//final int numPeaksets = header.getNrPeaks();
		//this.numPeaks = peakset.size();
		this.numFormulae = molecules.size();
		this.adducts = adducts;
		this.minDistributionValue = minDistributionValue;
		this.maxValues = maxValues;
		this.massPrecision = calculateMassPrecision(massPrecisionPPM);
		//System.err.println("Number of replicates: " + numReplicates);
		//System.err.println("Number of peaks: " + numPeaks);

		calculateCorrelationLikelihoods(data, rtWindow, inScorer, outScorer);
		//calculateMassIntensityLikelihoods(header, peakset, rtWindow, inScorer, outScorer, molecules);

		for (int i = 0; i < data.numPeaksets; ++i) {
			order.add(i);
		}
	}
	
	public List<Pair<int[],Double>> sample() {
		final List<Pair<int[],Double>> samples = new ArrayList<Pair<int[],Double>>(numSamples);
		Clustering currentSample = new Clustering(data.numPeaksets, initialNumClusters, random);
		ClusteringScorer scorer = new ClusteringScorer(correlationLikelihood, baseLikelihood, alpha, defaultLikelihood);
		
		for (int i = 0; i < numSamples; ++i) {
			final double currentScore = singleSample(currentSample, scorer);
			final int[] clusterSize = currentSample.getAllClusterSizes();
			System.err.println(Common.arrayToString(clusterSize));
			System.err.println("Score: " + currentScore);
			System.err.println("Number of clusters: " + clusterSize.length);
			samples.add(new Pair<int[],Double>(currentSample.peakClusteringCopy(),currentScore));
		}
		return samples;
	}
	
	public int[] bestSample()  {
		final List<Pair<int[],Double>> samples = sample();
		int[] bestSamp = null;
		double bestScore = Double.NEGATIVE_INFINITY;
		for (Pair<int[],Double> p : samples) {
			if ( p.v2 > bestScore ) {
				bestSamp = p.v1;
				bestScore = p.v2;
			}
		}
		if ( bestSamp == null ) {
			throw new RuntimeException("No clustering was better than NEGATIVE_INFINITY");
		}
		return bestSamp;
	}
	
	public double singleSample(final Clustering currentClustering, final ClusteringScorer scorer) {
		// Clustering must be from 0 to numClusters - 1
		//assert clusteringOK(currentClustering);
		Collections.shuffle(order, random);
		//Collections.shuffle(order);
		
		for (int peak : order) {
			final int oldCluster = currentClustering.getCluster(peak);
			final double[] clusterLogPosterior = scorer.calculatePeakLogPosterior(currentClustering, peak);
			//final double[] clusterLogPosterior = calculatePeakLogPosterior(currentClustering, peak);
			//System.err.println(Common.arrayToString(clusterSize));
			//System.err.println(Common.arrayToString(clusterLogPosterior));
			final double[] clusterPosterior;
			if ( currentClustering.clusterSize(oldCluster) == 1 ) {
				clusterPosterior = normaliseDistribution(clusterLogPosterior, oldCluster);
			} else {
				clusterPosterior = normaliseDistribution(clusterLogPosterior, -1);
			}
			assert clusterPosterior[clusterPosterior.length - 1] <= 1.0f : clusterPosterior[clusterPosterior.length - 1];
			//final double[] cs = cumsum(clusterPosterior);
			final double[] cs = cumsumNormalise(clusterPosterior);
			assert cs[cs.length - 1] == 1.0f;
			final double rn = random.nextDouble();
			int selectedCluster = -1;
			
			//System.err.println(rn);
			//System.err.println(clusterPosterior.length);
			for (int i = 0; i < clusterPosterior.length; ++i) {
				//System.err.println("clp: " + clusterLogPosterior[i]);
				//System.err.println("cp: " + clusterPosterior[i]);
				//System.err.println("cs: " + cs[i]);
				
				if ( rn < cs[i] ) {
					selectedCluster = i;
					break;
				}
			}
			//System.err.println("selectedCluster: " + selectedCluster);
			assert selectedCluster != -1 : "rn: " + rn + " cs: " + Common.arrayToString(cs);
			if ( currentClustering.clusterSize(oldCluster) == 1 ) {
				assert oldCluster != selectedCluster;
			}
			currentClustering.setCluster(peak, selectedCluster);
		}
		final double l = scorer.calculateLikelihood(currentClustering);
		return l;
	}
	
	private double[] normaliseDistribution(double[] distribution, final int pos) {
		final double[] normalisedDistribution = new double[distribution.length];
		final double maxDistribution = max(distribution);
		//System.err.println("max: " + maxDistribution);
		//int offset = 0;
		double innerSum = 0.0f;
		for (int i = 0; i < normalisedDistribution.length; ++i) {
			if ( i == pos ) continue;
			normalisedDistribution[i] = distribution[i];
			assert ! Double.isNaN(normalisedDistribution[i]);
			innerSum += Math.exp(normalisedDistribution[i] - maxDistribution);
			//System.err.println("d: " + distribution[i]);
			//System.err.println("nd: " + normalisedDistribution[i]);
		}
		final double logSumDistribution = maxDistribution + Math.log(innerSum);
		assert ! Double.isNaN(logSumDistribution);
		
		for (int i = 0; i < normalisedDistribution.length; ++i) {
			if ( i == pos ) continue;
			normalisedDistribution[i] = Math.exp(normalisedDistribution[i] - logSumDistribution);
			assert ! Double.isNaN(normalisedDistribution[i]);
		}
		return normalisedDistribution;
	}

	private static int numClusters(int[] clustering) {
		final Set<Integer> set = new HashSet<Integer>();
		for (int i : clustering) set.add(i);
		return set.size();
	}

	private void calculateCorrelationLikelihoods(final Data data,
			final double rtWindow, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer) {
		//final SignalsAndRetentionTimes sart = ShapeCorrelations.getSignals(header, peakset);
		assert inScorer != null;
		assert outScorer != null;
		final double inTerm = inScorer.constantTerm();
		final double outTerm = outScorer.constantTerm();
		
		final double outWindowIn = p1 == 0.0 ? Double.NEGATIVE_INFINITY : Math.log(p1);
		final double outWindowOut = p0 == 0.0 ? Double.NEGATIVE_INFINITY : Math.log(p0);

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
					if ( Math.abs(signal1rt - signal2rt) < rtWindow ) {
						final double correlation;
						if ( signal1 == null || signal2 == null ) {
							correlation = 0.0f;
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
	/*
	private void calculateMassLikelihoods(final Header header, final IPeakSet<IPeak> peakset,
			final double rtWindow, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final SortedMap<String,Molecule> moleculeMap, final double mu0, final double kappa, final double kappa0) {
		//final SignalsAndRetentionTimes sart = ShapeCorrelations.getSignals(header, peakset);
		final double[][] masses = getMasses(header, peakset, true);
		final double[][] intensities = getIntensities(header, peakset);
		
		assert inScorer != null;
		assert outScorer != null;
		final double inTerm = inScorer.constantTerm();
		final double outTerm = outScorer.constantTerm();
		
		final double outWindowIn = p1 == 0.0 ? Double.NEGATIVE_INFINITY : Math.log(p1);
		final double outWindowOut = p0 == 0.0 ? Double.NEGATIVE_INFINITY : Math.log(p0);

		//final FlexCompRowMatrix likelihood = new FlexCompRowMatrix(numFormulae + 1, numPeaks);
		
		final ScoringCache[] scoringCaches = new ScoringCache[numReplicates];
		for (int i = 0; i < scoringCaches.length; ++i) {
			scoringCaches[i] = new ScoringCache(numPeaks, numFormulae, adducts.length * maxValues);
		}
		final double[] baseLikelihood = new double[this.numPeaks];
		final List<Molecule> molecules = new ArrayList<Molecule>(moleculeMap.values());
		
		final double maxLogMass = Common.max(masses, true);
		final double minLogMass = Common.min(masses, true);
		final double uniformLikelihood = 1.0 / (maxLogMass - minLogMass);
		
		final List<GeneralMassSpectrum> massSpectrums = new ArrayList<GeneralMassSpectrum>();
		for (int formulaId = 0; formulaId < numFormulae; ++formulaId) {
			final MolecularFormula formula = molecules.get(formulaId).getFormula();
			final GeneralMassSpectrum ms = GeneralMassSpectrum.getGeneralMassSpectrum(formula, adducts,
					minDistributionValue, maxValues);
			massSpectrums.add(ms);
		}
		
		for (int rep = 0; rep < this.numReplicates; ++rep) {
			for (int peak = 0; peak < numPeaks; ++peak) {
				baseLikelihood[peak] = Math.log(uniformLikelihood) +
					intensityScore(intensities[rep][peak], mu0, 1 / (1/kappa + 1/kappa0));

				for (int formulaId = 0; formulaId < numFormulae; ++formulaId) {
					final GeneralMassSpectrum ms = massSpectrums.get(formulaId);
					for (int i = 0; i < ms.size(); ++i) {
						final double theoreticalMass = ms.getMass(i);
						final double massValue = massScore(masses[rep][peak], theoreticalMass);
						if ( massValue < massCutOff ) {
							continue;
						}
						scoringCaches[rep].set(peak, formulaId, i, massValue);
					}
				}
			}
		}

	}
	*/
	/*
	private double massIntensityScore(final GeneralMassSpectrum ms, final int peak, final double[] masses,
			final double[] intensities) {
		for (int i = 0; i < ms.size(); ++i) {
			final double theoreticalMass = ms.getMass(i);
			final double massValue = massScore(masses[peak], theoreticalMass);
			if ( massValue < massCutOff ) {
				continue;
			}
			final double theoreticalIntensity = ms.getDistribution(i);
			final double intensityValue = intensityScore(intensities, peak, theoreticalIntensity);
		}
	}
	*/
	private double massScore(final double measuredMass, final double theoreticalMass) {
		final double logMeasuredMass = Math.log(measuredMass);
		final double logTheoreticalMass = Math.log(theoreticalMass);
		
		return logNormalDensity(logMeasuredMass, logTheoreticalMass, this.massPrecision);
	}
	/*
	
	private double intensityScore(final double measuredIntensity, final double theoreticalIntensity) {
		final double kappa_k = kappa_0 + kappa * 	
	}
	*/
	private double intensityScore(final double measuredIntensity, final double theoreticalIntensity, final double precision) {
		return logNormalDensity(measuredIntensity, theoreticalIntensity, precision);
	}
	
	private double logNormalDensity(final double x, final double mu, final double precision) {
		return -0.5 * Math.log(2 * Math.PI) + 0.5 * Math.log(precision)
				- 0.5 * precision * Math.pow(x - mu, 2);
	}

	private double max(final double[] array) {
		double m = Double.NEGATIVE_INFINITY;
		for (double v : array) {
			if (v > m) {
				m = v;
			}
		}
		return m;
	}

	private double[] cumsum(final double[] array) {
		final double[] cs = new double[array.length];
		cs[0] = array[0];
		for (int i = 1; i < array.length; ++i) {
			cs[i] = cs[i - 1] + array[i];
		}
		return cs;
	}
	
	private double[] cumsumNormalise(final double[] array) {
		final double[] cs = cumsum(array);
		final double total = cs[cs.length - 1];
		for (int i = 0; i < cs.length - 1; ++i) {
			cs[i] = cs[i] / total;
		}
		cs[cs.length - 1] = 1.0f;
		return cs;
	}
	
	private double[][] getMasses(final Header header, final IPeakSet<IPeak> peakset, final boolean logMass) {
		final Vector<IPeak> peaks = peakset.getPeaks();
		final int numReplicates = header.getNrMeasurementInfos();
		//final int numPeaksets = header.getNrPeaks();
		final int numPeaksets = peakset.size();
		
		final double[][] masses = new double[numReplicates][numPeaksets];
		
		for (int j = 0; j < numPeaksets; ++j) {
			final IPeak peak1 = peaks.get(j);
			if ( peakset.getContainerClass().equals(MassChromatogram.class) ) {
				if ( logMass ) {
					masses[0][j] = Math.log(peak1.getMass());
				} else {
					masses[0][j] = peak1.getMass();
				}
			} else {
				for (int i = 0; i < numReplicates; ++i) {
					IPeakSet<MassChromatogram<Peak>> peakset1 = (IPeakSet<MassChromatogram<Peak>>) peak1;
					IPeak mc1 = ShapeCorrelations.getPeak(peakset1, header, i);
					if ( logMass ) {
						masses[i][j] = Math.log(peak1.getMass());
					} else {
						masses[i][j] = mc1.getMass();
					}
				}
			}
		}
		return masses;
	}
	
	private double[][] getIntensities(final Header header, final IPeakSet<IPeak> peakset) {
		final Vector<IPeak> peaks = peakset.getPeaks();
		final int numReplicates = header.getNrMeasurementInfos();
		//final int numPeaksets = header.getNrPeaks();
		final int numPeaksets = peakset.size();
		
		final double[][] intensities = new double[numReplicates][numPeaksets];
		
		for (int j = 0; j < numPeaksets; ++j) {
			final IPeak peak1 = peaks.get(j);
			if ( peakset.getContainerClass().equals(MassChromatogram.class) ) {
				intensities[0][j] = peak1.getIntensity();
			} else {
				for (int i = 0; i < numReplicates; ++i) {
					IPeakSet<MassChromatogram<Peak>> peakset1 = (IPeakSet<MassChromatogram<Peak>>) peak1;
					IPeak mc1 = ShapeCorrelations.getPeak(peakset1, header, i);
					intensities[i][j] = mc1.getIntensity();
				}
			}
		}
		return intensities;
	}
	
	private double calculateMassPrecision(final double massPrecisionPPM) {
		final double onePPM = Math.log(1000001) - Math.log(1000000);
		final double deviation = onePPM * massPrecisionPPM;
		final double standardDeviation = deviation;
		final double precision = 1.0 / (standardDeviation * standardDeviation);
		return precision;
	}
	
	private static class Clustering {
		private final int[] peakClustering;
		private final List<Set<Integer>> clusterPeaks = new ArrayList<Set<Integer>>();
		//private final List<Integer> allClusterSizes;
		private int[] allClusterSizes;
		
		private Clustering(final int numPeaks, final int initialNumClusters, final Random random) {
			peakClustering = new int[numPeaks];
			for (int i = 0; i < numPeaks; ++i) {
				peakClustering[i] = random.nextInt(initialNumClusters);
			}
			initialiseClusterPeaks();
			//allClusterSizes = new ArrayList<Integer>();
			//for (int i = 0; i < initialNumClusters; ++i) {
			//	allClusterSizes.add(clusterSize(i));
			//}
			generateAllClusterSizes();
			//allClusterSizesArray = allClusterSizes.toArray(allClusterSizesArray);
		}
		
		private void initialiseClusterPeaks() {
			final int K = numClusters(peakClustering);
			for (int i = 0; i < K; ++i) {
				clusterPeaks.add(new HashSet<Integer>());
			}
			
			for (int i = 0; i < peakClustering.length; ++i) {
				clusterPeaks.get(peakClustering[i]).add(i);
			}
		}
		
		private int getCluster(final int peak) {
			return peakClustering[peak];
		}
		
		private void setCluster(final int peak, final int cluster) {
			assert clusteringOK();
			final int oldCluster = peakClustering[peak];
			
			//System.err.println("Peak: " + peak + " Cluster: " + cluster + " oldCluster: " + oldCluster + " oldSize: " + clusterSize(oldCluster));
			//System.err.println(numberOfClusters());
			if ( cluster == oldCluster ) {
				// Do nothing
			} else if ( cluster == numberOfClusters() && clusterSize(oldCluster) == 1 ) {
				// Do nothing
			} else  {
				if ( cluster == numberOfClusters() ) {
					clusterPeaks.add(new HashSet<Integer>());
				}
				peakClustering[peak] = cluster;
				clusterPeaks.get(oldCluster).remove(peak);
				clusterPeaks.get(cluster).add(peak);

				if ( clusterSize(oldCluster) == 0 ) {
					for (int i = 0; i < peakClustering.length; ++i) {
						if ( peakClustering[i] > oldCluster ) {
							peakClustering[i]--;
						}
					}
					clusterPeaks.remove(oldCluster);
				}
				generateAllClusterSizes();
			}
			assert clusteringOK() : "Peak: " + peak + " Cluster: " + cluster + " size: " + clusterSize(cluster) + " oldSize: " + clusterSize(oldCluster);
		}

		
		private int clusterSize(final int cluster) {
			return clusterPeaks.get(cluster).size();
		}
		
		private int numberOfClusters() {
			return clusterPeaks.size();
		}
		
		private void generateAllClusterSizes() {
			allClusterSizes = new int[numberOfClusters()];
			for (int i = 0; i < allClusterSizes.length; ++i) {
				allClusterSizes[i] = clusterSize(i);
			}
		}
		
		private int[] getAllClusterSizes() {
			return allClusterSizes;
		}
		
		private int[] peakClusteringCopy() {
			return peakClustering.clone();
		}
		
		private int[] getPeakClustering() {
			return peakClustering;
		}
		private boolean clusteringOK() {
			final int K = numberOfClusters();
			if ( K != numClusters(peakClustering) ) {
				return false;
			}
			for (int c : peakClustering) {
				if ( c < 0 || c >= K ) {
					return false;
				}
			}
			for (int i = 0; i < K; ++i) {
				final Set<Integer> c = clusterPeaks.get(i);
				for (int peak : c) {
					if ( peakClustering[peak] != i ) {
						return false;
					}
				}
			}
			int size = 0;
			for (int i = 0; i < K; ++i) {
				size += clusterPeaks.get(i).size();
			}
			if ( size != peakClustering.length ) {
				return false;
			}
			
			return true;
		}
	}
	
	private static class ClusteringScorer {
		private final FlexCompRowMatrix correlationLikelihood;
		private final double[] baseLikelihood;
		private final int numPeaks;
		private final double alpha;
		private final double defaultLikelihood;
		
		private ClusteringScorer(final FlexCompRowMatrix likelihood, final double[] baseLikelihood, final double alpha,
				final double defaultLikelihood) {
			this.correlationLikelihood = likelihood;
			this.baseLikelihood = baseLikelihood;
			this.numPeaks = baseLikelihood.length;
			this.alpha = alpha;
			this.defaultLikelihood = defaultLikelihood;
		}
		
		private double[] calculatePeakLikelihood(final Clustering currentClustering, final int peak) {
			final int K = currentClustering.numberOfClusters();
			final double[] clusterLikelihood = new double[K + 1];
			Arrays.fill(clusterLikelihood, baseLikelihood[peak]);
			
			final int[] clusterSizes = currentClustering.getAllClusterSizes().clone();
			final double[] like = correlationLikelihood.getRow(peak).getData();
			final int[] indices = correlationLikelihood.getRow(peak).getIndex();

			final int[] c = currentClustering.getPeakClustering();
			for (int i = 0; i < indices.length; ++i) {
				final int cluster = c[indices[i]];
				clusterLikelihood[cluster] += like[i];
				clusterSizes[cluster]--;
			}
			clusterSizes[c[peak]]--;
			
			for (int cluster = 0; cluster < clusterSizes.length; ++cluster) {
				clusterLikelihood[cluster] += clusterSizes[cluster] * this.defaultLikelihood;
			}
			return clusterLikelihood;
		}
		
		private double[] calculatePeakLogPosterior(final Clustering currentClustering, int peak) {
			final int K = currentClustering.numberOfClusters();
			final double[] clusterLikelihood = calculatePeakLikelihood(currentClustering, peak);
			final int[] clusterSizes = currentClustering.getAllClusterSizes();
			for (int i = 0; i < K; ++i) {
				assert clusterSizes[i] != 0;
				clusterLikelihood[i] +=  Math.log(clusterSizes[i]);
				assert ! Double.isNaN(clusterLikelihood[i]);
			}
			clusterLikelihood[K] += Math.log(alpha);
			return clusterLikelihood;
		}
		
		private double calculateLikelihood(final Clustering currentClustering) {
			double totalLikelihood = 0.0f;

			for (int i = 0; i < numPeaks; ++i) {
				final double[] like = calculatePeakLikelihood(currentClustering, i);
				for (int j = 0; j < like.length - 1; ++j) {
					totalLikelihood += like[j];
				}
			}
			return totalLikelihood;
		}
	}
	
	public interface LikelihoodScorer {
		double constantTerm();
		double correlationTerm(double correlation);
	}
	
	//TODO REMOVE ME
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
	
	//TODO REMOVE ME
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
		
		public double get(final int peak, final int cluster, final int clusterPeak) {
			return cache[peak].get(cluster, clusterPeak);
		}
		
		public void set(final int peak, final int cluster, final int clusterPeak, final double value) {
			cache[peak].set(cluster, clusterPeak, value);
		}
	}
}
