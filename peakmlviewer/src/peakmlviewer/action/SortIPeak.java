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





/**
 * 
 */
public class SortIPeak extends Sort
{
	public static final int SORT_NONE						= 0;
	public static final int SORT_MASS_ASCENDING				= 1;
	public static final int SORT_MASS_DESCENDING			= 2;
	public static final int SORT_INTENSITY_ASCENDING		= 3;
	public static final int SORT_INTENSITY_DESCENDING		= 4;
	public static final int SORT_RETENTIONTIME_ASCENDING	= 5;
	public static final int SORT_RETENTIONTIME_DESCENDING	= 6;
	public static final int SORT_SIZE						= 7;
	
	public static final String sort_names[] = new String[] {
		"none",
		"mass ascending",
		"mass descending",
		"intensity ascending",
		"intensity descending",
		"retention-time ascending",
		"retention-time descending",
	};
	
	
	// constructor(s)
	public SortIPeak(int sort)
	{
		this.sort = sort;
	}
	
	
	// action
	public String getName()
	{
		return "IPeak sort";
	}
	
	public String getDescription()
	{
		return sort_names[sort];
	}
	
	public Vector<IPeak> execute(Vector<IPeak> peaks)
	{
		if (sort == SORT_MASS_ASCENDING)
			Collections.sort(peaks, IPeak.sort_mass_ascending);
		else if (sort == SORT_MASS_DESCENDING)
			Collections.sort(peaks, IPeak.sort_mass_descending);
		else if (sort == SORT_INTENSITY_ASCENDING)
			Collections.sort(peaks, IPeak.sort_intensity_ascending);
		else if (sort == SORT_INTENSITY_DESCENDING)
			Collections.sort(peaks, IPeak.sort_intensity_descending);
		else if (sort == SORT_RETENTIONTIME_ASCENDING)
			Collections.sort(peaks, IPeak.sort_scanid_ascending);
		else if (sort == SORT_RETENTIONTIME_DESCENDING)
			Collections.sort(peaks, IPeak.sort_scanid_descending);
		
		return peaks;
	}
	
	
	// data
	protected int sort = SORT_NONE;
}
