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
import java.util.Collection;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;





/**
 * Implementation of a class tracking all the properties of a molecule. This can be used
 * to load databases with molecule information from files with the 
 * {@link peakml#io#chemistry#MoleculeIO} class.
 */
public class Molecule
{
	// constructor(s)
	/**
	 * Constructs a new {@link Molecule} instance with the given id, name and molecular
	 * formula.
	 * 
	 * @param databaseid		The unique id of the molecule.
	 * @param name				The name of the molecule.
	 * @param formula			The molecular formula of the molecule.
	 */
	public Molecule(String databaseid, String name, String formula)
	{
		this.databaseid = databaseid;
		this.name = name;
		this.formula = new MolecularFormula(formula);
	}
	
	/**
	 * Constructs a new {@link Molecule} instance with the given id, name and molecular
	 * formula.
	 * 
	 * @param databaseid		The unique id of the molecule.
	 * @param name				The name of the molecule.
	 * @param formula			The molecular formula of the molecule.
	 */
	public Molecule(String databaseid, String name, MolecularFormula formula)
	{
		this.databaseid = databaseid;
		this.name = name;
		this.formula = formula;
	}
	
	/**
	 * Constructs a new {@link Molecule} instance with the given id, name and mass. This
	 * options has been included to support files with only mass information, but no
	 * formulae.
	 * 
	 * @param databaseid		The unique id of the molecule.
	 * @param name				The name of the molecule.
	 * @param mass				The mass of the molecule.
	 */
	public Molecule(String databaseid, String name, double mass)
	{
		this.databaseid = databaseid;
		this.name = name;
		this.mass = mass;
	}
	
	// access
	/**
	 * Returns the unique id of the molecule.
	 * 
	 * @return					The unique id of the molecule.
	 */
	public String getDatabaseID()
	{
		return databaseid;
	}
	
	public void setDatabaseID(String id)
	{
		databaseid = id;
	}
	
	/**
	 * Returns the name of the molecule.
	 * 
	 * @return					The name of the molecule.
	 */
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns the molecular formula of the molecule.
	 * 
	 * @return					The molecular formula of the molecule.
	 */
	public MolecularFormula getFormula()
	{
		return formula;
	}
	
	public String getPlainFormula() {
		// e.g. to get "C8H7NO3" out of "[M1];[C8H7NO3]n"
		Pattern pattern = Pattern.compile(";\\[(.*?)\\]");
		final Matcher m = pattern.matcher(formula.toString());
		String result = formula.toString();
		if (m.find()) {
			result = m.group(1);
		}
		return result;
	}
	
	/**
	 * Sets the override mass of the molecule.
	 * 
	 * @param mass			The mass.
	 */
	public void setMass(double mass) {
		this.mass = mass;
	}
	
	/**
	 * Returns the mass of the molecule. The given
	 * masstype can be used to either retrieve the monoisotopic (most common
	 * isotopes) or the molecular (average of all the isotopes as they occur
	 * naturally) mass. The calculation of the mass is defered to
	 * {@link MolecularFormula#getMass(Mass)}.
	 * 
	 * @param masstype			The mass type.
	 * @return					The mass of the molecule.
	 */
	public double getMass(Mass masstype)
	{
		if ( formula != null ) {
			return formula.getMass(masstype);
		}
		return this.mass;
	}
	
	/**
	 * Returns the mass of the molecule. If override has been specified
	 * and the override mass is present,
	 * this is returned. Otherwise the calculated mass is returned. The given
	 * masstype can be used to either retrieve the monoisotopic (most common
	 * isotopes) or the molecular (average of all the isotopes as they occur
	 * naturally) mass. The calculation of the mass is defered to
	 * {@link MolecularFormula#getMass(Mass)}.
	 * 
	 * @param masstype			The mass type.
	 * @param override			Whether to use the override mass if present
	 * @return					The mass of the molecule.
	 */
	public double getMass(Mass masstype, boolean override) {
		if ( override && this.mass != -1 ) {
			return this.mass;
		}
		return this.getMass(masstype);
	}
	
	/**
	 * Sets the InChi string describing the molecular structure of the molecule.
	 * This string can be used to draw the structure.
	 * 
	 * @param inchi				The InChi string describing the structure of the molecule.
	 */
	public void setInChi(String inchi)
	{
		this.inchi = inchi;
	}
	
