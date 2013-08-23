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



package peakml.math;


// java
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;





/**
 * Implementation of a approaches for interpreting and analysing signals. In the context
 * of this project a signal is made up of mass spectrometry data or data from closely
 * related technologies.
 */
public class Signal
{
	public class Extremes
	{
		public final Vector<Double> minima = new Vector<Double>();
		public final Vector<Double> maxima = new Vector<Double>();
	}
	
	
	// constructor(s)
	/**
	 * This constructor creates an empty signal (no elements).
	 */
	public Signal()
	{
		xvals = new double[0];
		yvals = new double[0];
	}
	
	/**
	 * This constructor creates a signal of the given size. The x-values are initialized
	 * in ascending order and the y-values initialized to 0.
	 * 
	 * @param size			The size of the signal.
	 */
	public Signal(int size)
	{
		Init(size);
		for (int i=0; i<size; ++i)
		{
			xvals[i] = i;
			yvals[i] = 0;
		}
	}
	
	/**
	 * This constructor creates a signal out of the given y-values. The size of the signal
	 * is set to the length of the given array. The x-values are initialized in ascending
	 * order and the contents of the given array is copied to the y-values.
	 * 
	 * @param yvals			The y-values to initialize the signal with.
	 */
	public Signal(double[] yvals)
	{
		if (yvals == null)
			throw new NullPointerException();
		
		Init(yvals.length);
		
		// create the x-values
		for (int i=0; i<yvals.length; ++i)
			xvals[i] = i;
		
		// copy the data
		System.arraycopy(yvals, 0, this.yvals, 0, yvals.length);
	}
	
	/**
	 * This constructor creates a signal out of the given x- and y-values. Both the
	 * arrays need to have the same length, otherwise an exception is thrown.
	 * 
	 * @param xvals			The x-values.
	 * @param yvals			The y-values.
	 * @throws NullPointerException
	 * 						Thrown when null was passed to one of both of the parameters.
	 * @throws IllegalArgumentException
	 * 						Thrown when the size of the arrays is not equal.
	 */
	public Signal(double[] xvals, double[] yvals) throws NullPointerException, IllegalArgumentException
	{
		if (xvals==null || yvals==null)
			throw new NullPointerException();
		if (xvals.length != yvals.length)
			throw new IllegalArgumentException("Array-lengths are not the same: " + xvals.length + " - " + yvals.length);
		
		Init(yvals.length);
		
		// copy the data
		System.arraycopy(xvals, 0, this.xvals, 0, xvals.length);
		System.arraycopy(yvals, 0, this.yvals, 0, yvals.length);
	}
	
	/**
	 * Copy-constructor, which copies the contents of the given signal to this signal.
	 * 
	 * @param signal		The signal to copy.
	 */
	public Signal(Signal signal)
	{
		this(signal.xvals, signal.yvals);
	}
	
	/**
	 * Initializes the signal to the given size. Beware that the newly created arrays
	 * are not initialized.
	 * 
	 * @param size			The size to initialize the signal to.
	 */
	public void Init(int size)
	{
		xvals = new double[size];
		yvals = new double[size];
	}
	
	
	// access
	/**
	 * Returns the internal array with all the x-values. This is intended for accessing
	 * the x-values only and should not be changed unless the signal needs to be
	 * adapted.
	 * 
	 * @return				The array with all the x-values.
	 */
	public double[] getX()
	{
		return xvals;
	}
	
	/**
	 * Returns the internal array with all the y-values. This is intended for accessing
	 * the y-values only and should not be changed unless the signal needs to be
	 * adapted.
	 * 
	 * @return				The array with all the y-values.
	 */
	public double[] getY()
	{
		return yvals;
	}
	
