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

// swt
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// ThermoLogViewer
import thermologviewer.*;





/**
 * 
 */
public class ControlView extends View implements Listener
{
	// constructor(s)
	public ControlView(MainWnd mainwnd)
	{
		super(mainwnd, mainwnd.getShell(), SWT.None);
		
		// layout
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		setLayout(layout);
		
		// add the widgets
		btn_statuslog = new Button(this, SWT.CHECK);
		btn_statuslog.setText("Status log: ");
		btn_statuslog.setSelection(true);
		btn_statuslog.addListener(SWT.Selection, this);
		
		GridData layout_statuslog = new GridData(GridData.FILL_HORIZONTAL);
		cmb_statuslog = new Combo(this, SWT.None);
		cmb_statuslog.addListener(SWT.Selection, this);
		cmb_statuslog.setLayoutData(layout_statuslog);
		cmb_statuslog.setEnabled(true);
		
		btn_trailerextra = new Button(this, SWT.CHECK);
		btn_trailerextra.setText("Trailer extra: ");
		btn_trailerextra.setSelection(false);
		btn_trailerextra.addListener(SWT.Selection, this);
		
		GridData layout_trailerextra = new GridData(GridData.FILL_HORIZONTAL);
		cmb_trailerextra = new Combo(this, SWT.None);
		cmb_trailerextra.addListener(SWT.Selection, this);
		cmb_trailerextra.setLayoutData(layout_trailerextra);
		cmb_trailerextra.setEnabled(false);
	}
	
	
	// View overrides
	public void update(int event)
	{
		Document document = mainwnd.getDocument();
		if (event == Document.UPDATE_ADD_FILE)
		{
			cmb_statuslog.removeAll();
			for (String label : document.getStatusLogLabels())
				cmb_statuslog.add(label);
		}
	}
	
	
	// Listener overrides
	public void handleEvent(Event e)
	{
		Document document = mainwnd.getDocument();
		if (e.widget == cmb_statuslog)
		{
			int index = cmb_statuslog.getSelectionIndex();
			if (index != -1)
			{
				document.setStatusLogLabel(document.getStatusLogLabels().get(index));
				mainwnd.update(Document.UPDATE_LABEL_CHANGE);
			}
		}
//		else if (e.widget == cmb_trailerextra)
//		{
//			int index = cmb_trailerextra.getSelectionIndex();
//			if (index != -1)
//			{
//				document.setTrailerExtraLabel(document.getTrailerExtraLabels().get(index));
//				mainwnd.update(Document.UPDATE_LABEL_CHANGE);
//			}
//		}
		else if (e.widget==btn_statuslog || e.widget==btn_trailerextra)
		{
			if (e.widget==btn_statuslog && document.getActiveLog()==Document.LOG_STATUS)
			{
				btn_statuslog.setSelection(true);
				return;
			}
			if (e.widget==btn_trailerextra && document.getActiveLog()==Document.LOG_TRAILER_EXTRA)
			{
				btn_trailerextra.setSelection(true);
				return;
			}
			
			cmb_statuslog.setEnabled(e.widget==btn_statuslog);
			btn_statuslog.setSelection(e.widget==btn_statuslog);
			cmb_trailerextra.setEnabled(e.widget==btn_trailerextra);
			btn_trailerextra.setSelection(e.widget==btn_trailerextra);
			
			document.setActiveLog(e.widget==btn_statuslog ? Document.LOG_STATUS : Document.LOG_TRAILER_EXTRA);
			mainwnd.update(Document.UPDATE_ADD_FILE);
		}
	}
	
	
	// data
	protected Combo cmb_statuslog;
	protected Button btn_statuslog;
	protected Combo cmb_trailerextra;
	protected Button btn_trailerextra;
}
