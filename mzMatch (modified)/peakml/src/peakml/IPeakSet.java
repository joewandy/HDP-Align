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
 * Implementation of a class maintaining a set of peaks. The constructor of this
 * class efficiently pre-calculates a large number of properties from the set of
 * peaks, like min, max and mean values of intensity, mass and scan; furthermore
 * it provides a convenient interface for efficiently looking for specific masses
 * in a large list of peaks by employing a binary search approach.
 * <p />
 * This class implements the {@link Iterable} interface, making it possible to
 * use the construct:
 * <p />
 * <pre>
 * IPeakSet<IPeak> peakset = ...;
 * for (IPeak peak : peakset)
 *    ...;
 * </pre>
 * 
 * @param <Type>	The type of peaks stored in the peakset.
 */
@SuppressWarnings("unchecked")
public class IPeakSet<Type extends IPeak> extends IPeak implements Iterable<Type>, Measurement
{
	// constructor(s)
	public IPeakSet(Type t) throws RuntimeException
	{
		Vector<Type> vector = new Vector<Type>();
		vector.add(t);
		init(vector);
	}
	
	/**
	 * Constructs a new peakset with the given set of peaks. This constructor
	 * picks up an array of peaks, which is converted into a vector and passed
	 * on to {@link IPeakSet#IPeakSet(List)}.
	 * 
	 * @param set		An array of peaks to be stored in the peakset.
	 * @throws RuntimeException
	 * 					Thrown when a set of size 0 or 1 is passed.
	 */
	public IPeakSet(Type set[]) throws RuntimeException
	{
		Vector<Type> vector = new Vector<Type>();
		for (Type t : set)
			vector.add(t);
		init(vector);
	}
	
	/**
	 * Constructs a new peakset with the given set of peaks. From the peaks stored
	 * in the list a large number of properties is calculated, like min, max and
	 * mean values of intensity, mass and scan. Apart from this, an extra vector
	 * is created, which is sorted on ascending mass. This additional vector is
	 * used for an efficient search mechanism based on mass.
	 * <p />
	 * A peakset needs at least 2 peaks in order to function correctly.
	 * 
	 * @param set		A list of peaks to be stored in the peakset.
	 * @throws RuntimeException
	 * 					Thrown when a set of size 0 or 1 is passed.
	 */
	public IPeakSet(List<Type> set) throws RuntimeException
	{
		init(set);
	}
	
	protected void init(List<Type> set) throws RuntimeException
	{
		if (set.size() < 1)
			throw new RuntimeException("A peakset needs at least 1 peak to function correctly");
		
		// save the type-class
		container_class = set.get(0).getClass();
		
		// save the peaks and calculate the properties
		peaks.addAll(set);
		peaks_sorted_mass.addAll(set);
		
		int medianindex1 = (peaks_sorted_mass.size()==1 ? 0 : (peaks_sorted_mass.size()%2==0 ? (peaks_sorted_mass.size()/2)-1 : (int) Math.floor(peaks_sorted_mass.size()/2.)));
		int medianindex2 = (peaks_sorted_mass.size()%2==0 ? peaks_sorted_mass.size()/2 : medianindex1);
		
		Collections.sort(peaks_sorted_mass, IPeak.sort_intensity_ascending);
		medianintensity = (peaks_sorted_mass.get(medianindex1).getIntensity() + peaks_sorted_mass.get(medianindex2).getIntensity()) / 2.;
		Collections.sort(peaks_sorted_mass, IPeak.sort_mass_ascending);
		medianmass = (peaks_sorted_mass.get(medianindex1).getMass() + peaks_sorted_mass.get(medianindex2).getMass()) / 2.;
		
		minscanid = -1; maxscanid = 0;
		minretentiontime = -1; maxretentiontime = 0;
		minmass = -1; maxmass = 0;
		minintensity = -1; maxintensity = 0;
		
		meanmass = 0;
		meanscanid = 0;
		meanintensity = 0;
		meanretentiontime = 0;
		for (IPeak peak : peaks)
		{
			maxscanid = Math.max(maxscanid, peak.getScanID());
			minscanid = (minscanid==-1 ? peak.getScanID() : Math.min(minscanid,peak.getScanID()));
			maxretentiontime = Math.max(maxretentiontime, peak.getRetentionTime());
			minretentiontime = (minretentiontime==-1 ? peak.getRetentionTime() : Math.min(minretentiontime,peak.getRetentionTime()));
			maxmass = Math.max(maxmass, peak.getMass());
			minmass = (minmass==-1 ? peak.getMass() : Math.min(minmass,peak.getMass()));
			maxintensity = Math.max(maxintensity, peak.getIntensity());
			minintensity = (minintensity==-1 ? peak.getIntensity() : Math.min(minintensity,peak.getIntensity()));
			
			meanmass += peak.getMass();
			meanscanid += peak.getScanID();
			meanintensity += peak.getIntensity();
			meanretentiontime += peak.getRetentionTime();
		}
		meanmass /= peaks.size();
		meanscanid /= peaks.size();
		meanintensity /= peaks.size();
		meanretentiontime /= peaks.size();
	}
	
	
	// added functionality
	/**
	 * Returns the type of peaks stored in the peakset. The Java programming language
	 * does not provide a convenient interface for retrieving a generic type (due
	 * to erasure), which can be solved by taking the class information of the
	 * first element in the set.
	 * 
	 * @return			The generic class type of the peaks stored in the peakset.
	 */
	public Class<? extends Object> getContainerClass()
	{
		return container_class;
	}
	
