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
import peakml.math.*;
import peakml.chemistry.*;





/**
 * Type-binding for a spectrum (a single measurement on a mass spectrometer). It's
 * simply an extension of {@link IPeakSet}, which contains all the needed functionality
 * already but has not been made specific. In the implementation of this class
 * the only types that are allowed are those that inherit from {@link Peak}.
 * 
 * @param <gPeak>	The type of peak stored in the spectrum, either {@link Centroid} or {@link Profile}.
 */
public class Spectrum<gPeak extends Peak> extends IPeak implements Iterable<gPeak>, Measurement
{
	/** The type of data stored in the spectrum. */
	public static enum Type
	{
		/** Spectrum generated from a photodiode array detector (ultraviolet/visible spectrum). */
		PDA,
		/** MS1 refers to single-stage MS/MS experiments designed to record the first stage of the product ion spectra. Also used when no MS/MS experiments where performed. */
		MS1,
		/** MSn refers to multi-stage MS/MS experiments designed to record product ion spectra where n is the number of product ion stages (progeny ions). */
		MSn
	}
	
	/** The format (centroid or profile) of the data */
	public static enum DataFormat
	{
		/** Continuous data */
		PROFILE,
		/** Centroided data */
		CENTROID
	}

	
	// constructor(s)
	/**
	 * Calls the constructor {@link Spectrum#Spectrum(PeakData, Type, Polarity, double)} and assumes
	 * for type {@link Type#MS1}, for polarity {@link Polarity#NEUTRAL} and for precursormass -1.
	 * 
	 * @param peakdata		The {@link PeakData} instance containing the data of this spectrum.
	 */
	public Spectrum(PeakData<gPeak> peakdata)
	{
		this(peakdata, Type.MS1, Polarity.NEUTRAL, -1);
	}
	
	/**
	 * Calls the constructor {@link Spectrum#Spectrum(PeakData, Type, Polarity, double)} and assumes
	 * for type {@link Type#MS1} and for precursormass -1.
	 * 
	 * @param peakdata		The {@link PeakData} instance containing the data of this spectrum.
	 * @param polarity		The polarity of the measurement (positive, negative, neutral).
	 */
	public Spectrum(PeakData<gPeak> peakdata, Polarity polarity)
	{
		this(peakdata, Type.MS1, polarity, -1);
	}
	
	/**
	 * Calls the constructor {@link Spectrum#Spectrum(PeakData, Type, Polarity, double)} and assumes
	 * for precursor mass -1.
	 * 
	 * @param peakdata		The {@link PeakData} instance containing the data of this spectrum.
	 * @param type			The type of spectrum (ms, msn, pda)
	 * @param polarity		The polarity of the measurement (positive, negative, neutral).
	 */
	public Spectrum(PeakData<gPeak> peakdata, Type type, Polarity polarity)
	{
		this(peakdata, type, polarity, -1);
	}
	
	/**
	 * Standard constructor for the spectrum. The list of peaks forms the contents of the
	 * spectrum. As this class inherits from IPeakSet the list is passed to
	 * {@link IPeakSet#IPeakSet(List)}. When the spectrum is made up of profile data,
	 * the peak selection must already have been performed and all the data-points making
	 * up a peak in the spectrum have been stored within the {@link Profile} class.
	 * 
	 * @param peakdata		The {@link PeakData} instance containing the data of this spectrum.
	 * @param type			The type of spectrum (ms, msn, pda)
	 * @param polarity		The polarity of the measurement (positive, negative, neutral).
	 * @param precursormass	The precursor mass, only valid if type is {@link Type#MSn}.
	 */
	public Spectrum(PeakData<gPeak> peakdata, Type type, Polarity polarity, double precursormass)
	{
		if (type==Type.MSn && precursormass==-1)
			throw new RuntimeException("Spectrum is of type MSn, but is missing a precursosmass.");
		this.peakdata	= peakdata;
		this.type = type;
		this.polarity = polarity;
		this.precursormass = (type==Type.MSn ? precursormass : -1);
	}
	
	
	// general access
	/**
	 * Returns the type of the spectrum.
	 * 
	 * @return				The type of spectrum.
	 */
	public Type getType()
	{
		return type;
	}
	
	/**
	 * Returns the polarity of the data in the spectrum.
	 * 
	 * @return				The polarity of the data.
	 */
	public Polarity getPolarity()
	{
		return polarity;
	}
	
	/**
	 * Returns the number of peaks associated to this spectrum.
	 * 
	 * @return				The number of peaks in this spectrum.
	 */
	public int getNrPeaks()
	{
		return peakdata.size();
	}
	
	/**
	 * Returns the number of MSn spectra associated with this spectrum.
	 * 
	 * @return 				The number of MSn spectra associated with this spectrum.
	 */
	public int getNrMSnSpecra()
	{
		return msnspectra.size();
	}
	
	/**
	 * Returns the MSn spectrum at the given index.
	 * 
	 * @return				The MSn spectrum at the given index.
	 * @throws IndexOutOfBoundsException
	 * 						Thrown when the index is out of bounds.
	 */
	public Spectrum<gPeak> getMSnSpectrum(int index) throws IndexOutOfBoundsException
	{
		return msnspectra.get(index);
	}
	
	/**
	 * Adds the given MSn spectrum to the list.
	 * 
	 * @param spectrum		The MSn spectrum to be added.
	 */
	public void addMSnSpectrum(Spectrum<gPeak> spectrum)
	{
		if (spectrum.getType() != Type.MSn)
			throw new RuntimeException("The given spectrum is not of type Spectrum.Type.MSn.");
		msnspectra.add(spectrum);
	}
	
	
	// peak access
	/**
	 * Returns a vector with all the peaks.
	 * 
	 * @return				A vector with all the peaks.
	 */
	public Vector<gPeak> getPeaks()
	{
		return peakdata.getPeaks();
	}
	
	/**
	 * Returns the {@link PeakData} container associated to this spectrum.
	 * 
	 * @return				The peakdata container.
	 */
	public PeakData<gPeak> getPeakData()
	{
		return peakdata;
	}
	

	// IPeak overrides
	@Override
	public double getRetentionTime()
	{
		return retentiontime;
	}

	@Override
	public void setRetentionTime(double retentiontime)
	{
		this.retentiontime = retentiontime;
	}

	@Override
	public int getScanID()
	{
		return scanid;
	}

	@Override
	public void setScanID(int scan)
	{
		this.scanid = scan;
	}

	@Override
	public double getIntensity()
	{
		return -1;
	}

	@Override
	public void setIntensity(double intensity)
	{
	}

	@Override
	public double getMass()
	{
		return -1;
	}

	@Override
	public void setMass(double mass)
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
		this.patternid = id;
	}

	@Override
	protected byte[] getSha1Data()
	{
		return null;
	}

	@Override
	public Signal getSignal()
	{
		return null;
	}
	
	
	// iterable
	public Iterator<gPeak> iterator()
	{
		return peakdata.iterator();
	}

	
	// data
	protected Type type;
	protected Polarity polarity;
	protected PeakData<gPeak> peakdata;
	
	protected double retentiontime = -1;
	
	protected double precursormass;
	protected Vector<Spectrum<gPeak>> msnspectra = new Vector<Spectrum<gPeak>>();
	
	protected int scanid = -1;
	protected int patternid = -1;
	protected int measurementid = -1;
}
