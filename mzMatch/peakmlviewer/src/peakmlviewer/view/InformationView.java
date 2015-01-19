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

// standard widget toolkit
import org.eclipse.swt.widgets.*;

// metabolome

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class InformationView extends View
{
	// constructor(s)
	public InformationView(MainWnd mainwnd, Composite parent, int style)
	{
		super(mainwnd, parent, style);
	}
	
	
	// View overrides
	@Override
	public void update(int event)
	{
		view_info.update(event);
		view_control.update(event);
	}
	
	
	// data
	protected InfoView view_info;
	protected ControlView view_control;
}
