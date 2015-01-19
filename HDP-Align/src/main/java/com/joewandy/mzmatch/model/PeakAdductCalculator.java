package com.joewandy.mzmatch.model;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import peakml.chemistry.PeriodicTable;
import peakml.chemistry.PeriodicTable.Derivative;
import peakml.chemistry.Polarity;

import com.google.common.base.Function;
import com.google.common.base.Functions;

public class PeakAdductCalculator {
	
	private static final Function<String,Integer> cs;
	private static final Map<String,String> formulas;
	private static final Pattern pattern = Pattern.compile("(\\+|\\-)?(\\d*)(\\w+)");
	private final Derivative derivative;
	private final String adduct;
	
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

	public PeakAdductCalculator(double mass, final String adduct) {
		int charge = calculateCharge(adduct);
		int nmer = calculateNmer(adduct);
		derivative = new Derivative(PeriodicTable.ADDUCT, nmer, charge, "", adduct, mass);
		this.adduct = adduct;
	}
		
	public String getAdduct() {
		return adduct;
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
		
	private int calculateCharge(String adduct) {
		int runningCharge = 0;
		
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
				
	private int calculateNmer(final String adduct) {
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
	
	private int calculatePartPolarity(final Matcher adductPart) {
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
		final int nmer = calculatePartNmer(adductPart);
		final String submol = getSubmolecule(adductPart);
		final int charge = cs.apply(submol);
		return nmer * charge;
	}
	
	private int calculatePartNmer(final Matcher adductPart) {
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