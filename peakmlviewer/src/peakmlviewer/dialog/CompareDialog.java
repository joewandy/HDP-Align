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

// standard widget toolkit
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.widgets.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;

import peakml.util.swt.widget.*;





/**
 * 
 */
public class CompareDialog extends Dialog implements Listener
{
	// constructor(s)
	public CompareDialog(MainWnd mainwnd, Shell parent, String title)
	{
		super(parent, SWT.NONE);
		
		// save the parent pointer
		this.parent = parent;
		this.mainwnd = mainwnd;
		
		// create the window and set its properties
		shell = new Shell(parent, SWT.EMBEDDED|SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL|SWT.RESIZE);
		shell.setSize(700, 450);
		shell.setText(title);
		
		// layout
		FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		
		// add the components
		lbl_ppm = new Label(shell, SWT.NONE);
		lbl_ppm.setText("PPM");
		txt_ppm = new Text(shell, SWT.NONE);
		
		lbl_rt = new Label(shell, SWT.NONE);
		lbl_rt.setText("Retention time");
		txt_rt = new Text(shell, SWT.NONE);
		
		graph_intensity = new IPeakIntensityGraph(shell, SWT.NONE);
		FormData graph_intensity_data = new FormData();
		graph_intensity_data.top	= new FormAttachment(0, 5);
		graph_intensity_data.left	= new FormAttachment(0, 20);
		graph_intensity_data.right	= new FormAttachment(100, -20);
		graph_intensity_data.bottom	= new FormAttachment(0, 150);
		graph_intensity.setLayoutData(graph_intensity_data);
		
		tbl_peaks1 = new Table(shell, SWT.CHECK|SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.FLAT);
		tbl_peaks1.setLinesVisible(true);
		tbl_peaks1.setHeaderVisible(true);
		tbl_peaks1.addListener(SWT.Selection, this);
		FormData tbl_peaks1_data = new FormData();
		tbl_peaks1_data.top		= new FormAttachment(graph_intensity, 5);
		tbl_peaks1_data.left	= new FormAttachment(50, -125);
		tbl_peaks1_data.right	= new FormAttachment(50, -2);
		tbl_peaks1_data.bottom	= new FormAttachment(100, -30);
		tbl_peaks1.setLayoutData(tbl_peaks1_data);
		
		TableColumn clm_peaks_id1 = new TableColumn(tbl_peaks1, SWT.LEFT);
		clm_peaks_id1.setText("");
		clm_peaks_id1.setWidth(20);
		clm_peaks_id1.addListener(SWT.Selection, this);
		
		TableColumn clm_peaks_rt1 = new TableColumn(tbl_peaks1, SWT.LEFT);
		clm_peaks_rt1.setText("RT");
		clm_peaks_rt1.setWidth(30);
		clm_peaks_rt1.addListener(SWT.Selection, this);
		
		TableColumn clm_peaks_mass1 = new TableColumn(tbl_peaks1, SWT.LEFT);
		clm_peaks_mass1.setText("Mass");
		clm_peaks_mass1.setWidth(75);
		clm_peaks_mass1.addListener(SWT.Selection, this);
		
		TableColumn clm_peaks_intensity1 = new TableColumn(tbl_peaks1, SWT.LEFT);
		clm_peaks_intensity1.setText("Intensity");
		clm_peaks_intensity1.setWidth(75);
		clm_peaks_intensity1.addListener(SWT.Selection, this);
		
		tbl_peaks2 = new Table(shell, SWT.CHECK|SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.FLAT);
		tbl_peaks2.setLinesVisible(true);
		tbl_peaks2.setHeaderVisible(true);
		tbl_peaks2.addListener(SWT.Selection, this);
		FormData tbl_peaks2_data = new FormData();
		tbl_peaks2_data.top		= new FormAttachment(graph_intensity, 5);
		tbl_peaks2_data.left	= new FormAttachment(50);
		tbl_peaks2_data.right	= new FormAttachment(50, 125);
		tbl_peaks2_data.bottom	= new FormAttachment(100, -30);
		tbl_peaks2.setLayoutData(tbl_peaks2_data);
		
		TableColumn clm_peaks_id2 = new TableColumn(tbl_peaks2, SWT.LEFT);
		clm_peaks_id2.setText("");
		clm_peaks_id2.setWidth(20);
		clm_peaks_id2.addListener(SWT.Selection, this);
		
		TableColumn clm_peaks_rt2 = new TableColumn(tbl_peaks2, SWT.LEFT);
		clm_peaks_rt2.setText("RT");
		clm_peaks_rt2.setWidth(30);
		clm_peaks_rt2.addListener(SWT.Selection, this);
		
		TableColumn clm_peaks_mass2 = new TableColumn(tbl_peaks2, SWT.LEFT);
		clm_peaks_mass2.setText("Mass");
		clm_peaks_mass2.setWidth(75);
		clm_peaks_mass2.addListener(SWT.Selection, this);
		
		TableColumn clm_peaks_intensity2 = new TableColumn(tbl_peaks2, SWT.LEFT);
		clm_peaks_intensity2.setText("Intensity");
		clm_peaks_intensity2.setWidth(75);
		clm_peaks_intensity2.addListener(SWT.Selection, this);
		
		graph1 = new IPeakGraph(shell, SWT.NONE);
		FormData graph1_data = new FormData();
		graph1_data.top		= new FormAttachment(graph_intensity, 5);
		graph1_data.left	= new FormAttachment(0);
		graph1_data.right	= new FormAttachment(tbl_peaks1);
		graph1_data.bottom	= new FormAttachment(100, -30);
		graph1.setLayoutData(graph1_data);
		
		graph2 = new IPeakGraph(shell, SWT.NONE);
		FormData graph2_data = new FormData();
		graph2_data.top		= new FormAttachment(graph_intensity, 5);
		graph2_data.left	= new FormAttachment(tbl_peaks2);
		graph2_data.right	= new FormAttachment(100, -5);
		graph2_data.bottom	= new FormAttachment(100, -30);
		graph2.setLayoutData(graph2_data);
		
		btn_open = new Button(shell, SWT.PUSH);
		btn_open.setText("Open file");
		btn_open.addListener(SWT.Selection, this);
		
		lbl_set1 = new Label(shell, SWT.CENTER);
		FormData lbl_set1_data = new FormData();
		lbl_set1_data.left		= new FormAttachment(0);
		lbl_set1_data.right		= new FormAttachment(50);
		lbl_set1_data.bottom	= new FormAttachment(100);
		lbl_set1.setLayoutData(lbl_set1_data);
		lbl_set2 = new Label(shell, SWT.CENTER);
		FormData lbl_set2_data = new FormData();
		lbl_set2_data.left		= new FormAttachment(50);
		lbl_set2_data.right		= new FormAttachment(100);
		lbl_set2_data.bottom	= new FormAttachment(100);
		lbl_set2.setLayoutData(lbl_set2_data);
		
		// listeners
		shell.addListener(SWT.KeyDown, this);
		graph1.addListener(SWT.KeyDown, this);
		tbl_peaks1.addListener(SWT.KeyDown, this);
		graph2.addListener(SWT.KeyDown, this);
		tbl_peaks2.addListener(SWT.KeyDown, this);
		
		// fill the list
		Document document = mainwnd.getDocument();
		fillList(document.getFilteredPeaks(), tbl_peaks1, graph1);
		
		lbl_set1.setText(document.getFilename());
	}
	
	
	// Listener overrides
	@SuppressWarnings("unchecked")
	public void handleEvent(Event event)
	{
		Document document = mainwnd.getDocument();
		if (event.type == SWT.KeyDown)
		{
			if (event.keyCode!='c' && event.stateMask!=SWT.CTRL)
				return;
			
			copy();
		}
		else if (event.widget == tbl_peaks1)
		{
			if (event.detail == SWT.CHECK)
			{
				IPeak peak = document.getFilteredPeaks().get(tbl_peaks1.getSelectionIndex());
				peak.setPatternID(peak.getPatternID()==0 ? 1 : 0);
			}
			else
			{
				locateMatches();
				fillGraphs();
			}
		}
		else if (event.widget == tbl_peaks2)
		{
			fillGraphs();
		}
		else if (event.widget == btn_open)
		{
			FileDialog dlg = new FileDialog(shell, SWT.OPEN);
			String filename = dlg.open();
			if (filename == null)
				return;
			
			try {
				peakdata = PeakMLParser.parse(new FileInputStream(filename), false);
				peaks = (IPeakSet<IPeak>) peakdata.measurement;
				locateMatches();
				fillGraphs();
				lbl_set2.setText(new File(filename).getName());
			} catch (Exception e) { e.printStackTrace(); }
		}
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
	
	
	// helpers
	public void copy()
	{
		Document document = mainwnd.getDocument();
		
		IPeak peak1 = document.getFilteredPeaks().get(tbl_peaks1.getSelectionIndex());
		String str = peak1.getScanID() + "\t" + peak1.getMass() + "\t" + peak1.getIntensity();

		if (tbl_peaks2.getSelectionIndex() != -1)
		{
			IPeak peak2 = matching_peaks.get(tbl_peaks2.getSelectionIndex());
			str += "\t" + peak2.getScanID() + "\t" + peak2.getMass() + "\t" + peak2.getIntensity();
		}

		Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
		clipboard.setContents(
				new Object[] { str },
				new Transfer[] { TextTransfer.getInstance() }
			);
		clipboard.dispose();
	}
	
	protected void locateMatches()
	{
		if (peaks == null)
			return;
		
		Document document = mainwnd.getDocument();
		if (tbl_peaks1.getSelectionIndex() != -1)
		{
			final IPeak peak = document.getFilteredPeaks().get(tbl_peaks1.getSelectionIndex());
			
			double mass = peak.getMass();
			double delta = PeriodicTable.PPM(mass, 6);
			
			matching_peaks = peaks.getPeaksInMassRange(mass-delta, mass+delta);
			Collections.sort(matching_peaks, new Comparator<IPeak>() {
					public int compare(IPeak peak0, IPeak peak1)
					{
						double dist1 = Math.abs(peak.getRetentionTime() - peak0.getRetentionTime());
						double dist2 = Math.abs(peak.getRetentionTime() - peak1.getRetentionTime());
						
						return (dist1<dist2 ? -1 : (dist1>dist2 ? 1 : 0));
					}
				});
			fillList(matching_peaks, tbl_peaks2, graph2);
		}
	}
	
	protected void fillList(Vector<IPeak> peaks, Table table, IPeakGraph graph)
	{
		table.removeAll();
		for (IPeak peak : peaks)
		{
			TableItem item = new TableItem(table, SWT.NONE);
			
			item.setChecked(peak.getPatternID()!=0);
			item.setText(1, String.format("%d:%d", (int) (peak.getRetentionTime()/60), (int) (peak.getRetentionTime()%60)));
			item.setText(2, "" + peak.getMass());
			item.setText(3, "" + peak.getIntensity());
		}
		
		if (peaks.size() > 0)
			table.setSelection(0);
	}
	
	protected void fillGraphs()
	{
		Document document = mainwnd.getDocument();
		
		if (tbl_peaks2.getSelectionIndex() != -1)
		{
			IPeak peak1 = document.getFilteredPeaks().get(tbl_peaks1.getSelectionIndex());
			IPeak peak2 = matching_peaks.get(tbl_peaks2.getSelectionIndex());
			
			graph1.setPeak(peak1, document.getHeader(), document.getSetColors());
			graph2.setPeak(peak2, document.getHeader(), document.getSetColors());
			graph_intensity.clear();
			graph_intensity.setPeaks(
					new Header[] { document.getHeader(), peakdata.header },
					new IPeak[] { peak1, peak2 }
				);
		}
		else
		{
			IPeak peak1 = document.getFilteredPeaks().get(tbl_peaks1.getSelectionIndex());
			graph1.setPeak(peak1, document.getHeader(), document.getSetColors());
			graph2.clear();
			
			graph_intensity.clear();
			graph_intensity.setPeak(document.getHeader(), document.getFilteredPeaks().get(tbl_peaks1.getSelectionIndex()), "");
		}
	}
	
	
	// data
	protected Shell shell;
	protected Shell parent;
	protected MainWnd mainwnd;
	
	protected Label lbl_rt;
	protected Text txt_rt;
	protected Label lbl_ppm;
	protected Text txt_ppm;
	
	protected IPeakIntensityGraph graph_intensity;
	
	protected Label lbl_set1;
	protected IPeakGraph graph1;
	protected Table tbl_peaks1;
	protected Table tbl_peaks2;
	protected IPeakGraph graph2;
	protected Label lbl_set2;
	
	protected Button btn_open;
	
	protected IPeakSet<IPeak> peaks = null;
	protected Vector<IPeak> matching_peaks = null;
	protected ParseResult peakdata = null;
}
