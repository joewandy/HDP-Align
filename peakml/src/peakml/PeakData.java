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





/**
 * Implementation of a container for peak-data. This central container keeps track of
 * peak data in arrays of basic data-types in order to reduce the memory footprint 
 * and load-times of applications using large amounts of data.
 * 
 * @param <gPeak>			The {@link Peak} type associated to this container.
 */
public class PeakData<gPeak extends Peak> implements Iterable<gPeak>
{
	/**  */
	public static final int INDEX_REAL = 0;
	/**  */
	public static final int INDEX_INDEXTABLE = 1;
	
	
	// factory
	/**
	 * Interface describing a factory for the creation of instances of the generic gPeak
	 * type. This has been separated to make it easier to integrate new types of 
	 * {@link Peak} subclasses into the {@link PeakData} structure. Also, it provides
	 * a means of keeping track of the type associated to the {@link PeakData} instance.
	 */
	public interface PeakFactory<gPeak extends Peak>
	{
		/**
		 * Creates a new instance of the gPeak type and initiates it with the given
		 * {@link PeakData} instance and index indicating the location of the data
		 * associated to the new instance.
		 * 
		 * @param data			The {@link PeakData} instance associated to the new instance.
		 * @param index			The index of the data in the {@link PeakData} instance.
		 * @return				A new instance of the generic gPeak type.
		 */
		public gPeak create(PeakData<gPeak> data, int index);
		
		/**
		 * Returns the name of the type associated to the factory.
		 * 
		 * @return				The name of the generic type associated to the factory.
		 */
		public String getName();
		
		/**
		 * Returns the {@link Class} instance of the generic type associated to the factory.
		 * 
		 * @return				The class of the generic type associated to the factory.
		 */
		public Class<gPeak> getPeakClass();
	}
	
	
	// constructor(s)
	/**
	 * Creates a new {@link PeakData} instance associated to the given {@link PeakFactory}
	 * and of the given size. All the peak-data is initialized to -1.
	 * 
	 * @param factory				The factory for creating new instances of the generic gPeak type.
	 * @param size					The number of peaks stored in this container.
	 */
	public PeakData(PeakFactory<gPeak> factory, int size)
	{
		if (!factory.getPeakClass().equals(Centroid.class))
			throw new RuntimeException("Not yet supporting Profile mode data.");
		
		this.size = size;
		this.factory = factory;
		
		this.scanids = new int[size];
		this.patternids = new int[size];
		this.measurementids = new int[size];
		this.masses = new double[size];
		this.intensities = new double[size];
		this.retentiontimes = new double[size];
		
		// initialize two arrays, copy the values into the other arrays
		for (int i=0; i<size; ++i)
		{
			scanids[i]	= -1;	// int
			masses[i]	= -1;	// double
		}
		System.arraycopy(scanids,	0, patternids, 0, size);
		System.arraycopy(scanids,	0, measurementids, 0, size);
		System.arraycopy(masses,	0, intensities, 0, size);
		System.arraycopy(masses,	0, retentiontimes, 0, size);
	}
	
	/**
	 * Creates a new {@link PeakData} instance associated to the given {@link PeakFactory}
	 * and of the given size. The container is initialized with the given arrays (the
	 * reference is copies, so do not remove).
	 * 
	 * 
	 * @param factory				The factory for creating new instances of the generic gPeak type.
	 * @param size					The number of peaks stored in this container.
	 * @param scanids				Array containing all the scan-id's.
	 * @param patternids			Array containing all the pattern-id's.
	 * @param measurementids		Array containing all the measurement-id's.
	 * @param masses				Array containing all the masses.
	 * @param intensities			Array containing all the intensities.
	 * @param relativeintensities	Array containing all the relative-intensities.
	 * @param retentiontimes		Array containing all the retention-times.
	 */
	public PeakData(PeakFactory<gPeak> factory, int size, int scanids[], int patternids[], int measurementids[], double masses[], double intensities[], double retentiontimes[])
	{
		if (!factory.getPeakClass().equals(Centroid.class))
			throw new RuntimeException("Not yet supporting Profile mode data.");
		
		if (scanids.length!=size || patternids.length!=size || measurementids.length!=size || masses.length!=size || intensities.length!=size || retentiontimes.length!=size)
			new RuntimeException("");
		
		this.size = size;
		this.factory = factory;
		
		this.scanids = scanids;
		this.patternids = patternids;
		this.measurementids = measurementids;
		this.masses = masses;
		this.intensities = intensities;
		this.retentiontimes = retentiontimes;
	}
	
