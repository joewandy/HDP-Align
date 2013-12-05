package mzmatch.ipeak.util;

import peakml.chemistry.PeriodicTable;
import peakml.chemistry.PeriodicTable.Element;
import peakml.chemistry.PeriodicTable.Isotope;

public class GeneralMolecularFormula {
	private int[][] elements = new int[PeriodicTable.NR_ELEMENTS][];
	
	public GeneralMolecularFormula() {
		for (int i = 0; i < PeriodicTable.NR_ELEMENTS; ++i) {
			elements[i] = new int[PeriodicTable.elements[i].isotopes.length];
		}
	}
	
	public void addIsotope(final int element, final int isotope, final int count) {
		assert element >= 0 && element < elements.length;
		assert isotope >= 0 && isotope < elements[element].length;
		elements[element][isotope] += count;
	}
	
	public int[] getIsotopeCounts(final int element) {
		return elements[element];
	}
	
	public static GeneralMolecularFormula mergeFormulae(final Iterable<GeneralMolecularFormula> formulae) {
		final GeneralMolecularFormula formula = new GeneralMolecularFormula();
		
		for (int i = 0; i < PeriodicTable.NR_ELEMENTS; ++i) {
			for (GeneralMolecularFormula gmf : formulae) {
				final int[] isotopeCounts = gmf.getIsotopeCounts(i);
				for (int j = 0; j < isotopeCounts.length; ++j) {
					formula.addIsotope(i, j, isotopeCounts[j]);
				}
			}
		}
		return formula;
	}
	
	@Override
	public String toString() {
		final StringBuilder builder = new StringBuilder();
		for (int i = 0; i < elements.length; ++i) {
			final Element e = PeriodicTable.elements[i];
			final StringBuilder elementBuilder = new StringBuilder();
			for (int j = 0; j < elements[i].length; ++j) {
				final int numberOfIsotope = elements[i][j];
				if ( numberOfIsotope > 0 ) {
					final Isotope isotope = e.isotopes[j];
					elementBuilder.append("[");
					elementBuilder.append(isotope.atomicmass);
					elementBuilder.append(e.identifier);
					elementBuilder.append("]");
					elementBuilder.append(numberOfIsotope);
					
					
					//elementBuilder.append(numberOfIsotope);
					//elementBuilder.append("(");
					//elementBuilder.append(isotope.atomicmass);
					//elementBuilder.append(")");
				}
			}
			final String elementString = elementBuilder.toString();
			if ( elementString.length()  > 0 ) {
				builder.append(elementString);
				//builder.append("[");
				//builder.append(elementString);
				//builder.append("]");
				//builder.append(e.identifier);
			}
		}
		return builder.toString();
	}
	
}
