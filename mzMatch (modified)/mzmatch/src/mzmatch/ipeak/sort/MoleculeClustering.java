package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import mzmatch.ipeak.sort.Clusterer.LikelihoodScorer;
import mzmatch.ipeak.sort.FormulaClusterer.MassIntensityClusteringScorer;
import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import peakml.chemistry.Polarity;

public class MoleculeClustering extends AbstractClustering<MoleculeData> {
	private final List<Integer> clusterMolecules;
	private final int[] peakPositions;
	private final int[] peakMolecules;
	private List<Integer>[] moleculeClusters;
	private boolean fcCached = false;
	private final MassIntensityClusteringScorer scorer;
	public final MoleculeData data;
	private final String[] adducts;
	public final boolean[] junk;
	//private final CorrelationMeasure measure;
	
	public static enum InitialClusteringMethod {
		SinglePeakClusters, PregroupedClusters, FixedClusters
	}
	
	public static MoleculeClustering createMoleculeClustering(final Random random,
			final MoleculeData data, final FormulaParameters parameters, final LikelihoodScorer inScorer,
			final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, FormulaClusterer.MassIntensityClusteringScorer miScorer) {
		final int[] peakClustering = new int[data.numPeaksets];
		final List<Integer> clusterMolecules = new ArrayList<Integer>();
		final int[] peakPositions = new int[data.numPeaksets];
		final int[] peakMolecules = new int[data.numPeaksets];
		@SuppressWarnings("unchecked")
        final List<Integer>[] moleculeClusters = (List<Integer>[])Array.newInstance(List.class, data.numMolecules);
        for ( int i = 0; i < moleculeClusters.length; ++i ) {
               moleculeClusters[i] = new ArrayList<Integer>();
        }
		if ( parameters.options.clusteringMethod == InitialClusteringMethod.FixedClusters ) {
			for ( int i = 0; i < parameters.options.fixedClustersSize; ++i ) {
				final int rMolecule = random.nextInt(data.numMolecules);
				clusterMolecules.add(rMolecule);
				moleculeClusters[rMolecule].add(i);
			}
		}
		//final GeneralMassSpectrum RoNan = GeneralMassSpectrum.spectrumFromNumAdducts(adducts.length);
		//data.theoreticalSpectrums.add(RoNan);
		
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
			for ( int j = 0; j < data.numMolecules; ++j ) {
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
			assert peakIndex != -1 : "size: " + data.numMolecules;
			
			int cluster = -1;
			//peakPositions[i] = peakIndex;
			//peakMolecules[i] = msIndex;
			
			
			switch ( parameters.options.clusteringMethod ) {
			case SinglePeakClusters:
				cluster = clusterMolecules.size();
				peakPositions[i] = peakIndex;
				peakMolecules[i] = msIndex;
				//System.err.println("peak: " + i + " molecule: " + data.theoreticalSpectrums.getName(msIndex));
				clusterMolecules.add(msIndex);
				moleculeClusters[msIndex].add(cluster);
				break;
			case PregroupedClusters:
				if ( ! moleculeClusters[msIndex].isEmpty() ) {
					cluster = moleculeClusters[msIndex].get(0);
				} else {
					cluster = clusterMolecules.size();
					clusterMolecules.add(msIndex);
					moleculeClusters[msIndex].add(cluster);
				}
				peakPositions[i] = peakIndex;
				peakMolecules[i] = msIndex;
				break;
			case FixedClusters:
				cluster = random.nextInt(parameters.options.fixedClustersSize);
				final int molecule = clusterMolecules.get(cluster);
				final int position = data.theoreticalSpectrums.get(molecule).size();
				peakPositions[i] = position;
				peakMolecules[i] = molecule;
				break;
			}
			assert cluster != -1;
			peakClustering[i] = cluster;

			
//			if ( ! moleculeClusters[msIndex].isEmpty() ) {
//				cluster = moleculeClusters[msIndex].get(0);
//			} else {
//				clusterMolecules.add(msIndex);
//			}
//
//			peakClustering[i] = cluster;
//			moleculeClusters[msIndex].add(cluster);
			

			
			
			
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
		return new MoleculeClustering(random, peakClustering, clusterMolecules, peakPositions, peakMolecules, moleculeClusters, data,
				parameters, inScorer, outScorer, measure, adducts, miScorer);
	}

	private MoleculeClustering(final Random random, final int[] initialClustering, final List<Integer> clusterMolecules,
			final int[] peakPositions, final int[] peakMolecules, final List<Integer>[] moleculeClusters, final MoleculeData data,
			final FormulaParameters parameters, final LikelihoodScorer inScorer, final LikelihoodScorer outScorer,
			final CorrelationMeasure measure, final String[] adducts, FormulaClusterer.MassIntensityClusteringScorer miScorer) {
		super(initialClustering, data, measure);
		this.clusterMolecules = clusterMolecules;
		this.peakPositions = peakPositions;
		this.peakMolecules = peakMolecules;
		this.moleculeClusters = moleculeClusters;
		this.data = data;
		this.scorer = miScorer;
		this.adducts = adducts;
		this.junk = new boolean[peakPositions.length];

		//scorer = new MassIntensityClusteringScorer(data, parameters,
		//		inScorer, outScorer, measure, adducts, random);
	}
	
	public int[] getMolecules() {
		return peakMolecules;
	}
	
	public int getMolecule(final int cluster) {
		assert cluster >= 0 && cluster < numberOfClusters() : "cluster: " + cluster + " numberOfClusters: " + numberOfClusters();
		return clusterMolecules.get(cluster);
	}
	
	public int getPeakMolecule(final int peak) {
		assert peakMolecules[peak] == clusterMolecules.get(getCluster(peak));
		
		

		return peakMolecules[peak];
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
			assert clusterMolecules.size() == oldNumberOfClusters;
			clusterMolecules.remove(oldCluster);
			fcCached = false;
			//rebuildMoleculeClusters();
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
			assert clusterMolecules.size() == newNumberOfClusters;
		} else if ( newNumberOfClusters > oldNumberOfClusters ) {
			assert newNumberOfClusters == oldNumberOfClusters + 1;
			// This is a new cluster, sample a new formula
			final int newMolecule = scorer.pickFormula(peak);
			setMolecule(newCluster, newMolecule);
			assert clusterMolecules.size() == newNumberOfClusters;
		}
		// Now sample position
		final int clusterMolecule = getMolecule(newCluster);
		peakMolecules[peak] = clusterMolecule;
		final Sample<Integer> sample = scorer.pickPeak(peak, this);
		final int theoreticalPeak = sample.sample();
		assert theoreticalPeak >= 0 : theoreticalPeak;
		setPosition(peak, theoreticalPeak);
	}
	@SuppressWarnings("unchecked")
	private void rebuildMoleculeClusters() {
        moleculeClusters = (List<Integer>[])Array.newInstance(List.class, data.numMolecules);
        for ( int i = 0; i < moleculeClusters.length; ++i ) {
              moleculeClusters[i] = new ArrayList<Integer>();
        }
		for ( int c = 0; c < clusterMolecules.size(); ++c ) {
			final int f = clusterMolecules.get(c);
			moleculeClusters[f].add(c);
		}
		fcCached = true;
	}
	
