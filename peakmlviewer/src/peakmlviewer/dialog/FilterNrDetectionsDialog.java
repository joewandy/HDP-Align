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

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.action.*;





/**
 * 
 */
public class FilterNrDetectionsDialog extends ActionDialog implements Listener
{
	// constructor(s)
	public FilterNrDetectionsDialog(MainWnd mainwnd, Shell parent)
	{
		super(mainwnd, parent, "Filter number detections");
	}
	
	@Override
	public void init()
	{
		super.init();
		
		// initialize the action to null
		action = null;
		
		// create the layout
		shell.setLayout(new GridLayout(2, false));
		
		// create the widgets for the rt range
		txt_nrdetections = new Text(shell, SWT.SINGLE|SWT.BORDER);
		
		// create the widgets for the dialog
		btn_ok = new Button(shell, SWT.Selection);
		btn_ok.setText("Ok");
		btn_ok.addListener(SWT.Selection, this);
		
		btn_cancel = new Button(shell, SWT.Selection);
		btn_cancel.setText("Cancel");
		btn_cancel.addListener(SWT.Selection, this);
		
		//
		shell.pack();
	}
	
	
	// Listeners overrides
	public void handleEvent(Event event)
	{
		if (event.widget == btn_ok)
		{
			int nrdetections = 0;
			
			try
			{
				nrdetections = Integer.parseInt(txt_nrdetections.getText());
			}
			catch (Exception e)
			{
				MessageBox msg = new MessageBox(shell, SWT.OK);
				msg.setText("Error");
				msg.setMessage("Invalid number.");
				msg.open();
				return;
			}
			
			action = new FilterNrDetections(nrdetections);
			shell.dispose();
		}
		else if (event.widget == btn_cancel)
		{
			shell.dispose();
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
		return "Filter number detections";
	}
	
	
	// data
	protected FilterNrDetections action;
	
	protected Text txt_nrdetections;
	
	protected Button btn_ok;
	protected Button btn_cancel;
}