	/**
	 * Returns the minimum scan-id of all the peaks stored in this peakset. This
	 * value can be -1 in the case of peaks from a 1D technology.
	 * 
	 * @return			The minimum scan-id.
	 */
	public int getMinScanID()
	{
		return minscanid;
	}
	
	/**
	 * Returns the maximum scan-id of all the peaks stored in this peakset. This
	 * value can be -1 in the case of peaks from a 1D technology.
	 * 
	 * @return			The maximum scan-id.
	 */
	public int getMaxScanID()
	{
		return maxscanid;
	}
	
	/**
	 * Returns the mean scan-id of all the peaks stored in this peakset. This
	 * value can be -1 in the case of peaks from a 1D technology.
	 * 
	 * @return			The mean scan-id.
	 */
	public int getMeanScanID()
	{
		return meanscanid;
	}
	
	/**
	 * Returns the minimum retention time of all the peaks stored in this peakset. This
	 * value can be -1 in the case of peaks from a 1D technology.
	 * 
	 * @return			The minimum retention time.
	 */
	public double getMinRetentionTime()
	{
		return minretentiontime;
	}
	
	/**
	 * Returns the maximum retention time of all the peaks stored in this peakset. This
	 * value can be -1 in the case of peaks from a 1D technology.
	 * 
	 * @return			The maximum retention time.
	 */
	public double getMaxRetentionTime()
	{
		return maxretentiontime;
	}
	
	/**
	 * Returns the mean retention time of all the peaks stored in this peakset. This
	 * value can be -1 in the case of peaks from a 1D technology.
	 * 
	 * @return			The mean retention time.
	 */
	public double getMeanRetentionTime()
	{
		return meanretentiontime;
	}
	
	/**
	 * Returns the minimum mass of all the peaks stored in this peakset.
	 * 
	 * @return			The minimum mass.
	 */
	public double getMinMass()
	{
		return minmass;
	}
	
	/**
	 * Returns the maximum mass of all the peaks stored in this peakset.
	 * 
	 * @return			The maximum mass.
	 */
	public double getMaxMass()
	{
		return maxmass;
	}
	
	/**
	 * Returns the mean mass of all the peaks stored in this peakset.
	 * 
	 * @return			The mean mass.
	 */
	public double getMeanMass()
	{
		return meanmass;
	}
	
	/**
	 * Returns the median mass of all the peaks stored in this peakset.
	 * 
	 * @return			The median mass.
	 */
	public double getMedianMass()
	{
		return medianmass;
	}
	
	/**
	 * Returns the minimum intensity of all the peaks stored in this peakset.
	 * 
	 * @return			The minimum intensity.
	 */
	public double getMinIntensity()
	{
		return minintensity;
	}
	
	/**
	 * Returns the maximum intensity of all the peaks stored in this peakset.
	 * 
	 * @return			The maximum intensity.
	 */
	public double getMaxIntensity()
	{
		return maxintensity;
	}
	
	/**
	 * Returns the mean intensity of all the peaks stored in this peakset.
	 * 
	 * @return			The mean intensity.
	 */
	public double getMeanIntensity()
	{
		return meanintensity;
	}
	
