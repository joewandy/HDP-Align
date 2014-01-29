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





/**
 * 
 */
public class Protein
{
	public static enum Type
	{
		ENZYME,
		SCAFOLDING
	}
	
	
	// constructor(s)
	public Protein(String id, String name, Type type)
	{
		this.id = id;
		this.name = name;
	}
	
	
	// access
	public String getID()
	{
		return id;
	}
	
	public String getName()
	{
		return name;
	}
	
	public String getGeneID()
	{
		return geneid;
	}
	
	public void setGeneID(String geneid)
	{
		this.geneid = geneid;
	}
	
	public void addProducts(Vector<String> moleculeids)
	{
		products.addAll(moleculeids);
	}
	
	public Vector<String> getProducts()
	{
		return products;
	}
	
	public void addCoFactors(Vector<String> moleculeids)
	{
		cofactors.addAll(moleculeids);
	}
	
	public Vector<String> getCoFactors()
	{
		return cofactors;
	}
	
	public void addInhibitors(Vector<String> moleculeids)
	{
		inhibitors.addAll(moleculeids);
	}
	
	public Vector<String> getInhibitors()
	{
		return inhibitors;
	}
	
	public void addSubstrates(Vector<String> moleculeids)
	{
		substrates.addAll(moleculeids);
	}
	
	public Vector<String> getSubstrates()
	{
		return substrates;
	}
	
	
	// data
	protected String id;
	protected String name;
	protected String geneid;
	
	protected Vector<String> products = new Vector<String>();
	protected Vector<String> cofactors = new Vector<String>();
	protected Vector<String> inhibitors = new Vector<String>();
	protected Vector<String> substrates = new Vector<String>();
}
