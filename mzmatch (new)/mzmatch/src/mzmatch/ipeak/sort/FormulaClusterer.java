package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import no.uib.cipr.matrix.sparse.SparseVector;
import peakml.chemistry.Polarity;
import peakml.util.Pair;
import cern.colt.function.IntIntDoubleFunction;
import cern.colt.list.DoubleArrayList;
import cern.colt.list.IntArrayList;
import cern.colt.matrix.DoubleMatrix1D;
import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.DoubleMatrix3D;
import cern.colt.matrix.impl.SparseDoubleMatrix3D;

import com.google.common.base.Joiner;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public class FormulaClusterer extends Clusterer<MoleculeData,MoleculeClustering> {
	protected static double massCutOff = 1e-100; // The likelihood, below which we remove the value.
	protected static double logMassCutOff = Math.log(massCutOff);
	// If the ppm difference is greater than the value given, then the likelihood is less than the massCutOff
	protected static double ppmCutOff = 100 * Common.onePPM;
	protected static double peakBaseRatio = 100;
	
	protected final MoleculeParameters parameters;
	private final LikelihoodScorer inScorer;
	private final LikelihoodScorer outScorer;
	private final CorrelationMeasure measure;

	private final String[] adducts;
	protected final MassIntensityClusteringScorer miScorer;

	public FormulaClusterer(final MoleculeData data, final MoleculeParameters parameters, final Random random,
			final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, final PeakPosteriorScorer<MoleculeClustering> scorer,
			MassIntensityClusteringScorer miScorer, final boolean verbose) {
		super(parameters.numSamples, parameters.burnIn, data, parameters, random, scorer, verbose);
		this.parameters = parameters;

		this.inScorer = inScorer;
		this.outScorer = outScorer;
		this.measure = measure;
		this.adducts = adducts;
		this.miScorer = miScorer;
	}

	@Override
	public MoleculeClustering createClustering() {
		return MoleculeClustering.createMoleculeClustering(random, data, parameters, inScorer, outScorer,
				measure, adducts, miScorer);
	}
	
	@Override
	public double singleSample(final MoleculeClustering currentClustering) {
		super.singleSample(currentClustering);
		return scorer.calculateLikelihood(currentClustering);
	}

	public static class MassIntensityClusteringScorer implements PeakLikelihoodScorer<MoleculeClustering> {
		private final MoleculeParameters parameters;
		private final String[] adducts;
		private final double uniformLikelihood;
		private final double baseLikelihood;
		private final int maxPositions;
		private final int defaultPosition;
		
		private final double mu0;
		private final double kappa0;
		
		private final double kappa;
		private final MoleculeData data;
		private final double massPrecision;
		
		final ScoringCache[] scoringCaches;
		final double[] newClusterMassIntensityLikelihood;
		protected final FlexCompRowMatrix flatml;
		final FlexCompRowMatrix[] flatmlRep;
		final ScoringCache massCache;
		final Random random;
		final double[][] beta_mai;
		final double[][] background;
		final double[][] expBackground;
		final double[] backgroundSum;
		
		public MassIntensityClusteringScorer(final MoleculeData data, final MoleculeParameters parameters, final LikelihoodScorer inScorer,
				final LikelihoodScorer outScorer, final CorrelationMeasure measure,
				final String[] adducts, final Random random) {
			this.parameters = parameters;
			this.data = data;
			if ( parameters.debug ) System.err.println(data.numReplicates + " replicates and " + data.numPeaksets + " peaks in sample");
			this.adducts = adducts;
			final double overallRatio = peakBaseRatio * data.numMolecules;
			this.massPrecision = calculateMassPrecision(parameters.massPrecisionPPM);
			final double modeMassLikelihood = Math.sqrt(massPrecision/(2 * Math.PI));
			this.uniformLikelihood = modeMassLikelihood / overallRatio;
			this.baseLikelihood = Math.log(uniformLikelihood);
			this.maxPositions = (adducts.length * parameters.maxValues) + 1;
			this.defaultPosition = maxPositions - 1;
			final double[] flatIntensities = Doubles.concat(data.intensities);
			this.mu0 = Common.mean(flatIntensities, true);
			//System.err.println("Intensity prior mean (mu0): " + mu0);
			this.kappa0 = parameters.options.intensityKappa0;
			this.kappa = parameters.options.intensityKappa;
			
			this.scoringCaches = new ScoringCache[data.numReplicates];
			this.newClusterMassIntensityLikelihood = new double[data.numPeaksets];
			this.flatml = new FlexCompRowMatrix(data.numPeaksets, data.numMolecules);
			flatmlRep = new FlexCompRowMatrix[data.numReplicates];
			for ( int rep = 0; rep < data.numReplicates; ++rep ) {
				flatmlRep[rep] = new FlexCompRowMatrix(data.numPeaksets, data.numMolecules);
			}
			
			this.massCache = new ScoringCache(data.numPeaksets, data.numMolecules, maxPositions, defaultPosition,
					baseLikelihood);
			this.random = random;
			this.beta_mai = new double[data.numMolecules][];
			this.background = new double[data.numReplicates][data.numPeaksets];
			this.expBackground = new double[data.numReplicates][data.numPeaksets];
			this.backgroundSum = new double[data.numPeaksets];

			calculateMassLikelihoods(data, mu0, kappa, kappa0);
			calculateBeta();
		}

		public double[] calculatePeakLikelihood(final MoleculeClustering currentClustering, final int peak) {
			final int K = currentClustering.numberOfClusters();
			final double[] peakLikelihood = new double[K + 1];
			assert peakLikelihood.length == currentClustering.numberOfClusters() + 1;

			for (int rep = 0; rep < data.numReplicates; ++rep) {
				final double measuredIntensity = data.intensities[rep][peak];
				if ( Double.isNaN(measuredIntensity) ) {
					continue;
				}
				double[] likelihoodSum = new double[peakLikelihood.length - 1];
				Arrays.fill(likelihoodSum, expBackground[rep][peak]);
				final ScoringCache.NonZeros nz = scoringCaches[rep].getNonZeros(peak);
				final int numValues = nz.formulaList.size();
		
				for ( int i = 0; i < numValues; ++i ) {
					final int molecule = nz.formulaList.get(i);
					final int position = nz.positionList.get(i);
					final double value = nz.valueList.get(i);
					final List<Integer> clusters = currentClustering.getClustersFromMolecule(molecule);

					for ( int k : clusters ) {
						final List<Integer> clusterPeaks = currentClustering.getClusterPeaks(k);
						final List<Integer> peakPositions = currentClustering.getClusterPositions(k);

						final double clusterPeakIntensityLike = calculateIntensityLikelihood(molecule, rep, peak, position,
								clusterPeaks, peakPositions);
						likelihoodSum[k] += Math.exp(value + clusterPeakIntensityLike);
						assert ! Double.isNaN(likelihoodSum[k]) && ! Double.isInfinite(likelihoodSum[k]) && likelihoodSum[k] >= 0.0 :
							"likelihoodSum[k]: " + likelihoodSum[k];
					}
				}
				assert ! Double.isNaN(newClusterMassIntensityLikelihood[peak]);
				for ( int k = 0; k < peakLikelihood.length - 1; ++k ) {
					final int formula = currentClustering.getMolecule(k);
					peakLikelihood[k] += Math.log(likelihoodSum[k]) - Math.log(numberLikelihoodComponents(formula));
				}
			}
			peakLikelihood[peakLikelihood.length - 1] += newClusterMassIntensityLikelihood[peak];
			return peakLikelihood;
		}
		
		public String likelihoods(final MoleculeClustering clustering, final int peak, final int cluster) {
			final List<String> outLikes = new ArrayList<String>();
			if ( cluster == clustering.numberOfClusters() ) {
				final String like = String.format("%.1f", newClusterMassIntensityLikelihood[peak]);
				return like;
			}
			
			for ( int rep = 0; rep < data.numReplicates; ++rep ) {
				final List<String> likes = new ArrayList<String>();
				likes.add(String.format("%.1f", Math.log(expBackground[rep][peak])));
				final ScoringCache.NonZeros nz = scoringCaches[rep].getNonZeros(peak);
				final int numValues = nz.formulaList.size();

				for ( int i = 0; i < numValues; ++i ) {
					final int molecule = nz.formulaList.get(i);
					final int position = nz.positionList.get(i);
					final double value = nz.valueList.get(i);
					final List<Integer> clusters = clustering.getClustersFromMolecule(molecule);
	
					for ( int k : clusters ) {
						if ( k == cluster ) {
							final List<Integer> clusterPeaks = clustering.getClusterPeaks(k);
							final List<Integer> peakPositions = clustering.getClusterPositions(k);
							final double clusterPeakIntensityLike = calculateIntensityLikelihood(molecule, rep, peak, position,
									clusterPeaks, peakPositions);
							final String like = String.format("%.1f/%.1f", value, clusterPeakIntensityLike);
							likes.add(like);
						}
					}
				}
				outLikes.add(Joiner.on(":").join(likes));
			}
			return Joiner.on(" ").join(outLikes);
		}

		private double calculateKappaK(final int peak, final List<Integer> group, final List<Integer> groupPositions,
				final int formula, final int adduct, final int rep) {
			assert group.size() == groupPositions.size();
			double retval = 0.0;
			final GeneralMassSpectrum spectrum = data.theoreticalSpectrums.get(formula);
			for ( int i = 0; i < group.size(); ++i) {
				final int groupPeak = group.get(i);
				if ( groupPeak == peak || Double.isNaN(data.intensities[rep][groupPeak]) ) continue;
				final int groupPeakPosition = groupPositions.get(i);
				if ( spectrum.hasValue(groupPeakPosition) && spectrum.getAdduct(groupPeakPosition) == adduct ) {
					final double beta = beta_mai[formula][groupPeakPosition];
					retval += beta * beta;
				}
			}
			retval = kappa0 + kappa * retval;
			return retval;
		}
		
		private double calculateMuK(final int peak, final List<Integer> group, final List<Integer> groupPositions,
				final int formula, final double kappaK, final int rep, final int adduct) {
			assert group.size() == groupPositions.size();
			double retval = 0.0;
			final GeneralMassSpectrum spectrum = data.theoreticalSpectrums.get(formula);
			for (int i = 0; i < group.size(); ++i) {
				final int groupPeak = group.get(i);
				//if ( groupPeak == peak || Double.isNaN(data.intensities[rep][i]) ) continue;
				if ( groupPeak == peak || Double.isNaN(data.intensities[rep][groupPeak]) ) continue;
				final int groupPeakPosition = groupPositions.get(i);
				if ( spectrum.hasValue(groupPeakPosition) && spectrum.getAdduct(groupPeakPosition) == adduct ) {
					final double beta = beta_mai[formula][groupPeakPosition];
					assert ! Double.isNaN(beta);
					retval += data.intensities[rep][groupPeak] * beta;
					assert ! Double.isNaN(retval);
				}
			}
			retval = (kappa0 * mu0 + kappa * retval) / kappaK;
			return retval;
		}
		
		public void doClusterPositionStep(final MoleculeClustering currentClustering) {
			for ( int cluster = 0; cluster < currentClustering.numberOfClusters(); ++cluster ) {
				final List<Integer> clusterPeaks = new ArrayList<Integer>(currentClustering.getClusterPeaks(cluster));
				Collections.shuffle(clusterPeaks, random);
				final Pair<List<List<Integer>>,double[]> samples = generateFormulaeSamples(currentClustering, cluster, clusterPeaks);

				
				final Sample<Integer> selected = sampleDistribution(samples.v2);
				final int selectedFormula = selected.v1;
				final List<Integer> selectedPositions = samples.v1.get(selectedFormula);
				currentClustering.setMolecule(cluster, selectedFormula, clusterPeaks, selectedPositions);
			}
			
		}
		
		public Pair<List<List<Integer>>,double[]> generateFormulaeSamples(final MoleculeClustering currentClustering,
				final int cluster, final List<Integer> clusterPeaks) {
			final List<List<Integer>> formulaSamples = new ArrayList<List<Integer>>();
			final List<List<Integer>> formulaSamplePositions = new ArrayList<List<Integer>>();
			final double[] sampleProbabilities = new double[currentClustering.numberOfMolecules()];
			for ( int formula = 0; formula < currentClustering.numberOfMolecules(); ++formula ) {
				formulaSamples.add(new ArrayList<Integer>());
				formulaSamplePositions.add(new ArrayList<Integer>());
			}
			final int clusterSize = currentClustering.clusterSize(cluster);

			for ( int i = 0; i < clusterSize; ++i ) {
				final int peak = clusterPeaks.get(i);
				final List<SparseVector> distributions = new ArrayList<SparseVector>();
				for ( int j = 0; j < currentClustering.numberOfMolecules(); ++j ) {
					distributions.add(null);
				}
				for (int rep = 0; rep < data.numReplicates; ++rep) {
					final double measuredIntensity = data.intensities[rep][peak];
					if ( Double.isNaN(measuredIntensity) ) {
						continue;
					}
					final ScoringCache.NonZeros nz = scoringCaches[rep].getNonZeros(peak);
					final int numValues = nz.formulaList.size();
			
					for ( int j = 0; j < numValues; ++j ) {
						final int formula = nz.formulaList.get(j);
						final int position = nz.positionList.get(j);
						final double value = nz.valueList.get(j);
						
						SparseVector dist = distributions.get(formula);
						if ( dist == null ) {
							dist = new SparseVector(this.maxPositions);
							distributions.set(formula, dist);
						}
						final List<Integer> currentSample = formulaSamples.get(formula);
						final List<Integer> samplePositions = formulaSamplePositions.get(formula);
						final double clusterPeakIntensityLike = calculateIntensityLikelihood(formula, rep, peak, position,
								currentSample, samplePositions);
						dist.add(position, value + clusterPeakIntensityLike);
					}
				}
				for ( int formula = 0; formula < currentClustering.numberOfMolecules(); ++formula ) {
					final List<Integer> currentSample = formulaSamples.get(formula);
					final List<Integer> samplePositions = formulaSamplePositions.get(formula);
					final SparseVector dist = distributions.get(formula);

					currentSample.add(peak);
					if ( dist == null ) {
						samplePositions.add(defaultPosition);
						sampleProbabilities[formula] += backgroundSum[peak];
					} else {
						dist.set(defaultPosition, backgroundSum[peak]);
						final Sample<Integer> sample = sampleDistribution(dist);
						final int position = sample.v1;
						samplePositions.add(position);
						sampleProbabilities[formula] += sample.v2;
					}
				}
			}
			
			return new Pair<List<List<Integer>>,double[]>(formulaSamplePositions, sampleProbabilities);
		}
		
		private SparseVector combineLikelihoods(final List<SparseVector> likelihoodReps) {
			if ( likelihoodReps.size() == 0 ) {
				return null;
			}
			final SparseVector retval = likelihoodReps.get(0).copy();
			for ( int i = 1; i < likelihoodReps.size(); ++i) {
				final SparseVector likelihood = likelihoodReps.get(i);
				final double[] like = likelihood.getData();
				final int[] indices = likelihood.getIndex();
				for ( int j = 0; j < indices.length; ++j ) {
					final double ll = like[j];
					final int index = indices[j];
					if ( retval.get(index) == 0.0 ) {
						continue;
					}
					retval.add(index, ll);
				}
				final int[] retIndices = likelihood.getIndex();
				for ( int j = 0; j < retIndices.length; ++j ) {
					final int index = retIndices[j];
					if ( likelihood.get(index) == 0.0 ) {
						retval.set(index, 0.0);
					}
				}
			}
			retval.compact();
			return retval;
		}
		
		public double[][] generateFormulaeProposals(final MoleculeClustering currentClustering) {
			final int numClusters = currentClustering.numberOfClusters();
			final double[][] proposalDistributions = new double[numClusters][data.numMolecules];
			
			for ( int peak = 0; peak < data.numPeaksets; ++peak ) {
				final int cluster = currentClustering.getCluster(peak);
				final double[] dist = proposalDistributions[cluster];
				final SparseVector v = flatml.getRow(peak);
				for ( int i = 0; i < v.getUsed(); ++i ) {
					final int distIndex = v.getIndex()[i];
					final double value = v.getData()[i];
					dist[distIndex] += value;
				}
			}
			return proposalDistributions;
		}
		
		public Sample<Integer>[] pickFormulae(final double[][] proposalDistributions) {
			final int numClusters = proposalDistributions.length;

			@SuppressWarnings("unchecked")
			final Sample<Integer>[] samples = (Sample<Integer>[])Array.newInstance(Sample.class, numClusters);
			for ( int cluster = 0; cluster < numClusters; ++cluster) {
				final double[] dist = proposalDistributions[cluster];
				final double[] normalisedDistribution = Clusterer.normaliseDistribution(dist, -1);
				final int selectedFormula = Clusterer.samplePosition(normalisedDistribution, random);
				samples[cluster] = new Sample<Integer>(selectedFormula, dist[selectedFormula]);
			}
			return samples;
		}
		
		private Sample<Integer>[] pickPositions(final MoleculeClustering currentClustering, final Sample<Integer>[] formulae) {
			@SuppressWarnings("unchecked")
			final Sample<Integer>[] positionSamples = (Sample<Integer>[])Array.newInstance(Sample.class, data.numPeaksets);
			for ( int peak = 0; peak < data.numPeaksets; ++peak ) {
				final int cluster = currentClustering.getCluster(peak);
				final Sample<Integer> formulaSample = formulae[cluster];
				final int formula = formulaSample.sample();
				final Sample<Integer> positionSample = samplePosition(peak, formula);
				positionSamples[peak] = positionSample;
			}
			return positionSamples;
		}
		
		public Sample<Integer> sampleDistribution(final double[] dist) {
			final double[] normalisedDistribution = Clusterer.normaliseDistribution(dist, -1);
			final int pos = Clusterer.samplePosition(normalisedDistribution, random);
			return new Sample<Integer>(pos, dist[pos]);
		}
		
		public Sample<Integer> sampleDistribution(final SparseVector dist) {
			final double[] formulaDistribution = toCompactArray(dist);
			final Sample<Integer> sampleOffset = sampleDistribution(formulaDistribution);
			final int selectedOffset = sampleOffset.v1;
			final int selectedFormula = dist.getIndex()[selectedOffset];
			final double selectedValue = sampleOffset.v2;
			return new Sample<Integer>(selectedFormula, selectedValue);
		}
		
		public int pickFormula(final int peak) {
			final SparseVector v = flatml.getRow(peak);
			final Sample<Integer> sample = sampleDistribution(v);
			return sample.v1;
		}
		
		public Sample<Integer> pickPeak(final int peak, final MoleculeClustering currentClustering) {
			final int molecule = currentClustering.getPeakMolecule(peak);
			final Sample<Integer> sample = samplePosition(peak, molecule);
			assert sample.sample() >= 0 && sample.sample() < maxPositions;
			return sample;
		}
		
		public Sample<Integer> samplePosition(final int peak, final int molecule) {
			final DoubleMatrix1D dist = massCache.getRow(peak, molecule);
			final SparseVector v = ScoringCache.toSparseVector(dist);
			v.set(defaultPosition, baseLikelihood);
			return sampleDistribution(v);
		}

		private static double[] toCompactArray(final SparseVector v) {
			final int size = v.getUsed();
			assert size == v.getIndex().length : "size: " + size + " indices.length: " + v.getIndex().length;
			final double[] data = v.getData();
			final double[] retval = new double[size];
			System.arraycopy(data, 0, retval, 0, size);
			return retval;
		}

		public double calculateIntensityLikelihood(final int formula, final int rep, final int peak, final int position,
				final List<Integer> clusterPeaks, final List<Integer> peakPositions) {
			assert formula >=0 && formula < beta_mai.length;
			final double measuredIntensity = data.intensities[rep][peak];
			if ( Double.isNaN(measuredIntensity)  ) {
				return 0.0;
			}

			assert position >=0 && position < beta_mai[formula].length : "position: " + position + " beta_mai[formula].length: " +
				beta_mai[formula].length + " formula: " + formula + " size: " + data.theoreticalSpectrums.get(formula).size();
			if ( position == defaultPosition ) {
				return intensityScore(measuredIntensity, mu0, 1 / (1/kappa + 1/kappa0), true);
			}
			final int adduct = data.theoreticalSpectrums.get(formula).getAdduct(position);
			final double kappaK = calculateKappaK(peak, clusterPeaks, peakPositions, formula, adduct, rep);
			final double muK = calculateMuK(peak, clusterPeaks, peakPositions, formula, kappaK, rep, adduct);
			
			
			final double beta_kai = beta_mai[formula][position];
			final double mu_w = beta_kai * muK;
			final double kappa_w = 1 / (1/kappa + beta_kai * beta_kai / kappaK);		
			final double score = intensityScore(measuredIntensity, mu_w, kappa_w, true);
			
			return score;
		}
		
		public double calculateMassLikelihood(final int formula, final int peak, final int position) {
			if ( position == defaultPosition ) {
				return baseLikelihood;
			}
			final double value = massCache.get(peak, formula, position);
			if ( value == 0.0 ) {
				return -Double.MAX_VALUE;
			}
			return value;
		}
		
		public double calculateMassLikelihood(final int formula, final int rep, final int peak, final int position) {
			if ( Double.isNaN(data.masses[rep][peak]) ) {
				return 0.0;
			}
			if ( position == defaultPosition ) {
				return baseLikelihood;
			}
			final double value = scoringCaches[rep].get(peak, formula, position);
			if ( value == 0.0 ) {
				return -Double.MAX_VALUE;
			}
			return value;
		}
		
		public double calculateLikelihood(final AbstractClustering currentClustering) {
			// TODO
			return 0.0;
		}
		
		public int getDefaultPosition() {
			return defaultPosition;
		}
		
		private void calculateMassLikelihoods(final MoleculeData data,
				final double mu0,
				final double kappa, final double kappa0) {

			for (int rep = 0; rep < data.numReplicates; ++rep) {
				scoringCaches[rep] = new ScoringCache(data.numPeaksets, data.numMolecules,
						maxPositions, defaultPosition, baseLikelihood);
				for (int peak = 0; peak < data.numPeaksets; ++peak) {
					final double priorIntensityScore = intensityScore(data.intensities[rep][peak], mu0, 1 / (1/kappa + 1/kappa0), true);
					if ( Double.isNaN(priorIntensityScore) ) {
						// if the intensity is NaN, then the mass is NaN as well
						continue;
					}
					background[rep][peak] = priorIntensityScore + baseLikelihood;
					expBackground[rep][peak] = Math.exp(background[rep][peak]);
					assert ! Double.isNaN(expBackground[rep][peak]) && ! Double.isInfinite(expBackground[rep][peak]) && expBackground[rep][peak] >= 0.0 :
						"expBackground[rep][peak]: " + expBackground[rep][peak];
					backgroundSum[peak] += priorIntensityScore + baseLikelihood;
					assert ! Double.isNaN(baseLikelihood);
					double runningTotal = 0.0;
					for (int formulaId = 0; formulaId < data.numMolecules; ++formulaId) {
						final GeneralMassSpectrum ms = data.theoreticalSpectrums.get(formulaId);
						double subTotal = 0.0;

						for (int i = 0; i < ms.size(); ++i) {
							final Polarity theoreticalPolarity = ms.getPolarity(i);
							final Polarity measuredPolarity = data.polarities[peak];
							if ( theoreticalPolarity != measuredPolarity ) {
								continue;
							}
							final double theoreticalMass = ms.getMass(i);
							final double logTheoreticalMass = Math.log(theoreticalMass);
							if ( Math.abs(data.masses[rep][peak] - logTheoreticalMass) > ppmCutOff ) {
								continue;
							}
							
							final double massValue = massScore(data.masses[rep][peak], logTheoreticalMass, true);
							assert ! Double.isNaN(massValue);
							
							if ( massValue < logMassCutOff ) {
								continue;
							}

							scoringCaches[rep].set(peak, formulaId, i, massValue);
							massCache.add(peak, formulaId, i, massValue);
							subTotal += Math.exp(massValue);
						}
						subTotal += Math.exp(baseLikelihood);// + this.defaultLikelihood[rep][formulaId];
						
						final double averageOverTheoreticalPeaks = subTotal / numberLikelihoodComponents(formulaId);
						flatml.add(peak, formulaId, Math.log(averageOverTheoreticalPeaks));
						flatmlRep[rep].set(peak, formulaId, Math.log(averageOverTheoreticalPeaks));
						runningTotal += averageOverTheoreticalPeaks;
					}
					assert ! Double.isNaN(priorIntensityScore);
					assert runningTotal > 0.0;
					assert data.numMolecules != 0;
					final double value = Math.log(runningTotal / (double)data.numMolecules);
					assert ! Double.isNaN(value) : "runningTotal: " + runningTotal + " numFormulae: " + data.numMolecules;
					assert ! Double.isNaN(newClusterMassIntensityLikelihood[peak]);
					
					newClusterMassIntensityLikelihood[peak] += priorIntensityScore + value;
					assert ! Double.isNaN(newClusterMassIntensityLikelihood[peak]);

				}
			}
		}
		
		private void calculateBeta() {
			for (int formula = 0; formula < data.numMolecules; ++formula) {
				final List<Double> dist = data.theoreticalSpectrums.get(formula).getDistribution();
				beta_mai[formula] = new double[maxPositions];
				for (int i = 0; i < dist.size(); ++i) {
					beta_mai[formula][i] = dist.get(i);
				}
			}
		}

		private double massScore(final double logMeasuredMass, final double logTheoreticalMass, final boolean logged) {
			return Common.normalDensity(logMeasuredMass, logTheoreticalMass, this.massPrecision, logged);
		}

		private double intensityScore(final double measuredIntensity, final double theoreticalIntensity, final double precision,
				final boolean logged) {
			return Common.normalDensity(measuredIntensity, theoreticalIntensity, precision, logged);
		}
		
		private int numberLikelihoodComponents(final int formula) {
			return data.theoreticalSpectrums.get(formula).size() + 1;
		}
	}
	
	public static class NewScoringCache {
		private final Map<Integer,SparseVector>[] peakFormulaPositionMap;
		private final SparseVector defaultRetval;
		private final double defaultValue;
		private final int maxPositions;
		private final int defaultPosition;
		
		@SuppressWarnings("unchecked")
		public NewScoringCache(final int numPeaks, final int numFormulae, final int maxPositions, final int defaultPosition,
				final double defaultValue) {
			peakFormulaPositionMap = (Map<Integer,SparseVector>[])Array.newInstance(HashMap.class, numPeaks);
			for ( int i = 0; i < numPeaks; ++i ) {
				peakFormulaPositionMap[i] = new HashMap<Integer,SparseVector>();
			}
			defaultRetval = new SparseVector(maxPositions, new int[]{ defaultPosition }, new double[]{ defaultValue });
			this.maxPositions = maxPositions;
			this.defaultValue = defaultValue;
			this.defaultPosition = defaultPosition;
		}
		
		public SparseVector getPositions(final int peak, final int formula) {
			final Map<Integer,SparseVector> formulaPositionMap = peakFormulaPositionMap[peak];
			final SparseVector retval = formulaPositionMap.get(formula);
			if ( retval == null ) {
				return defaultRetval;
			}
			return retval;
		}
		
		public double get(final int peak, final int formula, final int position) {
			final SparseVector values = getPositions(peak, formula);
			return values.get(position);
		}
		
		public void set(final int peak, final int formula, final int[] positions, final double[] values) {
			set(peak, formula, new SparseVector(maxPositions, positions, values));
		}
		
		public void set(final int peak, final int formula, final SparseVector values) {
			if ( values.getUsed() == 0 ) {
				// Just the default value, so do nothing
				return;
			}
			if ( values.get(defaultPosition) != 0.0 ) {
				if ( values.getUsed() == 1 ) {
					// The only value is the default value, so return
					return;
				}
			} else {
				values.set(defaultPosition, defaultValue);
			}
			assert values.getUsed() > 1 : values;
			peakFormulaPositionMap[peak].put(formula, values);
		}
		
		public void set(final int peak, final int formula, final List<Integer> positions, final List<Double> values) {
			final int[] positionsArray = Ints.toArray(positions);
			final double[] valuesArray = Doubles.toArray(values);
			set(peak, formula, positionsArray, valuesArray);
		}
	}

	public static class ScoringCache {
		private final DoubleMatrix3D cache;
		private boolean cached[];
		private NonZeros[] nz;
		private final int defaultPosition;
		private final double defaultValue;
				
		public ScoringCache(final int numPeaks, final int numFormulae, final int maxFormulaPeaks,
				final int defaultPosition, final double defaultValue) {
			cache = new SparseDoubleMatrix3D(numPeaks, numFormulae, maxFormulaPeaks);
			this.defaultPosition = defaultPosition;
			this.defaultValue = defaultValue;
			this.cached = new boolean[numPeaks];
			this.nz = new NonZeros[numPeaks];
		}
		
		public NonZeros getNonZeros(final int peak) {
			if ( ! cached[peak] ) {
				nz[peak] = new NonZeros();
				final DoubleMatrix2D mat = cache.viewSlice(peak);
				mat.forEachNonZero(nz[peak]);
				cached[peak] = true;
			}
			return nz[peak];
		}

		public static class NonZeros implements IntIntDoubleFunction {
			public final IntArrayList formulaList;
			public final IntArrayList positionList;
			public final DoubleArrayList valueList;
			
			public NonZeros() {
				this.formulaList = new IntArrayList();
				this.positionList = new IntArrayList();
				this.valueList = new DoubleArrayList();
			}
			
			public double apply(int formula, int position, double value) {
				formulaList.add(formula);
				positionList.add(position);
				valueList.add(value);
				return value;
			}
		}
		
		public DoubleMatrix2D get(final int peak) {
			return cache.viewSlice(peak);
		}
		
		public double get(final int peak, final int formula, final int position) {
			return cache.get(peak, formula, position);
		}
		
		public DoubleMatrix1D getRow(final int peak, final int formula) {
			return get(peak).viewRow(formula);
		}
		
		public void set(final int peak, final int formula, final int position, final double value) {
			cache.set(peak, formula, position, value);
			cached[peak] = false;
		}
		
		public void add(final int peak, final int formula, final int position, final double value) {
			if ( value == 0.0 ) {
				return;
			}
			cache.set(peak, formula, position, cache.get(peak, formula, position) + value);
			cached[peak] = false;
		}
		
		public int[] cacheSize() {
			final int[] retval = new int[cache.slices()];
			for (int i = 0; i < cache.slices(); ++i) {
				retval[i] = get(i).cardinality();
			}
			return retval;
		}
		
		public static SparseVector toSparseVector(final DoubleMatrix1D matrix) {
			final IntArrayList indices = new IntArrayList(matrix.cardinality());
			final DoubleArrayList values = new DoubleArrayList(matrix.cardinality());
			matrix.getNonZeros(indices, values);
			indices.trimToSize(); values.trimToSize();
			final SparseVector retval = new SparseVector(matrix.size(), indices.elements(), values.elements());
			return retval;
		}
	}
}