	/**
	 * Highly optimized copy constructor, which makes a deep-copy of the data stored in
	 * the given PeakData instance.
	 * 
	 * @param peakdata				The PeakData instance to copy.
	 */
	public PeakData(PeakData<gPeak> peakdata)
	{
		this.size = peakdata.size;
		this.factory = peakdata.factory;
		
		this.scanids = new int[size];
		System.arraycopy(peakdata.scanids, 0, scanids, 0, size);
		this.patternids = new int[size];
		System.arraycopy(peakdata.patternids, 0, patternids, 0, size);
		this.measurementids = new int[size];
		System.arraycopy(peakdata.measurementids, 0, measurementids, 0, size);
		this.masses = new double[size];
		System.arraycopy(peakdata.masses, 0, masses, 0, size);
		this.intensities = new double[size];
		System.arraycopy(peakdata.intensities, 0, intensities, 0, size);
		this.retentiontimes = new double[size];
		System.arraycopy(peakdata.retentiontimes, 0, retentiontimes, 0, size);
	}
	
	
	// general access
	/**
	 * Returns the size of the container.
	 * 
	 * @return						The size (number of the peaks) of the container.
	 */
	public int size()
	{
		return size;
	}
	
	public void set(int index, gPeak peak)
	{
		if (peak.getClass().equals(Centroid.class))
		{
			this.setMass(index, peak.getMass());
			this.setScanID(index, peak.getScanID());
			this.setIntensity(index, peak.getIntensity());
			this.setRetentionTime(index, peak.getRetentionTime());
			this.setPatternID(index, peak.getPatternID());
			this.setMeasurementID(index, peak.getMeasurementID());
		}
		else
			throw new RuntimeException("Operation not supported for PeakData<Profile>.");
	}
	
	/**
	 * Set all the properties of a peak at the given index at one go.
	 * 
	 * @param index					The index of the peakdata to set.
	 * @param scanid				The scanid.
	 * @param retentiontime			The retention time.
	 * @param mass					The mass.
	 * @param intensity				The intensity.
	 */
	public void set(int index, int scanid, double retentiontime, double mass, double intensity)
	{
		scanids[index]				= scanid;
		retentiontimes[index]		= retentiontime;
		masses[index]				= mass;
		intensities[index]			= intensity;
	}
	
	/**
	 * Returns a {@link Peak} instance using the data stored at the given index in this
	 * container.
	 * 
	 * @param index					The index where the data is located.
	 * @return						A {@link Peak} instance.
	 */
	public gPeak getPeak(int index)
	{
		return factory.create(this, index);
	}
	
	/**
	 * Returns a vector with {@link Peak} instances covering all the data stored in this
	 * container.
	 * 
	 * @return						A vector with all the peaks associated with this container.
	 */
	public Vector<gPeak> getPeaks()
	{
		Vector<gPeak> peaks = new Vector<gPeak>();
		for (int i=0; i<size; ++i)
			peaks.add(factory.create(this, i));
		return peaks;
	}
	
	/**
	 * Returns the factory for the {@link Peak} type associated to this container. The
	 * factory can be used to create new instances.
	 * 
	 * @return						The factory for creating instances of the associated type.
	 */
	public PeakFactory<gPeak> getFactory()
	{
		return factory;
	}
	
	
	// searching interface
	/**
	 * 
	 * @return
	 */
	public double[] getIndexTableAscendingMass()
	{
		// create the index table if necessary
		if (indextable_ascending_mass == null)
		{
			double cpy[] = new double[size];
			System.arraycopy(masses, 0, cpy, 0, size);
			indextable_ascending_mass = new double[size];
			for (int i=0; i<size; ++i)
				indextable_ascending_mass[i] = i;
			Statistical.qsort(cpy, indextable_ascending_mass);
		}
		return indextable_ascending_mass;
	}
	
