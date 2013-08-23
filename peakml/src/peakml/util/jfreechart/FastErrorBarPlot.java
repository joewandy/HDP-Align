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

import org.jfree.data.general.*;

import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

// peakml
import peakml.math.*;






/**
 * 
 */
@SuppressWarnings("serial")
public class FastErrorBarPlot extends CategoryPlot
{
	public static class Data
	{
		// constructor(s)
		public Data(String xvalue, double yvalues[])
		{
			this.xvalue = xvalue;
			this.yvalues = yvalues;
			
			this.ystats = Statistical.stats(yvalues);
		}
		
		// access
		public double getMinY()
		{
			return ystats[Statistical.MINIMUM];
		}
		
		public double getMaxY()
		{
			return ystats[Statistical.MAXIMUM];
		}
		
		public double getMeanY()
		{
			return ystats[Statistical.MEAN];
		}
		
		// data
		protected String xvalue;
		protected double ystats[];
		protected double yvalues[];
	}
	
	
	// constructor
	public FastErrorBarPlot(String xlabel, String ylabel)
	{
		// create the x-axis
		xaxis = new CategoryAxis(xlabel);
		xaxis.setPlot(this);
		xaxis.addChangeListener(this);
		
		// create the y-axis
		yaxis = new NumberAxis(ylabel);
		yaxis.setPlot(this);
		yaxis.setAutoRange(false);
		yaxis.addChangeListener(this);
	}
	
	
	// access
	public void clear()
	{
		ymin = Double.MAX_VALUE;
		ymax = Double.MIN_VALUE;
		dataseries.clear();
	}
	
	public void addData(Data data)
	{
		dataseries.add(data);
		
		// xaxis range
		
		// yaxis range
		ymin = Math.min(ymin, data.getMinY());
		ymax = Math.max(ymax, data.getMaxY());
		yaxis.setRange(log ? ymin : 0, ymax);
	}
	
	public void setShowAll(boolean v)
	{
		if (showall != v)
		{
			showall = v;
			datasetChanged(new DatasetChangeEvent(this, null));
		}
	}
	
	public boolean getShowAll()
	{
		return showall;
	}
	
	public void setLogAxis(boolean v)
	{
		if (log == v)
			return;
		
		log = v;
		yaxis = log ? new LogarithmicAxis(yaxis.getLabel()) : new NumberAxis(yaxis.getLabel());
		yaxis.setPlot(this);
		yaxis.setAutoRange(false);
		yaxis.addChangeListener(this);
		yaxis.setRange(log ? ymin : 0, ymax);
	}
	
	public boolean getLogAxis()
	{
		return log;
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
		
		// create the strokes
		BasicStroke stroke_solid = new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.f);
		BasicStroke stroke_dashed = new BasicStroke(1.f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1.f, new float[] { 2,4 }, 0);
		
		g2.setStroke(stroke_solid);
		
		// count the number of labels
		int categoryCount = 0;
		if (showall)
		{
			for (Data data : dataseries)
				categoryCount += data.yvalues.length;
		}
		else
			categoryCount = dataseries.size();
		
		// draw all the values
		int pos = 0;
		boolean dashed = false;
		double prevx=-1, prevy=-1;
		for (Data data : dataseries)
		{
			if (data.yvalues.length == 0)
			{
				dashed = true;
				pos++;
				continue;
			}
			
			double mean[] = showall ? data.yvalues : new double[]{data.getMeanY()};
			double min[] = showall ? data.yvalues : new double[]{data.getMinY()};
			double max[] = showall ? data.yvalues : new double[]{data.getMaxY()};
			for (int i=0; i<mean.length; ++i)
			{
				double ypos, xpos = xaxis.getCategoryJava2DCoordinate(
						CategoryAnchor.MIDDLE, pos++, categoryCount, dataArea, RectangleEdge.BOTTOM
					);
				
				// draw the mean value
				g2.setColor(Color.RED);
				ypos = yaxis.valueToJava2D(mean[i], dataArea, RectangleEdge.LEFT);
				g2.drawLine((int) xpos-2, (int) ypos, (int) xpos+2, (int) ypos);
				
				// conect the dots
				if (prevx!=-1 && prevy!=-1)
				{
					g2.setColor(Color.BLACK);
					if (dashed)
						g2.setStroke(stroke_dashed);
					g2.drawLine((int) prevx, (int) prevy, (int) xpos, (int) ypos);
					if (dashed)
					{
						dashed = false;
						g2.setStroke(stroke_solid);
					}
	
				}
				prevy = ypos;
				prevx = xpos;
				
				// draw the outer values
				g2.setColor(Color.LIGHT_GRAY);
				double ypos_min = yaxis.valueToJava2D(min[i], dataArea, RectangleEdge.LEFT);
				g2.drawLine((int) xpos-2, (int) ypos_min, (int) xpos+2, (int) ypos_min);
				double ypos_max = yaxis.valueToJava2D(max[i], dataArea, RectangleEdge.LEFT);
				g2.drawLine((int) xpos-2, (int) ypos_max, (int) xpos+2, (int) ypos_max);
				g2.drawLine((int) xpos, (int) ypos_min, (int) xpos, (int) ypos_max);
			}
		}
		
		// reset
		g2.setClip(originalclip);
	}

	@Override
	public String getPlotType()
	{
		return "FastErrorBarPlot";
	}
	
	
	// CategoryPlot overrides
	@Override
	public java.util.List<String> getCategoriesForAxis(CategoryAxis axis)
	{
		Vector<String> list = new Vector<String>();
		if (showall)
		{
			for (Data data : dataseries)
				for (int i=0; i<data.yvalues.length; ++i)
					list.add(data.xvalue);
		}
		else
		{
			for (Data data : dataseries)
				list.add(data.xvalue);
		}
		return list;
	}
	
	
	// data
	protected Vector<Data> dataseries = new Vector<Data>();
	
	protected boolean log = false;
	protected boolean showall = false;
	
	protected double ymin = Double.MAX_VALUE;
	protected double ymax = Double.MIN_VALUE;
	
	protected NumberAxis yaxis;
	protected CategoryAxis xaxis;
}