	/**
	 * Returns the y-value at the given x-value. This method tries to find the real
	 * value when it is available, otherwise the y-value is estimated by looking for
	 * the x-value smaller than the given x and the x-value larger than the given x
	 * and the mean of the two y-values is returned. If the x-value is smaller or
	 * larger than the minimum or maximum x-value 0 is returned.
	 * 
	 * @param x				The x-value.
	 * @return				The y-value corresponding to the given x-value.
	 */
	public double getY(double x)
	{
		if (x<xvals[0] || xvals[xvals.length-1]<x)
			return 0;
		
		double min = 0;
		double max = 0;
		for (int i=0; i<xvals.length; ++i)
		{
			if (xvals[i] == x)
				return yvals[i];
			else if (xvals[i] < x)
				min = yvals[i];
			else
			{
				max = yvals[i];
				break;
			}
		}
		return (min+max) / 2.;
	}
	
	/**
	 * Returns a matrix with the x- and y-values. The x-values can be retrieved with
	 * matrix[0] and the y-values with matrix[1].
	 * 
	 * @return				A matrix with the x- and y-values.
	 */
	public double[][] getXY()
	{
		return new double[][]{xvals,yvals};
	}
	
	/**
	 * Returns the number of data-points in this signal. This is basically the size of
	 * both the x- and y-value arrays (which are required to be the same).
	 * 
	 * @return				The number of data-points in this signal.
	 */
	public int getSize()
	{
		return yvals.length;
	}
	
	/**
	 * Returns the minimum x-value.
	 * 
	 * @return				The minimum x-value.
	 */
	public double getMinX()
	{
		return xvals[0];
	}
	
	/**
	 * Returns the minimum y-value. This value is calculated in this method.
	 * 
	 * @return				The minimum y-value.
	 */
	public double getMinY()
	{
		if (yvals.length == 0)
			return -1;
		
		double min = yvals[0];
		for (int i=1; i<yvals.length; ++i)
			min = Math.min(min, yvals[i]);
		return min;
	}
	
	/**
	 * Returns the maximum x-value.
	 * 
	 * @return				The maximum x-value.
	 */
	public double getMaxX()
	{
		return xvals[xvals.length-1];
	}
	
	/**
	 * Returns the maximum y-value. This value is calculated in this method.
	 * 
	 * @return				The maximum y-value.
	 */
	public double getMaxY()
	{
		if (yvals.length == 0)
			return -1;
		
		double max = yvals[0];
		for (int i=1; i<yvals.length; ++i)
			max = Math.max(max, yvals[i]);
		return max;
	}
	
	/**
	 * 
	 * @return
	 */
	public int indexMaxY()
	{
		if (yvals.length == 0)
			return -1;
		
		int maxi = 0;
		double max = yvals[0];
		for (int i=1; i<yvals.length; ++i)
		{
			if (yvals[i] > max)
			{
				maxi = i;
				max = yvals[i];
			}
		}
		return maxi;
	}
	
	/**
	 * Returns the mean y-value. This value is calculated in this method.
	 * 
	 * @return				The mean y-value.
	 */
	public double getMeanY()
	{
		double yy = 0;
		for (double y : yvals)
			yy += y;
		return yy / yvals.length;
	}
	
	/**
	 * Returns the x-value where the signal reaches its maximum y-value. If there
	 * is a tie between 2 or more values the smallest x-value is returned.
	 * 
	 * @return				The x-value of the maximum y-value.
	 */
	public double getPeakX()
	{
		double maxx = 0;
		double maxy = 0;
		for (int i=0; i<yvals.length; ++i)
		{
			if (yvals[i] > maxy)
			{
				maxy = yvals[i];
				maxx = xvals[i];
			}
		}
		
		return maxx;
	}
	
	/**
	 * This function scales the y-values between 0 and 1. It calculates the maximum
	 * y-value and divides all the y-values with thus maximum, effectively scaling
	 * the data. Effectively {@link Signal#getMaxY()} and {@link Signal#normalize()}
	 * are called.
	 */
	public void normalize()
	{
		normalize(getMaxY());
	}
	
	/**
	 * This function scales the y-values between 0 and the given max.
	 * 
	 * @param max			The maximum y-value to scale to.
	 */
	public void normalize(double max)
	{
		for (int i=0; i<yvals.length; ++i)
			yvals[i] /= max;
	}	
	
