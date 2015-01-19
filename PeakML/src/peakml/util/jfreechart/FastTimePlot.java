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
import java.util.*;
import java.text.*;

import java.awt.*;
import java.awt.geom.*;

// jfreechart
import org.jfree.ui.*;

import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

// PeakML
import peakml.math.*;
import peakml.graphics.*;





/**
 * 
 */
public class FastTimePlot extends Plot implements Zoomable, Pannable
{
	public static class Data
	{
		public Data(String label, double time[], double values[]) throws RuntimeException
		{
			this(label, time, values, -1);
		}
		
		public Data(String label, double time[], double values[], int color) throws RuntimeException
		{
			if (time.length != values.length)
				throw new RuntimeException("FastTimePlot.Data: the time and values vectors need to be of the same length");
				
			this.size = time.length;
			this.label = label;
			this.time = time;
			this.values = values;
			this.color = color;
		}
		
		
		// data
		protected int size;
		protected String label;
		protected double time[];
		protected double values[];
		protected int color;
	}
	
	
	// constructor(s)
	public FastTimePlot(String xlabel, String ylabel)
	{
		// create the x-axis
		xaxis = new NumberAxis(xlabel);
		xaxis.setPlot(this);
		xaxis.setAutoRange(false);
		xaxis.addChangeListener(this);
		
		xaxis.setNumberFormatOverride(new java.text.NumberFormat() {
			@Override
			public StringBuffer format(double number, StringBuffer toAppendTo, FieldPosition pos)
			{
				if (number > 3600)
				{
					int hrs = (int) (number/3600);
					return toAppendTo.append(String.format("%02d:%02d:%02d",
							hrs, (int) ((number/60) - hrs*60), (int) (number%60)
						));
				}
				return toAppendTo.append(String.format("%02d:%02d", (int) (number/60), (int) (number%60)));
			}

			@Override
			public StringBuffer format(long number, StringBuffer toAppendTo, FieldPosition pos) { return null; }
			@Override
			public Number parse(String source, ParsePosition parsePosition) { return null; }
			private static final long serialVersionUID = -6728259541513959179L;
		});
		
		// create the y-axis
		yaxis = new NumberAxis(ylabel);
		yaxis.setPlot(this);
		yaxis.setAutoRange(false);
		yaxis.addChangeListener(this);
	}
	
	
	// data access
	public void addData(Data data)
	{
		dataseries.put(data.label, data);
		
		// calculate the min and max
		xmin = Math.min(xmin, Statistical.min(data.time));
		xmax = Math.max(xmax, Statistical.max(data.time));
		ymin = Math.min(ymin, Statistical.min(data.values));
		ymax = Math.max(ymax, Statistical.max(data.values));
		
		double xspace = (xmax!=xmin ? 0.01 * (xmax-xmin) : 0.01*xmin);
		double yspace = (ymax!=ymin ? 0.01 * (ymax-ymin) : 0.01*ymin);
		
		xaxis.setRange(xmin-xspace, xmax+xspace);
		yaxis.setRange(ymin-yspace, ymax+yspace);
		
		// trigger the redraw
		this.fireChangeEvent();
	}
	
	public void clear()
	{
		xmin = Double.MAX_VALUE;
		xmax = Double.MIN_VALUE;
		ymin = Double.MAX_VALUE;
		ymax = Double.MIN_VALUE;
		dataseries.clear();
		
		this.fireChangeEvent();
	}
	
	
	// general access
	/**
	 * 
	 */
	public void setOrientation(PlotOrientation orientation)
	{
		this.orientation = orientation;
		this.fireChangeEvent();
	}
	
