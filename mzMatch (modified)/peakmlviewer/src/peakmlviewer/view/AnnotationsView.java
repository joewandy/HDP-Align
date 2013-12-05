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

// swt
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// peakml
import peakml.*;

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class AnnotationsView extends View
{
	// constructor(s)
	public AnnotationsView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.None);
		
		
		// layout
		setLayout(new FillLayout());
		
		// annotations table
		table_annotations = new Table(this, SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.FLAT);
//		table_annotations.addListener(SWT.Selection, this);
		table_annotations.setLinesVisible(true);
		table_annotations.setHeaderVisible(true);
		
		TableColumn clm_formula = new TableColumn(table_annotations, SWT.LEFT);
		clm_formula.setText("Label");
		clm_formula.setWidth(75);
		
		TableColumn clm_quality = new TableColumn(table_annotations, SWT.LEFT);
		clm_quality.setText("Value");
		clm_quality.setWidth(75);
	}
	
	
	// View overrides
	public void update(int event)
	{
		Document doc = mainwnd.getDocument();
		IPeak peak = doc.getCurrentPeak();
		
		table_annotations.removeAll();
		if (peak==null || peak.getAnnotations()==null)
			return;
		
		for (Annotation annotation : peak.getAnnotations().values())
		{
			TableItem item = new TableItem(table_annotations, SWT.NONE);
			item.setText(0, annotation.getLabel());
			item.setText(1, annotation.getValue());
		}
	}
	
	
	// data
	protected Table table_annotations;
}
