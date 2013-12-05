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
import java.util.*;
import java.awt.image.*;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;

// libraries

// peakml
import peakml.*;
import peakml.io.*;

import peakml.util.swt.widget.*;

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class IntensityView extends View implements Listener
{
	// constructor(s)
	public IntensityView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.NONE);
		
		// layout
		setLayout(new FormLayout());
		
		// create the toolbar
		ToolBar toolbar = new ToolBar(this, SWT.FLAT);
		
		toolall = new ToolItem(toolbar, SWT.PUSH);
		toolall.setText("All");
		toolall.addListener(SWT.Selection, this);
		toollog = new ToolItem(toolbar, SWT.PUSH);
		toollog.setText("Log");
		toollog.addListener(SWT.Selection, this);
		
		// create the intensity-trend
		graph = new IntensityTrendGraph(this, SWT.BORDER);
		
		// set the layout data
		FormData toolbardata = new FormData();
		toolbardata.left	= new FormAttachment(0);
		toolbardata.top		= new FormAttachment(0);
		toolbardata.right	= new FormAttachment(100);
		toolbardata.bottom	= new FormAttachment(graph);
		
		FormData graphdata = new FormData();
		graphdata.left		= new FormAttachment(0);
		graphdata.top		= new FormAttachment(toolbar);
		graphdata.right		= new FormAttachment(100);
		graphdata.bottom	= new FormAttachment(100);
		graph.setLayoutData(graphdata);
	}
	
	
	// access
	public JFreeChart getGraph()
	{
		return graph.getGraph();
	}
	
	public BufferedImage getGraphImage(int width, int height)
	{
		return graph.getGraphImage(width, height);
	}
	
	
	// View overrides
	public void update(int event)
	{
		graph.clear();
		if (event != Document.UPDATE_INIT)
		{
			Document document = getMainWnd().getDocument();
			IPeak peak = document.getCurrentPeak();
			if (peak == null)
				return;
			Vector<IPeak> peaks = IPeak.unpack(peak);
			
			Header header = document.getHeader();
			for (SetInfo setinfo : header.getSetInfos())
			{
				// retrieve all the peaks for this set
				Vector<IPeak> set = IPeak.peaksOfMeasurements(peaks, setinfo.getAllMeasurementIDs());
				
				// grab the intensities and add to the data
				double intensities[] = new double[set.size()];
				for (int i=0; i<set.size(); ++i)
					intensities[i] = set.get(i).getIntensity();
				graph.addData(setinfo.getID(), intensities);
			}
		}
	}


	// Listener overrides
	public void handleEvent(Event e)
	{
		if (e.widget == toolall)
		{
			graph.setShowAll(!graph.getShowAll());
		}
		else if (e.widget == toollog)
		{
			graph.setLogAxis(!graph.getLogAxis());
		}
	}
	
	
	// data
	protected ToolItem toolall;
	protected ToolItem toollog;
	
	protected IntensityTrendGraph graph;
}