	/**
	 * Returns the median intensity of all the peaks stored in this peakset.
	 * 
	 * @return			The median intensity.
	 */
	public double getMedianIntensity()
	{
		return medianintensity;
	}
	
	/**
	 * Calculates the standard deviation on the mass.
	 * <p />
	 * Beware this is not a pre-calculated value, but is calculated by this
	 * method. As this is a reasonable heavy operation and is not used that
	 * often, this is the most optimal solution.
	 * 
	 * @return			The standard deviation of the mass.
	 */
	public double getMassStdDev()
	{
		if (peaks.size() == 1)
			return 0;
		double stddev = 0;
		for (IPeak peak : peaks)
			stddev += Math.pow(peak.getMass()-meanmass, 2);
		return Math.sqrt(stddev /= (peaks.size()-1));
	}
	
	/**
	 * Implementation of a binary search for the mass in the peak-list. The peaks are
	 * sorted on mass (small to big), so the binary search should be most efficient
	 * for looking for the required index. When the mass is outside the range of the
	 * peaks, the index -1 is returned. Otherwise the index of the peak, which is
	 * the last peak with a mass smaller or equal to the given mass.
	 * 
	 * @param mass		The mass to look for.
	 * @return			The index of the last element, which is smaller/equal to the given mass, -1 otherwise.
	 */
	public int indexOfMass(double mass)
	{
		if (peaks_sorted_mass.size() == 0)
			return -1;
		if (mass<peaks_sorted_mass.firstElement().getMass() || mass>peaks_sorted_mass.lastElement().getMass())
			return -1;
		
		if (mass>=peaks_sorted_mass.firstElement().getMass() && mass<peaks_sorted_mass.get(1).getMass())
			return 0;
		if (mass==peaks_sorted_mass.lastElement().getMass())
			return peaks_sorted_mass.size()-1;
		
		int index = peaks_sorted_mass.size() / 2;
		int end_index = peaks_sorted_mass.size() - 1;
		int begin_index = 0;
		while (true)
		{
			double current_mass = peaks_sorted_mass.get(index).getMass();
			
			int new_index;
			if (current_mass > mass)
			{
				new_index = (begin_index+index) / 2;
				end_index = index;
			}
			else
			{
				new_index = (end_index+index) / 2;
				begin_index = index;
			}
			
			current_mass = peaks_sorted_mass.get(new_index).getMass();
			double next_mass = peaks_sorted_mass.get(new_index+1).getMass();
			if (current_mass<=mass && next_mass>mass)
				return new_index;
			else
				index = new_index;
		}
	}
	
	/**
	 * Returns all the peaks in this peakset with a mass in the range
	 * mass-delta .. mass+delta. This method utilizes the binairy search
	 * algorithm implemented in {@link IPeakSet#indexOfMass(double)}, making the
	 * retrieval very speedy.
	 * 
	 * @param mass		The mass to look for.
	 * @param delta		The delta around the mass.
	 * @return			A vector with all the peaks in the mass range.
	 */
	public Vector<Type> getPeaksOfMass(double mass, double delta)
	{
		return getPeaksInMassRange(mass-delta, mass+delta);
	}
	
	/**
	 * Returns all the peaks in this peakset with a mass in the range
	 * minmass .. maxmass. This method utilizes the binairy search
	 * algorithm implemented in {@link IPeakSet#indexOfMass(double)}, making the
	 * retrieval very speedy.
	 * 
	 * @param minmass	The minimum mass to look for.
	 * @param maxmass	The maximum mass to look for.
	 * @return			A vector with all the peaks in the mass range.
	 */
	public Vector<Type> getPeaksInMassRange(double minmass, double maxmass)
	{
		Vector<Type> neighbourhood = new Vector<Type>();
		
		int i = 0;
		if (minmass > getMinMass())
			i = indexOfMass(minmass);
		
		// check whether we are out of bounds
		if (i==-1 || i==peaks_sorted_mass.size())
			return neighbourhood;
		
		// add all the peaks which comply to the neighbourhood
		if (peaks_sorted_mass.get(i).getMass() < minmass)
			i++;
		while (i<peaks_sorted_mass.size() && peaks_sorted_mass.get(i).getMass()<maxmass)
			neighbourhood.add(peaks_sorted_mass.get(i++));
		
		return neighbourhood;
	}
	
