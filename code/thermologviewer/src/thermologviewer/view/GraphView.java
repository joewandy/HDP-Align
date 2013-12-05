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



package thermologviewer.view;


// java

// swt
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml
import peakml.util.swt.widget.*;
import peakml.util.jfreechart.*;

// ThermoLogViewer
import thermologviewer.*;
import thermologviewer.data.*;





/**
 * 
 */
public class GraphView extends View implements Listener
{
	// constructor(s)
	public GraphView(MainWnd mainwnd)
	{
		super(mainwnd, mainwnd.getShell(), SWT.BORDER);
		
		// layout
		setLayout(new FillLayout());
		
		// add the widgets
		timeplot = new TimePlot(this, SWT.None);
	}
	
	
	// View overrides
	public void update(int event)
	{
		Document document = mainwnd.getDocument();
//		if (event == Document.UPDATE_ADD_FILE)
//		{
			timeplot.clear();
			String label = "";
			if (document.getActiveLog() == Document.LOG_STATUS)
				label = document.getStatusLogLabel();
//			else
//				label = document.getTrailerExtraLabel();
			if (label.equals(""))
				return;
		
			FastTimePlot plot = timeplot.getFastTimePlot();
			plot.setAxisLabels("retention time", label);
			for (RawFile rawfile : document.getFiles())
			{
				timeplot.addData(new FastTimePlot.Data(
						rawfile.getFilename(),
						rawfile.getRetentionTimes(),
						rawfile.getStatusLog().get(label),
						rawfile.getColor())
					);
			}
//		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event e)
	{
	}
	
	
	// data
	protected TimePlot timeplot;
}
