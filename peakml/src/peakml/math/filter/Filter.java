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



package peakml.math.filter;


// java

// peakml





/**
 * Interface for classes implementing a filter on a signal (a combination between
 * x-values and y-values). All classes implementing a filter should inherit from
 * this interface as it can be used at various location like {@link peakml.math#Signal}.
 */
public interface Filter
{
	/**
	 * With this method actual filtering is performed. The xvals and yvals arrays
	 * should be of equal length, otherwise an IllegalArgumentException is thrown.
	 * The return value is the new yvals array, with smoothed out values. The
	 * returned array is of the same size as the x and y value arrays.
	 * 
	 * @param xvals		The x-values of the signal.
	 * @param yvals		The y-values of the signal.
	 * @return			The resulting smoothed version of the y-values.
	 */
	public double[] filter(double xvals[], double yvals[]) throws IllegalArgumentException;
}
