package mzmatch.ipeak.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Molecule;
import peakml.chemistry.Polarity;
import peakml.util.Pair;

public class GeneralMassSpectrumDatabase implements Iterable<GeneralMassSpectrum> {
	private final List<GeneralMassSpectrum> theoreticalSpectrums;
	private final String[] adducts;
	private List<Integer>[][][] indexMap;
	private final List<peakml.util.Pair<String, Molecule>> molecules;
	
	public GeneralMassSpectrumDatabase(final List<peakml.util.Pair<String, Molecule>> molecules, final String[] adducts,
			final double minDistributionValue, final int maxValues) {
		theoreticalSpectrums = new ArrayList<GeneralMassSpectrum>();
		this.adducts = adducts;
		this.molecules = molecules;
		for ( Pair<String, Molecule> entry : molecules ) {
			final Molecule m = entry.v2;
			final MolecularFormula formula = m.getFormula();
			try {
				final GeneralMassSpectrum ms = GeneralMassSpectrum.getGeneralMassSpectrum(formula, adducts,
						minDistributionValue, maxValues);
				theoreticalSpectrums.add(ms);
			} catch (RuntimeException e) {
				System.err.println("Molecule: " + m.getName() + " formula: " + formula.toString());
				throw e;
			}
		}
		calculatePeakRanks();
		assert theoreticalSpectrums.size() == molecules.size();
	}
	
	private GeneralMassSpectrumDatabase(final List<GeneralMassSpectrum> spectrums,
			final List<peakml.util.Pair<String, Molecule>> molecules, final String[] adducts,
			final double minDistributionValue, final int maxValues) {
		assert spectrums.size() == molecules.size();
		this.adducts = adducts;
		this.molecules = molecules;
		this.theoreticalSpectrums = spectrums;
		calculatePeakRanks();
	}
	
	public GeneralMassSpectrumDatabase getSubset(final Collection<Integer> subset) {
		final List<GeneralMassSpectrum> selected = new ArrayList<GeneralMassSpectrum>();
		final List<peakml.util.Pair<String, Molecule>> selectedMolecules = new ArrayList<peakml.util.Pair<String, Molecule>>();
		
		for ( int i : subset) {
			selected.add(theoreticalSpectrums.get(i));
			selectedMolecules.add(molecules.get(i));
		}
		return new GeneralMassSpectrumDatabase(selected, selectedMolecules, this.adducts, Double.NaN, -1);
	}

	public boolean add(final GeneralMassSpectrum spectrum) {
		final boolean retval = theoreticalSpectrums.add(spectrum);
		calculatePeakRanks();
		return retval;
	}
	
	public int size() {
		return theoreticalSpectrums.size();
	}

	public Iterator<GeneralMassSpectrum> iterator() {
		return theoreticalSpectrums.iterator();
	}
	
	public int getIndex(final String name) {
		for ( int i = 0; i < molecules.size(); ++i ) {
			if ( molecules.get(i).v1.equals(name) ) {
				return i;
			}
		}
		return -1;
	}
	
	public GeneralMassSpectrum get(final int index) {
		return theoreticalSpectrums.get(index);
	}
	
	public double getRetentionTime(final int index) {
		return molecules.get(index).v2.getRetentionTime();
	}
	
	public MolecularFormula getFormula(final int index) {
		assert molecules.size() == theoreticalSpectrums.size();
		return molecules.get(index).v2.getFormula();
	}
	
	public String getName(final int index) {
		return molecules.get(index).v1;
	}
	
	public String getRealName(final int index) {
		return molecules.get(index).v2.getName();
	}
	
	public List<String> getNames() {
		final List<String> retval = new ArrayList<String>();
		for ( int i = 0; i < molecules.size(); ++i ) {
			retval.add(getName(i));
		}
		return retval;
	}
	
	public List<Integer> getRanks(final int formula, final Polarity polarity, final int adductIndex) {
		return indexMap[formula][polarityIndex(polarity)][adductIndex];
	}
	
	@SuppressWarnings("unchecked")
	private void calculatePeakRanks() {
		indexMap = (List<Integer>[][][])Array.newInstance(List.class, theoreticalSpectrums.size(), 2, adducts.length);
		final Polarity[] polarities = { Polarity.POSITIVE, Polarity.NEGATIVE };
		for ( int specIndex = 0; specIndex < theoreticalSpectrums.size(); ++specIndex) {
			final GeneralMassSpectrum spectrum = theoreticalSpectrums.get(specIndex);
			for ( int polIndex = 0; polIndex < 2; ++polIndex) {
				final Polarity polarity = polarities[polIndex];
				for ( int addIndex = 0; addIndex < adducts.length; ++addIndex) {
					indexMap[specIndex][polIndex][addIndex] = getPeakRanks(spectrum, polarity, addIndex);
				}
			}
		}
	}
	
	private static List<Integer> getPeakRanks(final GeneralMassSpectrum spectrum, final Polarity peakPolarity, final int adduct) {
		final List<Integer> positions = new ArrayList<Integer>();
		final List<Double> sortedDecending = sortPeaks(spectrum, peakPolarity, adduct, positions);
		final List<Integer> retval = new ArrayList<Integer>(); 
		for ( double intensityNeeded : sortedDecending ) {
		//final double intensityNeeded = sortedDecending.get(peakRank);
			final List<Double> dist = spectrum.getDistribution();
			for ( int i : positions ) {
				if ( intensityNeeded == dist.get(i) ) {
					retval.add(i);
				}
			}
		}
		return retval;
	}
	
	private static List<Double> sortPeaks(final GeneralMassSpectrum spectrum, final Polarity peakPolarity, final int adduct,
			final List<Integer> positions) {
		final List<Double> intens = new ArrayList<Double>();
		for (int i = 0; i < spectrum.size(); ++i) {
			final Polarity p = spectrum.getPolarity(i);
			final double inten = spectrum.getDistribution(i);
			final int theoAdduct = spectrum.getAdduct(i);
			if ( p == peakPolarity && theoAdduct == adduct ) {
				intens.add(inten);
				positions.add(i);
			}
		}
		Collections.sort(intens, Collections.reverseOrder());
		return intens;
	}
	
	private int polarityIndex(final Polarity polarity) {
		return polarity == Polarity.POSITIVE ? 0 : 1;
	}
}