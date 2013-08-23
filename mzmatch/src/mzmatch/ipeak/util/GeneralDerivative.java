package mzmatch.ipeak.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import peakml.chemistry.Mass;
import peakml.chemistry.MolecularFormula;
import peakml.chemistry.Molecule;
import peakml.chemistry.PeriodicTable;
import peakml.chemistry.PeriodicTable.Derivative;
import peakml.chemistry.PeriodicTable.Element;
import peakml.chemistry.Polarity;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public class GeneralDerivative {
	private static final Function<String,Integer> cs;
	private static final Map<String,String> formulas;
	private static final Pattern pattern = Pattern.compile("(\\+|\\-)?(\\d*)(\\w+)");
	//private static final Pattern pattern = Pattern.compile("\\D");
	private final Derivative derivative;
	private final MolecularFormula mf;
	private final String[] adducts;
	private final int adduct;
	
	static {
		final Map<String,Integer> chargeState = new HashMap<String,Integer>();
		chargeState.put("H", 1);
		chargeState.put("Na", 1);
		chargeState.put("NH4", 1);
		chargeState.put("K", 1);
		chargeState.put("Cl", -1);
		chargeState.put("Br", -1);
		cs = Functions.forMap(chargeState, 0);
		
		formulas = new HashMap<String,String>();
		formulas.put("IsoProp", "C3H8O");
		formulas.put("DMSO", "C2H6OS");
		formulas.put("ACN", "C2H3N");
		formulas.put("FA", "CH2O2");
	}

	public GeneralDerivative(final MolecularFormula molecularFormula, final int adduct, final boolean override, final String[] adducts) {
		this.adducts = adducts;
		try {
			this.mf = calculateTotalMolecularFormula(new MolecularFormula(molecularFormula), adduct);
		} catch (Exception e) {
			throw new RuntimeException("Problem calculating formula: " + molecularFormula + " adduct: " + adduct, e);
		}
		double mass;
		int nmer;
		int charge;
		if ( ! isValid() ) {
			mass = -1.0;
			nmer = -1;
			charge = 0;
		} else {
			charge = calculateCharge(adduct);
			mass = calculateMass();
			nmer = calculateNmer(adduct);
		}
		final String adductString = adducts[adduct];
		derivative = new Derivative(PeriodicTable.ADDUCT, nmer, charge, "", adductString, mass);
		this.adduct = adduct;
	}
	
	public boolean isValid() {
		return isValidMolecularFormula(this.mf);
	}

	public double getMass() {
		double runningMass = derivative.getMass();
		runningMass -= derivative.getCharge() * PeriodicTable.electronmass;
		runningMass /= Math.abs(derivative.getCharge());
		return runningMass;
	}
	
	public Polarity getPolarity() {
		if ( derivative.getCharge() > 0 ) {
			return Polarity.POSITIVE;
		} else if ( derivative.getCharge() < 0 ) {
			return Polarity.NEGATIVE;
		}
		return Polarity.NEUTRAL;
	}
	
	public GeneralMassSpectrum getDistribution(final double minDistributionValue, final int maxValues) {
		if ( ! isValid() ) {
			throw new RuntimeException("Calculated formula is not valid");
		}
		return PeakDistributionCalculator.getIntensityDistribution(this.mf, minDistributionValue, maxValues,
			this.derivative.charge, this.adduct, this.getPolarity(), adducts.length);
	}
	
	private int calculateCharge(int adductIndex) {
		int runningCharge = 0;
		final String adduct = adducts[adductIndex];
		final String[] sadduct = adduct.split("(?=\\+)|(?=\\-)");
		for (int i = 0; i < sadduct.length; ++i) {
			final String adductPart = sadduct[i];
			final Matcher m = pattern.matcher(adductPart);
			m.matches();
			final int multiplier = calculatePartPolarity(m);
			final int charge = calculatePartCharge(m);
			runningCharge += multiplier * charge;
		}
		return runningCharge;
	}
	
	private MolecularFormula calculateTotalMolecularFormula(final MolecularFormula moleculeFormula, final int adductIndex) {
		//final MolecularFormula moleculeFormula = new MolecularFormula(molecule.getFormula());
		final String adduct = adducts[adductIndex];
		final String[] sadduct = adduct.split("(?=\\+)|(?=\\-)");
		for (int i = 0; i < sadduct.length; ++i) {
			final String adductPart = sadduct[i];
			final Matcher m = pattern.matcher(adductPart);
			boolean matches = m.matches();
			assert matches;
			String submol;
			try {
				submol = getSubmolecule(m);
			} catch (Exception e) {
				throw new RuntimeException("Problem with adductPart: " + adductPart, e);
			}
			if ( submol.equals("M") ) {
				final int nmer = calculatePartNmer(m);
				for (Element e : PeriodicTable.elements) {
					final int element = e.id;
					final int numberOfMoleculeAtoms = moleculeFormula.getNrAtoms(element);
					moleculeFormula.setNrAtoms(element, nmer * numberOfMoleculeAtoms);
				}
				continue;
			}
			if ( formulas.containsKey(submol) ) {
				submol = formulas.get(submol);
			} 
			final MolecularFormula adductFormula = new MolecularFormula(submol);
			final int multiplier = calculatePartMultiplier(m);
			//System.err.println("multiplier: " + multiplier);
			
			for (Element e : PeriodicTable.elements) {
				final int element = e.id;
				final int numberOfAdductAtoms = multiplier * adductFormula.getNrAtoms(element);
				final int numberOfMoleculeAtoms = moleculeFormula.getNrAtoms(element);
				if (numberOfAdductAtoms != 0)
			//		System.err.println("element: " + e.id + " adductAtoms: " + numberOfAdductAtoms + " mAtoms: " + numberOfMoleculeAtoms);
				
				moleculeFormula.setNrAtoms(element, numberOfMoleculeAtoms + numberOfAdductAtoms);
			}
		}
		return moleculeFormula;
	}
	
	private boolean isValidMolecularFormula(final MolecularFormula mf) {
		for (Element e : PeriodicTable.elements) {
			final int element = e.id;
			final int numberOfMoleculeAtoms = mf.getNrAtoms(element);
			if (numberOfMoleculeAtoms < 0)
				return false;
		}
		return true;
	}
	
	private double calculateMass() {
		return this.mf.getMass(Mass.MONOISOTOPIC);
	}
	
	private int calculateNmer(final int adductIndex) {
		final String adduct = adducts[adductIndex];
		
		final String[] sadduct = adduct.split("(?=\\+)|(?=\\-)");
		for (int i = 0; i < sadduct.length; ++i) {
			final String adductPart = sadduct[i];
			final Matcher m = pattern.matcher(adductPart);
			m.matches();
			String submol = m.group(3);
			if ( submol.startsWith("M") ) {
				return calculatePartNmer(m);
			}
		}
		throw new RuntimeException("Adduct does not contain an M: " + adduct);
	}
	
	private int calculatePartMultiplier(final Matcher adductPart) {
		final int polarity = calculatePartPolarity(adductPart);
		final int nmer = calculatePartNmer(adductPart);
		return polarity * nmer;
	}

	private int calculatePartPolarity(final Matcher adductPart) {
//		final Matcher m = pattern.matcher(adductPart);
		//System.err.println(adductPart);
		final String polarity = adductPart.group(1);
		if ( polarity == null ) {
			return 1;
		}
		if ( polarity.equals("-") ) {
			return -1;
		}
		return 1;
	}
	
	private int calculatePartCharge(final Matcher adductPart) {
		//final Matcher m = pattern.matcher(adductPart);
		final int nmer = calculatePartNmer(adductPart);
		final String submol = getSubmolecule(adductPart);
		final int charge = cs.apply(submol);
		return nmer * charge;
	}
	
	private int calculatePartNmer(final Matcher adductPart) {
		//final Matcher m = pattern.matcher(adductPart);
		final String nmer = adductPart.group(2);
		if ( nmer.length() > 0 ) {
			return Integer.parseInt(nmer);
		}
		return 1;
	}
	
	private String getSubmolecule(final Matcher adductPart) {
		return adductPart.group(3);
	}
}