	/**
	 * Implementation of a binary search for a scan in the peak-list. The peaks are
	 * sorted on scan (small to big), so the binary search should be most efficient
	 * for looking for the required index. When the scan is outside the range of the
	 * peaks, the index -1 is returned. Otherwise the index of the peak, which is
	 * the last peak with a scan smaller or equal to the given scan.
	 * 
	 * @param scanid	The scan to look for.
	 * @return			The index of the peak to look for.
	 */
	public int indexOfScan(int scanid)
	{
		if (peaks_sorted_scanid == null)
		{
			peaks_sorted_scanid = (Vector<Type>) peaks.clone();
			Collections.sort(peaks_sorted_scanid, IPeak.sort_scanid_ascending);
		}
		
		
		if (peaks_sorted_scanid.size() == 0)
			return -1;
		if (scanid<peaks_sorted_scanid.firstElement().getScanID() || scanid>peaks_sorted_scanid.lastElement().getScanID())
			return -1;
		
		if (scanid>=peaks_sorted_scanid.firstElement().getScanID() && scanid<peaks_sorted_scanid.get(1).getScanID())
			return 0;
		if (scanid==peaks_sorted_scanid.lastElement().getScanID())
			return peaks_sorted_scanid.size()-1;
		
		int index = peaks_sorted_scanid.size() / 2;
		int end_index = peaks_sorted_scanid.size() - 1;
		int begin_index = 0;
		while (true)
		{
			double current_scanid = peaks_sorted_scanid.get(index).getScanID();
			
			int new_index;
			if (current_scanid > scanid)
			{
				new_index = (begin_index+index) / 2;
				end_index = index;
			}
			else
			{
				new_index = (end_index+index) / 2;
				begin_index = index;
			}
			
			current_scanid = peaks_sorted_scanid.get(new_index).getScanID();
			double next_scanid = peaks_sorted_scanid.get(new_index+1).getScanID();
			if (current_scanid<=scanid && next_scanid>scanid)
				return new_index;
			else
				index = new_index;
		}
	}
	
	/**
	 * Returns all the peaks in the given scan-range. The method utilizes the binary
	 * search algorithm implement in {@link IPeakSet#indexOfScan(int)} for optimal
	 * searching.
	 * 
	 * @param minscan	The scan where to start collecting peaks.
	 * @param maxscan	The scan where to end collecting peaks.
	 * @return			All peaks in the scan range.
	 */
	public Vector<Type> getPeaksInScanRange(int minscan, int maxscan)
	{
		if (peaks_sorted_scanid == null)
		{
			peaks_sorted_scanid = (Vector<Type>) peaks.clone();
			Collections.sort(peaks_sorted_scanid, IPeak.sort_scanid_ascending);
		}
		
		
		Vector<Type> neighbourhood = new Vector<Type>();
		
		int i = 0;
		if (minscan > getMinScanID())
			i = indexOfScan(minscan);
		
		// check whether we are out of bounds
		if (i==-1 || i==peaks_sorted_scanid.size())
			return neighbourhood;
		
		// add all the peaks which comply to the neighbourhood
		if (peaks_sorted_scanid.get(i).getScanID() < minscan)
			i++;
		while (i<peaks_sorted_scanid.size() && peaks_sorted_scanid.get(i).getScanID()<maxscan)
			neighbourhood.add(peaks_sorted_scanid.get(i++));
		
		return neighbourhood;
	}
	
