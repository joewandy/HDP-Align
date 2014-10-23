package com.joewandy.alignmentResearch.precursorPrediction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdductTransformComputer {

	private static final Double ELECTRON_MASS = 0.00054857990924;
	
	public List<String> adductList;
	private Integer[] charge;
	private Double[] adductMass;
	private Double[] electronMass;
	private Integer[] multiplicity;
	private Integer nAdducts;
	private Double[] mul, sub;
	
	private final Map<String, Integer> chargeThings;
	private final Map<String, Double> massThings;
	private final Map<String, String> formulas;

	public AdductTransformComputer(List<String> al) {
		
		adductList = al;
		nAdducts = adductList.size();
		charge = new Integer[nAdducts];
		adductMass = new Double[nAdducts];
		electronMass = new Double[nAdducts];
		multiplicity = new Integer[nAdducts];
		mul = new Double[nAdducts];
		sub = new Double[nAdducts];
		
		chargeThings = new HashMap<String, Integer>();
		chargeThings.put("H", 1);
		chargeThings.put("Na", 1);
		chargeThings.put("NH4", 1);
		chargeThings.put("K", 1);
		chargeThings.put("Cl", -1);
		chargeThings.put("Br", -1);

		massThings = new HashMap<String, Double>();
		massThings.put("O", 15.9949146223);
		massThings.put("H", 1.0078250319);
		massThings.put("C", 12.0);
		massThings.put("N", 14.0030740074);
		massThings.put("Na", 22.98976966);
		massThings.put("K", 38.9637069);
		massThings.put("S", 31.97207073);

		formulas = new HashMap<String, String>();
		formulas.put("IsoProp", "C3H8O");
		formulas.put("DMSO", "C2H6OS");
		formulas.put("ACN", "C2H3N");
		formulas.put("FA", "CH2O2");

	}

	public void displayAdducts() {
		for (int i = 0; i < adductList.size(); i++) {
			System.out.println(adductList.get(i));
		}
	}

	public void displayLists() {
		System.out.println();
		System.out.println();
		System.out.println();
		for (int i = 0; i < nAdducts; i++) {
//			String line = adductList.get(i) + ": " + multiplicity[i];
			String line = adductList.get(i) + "," + sub[i] + "," + mul[i];
			System.out.println(line);
		}
	}

	public void makeLists() {
		for (int i = 0; i < nAdducts; i++) {
			this.compute(adductList.get(i), i);
		}
	}

	private void compute(String adduct, int index) {

		System.out.println("\n\n\n");
		System.out.println("ADDUCT: " + adduct);
		List<String> components = new ArrayList<String>();
		List<Integer> polarity = new ArrayList<Integer>();

		Boolean fin = false;
		Integer pos = 0;
		while (!fin) {
			if (adduct.substring(pos, pos + 1).charAt(0) == '+'
					| adduct.substring(pos, pos + 1).charAt(0) == '-') {
				fin = true;
			}
			pos++;
		}

		String massTerm = adduct.substring(0, pos - 1);
		String addTerm = adduct.substring(pos - 1);
		System.out.println("Mass term: " + massTerm + " addTerm " + addTerm);
		// ArrayList<String> components = adduct.split("\\+");
		// Integer[] polarity = new Integer(components.length() - 1);

		// components[0] is the M term
		if (massTerm.length() == 1) {
			multiplicity[index] = 1;
		} else {
			multiplicity[index] = Character.getNumericValue(massTerm.charAt(0));
		}
		charge[index] = 0;
		electronMass[index] = 0.0;
		adductMass[index] = 0.0;

		fin = false;
		Integer prev = 0;
		for (int i = 1; i < addTerm.length(); i++) {
			if (addTerm.charAt(i) == '+' | addTerm.charAt(i) == '-'
					| i == addTerm.length() - 1) {
				if (addTerm.charAt(prev) == '+') {
					polarity.add(1);
				} else {
					polarity.add(-1);
				}
				if (i == addTerm.length() - 1) {
					components.add(addTerm.substring(prev + 1, i + 1));
				} else {
					components.add(addTerm.substring(prev + 1, i));
				}
				prev = i;
			}
		}
		System.out.print("All components: ");
		for (int i = 0; i < components.size(); i++) {
			System.out.print("  " + components.get(i) + " (" + polarity.get(i)
					+ "),");
		}
		System.out.println();

		for (int i = 0; i < components.size(); i++) {
			System.out.print("Component: " + components.get(i));
			// First check for substitutions
			// is the first character an integer?
			Integer fac = 1;
			String temp = components.get(i);
			if (Character.isDigit(temp.charAt(0))) {
				fac = Character.getNumericValue(temp.charAt(0));
				temp = temp.substring(1);
			}

			// Now search for one of the transformations
			String match = formulas.get(temp);
			if (match != null) {
				temp = match;
				System.out.print(" (" + temp + ") ");
			}
			System.out.print(" Fac: " + fac);
			Boolean finished = false;
			Double totMass = 0.0;
			String atom;
			Integer tempCharge = chargeThings.get(temp);
			if (tempCharge != null) {
				charge[index] += fac * tempCharge * polarity.get(i);
				System.out.println("\tCharge = " + fac * tempCharge);
			} else {
				System.out.println("\tCharge = 0");
			}
			while (!finished) {
				// Get two characters
				String current;
				Double atomMatch;
				if (temp.length() > 1) {
					current = temp.substring(0, 2);
					atomMatch = massThings.get(current);
					atom = current;
					if (atomMatch == null) {

						current = temp.substring(0, 1);
						atomMatch = massThings.get(current);
						atom = current;
						temp = temp.substring(1);
					} else {
						temp = temp.substring(2);
					}
				} else {
					current = temp.substring(0, 1);
					atomMatch = massThings.get(current);
					atom = current;
					temp = temp.substring(1);
				}
				if (atomMatch == null) {
					System.out.println("FATAL ERROR QUITTING");
					System.exit(-1);
				} else {
					System.out.print("Found " + atom + " times ");
					if (temp.length() > 0) {
						String check = temp.substring(0, 1);
						if (Character.isDigit(check.charAt(0))) {
							System.out.println(Character.getNumericValue(check
									.charAt(0)));
							totMass += Character.getNumericValue(check
									.charAt(0)) * massThings.get(atom);
							temp = temp.substring(1);
						} else {
							totMass += massThings.get(atom);
							System.out.println("1");
						}

					} else {
						System.out.println("1");
						totMass += massThings.get(atom);
					}

				}
				if (temp.length() == 0) {
					finished = true;
				}
			}

			adductMass[index] += fac * totMass * polarity.get(i);

		}
		electronMass[index] = charge[index] * ELECTRON_MASS;
		System.out.print("multiplicity: " + multiplicity[index]
				+ " Adduct Mass: " + adductMass[index]);
		System.out.println(" charge: " + charge[index] + " electronMass: "
				+ electronMass[index]);

		// Old values for testing
		mul[index] = 1.0 * multiplicity[index] / Math.abs(charge[index]);
		sub[index] = (adductMass[index] - electronMass[index])
				/ Math.abs(charge[index]);
		System.out.println("Mul: " + mul[index] + " Sub: " + sub[index]);

	}

	public void exportMulSub(String outFile) {
		try {
			BufferedWriter b = new BufferedWriter(new FileWriter(outFile));
			for (int i = 0; i < nAdducts; i++) {
				String line = adductList.get(i) + "," + sub[i] + "," + mul[i]
						+ "\n";
				b.write(line);
			}
			b.close();
		} catch (IOException e) {
			System.out.println("IO Error");
		}
	}

	public static void main(String[] args) {

		List<String> adductList = new ArrayList<String>();
//		String adductFile = args[0];
//		try {
//			BufferedReader b = new BufferedReader(new FileReader(adductFile));
//			String line;
//			while ((line = b.readLine()) != null) {
//				adductList.add(line.trim());
//			}
//			b.close();
//		} catch (IOException e) {
//			adductList.add(args[0]);
//		}
		adductList.add("M+3H");
		adductList.add("M+NH4");
		adductList.add("M+IsoProp+Na+H");
		adductList.add("M+2ACN+2H");
		
		AdductTransformComputer a = new AdductTransformComputer(adductList);
		a.displayAdducts();
		a.makeLists();
		a.exportMulSub("mulsub.txt");
		a.displayLists();
	
	}
	
}