/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.chemistry;


// java
import java.util.*;
import java.util.regex.*;





/**
 * Implementation of a container keeping track of the atom counts for a molecular
 * formula. Several convenience methods are provided, including a string parser,
 * validity checks and molecular formula math (subtraction and addition of other
 * molecular formulae to this molecular formula).
 */
public class MolecularFormula
{
	public static class SubFormula
	{
		// constructor(s)
		public SubFormula(String name, int defaultn, int charge, int elements[])
		{
			this.name = name;
			this.charge = charge;
			this.elements = elements;
			this.defaultn = defaultn;
		}
		
		public SubFormula(SubFormula subformula)
		{
			this.name = subformula.name;
			this.charge = subformula.charge;
			this.defaultn = subformula.defaultn;
			this.elements = new int[PeriodicTable.NR_ELEMENTS];
			System.arraycopy(subformula.elements, 0, this.elements, 0, PeriodicTable.NR_ELEMENTS);
		}
		
		
		// access
		public String getName()
		{
			return name;
		}
		
		public void setName(String name)
		{
			this.name = name;
		}
		
		public int getCharge()
		{
			return charge;
		}
		
		public void setCharge(int charge)
		{
			this.charge = charge;
		}
		
		public int getDefaultN()
		{
			return defaultn;
		}
		
		public void setDefaultN(int n)
		{
			defaultn = n;
		}
		
		public double getMass(Mass masstype)
		{
			return getMass(defaultn, masstype);
		}
		
		public double getMass(int n, Mass masstype) throws IllegalArgumentException
		{
			if (n!=1 && defaultn==0)
				throw new IllegalArgumentException("Requesting n='" + n + "', which is not supported for this sub-formula.");
			
			double mass = 0;
			if (masstype == Mass.MOLECULAR)
			{
				for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
					mass += elements[i] * PeriodicTable.elements[i].getMolecularWeight();
			}
			else if (masstype == Mass.MONOISOTOPIC)
			{
				for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
					mass += elements[i] * PeriodicTable.elements[i].getMonoIsotopicWeight();
			}
			
			return mass * n - (charge*PeriodicTable.electronmass);
		}
		
		public int getNrAtoms()
		{
			int n = 0;
			for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
				n += elements[i];
			return n;
		}
		
		public double getMassVariance()
		{
			double massvariance = 0;
			for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
				massvariance += elements[i] * PeriodicTable.elements[i].getMassVariance();
			
			return massvariance;
		}
		
		
		// Object overrides
		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer();
			str.trimToSize();
			
			if (defaultn != 0)
				str.append("[");
			for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
			{
				if (elements[i] == 0)
					continue;
				
				str.append(PeriodicTable.elements[i].identifier);
				if (elements[i] > 1)
					str.append(elements[i]);
			}
			
			if (charge != 0)
				str.append((charge<0 ? "-" : "+") + (Math.abs(charge)>1 ? Math.abs(charge) : ""));
			if (defaultn != 0)
				str.append("]n");
			