	/**
	 * Returns the InChi string describing the molecular structure of the molecule.
	 * This string can be used to draw the structure.
	 * 
	 * @return					The InChi string describing the structure of the molecule.
	 */
	public String getInChi()
	{
		return inchi;
	}
	
	/**
	 * Sets the smiles string describing the molecular structure of the molecule.
	 * This string can be used to draw the structure.
	 * 
	 * @param smiles			The smiles string describing the structure of the molecule.
	 */
	public void setSmiles(String smiles)
	{
		this.smiles = smiles;
	}
	
	/**
	 * Returns the smiles string describing the molecular structure of the molecule.
	 * This string can be used to draw the structure.
	 * 
	 * @return					The smiles string describing the structure of the molecule.
	 */
	public String getSmiles()
	{
		return smiles;
	}
	
	/**
	 * Optional description for the molecule. When this is not set a null-pointer is used
	 * to avoid too much memory consumption.
	 * 
	 * @param description		The description.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	/**
	 * Returns the optional description for the molecule. When this is not set a null-pointer
	 * is used to avoid too much memory consumption.
	 * 
	 * @return					The description.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Optional class-description for the molecule. When this is not set a null-pointer is used
	 * to avoid too much memory consumption.
	 * 
	 * @param description		The description.
	 */
	public void setClassDescription(String description)
	{
		this.classdescription = description;
	}
	
	/**
	 * Returns the optional description for the molecule. When this is not set a null-pointer
	 * is used to avoid too much memory consumption.
	 * 
	 * @return					The description.
	 */
	public String getClassDescription()
	{
		return classdescription;
	}
	
	/**
	 * Adds the list of synonyms to the internal synonyms list. When a name is already present
	 * it is not added again.
	 * 
	 * @param synonyms			The list of synonyms to be added.
	 */
	public void setSynonyms(String synonyms[])
	{
		for (String synonym : synonyms)
			if (!synonym.equals(name))
				this.synonyms.add(synonym);
	}
	
	/**
	 * Adds the list of synonyms to the internal synonyms list. When a name is already present
	 * it is not added again.
	 * 
	 * @param synonyms			The list of synonyms to be added.
	 */
	public void setSynonyms(Collection<String> synonyms)
	{
		for (String synonym : synonyms)
			if (!synonym.equals(name))
				this.synonyms.add(synonym);
	}
	
	/**
	 * Returns the list of synonyms associated to this molecule.
	 * 
	 * @return					The list of synonyms.
	 */
	public Vector<String> getSynonyms()
	{
		return synonyms;
	}
	
	/**
	 * Sets the optional expected retention time for this molecule.
	 * 
	 * @param rt				The retention time in minutes.
	 */
	public void setRetentionTime(double rt)
	{
		// TODO this needs to be extended to support multiple columns.
		this.retentiontime = rt * 60;
	}
	
	/**
	 * Returns the optional expected retention time for this molecule.
	 * 
	 * @return					The retention time.
	 */
	public double getRetentionTime()
	{
		return this.retentiontime;
	}
	
	/**
	 * Returns the optional polarity for this molecule.
	 * 
	 * @return					The polarity.
	 */
	public Polarity getPolarity() {
		return this.polarity;
	}

	/**
	 * Sets the optional polarity for this molecule.
	 * 
	 * @param polarity				The polarity.
	 */
	public void setPolarity(final Polarity polarity) {
		this.polarity = polarity;
	}
	
	
	// Object override
	@Override
	public String toString()
	{
		return "MOLECULE: " + formula.toString();
	}
	
	@Override
	public boolean equals(Object other)
	{
		if (!(other instanceof Molecule))
			return false;
		
		// try to compare on inchi, which is best; then on smiles, etc.
		Molecule m = (Molecule) other;
		if (m.inchi!=null && inchi!=null)
			return m.inchi.equals(inchi);
		else if (m.smiles!=null && smiles!=null)
			return m.smiles.equals(smiles);
		else
			return m.formula.equals(formula);
	}
	
	
	// data
	protected String databaseid = null;
	
	protected String name = null;
	protected Vector<String> synonyms = new Vector<String>();
	
	protected double retentiontime = -1;
	protected double mass = -1;
	protected String inchi = null;
	protected String smiles = null;
	protected String description = null;
	protected String classdescription = null;
	protected MolecularFormula formula = null;
	protected Polarity polarity;
}
