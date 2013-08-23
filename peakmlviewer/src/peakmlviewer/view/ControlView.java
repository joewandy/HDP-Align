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
import java.util.*;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// peakml
import peakml.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.action.*;
import peakmlviewer.dialog.*;





/**
 * 
 */
public class ControlView extends View implements Listener
{
	// contructor(s)
	public ControlView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.NONE);
		
		
		// create the instances of the actions dialogs
		action_dialogs = new ActionDialog[]
		{
			new FilterMassDialog			(mainwnd, mainwnd.getShell()),
			new FilterIntensityDialog		(mainwnd, mainwnd.getShell()),
			new FilterRetentionTimeDialog	(mainwnd, mainwnd.getShell()),
			new FilterNrDetectionsDialog	(mainwnd, mainwnd.getShell()),
			new FilterAnnotationsDialog		(mainwnd, mainwnd.getShell()),
			new SortIPeakDialog				(mainwnd, mainwnd.getShell()),
			new SortTimeSeriesDialog		(mainwnd, mainwnd.getShell()),
		};
		
		// set the layout
		GridLayout layout = new GridLayout(3, false);
		setLayout(layout);
		
		// create the widgets
		GridData layout_tbl_peaks = new GridData(GridData.FILL_BOTH);
		layout_tbl_peaks.horizontalSpan = 3;
		tbl_peaks = new Table(this, SWT.CHECK|SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.FLAT);
		tbl_peaks.setLinesVisible(true);
		tbl_peaks.setHeaderVisible(true);
		tbl_peaks.addListener(SWT.Selection, this);
		tbl_peaks.setLayoutData(layout_tbl_peaks);
		
		clm_peaks_id = new TableColumn(tbl_peaks, SWT.LEFT);
		clm_peaks_id.setText("");
		clm_peaks_id.setWidth(20);
		clm_peaks_id.addListener(SWT.Selection, this);
		
		clm_peaks_rt = new TableColumn(tbl_peaks, SWT.LEFT);
		clm_peaks_rt.setText("RT");
		clm_peaks_rt.setWidth(50);
		clm_peaks_rt.addListener(SWT.Selection, this);
		
		clm_peaks_mass = new TableColumn(tbl_peaks, SWT.LEFT);
		clm_peaks_mass.setText("Mass");
		clm_peaks_mass.setWidth(75);
		clm_peaks_mass.addListener(SWT.Selection, this);
		
		clm_peaks_intensity = new TableColumn(tbl_peaks, SWT.LEFT);
		clm_peaks_intensity.setText("Intensity");
		clm_peaks_intensity.setWidth(75);
		clm_peaks_intensity.addListener(SWT.Selection, this);
		
		clm_peaks_nosamples = new TableColumn(tbl_peaks, SWT.LEFT);
		clm_peaks_nosamples.setText("# samples");
		clm_peaks_nosamples.setWidth(75);
		clm_peaks_nosamples.addListener(SWT.Selection, this);
		
		GridData layout_tbl_actions = new GridData(GridData.FILL_BOTH);
		layout_tbl_actions.horizontalSpan = 3;
		tbl_actions = new Table(this, SWT.FULL_SELECTION|SWT.BORDER|SWT.MULTI|SWT.V_SCROLL|SWT.FLAT);
		tbl_actions.setLinesVisible(true);
		tbl_actions.setHeaderVisible(true);
		tbl_actions.addListener(SWT.Selection, this);
		tbl_actions.setLayoutData(layout_tbl_actions);
		
		TableColumn clm_actions_type = new TableColumn(tbl_actions, SWT.LEFT);
		clm_actions_type.setText("Type");
		clm_actions_type.setWidth(75);
		
		TableColumn clm_actions_settings = new TableColumn(tbl_actions, SWT.LEFT);
		clm_actions_settings.setText("Settings");
		clm_actions_settings.setWidth(150);
		
		cmb_action = new Combo(this, SWT.None);
		for (ActionDialog dlg : action_dialogs)
			cmb_action.add(dlg.getName());
		cmb_action.select(0);
		
		btn_add = new Button(this, SWT.Selection);
		btn_add.setText("Add");
		btn_add.addListener(SWT.Selection, this);
		
		btn_remove = new Button(this, SWT.Selection);
		btn_remove.setText("Remove");
		btn_remove.addListener(SWT.Selection, this);
		
		// add the key-listener
		addListener(SWT.KeyDown, this);
		tbl_peaks.addListener(SWT.KeyDown, this);
		tbl_actions.addListener(SWT.KeyDown, this);
		
		pack();
	}
	
	
	// View overrides
	@Override
	public void update(int event)
	{
		Document document = mainwnd.getDocument();
		
		if (event==Document.UPDATE_INIT || event==Document.UPDATE_FILE_LOAD || event==Document.UPDATE_ACTIONS)
		{
			// fill the action table
			tbl_actions.removeAll();
			for (Action action : document.getActions())
			{
				TableItem item = new TableItem(tbl_actions, SWT.NONE);
				item.setText(0, action.getName());
				item.setText(1, action.getDescription());
			}
			
			// fill the peaks list
			tbl_peaks.removeAll();
			for (IPeak peak : document.getFilteredPeaks())
			{
				TableItem item = new TableItem(tbl_peaks, SWT.NONE);
				if (peak.getAnnotation("identification") == null)
					item.setForeground(new Color(getDisplay(), 127,127,127));
				
				item.setChecked(peak.getPatternID()!=0);
				item.setText(1, String.format("%02d:%02d", (int) (peak.getRetentionTime()/60), (int) (peak.getRetentionTime()%60)));
				item.setText(2, "" + peak.getMass());
				item.setText(3, "" + peak.getIntensity());
				item.setText(4, "" + Document.getNrSamples(peak));
			}
			tbl_peaks.setSelection(document.getIndex());
		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event event)
	{
		Document document = mainwnd.getDocument();
		
		if (event.type == SWT.KeyDown)
		{
			int index = tbl_peaks.getSelectionIndex();
			if (index == -1)
				return;
			if (event.keyCode!='c' && event.stateMask!=SWT.CTRL)
				return;
			
			copy();
		}
		else if (event.widget == btn_add)
		{
			ActionDialog dlg = null;
			String selected_action = cmb_action.getText();
			for (ActionDialog a : action_dialogs)
				if (selected_action.equals(a.getName()))
				{
					dlg = a;
					break;
				}
			
			if (dlg == null)
			{
				MessageBox msg = new MessageBox(mainwnd.getShell(), SWT.OK);
				msg.setText("Error");
				msg.setMessage("Unavailable action '" + selected_action + "'.");
				msg.open();
			}
			else
			{
				Action action = dlg.open();
				document.addAction(action);
				mainwnd.update(Document.UPDATE_ACTIONS);
			}
		}
		else if (event.widget == btn_remove)
		{
			Vector<Action> actions = new Vector<Action>();
			for (int index : tbl_actions.getSelectionIndices())
				actions.add(document.getAction(index));
			document.removeActions(actions);
			mainwnd.update(Document.UPDATE_ACTIONS);
		}
		else if (event.widget == tbl_peaks)
		{
			if (tbl_peaks.getSelectionIndex() != document.getIndex())
			{
				document.setIndex(tbl_peaks.getSelectionIndex());
				mainwnd.update(Document.UPDATE_INDEX_CHANGED);
			}
			if (event.detail == SWT.CHECK)
			{
				IPeak peak = document.getFilteredPeaks().get(
						tbl_peaks.indexOf((TableItem) event.item)
					);
				peak.setPatternID(peak.getPatternID()==0 ? 1 : 0);
			}
		}
		else if (event.widget == clm_peaks_id)
		{
			document.setIPeakSort(IPeak.sort_patternid_ascending);
			// TODO find out where the current peak ends up and update the index
			mainwnd.update(Document.UPDATE_ACTIONS);
		}
		else if (event.widget == clm_peaks_rt)
		{
			document.setIPeakSort(IPeak.sort_scanid_ascending);
			// TODO find out where the current peak ends up and update the index
			mainwnd.update(Document.UPDATE_ACTIONS);
		}
		else if (event.widget == clm_peaks_mass)
		{
			document.setIPeakSort(IPeak.sort_mass_ascending);
			// TODO find out where the current peak ends up and update the index
			mainwnd.update(Document.UPDATE_ACTIONS);
		}
		else if (event.widget == clm_peaks_intensity)
		{
			document.setIPeakSort(IPeak.sort_intensity_ascending);
			// TODO find out where the current peak ends up and update the index
			mainwnd.update(Document.UPDATE_ACTIONS);
		}
	}
	
	
	// implementations for actions
	public void copy()
	{
		Document document = mainwnd.getDocument();
		IPeak ipeak = document.getCurrentPeak();

		Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
		clipboard.setContents(
				new Object[] { ipeak.getScanID() + "\t" + ipeak.getMass() + "\t" + ipeak.getIntensity() },
				new Transfer[] { TextTransfer.getInstance() }
			);
		clipboard.dispose();
	}
	
	public void copyAll()
	{
		Document document = mainwnd.getDocument();
		
		String object = "";
		for (IPeak ipeak : document.getFilteredPeaks())
			object += ipeak.getScanID() + "\t" + ipeak.getMass() + "\t" + ipeak.getIntensity() + "\n";
		
		Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
		clipboard.setContents(
				new Object[] { object.substring(0, object.length()-1) },
				new Transfer[] { TextTransfer.getInstance() }
			);
		clipboard.dispose();
	}
	
	public void copySelected()
	{
		Document document = mainwnd.getDocument();
		
		String object = "";
		for (IPeak ipeak : document.getFilteredPeaks())
			if (ipeak.getPatternID() == 1)
				object += ipeak.getScanID() + "\t" + ipeak.getMass() + "\t" + ipeak.getIntensity() + "\n";
		
		Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
		clipboard.setContents(
				new Object[] { object.substring(0, object.length()-1) },
				new Transfer[] { TextTransfer.getInstance() }
			);
		clipboard.dispose();
	}
	
	
	// data
	protected Table tbl_peaks;
	protected TableColumn clm_peaks_id;
	protected TableColumn clm_peaks_rt;
	protected TableColumn clm_peaks_mass;
	protected TableColumn clm_peaks_intensity;
	protected TableColumn clm_peaks_nosamples;
	
	protected Button btn_add;
	protected Button btn_remove;
	protected Combo cmb_action;
	protected Table tbl_actions;
	
	protected ActionDialog action_dialogs[];
}
