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

import peakml.util.jfreechart.*;





/**
* 
*/
public class DerivativeGraph extends Composite
{
	// constructor(s)
	public DerivativeGraph(Composite parent, int style)
	{
		super(parent, SWT.EMBEDDED|style);
		
		// layout
		setLayout(new FillLayout());
		
		// create the components
		spectrumplot = new FastSpectrumPlot("mass", "intensity");
		spectrumplot.setBackgroundPaint(Color.WHITE);
		linechart = new JFreeChart("", spectrumplot);
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
		current_relationid = -1;
		spectrumplot.clear();
	}
	
	public void setPeak(IPeak peak, Vector<IPeak> peaks, Header header)
	{
		if (peak == null)
			return;
		Annotation ann_relationid = peak.getAnnotation(Annotation.relationid);
		if (ann_relationid == null)
			return;
		int relationid = ann_relationid.getValueAsInteger();
		if (relationid == current_relationid)
			return;
		current_relationid = relationid;
		
		spectrumplot.clear();
		if (peak == null)
			return;
		
		Vector<IPeak> mypeaks = new Vector<IPeak>();
		for (IPeak p : peaks)
		{
			Annotation my_ann_relationid = p.getAnnotation(Annotation.relationid);
			if (my_ann_relationid == null)
				continue;
			
			int my_relationid = my_ann_relationid.getValueAsInteger();
			if (my_relationid == relationid)
				mypeaks.add(p);
		}
		
		for (IPeak p : mypeaks)
		{
			Annotation ann_relation = p.getAnnotation(Annotation.relationship);
			Annotation ann_reaction = p.getAnnotation("reaction");
			String description = ann_reaction!=null ? ann_reaction.getValue() : ann_relation!=null ? ann_relation.getValue() : "";
			
			spectrumplot.addData(
					new FastSpectrumPlot.Data(p.getMass(), p.getIntensity(), description)
				);
		}
	}
	
	
	// data
	protected int current_relationid = -1;
	
	protected JFreeChart linechart;
	protected FastSpectrumPlot spectrumplot;
}
