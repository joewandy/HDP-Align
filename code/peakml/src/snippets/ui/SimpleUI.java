/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
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



package snippets.ui;

// java
import java.io.*;

// standard widget toolkit
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.io.peakml.*;
import peakml.util.swt.*;
import peakml.util.swt.widget.*;



public class SimpleUI implements Listener
{
	// constructor(s)
	public SimpleUI()
	{
		display = new Display();
		shell = new Shell(display);
		
		// set the properties of the main-window
		shell.setText("Simple UI");
		shell.setSize(400, 300);
		
		// create the file-menu
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		Menu file_menu = SWTTools.createMenu(menu, "&File");
		file_open = SWTTools.createMenuItem(this, file_menu, "&Open...", null, SWT.PUSH, SWT.None);
		
		// set the layout
		shell.setLayout(new FillLayout());
		
		// add the components
		graph = new IPeakGraph(shell, SWT.NONE);
	}
	
	// Listener overrides
	public void handleEvent(Event e)
	{
		if (e.widget == file_open)
		{
			FileDialog dlg = new FileDialog(shell, SWT.OPEN|SWT.MULTI);
			String open = dlg.open();
			if (open == null)
				return;
			
			try {
				ParseResult parseresult = PeakMLParser.parse(new FileInputStream(open), true);
				if (parseresult.measurement.getClass().equals(IPeakSet.class))
				{
					peaks = (IPeakSet<IPeak>) parseresult.measurement;
					header = parseresult.header;
					graph.setPeak(peaks.get(0), header, null);
				}
			} catch (Exception ee) { ee.printStackTrace(); }
		}
	}
	
	// SWT binding
	public void display()
	{
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	
	// data
	protected Shell shell;
	protected Display display;
	protected MenuItem file_open;
	protected IPeakGraph graph;
	protected Header header;
	protected IPeakSet<IPeak> peaks;
	
	// main entry
	public static void main(String args[])
	{
		SimpleUI simpleui = new SimpleUI();
		simpleui.display();
	}
}
