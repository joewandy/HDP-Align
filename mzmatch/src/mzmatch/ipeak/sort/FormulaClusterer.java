package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.util.ArrayList;
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

import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;

public class FormulaClusterer extends Clusterer<FormulaData,FormulaClustering> {
	protected static double massCutOff = 1e-100; // The likelihood, below which we remove the value.
	protected static double logMassCutOff = Math.log(massCutOff);
	// If the ppm difference is greater than the value given, then the likelihood is less than the massCutOff
	protected static double ppmCutOff = 100 * Common.onePPM;
	protected static double peakBaseRatio = 100;
	
	private final FormulaParameters parameters;
	private final LikelihoodScorer inScorer;
	private final LikelihoodScorer outScorer;
	private final CorrelationMeasure measure;

	private final String[] adducts;
	private final MassIntensityClusteringScorer miScorer;

	public FormulaClusterer(final FormulaData data, final FormulaParameters parameters, final Random random,
			final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, final PeakPosteriorScorer<FormulaClustering> scorer,
			MassIntensityClusteringScorer miScorer) {
		super(parameters.numSamples, parameters.burnIn, data, parameters, random, scorer);
		this.parameters = parameters;

		this.inScorer = inScorer;
		this.outScorer = outScorer;
		this.measure = measure;
		this.adducts = adducts;
		this.miScorer = miScorer;
	}

	@Override
	public FormulaClustering createClustering() {
		return FormulaClustering.createFormulaClustering(random, data, parameters, inScorer, outScorer,
				measure, adducts, miScorer);
	}
	
	@Override
	public double singleSample(final FormulaClustering currentClustering) {
		super.singleSample(currentClustering);
		// TODO Need to sample new cluster assignment and peak positions.
		//System.err.println(currentClustering);
		currentClustering.doClusterPositionStep();
		
		//assert false;
		return scorer.calculateLikelihood(currentClustering);
	}

	public static class MassIntensityClusteringScorer implements PeakLikelihoodScorer<FormulaClustering> {
		//private final ClusteringScorer clusteringScorer;
		private final FormulaParameters parameters;
		private final String[] adducts;
		private final double uniformLikelihood;
		private final double baseLikelihood;
		private final int maxPositions;
		private final int defaultPosition;
		
		private final double mu0;
		private final double kappa0;
		
		private static final double kappa = 1e-10;
		private final FormulaData data;
		private final double massPrecision;
		
		final ScoringCache[] scoringCaches;
		final double[] newClusterMassIntensityLikelihood;
		final FlexCompRowMatrix flatml;
		final ScoringCache massCache;
		final Random random;
		final double[][] beta_mai;
		final double[][] background;
		final double[][] expBackground;
		final double[] backgroundSum;
		
