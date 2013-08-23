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
 * Implementation of a log-normal function, which can be used to get a fit to
 * log-normally distributed data. The log-normal has three degrees of freedom
 * controlling the fit to the given data: scale, mean and variance.
 */
public class LogNormalFunction extends LMAFunction implements Function
{
	/** The index of the scale-parameter in the array returned by {@link LogNormalFunction#getParameters()} */
	public static final int PARAM_SCALE		= 0;
	/** The index of the mean-parameter in the array returned by {@link LogNormalFunction#getParameters()} */
	public static final int PARAM_MEAN		= 2;
	/** The index of the variance-parameter in the array returned by {@link LogNormalFunction#getParameters()} */
	public static final int PARAM_VARIANCE	= 1;
	

	// constructor(s)
	protected LogNormalFunction()
	{
	}
	
	protected LogNormalFunction(LMA lma)
	{
		this.lma = lma;
		this.parameters = new double[lma.parameters.length];
		System.arraycopy(lma.parameters, 0, this.parameters, 0, lma.parameters.length);
		
		// grab the parameters
		m = this.parameters[PARAM_MEAN];
		s = this.parameters[PARAM_VARIANCE];
		s2 = Math.pow(s, 2);
	}
	
	/**
	 * Constructs a new log-normal function with the given parameters. The parameters
	 * should be passed as an array of 3 elements, where: element 0 is the scale
	 * parameter, element 1 is the variance parameter and the element 2 is the
	 * mean parameter.
	 * 
	 * @param parameters		The array with all the parameters.
	 */
	public LogNormalFunction(double parameters[])
	{
		this.parameters = parameters;
		
		// grab the parameters
		m = this.parameters[PARAM_MEAN];
		s = this.parameters[PARAM_VARIANCE];
		s2 = Math.pow(s, 2);
	}
	
	/**
	 * Constructs a new log-normal function with the given parameters.
	 * 
	 * @param scale				The scale-parameter.
	 * @param mean				The mean parameter.
	 * @param variance			The variance parameter.
	 */
	public LogNormalFunction(double scale, double mean, double variance)
	{
		parameters = new double[3];
		parameters[PARAM_SCALE] = scale;
		parameters[PARAM_MEAN] = mean;
		parameters[PARAM_VARIANCE] = variance;
		
		// grab the parameters
		m = this.parameters[PARAM_MEAN];
		s = this.parameters[PARAM_VARIANCE];
		s2 = Math.pow(s, 2);
	}
	
	
	// LMAFunction overrides
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
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function");
		
