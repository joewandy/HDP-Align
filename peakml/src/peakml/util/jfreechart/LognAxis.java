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



package peakml.util.jfreechart;


// java
import java.awt.geom.Rectangle2D;

// jfreechart
import org.jfree.ui.*;
import org.jfree.data.*;

import org.jfree.chart.axis.*;





/**
 * 
 */
@SuppressWarnings("serial")
public class LognAxis extends NumberAxis
{
	// constructor(s)
	public LognAxis(String label, int logscale)
	{
		super(label);
		this.logscale = logscale;
		this.lognvalue = Math.log(logscale);
	}
	
	
	// access
	public int getLogScale()
	{
		return logscale;
	}
	
	public void setLogScale(int logscale)
	{
		this.logscale = logscale;
		this.lognvalue = Math.log(logscale);
	}
	
	
	// Axis overrides
	@Override
	public double valueToJava2D(double value, Rectangle2D plotArea, RectangleEdge edge)
	{
		Range range = getRange();
		double axisMin = log(range.getLowerBound());
		double axisMax = log(range.getUpperBound());
		
		double min=0.0, max=0.0;
		if (RectangleEdge.isTopOrBottom(edge))
		{
			min = plotArea.getMinX();
			max = plotArea.getMaxX();
		}
		else if (RectangleEdge.isLeftOrRight(edge))
		{
			min = plotArea.getMaxY();
			max = plotArea.getMinY();
		}
		
		value = log(value);
		if (isInverted())
			return max - (((value-axisMin) / (axisMax-axisMin)) * (max-min));
		else
			return min + (((value-axisMin) / (axisMax-axisMin)) * (max-min));
	}
	
	@Override
	public double java2DToValue(double java2DValue, Rectangle2D plotArea, RectangleEdge edge)
	{
		Range range = getRange();
		double axisMin = log(range.getLowerBound());
		double axisMax = log(range.getUpperBound());
		
		double min=0.0, max=0.0;
		if (RectangleEdge.isTopOrBottom(edge))
		{
			min = plotArea.getX();
			max = plotArea.getMaxX();
		}
		else if (RectangleEdge.isLeftOrRight(edge))
		{
			min = plotArea.getMaxY();
			max = plotArea.getMinY();
		}
		
		if (isInverted())
			return pow(axisMax - ((java2DValue-min) / (max-min)) * (axisMax-axisMin));
		else
			return pow(axisMin + ((java2DValue-min) / (max-min)) * (axisMax-axisMin));
	}
	
//	@Override
//	public void autoAdjustRange()
//	{
//		Plot plot = getPlot();
//		if (plot == null)
//			return;
//		
//		if (plot instanceof ValueAxisPlot)
//		{
//			ValueAxisPlot vap = (ValueAxisPlot) plot;
//			Range r = vap.getDataRange(this);
//			System.out.println(r);
//		}
//	}
	
	
	// helpers
	protected double log(double value)
	{
		boolean isneg = value < 0;
		if (isneg)
			value = -value;
		if (value < logscale)
			value += (logscale - value) / logscale;
		
		return (isneg ? -Math.log(value) : Math.log(value)) / lognvalue;
	}
	
	protected double pow(double value)
	{
		boolean isneg = value < 0;
		if (isneg)
			value = -value;
		if (value < logscale)
			value += (logscale - value) / logscale;
		
		double result;
		if (value < 1)
			result = (Math.pow(logscale, value+1.0) - logscale) / (logscale-1);
		else
			result = Math.pow(logscale, value);
		return isneg ? -result : result;
	}
	
	
	// data
	protected int logscale;
	protected double lognvalue;
}