	private void setMolecule(final int cluster, final int newMolecule) {
		//assert cluster <= clusterFormulas.size() + 1;
		if ( cluster > clusterMolecules.size() - 1 ) {
			// An extra cluster has been added
			assert cluster == clusterMolecules.size();
			clusterMolecules.add(newMolecule);
		} else {
			clusterMolecules.set(cluster, newMolecule);
		}
		//formulaClusters[newFormula].add(cluster);
		fcCached = false;
		//rebuildFormulaClusters();
		for ( int peak : clusterPeaks.get(cluster) ) {
			peakMolecules[peak] = newMolecule;
		}
	}
	
	public void setMolecule(final int cluster, final int newMolecule, final List<Integer> peaks, final List<Integer> positions) {
		assert newMolecule >= 0 && newMolecule < data.numMolecules;
		setMolecule(cluster, newMolecule);
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
	
	public int numberOfMolecules() {
		return data.numMolecules;
	}
	
	public List<Integer> getClustersFromMolecule(final int formula) {
		if ( ! fcCached ) {
			rebuildMoleculeClusters();
		}
		return moleculeClusters[formula];
	}
	
	public void postProcess() {
		postProcess3();
		return;
		/*
		final Polarity[] twoPols = { Polarity.POSITIVE, Polarity.NEGATIVE };
		Arrays.fill(this.junk, true);
		
		for ( int m = 0; m < data.theoreticalSpectrums.size(); ++m ) {
			final List<Integer> clusters = getClustersFromMolecule(m);
		
			for ( int addIndex = 0; addIndex < this.adducts.length; ++addIndex) {
				for ( Polarity p : twoPols ) {
					final List<Integer> rankedPositions = data.theoreticalSpectrums.getRanks(m, p, addIndex);
					for ( int pos : rankedPositions ) {
						boolean found = false;
						for ( int c : clusters ) {
							for ( int peak : clusterPeaks.get(c) ) {
								if ( getPosition(peak) == pos ) {
									junk[peak] = false;
									found = true;
									//break outside;
								}
							}
						}
						if ( ! found ) {
							break;
						}
					}
				}
			}
		}
		//System.err.println(Common.arrayToString(junk));
		 */
	}
	
	public void postProcess2() {
		final Polarity[] twoPols = { Polarity.POSITIVE, Polarity.NEGATIVE };
		Arrays.fill(this.junk, true);
		
		for ( int m = 0; m < data.theoreticalSpectrums.size(); ++m ) {
			final List<Integer> clusters = getClustersFromMolecule(m);
		
			for ( int addIndex = 0; addIndex < this.adducts.length; ++addIndex) {
				for ( Polarity p : twoPols ) {
					final List<Integer> rankedPositions = data.theoreticalSpectrums.getRanks(m, p, addIndex);
					for ( int c : clusters ) {
						for ( int pos : rankedPositions ) {
							boolean found = false;
					//for ( int pos : rankedPositions ) {
					//	boolean found = false;
					//	for ( int c : clusters ) {
							
							for ( int peak : clusterPeaks.get(c) ) {
								if ( getPosition(peak) == pos ) {
									junk[peak] = false;
									found = true;
									//break outside;
								}
							}
							if ( ! found ) {
								break;
							}
						}
					}
				}
			}
		}
		//System.err.println(Common.arrayToString(junk));
	}
	
	public void postProcess3() {
		final int sizeNeeded = 4;
		final Polarity[] twoPols = { Polarity.POSITIVE, Polarity.NEGATIVE };
		Arrays.fill(this.junk, true);
		
		for ( int m = 0; m < data.theoreticalSpectrums.size(); ++m ) {
			final List<Integer> clusters = getClustersFromMolecule(m);
			final List<Integer> peaksFound = new ArrayList<Integer>();
			for ( int addIndex = 0; addIndex < this.adducts.length; ++addIndex) {
				for ( Polarity p : twoPols ) {
					final List<Integer> rankedPositions = data.theoreticalSpectrums.getRanks(m, p, addIndex);

					
					for ( int pos : rankedPositions ) {
						boolean found = false;
						for ( int c : clusters ) {
							for ( int peak : clusterPeaks.get(c) ) {
								if ( getPosition(peak) == pos ) {
									peaksFound.add(peak);
									//junk[peak] = false;
									found = true;
									//break outside;
								}
							}
						}
						if ( ! found ) {
							break;
						}
					}
				}
			}
			if ( peaksFound.size() >= sizeNeeded ) {
				for ( int p : peaksFound ) {
					junk[p] = false;
				}
			}
		}
		//System.err.println(Common.arrayToString(junk));
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

			final int formula = peakMolecules[peak];
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