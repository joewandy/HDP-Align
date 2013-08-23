/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml;


// java
import java.util.*;

// peakml
import peakml.chemistry.*;





/**
 * This class combines the information from a setup combining a chromatograph (LC or GC)
 * with a mass spectrometer. The data is acquired in separate scans over an amount of
 * time. All these scans are collected within an instance of this class.
 * 
 * @param <gPeak>	The type of peak stored in the spectra, either {@link Centroid} or {@link Profile}.
 */
public class ChromatographyMS<gPeak extends Peak> implements Measurement, Iterable<Spectrum<gPeak>>
{
	// constructor(s)
	public ChromatographyMS()
	{
	}
	
	public ChromatographyMS(Vector<Spectrum<gPeak>> scans)
	{
		this.scans.addAll(scans);
	}
	
	
	// access
	/**
	 * Returns the number of scans stored in this class.
	 * 
	 * @return				The number of scans.
	 */
	public int getNrScans()
	{
		return scans.size();
	}
	
	/**
	 * Returns the scan at the given index. This index is identical to the scan-id found
	 * in the IPeak classes.
	 * 
	 * @param index			The index or scanid.
	 * @return				The scan at the given index or scanid.
	 * @throws IndexOutOfBoundsException
	 * 						Thrown when the given index is out of bounds of the scan-vector.
	 */
	public Spectrum<gPeak> getScan(int index) throws IndexOutOfBoundsException
	{
		return scans.get(index);
	}
	
	/**
	 * Returns a reference to the vector with scans stored in this class. As this is a
	 * reference the vector should not be resorted or elements added or removed.
	 * 
	 * @return				A vector with all the scans in this class.
	 */
	public Vector<Spectrum<gPeak>> getScans()
	{
		return scans;
	}
	
	/**
	 * This method makes a collection of all the peaks in this class. This collection can
	 * be sorted to retrieve additional information about the data, without affecting the
	 * contents of this class.
	 * 
	 * @return				A vector with all the peaks in this class.
	 */
	public Vector<gPeak> getAllPeaks()
	{
		Vector<gPeak> peaks = new Vector<gPeak>();
		for (int scan=0; scan<getNrScans(); ++scan)
			peaks.addAll(scans.get(scan).getPeaks());
		
		return peaks;
	}
	
	/**
	 * Extracts the mass chromatogram with the given properties from the raw data.
	 * 
	 * @param minrt			The low retention time of the chromatogram in seconds.
	 * @param maxrt			The high retention time of the chromatogram in seconds.
	 * @param mass			The mass of the chromatogram.
	 * @param ppm			The expected accuracy.
	 * @return				The extracted mass chromatogram.
	 */
	public MassChromatogram<gPeak> extract(double minrt, double maxrt, double mass, double ppm)
	{
		if (scans.size() < 3)
			return null;
		
		// locate the first scan
		int scanid = -1;
		for (int i=0; i<scans.size(); ++i)
		{
			double rt = scans.get(i).getRetentionTime();
			if (rt >= minrt)
			{
				scanid = i;
				break;
			}
		}
		if (scanid == -1)
			return null;
		
		// locate the datapoins
		double delta = PeriodicTable.PPM(mass, ppm);
		Vector<gPeak> datapoints = new Vector<gPeak>();
		while (scanid < scans.size())
		{
			Spectrum<gPeak> spectrum = scans.get(scanid++);
			if (spectrum.getRetentionTime() > maxrt)
				break;
			
			PeakData<gPeak> peakdata = spectrum.getPeakData();
			int[][] neighbourhood = peakdata.getIndicesInMassRange(mass-delta, mass+delta);
			if (neighbourhood.length == 0)
				continue;
			
			// select the most intense, unprocessed peak
			int bestneighbour = -1;
			double bestneighbourintensity = -1;
			for (int neighbour=0; neighbour<neighbourhood.length; ++neighbour)
			{
				double intensity = peakdata.getIntensity(neighbourhood[neighbour][PeakData.INDEX_REAL]);
				if (intensity>bestneighbourintensity)
				{
					bestneighbour = neighbourhood[neighbour][PeakData.INDEX_REAL];
					bestneighbourintensity = intensity;
				}
			}
			
			// add the selected to the list
			datapoints.add(peakdata.getPeak(bestneighbour));
		}
		
		// create the data
		if (datapoints.size() < 3)
			return null;
		PeakData<gPeak> peakdata = new PeakData<gPeak>(
				scans.firstElement().getPeakData().getFactory(),
				datapoints.size()
			);
		for (int i=0; i<datapoints.size(); ++i)
			peakdata.set(i, datapoints.get(i));
		return new MassChromatogram<gPeak>(peakdata);
	}
	
	
	// general information
	public double getStartTime()
	{
		if (scans.isEmpty())
			return -1;
		return scans.firstElement().getRetentionTime();
	}
	
	public double getEndTime()
	{
		if (scans.isEmpty())
			return -1;
		return scans.lastElement().getRetentionTime();
	}


	// Iterable overrides
	/** {@link Iterable#iterator()} */
	public Iterator<Spectrum<gPeak>> iterator()
	{
		return scans.iterator();
	}
	
	
	// data
	protected Vector<Spectrum<gPeak>> scans = new Vector<Spectrum<gPeak>>();
}
