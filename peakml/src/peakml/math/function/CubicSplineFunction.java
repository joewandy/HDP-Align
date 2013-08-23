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



package peakml.math.function;


// java





/**
 * This class calculates the natural cubic spline for a given set of input values. In
 * mathematics a spline is a special function defined piecewise by polynomials
 * (see {@see PolynomialFunction}). The approach attempts to solve the following equation
 * by using row operations to convert the matrix to upper triangular and then back
 * substitution. The D[i] are the derivatives at the knots.

 * <pre>
 *   gamma        D        delta
 *  [2 1       ] [D[0]]   [3(x[1] - x[0])  ]
 *  |1 4 1     | |D[1]|   |3(x[2] - x[0])  |
 *  |  1 4 1   | | .  | = |      .         |
 *  |     ...  | | .  |   |      .         |
 *  |     1 4 1| | .  |   |3(x[n] - x[n-2])|
 *  [       1 2] [D[n]]   [3(x[n] - x[n-1])]
 * </pre>
 * 
 * {@link http://en.wikipedia.org/wiki/Cubic_spline}
 * {@link http://www.cse.unsw.edu.au/~lambert/splines/source.html}
 */
public class CubicSplineFunction implements Function
{
	// constructor
	/**
	 * Constructs (i.e. fits) the polynomial functions to the given input data. This constructor
	 * is provided as a convenience mechanism.
	 * 
	 * @param xvals				The x-values.
	 * @param yvals				The y-values.
	 */
	public CubicSplineFunction(double xvals[], double yvals[])
	{
		this.xvals = xvals;
		this.yvals = yvals;
		this.xfunctions = cubic(xvals);
		this.xfunctions = cubic(yvals);
	}
	
	
	// Function overrides
	public double getY(double x)
	{
		for (int i=0; i<xvals.length-1; ++i)
			if (x>=xvals[i] && x<xvals[i+1]) return yfunctions[i].getY((x-xvals[i])/(xvals[i+1]-xvals[i]));
		return 0;
	}

	
	// static access
	/**
	 * Constructs (i.e. fits) the polynomial functions to the given input data. This function
	 * is provided as a convenience mechanism.
	 * 
	 * @param xvals				The x-values.
	 * @param yvals				The y-values.
	 * @return					The cubic spline function.
	 */
	public static CubicSplineFunction fit(double xvals[], double yvals[])
	{
		if (xvals.length==0 || yvals.length==0)
			return null;
		
		return new CubicSplineFunction(xvals, yvals, cubic(xvals), cubic(yvals));
	}
	
	protected CubicSplineFunction(double xvals[], double yvals[], PolynomialFunction xfunctions[], PolynomialFunction yfunctions[])
	{
		this.xvals = xvals;
		this.yvals = yvals;
		this.xfunctions = xfunctions;
		this.yfunctions = yfunctions;
	}
	
	protected static PolynomialFunction[] cubic(double vals[])
	{
		int n = vals.length - 1;
		
		// create the gamma vector
	    double gamma[] = new double[n+1];
		gamma[0] = 1.0f / 2.0f;
		for (int i=1; i<n; ++i)
			gamma[i] = 1 / (4-gamma[i-1]);
		gamma[n] = 1 / (2-gamma[n-1]);
	    
		// create the delta array
		double delta[] = new double[n+1];
		delta[0] = 3 * (vals[1]-vals[0]) * gamma[0];
		for (int i=1; i<n; ++i)
			delta[i] = (3 * (vals[i+1]-vals[i-1]) - delta[i-1]) * gamma[i];
		delta[n] = (3*(vals[n]-vals[n-1])-delta[n-1])*gamma[n];
	    
	    // create the D vector
	    double D[] = new double[n+1];
		D[n] = delta[n];
		for (int i=n-1; i>=0; i--)
			D[i] = delta[i] - gamma[i]*D[i+1];
		
		// compute the coefficients of the cubics
		PolynomialFunction functions[] = new PolynomialFunction[n];
		for (int i=0; i<n; i++)
		{
			functions[i] = new PolynomialFunction(
					vals[i], D[i],
					3*(vals[i+1] - vals[i]) - 2*D[i] - D[i+1],
					2*(vals[i] - vals[i+1]) + D[i] + D[i+1]
				);
		}

		return functions;
	}
	
	
	// data
	protected double xvals[];
	protected double yvals[];
	protected PolynomialFunction xfunctions[];
	protected PolynomialFunction yfunctions[];
}

