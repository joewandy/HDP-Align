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
public class FilterIntensityDialog extends ActionDialog implements Listener
{
	// constructor(s)
	public FilterIntensityDialog(MainWnd mainwnd, Shell parent)
	{
		super(mainwnd, parent, "Filter intensity");
	}
	
	@Override
	public void init()
	{
		super.init();
		
		// initialize the action to null
		action = null;
		
		// create the layout
		shell.setLayout(new GridLayout(3, false));
		
		// create the intensity widgets
		lbl_intensity = new Label(shell, SWT.None);
		lbl_intensity.setText("Minimum intensity: ");
		
		txt_intensity = new Text(shell, SWT.BORDER);
		
		cmb_intensity_units = new Combo(shell, SWT.None);
		for (int i=0; i<FilterIntensity.UNIT_SIZE; ++i)
			cmb_intensity_units.add(FilterIntensity.unitnames[i]);
		cmb_intensity_units.select(0);
		
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
			try
			{
				int units = cmb_intensity_units.getSelectionIndex();
				double intensity = Double.parseDouble(txt_intensity.getText());
				
				action = new FilterIntensity(intensity, units);
			}
			catch (Exception e)
			{
				MessageBox msg = new MessageBox(shell, SWT.OK);
				msg.setText("Error");
				msg.setMessage("Invalid number.");
				msg.open();
				return;
			}
			
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
		return "Filter intensity";
	}
	
	
	// data
	protected FilterIntensity action;
	
	protected Label lbl_intensity;
	protected Text txt_intensity;
	protected Combo cmb_intensity_units;
	
	protected Button btn_ok;
	protected Button btn_cancel;
}
