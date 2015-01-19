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
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.action.*;





/**
 * 
 */
public class FilterAnnotationsDialog extends ActionDialog implements Listener
{
	// constructor(s)
	public FilterAnnotationsDialog(MainWnd mainwnd, Shell parent)
	{
		super(mainwnd, parent, "Filter annotations");
	}
	
	@Override
	public void init()
	{
		super.init();
		
		
		// create a list of relations
		final String relations[] = new String[] {
				"=", ">", "<", "like"
			};
		
		// initialize the action to null
		action = null;
		
		// create the layout
		shell.setLayout(new GridLayout(3, false));
		
		// create the widgets for the rt range
		txt_name = new Text(shell, SWT.SINGLE|SWT.BORDER);
		
		cmb_relation = new Combo(shell, SWT.None);
		for (String relation : relations)
			cmb_relation.add(relation);
		cmb_relation.select(0);
		
		txt_value = new Text(shell, SWT.SINGLE|SWT.BORDER);
		
		// create the widgets for the dialog
		btn_ok = new Button(shell, SWT.Selection);
		btn_ok.setText("Ok");
		btn_ok.addListener(SWT.Selection, this);
		
		btn_cancel = new Button(shell, SWT.Selection);
		btn_cancel.setText("Cancel");
		btn_cancel.addListener(SWT.Selection, this);
		
		//
		shell.pack();
	}
	
	
	// Listeners overrides
	public void handleEvent(Event event)
	{
		if (event.widget == btn_ok)
		{
			try
			{
				
			}
			catch (Exception e)
			{
				MessageBox msg = new MessageBox(shell, SWT.OK);
				msg.setText("Error");
				msg.setMessage("Invalid number.");
				msg.open();
				return;
			}
			
			FilterAnnotations.Relation relation = FilterAnnotations.Relation.EQUALS;
			if (cmb_relation.getText().equals(">"))
				relation = FilterAnnotations.Relation.GREATER_THEN;
			else if (cmb_relation.getText().equals("<"))
				relation = FilterAnnotations.Relation.SMALLER_THEN;
			else if (cmb_relation.getText().equals("like"))
				relation = FilterAnnotations.Relation.LIKE;
			
			action = new FilterAnnotations(txt_name.getText(), relation, txt_value.getText());
			shell.dispose();
		}
		else if (event.widget == btn_cancel)
		{
			shell.dispose();
		}
	}
	
	
	// ActionDialog overrides
	@Override
	protected Action getAction()
	{
		return action;
	}

	@Override
	public String getName()
	{
		return "Filter annotations";
	}
	
	
	// data
	protected FilterAnnotations action;
	
	protected Text txt_name;
	protected Combo cmb_relation;
	protected Text txt_value;
	
	protected Button btn_ok;
	protected Button btn_cancel;
}
