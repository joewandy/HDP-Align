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
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// peakml

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.action.*;





/**
 * 
 */
public abstract class ActionDialog extends Dialog
{
	// constructor(s)
	public ActionDialog(MainWnd mainwnd, Shell parent, String title)
	{
		super(parent, SWT.NONE);
		
		// save the parent pointer
		this.title = title;
		this.parent = parent;
		
		this.mainwnd = mainwnd;
	}
	
	public void init()
	{
		// create the window and set its properties
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setSize(500, 300);
		shell.setText(title);
	}
	
	
	// SWT specific
	/**
	 * 
	 */
	public Action open()
	{
		// retrieve the display
		Display display = parent.getDisplay();
		
		// init the dialog
		init();
		
		// place the dialog at the center of the parent
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = shell.getBounds();
		shell.setLocation(new Point(
				(parentSize.width - mySize.width)/2+parentSize.x,
				(parentSize.height - mySize.height)/2+parentSize.y
			));
		
		// open the window
		shell.open();
		
		// enter the event-loop
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		return getAction();
	}
	
	
	// overridable interface
	/**
	 * 
	 */
	public abstract String getName();
	
	/**
	 * 
	 */
	protected abstract Action getAction();
	
	
	// data
	/** */
	protected Shell shell;
	/** */
	protected Shell parent;
	/** */
	protected String title;
	/** */
	protected MainWnd mainwnd;
}
