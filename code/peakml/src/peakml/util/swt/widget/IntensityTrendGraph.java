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



package peakml.util.swt.widget;


// java

// eclipse
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;

// peakml
import peakml.util.jfreechart.*;





/**
* 
*/
public class IntensityTrendGraph extends Composite
{
	// constructor(s)
	public IntensityTrendGraph(Composite parent, int style)
	{
		super(parent, SWT.EMBEDDED|style);
		
		// layout
		setLayout(new FillLayout());
		
		// create the components
		errorbarplot = new FastErrorBarPlot("", "Intensity");
		errorbarplot.setBackgroundPaint(Color.WHITE);
		linechart = new JFreeChart("", errorbarplot);
		linechart.setBackgroundPaint(Color.WHITE);
		
		// add the components
		// --------------------------------------------------------------------------------
		// This uses the SWT-trick for embedding awt-controls in an SWT-Composite.
		try { 
			System.setProperty("sun.awt.noerasebackground", "true"); 
		} catch (NoSuchMethodError error) { ; } 
		java.awt.Frame frame = org.eclipse.swt.awt.SWT_AWT.new_Frame(this);
		
		// create a new ChartPanel, without the popup-menu (5x false)
		frame.add(new ChartPanel(linechart, false, false, false, false, false));
		// --------------------------------------------------------------------------------
	}
	
	
	// access
	public void clear()
	{
		errorbarplot.clear();
	}
	
	public void addData(String id, double intensities[])
	{
		errorbarplot.addData(new FastErrorBarPlot.Data(id, intensities));
	}
	
	public void setShowAll(boolean v)
	{
		errorbarplot.setShowAll(v);
	}
	
	public boolean getShowAll()
	{
		return errorbarplot.getShowAll();
	}
	
	public void setLogAxis(boolean v)
	{
		errorbarplot.setLogAxis(v);
	}
	
	public boolean getLogAxis()
	{
		return errorbarplot.getLogAxis();
	}
	
	public JFreeChart getGraph()
	{
		return linechart;
	}
	
	public BufferedImage getGraphImage(int width, int height)
	{
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		linechart.draw(g, new Rectangle(0, 0, width, height));
		
		return img;
	}
	
	
	// data
	protected JFreeChart linechart;
	protected FastErrorBarPlot errorbarplot;
}