			return str.toString();
		}
		
		
		// data
		protected String name;
		protected int defaultn;
		protected int charge;
		protected int elements[];
	}
	
	
	// constructor(s)
	/**
	 * Constructs an empty formula. The {@link MolecularFormula#Init(String,String)} function
	 * can be used to initialize a real molecular formula.
	 */
	public MolecularFormula()
	{
		subformulas.add(new SubFormula(
				"M", 1, 0, new int[PeriodicTable.NR_ELEMENTS]
			));
	}
	
	/**
	 * Constructs a new class with the given formula by calling
	 * {@link MolecularFormula#Init(String,String)}.
	 * 
	 * @param formula		The molecular formula to initialize to.
	 */
	public MolecularFormula(String formula)
	{
		String tokens[] = formula.split(";");
		if (tokens.length == 1)
			Init("M", formula); // the M+H is normalized to M-H+proton
		else
			Init(tokens[0], tokens[1]);
	}
	
	public MolecularFormula(String iontype, String formula)
	{
		Init(iontype, formula);
	}
	
	/**
	 * Copy-constructor, which copies the complete contents of the given
	 * instance.
	 * 
	 * @param formula		The molecular formula instance to copy.
	 */
	public MolecularFormula(MolecularFormula formula)
	{
		this.charge = formula.charge;
		this.elements = formula.elements.clone();
		for (SubFormula subformula : formula.subformulas) {
			subformulas.add(new SubFormula(subformula));
		}
		//subformulas.clear();
		//for (SubFormula subformula : subformulas)
		//	subformulas.add(new SubFormula(subformula));
	}
	
	/**
	 * Initialization function for the class; essentially a string parser for the
	 * internationally recognized method of writing molecular formulae (e.g. C13H26+).
	 * For each of the atom names the count is extracted and tracked within the
	 * instance. The + or - can be used to indicate a positive or negative charge on
	 * the molecule. When multiple charges are required the access methods for
	 * charges can be used.
	 * 
	 * @param formula		The molecular formula in string form.
	 */
	protected void Init(String iontype, String formula)
	{
		subformulas.clear();
		for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
			elements[i] = 0;
		
		// break up the formula 
		Matcher ma_formula = pa_formula.matcher(formula);
		while (ma_formula.find())
		{
			// 1 - ( ?; 2 - formula; 3 - charge ?; 4 - ?; 5 - n; 6 - count
			String str_charge = ma_formula.group(3);
			String str_count = ma_formula.group(6);
			
			// break up the subformula
			SubFormula subformula = new SubFormula(
					"M", ma_formula.group(4).length()!=0 ? 1 : 0, 0, parseFormula(ma_formula.group(2))
				);
			if (str_charge.equals("-"))
				subformula.setCharge(-1);
			else if (str_charge.equals("+"))
				subformula.setCharge(1);
			if (str_count.length() != 0)
			{
				int n = Integer.parseInt(str_count);
				for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
					subformula.elements[i] *= n;
			}
			subformulas.add(subformula);
		}
		
		// break up the iontype
		Matcher ma_iontype = pa_iontype.matcher(iontype);
		while (ma_iontype.find())
		{
			// 1 - [ ?; 2 - formula-names; 3 - formulas; 4 - ] ?; 5 - charge ?
			// break up the formula-names
			int subformulaid = 0;
			Matcher ma_iontype_formulaname = pa_iontype_formulaname.matcher(ma_iontype.group(2));
			while (ma_iontype_formulaname.find())
			{
				for (int j=1; j<=ma_iontype_formulaname.groupCount(); j+=2)
				{
					String name = ma_iontype_formulaname.group(j);
					String count = ma_iontype_formulaname.group(j+1);
					
					SubFormula subformula = subformulas.elementAt(subformulaid++);
					subformula.setName(name);
					subformula.setDefaultN(count.length()>0 ? Integer.parseInt(count) : 1);
				}
			}
			
			// break up the formulas
			Matcher ma_iontype_formulas = pa_iontype_formulas.matcher(ma_iontype.group(3));
			while (ma_iontype_formulas.find())
			{
				// 1 - operator; 2 - multiplication factor; 3 - formula;
				String str_operator	= ma_iontype_formulas.group(1);
				String str_factor	= ma_iontype_formulas.group(2);
				String str_formula	= ma_iontype_formulas.group(3);
				
				int factor = str_operator.equals("+") ? 1 : -1;
				if (str_factor.length() > 0)
					factor *= Integer.parseInt(str_factor);
				
				int current_elements[] = parseFormula(str_formula);
				for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
					elements[i] += current_elements[i] * factor;
			}
			
			// retrieve the charge
			String str_charge = ma_iontype.group(5);
			if (str_charge.equals("+"))
				charge = 1;
			else if (str_charge.equals("-"))
				charge = -1;
		}
	}

	
	// access
	public Vector<SubFormula> getSubFormulas()
	{
		return subformulas;
	}
	
	/**
	 * Returns the charge on the molecule. A positive value corresponds to a positive
	 * charge and visa-versa. A value bigger than 1 means there are multiple charges
	 * on the molecule.
	 * 
	 * @return				The charge on the molecule.
	 */
	public int getCharge()
	{
		int charge = 0;
		for (SubFormula subformula : subformulas)
			charge += subformula.getCharge();
		return charge;
	}
	
	/**
	 * Counts the total amount of atoms in the molecule, without taking an atom type into
	 * account.
	 * 
	 * @return				The total number of atoms in the molecular formula.
	 */
	public int getNrAtoms()
	{
		int n = 0;
		for (SubFormula subformula : subformulas)
			n += subformula.getNrAtoms();
		return n;
	}
	
	public int getNrAtoms(int atomid)
	{
		int n = 0;
		for (SubFormula subformula : subformulas)
			n += subformula.elements[atomid];
		return n;
	}
	
	public int getNrAtoms(int atomid, int subformulaindex)
	{
		return subformulas.get(subformulaindex).elements[atomid];
	}
	
	public void setNrAtoms(int atomid, int count)
	{
		setNrAtoms(atomid, count, 0);
	}
	
	public void setNrAtoms(int atomid, int count, int subformulaindex)
	{
		SubFormula subformula = subformulas.get(subformulaindex);
		subformula.elements[atomid] = count;
	}
	
	/**
	 * Calculates the mass for this molecular formula. The parameter masstype indicates
	 * whether the molecular mass or the monoisotopic mass should be calculated.
	 * 
	 * @param masstype		Indicates whether to use molecular or monoisotopic weight.
	 */
	public double getMass(Mass masstype)
	{
		double mass = 0;
		for (SubFormula subformula : subformulas)
			mass += subformula.getMass(masstype);
		for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
			mass += elements[i] * PeriodicTable.elements[i].getMass(masstype);
		
		if (charge!=0 && elements[PeriodicTable.HYDROGEN]!=0)
			mass -= charge * PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(masstype);
		return mass;
	}
	
	/**
	 * Calculates the mass-variance of the molecule.
	 * 
	 * @return				The mass variance for the molecule.
	 */
	public double getMassVariance()
	{
		double massvariance = 0;
		for (SubFormula subformula : subformulas)
			massvariance += subformula.getMassVariance();
		return massvariance;
	}
	
	/**
	 * Calculates the abundance of the molecule.
	 * 
	 * @return				The abundance of the molecule.
	 */