	/**
	 * Compares the given signal to this signal. Essentially the difference in area
	 * under the two curves is calculated, resulting in a single value which if small
	 * indicates the two signals are very similar and if large indicates the two
	 * signals are very unlike.
	 * <p />
	 * The method starts by copying both signals and normalizing them to 1. The minimum
	 * and maximum x-value is calculated for the combination of both the signals. By
	 * starting at the minimum value and incrementing by 1 all the y-value differences
	 * between both signals are calculated and summed. This is the returned value.
	 * 
	 * @param other			The signal to compare this signal to.
	 * @return				The total difference in y-values between both the signals.
	 */
	public double compareTo(Signal other)
	{
		Signal s1 = new Signal(xvals, yvals);
		s1.normalize();
		Signal s2 = new Signal(other.xvals, other.yvals);
		s2.normalize();
		
		double difference = 0;
		for (double x=Math.min(s1.getMinX(), s2.getMinX()); x<Math.max(s1.getMaxX(), s2.getMaxX()); ++x)
			difference += Math.pow(s1.getY(x)-s2.getY(x), 2);
		
		return difference;
	}
	
	/**
	 * Calculates the Pearson's correlation between this and the given signal. This
	 * method does the book keeping in order to call {@link Statistical#pearsonsCorrelation(double[], double[])}.
	 * Only the correlation is returned, even though the called method also returns
	 * test values.
	 * 
	 * @param other			The signal to compare this signal to.
	 * @return				The Pearson's correlation.
	 */
	public double[] pearsonsCorrelation(Signal other)
	{
		if (getSize() < other.getSize())
			return other.pearsonsCorrelation(this);
		
		Signal me_normal = new Signal(this.xvals, this.yvals);
		me_normal.normalize();
		Signal other_normal = new Signal(other.xvals, other.yvals);
		other_normal.normalize();
		
		double xmin = Math.min(me_normal.getMinX(), other_normal.getMinX());
		double xmax = Math.max(me_normal.getMaxX(), other_normal.getMaxX());
		
		// changed by joe ...
		// double xvals[] = new double[(int) (xmax-xmin) + 1];
		// double yvals[] = new double[(int) (xmax-xmin) + 1];
		// for (double x=xmin; x<xmax; ++x)
		List<Double> xvals = new ArrayList<Double>();
		List<Double> yvals = new ArrayList<Double>();
		final double quantum = 0.01;
		double x = xmin;
		for (;;)
		{
			// xvals[(int) (x-xmin)] = me_normal.getY(x);
			// yvals[(int) (x-xmin)] = other_normal.getY(x);
			xvals.add(me_normal.getY(x));
			yvals.add(other_normal.getY(x));
			x += quantum;
			if (Double.compare(x, xmax) > 0) {
				break;
			}
		}
		
		return Statistical.pearsonsCorrelation(toArray(xvals), toArray(yvals));
	}
	
	private double[] toArray(List<Double> doubles) {
		 double[] target = new double[doubles.size()];
		 for (int i = 0; i < target.length; i++) {
		    target[i] = doubles.get(i);
		 }
		 return target;
	}
	
	/**
	 * Calculates the Spearman correlation between this and the given signal. This
	 * method does the book keeping in order to call {@link Statistical#spearmanCorrelation(double[], double[])}.
	 * Only the correlation is returned, even though the called method also returns
	 * test values.
	 * 
	 * @param other			The signal to compare this signal to.
	 * @return				The Pearson's correlation.
	 */
	public double spearmanCorrelation(Signal other)
	{
		if (getSize() < other.getSize())
			return other.spearmanCorrelation(this);
		
		Signal me_normal = new Signal(this.xvals, this.yvals);
		me_normal.normalize();
		Signal other_normal = new Signal(other.xvals, other.yvals);
		other_normal.normalize();
		
		double xmin = Math.min(me_normal.getMinX(), other_normal.getMinX());
		double xmax = Math.max(me_normal.getMaxX(), other_normal.getMaxX());
		
		double xvals[] = new double[(int) (xmax-xmin) + 1];
		double yvals[] = new double[(int) (xmax-xmin) + 1];
		for (double x=xmin; x<xmax; ++x)
		{
			xvals[(int) (x-xmin)] = me_normal.getY(x);
			yvals[(int) (x-xmin)] = other_normal.getY(x);
		}
		
		return Statistical.spearmanCorrelation(xvals, yvals)[Statistical.PEARSON_CORRELATION];
	}
	
