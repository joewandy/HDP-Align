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

// peakml
import java.util.Iterator;

import peakml.math.*;





/**
 * Implementation of a background ion, useable for all types of mass spectrometry
 * (e.g. not only 2D methods like LC-MS). This class maintains a list of peaks
 * (minimal size of 1 and either {@link Centroid} or {@link Profile}), which make
 * up the background ion.  When a list of {@link Centroid} is maintained the background
 * ion is 2D, but if a list of {@link Profile} is maintained the background ion
 * is 3D.
 * <p />
 * The constructor of this class automatically calculates a number of properties,
 * like: mass, intensity and scan out of the peaks associated with the background
 * ion. The calculations are performed with {@link IPeakSet}.
 */
public class BackgroundIon<gPeak extends Peak> extends IPeak implements Iterable<gPeak>
{
	// constructor(s)
	public BackgroundIon(PeakData<gPeak> peakdata)
	{
		this.peakdata = peakdata;
		
//		int stats_scanid[] = Statistical.stats(peakdata.scanids);
//		minscanid = stats_scanid[Statistical.MINIMUM];
//		maxscanid = stats_scanid[Statistical.MAXIMUM];
//		meanscanid = stats_scanid[Statistical.MEAN];
//		medianscanid = Statistical.median(peakdata.scanids);
		
		double stats_mass[] = Statistical.stats(peakdata.masses);
		minmass = stats_mass[Statistical.MINIMUM];
		maxmass = stats_mass[Statistical.MAXIMUM];
		meanmass = stats_mass[Statistical.MEAN];
		medianmass = Statistical.median(peakdata.masses);
		
		double stats_intensity[] = Statistical.stats(peakdata.intensities);
		minintensity = stats_intensity[Statistical.MINIMUM];
		maxintensity = stats_intensity[Statistical.MAXIMUM];
		meanintensity = stats_intensity[Statistical.MEAN];
		medianintensity = Statistical.median(peakdata.intensities);
		
		double stats_retentiontime[] = Statistical.stats(peakdata.retentiontimes);
		minretentiontime = stats_retentiontime[Statistical.MINIMUM];
		maxretentiontime = stats_retentiontime[Statistical.MAXIMUM];
		meanretentiontime = stats_retentiontime[Statistical.MEAN];
		medianretentiontime = Statistical.median(peakdata.retentiontimes);
	}
	
	
	// access
	public PeakData<gPeak> getPeakData()
	{
		return peakdata;
	}
	
	
	// IPeak overrides
	@Override
	public double getMass()
	{
		return medianmass;
	}
	
	@Override
	public void setMass(double mass)
	{
	}

	@Override
	public double getIntensity()
	{
		return medianintensity;
	}
	
	@Override
	public void setIntensity(double intensity)
	{
	}
	
	@Override
	public int getScanID()
	{
		return -1;
	}
	
	@Override
	public void setScanID(int scan)
	{
	}
	
	@Override
	public double getRetentionTime()
	{
		return minretentiontime;
	}
	
	@Override
	public void setRetentionTime(double retentiontime)
	{
	}
	
	@Override
	public int getMeasurementID()
	{
		return measurementid;
	}
	
	@Override
	public void setMeasurementID(int id)
	{
		this.measurementid = id;
	}

	@Override
	public int getPatternID()
	{
		return patternid;
	}

	@Override
	public void setPatternID(int id)
	{
		patternid = id;
	}
	
	@Override
	protected byte[] getSha1Data()
	{
		return new byte[1];
	}
	
	@Override
	public Signal getSignal()
	{
		return new Signal(peakdata.retentiontimes, peakdata.intensities);
	}

	
	// link-through to IPeakSet
	/**
	 * Returns the scan-id where this background ion starts. This property is
	 * calculated by {@link IPeakSet#getMinScanID()}.
	 * 
	 * @return			The minimum scan-id.
	 */
	public int getMinScanID()
	{
		return -1;
	}
	
	/**
	 * Returns the scan-id where this background ion ends. This property is
	 * calculated by {@link IPeakSet#getMaxScanID()}.
	 * 
	 * @return			The maximum scan-id.
	 */
	public int getMaxScanID()
	{
		return -1;
	}
	
	/**
	 * Returns the retention time where this background ion starts. This property is
	 * calculated by {@link IPeakSet#getMinRetentionTime()}.
	 * 
	 * @return			The minimum retention time.
	 */
	public double getMinRetentionTime()
	{
		return minretentiontime;
	}
	
	/**
	 * Returns the retention time where this background ion ends. This property is
	 * calculated by {@link IPeakSet#getMaxRetentionTime()}.
	 * 
	 * @return			The maximum retention time.
	 */
	public double getMaxRetentionTime()
	{
		return maxretentiontime;
	}
	
	/**
	 * Returns the lowest mass of the peaks in this background ion. This property is
	 * calculated by {@link IPeakSet#getMinMass()}.
	 * 
	 * @return			The minimum mass.
	 */
	public double getMinMass()
	{
		return minmass;
	}
	
	/**
	 * Returns the highest mass of the peaks in this background ion. This property is
	 * calculated by {@link IPeakSet#getMaxMass()}.
	 * 
	 * @return			The maximum mass.
	 */
	public double getMaxMass()
	{
		return maxmass;
	}
	
	/**
	 * Returns the mean mass of the peaks in this background ion. This property is
	 * calculated by {@link IPeakSet#getMeanMass()}.
	 * 
	 * @return			The mean mass.
	 */
	public double getMeanMass()
	{
		return meanmass;
	}
	
	/**
	 * Returns the median mass of the peaks in this background ion. This property is
	 * calculated by {@link IPeakSet#getMedianMass()}.
	 * 
	 * @return			The median mass.
	 */
	public double getMedianMass()
	{
		return medianmass;
	}
	
	/**
	 * Returns the mean intensity of the peaks in this background ion. This property is
	 * calculated by {@link IPeakSet#getMeanIntensity()}.
	 * 
	 * @return			The mean intensity.
	 */
	public double getMeanIntensity()
	{
		return meanintensity;
	}
	
	/**
	 * Returns the median intensity of the peaks in this background ion. This property is
	 * calculated by {@link IPeakSet#getMedianRelativeIntensity()}.
	 * 
	 * @return			The median intensity.
	 */
	public double getMedianIntensity()
	{
		return medianintensity;
	}
	
	/**
	 * The number of data elements making up the background ion.
	 * 
	 * @return			The number of data elements.
	 */
	public int getNrPeaks()
	{
		return peakdata.size();
	}
	
	
	// Iterable overrides
	public Iterator<gPeak> iterator()
	{
		return peakdata.iterator();
	}
	
	
	// data
//	protected int minscanid, maxscanid, meanscanid, medianscanid;
	protected double minmass, maxmass, meanmass, medianmass;
	protected double minintensity, maxintensity, meanintensity, medianintensity;
	protected double minretentiontime, maxretentiontime, meanretentiontime, medianretentiontime;
	
	protected int patternid = -1;
	protected int measurementid = -1;
	protected PeakData<gPeak> peakdata = null;
}
