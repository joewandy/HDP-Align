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
		//System.err.println("Starting: " + mf.toString());
		final List<GeneralMassSpectrum> isotopeSpectrums = new ArrayList<GeneralMassSpectrum>();
		//System.err.println(mf);
		for (Element e : PeriodicTable.elements) {
			final int element = e.id;
			final int numberOfAtoms = mf.getNrAtoms(element);
			assert numberOfAtoms >= 0 : element + " " + numberOfAtoms;
			if ( numberOfAtoms == 0 ) continue;
			final GeneralMassSpectrum gms = getNewMassSpectrum(numberOfAtoms, element, minValue, adduct, polarity, numAdducts);
			//System.err.println(gms);
			isotopeSpectrums.add(gms);
		}
		//System.err.println("First done");
		assert isotopeSpectrums.size() > 0;
		final GeneralMassSpectrum finalSpectrum =
			calculateSpectrumFromBasisSpectra(isotopeSpectrums, maxValues, minValue, charge, adduct, polarity, numAdducts);
		//System.err.println("Final " + finalSpectrum);
		return finalSpectrum;
	}
	
	public static GeneralMassSpectrum getNewMassSpectrum(final int numberAtoms, final int elementIndex,
			final double minValue, final int adduct, final Polarity polarity, final int numAdducts) {
		assert numberAtoms > 0;
		final Element element = PeriodicTable.elements[elementIndex];
		//System.err.println("numberAtoms: " + numberAtoms + " element: " + element.name);
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
		//int totalSize = 1;
		
		/*
		final int[] distSizes = new int[isotopeSpectrums.size()];
		for (int i = 0; i < isotopeSpectrums.size(); ++i ) {
			final GeneralMassSpectrum ms = isotopeSpectrums.get(i);
			//System.err.println(ms);
			distSizes[i] = ms.getDistribution().size();
		//	totalSize *= distSizes[i];
		}
		//System.err.println("distSizes: " + Common.arrayToString(distSizes));
		*/
		
		class StackInformation {
			GeneralMassSpectrum basisSpectrum;
			int isotopeIndex;
			double probAccum;
			double massAccum;
			//List<GeneralMolecularFormula> gmfs = new ArrayList<GeneralMolecularFormula>();
			
			StackInformation(GeneralMassSpectrum basisSpectrum, int isotopeIndex, double probAccum, double massAccum) {
				this.basisSpectrum = basisSpectrum;
				this.isotopeIndex = isotopeIndex;
				this.probAccum = probAccum;
				this.massAccum = massAccum;
			}
		}
		
		final GeneralMassSpectrum retval = new GeneralMassSpectrum(maxValues, numAdducts);
		
		final Deque<GeneralMolecularFormula> gmfs = new ArrayDeque<GeneralMolecularFormula>();
		final Deque<StackInformation> workingSpectra = new ArrayDeque<StackInformation>();
		final Deque<GeneralMassSpectrum> basisSpectra = new ArrayDeque<GeneralMassSpectrum>(isotopeSpectrums);

		final StackInformation initialElement = new StackInformation(basisSpectra.pop(), 0, 1.0, 0.0);
		workingSpectra.push(initialElement);
		gmfs.push(workingSpectra.peek().basisSpectrum.getIsotopicFormula(0));
		
		while ( ! workingSpectra.isEmpty() ) {
			final StackInformation info = workingSpectra.peek();
			final GeneralMassSpectrum basisSpectrum = info.basisSpectrum;
			if ( info.isotopeIndex == basisSpectrum.size() ) {
				workingSpectra.pop();
				gmfs.pop();
				basisSpectra.push(basisSpectrum);
				continue;
			}
			//System.out.println("massAccum: " + info.massAccum);
			final double newProb = info.probAccum * basisSpectrum.getDistribution(info.isotopeIndex);
			double newMass = info.massAccum + basisSpectrum.getMass(info.isotopeIndex);
			final GeneralMolecularFormula basisFormula = basisSpectrum.getIsotopicFormula(info.isotopeIndex);
			//gmfs.push(basisSpectrum.getIsotopicFormula(info.isotopeIndex));
			info.isotopeIndex++;
			if ( newProb >= minValue ) {
				if ( workingSpectra.size() == isotopeSpectrums.size() ) {
					// We are at the bottom level, add the peak
					//System.out.println("newMass: " + newMass);
					newMass -= charge * PeriodicTable.electronmass;
					newMass /= Math.abs(charge);
					//System.out.println("newMass: " + newMass);
					//if (true) throw new RuntimeException();
					//System.err.println("Merging " + i);
					//gmfs.push(basisFormula);
					final GeneralMolecularFormula compositeGmf = GeneralMolecularFormula.mergeFormulae(gmfs);
					//System.err.println(compositeGmf);
					//if (true) throw new RuntimeException();
					retval.add(newMass, newProb, adduct, polarity, compositeGmf);
					//gmfs.pop();
				} else {
					final StackInformation newElement = new StackInformation(basisSpectra.pop(), 0, newProb, newMass);
					workingSpectra.push(newElement);
					
					gmfs.push(newElement.basisSpectrum.getIsotopicFormula(0));
				}
			}
		}
		return retval;
		
		
		
		/*
		
		
		int currentBasisSpectrumIndex = 0;
		final int[] basisSpectrumIsotopeIndex = new int[isotopeSpectrums.size()];
		final double[] probAccum = new double[isotopeSpectrums.size()];
		final double[] massAccum = new double[isotopeSpectrums.size()];
		
		@SuppressWarnings("unchecked")
		final List<GeneralMolecularFormula>[] gmfs = (List<GeneralMolecularFormula>[])new Object[isotopeSpectrums.size()];
		for (int i = 0; i < isotopeSpectrums.size(); ++i) {
			massAccum[i] = 1.0;
			gmfs[i] = new ArrayList<GeneralMolecularFormula>();                                
		}
		
		while (currentBasisSpectrumIndex >= 0) {
			final GeneralMassSpectrum currentBasisSpectrum = isotopeSpectrums.get(currentBasisSpectrumIndex);
			final int basisSpectrumIsotope = basisSpectrumIsotopeIndex[currentBasisSpectrumIndex];
			final double mass = currentBasisSpectrum.getMasses().get(basisSpectrumIsotope);
			final double intensity = currentBasisSpectrum.getDistribution().get(basisSpectrumIsotope);
			
			probAccum *= intensity;
			massAccum += mass;
			gmfs.add(currentBasisSpectrum.getGeneralMolecularFormulas().get(basisSpectrumIsotope));
			
			if ( probAccum < minValue ) {
				
			}
		}
		
		
		
		
		
		
		
		final int[] distSizes = new int[isotopeSpectrums.size()];
		for (int i = 0; i < isotopeSpectrums.size(); ++i ) {
			final GeneralMassSpectrum ms = isotopeSpectrums.get(i);
			//System.err.println(ms);
			distSizes[i] = ms.getDistribution().size();
		}
		
		final MultidimensionalCounter mc = new MultidimensionalCounter(distSizes);
		final GeneralMassSpectrum retval = new GeneralMassSpectrum(maxValues);
		for (int i : mc) {
			final int[] isotopeIndices = mc.getCounts(i);
			double probAccum = 1.0;
			double massAccum = 0.0;
			final List<GeneralMolecularFormula> gmfs = new ArrayList<GeneralMolecularFormula>();
			for (int j = 0; j < isotopeSpectrums.size(); ++j) {
				final int isotopeIndex = isotopeIndices[j];
				GeneralMassSpectrum ms = isotopeSpectrums.get(j);
				probAccum *= ms.getDistribution().get(isotopeIndex);
				massAccum += ms.getMasses().get(isotopeIndex);
				gmfs.add(ms.getGeneralMolecularFormulas().get(isotopeIndex));
			}
			if ( probAccum < minValue ) continue;
			massAccum -= charge * PeriodicTable.electronmass;
			massAccum /= Math.abs(charge);
			//System.err.println("Merging " + i);
			final GeneralMolecularFormula compositeGmf = GeneralMolecularFormula.mergeFormulae(gmfs);
			//System.err.println("Done");
			retval.add(massAccum, probAccum, adduct, compositeGmf);
		}
		System.err.println("Stopping");
		return retval;
		*/
	}
}
