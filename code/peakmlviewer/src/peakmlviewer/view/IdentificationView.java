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

// peakml
import peakml.*;
import peakml.chemistry.*;

// peakmlviewer
import peakmlviewer.*;
import peakmlviewer.widgets.*;





/**
 * 
 */
public class IdentificationView extends View implements Listener
{
	// constructor(s)
	public IdentificationView(MainWnd mainwnd, Composite parent)
	{
		super(mainwnd, parent, SWT.None);
		
		// layout
		FormLayout layout = new FormLayout();
		setLayout(layout);
		
		// annotations table
		table_identifications = new Table(this, SWT.FULL_SELECTION|SWT.BORDER|SWT.SINGLE|SWT.V_SCROLL|SWT.FLAT);
		FormData identificationslayout = new FormData();
		identificationslayout.left		= new FormAttachment(0);
		identificationslayout.right		= new FormAttachment(75);
		identificationslayout.top		= new FormAttachment(0);
		identificationslayout.bottom	= new FormAttachment(100);
		table_identifications.setLayoutData(identificationslayout);
		table_identifications.setLinesVisible(true);
		table_identifications.setHeaderVisible(true);
		table_identifications.addListener(SWT.Selection, this);
		
		TableColumn clm_id = new TableColumn(table_identifications, SWT.LEFT);
		clm_id.setText("ID");
		clm_id.setWidth(50);
		
		TableColumn clm_formula = new TableColumn(table_identifications, SWT.LEFT);
		clm_formula.setText("Formula");
		clm_formula.setWidth(100);
		
		TableColumn clm_ppm = new TableColumn(table_identifications, SWT.LEFT);
		clm_ppm.setText("PPM");
		clm_ppm.setWidth(50);
		
		TableColumn clm_adduct = new TableColumn(table_identifications, SWT.LEFT);
		clm_adduct.setText("Adduct");
		clm_adduct.setWidth(60);
		
		TableColumn clm_name = new TableColumn(table_identifications, SWT.LEFT);
		clm_name.setText("Name");
		clm_name.setWidth(300);
		
		TableColumn clm_class = new TableColumn(table_identifications, SWT.LEFT);
		clm_class.setText("Class");
		clm_class.setWidth(75);
		
		TableColumn clm_description = new TableColumn(table_identifications, SWT.LEFT);
		clm_description.setText("Description");
		clm_description.setWidth(300);
		
		// molecule panel
		panel_molecule = new MoleculePanel(this, SWT.NONE);
		FormData moleculelayout = new FormData();
		moleculelayout.left		= new FormAttachment(table_identifications);
		moleculelayout.right	= new FormAttachment(100);
		moleculelayout.top		= new FormAttachment(0);
		moleculelayout.bottom	= new FormAttachment(100);
		panel_molecule.setLayoutData(moleculelayout);
		
		// add the key listeners
		addListener(SWT.KeyDown, this);
		panel_molecule.addListener(SWT.KeyDown, this);
		table_identifications.addListener(SWT.KeyDown, this);
	}
	
	
	// View overrides
	@Override
	public void update(int event)
	{
		ids.clear();
		molecules.clear();
		panel_molecule.setMolecule(null);
		if (event == Document.UPDATE_INDEX_CHANGED)
			removed.clear();
		table_identifications.removeAll();
		
		MainWnd mainwnd = getMainWnd();
		Document document = mainwnd.getDocument();
		
		IPeak peak = document.getCurrentPeak();
		if (peak == null)
			return;
		HashMap<String,Molecule> database = document.getDatabase();
		if (database == null)
			return;
		Annotation annotation = peak.getAnnotation(Annotation.identification);
		if (annotation == null)
			return;
		
		molecules.clear();
		
		final String[] splitIds = annotation.getValueAsString().split(", ");
		final Annotation ppmAnnotation = peak.getAnnotation("ppm");
		final String[] ppms = ppmAnnotation == null ? null : ppmAnnotation.getValueAsString().split(", ");
		final Annotation adductAnnotation = peak.getAnnotation("adduct");
		final String[] adducts = adductAnnotation == null ? null : adductAnnotation.getValueAsString().split(", ");
		
		for ( int i = 0; i < splitIds.length; ++i ) {
			final String id = splitIds[i];
			Molecule molecule = database.get(id.trim());
			if (molecule == null)
				continue;
			
			//final Annotation ppmAnnotation = peak.getAnnotation("ppm");
			double ppm;
			if ( ppmAnnotation == null ) {
				ppm = PeriodicTable.PPMNr(
					Math.max(molecule.getMass(Mass.MONOISOTOPIC), peak.getMass()),
					molecule.getMass(Mass.MONOISOTOPIC) - peak.getMass()
				);
			} else {
				ppm = Double.parseDouble(ppms[i]);
			}

			String adductname = adductAnnotation == null ? "" : adducts[i];
				
			TableItem item = new TableItem(table_identifications, SWT.NONE);
			item.setText(0, molecule.getDatabaseID());
			item.setText(1, molecule.getFormula().toString());
			item.setText(2, String.format("%.4f", ppm));
			item.setText(3, adductname);
			item.setText(4, molecule.getName());
			item.setText(5, molecule.getClassDescription()==null?"":molecule.getClassDescription());
			item.setText(6, molecule.getDescription()==null?"":molecule.getDescription());
			
			ids.add(id.trim());
			molecules.add(molecule);
		}
		
		if (table_identifications.getSelectionIndex()==-1 && ids.size()>0)
			table_identifications.setSelection(0);
		
		Event e = new Event();
		e.widget = table_identifications;
		handleEvent(e);
	}

	
	// Listener overrides
	public void handleEvent(Event e)
	{
		if (e.type == SWT.KeyDown)
		{
			MainWnd mainwnd = getMainWnd();
			Document document = mainwnd.getDocument();
			
			IPeak peak = document.getCurrentPeak();
			if (peak == null)
				return;
			
			if (e.character == SWT.DEL)
			{
				if (ids.size()==0 || table_identifications.getSelectionIndex()==-1)
					return;
				
				removed.add(ids.get(table_identifications.getSelectionIndex()));
				
				ids.remove(table_identifications.getSelectionIndex());
				molecules.remove(table_identifications.getSelectionIndex());
				
				if (ids.size() > 0)
				{
					String annotation = "";
					for (String id : ids)
						annotation = annotation + (annotation.length()==0 ? "" : ", ") + id;
					peak.getAnnotation(Annotation.identification).setValue(annotation);
				}
				else
					peak.removeAnnotation(Annotation.identification);
				
				update(Document.UPDATE_FILE_LOAD);
			}
			else if (e.keyCode=='z' && e.stateMask==SWT.CTRL)
			{
				if (!removed.isEmpty())
				{
					ids.add(removed.pop());
					String annotation = "";
					for (String id : ids)
						annotation = annotation + (annotation.length()==0 ? "" : ", ") + id;
					peak.getAnnotation(Annotation.identification).setValue(annotation);
					
					update(Document.UPDATE_FILE_LOAD);
				}
			}
			else if (e.keyCode=='c' && e.stateMask==SWT.CTRL)
			{
				int index = table_identifications.getSelectionIndex();
				if (index != -1)
				{
					Molecule molecule = molecules.get(index);
					
					double detected = peak.getMass();
					double theoretical = molecule.getMass(Mass.MONOISOTOPIC);
					
					final Annotation ppmAnnotation = peak.getAnnotation("ppm");
					double ppm;
					if ( ppmAnnotation == null ) {
						ppm = PeriodicTable.PPMNr(
							Math.max(molecule.getMass(Mass.MONOISOTOPIC), peak.getMass()),
							molecule.getMass(Mass.MONOISOTOPIC) - peak.getMass()
						);
					} else {
						ppm = ppmAnnotation.getValueAsDouble();
					}
					
					Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
					clipboard.setContents(
							new Object[] { molecule.getDatabaseID() + "\t" + molecule.getName() + "\t" + molecule.getFormula() + "\t" + detected + "\t" + theoretical + "\t" + ppm + "\t" + molecule.getDescription() },
							new Transfer[] { TextTransfer.getInstance() }
						);
					clipboard.dispose();
				}
			}
			else if (e.keyCode=='a' && e.stateMask==SWT.CTRL)
			{
				StringBuilder str = new StringBuilder();
				for (Molecule molecule : molecules)
				{
					double detected = peak.getMass();
					double theoretical = molecule.getMass(Mass.MONOISOTOPIC);
					
					final Annotation ppmAnnotation = peak.getAnnotation("ppm");
					double ppm;
					if ( ppmAnnotation == null ) {
						ppm = PeriodicTable.PPMNr(
							Math.max(molecule.getMass(Mass.MONOISOTOPIC), peak.getMass()),
							molecule.getMass(Mass.MONOISOTOPIC) - peak.getMass()
						);
					} else {
						ppm = ppmAnnotation.getValueAsDouble();
					}
					
					str.append(molecule.getDatabaseID() + "\t" + molecule.getName() + "\t" + molecule.getFormula() + "\t" + detected + "\t" + theoretical + "\t" + ppm + "\t" + molecule.getDescription() + "\n");
				}
				
				Clipboard clipboard = new Clipboard(mainwnd.getDisplay());
				clipboard.setContents(
						new Object[] { str.toString() },
						new Transfer[] { TextTransfer.getInstance() }
					);
				clipboard.dispose();
			}
		}
		else if (e.widget == table_identifications)
		{
			if (table_identifications.getSelectionIndex() == -1)
				return;
			
			Molecule molecule = molecules.get(table_identifications.getSelectionIndex());
			
			if (molecule.getInChi()!=null && molecule.getInChi().length()!=0)
				panel_molecule.setInChI(molecule.getInChi());
			else if (molecule.getSmiles()!=null && molecule.getSmiles().length()!=0)
				panel_molecule.setSmiles(molecule.getSmiles());
			else
				panel_molecule.setMolecule(null);
		}
	}
	
	
	// data
	protected Table table_identifications;
	protected MoleculePanel panel_molecule;
	
	protected Stack<String> removed = new Stack<String>();
	protected Vector<String> ids = new Vector<String>();
	protected Vector<Molecule> molecules = new Vector<Molecule>();
}