	/**
	 * ...
	 * 
	 * <pre>
	 *  --------------------------------
	 * | 1  | 5  | 8  | 10  | 20  | 30  |
	 *  --------------------------------
	 *   ^--> ^--> ^--> ^---> ^---> ^--->
	 * </pre>
	 */
	public int[] getIndexOfMass(double mass)
	{
		// The naming convention used here:
		// indx:	index in the mass-table
		// index:	index in the mass-index-table
		
		getIndexTableAscendingMass();
		
		// check whether there is enough data
		if (size <= 2) return new int[]{-1,-1};
		
		// the easy case
		int indx0 = (int) indextable_ascending_mass[0];
		int indx1 = (int) indextable_ascending_mass[1];
		int indxn = (int) indextable_ascending_mass[size-1];
		// the given mass is smaller than the smallest in our data
		if (mass < masses[indx0])
			return new int[]{-1,-1};
		// the given mass equals the first
		if (mass >= masses[indx0] && mass <= masses[indx1])
			return new int[]{indx0, 0};
		// the given mass equals the last
		if (mass >= masses[indxn])
			return new int[]{indxn, size-1};
		
		// binary search
		int index = size / 2;
		int index_bgn = 0;
		int index_end = size - 1;
		while (true)
		{
			// retrieve the indices for the masses-table
			int indx = (int) indextable_ascending_mass[index];
			
			// check whether we are there
			int indxpp = (int) indextable_ascending_mass[index+1];
			if (mass>=masses[indx] && mass<=masses[indxpp])
				return new int[]{indx,index};
			
			// not yet finished, recalculate indices for the mass-index-table
			int index_new;
			if (masses[indx] > mass)
			{
				index_new = (index_bgn+index) / 2;
				index_end = index;
			}
			else
			{
				index_new = (index_end+index) / 2;
				index_bgn = index;
			}
			index = index_new;
		}
	}
	
	/**
	 * 
	 */
	public int[][] getIndicesInMassRange(double minmass, double maxmass)
	{
		// The naming convention used here:
		// indx:	index in the mass-table
		// index:	index in the mass-index-table
		
		// check whether there is enough data
		if (size <= 2) return new int[0][0];
		
		// fire up the indextable and retrieve the index of the minmass
		int index_of_minmass[] = getIndexOfMass(minmass);
		
		// check whether there are masses within our range
		int index = index_of_minmass[INDEX_INDEXTABLE];
		int indx0 = (int) indextable_ascending_mass[0];
		if (index==size-1 || (index==-1 && maxmass<masses[indx0]))
			return new int[0][0];
		
		// when -1 was returned 
		if (index == -1) index = 0;
		while (minmass > masses[(int) indextable_ascending_mass[index]])
			index++;
		
		// when there are no peaks
		if (masses[(int) indextable_ascending_mass[index]] > maxmass)
			return new int[0][0];
		
		// find out how many masses are within our range
		int index_bgn = index;
		int index_end = index+1;
		for ( ; index_end<size; ++index_end)
		{
			int indx = (int) indextable_ascending_mass[index_end];
			if (masses[indx] > maxmass)
				break;
		}
		
		// create the array and fill it
		int indices[][] = new int[index_end-index_bgn][];
		for (index=index_bgn; index<index_end; ++index)
			indices[index-index_bgn] = new int[]{(int)indextable_ascending_mass[index],index};
		return indices;
	}
	
	/**
	 * 
	 * @return
	 */
	public double[] getIndexTableAscendingIntensity()
	{
		// create the index table if necessary
		if (indextable_ascending_intensity == null)
		{
			double cpy[] = new double[size];
			System.arraycopy(intensities, 0, cpy, 0, size);
			indextable_ascending_intensity = new double[size];
			for (int i=0; i<size; ++i)
				indextable_ascending_intensity[i] = i;
			Statistical.qsort(cpy, indextable_ascending_intensity);
		}
		return indextable_ascending_intensity;
	}
	
	
	// scan access
	/**
	 * Returns all the scan-id's stored in this container.
	 * 
	 * @return						All the scan-id's stored in this container.
	 */
	public int[] getScanIDs()
	{
		return scanids;
	}
	
	/**
	 * Returns the scan-id stored at the given index.
	 * 
	 * @param index					The index.
	 * @return						The scan-id at the given index.
	 */
	public int getScanID(int index)
	{
		return scanids[index];
	}
	
	/**
	 * Sets the given scan-id at the given index.
	 * 
	 * @param index					The index.
	 * @param scanid				The new scan-id.
	 */
	public void setScanID(int index, int scanid)
	{
		scanids[index] = scanid;
	}
	
	
	// retention time access
	/**
	 * Returns all the retention-times stored in this container.
	 * 
	 * @return						All the retention-times stored in this container.
	 */
	public double[] getRetentionTimes()
	{
		return retentiontimes;
	}
	
