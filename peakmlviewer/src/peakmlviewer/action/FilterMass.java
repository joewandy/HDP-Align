/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakmlviewer.action;


// java
import java.util.*;

// metabolome
import peakml.*;
import peakml.chemistry.*;





/**
 * 
 */
public class FilterMass extends Filter
{
	// constructor(s)
	public FilterMass(double minmass, double maxmass)
	{
		this.minmass = minmass;
		this.maxmass = maxmass;
	}
	
	public FilterMass(MolecularFormula formula, double ppm, int charge)
	{
		double mass = formula.getMass(Mass.MONOISOTOPIC) / charge;
		double delta = PeriodicTable.PPM(mass, ppm);
		
		minmass = mass-delta;
		maxmass = mass+delta;
	}
	
//	public FilterMass(Vector<Double> mass, double ppm)
//	{
//	}
	
	
	// action
	public String getName()
	{
		return "Mass filter";
	}
	
	public String getDescription()
	{
		return minmass + "-" + maxmass;
	}
	
	public Vector<IPeak> execute(Vector<IPeak> peaks)
	{
		IPeakSet<IPeak> peakset = new IPeakSet<IPeak>(peaks);
		return peakset.getPeaksInMassRange(minmass, maxmass);
	}
	
	
	// data
	/** */
	protected double minmass;
	/** */
	protected double maxmass;
}
