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



package peakmlviewer.dialog.timeseries;


// java
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Color;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

// jfreechart
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.entity.*;

import org.jfree.chart.renderer.category.*;

import org.jfree.data.category.*;

// peakml

// peakmlviewer
import peakmlviewer.action.*;





/**
 * 
 */
public class EditableChart extends Composite implements MouseListener, MouseMotionListener
{
	// constructor(s)
	public EditableChart(Composite parent, int style)
	{
		super(parent, SWT.EMBEDDED|style);
		
		setLayout(new FillLayout());
		
		// create the components
		linechart = ChartFactory.createLineChart(
				null, "", "Intensity", dataset, PlotOrientation.VERTICAL,
				false, // legend
				false, // tooltips
				false  // urls
			);
		
		CategoryPlot plot = (CategoryPlot) linechart.getPlot();
		ValueAxis yaxis = plot.getRangeAxis();
		yaxis.setRange(0, 1);
		CategoryAxis xaxis = (CategoryAxis) plot.getDomainAxis();
		xaxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
		
		renderer.setSeriesShapesFilled(0, true);
		renderer.setSeriesShapesVisible(0, true);
		
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

		// create a few ChartPanel, without the popup-menu (5x false)
		panel = new ChartPanel(linechart, false, false, false, false, false);
		
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		panel.setRangeZoomable(false);
		panel.setDomainZoomable(false);
		
		frame.add(panel);
		// --------------------------------------------------------------------------------
	}
	
	
	// access
	public void addValue(String x, double y)
	{
		dataset.addValue(y, "", x);
	}
	
	public double getValue(String x)
	{
		return (Double) dataset.getValue("", x);
	}
	
	public BufferedImage getGraphImage(int width, int height)
	{
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		linechart.draw(g, new Rectangle(0, 0, width, height));
		
		return img;
	}
	
	
	// MouseMotionListener
	public void mouseDragged(MouseEvent event)
	{
		if (entity != null)
		{
			// calculate the new value
			Point curpoint = event.getPoint();
			Rectangle2D plotrect = panel.getChartRenderingInfo().getPlotInfo().getPlotArea();
			
			double value = (Double) dataset.getValue(entity.getRowKey(), entity.getColumnKey());
			double newvalue = value - (curpoint.y-prevpoint.y) / plotrect.getHeight();
			
			if (newvalue<0)
				newvalue = 0;
			else if (newvalue>1)
				newvalue = 1;
			
			dataset.setValue(newvalue, entity.getRowKey(), entity.getColumnKey());
			
			// set the prevpos to the curpos
			prevpoint = curpoint;
		}
	}
	
	public void mousePressed(MouseEvent event)
	{
		Point p = event.getPoint();
		entity = (CategoryItemEntity) panel.getEntityForPoint(p.x, p.y);
		if (entity != null)
			prevpoint = p;
	}
	
	public void mouseReleased(MouseEvent event)
	{
		entity = null;
		prevpoint = null;
	}

	public void mouseMoved(MouseEvent event)		{;}
	public void mouseExited(MouseEvent event)		{;}
	public void mouseClicked(MouseEvent event)		{;}
	public void mouseEntered(MouseEvent event)		{;}
	
	
	// data
	/** */
	protected SortIPeak action;
	
	/** */
	protected ChartPanel panel;
	/** */
	protected JFreeChart linechart;
	/** */
	protected DefaultCategoryDataset dataset = new DefaultCategoryDataset();
	
	/** */
	protected Point prevpoint = null;
	/** */
	protected CategoryItemEntity entity = null;
}
