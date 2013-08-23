package mzmatch.ipeak.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Polarity;

public class GeneralMassSpectrum {
	//private final MolecularFormula mf;
	//private final List<Double> masses = new ArrayList<Double>();
	private final Map<Double,Integer> indexMap = new HashMap<Double,Integer>();
	private final List<List<Integer>> adductMap;
	//private final Map<String,List<Integer>> adductMap = new HashMap<String,List<Integer>>();
	
	private final List<Double> masses = new ArrayList<Double>();
	private final List<Double> intensities = new ArrayList<Double>();
	private final List<Integer> adducts = new ArrayList<Integer>();
	private final List<GeneralMolecularFormula> isotopicFormulas = new ArrayList<GeneralMolecularFormula>();
	private final List<Polarity> polarities = new ArrayList<Polarity>();
	private final List<Integer> adductIndex = new ArrayList<Integer>();
	
	//private final SortedMap<Double,Double> intensities = new TreeMap<Double,Double>();
	//private final SortedMap<Double,String> adducts = new TreeMap<Double,String>();
	//private final SortedMap<Double,GeneralMolecularFormula> isotopicFormulas = new TreeMap<Double,GeneralMolecularFormula>();
	private final int maxEntries;
	/*
	public GeneralMassSpectrum(final int numAdducts) {
		this(Integer.MAX_VALUE, numAdducts);
	}
	*/
	public static GeneralMassSpectrum spectrumFromNumAdducts(final int numAdducts) {
		return new GeneralMassSpectrum(Integer.MAX_VALUE, numAdducts);
	}
	/*
	public GeneralMassSpectrum(final MolecularFormula mf) {
		this(mf, Integer.MAX_VALUE);
	}
	*/
	public GeneralMassSpectrum(final int maxEntries, final int numAdducts) {
		this.maxEntries = maxEntries;
		adductMap = new ArrayList<List<Integer>>(numAdducts);
		adductMap.addAll(Collections.nCopies(numAdducts, (List<Integer>)null));
	}
	/*
	public static GeneralMassSpectrum spectrumGivenPolarity(final GeneralMassSpectrum gms, final Polarity givenPolarity) {
		final GeneralMassSpectrum retval = new GeneralMassSpectrum(gms.maxEntries, numAdducts);
		
		final List<Double> masses = gms.getMasses();
		final List<Double> distribution = gms.getDistribution();
		final List<Integer> adducts = gms.getAdducts();
		final List<GeneralMolecularFormula> formulae = gms.getGeneralMolecularFormulas();
		final List<Polarity> polarities = gms.getPolarities();
		
		for (int i = 0; i < masses.size(); ++i) {
			if ( polarities.get(i) == givenPolarity ) {
				retval.add(masses.get(i), distribution.get(i), adducts.get(i), polarities.get(i), formulae.get(i));
			}
		}
		return retval;
	}
	*/
	public static GeneralMassSpectrum getGeneralMassSpectrum(final MolecularFormula mf, final String[] adducts,
			final double minDistributionValue, final int maxValues) {
		final List<GeneralMassSpectrum> spectra = new ArrayList<GeneralMassSpectrum>();
		for (int adductIndex = 0; adductIndex < adducts.length; ++adductIndex ) {
			final GeneralDerivative gd = new GeneralDerivative(mf, adductIndex, false, adducts);
			if ( ! gd.isValid() ) {
				continue;
			}
			final GeneralMassSpectrum gms = gd.getDistribution(minDistributionValue, maxValues);
			spectra.add(gms);
		}
		final GeneralMassSpectrum retval = mergeSpectra(spectra, maxValues, adducts.length);
		return retval;
	}
	
	public static GeneralMassSpectrum mergeSpectra(final List<GeneralMassSpectrum> spectra, final int maxValues, final int numAdducts) {
		final GeneralMassSpectrum spectrum = spectrumFromNumAdducts(numAdducts);
		
		for (GeneralMassSpectrum gms : spectra) {
			final List<Double> masses = gms.getMasses();
			final List<Double> distribution = gms.getDistribution();
			final List<Integer> adducts = gms.getAdducts();
			final List<GeneralMolecularFormula> formulae = gms.getGeneralMolecularFormulas();
			final List<Polarity> polarities = gms.getPolarities();
			
			for (int i = 0; i < masses.size(); ++i) {
				spectrum.add(masses.get(i), distribution.get(i), adducts.get(i), polarities.get(i), formulae.get(i));
			}
		}
		return spectrum;
	}
	
