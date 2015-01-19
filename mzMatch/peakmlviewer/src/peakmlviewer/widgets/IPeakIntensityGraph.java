/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakmlviewer.widgets;


// java
import java.util.*;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;

import org.jfree.data.category.*;

// libraries

// metabolome
import peakml.*;
import peakml.io.*;

// peakmlviewer






/**
* 
*/
public class IPeakIntensityGraph extends Composite
{
	// constructor(s)
	@SuppressWarnings("deprecation")
	public IPeakIntensityGraph(Composite parent, int style)
	{
		super(parent, style|SWT.EMBEDDED);
		
		// create the components
		linechart = ChartFactory.createLineChart(
				null, "", "Intensity", dataset, PlotOrientation.VERTICAL,
				false, // legend
				false, // tooltips
				false  // urls
			);
		
		CategoryPlot plot = (CategoryPlot) linechart.getPlot();
		CategoryAxis axis = (CategoryAxis) plot.getDomainAxis();
		axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		
		renderer.setShapesFilled(true);
		renderer.setShapesVisible(true);
		
		linechart.setBackgroundPaint(java.awt.Color.WHITE);
		linechart.setBorderVisible(false);
		linechart.setAntiAlias(true);
		
		plot.setBackgroundPaint(java.awt.Color.WHITE);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		
		// add the components
		// --------------------------------------------------------------------------------
		// This uses the SWT-trick for embedding AWT-controls in an SWT-Composite.
		try { 
			System.setProperty("sun.awt.noerasebackground", "true"); 
		} catch (NoSuchMethodError error) { ; } 
			
		// create a new ChartPanel, without the popup-menu (5x false)
		java.awt.Frame frame = org.eclipse.swt.awt.SWT_AWT.new_Frame(this);
		frame.add(new ChartPanel(linechart, false, false, false, false, false));
		// --------------------------------------------------------------------------------
	}
	
	
	// access
	public void clear()
	{
		dataset.clear();
	}
	
	@SuppressWarnings("unchecked")
	public void setPeak(Header header, IPeak peak, String id)
	{
		if (IPeakSet.class.equals(peak.getClass()))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			
			java.util.Collections.sort(peakset.getPeaks(), IPeak.sort_profileid_ascending);
			for (IPeak p : peakset)
				dataset.addValue(
						p.getIntensity(), id,
						header.getSetInfoForMeasurementID(p.getMeasurementID()).getID()
					);
		}
		else
		{
			
		}
	}
	
	public void setPeaks(Header headers[], IPeak peaks[])
	{
		if (headers.length != peaks.length)
			throw new RuntimeException("setPeaks requires both the headers and the peaks vectors to be of the same size.");
		
		int size = headers.length;
		for (int i=0; i<size; ++i)
			setPeak(headers[i], peaks[i], Integer.toString(i));
	}
	
	public void setPeaks(Vector<Header> headers, Vector<IPeak> peaks)
	{
		setPeaks((Header[]) headers.toArray(), (IPeak[]) peaks.toArray());
	}
	
	
	// data
	protected JFreeChart linechart = null;
	protected DefaultCategoryDataset dataset = new DefaultCategoryDataset();
}