	/**
	 * Implementation of a binary search for a retention time in the peak-list. The peaks are
	 * sorted on retention time (small to big), so the binary search should be most efficient
	 * for looking for the required index. When the retention time is outside the range of the
	 * peaks, the index -1 is returned. Otherwise the index of the peak, which is
	 * the last peak with a retention time smaller or equal to the given retention time.
	 * 
	 * @param rt		The retention time to look for.
	 * @return			The index of the peak to look for.
	 */
	public int indexOfRetentionTime(double rt)
	{
		if (peaks_sorted_retentiontime == null)
		{
			peaks_sorted_retentiontime = (Vector<Type>) peaks.clone();
			Collections.sort(peaks_sorted_retentiontime, IPeak.sort_retentiontime_ascending);
		}
		
		
		if (peaks_sorted_retentiontime.size() <= 1)
			return -1;
		if (rt<peaks_sorted_retentiontime.firstElement().getRetentionTime() || rt>peaks_sorted_retentiontime.lastElement().getRetentionTime())
			return -1;
		
		if (rt>=peaks_sorted_retentiontime.firstElement().getRetentionTime() && rt<peaks_sorted_retentiontime.get(1).getRetentionTime())
			return 0;
		if (rt==peaks_sorted_retentiontime.lastElement().getRetentionTime())
			return peaks_sorted_retentiontime.size()-1;
		
		int index = peaks_sorted_retentiontime.size() / 2;
		int end_index = peaks_sorted_retentiontime.size() - 1;
		int begin_index = 0;
		while (true)
		{
			double current_rt = peaks_sorted_retentiontime.get(index).getRetentionTime();
			
			int new_index;
			if (current_rt > rt)
			{
				new_index = (begin_index+index) / 2;
				end_index = index;
			}
			else
			{
				new_index = (end_index+index) / 2;
				begin_index = index;
			}
			
			current_rt = peaks_sorted_retentiontime.get(new_index).getRetentionTime();
			double next_rt = peaks_sorted_retentiontime.get(new_index+1).getRetentionTime();
			if (current_rt<=rt && next_rt>rt)
				return new_index;
			else
				index = new_index;
		}
	}
	
	/**
	 * Returns all the peaks in the given retention time range. The method utilizes the binary
	 * search algorithm implement in {@link IPeakSet#indexOfRetentionTime(double)} for optimal
	 * searching.
	 * 
	 * @param minrt		The retention time where to start collecting peaks.
	 * @param maxrt		The retention time where to end collecting peaks.
	 * @return			All peaks in the retention time range.
	 */
	public Vector<Type> getPeaksInRetentionTimeRange(double minrt, double maxrt)
	{
		if (peaks_sorted_retentiontime == null)
		{
			peaks_sorted_retentiontime = (Vector<Type>) peaks.clone();
			Collections.sort(peaks_sorted_retentiontime, IPeak.sort_retentiontime_ascending);
		}
		
		
		Vector<Type> neighbourhood = new Vector<Type>();
		
		int i = 0;
		if (minrt > getMinRetentionTime())
			i = indexOfRetentionTime(minrt);
		
		// check whether we are out of bounds
		if (i==-1 || i==peaks_sorted_retentiontime.size())
			return neighbourhood;
		
		// add all the peaks which comply to the neighbourhood
		if (peaks_sorted_retentiontime.get(i).getRetentionTime() < minrt)
			i++;
		while (i<peaks_sorted_retentiontime.size() && peaks_sorted_retentiontime.get(i).getRetentionTime()<maxrt)
			neighbourhood.add(peaks_sorted_retentiontime.get(i++));
		
		return neighbourhood;
	}
	
	/**
	 * This method sets the pattern id of the peaks stored in this peakset to the given
	 * id.
	 * 
	 * @param id		The id for the peaks in the set.
	 */
	public void resetPatternIDs(int id)
	{
		for (Type peak : peaks)
			peak.setPatternID(id);
	}
	
	// should be used for debugging only
	public void setSourcePeakset(int peaksetIndex)
	{
		int peakId = 0;
		for (Type peak : peaks) {
			peak.addAnnotation(Annotation.sourcePeakset, peaksetIndex);
			peak.addAnnotation(Annotation.peakId, peakId);
			peakId++;
		}
	}
	
	
	// Collection overrides
	/**
	 * Returns the number of peaks in this peakset.
	 * 
	 * @return		The number of peaks in this peakset.
	 */
	public int size()
	{
		return peaks.size();
	}
	
	/**
	 * Returns the peak at the given position. When the position is smaller than
	 * 0 or larger or equal to {@link IPeakSet#size()} an {@link IndexOutOfBoundsException}
	 * is thrown.
	 * 
	 * @param pos	The position in the list of the peak to be retrieved.
	 * @return		The peak at position pos.
	 * @throws IndexOutOfBoundsException
	 * 				When the given position is smaller than 0 or larger or equal to {@link IPeakSet#size()}.
	 */
	public Type get(int pos) throws IndexOutOfBoundsException
	{
		return peaks.get(pos);
	}
	
