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



package peakmlviewer.dialog.peakinformation;


// java
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;

import org.jfree.chart.renderer.category.*;

import org.jfree.data.category.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

// peakmlviewer
import peakmlviewer.*;





/**
 * 
 */
public class Graph extends Composite
{
	// constructor(s)
	public Graph(Composite parent)
	{
		super(parent, SWT.EMBEDDED);
		
		setLayout(new FillLayout());
		
		
		// create the chart
		linechart = ChartFactory.createLineChart(
				null, "", "Abundance", dataset_intensity, PlotOrientation.VERTICAL,
				false, // legend
				false, // tooltips
				false  // urls
			);
		
		CategoryPlot plot = (CategoryPlot) linechart.getPlot();
		
		// make the labels for the xaxis 45 degrees
		CategoryAxis xaxis = (CategoryAxis) plot.getDomainAxis();
		xaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		
		// add the mass accuracy yaxis
		NumberAxis yaxis_massacc = new NumberAxis("Mass accuracy (ppm)");
		plot.setRangeAxis(1, yaxis_massacc);
		plot.setRangeAxisLocation(1, AxisLocation.BOTTOM_OR_RIGHT);
		
		// create the mass accuracy dataset
		dataset_ppm = new DefaultCategoryDataset();
		plot.setDataset(1, dataset_ppm);
		plot.mapDatasetToRangeAxis(1, 1);
		
		// create the renderer for the mass accuracy dataset
		LineAndShapeRenderer renderer_ppm = new LineAndShapeRenderer();
		renderer_ppm.setBaseShapesFilled(true);
		renderer_ppm.setBaseShapesVisible(true);
		renderer_ppm.setBaseStroke(new BasicStroke(
				1, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_ROUND, 1,
				new float[] { 5, 5 }, 0
			));
		plot.setRenderer(1, renderer_ppm);
		
		// setup the renderer for the intensity dataset
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		renderer.setBaseShapesFilled(true);
		renderer.setBaseShapesVisible(true);
		
		// general properties
		linechart.setBackgroundPaint(Color.WHITE);
		linechart.setBorderVisible(false);
		linechart.setAntiAlias(true);
		
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		
		// add the components
		// --------------------------------------------------------------------------------
		// This uses the SWT-trick for embedding awt-controls in an SWT-Composit.
		try { 
			System.setProperty("sun.awt.noerasebackground", "true"); 
		} catch (NoSuchMethodError error) { ; } 
			
		java.awt.Frame frame = org.eclipse.swt.awt.SWT_AWT.new_Frame(this);

		// create a new ChartPanel, without the popup-menu (5x false)
		frame.add(new ChartPanel(linechart, false, false, false, false, false));
		// --------------------------------------------------------------------------------
	}
	
	
	// access
	@SuppressWarnings("unchecked")
	public void setPeak(IPeak peak, Document document, MolecularFormula formula)
	{
		if (peak == null)
			return;
		
		for (String setname : document.getSetNames())
		{
			dataset_intensity.addValue(0, "INTENSITY", setname);
			dataset_ppm.addValue(0, "PPM", setname);
		}
		
		// fill the graph with the appropriate data
		Class<? extends IPeak> c = peak.getClass();
		if (c.equals(IPeakSet.class))
		{
			IPeakSet<? extends IPeak> peakset = (IPeakSet<? extends IPeak>) peak;
			double mass = peakset.getMeanMass();
			if (formula != null)
				mass = formula.getMass(Mass.MONOISOTOPIC);
			
			java.util.Collections.sort(peakset.getPeaks(), IPeak.sort_profileid_ascending);
			for (IPeak p : peakset)
			{
				dataset_ppm.addValue(
						PeriodicTable.PPMNr(mass, mass-p.getMass()),
						"PPM", document.getSetName(p)
					);
				dataset_intensity.addValue(
						p.getIntensity(),
						"INTENSITY", document.getSetName(p)
					);
			}
		}
		else
		{
			dataset_ppm.addValue(0, "", document.getSetName(peak));
			dataset_intensity.addValue(peak.getIntensity(), "", document.getSetName(peak));
		}
	}
	
	public BufferedImage getGraphImage(int width, int height)
	{
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		linechart.draw(g, new Rectangle(0, 0, width, height));
		
		return img;
	}
	
	
	// data
	protected JFreeChart linechart = null;
	
	protected DefaultCategoryDataset dataset_ppm = new DefaultCategoryDataset();
	protected DefaultCategoryDataset dataset_intensity = new DefaultCategoryDataset();
}