		public MassIntensityClusteringScorer(final FormulaData data, final FormulaParameters parameters, final LikelihoodScorer inScorer,
				final LikelihoodScorer outScorer, final CorrelationMeasure measure,
				final String[] adducts, final Random random) {
			this.parameters = parameters;
			//this.clusteringScorer = new ClusteringScorer(data, parameters, inScorer, outScorer, measure);
			this.data = data;
			if ( parameters.debug ) System.err.println(data.numReplicates + " replicates and " + data.numPeaksets + " peaks in sample");
			this.adducts = adducts;
			final double overallRatio = peakBaseRatio * data.numFormulae;
			//final double crossingPoint = calculateMassDeviation(massPrecisionPPM);
			//this.massPrecision = (2 * Math.log(overallRatio)) / (crossingPoint * crossingPoint);
			//this.uniformLikelihood = 1 / (overallRatio * crossingPoint * Math.sqrt(Math.PI / Math.log(overallRatio)));
			this.massPrecision = calculateMassPrecision(parameters.massPrecisionPPM);
			final double modeMassLikelihood = Math.sqrt(massPrecision/(2 * Math.PI));
			this.uniformLikelihood = modeMassLikelihood / overallRatio;
			this.baseLikelihood = Math.log(uniformLikelihood);
			this.maxPositions = (adducts.length * parameters.maxValues) + 1;
			this.defaultPosition = maxPositions - 1;
			//this.uniformLikelihood = normalDensity(Math.log(1000003), Math.log(1000000), this.massPrecision, false);
			//System.err.println("ul: " + uniformLikelihood + " mp: " + massPrecision + " ppm: " +
			//		parameters.massPrecisionPPM + " peak: " + 1 / Math.sqrt((2 * Math.PI) / massPrecision));
			//assert false;
			final double[] flatIntensities = Doubles.concat(data.intensities);
			this.mu0 = Common.mean(flatIntensities, true);
			//this.kappa0 = 1.0 / Common.variance(flatIntensities, true);
			this.kappa0 = 1.0e-16;
			//System.out.println("mu0: " + mu0);
			//System.out.println("kappa0: " + kappa0);
			
			this.scoringCaches = new ScoringCache[data.numReplicates];
			this.newClusterMassIntensityLikelihood = new double[data.numPeaksets];
			this.flatml = new FlexCompRowMatrix(data.numPeaksets, data.numFormulae);
			this.massCache = new ScoringCache(data.numPeaksets, data.numFormulae, maxPositions, defaultPosition,
					baseLikelihood);
			this.random = random;
			this.beta_mai = new double[data.numFormulae][];
			this.background = new double[data.numReplicates][data.numPeaksets];
			this.expBackground = new double[data.numReplicates][data.numPeaksets];
			this.backgroundSum = new double[data.numPeaksets];

			calculateMassLikelihoods(data, mu0, kappa, kappa0);
			calculateBeta();
		}
		/*
		public double[] calculatePeakLogPosterior(final FormulaClustering clustering, int peak) {
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
		*/
		/*
		private double calculateRelativePeakLikelihood(final List<Integer> positions, final List<Integer> peaks,
				final int formula, final int rep) {
			final double[] kappaK = calculateKappaKChain(positions, formula);
			final double[] muK = calculateMuKChain(kappaK, positions, peaks, formula, rep);
			
			double LL = 0.0;
			for ( int i = 0; i < kappaK.length; ++i ) {
				LL += calculateMassLikelihood(formula, rep, peaks.get(i), positions.get(i));
				LL += calculateIntensityLikelihood(formula, rep, peaks.get(i), positions.get(i), kappaK[i], muK[i]);
			}
			assert ! Double.isNaN(LL);
			return LL;
		}
		*/
		public double[] calculatePeakLikelihood(final FormulaClustering currentClustering, final int peak) {
			final int K = currentClustering.numberOfClusters();
			final double[] peakLikelihood = new double[K + 1];
			//final double[] peakLikelihood = clusteringScorer.calculatePeakLikelihood(currentClustering, peak);
			//System.out.println("Peak: " + peak + " peakLikelihood: " + Arrays.toString(peakLikelihood));
			//System.out.println("Correlation");
			//System.out.println(Common.arrayToString(peakLikelihood));
			//System.out.println("Peak: " + peak);
			assert peakLikelihood.length == currentClustering.numberOfClusters() + 1;
			//final double[] kappaK = calculateKappaK(currentClustering, peak);
			for (int rep = 0; rep < data.numReplicates; ++rep) {
				final double measuredIntensity = data.intensities[rep][peak];
				if ( Double.isNaN(measuredIntensity) ) {
					continue;
				}
				//final double background = intensityScore(measuredIntensity, mu0, 1 / (1/kappa + 1/kappa0), true) + baseLikelihood;
				for ( int k = 0; k < peakLikelihood.length - 1; ++k ) {
					peakLikelihood[k] += expBackground[rep][peak];
				}
				final ScoringCache.NonZeros nz = scoringCaches[rep].getNonZeros(peak);
				final int numValues = nz.formulaList.size();
		
				for ( int i = 0; i < numValues; ++i ) {
					final int formula = nz.formulaList.get(i);
					final int position = nz.positionList.get(i);
					final double value = nz.valueList.get(i);
					final List<Integer> clusters = currentClustering.getClustersFromFormula(formula);

					for ( int k : clusters ) {
						final List<Integer> clusterPeaks = currentClustering.getClusterPeaks(k);
						final List<Integer> peakPositions = currentClustering.getClusterPositions(k);
						//final int groupSize = currentClustering.clusterSize(k);
						//final List<Integer> clusterPeaks = new ArrayList<Integer>(currentClustering.getClusterPeaks(k));
						//final List<Integer> peakPositions = currentClustering.getClusterPositions(k);

						final double clusterPeakIntensityLike = calculateIntensityLikelihood(formula, rep, peak, position,
								clusterPeaks, peakPositions);
						peakLikelihood[k] += Math.exp(value + clusterPeakIntensityLike);
					}
				}
				
				for ( int k = 0; k < peakLikelihood.length - 1; ++k) {
					final int currentFormula = currentClustering.getFormula(k);
					assert ! Double.isNaN(peakLikelihood[k]) && ! Double.isInfinite(peakLikelihood[k]) && peakLikelihood[k] >= 0.0 :
						"peakLikelihood[k]: " + peakLikelihood[k] +
						" k: " + k + " size: " + currentClustering.clusterSize(k) +
						" current formula: " + currentFormula;
					if ( peakLikelihood[k] == 0.0 ) {
						peakLikelihood[k] = -Double.MAX_VALUE;
					} else {
						peakLikelihood[k] = Math.log(peakLikelihood[k]) - Math.log(numberLikelihoodComponents(currentFormula));
					}
					assert ! Double.isNaN(peakLikelihood[k]) : "peakLikelihood[k]: " + peakLikelihood[k] +
						" numberTheoreticalPeaks: " + numberLikelihoodComponents(currentFormula);
				}
				//final double[] muK = calculateMuK(currentClustering, peak, rep, kappaK);
				//assert muK.length == peakLikelihood.length - 1;
				//final FlexCompRowMatrix massLikelihood = scoringCaches[rep].get(peak);
				/*
				for ( int k = 0; k < peakLikelihood.length - 1; ++k) {
					final int currentFormula = currentClustering.getFormula(k);
					assert currentFormula >= 0 && currentFormula < data.numFormulae : "Current Formula: " + currentFormula;
					
					final SparseVector likelihoods = calculateMassIntensityLikelihood(currentFormula, currentClustering, peak, k, rep, false);
					final double subTotal = calculateSubTotal(likelihoods);

					
					//subTotal += defaultLikelihood[rep][currentFormula];
					//subTotal += baseLikelihood[peak];
					assert ! Double.isNaN(subTotal) && ! Double.isInfinite(subTotal) : "subTotal: " + subTotal +
						" k: " + k + " size: " + currentClustering.clusterSize(k) +
						" current formula: " + currentFormula;
					if ( subTotal == 0.0 ) {
						peakLikelihood[k] += -Double.MAX_VALUE;
					} else {
						peakLikelihood[k] += Math.log(subTotal) - Math.log(numberLikelihoodComponents(currentFormula));
					}
					assert ! Double.isNaN(peakLikelihood[k]) : "peakLikelihood[k]: " + peakLikelihood[k] +
						" subTotal: " + subTotal + " numberTheoreticalPeaks: " + numberLikelihoodComponents(currentFormula);
				}
				*/
				assert ! Double.isNaN(newClusterMassIntensityLikelihood[peak]);
				assert ! Double.isNaN(peakLikelihood[peakLikelihood.length - 1]);
			}
			peakLikelihood[peakLikelihood.length - 1] += newClusterMassIntensityLikelihood[peak];
			//System.out.println("End Peak: " + peak + " peakLikelihood: " + Arrays.toString(peakLikelihood));
			//System.out.println("Peak: " + peak + " max: " + Doubles.max(peakLikelihood) + " argmax: " + Doubles.indexOf(peakLikelihood, Doubles.max(peakLikelihood)));
			//System.out.println("Mass");
			//System.out.println("Peak: " + peak + " " + peakLikelihood[68]);
			
			/*

			for ( int k = 0; k < peakLikelihood.length - 1; ++k ) {
				final int currentFormula = currentClustering.getFormula(k);
				assert currentFormula >= 0 && currentFormula < numFormulae : "Current Formula: " + currentFormula;
				double runningTotal = 0.0;
				for ( int rep = 0; rep < numReplicates; ++rep ) {
					final FlexCompRowMatrix massLikelihood = scoringCaches[rep].get(peak);
					final double[] like = massLikelihood.getRow(currentFormula).getData();
					final int[] indices = massLikelihood.getRow(currentFormula).getIndex();
					double subTotal = 0.0;
					for (int i = 0; i < indices.length; ++i) {
						final double clusterPeakMassLike = like[i];
						final double clusterPeakIntensityLike = calculateIntensityLikelihood(currentClustering, peak, k, rep);
						subTotal += Math.exp(clusterPeakMassLike + clusterPeakIntensityLike);
						//subTotal += Math.exp(clusterPeakMassLike);
					}
					//subTotal += baseLikelihood[peak];
					assert ! Double.isNaN(subTotal) && ! Double.isInfinite(subTotal) && subTotal > 0.0: "subTotal: " + subTotal;
					runningTotal += Math.log(subTotal) - Math.log(numberLikelihoodComponents(currentFormula));
					assert ! Double.isNaN(runningTotal) && ! Double.isInfinite(runningTotal) : "runningTotal: " + runningTotal +
						" subTotal: " + subTotal + " numberTheoreticalPeaks: " + numberLikelihoodComponents(currentFormula);
				}
				peakLikelihood[k] += runningTotal;
			}
			peakLikelihood[peakLikelihood.length - 1] += newClusterMassIntensityLikelihood[peak];
			*/
			return peakLikelihood;
		}
		/*
		private SparseVector calculateMassIntensityLikelihood(final int currentFormula,
				final FormulaClustering currentClustering, final int peak, final int k, final int rep,
				final boolean logged) {
			//assert currentClustering.getFormula(k) == currentFormula : currentClustering.getFormula(k) + " " + currentFormula;
			if ( data.isMissing(rep, peak) ) {
				return null;
			}
			final SparseVector vec = scoringCaches[rep].getPositions(peak, currentFormula);
			//final FlexCompRowMatrix massLikelihood = scoringCaches[rep].get(peak);
			//final SparseVector vec = massLikelihood.getRow(currentFormula);
			//final DoubleMatrix1D vec = scoringCaches[rep].getRow(peak, currentFormula);
			//final SparseVector vec = ScoringCache.toSparseVector(scoringCaches[rep].getRow(peak, currentFormula));
			final double[] like = vec.getData();
			final int[] indices = vec.getIndex();
			//final SparseVector retval = new SparseVector(vec.size(), indices.length);
			final double[] values = new double[indices.length];
			assert indices.length != 0;
			final List<Integer> clusterPeaks = currentClustering.getClusterPeaks(k);
			final List<Integer> peakPositions = currentClustering.getClusterPositions(k);
			final int groupSize = currentClustering.clusterSize(k);
			for (int i = 0; i < indices.length; ++i) {
				final double clusterPeakMassLike = like[i];
				final int position = indices[i];
				//final String adduct = data.theoreticalSpectrums.get(currentFormula).getAdduct(position);
				//final double kappaK = calculateKappaK(peak, clusterPeaks, peakPositions, currentFormula, adduct);
				//final double muK = calculateMuK(peak, clusterPeaks, peakPositions, currentFormula, kappaK, rep, adduct);
				
				final double clusterPeakIntensityLike = calculateIntensityLikelihood(currentFormula, rep, peak, position, k,
						clusterPeaks, peakPositions, groupSize);
				//assert false : position;
				//if (k == 24 ) {
				//System.out.println("Peak: " + peak + " clusterPeakMassLike: " + clusterPeakMassLike +
				//		" clusterPeakIntensityLike: " + clusterPeakIntensityLike + " position: " + position +
				//		" indices.length: " + indices.length + " i: " + i + " currentFormula: " + currentFormula + " k: " + k);
				//}
				//final double clusterPeakIntensityLike = 0.0;

				final double total = clusterPeakMassLike + clusterPeakIntensityLike;
				final double value = logged ? total : Math.exp(total);
				values[i] = value;
				//retval.set(position, value);
				//assert value != 0.0 : "k: " + k + " value: " + value + " clusterPeakMassLike: " + clusterPeakMassLike +
				//		" clusterPeakIntensityLike: " + clusterPeakIntensityLike + " sum: " +
				//		(clusterPeakMassLike + clusterPeakIntensityLike) + " size: " + currentClustering.clusterSize(k);
				//		
			}
			final SparseVector retval = new SparseVector(vec.size(), indices, values);
			return retval;
		}
		*/
		/*
		private double calculateSubTotal(final SparseVector vector) {
			double retval = 0.0;
			for ( int index : vector.getIndex() ) {
				retval += vector.get(index);
			}
			return retval;
		}
		*/
		private double calculateKappaK(final int peak, final List<Integer> group, final List<Integer> groupPositions,
				final int formula, final int adduct) {
			assert group.size() == groupPositions.size();
			double retval = 0.0;
			final GeneralMassSpectrum spectrum = data.theoreticalSpectrums.get(formula);
			for ( int i = 0; i < group.size(); ++i) {
				final int groupPeak = group.get(i);
				if ( groupPeak == peak ) continue;
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
				if ( groupPeak == peak || Double.isNaN(data.intensities[rep][i]) ) continue;
				final int groupPeakPosition = groupPositions.get(i);
				if ( spectrum.hasValue(groupPeakPosition) && spectrum.getAdduct(groupPeakPosition) == adduct ) {
					final double beta = beta_mai[formula][groupPeakPosition];
					assert ! Double.isNaN(beta);
					retval += data.intensities[rep][i] * beta;
					assert ! Double.isNaN(retval);
				}
			}
			retval = (kappa0 * mu0 + kappa * retval) / kappaK;
			return retval;
		}
		/*
		private double[] calculateKappaK(final int[] assignments, final int[] positions, final int[] formulae, final int numberOfAssignments, final int peak) {
			assert ! Double.isNaN(kappa);
			assert ! Double.isNaN(kappa0);
			final double[] retval = new double[numberOfAssignments];

			for (int i = 0; i < data.numPeaksets; ++i) {
				if ( i == peak ) continue;
				final int formula = formulae[i];
				if ( formula == -1 ) continue;
				final int k = assignments[i];
				final int theoreticalPosition = positions[i];
				assert formula >= 0 : formula;
				assert theoreticalPosition >= 0 : theoreticalPosition;
				//System.err.println("formula: " + formula);
				//System.err.println("theoreticalPosition: " + theoreticalPosition);
				//System.err.println("length: " + beta_mai[formula].length);
				//System.err.println("num spectrums: " + theoreticalSpectrums.size());
				final double beta = beta_mai[formula][theoreticalPosition];
				retval[k] += beta * beta;
				assert ! Double.isNaN(retval[k]);
			}
			for (int i = 0; i < retval.length; ++i) {
				retval[i] = kappa * retval[i] + kappa0;
				assert ! Double.isNaN(retval[i]);
			}
			return retval;
		}
		*/
		/*
		private double[] calculateKappaK(final FormulaClustering currentClustering, final int peak) {
			final double[] retval = new double[currentClustering.numberOfClusters()];
			
			
			//for ( int cluster = 0; cluster < currentClustering.numberOfClusters(); ++cluster ) {
			//	retval[cluster] = calculateKappaK(peak, currentClustering.getClusterPeaks(cluster),
			//		currentClustering.getClusterPositions(cluster), currentClustering.getFormula(cluster));
			//}
			//return retval;
			

			
			
			
			final int[] clusters = currentClustering.getPeakClustering();
			final int[] theoreticalPositions = currentClustering.getPeakPositions();
			final int[] peakFormulas = currentClustering.getFormulas();
			
			for (int i = 0; i < data.numPeaksets; ++i) {
				if ( i == peak ) continue;
				final int k = clusters[i];
				final int theoreticalPosition = theoreticalPositions[i];
				final int formula = peakFormulas[i];
				assert formula >= 0 : formula;
				assert theoreticalPosition >= 0 : theoreticalPosition;
				
				
				
				
				//System.err.println("formula: " + formula);
				//System.err.println("theoreticalPosition: " + theoreticalPosition);
				//System.err.println("length: " + beta_mai[formula].length);
				//System.err.println("num spectrums: " + theoreticalSpectrums.size());
				final double beta = beta_mai[formula][theoreticalPosition];
				retval[k] += beta * beta;
				assert ! Double.isNaN(retval[k]);
			}
			for (int i = 0; i < retval.length; ++i) {
				retval[i] = kappa * retval[i] + kappa0;
				assert ! Double.isNaN(retval[i]);
			}
			return retval;
			
		}
		*/
		/*
		private double[] calculateKappaKFormula(final int[] peakClustering, final int[] peakFormulaAssignments, final int[] peakPositionAssignments, final int formula, final int peak) {
			return calculateKappaK(peakClustering, peakFormulaAssignments, peakPositionAssignments, theoreticalSpectrums.size(), peak);
		}
		*/
		/*
		private double calculateMuKFormula(final FormulaClustering currentClustering, final List<Integer> peaks, final List<Integer> givenPositions,
				final int formula, final int rep, final double kappaK) {
			double retval = 0.0;
			for ( int i = 0; i < peaks.size(); ++i) {
				final int peak = peaks.get(i);
				final int position = givenPositions.get(i);
				if ( Double.isNaN(data.intensities[rep][peak]) ) continue;
				//final int theoreticalPosition = theoreticalPositions[i];
				//final int formula = peakFormulas[i];
				final double beta = beta_mai[formula][position];
				retval += beta * data.intensities[rep][i];
			}
			retval += kappa0 * mu0 + retval * kappa / kappaK;
			return retval;
		}
		*/
		/*
		private double[] calculateKappaKChain(final List<Integer> positions, final int formula) {
			//final Set<Integer> peaks = currentClustering.clusterPeaks.get(cluster);
			final double[] retval = new double[positions.size()];

			retval[0] = kappa0;
			for ( int i = 1; i < positions.size(); ++i ) {
				final int theoreticalPosition = positions.get(i);
				final double beta = beta_mai[formula][theoreticalPosition];
				retval[i] = retval[i - 1] + kappa * beta * beta;
			}
			return retval;
		}
		
		private double[] calculateMuKChain(final double[] kappaK, final List<Integer> positions, final List<Integer> peaks, final int formula, final int rep) {
			final double[] retval = new double[kappaK.length];
			
			retval[0] = kappa0 * mu0;
			for ( int i = 1; i < retval.length; ++i ) {
				final int theoreticalPosition = positions.get(i);
				final int peak = peaks.get(i);
				final double beta = beta_mai[formula][theoreticalPosition];
				retval[i] = retval[i - 1] + data.intensities[rep][peak] * beta;
				
			}
			for ( int i = 0; i < retval.length; ++i ) {
				retval[i] /= kappaK[i];
			}
			return retval;
		}
		*/
		/*
		private double[] calculateMuK(final int[] assignments, final int[] positions, final int[] formulae, final int numberOfClusters, final int peak, final int rep,
				final double[] kappaK) {
			final double[] retval = new double[numberOfClusters];
			
			for (int i = 0; i < data.numPeaksets; ++i) {
				if ( i == peak || Double.isNaN(data.intensities[rep][i]) ) continue;
				final int formula = formulae[i];
				if ( formula == -1 ) continue;
				final int k = assignments[i];
				final int theoreticalPosition = positions[i];
				final double beta = beta_mai[formula][theoreticalPosition];
				assert ! Double.isNaN(beta);
				retval[k] += data.intensities[rep][i] * beta;
				assert ! Double.isNaN(retval[k]);
			}
			for (int i = 0; i < retval.length; ++i) {
				assert ! Double.isNaN(kappaK[i]);
				retval[i] = (kappa0 * mu0 + retval[i] * kappa) / kappaK[i];
				assert ! Double.isNaN(retval[i]);
			}
			return retval;
		}
		*/

		
		
		
		
