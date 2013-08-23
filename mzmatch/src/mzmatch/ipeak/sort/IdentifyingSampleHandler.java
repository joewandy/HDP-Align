package mzmatch.ipeak.sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mzmatch.ipeak.util.GeneralMassSpectrumDatabase;
import peakml.chemistry.Polarity;

public  abstract class IdentifyingSampleHandler implements SampleHandler<FormulaData, FormulaClustering> {
	final int[] totalSupport;
	final List<String> moleculeNames;
	final GeneralMassSpectrumDatabase theoreticalSpectrums;
	int numSamples;
	
	public IdentifyingSampleHandler(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums) {
		totalSupport = new int[moleculeNames.size()];
		this.moleculeNames = moleculeNames;
		this.theoreticalSpectrums = theoreticalSpectrums;
	}
	

	
	

	/*
	private static int getPeakRank(final GeneralMassSpectrum spectrum, final Polarity peakPolarity, final String adduct,
			final int peakRank) {
		final List<Integer> positions = new ArrayList<Integer>();
		final List<Double> sortedDecending = sortPeaks(spectrum, peakPolarity, adduct, positions);
		if ( sortedDecending.isEmpty() ) {
			return -1;
		}
		final double intensityNeeded = sortedDecending.get(peakRank);
		final List<Double> dist = spectrum.getDistribution();
		for ( int i : positions ) {
			if ( intensityNeeded == dist.get(i) ) {
				return i;
			}
		}
		return -1;
		
		assert spectrum.getDistribution().indexOf(intensityNeeded) == spectrum.getDistribution().lastIndexOf(intensityNeeded) :
			"intensityNeeded: " + intensityNeeded + " first: " + spectrum.getDistribution().indexOf(intensityNeeded) + " last: " +
			spectrum.getDistribution().lastIndexOf(intensityNeeded);
		return spectrum.getDistribution().indexOf(intensityNeeded);
		
	}
	*/
	
	public String output(final int oldFormula) {
		//final double proportion = (double)totalSupport[formula] / (double)numSamples;
		//return Double.toString(proportion);
		final String name = moleculeNames.get(oldFormula);
		final int newFormula = theoreticalSpectrums.getIndex(name);
		if ( newFormula == -1 ) {
			return "0";
		}
		return Integer.toString(totalSupport[newFormula]);
	}
	/*
	public static class IdentifyMostLikelyPeak extends IdentifyingSampleHandler {
		private final Polarity[] polarities;
		private final String[] adducts;
		
		public IdentifyMostLikelyPeak(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums,
				final Polarity[] polarities, final String[] adducts) {
			super(moleculeNames, theoreticalSpectrums);
			this.polarities = polarities;
			this.adducts = adducts;
		}
		
		public int[] getFormulaSupport(final FormulaClustering clustering, final int peakRank) {
			final int[] formulaSupport = new int[moleculeNames.size()];
			final int[] sumFormulaSupport = new int[moleculeNames.size()];
			final int[] nonDefaultSupport = new int[moleculeNames.size()];
			
			for (int peak = 0; peak < clustering.numberOfPeaks(); ++peak) {
				final Polarity peakPolarity = polarities[peak];
				//final int cluster = clustering.getCluster(peak);
				//final int formula = clustering.getFormula(i);
				final int formula = clustering.getPeakFormula(peak);
				sumFormulaSupport[formula]++;
				final GeneralMassSpectrum spectrum = theoreticalSpectrums.get(formula);
				for ( String adduct : adducts) {
					final int highestTheoreticalPeak = getPeakRank(spectrum, peakPolarity, adduct, peakRank);
					if ( highestTheoreticalPeak == -1 ) {
						continue;
					}
					final int peakPosition = clustering.getPosition(peak);
					if ( peakPosition != spectrum.size() ) {
						nonDefaultSupport[formula]++;
					}
					
					if ( peakPosition == highestTheoreticalPeak ) {
						formulaSupport[formula] = 1;
						break;
					}
				}
			}
			return formulaSupport;
		}
		
		public void handleSample(final FormulaClustering clustering) {
			numSamples++;
			final int[] formulaSupport = getFormulaSupport(clustering, 0);

			//System.err.println(Arrays.toString(sumFormulaSupport));
			//System.err.println(Arrays.toString(formulaSupport));
			for ( int formula = 0; formula < formulaSupport.length; ++formula) {
				totalSupport[formula] += formulaSupport[formula];
			}
		}
		
		public String output(final int formula) {
			final double proportion = (double)totalSupport[formula] / (double)numSamples;
			return Double.toString(proportion);
		}
		
	}
	*/
	/*
	public static class IdentifyMostAndNextLikelyPeak extends IdentifyMostLikelyPeak {
		
		public IdentifyMostAndNextLikelyPeak(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums,
				final Polarity[] polarities, final String[] adducts) {
			super(moleculeNames, theoreticalSpectrums, polarities, adducts);
		}
		
		public void handleSample(final FormulaClustering clustering) {
			numSamples++;
			final int[] mlSupport = getFormulaSupport(clustering, 0);
			final int[] nlSupport = getFormulaSupport(clustering, 1);
			//System.err.println(Arrays.toString(sumFormulaSupport));
			//System.err.println(Arrays.toString(nonDefaultSupport));
			for ( int formula = 0; formula < mlSupport.length; ++formula) {
				totalSupport[formula] += mlSupport[formula] == 1 && nlSupport[formula] == 1 ? 1 : 0;
				//totalSupport[formula] += nlSupport[formula];
			}
		}
	}

	public static class IdentifyOnePeak extends IdentifyingSampleHandler {
		static double cutOff = 0.05;
		
		public IdentifyOnePeak(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums) {
			super(moleculeNames, theoreticalSpectrums);
		}
		
		public void handleSample(final FormulaClustering clustering) {
			numSamples++;
			final int[] formulaSupport = new int[moleculeNames.size()];

			for ( int i = 0; i < clustering.numberOfClusters(); ++i ) {
				final int formula = clustering.getFormula(i);
				formulaSupport[formula] = 1;
			}
			for ( int formula = 0; formula < formulaSupport.length; ++formula) {
				totalSupport[formula] += formulaSupport[formula];
			}
		}
		
		public String output(final int formula) {
			final double proportion = (double)totalSupport[formula] / (double)numSamples;
			return Double.toString(proportion);
		}
		
	}
	*/
	
