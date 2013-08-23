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
 * Implementation of a LOESS filter (also known as local regression) for smoothing
 * signals. The method is also called Locally Weighted Polynomial Regression, because
 * at each point in the dataset a low-degree polynomial is fit to a subset of the
 * given window-size. The polynomial function is fitted to the data using weighted
 * least squaresgiving more weight to points near the point whose response is being
 * estimated and less weight to points further away. The value of the regression function
 * for the point is then obtained by evaluating the local polynomial using the explanatory
 * variable values for that data point. The LOESS fit is complete after regression
 * function values have been computed for each of the n data points.
 * <p />
 * The disadvantage of the LOESS filter is that it requires fairly large, densely sampled
 * datasets in order to produce good models. Furthermore, the method is known to be
 * fairly computational intensive.
 * 
 * {@link http://en.wikipedia.org/wiki/Local_regression}
 */
public class LoessFilter implements Filter
{
	// constructor(s)
	/**
	 * Standard constructor, which accepts the window-size to use for the
	 * smoothing filter.
	 * 
	 * @param windowsize		The windowsize expressed in percentage of the number of data-points.
	 */
	public LoessFilter(double windowsize)
	{
		this.windowsize = windowsize;
	}
	
	
	// Filter overrides
	public double[] filter(double[] xvals, double[] yvals) throws IllegalArgumentException
	{
		if (xvals.length != yvals.length)
			throw new IllegalArgumentException("The arrays xvals and yvals need to be of equal length.");
		
		int window = (int) Math.round(xvals.length * windowsize);
		if (window <= 1)
			return yvals;

		double smooth[] = new double[xvals.length];
		double[] windowXa = new double[window];
		double[] windowYa = new double[window];
		double[] weights = new double[window];

		Jama.Matrix X = new Jama.Matrix( window, 2 );
		Jama.Matrix Y = new Jama.Matrix( window, 1 );
		Jama.Matrix W = new Jama.Matrix( window, window );

		// iterate each given point.
		for (int index=0; index<xvals.length; index++)
		{
			// Calculates window value and weights using LOWESS algorithm.
			int total = xvals.length;
			int windowStart = 0;
			double maxDistance = 0;

			// detect a proper window range first.
			if (index < window)
			{
				for (int i=0; i<window; i++)
				{
					if ((xvals[index]-xvals[i]) <= (xvals[window+i]-xvals[index]))
					{
						windowStart = i;
						maxDistance = Math.max(
								xvals[index] - xvals[windowStart],
								xvals[windowStart+window-1] - xvals[index]
							);
						break;
					}
				}
			}
			else if (index>=window && index<total-window)
			{
				for (int i=0; i<window; i++)
				{
					if (xvals[index]-xvals[index-window+1+i] <= xvals[index+1+i]-xvals[index])
					{
						windowStart = index - window + 1 + i;
						maxDistance = Math.max(
								xvals[index] - xvals[windowStart],
								xvals[windowStart+window-1] - xvals[index]
							);
						break;
					}
				}
			}
			else
			{
				for (int i=0; i<window; i++)
				{
					if ((xvals[total-1-i]-xvals[index] ) <= (xvals[index]-xvals[total-window-1-i]))
					{
						windowStart = total - window - i;
						maxDistance = Math.max(
								xvals[index] - xvals[windowStart],
								xvals[windowStart+window-1] - xvals[index]
							);
						break;
					}
				}
			}

			// construct window data
			System.arraycopy(xvals, windowStart, windowXa, 0, window);
			System.arraycopy(yvals, windowStart, windowYa, 0, window);

			// calculate weights using tricube function.
			int windowIndex = index - windowStart;

			for (int i=0; i<window; i++)
			{
				double distance = Math.abs(windowXa[windowIndex] - windowXa[i]);
				weights[i] = Math.pow(1.0-Math.pow((distance/maxDistance ), 3.0 ),3.0 );
			}

			// Apply WLS(Weighted Least Square) regression method
			// ===================================================
			// XL = Y
			// WXL = WY
			// XtWXL = XtWY
			// (XtWX)i(XtWX)L = (XtWX)i(XtWY)
			// L = (XtWX)i(XtWY)
			//
			// ** (M)i is the inverse of M.
			for (int i=0; i<window; i++)
			{
				X.set(i, 0, 1);
				X.set(i, 1, windowXa[i]);
				Y.set(i, 0, windowYa[i]);
				W.set(i, i, weights[i]);
			}

			Jama.Matrix XTW = X.transpose().times(W);

			Jama.Matrix L;
			try
			{
				L = XTW.times(X).inverse().times(XTW.times(Y));
			}
			catch (Exception ex)
			{
				// in some cases, the matrix may be singular due to too many
				// null weights, just use original value as the estimation.
				L = new Jama.Matrix(2, 1);
				L.set(0, 0, yvals[index]);
				L.set(1, 0, 0);
			}

			smooth[index] = L.get(0, 0) + L.get(1, 0) * xvals[index];
		}
		
		return smooth;
	}

	
	// data
	protected double windowsize;
}