		/*
		private double[] calculateMuK(final FormulaClustering currentClustering, final int peak, final int rep,
				final double[] kappaK) {
			
			final double[] retval = new double[currentClustering.numberOfClusters()];
			
			//for ( int cluster = 0; cluster < currentClustering.numberOfClusters(); ++cluster ) {
			//	retval[cluster] = calculateMuK(peak, currentClustering.getClusterPeaks(cluster),
			//		currentClustering.getClusterPositions(cluster), currentClustering.getFormula(cluster), kappaK[cluster], rep);
			//}
			//return retval;
			
			final int[] clusters = currentClustering.getPeakClustering();
			final int[] theoreticalPositions = currentClustering.getPeakPositions();
			final int[] peakFormulas = currentClustering.getFormulas();
			
			for (int i = 0; i < data.numPeaksets; ++i) {
				if ( i == peak || Double.isNaN(data.intensities[rep][i]) ) continue;
				final int k = clusters[i];
				final int theoreticalPosition = theoreticalPositions[i];
				final int formula = peakFormulas[i];
				final double beta = beta_mai[formula][theoreticalPosition];
				assert ! Double.isNaN(beta);
				retval[k] += data.intensities[rep][i] * beta;
				assert ! Double.isNaN(retval[k]);
			}
			for (int i = 0; i < retval.length; ++i) {
				assert ! Double.isNaN(kappaK[i]);
				retval[i] = (kappa0 * mu0 + retval[i] * kappa) / kappaK[i];
				assert ! Double.isNaN(retval[i]);
			}
			return retval;
		}
		*/
			
			
			
