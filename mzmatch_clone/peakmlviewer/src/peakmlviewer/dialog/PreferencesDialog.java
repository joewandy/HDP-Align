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
import org.eclipse.swt.custom.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.dialog.preferences.*;





/**
 * 
 */
public class PreferencesDialog extends Dialog implements Listener
{
	// constructor(s)
	public PreferencesDialog(MainWnd mainwnd, Shell parent, String title)
	{
		super(parent, SWT.NONE);
		
		// save the parent pointer
		this.title = title;
		this.parent = parent;
		
		this.mainwnd = mainwnd;
		
		// create the window and set its properties
		shell = new Shell(parent, SWT.EMBEDDED|SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setSize(500, 300);
		shell.setText(title);
		
		// set the layout
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		shell.setLayout(layout);
		
		// create the tabs
		GridData tabfolder_data = new GridData(GridData.FILL_BOTH);
		tabfolder_data.horizontalSpan = 2;
		
		tabfolder = new CTabFolder(shell, SWT.BORDER);
		tabfolder.setLayoutData(tabfolder_data);
		tabfolder.setSimple(false);
		
		// create the smoothing tab
		view_smoothing = new TabSmoothing(tabfolder, mainwnd, SWT.NONE);
		CTabItem tab_smoothing = new CTabItem(tabfolder, SWT.NONE);
		tab_smoothing.setText("Curve smoothing");
		tab_smoothing.setControl(view_smoothing);
		
		// create the hotkey tab
		CTabItem tab_hotkey = new CTabItem(tabfolder, SWT.NONE);
		tab_hotkey.setText("Hot keys");
		
		// create the databases tab
		view_databases = new TabDatabases(tabfolder, mainwnd, SWT.NONE);
		CTabItem tab_databases = new CTabItem(tabfolder, SWT.NONE);
		tab_databases.setText("Databases");
		tab_databases.setControl(view_databases);
		
		// insert the ok/cancel buttons
		btn_ok = new Button(shell, SWT.PUSH);
		btn_ok.setText("Ok");
		btn_ok.addListener(SWT.Selection, this);
		
		btn_cancel = new Button(shell, SWT.PUSH);
		btn_cancel.setText("Cancel");
		btn_cancel.addListener(SWT.Selection, this);
	}
	
	
	// SWT specific
	public void open()
	{
		// retrieve the display
		Display display = parent.getDisplay();
		
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
	}
	
	
	// Listener overrides
	public void handleEvent(Event e)
	{
		if (e.widget == btn_ok)
		{
			view_smoothing.updateSettings();
			shell.dispose();
		}
		else if (e.widget == btn_cancel)
		{
			shell.dispose();
		}
	}
	
	
	// data
	protected Shell shell;
	protected Shell parent;
	
	protected String title;
	protected MainWnd mainwnd;
	
	protected CTabFolder tabfolder;
	protected TabSmoothing view_smoothing;
	protected TabDatabases view_databases;
	
	protected Button btn_ok;
	protected Button btn_cancel;
}
