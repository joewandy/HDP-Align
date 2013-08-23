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


// libraries
import jaolho.data.lma.*;





/**
 * Implementation of a polynomial function, which can be plugged with the order (n)
 * to use. The polynomial has the form f(x) = x^n + x^n-1 + .. + x. This class provides
 * tight integration with the LMA fit package for function fitting.
 */
public class PolynomialFunction extends LMAFunction implements Function
{
	// constructors
	protected PolynomialFunction(LMA lma)
	{
		this.lma = lma;
		this.order = lma.parameters.length - 1;
		this.parameters = new double[lma.parameters.length];
		System.arraycopy(lma.parameters, 0, this.parameters, 0, lma.parameters.length);
	}
	
	/**
	 * Constructs a new polynomial function of the given order. All the parameters are
	 * initialized to 0 (i.e. this type of function does not do anything).
	 * 
	 * @param order			The order of the polynomial.
	 */
	public PolynomialFunction(int order)
	{
		this.order = order;
	}
	
	/**
	 * Constructs a new polynomial function with the given parameters. Necessarily, the order
	 * of the function will be the number of parameters minus 1.
	 * 
	 * @param parameters	The parameters for the function.
	 */
	public PolynomialFunction(double... parameters)
	{
		this.order = parameters.length - 1;
		this.parameters = new double[parameters.length];
		System.arraycopy(parameters, 0, this.parameters, 0, parameters.length);
	}
	
	
	// access
	/**
	 * Returns the order of the polynomial function.
	 * 
	 * @return				The order of the function.
	 */
	public int getOrder()
	{
		return order;
	}
	
	
	// Function and LMAFunction overrides
	/**
	 * Returns the y-value for the given x-value with the parameters stored in the
	 * instance of the class.
	 * 
	 * @param x				The x-value.
	 * @return				The y-value for the given x-value.
	 * @throws NullPointerException
	 * 						When no parameters are stored in the class.
	 */
	public double getY(double x) throws NullPointerException
	{
		return getY(x, parameters);
	}
	
	/**
	 * Returns the y-value for the given x-value with the given parameters
	 * 
	 * @param x				The x-value.
	 * @param a				The parameters for the function.
	 * @return				The y-value for the given x-value.
	 * @throws RuntimeException
	 * 						Thrown when the parameters array a has a different size than 2 elements.
	 * @throws NullPointerException
	 * 						When the a is null.
	 */
	public double getY(double x, double[] a) throws RuntimeException, NullPointerException
	{
		double result = 0;
		for (int i=0; i<a.length; i++)
			result += Math.pow(x, i) * a[i]; 

		return result;
	}
	
	@Override
	public double getPartialDerivate(double x, double[] a, int parameterIndex)
	{
		if (parameterIndex<0 || parameterIndex>order)
			throw new RuntimeException("No such parameter index: " + parameterIndex);
		
		return Math.pow(x, parameterIndex);
	}
	
	
	// access
	/**
	 * When a fit has been performed by calling {@link PolynomialFunction#fit(int, double[], double[])}
	 * the returned object contains data on the fit. This is provided for integration purposes
	 * with the LMA fit package. It's encouraged to use the access methods of this class to
	 * get to the information in this object.
	 * 
	 * @return				The result of the fitting procedure.
	 */
	public LMA getLMA()
	{
		return lma;
	}
	
	/**
	 * Returns the array with the parameters of this function. This array is necessarily
	 * order-1 of size.
	 * 
	 * @return				The parameters of the function.
	 */
	public double[] getParameters()
	{
		return parameters;
	}
	
	
	// static access
	/**
	 * Performs an estimation of the best starting parameters for the fitting procedure. This
	 * function is used by {@link PolynomialFunction#fit(int, double[], double[])} and has been made
	 * public for integration purposes.
	 * 
	 * @return				The best parameters for starting the fit on the provided x-values.
	 */
	public static double[] MLE(int order, double xvals[])
	{
		double estimation[] = new double[order+1];
		for (int i=0; i<order+1; ++i)
			estimation[i] = 1;
		return estimation;
	}
	
	/**
	 * Fits a polynomial function of the given order through the given data. This method
	 * makes use of the LMA fit package for making the fit. The parameters are estimated
	 * and returned in the new LinearFunction object.
	 * 
	 * @param order			The order of the polynomial function.
	 * @param xvals			The x-values to fit the line to.
	 * @param yvals			The y-values to fit the line to.
	 * @return				A LinearFunction instance fitted to the data.
	 * @throws RuntimeException
	 * 						Thrown when an error is encountered in the fitting procedure.
	 */
	public static PolynomialFunction fit(int order, double xvals[], double yvals[]) throws RuntimeException
	{
		LMA lma = new LMA(
				new PolynomialFunction(order),
				MLE(order, xvals),
				new double[][]{xvals,yvals}
			);
		lma.fit();
		
		return new PolynomialFunction(lma);
	}
	
	
	// data
	protected int order;
	protected LMA lma = null;
	protected double parameters[] = null;
}
