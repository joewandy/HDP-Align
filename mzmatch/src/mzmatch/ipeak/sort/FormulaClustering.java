package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mzmatch.ipeak.sort.Clusterer.LikelihoodScorer;
import mzmatch.ipeak.sort.FormulaClusterer.MassIntensityClusteringScorer;
import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import peakml.chemistry.Polarity;

public class FormulaClustering extends AbstractClustering<FormulaData> {
	private final List<Integer> clusterFormulas;
	private final int[] peakPositions;
	private final int[] peakFormulas;
	private List<Integer>[] formulaClusters;
	private boolean fcCached = false;
	private final MassIntensityClusteringScorer scorer;
	private final FormulaData data;
	//private final CorrelationMeasure measure;
	
	public static FormulaClustering createFormulaClustering(final Random random,
			final FormulaData data, final FormulaParameters parameters, final LikelihoodScorer inScorer,
			final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, FormulaClusterer.MassIntensityClusteringScorer miScorer) {
		final int[] peakClustering = new int[data.numPeaksets];
		final List<Integer> clusterFormulas = new ArrayList<Integer>();
		final int[] peakPositions = new int[data.numPeaksets];
		final int[] peakFormulas = new int[data.numPeaksets];
		@SuppressWarnings("unchecked")
        final List<Integer>[] formulaClusters = (List<Integer>[])Array.newInstance(List.class, data.numFormulae);
        for ( int i = 0; i < formulaClusters.length; ++i ) {
               formulaClusters[i] = new ArrayList<Integer>();
        }
		final GeneralMassSpectrum RoNan = GeneralMassSpectrum.spectrumFromNumAdducts(adducts.length);
		data.theoreticalSpectrums.add(RoNan);
		
		// So called smart clustering
		for (int i = 0; i < data.numPeaksets; ++i) {
			double massSum = 0.0;
			int numberSummed = 0;
			for (int j = 0; j < data.numReplicates; ++j) {
				if ( ! Double.isNaN(data.masses[j][i]) ) {
					massSum += Math.exp(data.masses[j][i]);
					numberSummed++;
				}
			}
			assert ! Double.isNaN(massSum);
			double avgMass = massSum / numberSummed;
			double minDiff = Double.MAX_VALUE;
			int msIndex = 0;
			int peakIndex = -1;
			
			//System.err.println("Here: " + avgMass);
			for ( int j = 0; j < data.numFormulae; ++j ) {
				GeneralMassSpectrum ms = data.theoreticalSpectrums.get(j);
				final List<Double> theoreticalMasses = ms.getMasses();
				for ( int k = 0; k < theoreticalMasses.size(); ++k ) {
					final Polarity theoreticalPolarity = ms.getPolarity(k);
					final Polarity measuredPolarity = data.polarities[i];
					
					if ( theoreticalPolarity != measuredPolarity ) {
						continue;
					}
					
					final double mass = theoreticalMasses.get(k);
					//System.err.println("theo: " + theoreticalPolarity + " measured: " + measuredPolarity + " mass: " + mass);
					final double sqDiff = Math.pow(mass - avgMass, 2);
					if ( sqDiff < minDiff) {
						minDiff = sqDiff;
						msIndex = j;
						peakIndex = k;
					}
				}
			}
			assert peakIndex != -1 : "size: " + data.numFormulae;
			
			int cluster = clusterFormulas.size();
			peakClustering[i] = cluster;
			peakPositions[i] = peakIndex;
			peakFormulas[i] = msIndex;
			clusterFormulas.add(msIndex);
			formulaClusters[msIndex].add(cluster);
/*
			// Check to see if we have used this formula before
			boolean usedFormula = false;
			int cluster = clusterFormulas.size();
			for (int j = 0; j < clusterFormulas.size(); ++j ) {
				final int formula = clusterFormulas.get(j);
				if ( formula == msIndex ) {
					usedFormula = true;
					cluster = j;
					break;
				}
			}
			peakClustering[i] = cluster;
			peakPositions[i] = peakIndex;
			peakFormulas[i] = msIndex;
			if ( ! usedFormula ) {
				clusterFormulas.add(msIndex);
			}
			*/
		}
		return new FormulaClustering(random, peakClustering, clusterFormulas, peakPositions, peakFormulas, formulaClusters, data,
				parameters, inScorer, outScorer, measure, adducts, miScorer);
	}

	private FormulaClustering(final Random random, final int[] initialClustering, final List<Integer> clusterFormulas,
			final int[] peakPositions, final int[] peakFormulas, final List<Integer>[] formulaClusters, final FormulaData data,
			final FormulaParameters parameters, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, FormulaClusterer.MassIntensityClusteringScorer miScorer) {
		super(initialClustering, data, measure);
		this.clusterFormulas = clusterFormulas;
		this.peakPositions = peakPositions;
		this.peakFormulas = peakFormulas;
		this.formulaClusters = formulaClusters;
		this.data = data;
		this.scorer = miScorer;

		//scorer = new MassIntensityClusteringScorer(data, parameters,
		//		inScorer, outScorer, measure, adducts, random);
	}
	