	public Extremes getLocalExtremes(int windowsize)
	{
		Extremes extremes = new Extremes();
		if (windowsize>=this.getSize() || windowsize<=2)
			return extremes;
		
		int size = (int) (windowsize/2.);
		double window1[] = new double[size];
		double window2[] = new double[size];
		
		for (int i=0; i<size; ++i)
			window1[i] = yvals[0];
		for (int i=0; i<size; ++i)
			window2[i] = yvals[i+1];
		
		double prev = -1;
		for (int i=0; i<getSize(); ++i)
		{
			// calculate the means
			double mean1 = 0;
			for (int j=1; j<size; ++j)
				mean1 += window1[j-1] - window1[j];
			mean1 /= size;
			double mean2 = 0;
			for (int j=1; j<size; ++j)
				mean2 += window2[j-1] - window2[j];
			mean2 /= size;
			
			// 
			double val = (mean1 + mean2) / 2.;
			if (prev!=-1 && prev>0 && val<0)
				extremes.minima.add(xvals[i-1]);
			else if (prev!=-1 && prev<0 && val>0)
				extremes.maxima.add(xvals[i-1]);
			prev = val;
			
			// move the window
			for (int j=1; j<size; ++j)
			{
				window1[j-1] = window1[j];
				window2[j-1] = window2[j];
			}
			
			window1[size-1] = yvals[i];
			if (i+size >= getSize()-1)
				window2[size-1] = yvals[yvals.length-1];
			else
				window2[size-1] = yvals[i+size+1];
		}
		
		return extremes;
	}
	
	public Vector<Double> getLocalMinima(int windowsize)
	{
		return getLocalExtremes(windowsize).minima;
	}
	
	public Vector<Double> getLocalMaxima(int windowsize)
	{
		return getLocalExtremes(windowsize).maxima;
	}
	
	
	// Graph functionality
	public JFreeChart createGraph(String name, String xlabel, String ylabel)
	{
		// create the series-container for our data
		org.jfree.data.xy.XYSeries series = new org.jfree.data.xy.XYSeries(name);
		org.jfree.data.xy.XYSeriesCollection dataset = new org.jfree.data.xy.XYSeriesCollection();
		
		for (int i=0; i<getSize(); ++i)
			series.add(xvals[i], yvals[i]);
		
		dataset.addSeries(series);
		
		// create the line-chart
		JFreeChart linechart = ChartFactory.createXYLineChart(
				null, xlabel, ylabel, dataset, PlotOrientation.VERTICAL,
				false, // legend
				false, // tooltips
				false  // urls
			);
		linechart.setTitle(name);
		linechart.setBackgroundPaint(Color.WHITE);
		
		// 
		return linechart;
	}
	
	public BufferedImage createGraphImage(String name, String xlabel, String ylabel, int width, int height)
	{
		JFreeChart linechart = createGraph(name, xlabel, ylabel);
		
		// create the image
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		// create the graphics context
		Graphics2D g = img.createGraphics();
		
		// draw the line chart
		linechart.draw(g, new Rectangle(0, 0, width, height));
		
		// return the result
		return img;
	}

	
	
	// Object overrides
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		for (int i=0; i<yvals.length; ++i)
		{
			str.append(xvals[i]);
			str.append("\t");
			str.append(yvals[i]);
			str.append("\n");
		}
		
		return str.toString();
	}
	
	
	// data
	protected double[] xvals = null;
	protected double[] yvals = null;
}


