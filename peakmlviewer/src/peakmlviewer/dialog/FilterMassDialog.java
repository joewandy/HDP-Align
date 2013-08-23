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
import peakml.chemistry.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.action.*;





/**
 * 
 */
public class FilterMassDialog extends ActionDialog implements Listener
{
	// constructor(s)
	public FilterMassDialog(MainWnd mainwnd, Shell parent)
	{
		super(mainwnd, parent, "Filter mass");
	}
	
	@Override
	public void init()
	{
		super.init();
		
		// initialize the action to null
		action = null;
		
		// create the layout
		shell.setLayout(new GridLayout(4, false));
		
		// create the widgets for the mass range
		tgl_massrange = new Button(shell, SWT.RADIO);
		tgl_massrange.addListener(SWT.Selection, this);
		tgl_massrange.setText("Mass range");
		tgl_massrange.setSelection(true);
		
		txt_massrange_min = new Text(shell, SWT.SINGLE|SWT.BORDER);
		txt_massrange_max = new Text(shell, SWT.SINGLE|SWT.BORDER);
		
		new Label(shell, SWT.NONE);
		
		// create the widgets for the formula
		tgl_formula = new Button(shell, SWT.RADIO);
		tgl_formula.addListener(SWT.Selection, this);
		tgl_formula.setText("Formula");
		
		txt_formula = new Text(shell, SWT.SINGLE|SWT.BORDER);
		txt_formula.setEnabled(false);
		txt_formula_ppm = new Text(shell, SWT.SINGLE|SWT.BORDER);
		txt_formula_ppm.setEnabled(false);
		txt_charge = new Text(shell, SWT.SINGLE|SWT.BORDER);
		txt_charge.setText("1");
		txt_charge.setEnabled(false);
		
			
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
			if (tgl_massrange.getSelection())
			{
				double minmass = 0;
				double maxmass = 0;
				
				try
				{
					minmass = Double.parseDouble(txt_massrange_min.getText());
					maxmass = Double.parseDouble(txt_massrange_max.getText());
				}
				catch (Exception e)
				{
					MessageBox msg = new MessageBox(shell, SWT.OK);
					msg.setText("Error");
					msg.setMessage("Invalid number.");
					msg.open();
					return;
				}
				
				if (minmass >= maxmass)
				{
					MessageBox msg = new MessageBox(shell, SWT.OK);
					msg.setText("Error");
					msg.setMessage("The minimum mass is larger than the maximum mass.");
					msg.open();
					return;
				}
				
				action = new FilterMass(minmass, maxmass);
			}
			else if (tgl_formula.getSelection())
			{
				double ppm = 0;
				int charge = 1;
				MolecularFormula formula = null;
				
				try
				{
					formula = new MolecularFormula(txt_formula.getText());
				}
				catch (Exception e)
				{
					MessageBox msg = new MessageBox(shell, SWT.OK);
					msg.setText("Error");
					msg.setMessage("Chemical formula is incorrect.");
					msg.open();
					return;
				}
				try
				{
					ppm = Double.parseDouble(txt_formula_ppm.getText());
				}
				catch (Exception e)
				{
					MessageBox msg = new MessageBox(shell, SWT.OK);
					msg.setText("Error");
					msg.setMessage("Incorrect number for ppm.");
					msg.open();
					return;
				}
				try
				{
					charge = Integer.parseInt(txt_charge.getText());
				}
				catch (Exception e)
				{
					MessageBox msg = new MessageBox(shell, SWT.OK);
					msg.setText("Error");
					msg.setMessage("Incorrect number for charge.");
					msg.open();
					return;
				}
				
				action = new FilterMass(formula, ppm, charge);
			}
			
			shell.dispose();
		}
		else if (event.widget == btn_cancel)
		{
			shell.dispose();
		}
		else if (event.widget==tgl_massrange || event.widget==tgl_formula)
		{
			txt_massrange_min.setEnabled(event.widget==tgl_massrange);
			txt_massrange_max.setEnabled(event.widget==tgl_massrange);
			
			txt_formula.setEnabled(event.widget==tgl_formula);
			txt_formula_ppm.setEnabled(event.widget==tgl_formula);
			txt_charge.setEnabled(event.widget==tgl_formula);
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
		return "Filter Mass";
	}
	
	
	// data
	protected FilterMass action;
	
	protected Text txt_massrange_min;
	protected Text txt_massrange_max;
	protected Button tgl_massrange;
	
	protected Text txt_formula;
	protected Text txt_formula_ppm;
	protected Text txt_charge;
	protected Button tgl_formula;
	
	protected Button btn_ok;
	protected Button btn_cancel;
}
