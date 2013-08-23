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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class InfoView extends View implements Listener
{
	// constructor(s)
	public InfoView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.None);
		
		// set the layout
		GridLayout layout = new GridLayout();
		setLayout(layout);
		
		layout.numColumns = 2;
		
		// add the info panels
		Label filenamelabel = new Label(this, SWT.LEFT);
		filenamelabel.setText("Filename: ");
		filenamevalue = new Label(this, SWT.LEFT);
		filenamevalue.setText("-----------------------------------------------------------------");
		
		Label nrpeakslabel = new Label(this, SWT.LEFT);
		nrpeakslabel.setText("Nr peaks: ");
		nrpeaksvalue = new Label(this, SWT.LEFT);
		nrpeaksvalue.setText("-----------------------------------------------------------------");
		
		pack();
	}
	
	
	// View overrides
	@Override
	public void update(int event)
	{
		MainWnd mainwnd = getMainWnd();
		Document document = mainwnd.getDocument();
		
		if (event == Document.UPDATE_INIT)
		{
			filenamevalue.setText("");
			nrpeaksvalue.setText("");
		}
		else
		{
			if (document.getFilename() != null)
				filenamevalue.setText(document.getFilename());
			nrpeaksvalue.setText(
					document.getFilteredPeaks().size()
					+ " (" + document.getTotalNrPeaks() + ")"
				);
		}
	}

	
	// Listener overrides
	public void handleEvent(Event e)
	{
	}

	
	// data
	protected Label filenamevalue;
	protected Label nrpeaksvalue;
}