	public void add(final double mass, final double intensity, final int adduct, final Polarity polarity,
			final GeneralMolecularFormula gmf) {
		
		List<Integer> adductIsotopes = adductMap.get(adduct);
		if ( adductIsotopes == null ) {
			adductIsotopes = new ArrayList<Integer>();
			adductMap.set(adduct, adductIsotopes);
		}
		if ( adductIsotopes.size() == maxEntries ) {
			int smallestIndex = -1;
			int adductIndex = -1;
			Double smallestValue = intensity;
			
			for ( int i = 0; i < adductIsotopes.size(); ++i) {
				final int index = adductIsotopes.get(i);
				final Double entryIntensity = intensities.get(index);
				if ( entryIntensity < smallestValue ) {
					smallestValue = entryIntensity;
					smallestIndex = index;
					adductIndex = i;
				}
			}
			if ( smallestIndex != -1 ) {
				indexMap.remove(masses.get(smallestIndex));
				indexMap.put(mass, smallestIndex);
				adductIsotopes.set(adductIndex, smallestIndex);
				masses.set(smallestIndex, mass);
				intensities.set(smallestIndex, intensity);
				adducts.set(smallestIndex, adduct);
				polarities.set(smallestIndex, polarity);
				isotopicFormulas.set(smallestIndex, gmf);
			}
		} else {
			masses.add(mass);
			intensities.add(intensity);
			adducts.add(adduct);
			polarities.add(polarity);
			isotopicFormulas.add(gmf);
			indexMap.put(mass, masses.size() - 1);
			adductIsotopes.add(masses.size() - 1);
		}
	}
	/* Old method, the maxEntries is over all adducts
	 * 
	public void add(final double mass, final double intensity, final String adduct, final GeneralMolecularFormula gmf) {
		assert intensities.size() <= maxEntries;
		if ( intensities.size() == maxEntries) {
			int smallestIndex = -1;
			Double smallestValue = intensity;
			
			for (Entry<Double,Integer> entry : indexMap.entrySet()) {
				final int index = entry.getValue();
				final Double entryIntensity = intensities.get(index);
				if ( entryIntensity < smallestValue ) {
					smallestValue = entryIntensity;
					smallestIndex = index;
				}
			}
			if ( smallestIndex != -1 ) {
				indexMap.remove(masses.get(smallestIndex));
				indexMap.put(mass, smallestIndex);
				masses.set(smallestIndex, mass);
				intensities.set(smallestIndex, intensity);
				adducts.set(smallestIndex, adduct);
				isotopicFormulas.set(smallestIndex, gmf);
			}
			assert intensities.size() == maxEntries;
		} else {
			masses.add(mass);
			intensities.add(intensity);
			adducts.add(adduct);
			isotopicFormulas.add(gmf);
			indexMap.put(mass, masses.size() - 1);
		}
		assert intensities.size() <= maxEntries;
	}
	*/
	public boolean hasValue(final int index) {
		return index < size();
	}
	
	public double getMass(final int index) {
		return masses.get(index);
	}
	
	public double getDistribution(final int index) {
		return intensities.get(index);
	}
	
	public int getAdduct(final int index) {
		return adducts.get(index);
	}
	
	public GeneralMolecularFormula getIsotopicFormula(final int index) {
		return isotopicFormulas.get(index);
	}
	
	public Polarity getPolarity(final int index) {
		return polarities.get(index);
	}
	
	public List<Double> getMasses() {
		return masses;
	}
	
	public List<Double> getDistribution() {
		return intensities;
	}
	
	public List<Integer> getAdducts() {
		return adducts;
	}
	
	public List<GeneralMolecularFormula> getGeneralMolecularFormulas() {
		return isotopicFormulas;
	}
	
	public List<Polarity> getPolarities() {
		return polarities;
	}
	
	public List<Integer> getPeaksFromAdduct(final int adduct) {
		return adductMap.get(adduct);
	}
	
	public int size() {
		return masses.size();
	}

	@Override
	public String toString() {
		return Common.join(intensities, " ") + "," + Common.join(masses, " ") + "," +
				Common.join(adducts, " ") + "," + Common.join(isotopicFormulas, " ");
	}
}