	public int[] getFormulas() {
		return peakFormulas;
	}
	
	public int getFormula(final int cluster) {
		assert cluster >= 0 && cluster < numberOfClusters() : "cluster: " + cluster + " numberOfClusters: " + numberOfClusters();
		return clusterFormulas.get(cluster);
	}
	
	public int getPeakFormula(final int peak) {
		return peakFormulas[peak];
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
	public void doClusterPositionStep() {
		scorer.doClusterPositionStep(this);
	}
	
	@Override
	public void setCluster(final int peak, final int cluster) {
		final int oldCluster = peakClustering[peak];
		final int oldNumberOfClusters = numberOfClusters();
		assert cluster <= oldNumberOfClusters;
		super.setCluster(peak, cluster);
		
		final int newCluster = getCluster(peak);
		assert newCluster <= oldNumberOfClusters;
		final int newNumberOfClusters = numberOfClusters();

		if ( newNumberOfClusters < oldNumberOfClusters ) {
			assert newNumberOfClusters == oldNumberOfClusters - 1;
			// One of the clusters has been deleted
			assert clusterFormulas.size() == oldNumberOfClusters;
			clusterFormulas.remove(oldCluster);
			fcCached = false;
			//rebuildFormulaClusters();
			/*
			formulaClusters[formula].remove(Integer.valueOf(oldCluster));
			for ( int f = 0; f < numberOfFormulae(); ++f ) {
				for ( int i = 0; i < formulaClusters[f].size(); ++i ) {
					final int c = formulaClusters[f].get(i);
					assert c < oldNumberOfClusters;
					if ( c > oldCluster ) {
						formulaClusters[f].set(i, c - 1);
					}
					assert formulaClusters[f].get(i) < newNumberOfClusters;
				}
			}
			
			*/
			assert clusterFormulas.size() == newNumberOfClusters;
		} else if ( newNumberOfClusters > oldNumberOfClusters ) {
			assert newNumberOfClusters == oldNumberOfClusters + 1;
			// This is a new cluster, sample a new formula
			final int newFormula = scorer.pickFormula(peak, this);
			setFormula(newCluster, newFormula);
			assert clusterFormulas.size() == newNumberOfClusters;
		}
		// Now sample position
		final int clusterFormula = getFormula(newCluster);
		peakFormulas[peak] = clusterFormula;
		final Sample<Integer> sample = scorer.pickPeak(peak, this);
		final int theoreticalPeak = sample.sample();
		assert theoreticalPeak >= 0 : theoreticalPeak;
		setPosition(peak, theoreticalPeak);
	}
	@SuppressWarnings("unchecked")
	private void rebuildFormulaClusters() {
        formulaClusters = (List<Integer>[])Array.newInstance(List.class, data.numFormulae);
        for ( int i = 0; i < formulaClusters.length; ++i ) {
              formulaClusters[i] = new ArrayList<Integer>();
        }
		for ( int c = 0; c < clusterFormulas.size(); ++c ) {
			final int f = clusterFormulas.get(c);
			formulaClusters[f].add(c);
		}
		fcCached = true;
	}
	
	private void setFormula(final int cluster, final int newFormula) {
		//assert cluster <= clusterFormulas.size() + 1;
		if ( cluster > clusterFormulas.size() - 1 ) {
			// An extra cluster has been added
			assert cluster == clusterFormulas.size();
			clusterFormulas.add(newFormula);
		} else {
			clusterFormulas.set(cluster, newFormula);
		}
		//formulaClusters[newFormula].add(cluster);
		fcCached = false;
		//rebuildFormulaClusters();
		for ( int peak : clusterPeaks.get(cluster) ) {
			peakFormulas[peak] = newFormula;
		}
	}
	
	public void setFormula(final int cluster, final int newFormula, final List<Integer> peaks, final List<Integer> positions) {
		setFormula(cluster, newFormula);
		for ( int i = 0; i < peaks.size(); ++i ) {
			final int peak = peaks.get(i);
			final int position = positions.get(i);
			//System.err.println("Peak: " + peak + " position: " + position);
			peakPositions[peak] = position;
		}
	}
	
	public void setPosition(final int peak, final int theoreticalPeak) {
		//System.err.println("Setting peak " + peak + " to position " + theoreticalPeak);
		assert theoreticalPeak >= 0 : theoreticalPeak;
		peakPositions[peak] = theoreticalPeak;
	}
	
	public int getPosition(final int peak) {
		return peakPositions[peak];
	}
	
	public int[] getPeakPositions() {
		return peakPositions;
	}
	
	public List<Integer> getClusterPositions(final int cluster) {
		final List<Integer> peaks = getClusterPeaks(cluster);
		final List<Integer> positions = new ArrayList<Integer>();
		for ( int i = 0; i < clusterSize(cluster); ++i ) {
			final int peak = peaks.get(i);
			positions.add(getPosition(peak));
		}
		return positions;
	}
	
	public int numberOfFormulae() {
		return data.numFormulae;
	}
	
	public List<Integer> getClustersFromFormula(final int formula) {
		if ( ! fcCached ) {
			rebuildFormulaClusters();
		}
		return formulaClusters[formula];
	}
	
	@Override
	public String toString() {
		final List<List<Object>> debugData = getDebugData(false);
		final StringBuilder builder = new StringBuilder();
		for ( int peak = 0; peak < peakClustering.length; ++peak) {
			builder.append(String.format("Peak: %d intensity: %f rt: %f mass: %f theo mass: %s cluster: %d formula: %d " +
				"adduct: %d postion: %d beta_mai: %f corrs: %s rtDiff: %s", debugData.get(peak).toArray()));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	@Override
	public String toCSVString(final int sampleNumber) {
		final List<List<Object>> debugData = getDebugData(true);
		final StringBuilder builder = new StringBuilder();
		for ( int peak = 0; peak < peakClustering.length; ++peak) {
			final List<Object> d = debugData.get(peak);
			d.add(0, sampleNumber);
			builder.append(String.format("%d,%d,%f,%f,%f,%s,%d,%d,%d,%d,%f", d.toArray()));
			builder.append("\n");
		}
		return builder.toString();
	}
	
	@Override
	public String columnNames() {
		return "sample,peak,intensity,rt,mass,theoMass,cluster,formula,adduct,position,betaMai";
	}
/*

	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for ( int peak = 0; peak < peakClustering.length; ++peak) {
			final int formula = peakFormulas[peak];
			final GeneralMassSpectrum spectrum = data.theoreticalSpectrums.get(formula);
			final int position = peakPositions[peak];
			final String theoreticalMass = position == scorer.getDefaultPosition() ? "background" : Double.toString(Math.log(spectrum.getMass(position)));
			final int cluster = peakClustering[peak];
			
			builder.append("Peak: " + peak);
			builder.append(" intensity: " + toString(data.intensities, peak));
			builder.append(" rt: " + averageRt(peak));
			builder.append(" mass: " +  toString(data.masses, peak));
			builder.append(" theo mass: " + theoreticalMass);
			builder.append(" cluster: " + cluster);
			builder.append(" formula: " + formula);
			builder.append(" position: " + position);
			builder.append(" beta_mai: " + scorer.beta_mai[formula][position]);
			builder.append(" corrs: " + correlations(peak));
			builder.append(" rtDiff: " + rtDiff(peak));
			builder.append("\n");
		}
		return builder.toString();
	}
	*/
	public List<List<Object>> getDebugData(final boolean numerical) {
		final List<List<Object>> debugData = new ArrayList<List<Object>>();
		System.err.println("Getting debug data");
		for ( int peak = 0; peak < peakClustering.length; ++peak) {
			final List<Object> dataRow = new ArrayList<Object>();

			final int formula = peakFormulas[peak];
			final GeneralMassSpectrum spectrum = data.theoreticalSpectrums.get(formula);
			final int position = peakPositions[peak];
			final String theoreticalMass = position == scorer.getDefaultPosition() ? ( numerical ? "NaN" : "background") : Double.toString(Math.log(spectrum.getMass(position)));
			final int cluster = peakClustering[peak];
			//final String formulaString = data.theoreticalSpectrums.getName(formula);
			final int adduct = position == scorer.getDefaultPosition() ? -1 : spectrum.getAdduct(position);
			
			dataRow.add(peak);
			dataRow.add(averageSlice(data.intensities, peak));
			dataRow.add(averageSlice(data.retentionTimes, peak));
			dataRow.add(averageSlice(data.masses, peak));
			dataRow.add(theoreticalMass);
			dataRow.add(cluster);
			dataRow.add(formula);
			//dataRow.add(formulaString);
			dataRow.add(adduct);
			dataRow.add(position);
			dataRow.add(scorer.beta_mai[formula][position]);
			dataRow.add(correlations(peak));
			dataRow.add(rtDiff(peak));
			
			debugData.add(dataRow);
		}
		System.err.println("Done");
		return debugData;
	}
	
	public double averageSlice(double[][] array, int secondIndex) {
		final double[] slice = new double[array.length];
		for (int i = 0; i < array.length; ++i) {
			slice[i] = array[i][secondIndex];
		}
		return Common.mean(slice, true);
	}
	
	private String toString(double[][] array, int secondIndex) {
		final double[] slice = new double[array.length];
		for (int i = 0; i < array.length; ++i) {
			slice[i] = array[i][secondIndex];
		}
		return Arrays.toString(slice);
	}
	

	/*
	private static double firstNonNaN(double[][] array, final int peak) {
		for (int i = 0; i < array.length; ++i) {
			if ( ! Double.isNaN(array[i][peak]) ) {
				return array[i][peak];
			}
		}
		return Double.NaN;
	}
	*/
}