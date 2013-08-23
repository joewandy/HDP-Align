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



package peakmlviewer.action;


// java
import java.util.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.math.*;





/**
 * 
 */
public class SortTimeSeries extends Sort
{
	// constructor(s)
	public SortTimeSeries(double series[], Header header)
	{
		this.header = header;
		this.series = series;
	}
	
	
	// action
	public String getName()
	{
		return "Time-series sort";
	}
	
	public String getDescription()
	{
		return "";
	}
	
	@SuppressWarnings("unchecked")
	public Vector<IPeak> execute(Vector<IPeak> peaks)
	{
		for (IPeak ipeak : peaks)
		{
			if (!ipeak.getClass().equals(IPeakSet.class))
				continue;
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) ipeak;
			
			// setup the counters
			int mysize[] = new int[series.length];
			double myseries[] = new double[series.length];
			for (int i=0; i<series.length; ++i)
				myseries[i] = mysize[i] = 0;
			
			// retrieve the intensity values for each of the sets
			for (IPeak p : peakset)
			{
				SetInfo set = header.getSetInfoForMeasurementID(p.getMeasurementID());
				int setid = header.indexOfSetInfo(set.getID());
				
				mysize[setid] += 1;
				myseries[setid] += p.getIntensity();
			}
			
			double maxintensity = 0;
			for (int i=0; i<series.length; ++i)
				if (mysize[i]>0)
				{
					myseries[i] /= mysize[i];
					maxintensity = Math.max(maxintensity, myseries[i]);
				}
			
			// normalize
			for (int i=0; i<series.length; ++i)
				myseries[i] /= maxintensity;
			
			// set the correlation score in the annotation for the current peakset
			peakset.addAnnotation(
					"timeseries.correlation",
					Double.toString(Statistical.pearsonsCorrelation(series, myseries)[Statistical.PEARSON_CORRELATION]),
					Annotation.ValueType.DOUBLE
				);
		}
		
		// sort the peaks on the annotation
		Collections.sort(peaks, new IPeak.AnnotationDescending("timeseries.correlation"));
		
		return peaks;
	}
	
	
	// data
	protected Header header;
	protected double[] series;
}
