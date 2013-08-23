/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzMatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzMatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package mzmatch.util;


// java

// peakml
import peakml.*;





/**
 * Generic implementation for a match function for peaks to be used as a plugin for the
 * method {@link IPeak#match(java.util.Vector, double, peakml.IPeak.MatchCompare)}. The
 * match method finds matching peaks based on mass alone with a given delta. All matching
 * peaks are offered to the distance method for comparison.
 * <p />
 * The distance method automatically determines the distance between two peaks. The type
 * of the peaks is determined and the most optimal method used for comparing them. The
 * returned value is the distance between the two peaks, which is optimized to the smallest
 * value of all the matches on mass alone by the match method.
 */
public class MatchCompare<gPeak extends IPeak> implements IPeak.MatchCompare<gPeak>
{
	// constructor(s)
	/**
	 * Constructs a new match-compare instance with the given number of seconds as the
	 * maximum time two peaks can differ in retention time.
	 * 
	 * @param maxrtoffset		The maximum number of seconds the retention time of two peaks can differ to be considered a match.
	 */
	public MatchCompare(double maxrtoffset)
	{
		this.maxrtoffset = maxrtoffset;
	}
	
	
	// MatchCompare implementation
	@SuppressWarnings("unchecked")
	public double distance(gPeak peak1, gPeak peak2) throws RuntimeException
	{
		// sanity check
		if (!peak1.getClass().equals(peak2.getClass()))
			throw new RuntimeException(
					"Comparing two different peak-types: '" + peak1.getClass().getName() + "' and '" + peak2.getClass().getName() + "'"
				);
		
		// check whether the retention time is close enough
		if (Math.abs(peak1.getRetentionTime()-peak2.getRetentionTime()) > maxrtoffset)
			return -1;
		
		// validate the match
		if (peak1.getClass().equals(IPeakSet.class))
			return calculate((IPeakSet<IPeak>) peak1, (IPeakSet<IPeak>) peak2);
		else if (peak1.getClass().equals(MassChromatogram.class))
			return calculate((MassChromatogram<Peak>) peak1, (MassChromatogram<Peak>) peak2);
		else
			return -1;
	}
	
	
	// real implementations
	protected final double calculate(MassChromatogram<Peak> mc1, MassChromatogram<Peak> mc2)
	{
		return mc1.getSignal().compareTo(mc2.getSignal());
	}
	
	protected final double calculate(IPeakSet<IPeak> set1, IPeakSet<IPeak> set2)
	{
		if (!set1.getContainerClass().equals(set2.getContainerClass()))
			throw new RuntimeException("Comparing two peaksets with different peak contents: '" + set1.getContainerClass() + "' and '" + set2.getContainerClass() + "'");
		
		return set1.getSignal().compareTo(set2.getSignal());
	}
	
	
	// data
	protected double maxrtoffset;
}