	/**
	 * Returns the list of peaks stored in this peakset. The reference is to
	 * the vector stored in the peakset and should not be altered.
	 * 
	 * @return		A vector containing all the peaks stored in this peakset.
	 */
	public final Vector<Type> getPeaks()
	{
		return peaks;
	}
	
	
	// Iterable overrides
	/** {@link Iterable#iterator()} */
	public Iterator<Type> iterator()
	{
		return peaks.iterator();
	}
	
	
	// IPeak overrides
	@Override
	public double getMass()
	{
		return meanmass;
	}

	@Override
	public void setMass(double mass)
	{
	}

	@Override
	public double getIntensity()
	{
		return maxintensity;
	}

	@Override
	public void setIntensity(double intensity)
	{
	}

	@Override
	public int getScanID()
	{
		return meanscanid;
	}

	@Override
	public void setScanID(int scan)
	{
	}
	
	@Override
	public double getRetentionTime()
	{
		return meanretentiontime;
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
		measurementid = id;
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
		StringBuffer str = new StringBuffer();
		
		str.append(measurementid);
		str.append(patternid);
		for (Type peak : peaks)
			str.append(peak.getSha1Data());
		
		return str.toString().getBytes();
	}
	
	@Override
	public Signal getSignal()
	{
		if (container_class.equals(MassChromatogram.class))
		{
			double maxintensity = getMaxIntensity();
			
			double minrt = Double.MAX_VALUE;
			double maxrt = Double.MIN_VALUE;
			for (MassChromatogram masschromatogram : (Vector<MassChromatogram>) peaks)
			{
				minrt = Math.min(minrt, masschromatogram.getMinRetentionTime());
				maxrt = Math.max(maxrt, masschromatogram.getMaxRetentionTime());
			}
			
			int length = (int) (maxrt - minrt + 1);
			double xvals[] = new double[length];
			double yvals[] = new double[length];
			for (int i=0; i<length; ++i)
			{
				xvals[i] = minrt + i;
				yvals[i] = 0;
			}
			
			double weights = 0;
			for (MassChromatogram masschromatogram : (Vector<MassChromatogram>) peaks)
			{
				double weight = masschromatogram.getIntensity()/maxintensity;
				weights += weight;
				
				Signal signal = masschromatogram.getSignal();
				for (double rt=minrt; rt<maxrt; ++rt)
					yvals[(int) (rt-minrt)] += weight * signal.getY(rt);
			}
			
			for (int i=0; i<length; ++i)
				yvals[i] /= weights;
			
			return new Signal(xvals, yvals);
		}
		else	// revert to default behaviour
		{
			double minx = Double.MAX_VALUE;
			double maxx = Double.MIN_VALUE;
			
			int nrpeaks = peaks.size();
			Signal signals[] = new Signal[nrpeaks];
			for (int i=0; i<nrpeaks; ++i)
			{
				IPeak peak = peaks.get(i);
				signals[i] = peak.getSignal();
				
				minx = Math.min(minx, signals[i].getMinX());
				maxx = Math.max(maxx, signals[i].getMaxX());
			}
			
			int length = (int) ((maxx-minx) + 1);
			double xvals[] = new double[length];
			double yvals[] = new double[length];
			for (int i=0; i<length; ++i)
			{
				xvals[i] = minx + i;
				yvals[i] = 0;
			}
			
			for (Signal signal : signals)
			{
				for (double x=minx; x<maxx; ++x)
					yvals[(int) (x-minx)] += signal.getY(x);
			}
			
			for (int i=0; i<length; ++i)
				yvals[i] /= nrpeaks;
			
			return new Signal(xvals, yvals);
		}
	}
	
	
	// data
	protected Class<? extends IPeak> container_class = null;
	
	protected Vector<Type> peaks = new Vector<Type>();
	protected Vector<Type> peaks_sorted_mass = new Vector<Type>();
	protected Vector<Type> peaks_sorted_scanid = null;
	protected Vector<Type> peaks_sorted_retentiontime = null;
	
	protected int patternid = -1;
	protected int measurementid = -1;
	
	protected int minscanid;
	protected int maxscanid;
	protected int meanscanid;
	protected double minretentiontime;
	protected double maxretentiontime;
	protected double meanretentiontime;
	protected double minmass;
	protected double maxmass;
	protected double minintensity;
	protected double maxintensity;
	
	protected double meanmass;
	protected double medianmass;
	protected double meanintensity;
	protected double medianintensity;
}
