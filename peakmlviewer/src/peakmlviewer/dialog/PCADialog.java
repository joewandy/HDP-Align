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
import java.awt.Color;
import java.awt.RenderingHints;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// javastat
//import javastat.multivariate.*;

//jfreechart
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.labels.*;

import org.jfree.data.xy.*;

import org.jfree.chart.renderer.xy.*;

// peakml
//import peakml.*;
//import peakml.math.*;

// PeakML viewer
import peakmlviewer.*;





/**
 * 
 */
public class PCADialog extends Dialog
{
	// constructor(s)
	public PCADialog(MainWnd mainwnd, Shell parent, String title)
	{
		super(parent, SWT.NONE);
		
		// save the parent pointer
		this.title = title;
		this.parent = parent;
		
		this.mainwnd = mainwnd;
		
		// create the window and set its properties
		shell = new Shell(parent, SWT.EMBEDDED|SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setSize(500, 300);
		shell.setText(title);
		
		// create the jfreechart
		plot = new XYPlot(
				collection,
				new NumberAxis("principal component 1"),
				new NumberAxis("principal component 2"),
				new StandardXYItemRenderer(StandardXYItemRenderer.SHAPES)
			);
		plot.setBackgroundPaint(Color.WHITE);
		plot.setDomainGridlinesVisible(true);
		plot.setRangeGridlinesVisible(true);
		plot.getRenderer().setBaseItemLabelsVisible(true);
		plot.getRenderer().setBaseItemLabelGenerator(new XYItemLabelGenerator() {
			public String generateLabel(XYDataset dataset, int series, int item)
			{
				return labels[item];
			}
		});
		
		chart = new JFreeChart("Principle Component Analysis", plot);
		chart.removeLegend();
		chart.setBackgroundPaint(Color.WHITE);
		chart.getRenderingHints().put(
				RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON
			);
		
		
		// add the components
		// --------------------------------------------------------------------------------
		// This uses the SWT-trick for embedding awt-controls in an SWT-Composit.
		try { 
			System.setProperty("sun.awt.noerasebackground", "true"); 
		} catch (NoSuchMethodError error) { ; } 
			
		java.awt.Frame frame = org.eclipse.swt.awt.SWT_AWT.new_Frame(shell);
		
		// create a new ChartPanel, without the popup-menu (5x false)
		frame.add(new ChartPanel(chart, false, false, false, false, false));
		// --------------------------------------------------------------------------------
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
		
		// do the analysis
		analyze();
		
		// open the window
		shell.open();
		
		// enter the event-loop
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
	}
	
	
	// 
	public void analyze()
	{
		Document document = mainwnd.getDocument();
		
		labels = new String[document.getNrTimePoints()];
		for (int index=0; index<document.getNrTimePoints(); ++index)
			labels[index] = document.getSetNames().get(index);
		
		// TODO check whether this makes sense
//		double data[][] = new double[document.getNrTimePoints()][document.getNrPeaks()];
//		for (int index=0; index<document.getNrPeaks(); ++index)
//		{
//			IPeakSet<IPeak> peakset = (IPeakSet) document.getFilteredPeaks().get(index);
//			for (IPeak peak : peakset)
//				data[ExperimentalDesign.idToSetID(peak.getSpectrumID())][index] = peak.getIntensity();
//		}
//		
//		PCA testclass1 = new PCA(0.95, "covariance", data);
//		principal_components = testclass1.principalComponents;
//		
//		XYSeries series = new XYSeries("PCA");
//		collection.addSeries(series);
//		for (int i=0; i<principal_components[0].length; ++i)
//			series.add(principal_components[0][i], principal_components[1][i]);
//		plot.setDataset(collection);
//		
//		double stats_range[] = Statistical.stats(principal_components[1]);
//		double stats_domain[] = Statistical.stats(principal_components[0]);
//		plot.getRangeAxis().setRange(
//				stats_range[Statistical.MINIMUM] - Math.abs(0.3*stats_range[Statistical.MINIMUM]),
//				stats_range[Statistical.MAXIMUM] + Math.abs(0.3*stats_range[Statistical.MAXIMUM])
//			);
//		plot.getDomainAxis().setRange(
//				stats_domain[Statistical.MINIMUM] - Math.abs(0.3*stats_domain[Statistical.MINIMUM]),
//				stats_domain[Statistical.MAXIMUM] + Math.abs(0.3*stats_domain[Statistical.MAXIMUM])
//			);
	}
	
	
	// data
	protected Shell shell;
	protected Shell parent;
	
	protected String title;
	protected MainWnd mainwnd;

	protected XYPlot plot;
	protected JFreeChart chart;
	protected XYSeriesCollection collection = new XYSeriesCollection();
	
	protected String labels[];
	protected double principal_components[][];
}
