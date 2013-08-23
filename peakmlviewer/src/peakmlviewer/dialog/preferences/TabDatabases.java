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

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.util.*;





/**
 * 
 */
public class TabDatabases extends Composite implements Listener
{
	// constructor(s)
	public TabDatabases(Composite parent, MainWnd mainwnd, int style)
	{
		super(parent, style);
		
		// save the mainwnd
		this.mainwnd = mainwnd;
		
		// create the widgets
		table_databases = new Table(this, SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.FLAT);
		table_databases.setLinesVisible(true);
		table_databases.setHeaderVisible(true);
		table_databases.addListener(SWT.Selection, this);
		
		TableColumn clm_name = new TableColumn(table_databases, SWT.LEFT);
		clm_name.setText("Name");
		clm_name.setWidth(75);
		
		TableColumn clm_filename = new TableColumn(table_databases, SWT.LEFT);
		clm_filename.setText("Filename");
		clm_filename.setWidth(250);
		
		TableColumn clm_size = new TableColumn(table_databases, SWT.LEFT);
		clm_size.setText("Size");
		clm_size.setWidth(50);
		
		TableColumn clm_description = new TableColumn(table_databases, SWT.LEFT);
		clm_description.setText("Description");
		clm_description.setWidth(250);
		
		btn_add = new Button(this, SWT.Selection);
		btn_add.setText("Add");
		btn_add.addListener(SWT.Selection, this);
		
		btn_remove = new Button(this, SWT.Selection);
		btn_remove.setText("Remove");
		btn_remove.addListener(SWT.Selection, this);
		
		// setup the layout
		GridLayout gridlayout = new GridLayout(2, false);
		setLayout(gridlayout);
		
		GridData grid_databases = new GridData(GridData.FILL_VERTICAL|GridData.FILL_HORIZONTAL);
		grid_databases.verticalSpan = 2;
		table_databases.setLayoutData(grid_databases);
		
		GridData grid_add = new GridData(GridData.FILL_HORIZONTAL);
		grid_add.grabExcessVerticalSpace = false;
		grid_add.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		btn_add.setLayoutData(grid_add);
		
		GridData grid_remove = new GridData();
		grid_remove.grabExcessVerticalSpace = false;
		grid_remove.verticalAlignment = GridData.VERTICAL_ALIGN_BEGINNING;
		btn_remove.setLayoutData(grid_remove);
		
		// fill the database table
		for (String filename : Settings.databases)
		{
			TableItem item = new TableItem(table_databases, SWT.NONE);
			item.setText(1, filename);
		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event e)
	{
		Document document = mainwnd.getDocument();
		if (e.widget == btn_add)
		{
			FileDialog dlg = new FileDialog(mainwnd.getShell(), SWT.OPEN);
			String filename = dlg.open();
			if (filename == null)
				return;
			
			
		}
		else if (e.widget == btn_remove)
		{
			
		}
	}
	
	
	// data
	protected MainWnd mainwnd;
	
	protected Table table_databases;
	protected Button btn_add;
	protected Button btn_remove;
}
