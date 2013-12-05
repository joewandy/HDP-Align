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



package peakmlviewer.view;


// java

// eclipse
import org.eclipse.swt.*;

import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;

// libraries

// peakml
import peakml.util.swt.widget.*;

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class IPeakView extends View implements Listener
{
	// constructor(s)
	public IPeakView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.BORDER);
		
		
		// layout
		setLayout(new FillLayout());
		
		// create the components
		tabview = new CTabFolder(this, SWT.TOP|SWT.FLAT);
		
		graphview = new IPeakGraph(tabview, SWT.NONE);
		tab_graphview = new CTabItem(tabview, SWT.NONE);
		tab_graphview.setText("peak");
		tab_graphview.setControl(graphview);		
		
		derivativeview = new DerivativeGraph(tabview, SWT.NONE);
		tab_derivativeview = new CTabItem(tabview, SWT.NONE);
		tab_derivativeview.setText("derivatives");
		tab_derivativeview.setControl(derivativeview);
		
		intensityview = new IntensityView(mainwnd, tabview);
		tab_intensityview = new CTabItem(tabview, SWT.NONE);
		tab_intensityview.setText("intensity pattern");
		tab_intensityview.setControl(intensityview);
		
		tabview.setSelection(tab_graphview);
	}
	
	
	// access
	public JFreeChart getGraph()
	{
		CTabItem selected = tabview.getSelection();
		if (selected == tab_graphview)
			return graphview.getChart();
		else if (selected == tab_derivativeview)
			return null;
		else
			return null;
	}
	
	public java.awt.image.BufferedImage getGraphImage(int width, int height)
	{
		return graphview.getGraphImage(width, height);
	}
	
	
	// View overrides
	public void update(int event)
	{
		MainWnd mainwnd = getMainWnd();
		Document document = mainwnd.getDocument();
		
		intensityview.update(event);
		if (event == Document.UPDATE_INIT)
		{
			graphview.clear();
			derivativeview.clear();
		}
		else
		{
			graphview.setPeak(document.getCurrentPeak(), document.getHeader(), document.getSetColors());
			derivativeview.setPeak(document.getCurrentPeak(), document.getFilteredPeaks(), document.getHeader());
		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event event)
	{
		if (event.type == SWT.FocusIn)
		{
			mainwnd.getFocus();
		}
	}
	
	
	// data
	protected CTabFolder tabview;
	
	protected CTabItem tab_graphview;
	protected IPeakGraph graphview;
	protected CTabItem tab_derivativeview;
	protected DerivativeGraph derivativeview;
	protected CTabItem tab_intensityview;
	protected IntensityView intensityview;
}
