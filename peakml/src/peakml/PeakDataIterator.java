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



package peakml;


// java
import java.util.*;





/**
 * An iterator for peak-data, allowing for sequential access to {@link Peak} data
 * stored in a {@link PeakData} instance. It uses the {@link Centroid} or
 * {@link Profile} implementations for access to the different elements of a data
 * point.
 */
public class PeakDataIterator<gPeak extends Peak> implements Iterator<gPeak>
{
	// constructor
	protected PeakDataIterator()
	{
	}
	
	protected PeakDataIterator(PeakData<gPeak> peakdata)
	{
		index = 0;
		this.peakdata = peakdata;
	}
	
	
	// access
	/**
	 * Returns true when there are still more elements in the {@link PeakData} instance
	 * to be traversed.
	 * 
	 * @return				True when there are still more elements.
	 */
	public boolean hasNext()
	{
		return index < peakdata.size;
	}

	/**
	 * Returns the current {@link Peak} instance and moves the iterator one step further.
	 * 
	 * @return				The next {@link Peak} instance.
	 */
	public gPeak next()
	{
		return peakdata.getPeak(index++);
	}

	/**
	 * This function has not been implemented and throws a RuntimeException on
	 * invocation.
	 */
	public void remove()
	{
		throw new RuntimeException("Remove operation not supported for PeakDataIterator.");
	}
	
	
	// data
	protected int index;
	protected PeakData<gPeak> peakdata;
}
