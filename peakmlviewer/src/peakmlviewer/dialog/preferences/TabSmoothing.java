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



package peakmlviewer.dialog.preferences;


// java

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml
import peakml.math.filter.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.util.*;





/**
 * 
 */
public class TabSmoothing extends Composite implements Listener
{
	// constructor(s)
	public TabSmoothing(Composite parent, MainWnd mainwnd, int style)
	{
		super(parent, style);
		
		// save the mainwnd
		this.mainwnd = mainwnd;
		
		// set the layout
		FormLayout layout = new FormLayout();
		layout.marginLeft = 5;
		layout.marginRight = 5;
		setLayout(layout);
		
		// create the no-filter
		btn_nofilter = new Button(this, SWT.CHECK);
		btn_nofilter.setText("No Filter");
		btn_nofilter.setSelection(true);
		btn_nofilter.addListener(SWT.Selection, this);
		
		// add a line
		
		// create the general options
		Group grp_generaloptions = new Group(this, SWT.SHADOW_ETCHED_IN);
		grp_generaloptions.setText("General options");
		grp_generaloptions.setLayout(new FillLayout());
		
		FormData grp_generaloptions_data = new FormData();
		grp_generaloptions.setLayoutData(grp_generaloptions_data);
		grp_generaloptions_data.left = new FormAttachment(0);
		grp_generaloptions_data.right = new FormAttachment(100);
		grp_generaloptions_data.top = new FormAttachment(btn_nofilter);
		
		btn_general_showoriginal = new Button(grp_generaloptions, SWT.CHECK);
		btn_general_showoriginal.setText("Show original");
		btn_general_showoriginal.addListener(SWT.Selection, this);
		
		// create the sliding mean group
		
		// create the Savitzky Golay group
		Group grp_savitzkygolay = new Group(this, SWT.SHADOW_ETCHED_IN);
		grp_savitzkygolay.setText("Savitzky-Golay");
		grp_savitzkygolay.setLayout(new FillLayout());
		
		FormData grp_savitzkygolay_data = new FormData();
		grp_savitzkygolay_data.left = new FormAttachment(0);
		grp_savitzkygolay_data.right = new FormAttachment(100);
		grp_savitzkygolay_data.top = new FormAttachment(grp_generaloptions);
		grp_savitzkygolay.setLayoutData(grp_savitzkygolay_data);
		
		btn_savitzkygolay_enable = new Button(grp_savitzkygolay, SWT.CHECK);
		btn_savitzkygolay_enable.setText("Enable");
		btn_savitzkygolay_enable.addListener(SWT.Selection, this);
		
		combo_savitzkygolay_points = new Combo(grp_savitzkygolay, SWT.DROP_DOWN);
		for (SavitzkyGolayFilter.Points points : SavitzkyGolayFilter.Points.values())
			combo_savitzkygolay_points.add(points.toString());
		
		// enable/disable the controls
		if (Settings.smooth == false)
			enable(FILTER_NONE);
		else
			enable(FILTER_SAVITZKY_GOLAY);
	}
	
	
	// 
	public void updateSettings()
	{
		if (btn_nofilter.getSelection())
		{
			Settings.smooth = false;
			Settings.smooth_show_original = false;
		}
		else if (btn_savitzkygolay_enable.getSelection())
		{
			Settings.smooth = true;
			if (btn_general_showoriginal.getSelection())
				Settings.smooth_show_original = true;
			else
				Settings.smooth_show_original = false;
		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event e)
	{
		if (e.widget == btn_nofilter)
		{
			enable(FILTER_NONE);
		}
		else if (e.widget == btn_savitzkygolay_enable)
		{
			enable(FILTER_SAVITZKY_GOLAY);
		}
	}
	
	
	//
	protected static int FILTER_NONE = 0;
	protected static int FILTER_SAVITZKY_GOLAY = 1;
	protected void enable(int filter)
	{
		btn_nofilter.setSelection(filter == FILTER_NONE);
		btn_savitzkygolay_enable.setSelection(filter == FILTER_SAVITZKY_GOLAY);
		
		// enable/disable general options
		btn_general_showoriginal.setEnabled(filter != FILTER_NONE);
		
		// enable/disable savitzky golay options
		combo_savitzkygolay_points.setEnabled(filter == FILTER_SAVITZKY_GOLAY);
	}
	
	
	// data
	protected MainWnd mainwnd;
	
	protected Button btn_nofilter;
	
	protected Button btn_general_showoriginal;
	
	protected Button btn_savitzkygolay_enable;
	protected Combo combo_savitzkygolay_points;
}
