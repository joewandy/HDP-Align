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



package peakmlviewer.widgets;


// java

// swing
import javax.swing.*;

// eclipse
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

// chemical development kit
import org.openscience.cdk.*;
import org.openscience.cdk.layout.*;
import org.openscience.cdk.smiles.SmilesParser;
import org.openscience.cdk.geometry.*;
import org.openscience.cdk.interfaces.*;

import org.openscience.cdk.inchi.*;
import org.openscience.cdk.renderer.*;




/**
 * 
 */
public class MoleculePanel extends Composite
{
	// constructor(s)
	/**
	 * 
	 */
	public MoleculePanel(Composite parent, int style)
	{
		super(parent, style|SWT.EMBEDDED);
		
		// setup the Composite
		// --------------------------------------------------------------------------------
		// This uses the SWT-trick for embedding awt-controls in an SWT-Composit. The Panel
		// is added because the from JDK1.5 the frame does not receive mouse-events and the
		// panel does.
		try { 
			System.setProperty("sun.awt.noerasebackground", "true");
		} catch (NoSuchMethodError error) { error.printStackTrace(); } 
			
		java.awt.Frame frame = org.eclipse.swt.awt.SWT_AWT.new_Frame(this);
		
		panel = new MoleculeJPanel();
		frame.add(panel);
		// --------------------------------------------------------------------------------
	}
	
	
	// access
	/**
	 * 
	 */
	public void setMolecule(IMolecule m)
	{
		panel.setMolecule(m);
	}
	
	/**
	 * 
	 */
	public void setSmiles(String smiles)
	{
		try {
			setMolecule(smilesparser.parseSmiles(smiles));
		} catch (Exception e) { setMolecule(null); }
	}
	
	/**
	 * 
	 */
	public void setInChI(String inchi)
	{
		try {
			InChIGeneratorFactory factory = new InChIGeneratorFactory();
			InChIToStructure intostruct = factory.getInChIToStructure(inchi, DefaultChemObjectBuilder.getInstance());

			IMolecule m = new Molecule(intostruct.getAtomContainer());
			setMolecule(m);
		} catch (Exception e) { setMolecule(null); }
	}
	
	
	// The swing implementation of the panel
	private class MoleculeJPanel extends JPanel
	{
		// constructor(s)
		public MoleculeJPanel()
		{
			// setup the renderer
			renderer_model = new Renderer2DModel();
			renderer = new Java2DRenderer(renderer_model);
			setBackground(renderer_model.getBackColor());
			
			try
			{
				// TODO maybe we need to tweak this a bit ?
				renderer_model.setDrawNumbers(false);
				renderer_model.setUseAntiAliasing(true);
				renderer_model.setColorAtomsByType(false);
				renderer_model.setShowAromaticity(true);
				
				renderer_model.setShowEndCarbons(true);
				renderer_model.setShowImplicitHydrogens(false);
			} 
			catch(Exception exc)
			{
				exc.printStackTrace();		
			}
		}
		
		
		// JPanel overrides
		public synchronized void paint(java.awt.Graphics g)
		{
			java.awt.Dimension size = getSize();
			g.clearRect(0, 0, size.width, size.height);
			if (molecule != null)
			{
				try
				{
					renderer_model.setBackgroundDimension(this.getSize());
					
					StructureDiagramGenerator sdg = new StructureDiagramGenerator();
					sdg.setMolecule(molecule);
					sdg.generateCoordinates();
					IAtomContainer current_container = sdg.getMolecule();
					
					GeometryTools.translateAllPositive(current_container);
					GeometryTools.scaleMolecule(current_container, size, 0.8);			
					GeometryTools.center(current_container, size);
					
					renderer.paintMolecule(current_container, (java.awt.Graphics2D) g, new java.awt.Rectangle(size.width,size.height));
				}
				catch (Exception e)
				{
					//e.printStackTrace();
					molecule = null;
				}
			}
			
			if (molecule == null)
			{
				java.awt.Graphics2D gc = (java.awt.Graphics2D) g;
				
				gc.setStroke(new java.awt.BasicStroke(5));
				gc.setColor(new java.awt.Color(230, 230, 230));
				
				gc.drawLine(0, 0, size.width, size.height);
				gc.drawLine(0, size.height, size.width, 0);
			}
		}
		
		
		// access
		public synchronized void setMolecule(IMolecule m)
		{
			molecule = m;
			repaint();
		}
		
		
		// data
		protected IMolecule molecule = null;
		protected Java2DRenderer renderer = null;
		protected Renderer2DModel renderer_model = null;
		
		private static final long serialVersionUID = 1L;
	}
	
	
	// data
	/** Implementation of the real JPanel */
	protected MoleculeJPanel panel = null;
	/** */
	protected SmilesParser smilesparser = new SmilesParser(DefaultChemObjectBuilder.getInstance());
}