//	public double getAbundance()
//	{
//		double abundance = 1.;
//		for (int atomid=0; atomid<PeriodicTable.NR_ELEMENTS; ++atomid)
//		{
//			if (atoms[atomid] == 0)
//				continue;
//			abundance *= Math.pow(PeriodicTable.elements[atomid].getAbundance()/100., atoms[atomid]);
//		}
//		
//		return abundance * 100.;
//	}

	/**
	 * Validity check for molecular formulae.
	 * 
	 * @return			Returns true when the molecule conforms to all the validity checks.
	 */
	public boolean isValid()
	{
        // TODO maybe the ions come into play here
		// the sum of valences is an even number, OR the total number of atoms having odd valences is even
		int valences = 0;
		for (int i=0; i<PeriodicTable.NR_ELEMENTS; i++)
			valences += getNrAtoms(i) * PeriodicTable.elements[i].valency;
		
		if (valences%2 != 0)
		{
			int atoms_wt_odd_valences = 0;
			for (int i=0; i<PeriodicTable.NR_ELEMENTS; i++)
				atoms_wt_odd_valences += (PeriodicTable.elements[i].valency%2!=0 ? 1 : 0);
			
			if (atoms_wt_odd_valences%2 != 0)
				return false;
		}

		// the sum of valences is greater than or equal to twice the maximum valence
//		int max_valence = 0;
//		for (int i=0; i<PeriodicTable.NR_ATOMS; i++)
//		{
//			int valence = formula[i] * PeriodicTable.elements[i].valency;
//			max_valence = (max_valence>valence ? max_valence : valence);
//		}
//		if (valences < 2*max_valence)
//			return false;

		// the sum of valences is greater than or equal to twice the number of atoms - 1
		int nr_atoms = 0;
		for (int i=0; i<PeriodicTable.NR_ELEMENTS; i++)
			nr_atoms += getNrAtoms(i);
		
		if (valences < (2*nr_atoms)-1)
			return false;
		
		return true;
	}
	
//	/**
//	 * Adds the atom counts of the given formula to this molecular formula.
//	 * 
//	 * @param formula		The molecular formula to add to this molecular formula.
//	 */
//	public void add(String formula)
//	{
//		add(new MolecularFormula(formula));
//	}
	
//	/**
//	 * Adds the atom counts of the given formula to this molecular formula.
//	 * 
//	 * @param formula		The molecular formula to add to this molecular formula.
//	 */
//	public void add(MolecularFormula formula)
//	{
//		for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
//			atoms[i] += formula.atoms[i];
//	}
	
//	/**
//	 * Removes the atom counts of the given formula from this molecular formula.
//	 * 
//	 * @param formula		The molecular formula to remove from this molecular formula.
//	 */
//	public void remove(String formula)
//	{
//		remove(new MolecularFormula(formula));
//	}
	
