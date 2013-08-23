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



package peakml.io;


// java

// peakml
import peakml.*;
import peakml.chemistry.*;





/**
 * Central point for collecting all information about a scan. It has been chosen to
 * maintain this information in a memory intensive class instance as it is expected
 * that more information will be incorporated for scans.
 */
public class ScanInfo extends Annotatable
{
	// constructor
	/**
	 * Constructs a new ScanInfo instance with the given retention-time and polarity. The
	 * retention is required to be in seconds, which is taken care of by the parsers.
	 * 
	 * @param retentiontime			The retention time in seconds.
	 * @param polarity				The polarity of the scan.
	 */
	public ScanInfo(double retentiontime, Polarity polarity)
	{
		this.polarity = polarity;
		this.retentiontime = retentiontime;
	}
	
	
	// access
	/**
	 * Returns the retention time of the scan in seconds.
	 * 
	 * @return						The retention time in seconds.
	 */
	public double getRetentionTime()
	{
		return retentiontime;
	}
	
	/**
	 * Returns the polarity of the scan.
	 * 
	 * @return						The polarity of the scan.
	 */
	public Polarity getPolarity()
	{
		return polarity;
	}
	
	
	// static access
	/**
	 * Convenience function for converting a time-value in seconds to a readable string
	 * representation.
	 * 
	 * @param rt					The time in seconds.
	 * @return						The string represention of the given time.
	 */
	public static String rtToString(double rt)
	{
		if (rt > 3600)
		{
			int hrs = (int) (rt/3600);
			return String.format("%02d:%02d:%02d",
					hrs, (int) ((rt/60) - hrs*60), (int) (rt%60)
				);
		}
		return String.format("%02d:%02d", (int) (rt/60), (int) (rt%60));
	}
	
	
	// data
	protected double retentiontime;
	protected Polarity polarity;
}
