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
import java.util.*;

// eclipse
import java.awt.Color;

import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.graphics.*;

import peakml.util.jfreechart.*;





/**
* 
*/
public class IPeakGraph extends Composite
{
	// constructor(s)
	public IPeakGraph(Composite parent, int style)
	{
		super(parent, SWT.EMBEDDED|style);
		
		// layout
		setLayout(new FillLayout());
		
		// create the components
		timeplot = new FastTimePlot("retention time", "intensity");
		timeplot.setBackgroundPaint(Color.WHITE);
		linechart = new JFreeChart("", timeplot);
		linechart.setBackgroundPaint(Color.WHITE);
		
//		org.jfree.experimental.chart.swt.ChartComposite c =
//			new org.jfree.experimental.chart.swt.ChartComposite(this, SWT.NONE, linechart);
//		
//		c.setRangeZoomable(true);
//		c.setDomainZoomable(true);
		
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
	
	public void setPeak(IPeak peak, Header header, HashMap<String,Integer> colors)
	{
		if (peak == null)
		{
			timeplot.clear();
		}
		else
		{
			timeplot.clear();
			updateGraph(peak, header, colors);
		}
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
	
	
	// helper
	@SuppressWarnings("unchecked")
	protected void updateGraph(IPeak peak, Header header, HashMap<String,Integer> colors)
	{
		if (peak == null)
			return;
		
		// fill the graph with the appropriate data
		Class<? extends IPeak> c = peak.getClass();
		if (c.equals(MassChromatogram.class) || c.equals(BackgroundIon.class))
		{
			PeakData<Centroid> peakdata;
			if (c.equals(MassChromatogram.class))
				peakdata = ((MassChromatogram<Centroid>) peak).getPeakData();
			else
				peakdata = ((BackgroundIon<Centroid>) peak).getPeakData();
			
			MeasurementInfo measurementinfo = header.getMeasurementInfo(peak.getMeasurementID());
			if (measurementinfo!=null && measurementinfo.getVisible()==false)
				return;
			SetInfo setinfo = null;
			if (measurementinfo != null)
				setinfo = header.getSetInfoForMeasurementID(measurementinfo.getID());
			
			int color = 0;
			if (colors!=null && setinfo!=null)
				color = colors.get(setinfo.getID());
			else if (setinfo!=null)
				color = colormap.getColor(header.indexOfSetInfo(setinfo.getID()));
			
			FastTimePlot.Data data = new FastTimePlot.Data(
					measurementinfo!=null ? measurementinfo.getLabel() : "",
					peakdata.getRetentionTimes(), peakdata.getIntensities(),
					color
				);
			timeplot.addData(data);
		}
		else if (c.equals(IPeakSet.class))
		{
			IPeakSet<? extends IPeak> peakset = (IPeakSet<? extends IPeak>) peak;
			for (IPeak p : peakset)
				updateGraph(p, header, colors);
		}
	}
	
	
	// data
	protected JFreeChart linechart;
	protected FastTimePlot timeplot;
	
	protected boolean smooth;
	protected boolean smooth_show_original;
	
	protected static final Colormap colormap = new Colormap(Colormap.EXCEL);
}