	public static class IdentifyNthLikelyPeaks extends IdentifyingSampleHandler {
		private final Polarity[] polarities;
		private final String[] adducts;
		private final int neededSupport;
		
		//private final List<Integer>[][][] indexMap;
		
		
		public IdentifyNthLikelyPeaks(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums,
				final Polarity[] polarities, final String[] adducts, final int neededSupport) {
			super(moleculeNames, theoreticalSpectrums);
			this.polarities = polarities;
			this.adducts = adducts;
			this.neededSupport = neededSupport;
			//indexMap = (List<Integer>[][][])Array.newInstance(List.class, theoreticalSpectrums.size(), 2, adducts.length);
			//calculatePeakRanks();
		}
		
		public int[] getFormulaSupport(final FormulaClustering clustering) {
			final int[] formulaSupport = new int[theoreticalSpectrums.size()];
			//final int[] sumFormulaSupport = new int[moleculeNames.size()];
			//final int[] nonDefaultSupport = new int[moleculeNames.size()];
			
			final List<Map<String,Integer>> adductPositionMaps = new ArrayList<Map<String,Integer>>();
			for ( int i = 0; i < formulaSupport.length; ++i) {
				final Map<String,Integer> adductPositionMap = new HashMap<String,Integer>();
				for ( String adduct : adducts ) {
					adductPositionMap.put(adduct, 0);
				}
				adductPositionMaps.add(adductPositionMap);
			}
			
			for (int peak = 0; peak < clustering.numberOfPeaks(); ++peak) {
				final Polarity peakPolarity = polarities[peak];
				//final int pIndex = polarityIndex(peakPolarity);
				final int peakPosition = clustering.getPosition(peak);
				final int formula = clustering.getPeakFormula(peak);
				final Map<String,Integer> adductPositionMap = adductPositionMaps.get(formula);
				//sumFormulaSupport[formula]++;
				//final GeneralMassSpectrum spectrum = theoreticalSpectrums.get(formula);
				for ( int addIndex = 0; addIndex < adducts.length; ++addIndex) {
					final String adduct = adducts[addIndex];
					final int rankNeeded = adductPositionMap.get(adduct);
					final List<Integer> rankedPositions = theoreticalSpectrums.getRanks(formula, peakPolarity, addIndex);
					//final List<Integer> rankedPositions = indexMap[formula][pIndex][addIndex];
					if ( rankedPositions.size() < rankNeeded + 1 ) {
						continue;
					}
					final int highestTheoreticalPeak = rankedPositions.get(rankNeeded);
					//final int highestTheoreticalPeak = getPeakRank(spectrum, peakPolarity, adduct, rankNeeded);
					/*
					if ( peak == 133 ) {
					System.err.println("Peak: " + peak + " peakPosition: " + peakPosition + " adduct: " + adduct +
							" rankNeeded: " + rankNeeded + " highestTheoreticalPeak: " + highestTheoreticalPeak +
							" peakPolarity: " + peakPolarity + " formula: " + formula);
					}
					*/
					if ( highestTheoreticalPeak == -1 ) {
						continue;
					}
					
					/*
					if ( peakPosition != spectrum.size() ) {
						nonDefaultSupport[formula]++;
					}
					*/
					if ( peakPosition == highestTheoreticalPeak ) {
						formulaSupport[formula]++;
						adductPositionMap.put(adduct, rankNeeded + 1);
						break;
					}
				}
			}
			//assert false;
			return formulaSupport;
		}
		
		public void handleSample(final FormulaClustering clustering) {
			numSamples++;
			final int[] formulaSupport = getFormulaSupport(clustering);

			for ( int formula = 0; formula < formulaSupport.length; ++formula) {
				if ( formulaSupport[formula] >= neededSupport ) {
					totalSupport[formula]++;
				}
			}
		}

		
		
		

	}
}