	/**
	 * Returns the retention-time stored at the given index.
	 * 
	 * @param index					The index.
	 * @return						The retention-time at the given index.
	 */
	public double getRetentionTime(int index)
	{
		return retentiontimes[index];
	}
	
	/**
	 * Sets the given retention-time at the given index.
	 * 
	 * @param index					The index.
	 * @param retentiontime			The new retention-time.
	 */
	public void setRetentionTime(int index, double retentiontime)
	{
		retentiontimes[index] = retentiontime;
	}
	
	
	// mass access
	/**
	 * Returns all the masses stored in this container.
	 * 
	 * @return						All the masses stored in this container.
	 */
	public double[] getMasses()
	{
		return masses;
	}
	
	/**
	 * Returns the mass stored at the given index.
	 * 
	 * @param index					The index.
	 * @return						The mass at the given index.
	 */
	public double getMass(int index)
	{
		return masses[index];
	}
	
	/**
	 * Sets the given mass at the given index.
	 * 
	 * @param index					The index.
	 * @param mass					The new mass.
	 */
	public void setMass(int index, double mass)
	{
		masses[index] = mass;
		
		// any index of 'indextable_ascending_mass' is now invalid.
		indextable_ascending_mass = null;
	}
	
	
	// intensity access
	public int indexOfMaxIntensity()
	{
		int indx = 0;
		double max = intensities[0];
		for (int i=1; i<size; ++i)
			if (intensities[i] > max)
			{
				indx = i;
				max = intensities[i];
			}
		return indx;
	}
	
	/**
	 * Returns all the intensities stored in this container.
	 * 
	 * @return						All the intensities stored in this container.
	 */
	public double[] getIntensities()
	{
		return intensities;
	}
	
	/**
	 * Returns the intensity stored at the given index.
	 * 
	 * @param index					The index.
	 * @return						The intensity at the given index.
	 */
	public double getIntensity(int index)
	{
		return intensities[index];
	}
	
	/**
	 * Sets the given intensity at the given index.
	 * 
	 * @param index					The index.
	 * @param intensity				The new intensity.
	 */
	public void setIntensity(int index, double intensity)
	{
		intensities[index] = intensity;
	}
	
	
	// patternid access
	/**
	 * Returns all the pattern-id's stored in this container.
	 * 
	 * @return						All the pattern-id's stored in this container.
	 */
	public int[] getPatternIDs()
	{
		return patternids;
	}
	
	/**
	 * Returns the pattern-id stored at the given index.
	 * 
	 * @param index					The index.
	 * @return						The pattern-id at the given index.
	 */
	public int getPatternID(int index)
	{
		return patternids[index];
	}
	
	/**
	 * Sets the given pattern-id at the given index.
	 * 
	 * @param index					The index.
	 * @param patternid				The new scan-id.
	 */
	public void setPatternID(int index, int patternid)
	{
		patternids[index] = patternid;
	}
	
	
	// measurementid access
	/**
	 * Returns all the measurement-id's stored in this container.
	 * 
	 * @return						All the measurement-id's stored in this container.
	 */
	public int[] getMeasurementIDs()
	{
		return measurementids;
	}
	
	/**
	 * Returns the measurement-id stored at the given index.
	 * 
	 * @param index					The index.
	 * @return						The measurement-id at the given index.
	 */
	public int getMeasurementID(int index)
	{
		return measurementids[index];
	}
	
	/**
	 * Sets the given measurement-id at the given index.
	 * 
	 * @param index					The index.
	 * @param measurementid			The new measurement-id.
	 */
	public void setMeasurementID(int index, int measurementid)
	{
		measurementids[index] = measurementid;
	}
	
	
	// iterable overrides
	public Iterator<gPeak> iterator()
	{
		return new PeakDataIterator<gPeak>(this);
	}
	
	
	// data
	protected int size;
	protected PeakFactory<gPeak> factory;
	
	protected int scanids[];
	protected int patternids[];
	protected int measurementids[];
	
	protected double masses[];
	protected double intensities[];
	protected double retentiontimes[];
	
	protected double indextable_ascending_mass[] = null;
	protected double indextable_ascending_intensity[] = null;
}
