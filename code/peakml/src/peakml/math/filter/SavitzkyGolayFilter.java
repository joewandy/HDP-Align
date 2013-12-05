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





/**
 * Implementation of the Savitzky-Golay filter taken from Numerical Recipes in C
 * (chapter 14.8, page 650). This filter smoothes the incoming signal by replacing
 * each value in the series with the value obtained from a polynomial fit on the
 * window around the value with size 2n+1.
 * <p />
 * The Points-enum indicates the possible window-sizes. This has been restricted
 * due to the fact that the algorithm utilizes pre-calculated coefficients. A
 * smaller window-size keeps the signal intact, but removes less noise.
 */
public class SavitzkyGolayFilter implements Filter
{
	/**
	 * Fixed definition of the number of data points to use for smoothing. The
	 * range is restricted because the algorithm utilizes pre-calculated coefficients.
	 */
	public enum Points {
		FIVE, SEVEN, NINE,
		ELEVEN, THIRTEEN, FIFTEEN, SEVENTEEN, NINETEEN,
		TWENTYONE, TWENTYTHREE, TWENTYFIVE
	}
	
	
	// constructor(s)
	/**
	 * Standard constructor, which accepts the number of data points to use in for
	 * the smoothing filter.
	 * 
	 * @param points		The number of data points to use.
	 */
	public SavitzkyGolayFilter(Points points)
	{
		this.points = points.ordinal();
	}
	
	
	// Filter overrides
	public double[] filter(double xvals[], double yvals[]) throws IllegalArgumentException
	{
		if (xvals.length != yvals.length)
			throw new IllegalArgumentException("The arrays xvals and yvals need to be of equal length.");
		
		int n = xvals.length;
		double smooth[] = new double[n];
		
		// collect the constants applicable for this case
		int h = hvalues[points];
		int avals[] = avalues[points];
		
		// start the process (5+points*2 actually makes the real points in the enum)
		int marginsize = (5+points*2+1) / 2 - 1;
		for (int index=marginsize; index<n-marginsize; ++index)
		{
			double value = avals[0] * yvals[index];
			for (int winindex=1; winindex<=marginsize; ++winindex)
				value += avals[winindex] * (yvals[index+winindex]+yvals[index-winindex]);
			value /= h;
			
			if (value < 0)
				value = 0;
			smooth[index] = value;
		}
		
		return smooth;
	}
	
	
	// data
	protected int points;
	
	
	// constants
	// see also: http://www.vias.org/tmdatanaleng/cc_savgol_coeff.html
	private static final int avalues[][] = new int[][] {
		{  17,  12,  -3,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0},
		{   7,   6,   3,  -2,   0,   0,   0,   0,   0,   0,   0,   0,   0},
		{  59,  54,  39,  14, -21,   0,   0,   0,   0,   0,   0,   0,   0},
		{  89,  84,  69,  44,   9, -36,   0,   0,   0,   0,   0,   0,   0},
		{  25,  24,  21,  16,   9,   0, -11,   0,   0,   0,   0,   0,   0},
		{ 167, 162, 147, 122,  87,  42, -13, -78,   0,   0,   0,   0,   0},
		{  43,  42,  39,  34,  27,  18,   7,  -6, -21,   0,   0,   0,   0},
		{ 269, 264, 249, 224, 189, 144,  89,  24, -51,-136,   0,   0,   0},
		{ 329, 324, 309, 284, 249, 204, 149,  84,   9, -76,-171,   0,   0},
		{  79,  78,  75,  70,  63,  54,  43,  30,  15,  -2, -21, -42,   0},
		{ 467, 462, 447, 422, 387, 343, 287, 222, 147,  62, -33,-138,-253},
	};
	private static final int hvalues[] = new int[] {
		35, 21, 231, 429, 143, 1105, 323, 2261, 3059, 805, 5175
	};
}
