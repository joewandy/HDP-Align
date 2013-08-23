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



package peakmlviewer.dialog;


// java
import java.io.File;

import javax.imageio.ImageIO;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.action.*;

import peakmlviewer.dialog.timeseries.*;





/**
 * 
 */
public class SortTimeSeriesDialog extends ActionDialog implements Listener
{
	// constructor(s)
	public SortTimeSeriesDialog(MainWnd mainwnd, Shell parent)
	{
		super(mainwnd, parent, "Sort time-series");
	}
	
	@Override
	public void init()
	{
		super.init();
		
		// initialize the action to null
		action = null;
		
		// create the layout
		shell.setLayout(new GridLayout(3, false));
		
		// create the chart
		GridData layout_chart = new GridData(GridData.FILL_BOTH);
		layout_chart.horizontalSpan = 3;
		chart = new EditableChart(shell, SWT.None);
		chart.setLayoutData(layout_chart);
		
		// create the buttons
		btn_ok = new Button(shell, SWT.Selection);
		btn_ok.setText("Ok");
		btn_ok.addListener(SWT.Selection, this);
		
		btn_cancel = new Button(shell, SWT.Selection);
		btn_cancel.setText("Cancel");
		btn_cancel.addListener(SWT.Selection, this);
		
		btn_exportgraph = new Button(shell, SWT.Selection);
		btn_exportgraph.setText("Export graph");
		btn_exportgraph.addListener(SWT.Selection, this);
		
		// set up the chart
		Document document = mainwnd.getDocument();
		for (int i=0; i<document.getNrTimePoints(); ++i)
			chart.addValue(""+i, 0.5);
	}
	
	
	// Listeners overrides
	public void handleEvent(Event event)
	{
		if (event.widget == btn_ok)
		{
			Document document = mainwnd.getDocument();
			double timepoints[] = new double[document.getNrTimePoints()];
			for (int i=0; i<document.getNrTimePoints(); ++i)
				timepoints[i] = chart.getValue(""+i);
			
			action = new SortTimeSeries(timepoints, document.getHeader());
			
			shell.dispose();
		}
		else if (event.widget == btn_cancel)
		{
			shell.dispose();
		}
		else if (event.widget == btn_exportgraph)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			String filename = dlg.open();
			if (filename == null)
				return;
			
			try
			{
				ImageIO.write(chart.getGraphImage(800, 500), "png", new File(filename));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	
	// ActionDialog overrides
	@Override
	protected Action getAction()
	{
		return action;
	}

	@Override
	public String getName()
	{
		return "Sort time-series";
	}
	
	
	// data
	/** */
	protected SortTimeSeries action;
	
	/** */
	protected Button btn_exportgraph;
	/** */
	protected Button btn_ok;
	/** */
	protected Button btn_cancel;
	/** */
	protected EditableChart chart;
}
