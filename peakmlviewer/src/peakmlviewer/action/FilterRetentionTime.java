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

// peakml
import peakml.*;





/**
 * 
 */
public class FilterRetentionTime extends Filter
{
	// constructor(s)
	public FilterRetentionTime(double minrt, double maxrt)
	{
		this.minrt = minrt;
		this.maxrt = maxrt;
	}
	
	
	// action
	public String getName()
	{
		return "Retention-time filter";
	}
	
	public String getDescription()
	{
		return minrt + "-" + maxrt;
	}
	
	public Vector<IPeak> execute(Vector<IPeak> peaks)
	{
		Vector<IPeak> peaks_filtered = new Vector<IPeak>();
		for (IPeak peak : peaks)
		{
			if (peak.getScanID()>=minrt && peak.getScanID()<=maxrt)
				peaks_filtered.add(peak);
		}
		return peaks_filtered;
	}
	
	
	// data
	protected double minrt;
	protected double maxrt;
}
