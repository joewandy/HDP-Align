package mzmatch.ipeak.sort;

import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mzmatch.ipeak.sort.AnnotationSampleHandler.IdentifyingAnnotationSampleHandler.MoleculeIsotope;
import mzmatch.ipeak.util.Common;
import mzmatch.ipeak.util.GeneralMassSpectrum;
import mzmatch.ipeak.util.GeneralMassSpectrumDatabase;
import peakml.chemistry.Polarity;

public abstract class IdentifyingSampleHandler implements SampleHandler<MoleculeData, MoleculeClustering> {
	final int[] totalSupport;
	final List<String> moleculeNames;
	final GeneralMassSpectrumDatabase theoreticalSpectrums;
	int numSamples;
	final static DecimalFormat format = new DecimalFormat("0.0###");

	
	public IdentifyingSampleHandler(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums) {
		totalSupport = new int[moleculeNames.size()];
		this.moleculeNames = moleculeNames;
		this.theoreticalSpectrums = theoreticalSpectrums;

	}
	
	public String output(final int oldFormula) {
		final String name = moleculeNames.get(oldFormula);
		final int newFormula = theoreticalSpectrums.getIndex(name);
		if ( newFormula == -1 ) {
			return "0.0";
		}
		return format.format((double)totalSupport[newFormula] / numSamples);
		
		//return Double.toString((double)totalSupport[newFormula] / numSamples);
		//return Integer.toString(totalSupport[newFormula]);
	}
	
	public static class IdentifyNthLikelyPeaks extends IdentifyingSampleHandler {
		private final Polarity[] polarities;
		private final String[] adducts;
		private final int neededSupport;
		final boolean alternativeSampleHandler;
		private final Map<MoleculeIsotope,Integer>[] peakCounts;

		public IdentifyNthLikelyPeaks(final List<String> moleculeNames, final GeneralMassSpectrumDatabase theoreticalSpectrums,
				final Polarity[] polarities, final String[] adducts, final int neededSupport,
				boolean alternativeSampleHandler) {
			super(moleculeNames, theoreticalSpectrums);
			this.polarities = polarities;
			this.adducts = adducts;
			this.neededSupport = neededSupport;
			this.alternativeSampleHandler = alternativeSampleHandler;
			this.peakCounts = (Map<MoleculeIsotope,Integer>[])Array.newInstance(Map.class, polarities.length);
			for ( int peak = 0; peak < peakCounts.length; ++peak ) {
				this.peakCounts[peak] = new HashMap<MoleculeIsotope,Integer>();
			}
		}
		
		public int[] getFormulaSupport(final MoleculeClustering clustering) {
			final int[] formulaSupport = new int[theoreticalSpectrums.size()];
			
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
				final int peakPosition = clustering.getPosition(peak);
				final int molecule = clustering.getPeakMolecule(peak);
				final Map<String,Integer> adductPositionMap = adductPositionMaps.get(molecule);
				for ( int addIndex = 0; addIndex < adducts.length; ++addIndex) {
					final String adduct = adducts[addIndex];
					final int rankNeeded = adductPositionMap.get(adduct);
					final List<Integer> rankedPositions = theoreticalSpectrums.getRanks(molecule, peakPolarity, addIndex);
					if ( rankedPositions.size() < rankNeeded + 1 ) {
						continue;
					}
					final int highestTheoreticalPeak = rankedPositions.get(rankNeeded);
					if ( highestTheoreticalPeak == -1 ) {
						continue;
					}
					if ( peakPosition == highestTheoreticalPeak ) {
						formulaSupport[molecule]++;
						adductPositionMap.put(adduct, rankNeeded + 1);
						break;
					}
				}
			}
			return formulaSupport;
		}
		
		public int[] getFormulaSupport2(final MoleculeClustering clustering) {
			final int[] formulaSupport = new int[theoreticalSpectrums.size()];
			
			final List<Integer>[][][] indexMap = (List<Integer>[][][])Array.newInstance(List.class, theoreticalSpectrums.size(), 2, adducts.length);
			final List<Integer>[][][] peakMap = (List<Integer>[][][])Array.newInstance(List.class, theoreticalSpectrums.size(), 2, adducts.length);
			for ( int specIndex = 0; specIndex < theoreticalSpectrums.size(); ++specIndex) {
				for ( int polIndex = 0; polIndex < 2; ++polIndex) {
					for ( int addIndex = 0; addIndex < adducts.length; ++addIndex) {
						indexMap[specIndex][polIndex][addIndex] = new ArrayList<Integer>();
						peakMap[specIndex][polIndex][addIndex] = new ArrayList<Integer>();
					}
				}
			}
			
			for (int peak = 0; peak < clustering.numberOfPeaks(); ++peak) {
				final Polarity peakPolarity = polarities[peak];
				final int peakPosition = clustering.getPosition(peak);
				final int molecule = clustering.getPeakMolecule(peak);
				final GeneralMassSpectrum gms = theoreticalSpectrums.get(molecule);
				
				if ( gms.hasValue(peakPosition) ) {
					final int adduct = gms.getAdduct(peakPosition);
					indexMap[molecule][polarityIndex(peakPolarity)][adduct].add(peakPosition);
					peakMap[molecule][polarityIndex(peakPolarity)][adduct].add(peak);
				}
			}
			
			final Polarity[] twoPols = { Polarity.POSITIVE, Polarity.NEGATIVE };
			
			for ( int m = 0; m < theoreticalSpectrums.size(); ++m ) {
				for ( int addIndex = 0; addIndex < adducts.length; ++addIndex) {
					for ( Polarity p : twoPols ) {
						final List<Integer> rankedPositions = theoreticalSpectrums.getRanks(m, p, addIndex);
						for ( int pos : rankedPositions ) {
							if ( ! indexMap[m][polarityIndex(p)][addIndex].contains(pos) ) {
								break;
							}
							formulaSupport[m]++;
						}
					}
				}
			}

			return formulaSupport;
		}
		
		public int[] getFormulaSupport3(final MoleculeClustering clustering) {
			final int[] formulaSupport = new int[theoreticalSpectrums.size()];
			final List<List<Integer>> posCovered = new ArrayList<List<Integer>>();
			for ( int i = 0; i < clustering.numberOfMolecules(); ++i ) {
				posCovered.add(new ArrayList<Integer>());
			}
			
			for (int peak = 0; peak < clustering.numberOfPeaks(); ++peak) {
				if ( clustering.junk[peak] ) {
					continue;
				}
				final int m = clustering.getPeakMolecule(peak);
				final int pos = clustering.getPosition(peak);

				if ( posCovered.get(m).contains(pos) ) {
					continue;
				}
				formulaSupport[m]++;
				posCovered.get(m).add(pos);
			}
			return formulaSupport;
		}
		
		public void handleSample(final MoleculeClustering clustering) {
			numSamples++;
			int[] formulaSupport = getFormulaSupport3(clustering);
			/*
			if ( neededSupport == 1 ) {
				System.err.println("Total support: " + Common.sum(formulaSupport));
			}
			*/

			for ( int formula = 0; formula < formulaSupport.length; ++formula) {
				if ( formulaSupport[formula] >= neededSupport ) {
					totalSupport[formula]++;
				}
			}
		}

		private int polarityIndex(final Polarity polarity) {
			return polarity == Polarity.POSITIVE ? 0 : 1;
		}
	}
}