	/**
	 * 
	 */
	public void setAxisLabels(String xlabel, String ylabel)
	{
		xaxis.setLabel(xlabel);
		yaxis.setLabel(ylabel);
	}
	
	
	// Plot overrides
	@Override
	public void draw(Graphics2D g2, Rectangle2D area, Point2D anchor, PlotState parentState, PlotRenderingInfo info)
	{
		// add the plot area to the info (used amongst other by the axis for zooming)
		if (info != null)
			info.setPlotArea(area);
		
		// add the insets (if any)
		RectangleInsets insets = getInsets();
		insets.trim(area);
		
		// draw the axis and add the dataArea to the info (used amongst other by the axis for zooming)
		AxisSpace space = new AxisSpace();
		space = xaxis.reserveSpace(g2, this, area, RectangleEdge.BOTTOM, space);
		space = yaxis.reserveSpace(g2, this, area, RectangleEdge.LEFT,   space);
		
		Rectangle2D dataArea = space.shrink(area, null);
		if (info != null)
		    info.setDataArea(dataArea);
		
		// flood fill the whole area with the background color
		drawBackground(g2, dataArea);
		
		// draw the axis
		xaxis.draw(g2, dataArea.getMaxY(), area, dataArea, RectangleEdge.BOTTOM, info);
		yaxis.draw(g2, dataArea.getMinX(), area, dataArea, RectangleEdge.LEFT,   info);
		
		// sanity check
		if (dataseries.size() == 0)
			return;
		
		// clip the draw area
		Shape originalclip = g2.getClip();
		g2.clip(dataArea);
		
		// draw all the values
		int index = 0;
		for (Data data : dataseries.values())
		{
			g2.setColor(new Color(data.color==-1 ? colormap.getColor(index++) : data.color));
			for (int i=0; i<data.size-1; ++i)
			{
				g2.drawLine(
						(int) xaxis.valueToJava2D(data.time[i], dataArea, RectangleEdge.BOTTOM),
						(int) yaxis.valueToJava2D(data.values[i], dataArea, RectangleEdge.LEFT),
						(int) xaxis.valueToJava2D(data.time[i+1], dataArea, RectangleEdge.BOTTOM),
						(int) yaxis.valueToJava2D(data.values[i+1], dataArea, RectangleEdge.LEFT)
					);
			}
		}
		
		// reset
		g2.setClip(originalclip);
	}

	@Override
	public String getPlotType()
	{
		return "FastTimePlot";
	}
	
	
	// Zoomable overrides
	public PlotOrientation getOrientation()
	{
		return orientation;
	}

	public boolean isDomainZoomable()
	{
		return true;
	}

	public boolean isRangeZoomable()
	{
		return true;
	}

	public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source)
	{
		if (factor == 0)
			xaxis.setRange(xmin, xmax);
		else
			xaxis.resizeRange(factor);
	}

	public void zoomDomainAxes(double factor, PlotRenderingInfo state, Point2D source, boolean useAnchor)
	{
		if (useAnchor)
		{
			// get the source coordinate - this plot has always a VERTICAL orientation
			double sourceX = source.getX();
			double anchorX = xaxis.java2DToValue(sourceX, state.getDataArea(), RectangleEdge.BOTTOM);
			
			xaxis.resizeRange2(factor, anchorX);
		}
		else
		{
			xaxis.resizeRange(factor);
		}
	}

	public void zoomDomainAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source)
	{
		xaxis.zoomRange(lowerPercent, upperPercent);
	}

	public void zoomRangeAxes(double factor, PlotRenderingInfo state, Point2D source)
	{
		if (factor == 0)
			yaxis.setRange(ymin, ymax);
		else
			yaxis.resizeRange(factor);
	}

	public void zoomRangeAxes(double factor, PlotRenderingInfo state, Point2D source, boolean useAnchor)
	{
		if (useAnchor)
		{
			// get the source coordinate - this plot has always a VERTICAL orientation
			double sourceY = source.getY();
			double anchorY = yaxis.java2DToValue(sourceY, state.getDataArea(), RectangleEdge.LEFT);
			
			yaxis.resizeRange2(factor, anchorY);
		}
		else
		{
			yaxis.resizeRange(factor);
		}
	}

	public void zoomRangeAxes(double lowerPercent, double upperPercent, PlotRenderingInfo state, Point2D source)
	{
		yaxis.zoomRange(lowerPercent, upperPercent);
	}
	
	
	// Pannable overrides
	public boolean isDomainPannable()
	{
		return true;
	}

	public boolean isRangePannable()
	{
		return true;
	}

	public void panDomainAxes(double percent, PlotRenderingInfo info, Point2D source)
	{
        double length = xaxis.getRange().getLength();
        double adj = (xaxis.isInverted() ? percent : -percent) * length;
        
        xaxis.setRange(xaxis.getLowerBound()+adj, xaxis.getUpperBound()+adj);
	}

	public void panRangeAxes(double percent, PlotRenderingInfo info, Point2D source)
	{
        double length = yaxis.getRange().getLength();
        double adj = (yaxis.isInverted() ? -percent : percent) * length;
        
        yaxis.setRange(yaxis.getLowerBound()+adj, yaxis.getUpperBound()+adj);
	}

	
	// data
	double xmin = Double.MAX_VALUE;
	double xmax = Double.MIN_VALUE;
	double ymin = Double.MAX_VALUE;
	double ymax = Double.MIN_VALUE;
	
	protected NumberAxis xaxis;
	protected NumberAxis yaxis;
	
	protected PlotOrientation orientation = PlotOrientation.VERTICAL;
	protected HashMap<String,Data> dataseries = new HashMap<String,Data>();
	
	protected static final Colormap colormap = new Colormap(Colormap.EXCEL);
	private static final long serialVersionUID = 3767894050118259565L;
}
