/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of ThermoLogViewer.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package thermologviewer;


// java
import java.io.*;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// libraries

// peakml

// ThermoLogViewer
import thermologviewer.view.*;





/**
* 
*/
public class MainWnd implements Listener
{
	// constructor(s)
	public MainWnd(int width, int height, String title)
	{
		display = new Display();
		shell = new Shell(display);
		
		// set the properties of the main-window
		shell.setText(title);
		shell.setSize(width, height);
		
//		Image images[] = {
//				new Image(display, Resource.getResourceAsStream(Resource.ICON_APPLICATION_SMALL)),
//				new Image(display, Resource.getResourceAsStream(Resource.ICON_APPLICATION_LARGE))
//			};
//		shell.setImages(images);
		
		// add listeners
		shell.addListener(SWT.KeyDown, this);
		
		// create the document
		document = new Document(this);
		
		// set the layout
		FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		
		// create the menu-bar
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		// create the file-menu
		Menu file_menu = createMenu(menu, "&File");
		
		file_open				= createMenuItem(file_menu, "&Open...",					null, SWT.PUSH, SWT.None);
		
		
		// create the view(s)
		FormData layout_control = new FormData();
		layout_control.left			= new FormAttachment(0);
		layout_control.right		= new FormAttachment(30);
		layout_control.top			= new FormAttachment(0);
		layout_control.bottom		= new FormAttachment(30);
		view_control = new ControlView(this);
		view_control.setLayoutData(layout_control);
		
		FormData layout_filelist = new FormData();
		layout_filelist.left		= new FormAttachment(0);
		layout_filelist.right		= new FormAttachment(30);
		layout_filelist.top			= new FormAttachment(view_control);
		layout_filelist.bottom		= new FormAttachment(100);
		view_filelist = new FileListView(this);
		view_filelist.setLayoutData(layout_filelist);
		
		FormData layout_graph = new FormData();
		layout_graph.left			= new FormAttachment(view_filelist);
		layout_graph.right			= new FormAttachment(100);
		layout_graph.top			= new FormAttachment(0);
		layout_graph.bottom			= new FormAttachment(100);
		view_graph = new GraphView(this);
		view_graph.setLayoutData(layout_graph);
		
		// init
		document.init();
	}
	
	
	// access
	public void update(int event)
	{
		view_graph.update(event);
		view_control.update(event);
		view_filelist.update(event);
	}
	
	public Document getDocument()
	{
		return document;
	}
	
	
	// SWT-specific
	/**
	 * 
	 */
	public void dispose()
	{
		// destroy it all
		document.finish();
		display.dispose();
	}
	
	/**
	 * 
	 */
	public void display()
	{
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		dispose();
	}
	
	/**
	 * 
	 */
	public Shell getShell()
	{
		return shell;
	}
	
	/**
	 * 
	 */
	public Display getDisplay()
	{
		return display;
	}
	
	/**
	 * 
	 */
	public void getFocus()
	{
		display.asyncExec(new Runnable() {
			public void run() {
				shell.forceFocus();
			}
		});
	}
	
	/**
	 * 
	 */
	public void handleEvent(Event event)
	{
		if (event.widget == file_open)
		{
			FileDialog dlg = new FileDialog(shell, SWT.OPEN|SWT.MULTI);
			String open = dlg.open();
			if (open == null)
				return;
			
			File path = new File(open).getParentFile();
			for (String filename : dlg.getFileNames())
				document.addFile(path.getAbsolutePath() + "/" + filename);
			update(Document.UPDATE_ADD_FILE);
		}
	}
	
	// utility functions
	/** */
	protected Menu createMenu(Menu menu, String name)
	{
		Menu m = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.CASCADE);
		
		item.setMenu(m);
		item.setText(name);
		
		return m;
	}
	
	/** */
	protected MenuItem createMenuItem(Menu menu, String name, Image icon, int style, int accelerator)
	{
		MenuItem m = new MenuItem(menu, style);
		
		m.setText(name);
		m.setAccelerator(accelerator);
		m.addListener(SWT.Selection, this);
		
		return m;
	}
	
	
	// data
	protected Shell shell;
	protected Display display;
	
	protected Document document;
	
	protected MenuItem file_open;
	
	protected GraphView view_graph;
	protected ControlView view_control;
	protected FileListView view_filelist;
}
