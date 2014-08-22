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
import java.io.*;
import java.util.*;

import javax.imageio.ImageIO;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.dialog.peakinformation.*;





/**
 * 
 */
public class PeakInformationDialog extends Dialog implements Listener
{
	// constructor(s)
	public PeakInformationDialog(MainWnd mainwnd, Shell parent, String title)
	{
		super(parent, SWT.Resize);
		
		// save the parent pointer
		this.title = title;
		this.parent = parent;
		
		this.mainwnd = mainwnd;
		
		// create the window and set its properties
		shell = new Shell(parent, SWT.EMBEDDED|SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setSize(750, 300);
		shell.setText(title);
		
		// set the layout
		shell.setLayout(new GridLayout(4, false));
		
		
		// create the components
		tbl_ipeak_info = new Table(shell, SWT.FULL_SELECTION|SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.FLAT);
		tbl_ipeak_info.setLinesVisible(true);
		tbl_ipeak_info.setHeaderVisible(true);
		GridData layout_ipeak_info = new GridData(GridData.FILL_VERTICAL);
		tbl_ipeak_info.setLayoutData(layout_ipeak_info);
		
		TableColumn clm_set = new TableColumn(tbl_ipeak_info, SWT.LEFT);
		clm_set.setText("Set");
		clm_set.setWidth(45);
		
		TableColumn clm_profile = new TableColumn(tbl_ipeak_info, SWT.LEFT);
		clm_profile.setText("Profile");
		clm_profile.setWidth(45);
		
		TableColumn clm_rt = new TableColumn(tbl_ipeak_info, SWT.LEFT);
		clm_rt.setText("RT");
		clm_rt.setWidth(45);
		
		TableColumn clm_mass = new TableColumn(tbl_ipeak_info, SWT.LEFT);
		clm_mass.setText("Mass");
		clm_mass.setWidth(75);
		
		TableColumn clm_intensity = new TableColumn(tbl_ipeak_info, SWT.LEFT);
		clm_intensity.setText("Intensity");
		clm_intensity.setWidth(75);
		
		graph = new Graph(shell);
		GridData layout_graph = new GridData(GridData.FILL_BOTH);
		layout_graph.grabExcessVerticalSpace = true;
		layout_graph.grabExcessHorizontalSpace = true;
		layout_graph.horizontalSpan = 3;
		graph.setLayoutData(layout_graph);
		
		btn_ok = new Button(shell, SWT.PUSH);
		btn_ok.setText("Ok");
		btn_ok.addListener(SWT.Selection, this);
		
		btn_export = new Button(shell, SWT.PUSH);
		btn_export.setText("Export graph");
		btn_export.addListener(SWT.Selection, this);
		
		txt_formula = new Text(shell, SWT.BORDER);
		
		btn_update = new Button(shell, SWT.PUSH);
		btn_update.setText("Update");
		btn_update.addListener(SWT.Selection, this);
		
		//
		shell.addListener(SWT.KeyDown, this);
		tbl_ipeak_info.addListener(SWT.KeyDown, this);
		graph.addListener(SWT.KeyDown, this);
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
		
		// update the graph
		update();
		
		// open the window
		shell.open();
		
		// enter the event-loop
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	
	// listener overrides
	public void handleEvent(Event e)
	{
		if (e.type == SWT.KeyDown)
		{
			if (e.keyCode!='c' && e.stateMask!=SWT.CTRL)
				return;
			
			copySelected();
		}
		else if (e.widget == btn_ok)
		{
			shell.close();
		}
		else if (e.widget == btn_export)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			String filename = dlg.open();
			if (filename == null)
				return;
			
			try
			{
				ImageIO.write(graph.getGraphImage(800, 500), "png", new File(filename));
			}
			catch (Exception exc)
			{
				exc.printStackTrace();
			}
		}
		else if (e.widget == btn_update)
		{
			update();
		}
	}
	
	
	// access
	public void update()
	{
		Document document = mainwnd.getDocument();
		IPeak peak = document.getCurrentPeak();
		
		if (peak != null)
		{
			MolecularFormula formula = null;
			try {
				if (txt_formula.getText().length() != 0)
					formula = new MolecularFormula(txt_formula.getText());
			} catch (Exception e) { formula = null; }
			
			graph.setPeak(peak, document, formula);
			
			updateTable(peak);
		}
	}
	
	
	// implementations for actions
	protected void copySelected()
	{
		String object = "";
		for (int index : tbl_ipeak_info.getSelectionIndices())
		{
			TableItem item = tbl_ipeak_info.getItem(index);
			object += item.getText(0) +"\t"+ item.getText(1) +"\t"+ item.getText(2) +"\t"+ item.getText(3) +"\t"+ item.getText(4) + "\n";//ipeak.getScanID() + "\t" + ipeak.getMass() + "\t" + ipeak.getIntensity() + "\n";
		}
		
		Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
		clipboard.setContents(
				new Object[] { object.substring(0, object.length()-1) },
				new Transfer[] { TextTransfer.getInstance() }
			);
		clipboard.dispose();
	}
	
	@SuppressWarnings("unchecked")
	protected void updateTable(IPeak peak)
	{
		Document document = mainwnd.getDocument();
		
		Class<? extends IPeak> c = peak.getClass();
		if (c.equals(IPeakSet.class))
		{
			IPeakSet<? extends IPeak> peakset = (IPeakSet<? extends IPeak>) peak;
			for (IPeak p : peakset)
				updateTable(p);
		}
		else
		{
			peaks.add(peak);
			
			TableItem item = new TableItem(tbl_ipeak_info, SWT.NONE);
			item.setText(0, document.getSetName(peak));
			item.setText(1, Integer.toString(peak.getMeasurementID()));
			item.setText(2, Integer.toString(peak.getScanID()));
			item.setText(3, Double.toString(peak.getMass()));
			item.setText(4, Double.toString(peak.getIntensity()));
		}
	}
	
	
	// data
	protected Shell shell;
	protected Shell parent;
	
	protected String title;
	protected MainWnd mainwnd;
	
	protected Table tbl_ipeak_info;
	protected Graph graph;
	
	protected Button btn_ok;
	protected Button btn_export;
	protected Text txt_formula;
	protected Button btn_update;
	
	protected Vector<IPeak> peaks = new Vector<IPeak>();
}
