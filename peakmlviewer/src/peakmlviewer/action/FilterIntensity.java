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
public class FilterIntensity extends Filter
{
	public static final int UNIT_ABSOLUTE			= 0;
	public static final int UNIT_RELATIVE			= 1;
	public static final int UNIT_PERCENTAGE			= 2;
	public static final int UNIT_SIZE				= 3;
	
	public static String unitnames[] = new String[] {
		"Absolute",
		"Relative",
		"Percentage"
	};
	
	
	// constructor(s)
	public FilterIntensity(double intensity, int units)
	{
		this.units = units;
		this.intensity = intensity;
	}
	
	
	// action
	public String getName()
	{
		return "Intensity filter";
	}
	
	public String getDescription()
	{
		return intensity + " (" + unitnames[units] + ")";
	}
	
	public Vector<IPeak> execute(Vector<IPeak> peaks)
	{
		Vector<IPeak> filtered = new Vector<IPeak>();
		if (units == UNIT_ABSOLUTE)
		{
			for (IPeak ipeak : peaks)
				if (ipeak.getIntensity() > intensity)
					filtered.add(ipeak);
		}
		else if (units == UNIT_RELATIVE)
		{
		}
		else if (units == UNIT_PERCENTAGE)
		{
		}
		
		return filtered;
	}
	
	
	// data
	/** */
	protected int units;
	/** */
	protected double intensity;
}
