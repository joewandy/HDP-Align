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
import org.eclipse.swt.graphics.*;

// libraries

// peakml
import peakml.io.*;

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class SetView extends View implements Listener
{
	// constructor(s)
	public SetView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.BORDER);
		
		// set the layout
		setLayout(new FillLayout());
		
		//
		tree_sets = new Tree(this, SWT.BORDER|SWT.MULTI|SWT.CHECK|SWT.H_SCROLL|SWT.V_SCROLL);
		tree_sets.setLinesVisible(true);
		tree_sets.setHeaderVisible(true);
		tree_sets.addListener(SWT.Selection, this);
		tree_sets.addListener(SWT.MouseDown, this);
		
		TreeColumn clm_name = new TreeColumn(tree_sets, SWT.LEFT);
		clm_name.setText("Name");
		clm_name.setWidth(120);
		
		TreeColumn clm_profile = new TreeColumn(tree_sets, SWT.LEFT);
		clm_profile.setText("Color");
		clm_profile.setWidth(60);
	}
	
	protected void initSet(Document document, SetInfo set, TreeItem parent)
	{
		Header header = document.getHeader();
		
		int color = document.getSetColor(set.getID());
		
		TreeItem item_set;
		if (parent == null)
			item_set = new TreeItem(tree_sets, SWT.None);
		else
			item_set = new TreeItem(parent, SWT.None);
		
		item_set.setChecked(set.getVisible());
		item_set.setText(0, set.getID());
		item_set.setText(1, "======");
		item_set.setForeground(1,
				new Color(mainwnd.getDisplay(), (color>>16)&255, (color>>8)&255, color&255)
			);
		item_set.setData(set);
		
		for (SetInfo subset : set.getChildren())
			initSet(document, subset, item_set);
		for (MeasurementInfo measurement : header.getMeasurementInfos(set))
		{
			TreeItem item_profile = new TreeItem(item_set, SWT.None);
			item_profile.setText(0, measurement.getLabel());
			item_profile.setChecked(measurement.getVisible());
			item_profile.setData(measurement);
		}
	}
	
	protected void init()
	{
		MainWnd mainwnd = getMainWnd();
		Document document = mainwnd.getDocument();
		
		tree_sets.removeAll();
		
		Header header = document.getHeader();
		if (header == null)
			return;
		
		for (SetInfo set : header.getSetInfos())
			initSet(document, set, null);
	}
	
	
	// View overrides
	public void update(int event)
	{
		if (event==Document.UPDATE_INIT || event==Document.UPDATE_FILE_LOAD)
		{
			init();
		}
	}


	// 
	public void handleEvent(Event event)
	{
		Document document = mainwnd.getDocument();
		
		if (event.widget==tree_sets && event.detail==SWT.CHECK)
		{
			TreeItem item = (TreeItem) event.item;
			boolean check = item.getChecked();
			
			// set the property for the selected set
			Object data = item.getData();
			if (data.getClass().equals(MeasurementInfo.class))
			{
				((MeasurementInfo) data).setVisible(check);
			}
			else if (data.getClass().equals(SetInfo.class))
			{
				((SetInfo) data).setVisible(check);
				
				// set the chosen check for all the profiles
				TreeItem[] items = item.getItems();
				for (int i=0; i<items.length; ++i)
				{
					items[i].setChecked(check);
					((MeasurementInfo) items[i].getData()).setVisible(check);
				}
			}
			
			mainwnd.update(Document.UPDATE_INDEX_CHANGED);
		}
		else if (event.type==SWT.MouseDown && event.button==3)
		{
			ColorDialog dlg = new ColorDialog(mainwnd.getShell());
			
			RGB color = dlg.open();
			if (color != null)
			{
				for (TreeItem item : tree_sets.getSelection())
					document.setSetColor(item.getText(), color);
				
				init();
				mainwnd.update(Document.UPDATE_INDEX_CHANGED);
			}
		}
	}
	
	
	// data
	/** */
	protected Tree tree_sets;
}
