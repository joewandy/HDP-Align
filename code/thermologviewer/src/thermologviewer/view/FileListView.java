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
import java.util.*;

// swt
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// PeakML
import peakml.io.xrawfile.*;

// ThermoLogViewer
import thermologviewer.*;
import thermologviewer.data.*;





/**
 * 
 */
public class FileListView extends View implements Listener
{
	// constructor(s)
	public FileListView(MainWnd mainwnd)
	{
		super(mainwnd, mainwnd.getShell(), SWT.None);
		
		// layout
		setLayout(new FillLayout());
		
		// add the widgets
		tbl_files = new Table(this, SWT.FULL_SELECTION|SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.FLAT);
		tbl_files.setLinesVisible(true);
		tbl_files.setHeaderVisible(true);
		tbl_files.addListener(SWT.Selection, this);
		tbl_files.addListener(SWT.MouseDown, this);
		
		clm_files_file = new TableColumn(tbl_files, SWT.LEFT);
		clm_files_file.setText("File");
		clm_files_file.setWidth(100);
		clm_files_controller = new TableColumn(tbl_files, SWT.LEFT);
		clm_files_controller.setText("Controller");
		clm_files_controller.setWidth(75);
		clm_files_color = new TableColumn(tbl_files, SWT.LEFT);
		clm_files_color.setText("Color");
		clm_files_color.setWidth(75);
	}
	
	
	// View overrides
	public void update(int event)
	{
		Document document = mainwnd.getDocument();
		if (event == Document.UPDATE_ADD_FILE)
		{
			tbl_files.removeAll();
			for (RawFile rawfile : document.getFiles())
			{
				Vector<String> controllers = new Vector<String>();
				for (IXRawfile.Controller controller : rawfile.getControllers())
				{
					switch (controller.getControllerType())
					{
					case IXRawfile.CONTROLLER_MS:
						controllers.add("MS (" + controller.getControllerNumber() + ")");
						break;
					case IXRawfile.CONTROLLER_ANALOG:
						controllers.add("ANALOG (" + controller.getControllerNumber() + ")");
						break;
					case IXRawfile.CONTROLLER_AD_CARD:
						controllers.add("A/D CARD (" + controller.getControllerNumber() + ")");
						break;
					case IXRawfile.CONTROLLER_PDA:
						controllers.add("PDA (" + controller.getControllerNumber() + ")");
						break;
					case IXRawfile.CONTROLLER_UV:
						controllers.add("UV (" + controller.getControllerNumber() + ")");
						break;
					}
				}
				
				int color = rawfile.getColor();
				
				TableItem item = new TableItem(tbl_files, SWT.NONE);
				item.setData(rawfile);
				item.setText(0, rawfile.getFilename());
				item.setText(1, controllers.firstElement());
				item.setText(2, "======");
				item.setForeground(2, new Color(mainwnd.getDisplay(), (color>>16)&255, (color>>8)&255, color&255));
			}
		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event e)
	{
		if (e.type==SWT.MouseDown && e.button==3)
		{
			ColorDialog dlg = new ColorDialog(mainwnd.getShell());
			
			RGB color = dlg.open();
			for (TableItem item : tbl_files.getSelection())
				((RawFile) item.getData()).setColor(color.red<<16|color.green<<8|color.blue);
			
			mainwnd.update(Document.UPDATE_ADD_FILE);
		}
	}
	
	
	// data
	protected Table tbl_files;
	protected TableColumn clm_files_file;
	protected TableColumn clm_files_controller;
	protected TableColumn clm_files_color;
}