		return getY(x, this.parameters);
	}
	
	@Override
	public double getY(double x, double[] a)
	{
		return a[PARAM_SCALE] * Math.exp(-Math.pow(Math.log(x)-a[PARAM_MEAN], 2) / (2*Math.pow(a[PARAM_VARIANCE], 2))) / (x * a[PARAM_VARIANCE] * sqrt2pi);
	}

	@Override
	public double getPartialDerivate(double x, double[] a, int parameterIndex)
	{
		double y = getY(x, a);
		switch (parameterIndex)
		{
			case 0: return  y / a[PARAM_SCALE];
			case 1: return -y / Math.pow(a[PARAM_VARIANCE], 3) * ( a[PARAM_MEAN] + a[PARAM_VARIANCE] - Math.log(x)) * (-a[PARAM_MEAN] + a[PARAM_VARIANCE] + Math.log(x));
			case 2: return  y / Math.pow(a[PARAM_VARIANCE], 2) * (-a[PARAM_MEAN] + Math.log(x));
		}
		throw new ArrayIndexOutOfBoundsException("No such parameterIndex");
	}
	
	
	// access
	/**
	 * Returns the LMA-fit used for constructing this function. If no LMA-fit  was used
	 * for constructing this function null is returned.
	 * 
	 * @return			The LMA object used to make the fit.
	 */
	public LMA getLMA()
	{
		return lma;
	}
	
	/**
	 * Returns the quality of the fit when an LMA-fit was done. If no LMA-fit was done
	 * for this function a NullPointerException is throws. The quality is expressed in
	 * a Chi-square coeficient. Closer to zero is a better fit.
	 * 
	 * @return			The quality of the fitted function to the data as a chi-square.
	 */
	public double getQuality() throws NullPointerException
	{
		if (lma == null)
			throw new NullPointerException("No LMA-fit was used for constructing this function.");
		return lma.getRelativeChi2();
	}
	
	public double[] getParameters() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		return this.parameters;
	}
	
	public double getMean() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		
		return Math.exp(m + 0.5*s2);
	}
	
	public double getMedian() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		
		return Math.exp(m);
	}
	
	public double getStandardDeviation() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		
		return Math.sqrt(Math.exp(s2) * Math.exp((s2-1)));
	}
	
	/**
	 * This function retrieves the mode of this function. The mode is defined as
	 * the x-value where the distribution reaches its peak and is calculated with
	 * the formula: e^[m - s^2].
	 * 
	 * @return			The mode of the log-normal as an x-coordinate.
	 * @throws NullPointerException
	 * 					Thrown when no parameters are stored in the instance.
	 */
	public double getMode() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		
		return Math.exp(m-s2);
	}
	
	public double getSkewness() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		
		return (Math.exp(s2)+2) * Math.sqrt(Math.exp(s2)-1);
	}
	
	public double getVariation() throws NullPointerException
	{
		if (this.parameters == null)
			throw new NullPointerException("No parameters stored in function.");
		
		return Math.exp(s2) - 1;
	}
	
	/**
	 * Returns the starting position of the log-normal distribution. This starting
	 * position is determined by mode - e^(m-s).
	 * 
	 * @return			The x-coordinate where the log-normal curve starts.
	 */
	public double getStartX()
	{
		return getMode() - Math.exp(m-s);
	}
	
	
	// static access
	/**
	 * Implementation of the maximum likelihood estimation for log-normal distributions,
	 * which can be used as an initial best guess for the parameters for the LMA fit
	 * algorithm. It takes an array containing the x-values to be used for the fit,
	 * transforms these to log-space and calculates the  mean and standard-deviation for
	 * these values. The resulting vector can be plugged into the LMA-algorithm.
	 * 
	 * @param xvals		The x-values to estimate the parameters to.
	 * @return			The estimated parameters.
	 */
	public static double[] MLE(double xvals[])
	{
		double parameters[] = new double[3];
		
		parameters[PARAM_SCALE] = 1;
		
		parameters[PARAM_MEAN] = 0;
		for (int i=0; i<xvals.length; ++i)
			parameters[PARAM_MEAN] += Math.log(xvals[i]);
		parameters[PARAM_MEAN] /= xvals.length;
		
		parameters[PARAM_VARIANCE] = 0;
		for (int i=0; i<xvals.length; ++i)
			parameters[PARAM_VARIANCE] += Math.pow(Math.log(xvals[i]) - parameters[PARAM_MEAN], 2);
		parameters[PARAM_VARIANCE] = Math.sqrt(parameters[PARAM_VARIANCE] / xvals.length);
		
		return parameters;
	}
	
	/**
	 * Fits (i.e. estimates) the best parameters for the log-normal curve to the given data.
	 * The parameters are estimated and returned in the new LogNormalFunction object.
	 * 
	 * @param xvals			The x-values to fit the line to.
	 * @param yvals			The y-values to fit the line to.
	 * @return				A LinearFunction instance fitted to the data.
	 * @throws RuntimeException
	 * 						Thrown when an error is encountered in the fitting procedure.
	 */
	public static LogNormalFunction fit(double xvals[], double yvals[]) throws RuntimeException
	{
		LMA lma = new LMA(
				new LogNormalFunction(),
				LogNormalFunction.MLE(xvals),
				new double[][]{xvals,yvals}
			);
		lma.fit();
		
		return new LogNormalFunction(lma);
	}
	
	
	// data
	protected LMA lma = null;
	protected double parameters[] = null;
	
	protected double m = -1;
	protected double s = -1;
	protected double s2 = -1;
	
	protected static final double sqrt2pi = Math.sqrt(2 * Math.PI);
}
