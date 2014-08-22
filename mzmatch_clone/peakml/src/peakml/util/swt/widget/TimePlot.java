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
import java.awt.Color;

// eclipse
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
public class TimePlot extends Composite
{
	// constructor(s)
	public TimePlot(Composite parent, int style)
	{
		super(parent, SWT.EMBEDDED|style);
		
		// layout
		setLayout(new FillLayout());
		
		// create the components
		timeplot = new peakml.util.jfreechart.FastTimePlot("", "");
		timeplot.setBackgroundPaint(Color.WHITE);
		linechart = new JFreeChart("", timeplot);
		linechart.setBackgroundPaint(Color.WHITE);
		
		// add the components
		// --------------------------------------------------------------------------------
		// This uses the SWT-trick for embedding AWT-controls in an SWT-Composite.
		// 2009-10-17: Tested the SWT setup in jfreechart.experimental - this is still better.
		try { 
			System.setProperty("sun.awt.noerasebackground", "true"); 
		} catch (NoSuchMethodError error) { ; } 
			
		java.awt.Frame frame = org.eclipse.swt.awt.SWT_AWT.new_Frame(this);
		ChartPanel panel = new ChartPanel(linechart, false, false, false, false, false);
		
		panel.setFillZoomRectangle(true);
		panel.setMouseZoomable(true, true);
		
		// create a new ChartPanel, without the popup-menu (5x false)
		frame.add(panel);
		// --------------------------------------------------------------------------------
	}
	
	
	// access
	public void clear()
	{
		timeplot.clear();
	}
	
	public void addData(FastTimePlot.Data data)
	{
		timeplot.addData(data);
	}
	
	public FastTimePlot getFastTimePlot()
	{
		return timeplot;
	}
	
	public java.awt.image.BufferedImage getGraphImage(int width, int height)
	{
		java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_ARGB);
		java.awt.Graphics2D g = img.createGraphics();
		
		g.setRenderingHint(
				java.awt.RenderingHints.KEY_ANTIALIASING,
				java.awt.RenderingHints.VALUE_ANTIALIAS_ON
			);
		
		linechart.draw(g, new java.awt.Rectangle(0, 0, width, height));
		
		return img;
	}
	
	public JFreeChart getChart()
	{
		return linechart;
	}
	
	
	// data
	protected JFreeChart linechart;
	protected FastTimePlot timeplot;
}
