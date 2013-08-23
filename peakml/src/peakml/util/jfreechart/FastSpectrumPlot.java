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

import java.awt.*;
import java.awt.geom.*;

// jfreechart
import org.jfree.ui.*;

import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;






/**
 * 
 */
public class FastSpectrumPlot extends Plot implements Zoomable, Pannable
{
	public static class Data
	{
		// constructor(s)
		public Data(double mass, double intensity, String description)
		{
			this.mass = mass;
			this.intensity = intensity;
			this.description = description;
		}
		
		
		// data
		protected double mass;
		protected double intensity;
		protected String description;
	}
	
	
	// constructor(s)
	public FastSpectrumPlot(String xlabel, String ylabel)
	{
		// create the x-axis
		xaxis = new NumberAxis(xlabel);
		xaxis.setPlot(this);
		xaxis.setAutoRange(false);
		xaxis.addChangeListener(this);
		
		// create the y-axis
		yaxis = new NumberAxis(ylabel);
		yaxis.setPlot(this);
		yaxis.setAutoRange(false);
		yaxis.addChangeListener(this);
	}
	
	
	// access
	public void addData(Data data)
	{
		dataseries.add(data);
		
		xmin = Math.min(xmin, data.mass-50);
		xmax = Math.max(xmax, data.mass+50);
		ymax = Math.max(ymax, data.intensity+(0.1*data.intensity));
		
		xaxis.setRange(xmin, xmax);
		yaxis.setRange(0, ymax);
		
		this.fireChangeEvent();
	}
	
	public void clear()
	{
		xmin = Double.MAX_VALUE;
		xmax = Double.MIN_VALUE;
		ymax = Double.MIN_VALUE;
		
		dataseries.clear();
		this.fireChangeEvent();
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
		for (Data data : dataseries)
		{
			int xpos = (int) xaxis.valueToJava2D(data.mass, dataArea, RectangleEdge.BOTTOM);
			int ypos = (int) yaxis.valueToJava2D(data.intensity, dataArea, RectangleEdge.LEFT);
			g2.drawLine(
					xpos, (int) yaxis.valueToJava2D(0, dataArea, RectangleEdge.LEFT), xpos, ypos
				);
			
			// draw the label
			if (data.description!=null && data.description.length()!=0)
			{
				g2.setColor(Color.RED);
				g2.drawLine(xpos+2, ypos-2, xpos+15, ypos-15);
				g2.setColor(Color.BLACK);
				g2.drawString(data.description, xpos+17, ypos-17);
			}
		}
		
		// reset
		g2.setClip(originalclip);
	}

	@Override
	public String getPlotType()
	{
		return "FastSpectrumPlot";
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
			yaxis.setRange(0, ymax);
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
	double ymax = Double.MIN_VALUE;
	
	protected NumberAxis xaxis;
	protected NumberAxis yaxis;
	
	protected PlotOrientation orientation = PlotOrientation.VERTICAL;
	
	protected Vector<Data> dataseries = new Vector<Data>();
	
	private static final long serialVersionUID = 3786762435041479050L;
}
