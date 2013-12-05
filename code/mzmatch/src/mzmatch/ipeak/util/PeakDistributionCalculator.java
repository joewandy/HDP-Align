package mzmatch.ipeak.util;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import peakml.chemistry.MolecularFormula;
import peakml.chemistry.PeriodicTable;
import peakml.chemistry.PeriodicTable.Element;
import peakml.chemistry.Polarity;

import com.google.common.primitives.Doubles;

public class PeakDistributionCalculator {
	public static GeneralMassSpectrum getIntensityDistribution(final MolecularFormula mf, final double minValue, final int maxValues,
			final int charge, final int adduct, final Polarity polarity, final int numAdducts) {
		final List<GeneralMassSpectrum> isotopeSpectrums = new ArrayList<GeneralMassSpectrum>();
		for (Element e : PeriodicTable.elements) {
			final int element = e.id;
			final int numberOfAtoms = mf.getNrAtoms(element);
			assert numberOfAtoms >= 0 : element + " " + numberOfAtoms;
			if ( numberOfAtoms == 0 ) continue;
			final GeneralMassSpectrum gms = getNewMassSpectrum(numberOfAtoms, element, minValue, adduct, polarity, numAdducts);
			isotopeSpectrums.add(gms);
		}
		assert isotopeSpectrums.size() > 0 : mf.toString();
		final GeneralMassSpectrum finalSpectrum =
			calculateSpectrumFromBasisSpectra(isotopeSpectrums, maxValues, minValue, charge, adduct, polarity, numAdducts);
		return finalSpectrum;
	}
	
	public static GeneralMassSpectrum getNewMassSpectrum(final int numberAtoms, final int elementIndex,
			final double minValue, final int adduct, final Polarity polarity, final int numAdducts) {
		assert numberAtoms > 0;
		final Element element = PeriodicTable.elements[elementIndex];
		final int numberIsotopes = element.getNrIsotopes();
		
		final List<Double> miws = new ArrayList<Double>();
		final List<Double> abund = new ArrayList<Double>();
		for (int i = 0; i < numberIsotopes; ++i) {
			final double abundance = element.getAbundance(i);
			if ( abundance != 0.0 ) {
				abund.add(abundance);
				final double miw = element.getMonoIsotopicWeight(i);
				miws.add(miw);
			}
		}
		final double[] miwsArray = Doubles.toArray(miws);
		final double[] abundArray = Doubles.toArray(abund);
		
		final MultinomialDistribution dist = new MultinomialDistribution(numberAtoms, abundArray);		
		assert dist.size() > 0;
		
		final GeneralMassSpectrum ms = GeneralMassSpectrum.spectrumFromNumAdducts(numAdducts);

		for (int[] index : dist ) {
			final double prob = dist.getProbability(index);
			if ( prob < minValue ) continue;
			double massAccum = 0.0;
			final GeneralMolecularFormula gmf = new GeneralMolecularFormula();
			for (int i = 0; i < index.length; ++i) {
				final int numberIsotopicAtoms = index[i];
				final double mass = miwsArray[i];
				massAccum += mass * numberIsotopicAtoms;
				gmf.addIsotope(elementIndex, i, numberIsotopicAtoms);
			}
			ms.add(massAccum, prob, adduct, polarity, gmf);
			
		}
		return ms;
	}
	
	/**
	 * This method implements a depth first search
	 * @param isotopeSpectrums
	 * @param maxValues
	 * @param minValue
	 * @param charge
	 * @param adduct
	 * @return
	 */
	private static GeneralMassSpectrum calculateSpectrumFromBasisSpectra(final List<GeneralMassSpectrum> isotopeSpectrums,
			final int maxValues, final double minValue, final int charge, final int adduct, final Polarity polarity,
			final int numAdducts) {
		assert isotopeSpectrums.size() > 0;
		
		class StackInformation {
			GeneralMassSpectrum basisSpectrum;
			int isotopeIndex;
			double probAccum;
			double massAccum;
			
			StackInformation(GeneralMassSpectrum basisSpectrum, int isotopeIndex, double probAccum, double massAccum) {
				this.basisSpectrum = basisSpectrum;
				this.isotopeIndex = isotopeIndex;
				this.probAccum = probAccum;
				this.massAccum = massAccum;
			}
		}
		
		final GeneralMassSpectrum retval = new GeneralMassSpectrum(maxValues, numAdducts);
		
		final Deque<StackInformation> workingSpectra = new ArrayDeque<StackInformation>();
		final Deque<GeneralMassSpectrum> basisSpectra = new ArrayDeque<GeneralMassSpectrum>(isotopeSpectrums);

		final StackInformation initialElement = new StackInformation(basisSpectra.pop(), 0, 1.0, 0.0);
		workingSpectra.push(initialElement);

		while ( ! workingSpectra.isEmpty() ) {
			final StackInformation info = workingSpectra.peek();
			final GeneralMassSpectrum basisSpectrum = info.basisSpectrum;
			if ( info.isotopeIndex == basisSpectrum.size() ) {
				workingSpectra.pop();
				basisSpectra.push(basisSpectrum);
				continue;
			}
			final double newProb = info.probAccum * basisSpectrum.getDistribution(info.isotopeIndex);
			double newMass = info.massAccum + basisSpectrum.getMass(info.isotopeIndex);

			info.isotopeIndex++;
			if ( newProb >= minValue ) {
				if ( workingSpectra.size() == isotopeSpectrums.size() ) {
					// We are at the bottom level, add the peak
					newMass -= charge * PeriodicTable.electronmass;
					newMass /= Math.abs(charge);
					final List<GeneralMolecularFormula> forms = new ArrayList<GeneralMolecularFormula>();
					for ( StackInformation s : workingSpectra ) {
						forms.add(s.basisSpectrum.getIsotopicFormula(s.isotopeIndex - 1));
					}

					final GeneralMolecularFormula compositeGmf = GeneralMolecularFormula.mergeFormulae(forms);
					retval.add(newMass, newProb, adduct, polarity, compositeGmf);
				} else {
					final StackInformation newElement = new StackInformation(basisSpectra.pop(), 0, newProb, newMass);
					workingSpectra.push(newElement);
				}
			}
		}
		return retval;
	}
}