		/*	
			final int[] clusters = currentClustering.getPeakClustering();
			final int[] theoreticalPositions = currentClustering.getPeakPositions();
			final int[] peakFormulas = currentClustering.getFormulas();
			return calculateMuK(clusters, theoreticalPositions, peakFormulas, currentClustering.numberOfClusters(), peak, rep, kappaK);
			
		}
		
		private double[] calculateMuK(final FormulaClustering currentClustering, final int peak, final int rep,
				final double[] kappaK) {
			final double[] retval = new double[currentClustering.numberOfClusters()];
			
			final int[] clusters = currentClustering.getPeakClustering();
			final int[] theoreticalPositions = currentClustering.getPeakPositions();
			final int[] peakFormulas = currentClustering.getFormulas();
			
			for (int i = 0; i < data.numPeaksets; ++i) {
				if ( i == peak || Double.isNaN(data.intensities[rep][i]) ) continue;
				final int k = clusters[i];
				final int theoreticalPosition = theoreticalPositions[i];
				final int formula = peakFormulas[i];
				final double beta = beta_mai[formula][theoreticalPosition];
				assert ! Double.isNaN(beta);
				retval[k] += data.intensities[rep][i] * beta;
				assert ! Double.isNaN(retval[k]);
			}
			for (int i = 0; i < retval.length; ++i) {
				assert ! Double.isNaN(kappaK[i]);
				retval[i] = (kappa0 * mu0 + retval[i] * kappa) / kappaK[i];
				assert ! Double.isNaN(retval[i]);
			}
			return retval;
		}
		*/
		
		public void doClusterPositionStep(final FormulaClustering currentClustering) {
			for ( int cluster = 0; cluster < currentClustering.numberOfClusters(); ++cluster ) {
				final List<Integer> clusterPeaks = new ArrayList<Integer>(currentClustering.getClusterPeaks(cluster));
				Collections.shuffle(clusterPeaks, random);
				final Pair<List<List<Integer>>,double[]> samples = generateFormulaeSamples(currentClustering, cluster, clusterPeaks);

				
				final Sample<Integer> selected = sampleDistribution(samples.v2);
				final int selectedFormula = selected.v1;
				//if ( cluster == 24 ) {
				//	System.err.println(Arrays.toString(samples.v2));
				//	System.err.println(selectedFormula);	
				//	assert false;
				//}
				final List<Integer> selectedPositions = samples.v1.get(selectedFormula);
				//final double selectedValue = selected.v2;
				currentClustering.setFormula(cluster, selectedFormula, clusterPeaks, selectedPositions);
			}
			
		}
		
