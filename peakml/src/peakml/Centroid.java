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
import peakml.math.*;





/**
 * Implementation of a centroid or data-point in a mass spectrometry spectrum or
 * profile. This class keeps track of a single mass, intensity and optional
 * scanid value, which makes up a single data-point in a measurement. It's been
 * chosen to mis-use the centroid value for a data-point as they are essential
 * the same thing. The actual data associated to a {@link Centroid} instance
 * is stored in a {@link PeakData} instance.
 * <p />
 * The centroid is the smallest (indivisible) component of mass spectrometry data.
 */
public class Centroid extends Peak
{
	// factory
	/**
	 * Factory for creating a {@link Centroid} instance. This construct is needed to
	 * separate object creation from the implementation in {@link PeakData}.
	 */
	public static class Factory implements PeakData.PeakFactory<Centroid>
	{
		/** {@link PeakData.PeakFactory#create(PeakData, int)} */
		public Centroid create(PeakData<Centroid> data, int index)
		{
			return new Centroid(data, index);
		}
		
		/** {@link PeakData.PeakFactory#getName()} */
		public String getName()
		{
			return "centroid";
		}

		/** {@link PeakData.PeakFactory#getPeakClass()} */
		public Class<Centroid> getPeakClass()
		{
			return Centroid.class;
		}
	}
	
	/** Static instance for the factory, so not everybody will need to produce one */
	public static PeakData.PeakFactory<Centroid> factory = new Factory();
	
	
	// constructor(s)
	/**
	 * Constructs a new {@link Centroid} instance linked to the given {@link PeakData}
	 * instance. The given index is used to find the correct data in the collection
	 * of peaks contained in the {@link PeakData} instance.
	 * 
	 * @param peakdata		A {@link PeakData} instance containing all the data.
	 * @param index			The index indicating the location of the data in the {@link PeakData} instance.
	 */
	public Centroid(PeakData<Centroid> peakdata, int index)
	{
		this.index = index;
		this.peakdata = peakdata;
	}
	
	
	// IPeak overrides
	@Override
	public double getMass()
	{
		return peakdata.masses[index];
	}
	
	@Override
	public void setMass(double mass)
	{
		peakdata.masses[index] = mass;
	}
	
	@Override
	public double getIntensity()
	{
		return peakdata.intensities[index];
	}
	
	@Override
	public void setIntensity(double intensity)
	{
		peakdata.intensities[index] = intensity;
	}
	
	@Override
	public int getScanID()
	{
		return peakdata.scanids[index];
	}
	
	@Override
	public void setScanID(int scan)
	{
		peakdata.scanids[index] = scan;
	}
	
	@Override
	public double getRetentionTime()
	{
		return peakdata.retentiontimes[index];
	}
	
	@Override
	public void setRetentionTime(double retentiontime)
	{
		peakdata.retentiontimes[index] = retentiontime;
	}
	
	@Override
	public int getMeasurementID()
	{
		return peakdata.measurementids[index];
	}
	
	@Override
	public void setMeasurementID(int id)
	{
		peakdata.measurementids[index] = id;
	}
	
	@Override
	public int getPatternID()
	{
		return peakdata.patternids[index];
	}
	
	@Override
	public void setPatternID(int id)
	{
		peakdata.patternids[index] = id;
	}
	
	@Override
	protected byte[] getSha1Data()
	{
		StringBuffer str = new StringBuffer();
		
		str.append(getMass());
		str.append(getIntensity());
		str.append(getScanID());
		str.append(getRetentionTime());
		str.append(getMeasurementID());
		str.append(getPatternID());
		
		return str.toString().getBytes();
	}
	
	@Override
	public Signal getSignal()
	{
		return new Signal(new double[] {getMass()}, new double[] {getIntensity()});
	}
	
	
	// data
	protected int index;
	protected PeakData<Centroid> peakdata;
}