//	/**
//	 * Removes the atom counts of the given formula from this molecular formula.
//	 * 
//	 * @param formula		The molecular formula to remove from this molecular formula.
//	 */
//	public void remove(MolecularFormula formula)
//	{
//		for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
//			atoms[i] -= formula.atoms[i];
//	}

	
	// Object overrides
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		str.trimToSize();
		
		str.append("[");
		for (SubFormula subformula : subformulas)
			str.append(subformula.name + (subformula.defaultn!=0?subformula.defaultn:""));
		for (int i=0; i<PeriodicTable.NR_ELEMENTS; ++i)
		{
			if (elements[i] != 0)
			{
				String identifier = PeriodicTable.elements[i].identifier;
				if (elements[i] == -1)
					str.append("-" + identifier);
				else if (elements[i] == 1)
					str.append("+" + identifier);
				else
					str.append((elements[i]<0?"-":"+") + identifier + Math.abs(elements[i]));
			}
		}
		str.append("]" + (charge==0?"":(charge<0?"-":"+")));
		str.append(";");
		for (SubFormula subformula : subformulas)
			str.append(subformula);
		
		return str.toString();
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof MolecularFormula))
			return false;
		
		MolecularFormula p = (MolecularFormula) other;
		for (int atomid=0; atomid<PeriodicTable.NR_ELEMENTS; ++atomid)
			if (getNrAtoms(atomid) != p.getNrAtoms(atomid))
				return false;
		return true;
	}
	
	
	// data
	protected int charge = 0;
	protected int elements[] = new int[PeriodicTable.NR_ELEMENTS];
	protected Vector<SubFormula> subformulas = new Vector<SubFormula>();
	
	
	// helpers
	final static private String re_atom_name		= "[A-Z][a-z]*";
	final static private String re_formula_name		= "[A-Za-z]";
	final static private String re_count			= "[0-9]*";
	final static private String re_identifier		= "[a-z]?";
	final static private String re_charge			= "[+|-]?";
	final static private String re_operator			= "[+|-]?";
	final static private String re_open_all			= "[\\[|\\(]?";
	final static private String re_open_square		= "[\\[]?";
	final static private String re_close_all		= "[\\]|\\)]?";
	final static private String re_close_square		= "[\\]]?";
	final static private String re_formula			= "[" + re_atom_name + re_count + "]+";
	final static private Pattern pa_iontype = Pattern.compile(
			"(" + re_open_square + ")"
			+
			"([" + re_formula_name + re_count + "]+)"
			+
			"([" + re_operator + re_count + re_formula + "]*)"
			+
			"(" + re_close_square + ")"
			+
			"(" + re_charge + ")"
		);
	final static private Pattern pa_iontype_formulaname = Pattern.compile(
			"(" + re_formula_name + ")("+ re_count + ")"
		);
	final static private Pattern pa_iontype_formulas = Pattern.compile(
			"(" + re_operator + ")("+ re_count + ")(" + re_formula + ")"
		);
	
	final static private Pattern pa_formula = Pattern.compile(
			"(" + re_open_all + ")"
			+
			"(" + re_formula + ")"
			+
			"(" + re_charge + ")"
			+
			"(" + re_close_all + ")"
			+
			"(" + re_identifier + ")"
			+
			"(" + re_count + ")"
		);
	final static private Pattern pa_subformula = Pattern.compile(
			"(" + re_atom_name + ")(" + re_count + ")"
		);
	
	private static int[] parseFormula(String formula)
	{
		int elements[] = new int[PeriodicTable.NR_ELEMENTS];
		for (int elementid=0; elementid<PeriodicTable.NR_ELEMENTS; ++elementid)
			elements[elementid] = 0;
		Matcher ma_subformula = pa_subformula.matcher(formula);
		while (ma_subformula.find())
		{
			for (int j=1; j<=ma_subformula.groupCount(); j+=2)
			{
				// 1 - atom; 2 - count
				String identifier = ma_subformula.group(j);
				String count = ma_subformula.group(j+1);
				
				PeriodicTable.Element element = PeriodicTable.getElement(identifier);
				if (element == null)
					throw new RuntimeException("Unknown element: " + identifier);
				elements[element.id] += (count.length()==0 ? 1 : Integer.parseInt(count));
			}
		}
		return elements;
	}
	
	public static void main(String args[])
	{
		try
		{
			System.out.println(PeriodicTable.adducts_positive[0].formula);
			
			MolecularFormula formula1 = new MolecularFormula("[M1];[C2H3NNa]n");
			MolecularFormula formula2 = new MolecularFormula("C2H3NaN+");
			
			System.out.println(formula1.getMass(Mass.MONOISOTOPIC));
			System.out.println(formula2.getMass(Mass.MONOISOTOPIC));
			System.out.println(formula2.toString());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