		public Pair<List<List<Integer>>,double[]> generateFormulaeSamples(final FormulaClustering currentClustering,
				final int cluster, final List<Integer> clusterPeaks) {
			//final List<Integer> clusterPeaks = new ArrayList<Integer>(currentClustering.getClusterPeaks(cluster));
			//Collections.shuffle(clusterPeaks, random);
			final List<List<Integer>> formulaSamples = new ArrayList<List<Integer>>();
			final List<List<Integer>> formulaSamplePositions = new ArrayList<List<Integer>>();
			final double[] sampleProbabilities = new double[currentClustering.numberOfFormulae()];
			for ( int formula = 0; formula < currentClustering.numberOfFormulae(); ++formula ) {
				formulaSamples.add(new ArrayList<Integer>());
				formulaSamplePositions.add(new ArrayList<Integer>());
			}
			final int clusterSize = currentClustering.clusterSize(cluster);

			for ( int i = 0; i < clusterSize; ++i ) {
				final int peak = clusterPeaks.get(i);
				final List<SparseVector> distributions = new ArrayList<SparseVector>();
				for ( int j = 0; j < currentClustering.numberOfFormulae(); ++j ) {
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
							//dist = new SparseVector(currentClustering.numberOfFormulae());
							distributions.set(formula, dist);
						}
						final List<Integer> currentSample = formulaSamples.get(formula);
						final List<Integer> samplePositions = formulaSamplePositions.get(formula);
						final double clusterPeakIntensityLike = calculateIntensityLikelihood(formula, rep, peak, position,
								currentSample, samplePositions);
						dist.add(position, value + clusterPeakIntensityLike);
					}
				}
				for ( int formula = 0; formula < currentClustering.numberOfFormulae(); ++formula ) {
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
			
			
			
			
			
			/*
			for ( int i = 0; i < clusterSize; ++i ) {
				final int peak = clusterPeaks.get(i);
				for (int formula = 0; formula < currentClustering.numberOfFormulae(); ++formula ) {
					final List<Integer> currentSample = formulaSamples.get(formula);
					final List<Integer> samplePositions = formulaSamplePositions.get(formula);
					//final double kappaK = calculateKappaK(peak, currentSample, samplePositions, formula);
					final List<SparseVector> likelihoodReps = new ArrayList<SparseVector>();
					for ( int rep = 0; rep < data.numReplicates; ++rep) {
						//final double muK = calculateMuK(peak, currentSample, samplePositions, formula, kappaK, rep);
						final SparseVector likelihoods = calculateMassIntensityLikelihood(formula, currentClustering, peak, cluster, rep, true);
						if ( likelihoods != null) {
							likelihoodReps.add(likelihoods);
						}
					}
					final SparseVector combinedLikelihood = combineLikelihoods(likelihoodReps);
					assert combinedLikelihood != null;
					final Sample<Integer> sample = sampleDistribution(combinedLikelihood);
					final int position = sample.v1;
					currentSample.add(peak);
					samplePositions.add(position);
					sampleProbabilities[formula] += sample.v2;
					//if ( cluster == 24 ) {
						//System.err.println(combinedLikelihood);
					//	System.err.println("position: " + position + " logLike: " + sample.v2);
					//}
				}
			}
			*/
			//if (cluster == 24) {
			//	System.err.println(Arrays.toString(sampleProbabilities));
			//	assert false;
			//}
			
			return new Pair<List<List<Integer>>,double[]>(formulaSamplePositions, sampleProbabilities);
			/*
			final int[][] positionAssignments = new int[numFormulae][data.numPeaksets];
			Arrays.fill(positionAssignments, -1);
			final int[] formulaAssignments = new int[data.numPeaksets];
			Arrays.fill(formulaAssignments, -1);
			final int[] clusterAssignments = currentClustering.getPeakClustering();
			
			
			
			
			for ( int formula = 0; formula < currentClustering.numberOfFormulae(); ++formula ) {
				//final List<Integer> formulaAssignments = currentAssignments.get(formula);
				calculateKappaKFormula(currentClustering, formulaAssignments, formula);
				for ( int rep = 0; rep < data.numReplicates; ++rep) {
					calculateMuKFormula(currentClustering, peaks, formulaAssignments, formula);
					for ( int peak = 0; peak < data.numPeaksets; ++peak ) {
						final FlexCompRowMatrix massLikelihood = scoringCaches[rep].get(peak);
					}
					
					
					
					
				}
				
				
				
			}
			*/
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

		/*
		public void doClusterPositionStep(final FormulaClustering currentClustering) {
			final double[][] proposalDistributions = generateFormulaeProposals(currentClustering);
			final Sample<Integer>[] formulaeSamples = pickFormulae(proposalDistributions);
			final Sample<Integer>[] positionSamples = pickPositions(currentClustering, formulaeSamples);
			final int numClusters = currentClustering.numberOfClusters();
			
			//System.err.println("Proposals");
			//System.err.println(Arrays.toString(proposalDistributions[1]));
			//System.err.println(proposalDistributions[1][76]);
			//System.err.println(flatml.get(1, 76));
			//System.err.println(flatml.get(1, 19));
			//assert false;
			
			// Calculate relative posterior - in this case the likelihood involving mass and intensity terms

			for ( int cluster = 0; cluster < numClusters; ++cluster ) {
				final Sample<Integer> fSample = formulaeSamples[cluster];
				final int newFormula = fSample.sample();
				final int oldFormula = currentClustering.getFormula(cluster);
				final Set<Integer> peaks = currentClustering.clusterPeaks.get(cluster);
				final List<Integer> newPositions = new ArrayList<Integer>();
				final List<Integer> oldPositions = new ArrayList<Integer>();
				final List<Integer> peaksList = new ArrayList<Integer>();
				
				double newProposal = fSample.probability();
				double oldProposal = proposalDistributions[cluster][oldFormula];
				for ( int peak : peaks ) {
					final Sample<Integer> pSample = positionSamples[peak];
					newPositions.add(pSample.sample());
					final int oldPosition = currentClustering.getPosition(peak);
					oldPositions.add(currentClustering.getPosition(peak));
					peaksList.add(peak);
					newProposal += pSample.probability();
					oldProposal += massCache.get(peak, oldFormula, oldPosition);
				}

				double newAssignmentLogLikelihood = 0.0;
				double oldAssignmentLogLikelihood = 0.0;
				for ( int rep = 0; rep < data.numReplicates; ++rep ) {
					newAssignmentLogLikelihood += calculateRelativePeakLikelihood(newPositions, peaksList, newFormula, rep);
					oldAssignmentLogLikelihood += calculateRelativePeakLikelihood(oldPositions, peaksList, oldFormula, rep);
				}
				final double likelihoodRatio = Math.exp(newAssignmentLogLikelihood + oldProposal - oldAssignmentLogLikelihood - newProposal);
				final double acceptanceProbability = Math.min(1.0, likelihoodRatio);
				System.out.println("cluster: " + cluster + " oldFormula: " + oldFormula + " newFormula: " + newFormula + " ap: " +
						acceptanceProbability + " qnew: " + newProposal + " qold: " + oldProposal + " pnew: " +
						newAssignmentLogLikelihood + " pold: " + oldAssignmentLogLikelihood + " lr: " + likelihoodRatio +
						" op: " + Joiner.on(",").join(oldPositions) + " np: " + Joiner.on(",").join(newPositions));
				assert acceptanceProbability >= 0.0 && acceptanceProbability <= 1.0;
				final double draw = random.nextDouble();
				if ( acceptanceProbability > draw ) {
					// Make the move
					currentClustering.setFormula(cluster, newFormula);
					for ( int i = 0; i < peaksList.size(); ++i) {
						final int peak = peaksList.get(i);
						final int position = newPositions.get(i);
						currentClustering.setPosition(peak, position);
					}
				}
			}
		}
		*/
		
		public double[][] generateFormulaeProposals(final FormulaClustering currentClustering) {
			final int numClusters = currentClustering.numberOfClusters();
			final double[][] proposalDistributions = new double[numClusters][data.numFormulae];
			
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
			//final FormulaClustering proposedClustering;
			final int numClusters = proposalDistributions.length;

			@SuppressWarnings("unchecked")
			final Sample<Integer>[] samples = (Sample<Integer>[])Array.newInstance(Sample.class, numClusters);
			for ( int cluster = 0; cluster < numClusters; ++cluster) {
				//final int currentFormula = currentClustering.getFormula(cluster);
				final double[] dist = proposalDistributions[cluster];
				final double[] normalisedDistribution = Clusterer.normaliseDistribution(dist, -1);
				final int selectedFormula = Clusterer.samplePosition(normalisedDistribution, random);
				samples[cluster] = new Sample<Integer>(selectedFormula, dist[selectedFormula]);
			}
			return samples;
		}
		
		private Sample<Integer>[] pickPositions(final FormulaClustering currentClustering, final Sample<Integer>[] formulae) {
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
		/*
		private void pickClusterFormulae(final FormulaClustering currentClustering, final double[][] proposalDistributions,
				final int[] formulae, final double[] oldValue, final double[] newValue) {

			for ( int cluster = 0; cluster < currentClustering.numberOfClusters(); ++cluster) {
				final int currentFormula = currentClustering.getFormula(cluster);
				final double[] dist = proposalDistributions[cluster];
				final double[] normalisedDistribution = Clusterer.normaliseDistribution(dist, -1);
				final int selectedFormula = Clusterer.samplePosition(normalisedDistribution, random);
				formulae[cluster] = selectedFormula;
				oldValue[cluster] = dist[currentFormula];
				newValue[cluster] = dist[selectedFormula];
			}
		}
		*/
		
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
		
		public int pickFormula(final int peak, final FormulaClustering currentClustering) {
			final SparseVector v = flatml.getRow(peak);
			final Sample<Integer> sample = sampleDistribution(v);
			return sample.v1;
			
			/*
			final double[] formulaDistribution = toCompactArray(v);
			final double[] normalisedDistribution = Clusterer.normaliseDistribution(formulaDistribution, -1);
			final int selectedOffset = Clusterer.samplePosition(normalisedDistribution, random);
			final int selectedFormula = v.getIndex()[selectedOffset];
			//System.err.println("peak " + peak + " normalisedDistribution: " + Arrays.toString(normalisedDistribution));
			//System.err.println("peak " + peak + " sum: " + Common.sum(normalisedDistribution));
			//System.err.println("peak " + peak + " selectedFormula: " + selectedFormula);
			return selectedFormula;
			*/
		}
		
		public Sample<Integer> pickPeak(final int peak, final FormulaClustering currentClustering) {
			//final int cluster = currentClustering.getCluster(peak);
			final int formula = currentClustering.getPeakFormula(peak);
			//final int formula = currentClustering.getFormula(cluster);
			final Sample<Integer> sample = samplePosition(peak, formula);
			assert sample.sample() >= 0 && sample.sample() < maxPositions;
			return sample;
		}
		
		private Sample<Integer> samplePosition(final int peak, final int formula) {
			final DoubleMatrix1D dist = massCache.getRow(peak, formula);
			final SparseVector v = ScoringCache.toSparseVector(dist);
			v.set(defaultPosition, backgroundSum[peak]);
			//final SparseVector v = massCache.getPositions(peak, formula);
			//final SparseVector v = ScoringCache.toSparseVector(massCache.getRow(peak, formula));
			return sampleDistribution(v);
			
			
			
			/*
			//assert v.getUsed() > 0;
			final double[] peakDistribution = toCompactArray(v);
			//if ( peakDistribution.length == 0 ) {
			//	// We aren't matching any of the peaks, so return the base likelihood (-1)
			//	return -1;
			//}
			assert peakDistribution.length > 0;
			final double[] normalisedDistribution = Clusterer.normaliseDistribution(peakDistribution, -1);
			final int selectedOffset = Clusterer.samplePosition(normalisedDistribution, random);
			final int selectedPosition = v.getIndex()[selectedOffset];
			assert selectedPosition == data.theoreticalSpectrums.get(formula).size() ||
					data.polarities[peak] == data.theoreticalSpectrums.get(formula).getPolarity(selectedPosition);
			return new Sample<Integer>(selectedPosition, v.getData()[selectedOffset]);
			*/
		}
		/*
		private Sample<Integer> samplePosition(final int peak, final int formula) {
			
		}
		*/
		private static double[] toCompactArray(final SparseVector v) {
			final int size = v.getUsed();
			assert size == v.getIndex().length : "size: " + size + " indices.length: " + v.getIndex().length;
			final double[] data = v.getData();
			final double[] retval = new double[size];
			System.arraycopy(data, 0, retval, 0, size);
			return retval;
		}
		/*
		public double calculateIntensityLikelihood(final FormulaClustering currentClustering, final int cluster, final int rep,
				final int peak, final int position) {
			final int formula = currentClustering.getFormula(cluster);
			final List<Integer> clusterPeaks = new ArrayList<Integer>(currentClustering.getClusterPeaks(cluster));
			final List<Integer> peakPositions = currentClustering.getClusterPositions(cluster);
			return calculateIntensityLikelihood(formula, rep, peak, position, -1, clusterPeaks, peakPositions);
		}
		*/
		public double calculateIntensityLikelihood(final int formula, final int rep, final int peak, final int position,
				final List<Integer> clusterPeaks, final List<Integer> peakPositions) {
			assert formula >=0 && formula < beta_mai.length;
			final double measuredIntensity = data.intensities[rep][peak];
			if ( Double.isNaN(measuredIntensity)  ) {
				return 0.0;
			}

			assert position >=0 && position < beta_mai[formula].length : "position: " + position + " beta_mai[formula].length: " +
				beta_mai[formula].length + " formula: " + formula + " size: " + data.theoreticalSpectrums.get(formula).size();
//			assert false : position + " " + beta_mai[formula].length + " " + formula;
			if ( position == defaultPosition ) {
				return intensityScore(measuredIntensity, mu0, 1 / (1/kappa + 1/kappa0), true);
			}
			final int adduct = data.theoreticalSpectrums.get(formula).getAdduct(position);
			final double kappaK = calculateKappaK(peak, clusterPeaks, peakPositions, formula, adduct);
			final double muK = calculateMuK(peak, clusterPeaks, peakPositions, formula, kappaK, rep, adduct);
			
			
			final double beta_kai = beta_mai[formula][position];
			final double mu_w = beta_kai * muK;
			final double kappa_w = 1 / (1/kappa + beta_kai * beta_kai / kappaK);
			/*
			System.out.println("kappaK: " + Common.arrayToString(kappaK));
			System.out.println("muK: " + Common.arrayToString(muK));
			System.out.println("beta_kai: " + beta_kai);
			System.out.println("measuredIntensity: " + measuredIntensity);
			System.out.println("mu_w: " + mu_w);
			System.out.println("kappa_w: " + kappa_w);
			*/
			//if ( peak == 182 && k == 170 ) {
			//	System.err.println("Peak: " + peak + " pos: " + position + " rep: " + rep + " measuredIntensity: " + measuredIntensity + " muK: " + muK + " mu_w: " + mu_w + " kappa_w: " + kappa_w);
			//}
			
			final double score = intensityScore(measuredIntensity, mu_w, kappa_w, true);
			//System.out.println("Peak: " + peak + " measuredIntensity: " + measuredIntensity + " mu_w: " + mu_w + " kappa_w: " + kappa_w + " score: " + score);
			return score;
		}
		
		public double calculateMassLikelihood(final int formula, final int rep, final int peak, final int position) {
			if ( Double.isNaN(data.masses[rep][peak]) ) {
				return 0.0;
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
		
		private void calculateMassLikelihoods(final FormulaData data,
				final double mu0,
				final double kappa, final double kappa0) {


			//final double maxLogMass = Common.max(data.masses, true);
			//final double minLogMass = Common.min(data.masses, true);
			//final double uniformLikelihood = uniformWeighting / (Math.exp(maxLogMass) - Math.exp(minLogMass));

			for (int rep = 0; rep < data.numReplicates; ++rep) {
				scoringCaches[rep] = new ScoringCache(data.numPeaksets, data.numFormulae,
						maxPositions, defaultPosition, baseLikelihood);
				for (int peak = 0; peak < data.numPeaksets; ++peak) {
					final double priorIntensityScore = intensityScore(data.intensities[rep][peak], mu0, 1 / (1/kappa + 1/kappa0), true);
					if ( Double.isNaN(priorIntensityScore) ) {
						// if the intensity is NaN, then the mass is NaN as well
						continue;
					}
					background[rep][peak] = priorIntensityScore + baseLikelihood;
					expBackground[rep][peak] = Math.exp(background[rep][peak]);
					backgroundSum[peak] += priorIntensityScore + baseLikelihood;
					//System.err.println(priorIntensityScore);
					//priorIntensityScores[rep][peak] = priorIntensityScore;
					//assert ! Double.isNaN(priorIntensityScore);
					//final double baseLikelihood = Math.exp(Math.log(uniformLikelihood) + priorIntensityScore);
					
					//baseLikelihood[peak] += Math.exp(Math.log(uniformLikelihood) + priorIntensityScore);
					assert ! Double.isNaN(baseLikelihood);
					//assert baseLikelihood[peak] != 0;
					double runningTotal = 0.0;
					for (int formulaId = 0; formulaId < data.numFormulae; ++formulaId) {
						final GeneralMassSpectrum ms = data.theoreticalSpectrums.get(formulaId);
						double subTotal = 0.0;
						//int numDefault = 0;
						//final List<Integer> positions = new ArrayList<Integer>();
						//final List<Double> values = new ArrayList<Double>();
						
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
							//if ( formulaId == 76 && peak == 134 ) {
							//	System.out.println("massValue: " + massValue + " peak: " + peak + " mass: " + data.masses[rep][peak] + " rep: " + rep + " lmc: " + logMassCutOff);
							//}
							assert ! Double.isNaN(massValue);
							
							if ( massValue < logMassCutOff ) {
								//massCache.set(peak, formulaId, i, 0.0);
								continue;
							}
							
							//assert false : "We have gotten here!";
							//positions.add(i);
							//values.add(massValue);
							scoringCaches[rep].set(peak, formulaId, i, massValue);
							massCache.add(peak, formulaId, i, massValue);
							//final double clusterPeakIntensityLike = calculateIntensityLikelihood(currentClustering, peak, k, rep);
							//subTotal += clusterPeakMassLike * clusterPeakIntensityLike;
							subTotal += Math.exp(massValue);
						}
						//scoringCaches[rep].set(peak, formulaId, ms.size(), baseLikelihood);
						//massCache.add(peak, formulaId, ms.size(), baseLikelihood);
						//scoringCaches[rep].set(peak, formulaId, positions, values);
						
						//this.defaultLikelihood[rep][formulaId] = massCutOff * numDefault;
						subTotal += Math.exp(baseLikelihood);// + this.defaultLikelihood[rep][formulaId];
						
						final double averageOverTheoreticalPeaks = subTotal / numberLikelihoodComponents(formulaId);
						//System.err.println("peak: " + peak + " formula: " + formulaId + " subTotal: " + subTotal + " averageOverTheoreticalPeaks: " + averageOverTheoreticalPeaks);
						//if ( averageOverTheoreticalPeaks > massCutOff ) {
							flatml.set(peak, formulaId, Math.log(averageOverTheoreticalPeaks));
						//}
//						flatml[formulaId][peak] = Math.log(averageOverTheoreticalPeaks);
						runningTotal += averageOverTheoreticalPeaks;
					}
					assert ! Double.isNaN(priorIntensityScore);
					assert runningTotal > 0.0;
					assert data.numFormulae != 0;
					final double value = Math.log(runningTotal / (double)data.numFormulae);
					assert ! Double.isNaN(value) : "runningTotal: " + runningTotal + " numFormulae: " + data.numFormulae;
					assert ! Double.isNaN(newClusterMassIntensityLikelihood[peak]);
					newClusterMassIntensityLikelihood[peak] += priorIntensityScore + value;
					//newClusterMassIntensityLikelihood[peak] += value;
					assert ! Double.isNaN(newClusterMassIntensityLikelihood[peak]);

				}
			}
			/*
			for (int peak = 0; peak < data.numPeaksets; ++peak) {
				for (int formula = 0; formula < data.numFormulae; ++formula) {
					final List<SparseVector> likelihoodReps = new ArrayList<SparseVector>();
					for (int rep = 0; rep < data.numReplicates; ++rep) {
						likelihoodReps.add(scoringCaches[rep].getPositions(peak, formula));
					}
					final SparseVector combined = combineLikelihoods(likelihoodReps);
					massCache.set(peak, formula, combined);
				}
			}
			*/
			//System.out.println(flatml.getRow(1));
			//System.out.println(flatml.getRow(1).get(79));
			//System.out.println(flatml.getRow(1).get(80));
			//System.out.println(Arrays.toString(newClusterMassIntensityLikelihood));
			//System.out.println(newClusterMassIntensityLikelihood);
			//System.exit(1);
			/*
			final int[] cs = massCache.cacheSize();
			int total = 0;
			for (int i = 0; i < cs.length; ++i) {
				cs[i] -= 105;
				total += cs[i];
			}
			System.err.println(Arrays.toString(cs));
			System.err.println(total);
			*/
			//System.exit(1);
			
		}
		
		private void calculateBeta() {
			for (int formula = 0; formula < data.numFormulae; ++formula) {
				final List<Double> dist = data.theoreticalSpectrums.get(formula).getDistribution();
				beta_mai[formula] = new double[maxPositions];
				for (int i = 0; i < dist.size(); ++i) {
					beta_mai[formula][i] = dist.get(i);
				}
				//beta_mai[formula][maxPositions - 1] = 1.0;
			}
		}

		private double massScore(final double logMeasuredMass, final double logTheoreticalMass, final boolean logged) {
			//final double logMeasuredMass = Math.log(measuredMass);
			//final double logTheoreticalMass = Math.log(theoreticalMass);
			//System.err.println("measuredMass: " + measuredMass);
			//System.err.println("theoreticalMass: " + theoreticalMass);
			//System.err.println("logMeasuredMass: " + logMeasuredMass);
			//System.err.println("logTheoreticalMass: " + logTheoreticalMass);
			//System.err.println("massPrecision: " + massPrecision);
			
			return Common.normalDensity(logMeasuredMass, logTheoreticalMass, this.massPrecision, logged);
		}

		private double intensityScore(final double measuredIntensity, final double theoreticalIntensity, final double precision,
				final boolean logged) {
			return Common.normalDensity(measuredIntensity, theoreticalIntensity, precision, logged);
		}
		/*
		private double normalDensity(final double x, final double mu, final double precision, final boolean logged) {
			final double logDensity = -0.5 * Math.log(2 * Math.PI) + 0.5 * Math.log(precision)
					- 0.5 * precision * Math.pow(x - mu, 2);
			return logged ? logDensity : Math.exp(logDensity);
		}
		*/
		/* FIXME TO REMOVE
		private double calculateMassPrecision(final double massPrecisionPPM) {
			final double onePPM = Math.log(1000001) - Math.log(1000000);
			final double deviation = onePPM * massPrecisionPPM;
			final double standardDeviation = deviation;
			final double precision = 1.0 / (standardDeviation * standardDeviation);
			return precision;
		}
		*/
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
			//System.err.println("data: " + numPeaks + " " + numFormulae + " " + maxFormulaPeaks);
			peakFormulaPositionMap = (Map<Integer,SparseVector>[])Array.newInstance(HashMap.class, numPeaks);
			for ( int i = 0; i < numPeaks; ++i ) {
				peakFormulaPositionMap[i] = new HashMap<Integer,SparseVector>();
			}
			defaultRetval = new SparseVector(maxPositions, new int[]{ defaultPosition }, new double[]{ defaultValue });
			this.maxPositions = maxPositions;
			this.defaultValue = defaultValue;
			this.defaultPosition = defaultPosition;
			//cache = new FlexCompRowMatrix[numPeaks];
			//for (int i = 0; i < cache.length; ++i) {
			//	cache[i] = new FlexCompRowMatrix(numFormulae, maxFormulaPeaks);
			//}
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
			//System.err.println("Blah");
			//System.err.println(values);
			if ( values.get(defaultPosition) != 0.0 ) {
				if ( values.getUsed() == 1 ) {
					// The only value is the default value, so return
					return;
				}
				//System.err.println("Ble");
				//System.err.println(values);
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
		/*
		public void add(final int peak, final int formula, final List<Integer> positions, final List<Double> values) {
			final int[] positionsArray = Ints.toArray(positions);
			final double[] valuesArray = Doubles.toArray(values);
			peakFormulaPositionMap[peak].put(formula, new Pair<int[],double[]>(positionsArray, valuesArray));
		}
		*/
	}
	/*
	public static class IndexedValues {
		public final int[] indices;
		public final double[] values;
		
		public IndexedValues(final int[] indices, final double[] values) {
			this.indices = indices;
			this.values = values;
		}
		
		
		public static IndexedValues combineLikelihoods(final List<IndexedValues> likelihoodReps) {
			if ( likelihoodReps.size() == 0 ) {
				return null;
			}
			final IndexedValues retval = likelihoodReps.get(0);
			for ( int i = 1; i < likelihoodReps.size(); ++i) {
				final IndexedValues likelihood = likelihoodReps.get(i);
				final double[] like = likelihood.values;
				final int[] indices = likelihood.indices;
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
	}
*/
/*	
	public static class ListScoringCache {
		private final DoubleMatrix formulae
	}
	*/
	public static class ScoringCache {
		private final DoubleMatrix3D cache;
		private boolean cached[];
		private NonZeros[] nz;
		private final int defaultPosition;
		private final double defaultValue;
		
		//private final FlexCompRowMatrix[] cache;
				
		public ScoringCache(final int numPeaks, final int numFormulae, final int maxFormulaPeaks,
				final int defaultPosition, final double defaultValue) {
			//System.err.println("data: " + numPeaks + " " + numFormulae + " " + maxFormulaPeaks);
			cache = new SparseDoubleMatrix3D(numPeaks, numFormulae, maxFormulaPeaks);
			this.defaultPosition = defaultPosition;
			this.defaultValue = defaultValue;
			this.cached = new boolean[numPeaks];
			this.nz = new NonZeros[numPeaks];
			//cache = new FlexCompRowMatrix[numPeaks];
			//for (int i = 0; i < cache.length; ++i) {
			//	cache[i] = new FlexCompRowMatrix(numFormulae, maxFormulaPeaks);
			//}
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
		/*
		public int nnz(final int peak) {
			
		}
		*/
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
			//return cache[peak].get(formula, position);
		}
		
		public DoubleMatrix1D getRow(final int peak, final int formula) {
			return get(peak).viewRow(formula);
			//return cache[peak].getRow(formula);
		}
		
		public void set(final int peak, final int formula, final int position, final double value) {
			cache.set(peak, formula, position, value);
			cached[peak] = false;
			
			//cache[peak].set(formula, position, value);
			//if ( value == 0.0 ) {
			//	cache[peak].getRow(formula).compact();
			//}
		}
		
		public void add(final int peak, final int formula, final int position, final double value) {
			if ( value == 0.0 ) {
				return;
			}
			cache.set(peak, formula, position, cache.get(peak, formula, position) + value);
			cached[peak] = false;
			//cache[peak].add(formula, position, value);
		}
		
		public int[] cacheSize() {
			final int[] retval = new int[cache.slices()];
			for (int i = 0; i < cache.slices(); ++i) {
				retval[i] = get(i).cardinality();
				
				//retval[i] = ShapeCorrelations.nonZeros(cache[i]);
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
