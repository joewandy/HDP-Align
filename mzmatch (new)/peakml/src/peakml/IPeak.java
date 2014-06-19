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
import java.security.*;

// peakml
import peakml.io.*;
import peakml.util.*;
import peakml.util.Base64;
import peakml.math.*;
import peakml.chemistry.*;





/**
 * The base-class for all peak-types defined in the PeakML file format. This class
 * defines the properties common to all the different types of peaks: mass, intensity,
 * and optional scanid. The access methods to these properties have been defined
 * as abstract and thus need to be overridden by the sub-classes.
 * <p />
 * It was chosen to make this an abstract class instead of an interface to allow for
 * the central implementation of the annotation-scheme.
 */
public abstract class IPeak extends Annotatable
{
	// constructor
	protected IPeak() { ; }
	
	
	// overridable interface
	/**
	 * Retrieves the mass of this peak. For a single peak implementation this would be
	 * the real mass, but for a chromatographic peak for example this would consist of
	 * the mean of a cluster of peaks.
	 * 
	 * @return				The mass of the peak.
	 */
	public abstract double getMass();
	
	/**
	 * Sets the mass of this peak, for more information on mass see
	 * {@link IPeak#getMass()}. It is possible that the class implementing this interface
	 * does not support this method.
	 *  
	 * @param mass			The new mass for this peak.
	 */
	public abstract void setMass(double mass);
	
	/**
	 * Retrieves the measured intensity for this peak. For a single peak implementation this
	 * would be the real measured intensity, but for a chromatographic peak for example
	 * this would consist of the mean measured intensity for a cluster of peaks.
	 * 
	 * @return				The measured intensity of the peak.
	 */
	public abstract double getIntensity();
	
	/**
	 * Sets the intensity of this peak, for more information on mass see
	 * {@link IPeak#getIntensity()}. It is possible that the class implementing this interface
	 * does not support this method.
	 *  
	 * @param intensity		The new intensity for this peak.
	 */
	public abstract void setIntensity(double intensity);
	
	/**
	 * Retrieves the scan-id for this peak. For a single peak implementation this
	 * would be the scan at which it was measured, but for a chromatographic peak for
	 * example this would be the scan with the most intense peak.
	 * 
	 * @return				The scan at which the peak was measured.
	 */
	public abstract int getScanID();
	
	/**
	 * Sets the scan-id for this peak, for more information on scan-id see
	 * {@link IPeak#getScanID()}. It is possible that the class implementing this
	 * interface does not support this method.
	 * 
	 * @param scan			The new scan for this peak.
	 */
	public abstract void setScanID(int scan);
	
	/**
	 * Retrieves the retention time for this peak. The retention time is closely
	 * linked to the scan-id, which is the index or order at which a particular
	 * measurement was made. The time is expressed in seconds (all parsers make
	 * sure the time-value stored in a file is converted to this convention) and
	 * can easily be converted into minutes and seconds with:
	 * <pre>
	 * double rt = peak.getRetentionTime();
	 * int minutes = (int) (rt / 60);
	 * int seconds = (int) (rt % 60);
	 * </pre>
	 * 
	 * @return				The retention time at which the peak was measured.
	 */
	public abstract double getRetentionTime();
	
	/**
	 * Sets the retention time for this peak, for more information on retention
	 * time see {@link IPeak#setRetentionTime(double)}. It is possible that the class
	 * implementing this interface does not support this method.
	 * 
	 * @param retentiontime	The new retention time for this peak.
	 */
	public abstract void setRetentionTime(double retentiontime);
	
	/**
	 * Returns an optional measurement-id for this peak. This is a convenience mechanism
	 * for locating the measurement-information for this peak in the {@link Header}.
	 * 
	 * @return				The profile-id of this peak.
	 */
	public abstract int getMeasurementID();
	
	/**
	 * Sets the profile-id of this peak, for more information on mass see
	 * {@link IPeak#getMeasurementID()}. It is possible that the class implementing this
	 * interface does not support this method.
	 *  
	 * @param id			The new profile-id for this peak.
	 */
	public abstract void setMeasurementID(int id);
	
	/**
	 * Returns an optional id for the peak. This id can for example be used for peak
	 * sequence identification. Beware that this mechanism is highly volatile and can
	 * only be used in a single-threaded environment within a strictly controlled
	 * environment like a single function or a completely controlled application.
	 * 
	 * @return				The id of this peak.
	 */
	public abstract int getPatternID();
	
	/**
	 * Sets the pattern-id of this peak, for more information on mass see
	 * {@link IPeak#getPatternID()}. It is possible that the class implementing this
	 * interface does not support this method.
	 *  
	 * @param id			The new id for this peaks
	 */
	public abstract void setPatternID(int id);
	
	/**
	 * Returns the byte-data required to perform the sha1-sum. The more complex
	 * IPeak types can call this method to collect all data from the peaks they
	 * combine.
	 * 
	 * @return				The byte-data for the sha1-sum.
	 */
	protected abstract byte[] getSha1Data();
	
	/**
	 * Calculates the signal for this IPeak object. For a single data-point this
	 * will be a signal of one element. For a collection of IPeaks it will be
	 * a compounded version.
	 * 
	 * @return				The calculated signal.
	 */
	public abstract Signal getSignal();
	
	
	// sha1 digest
	/**
	 * Calculates the SHA-1 hash for this IPeak instance. The SHA-1 hash is used
	 * to verify that individual peaks have correctly been read from a storage point
	 * (e.g. a file). The SHA-1 hash is a string, which can be compared with
	 * {@link String#equals(Object)}.
	 * 
	 * @return				The sha1-hash for this peak.
	 */
	public String sha1()
	{
		return Base64.encodeBytes(hash.digest(getSha1Data()));
	}
	
	
	// list interface
	/**
	 * Locates the closest matching peak on the given mass from the list of peaks. This operation
	 * is performed often and this provides a convenient access point.
	 * 
	 * @param peaks			The list of peaks.
	 * @param mass			The mass to optimize to.
	 * @return				The best match (null if the list was empty).
	 */
	public static IPeak getBestPeakOnMass(Vector<IPeak> peaks, double mass)
	{
		if (peaks.size() == 0)
			return null;
		
		int index = 0;
		IPeak bestpeak = null;
		double bestmassdiff = Double.MAX_VALUE;
		do
		{
			IPeak peak = peaks.get(index++);
			double massdiff = Math.abs(mass-peak.getMass());
			if (massdiff < bestmassdiff)
			{
				bestpeak = peak;
				bestmassdiff = massdiff;
			}
		} while (index<peaks.size());
		
		return bestpeak;
	}
	
	/**
	 * Locates the closest matching peak on the given retention time from the list of peaks. This operation
	 * is performed often and this provides a convenient access point.
	 * 
	 * @param peaks			The list of peaks.
	 * @param rt			The retention time to optimize to.
	 * @return				The best match (null if the list was empty).
	 */
	public static IPeak getBestPeakOnRT(Vector<IPeak> peaks, double rt)
	{
		if (peaks.size() == 0)
			return null;
		
		int index = 0;
		IPeak bestpeak = null;
		double bestrtdiff = Double.MAX_VALUE;
		do
		{
			IPeak peak = peaks.get(index++);
			double rtdiff = Math.abs(rt-peak.getRetentionTime());
			if (rtdiff < bestrtdiff)
			{
				bestpeak = peak;
				bestrtdiff = rtdiff;
			}
		} while (index<peaks.size());
		
		return bestpeak;
	}
	
	/**
	 * Recursive function, which unpacks all the of the IPeakSet lists and returns a vector
	 * with all the non-list instances contained in the given peak.
	 * 
	 * @param peak			The peak to unpack.
	 * @return				The list with all the non-list instances.
	 */
	@SuppressWarnings("unchecked")
	public static Vector<IPeak> unpack(IPeak peak)
	{
		Vector<IPeak> result = new Vector<IPeak>();
		if (peak.getClass().equals(IPeakSet.class))
		{
			for (IPeak p : (IPeakSet<IPeak>) peak)
				result.addAll(unpack(p));
		}
		else
			result.add(peak);
		return result;
	}
	
	/**
	 * Returns the peaks with one of the given list of measurement id's
	 * 
	 * @param peaks				The list with the peaks
	 * @param measurementids	The list with the measurementid's
	 * @return					The filtered list
	 */
	public static Vector<IPeak> peaksOfMeasurements(Vector<IPeak> peaks, Vector<Integer> measurementids)
	{
		Vector<IPeak> filtered = new Vector<IPeak>();
		for (IPeak p : peaks)
		{
			for (int measurementid : measurementids)
				if (p.getMeasurementID() == measurementid)
				{
					filtered.add(p);
					break;
				}
		}
		return filtered;
	}
	
	
	// relatedness interface
	/** This string is the label for the annotation of relation-id's used. */
	public static final String relationid = "relation.id";
	
	/**
	 * This interface defines the setup for comparing the relatedness of two peaks. The
	 * callback method correlation has been so named, because the more two peaks correlate
	 * with each other, the more related they are. The method
	 * {@link IPeak#findRelatedPeaks(Vector, double, double, double, peakml.IPeak.RelationCompare)} tries
	 * to find all the peaks that correlate well enough (i.e. are above a threshold) with
	 * a base peak.
	 * 
	 * @param <Type>	The type of peak to compare.
	 */
	public interface RelationCompare<Type>
	{
		// TODO
		// We would like to make Type at least IPeak or something which extends IPeak.
		// However, if we use <? extends IPeak> the method match (below) will not
		// compile as this is working with IPeak and the compiler expects something
		// which is at least a child of IPeak.
		/**
		 * This function is offered two peaks to compare. When -1 is returned the peaks
		 * are considered incomparable and will never be related. The higher the value
		 * the more related the two peaks are.
		 * 
		 * @param peak1		The first peak.
		 * @param peak2		The second peak.
		 * @return			The correlation between the two peaks.
		 */
		public boolean related(Type peak1, Type peak2);
	}
	
	/**
	 * This method attempts to find all the peaks that are related to each other. Typical
	 * examples of this are isotopes, double charged, etc. and occur very frequently in
	 * mass spectrometry measurements. The process can be specialized with the
	 * parameters, of which the compare is the most important. The interface
	 * {@link IPeak.MatchCompare} allows the user to receive two {@link IPeak} instances
	 * and calculate the correlation of the two being related. As this is a correlation
	 * 1 is the maximum expected value, which indicates that the two peaks are closely
	 * related. When -1 is returned the two peaks are not considered for relation.
	 * The returned vector contains all the basepeaks.
	 * <p />
	 * The relationship is returned as an annotation in the peaks with label {@link IPeak#relationid}.
	 * The most abundant one is marked as the base-peak and all related peaks follow it in
	 * the list and have the same id.
	 * 
	 * @param peaks				The vector with all the peaks.
	 * @param minrt				The minimum RT before starting the matching (eg everything with a lower RT will be ignored).
	 * @param mincorrelation	The minimal correlation to be achieved.
	 * @param rtdelta			The maximum time two peaks can differ to be taken into acount.
	 * @param compare			Instance of a class implementing the {@link IPeak.MatchCompare} interface.
	 * @return					The base peaks.
	 */
	public static Vector<IPeak> findRelatedPeaks(Vector<IPeak> peaks, double minrt, double rtwindow, RelationCompare<IPeak> compare)
	{
		// TODO decreasing correlation for decreasing intensities
		// TODO when a peak has been assigned to someone else, re-check the correlation and claim when it's better
		
		// sort the peaks and reset the pattern-id to -1
		Collections.sort(peaks, IPeak.sort_intensity_descending);
		for (IPeak peak : peaks)
			peak.removeAnnotation(relationid);
		
		// create a peakset so we can quickly search on scanid
		IPeakSet<IPeak> sorted_peaks = new IPeakSet<IPeak>(peaks);
		
		// traverse all the peaks
		int id = 0;
		Vector<IPeak> basepeaks = new Vector<IPeak>();
		for (IPeak peak1 : peaks)
		{
			// check whether this peak was already processed
			if (peak1.getAnnotation(relationid) != null)
				continue;
			peak1.addAnnotation(relationid, Integer.toString(id), Annotation.ValueType.INTEGER);
			
			// check whether this peak is bigger than the minrt
			if (minrt!=-1 && minrt>peak1.getRetentionTime())
			{
				basepeaks.add(peak1);
				continue;
			}
			
			basepeaks.add(peak1);
			
			// process all peaks in the scan neighbourhood
			for (IPeak peak2 : sorted_peaks.getPeaksInRetentionTimeRange(peak1.getRetentionTime()-rtwindow, peak1.getRetentionTime()+rtwindow))
			{
				// check whether this peak was already processed
				if (peak2.getAnnotation(relationid) != null)
					continue;
				
				// see whether the two are related
				if (compare.related(peak1, peak2))
					peak2.addAnnotation(relationid, Integer.toString(id), Annotation.ValueType.INTEGER);
			}
			
			id++;
		}
		
		// sort the peaks so the related ones are following
		Collections.sort(peaks, new AnnotationAscending(relationid));
		
		// return the basepeaks
		return basepeaks;
	}
	
	
	// matching interface
	/**
	 * This interface defines the setup for the comparison of peaks from two datasets, in
	 * order to determine whether they were caused by the same molecule. The method distance
	 * has so been called because the method {@link IPeak#match(Vector, double, peakml.IPeak.MatchCompare)}
	 * attempts minimize the distance between two peaks. This means that the returned
	 * value should be something like a distance.
	 * 
	 * @param <Type>	The type of peak to compare.
	 */
	public interface MatchCompare<Type>
	{
		// TODO
		// We would like to make Type at least IPeak or something which extends IPeak.
		// However, if we use <? extends IPeak> the method match (below) will not
		// compile as this is working with IPeak and the compiler expects something
		// which is at least a child of IPeak.
		/**
		 * The function to implement for the calculation of the distance between two
		 * peaks. Any distance can apply, such as the distance in scans or more
		 * advanced algorithms as are needed. The resulting distance must always be
		 * positive, as a negative value causes the algorithm to discard this match.
		 * <p />
		 * When -1 is returned the two peaks are considered incomparable and will never
		 * be linked.
		 * 
		 * @param peak1		The first peak.
		 * @param peak2		The second peak.
		 * @return			A positive value defining the distance between the two peaks.
		 */
		public double distance(Type peak1, Type peak2);
	}
	
	/**
	 * A ready to-go implementation of the match-function, which works on IPeak. The
	 * match-compare function is set-up to optimize on the distance between retention time of
	 * potential matches. This is the most simple of cases and when more information
	 * is available should not be used. This function makes an anonymous implementation
	 * of {@link IPeak#match(Vector, double, peakml.IPeak.MatchCompare)}.
	 * 
	 * @param samples	Vector containing all the spectra to be matched
	 * @param ppm		The ppm-value to make the match on
	 * @return			Vector of IPeakSet's containing matched peaks
	 */
	@SuppressWarnings("unchecked")
	public static Vector<IPeakSet<IPeak>> match(Vector<IPeakSet<IPeak>> samples, double ppm)
	{
		return (Vector) match((Vector) samples, ppm, new MatchCompare<IPeak>() {
				public double distance(IPeak peak1, IPeak peak2) {
					return Math.abs(peak1.getRetentionTime() - peak2.getRetentionTime());
				}
			});
	}
	
	/**
	 * General function for matching peaks together. The function works by minimizing the
	 * distance between two peaks as calculated by the pluggable MatchCompare instance. The
	 * peaks are passed as peaksets (i.e. the peaks of one set are collected in a single
	 * peakset). The vector contains all the datasets and will treated as such.
	 * <p />
	 * Keep track of where a peak came from with the measurementid found in each peak.
	 * 
	 * @param samples		The samples to compare.
	 * @param ppm			The parts-per-milion the masses can differ at most.
	 * @param compare		The comparator for comparing two peaks.
	 * @return				A vector with all the matched peaks.
	 */
	@SuppressWarnings("unchecked")
	public static Vector<IPeakSet<IPeak>> match(Vector<IPeakSet<IPeak>> samples, double ppm, MatchCompare compare)
	{
		final int UNPROCESSED = 0;
		final int MATCHED = 1;
		
		
		// create a vector of mass-chromatogram sets sorted on intensity
		Vector<Vector<IPeak>> masschromatogramdata = new Vector<Vector<IPeak>>();
		for (IPeakSet<? extends IPeak> sample : samples)
		{
			// assign the UNPROCESSED id to all the mass chromatograms
			sample.resetPatternIDs(UNPROCESSED);
			
			// make a copy of the peak-vector and sort ascending
			Vector<IPeak> masschromatograms = new Vector<IPeak>(sample.getPeaks());
			Collections.sort(masschromatograms, IPeak.sort_intensity_descending);
			
			// add the new vector to the list
			masschromatogramdata.add(masschromatograms);
		}
		
		// create an array with the current index for each sorted mass-chromatogram set
		int nrsamples = samples.size();
		int indices[] = new int[nrsamples];
		for (int i=0; i<nrsamples; ++i)
			indices[i] = 0;
		
		// try to match everything
		Vector<IPeakSet<IPeak>> matchdata = new Vector<IPeakSet<IPeak>>();
		while (true)
		{
			// find the most intense mass chromatogram
			int refid = -1;
			double refintensity = -1;
			IPeak refmasschromatogram = null;
			for (int id=0; id<nrsamples; ++id)
			{
				Vector<IPeak> masschromatograms = masschromatogramdata.get(id);
				
				// locate the current index of this sample (e.g. the first unmatched mass chromatogram)
				while (indices[id]<masschromatograms.size() && masschromatograms.get(indices[id]).getPatternID()!=UNPROCESSED)
					indices[id]++;
				if (indices[id] == masschromatograms.size())
					continue;
				
				// 
				// TODO check if valid ??
				IPeak masschromatogram = masschromatograms.get(indices[id]);
				if (refid==-1 || masschromatogram.getIntensity()>refintensity)
				{
					refid = id;
					refintensity = masschromatogram.getIntensity();
					refmasschromatogram = masschromatogram;
				}
			}
			
			// check whether everything has been processed
			if (refmasschromatogram == null)
				break;
			
			// mark the reference mass chromatogram as matched
			refmasschromatogram.setPatternID(MATCHED);
			
			// retrieve the properties of the reference
			double refmass = refmasschromatogram.getMass();
			double refdelta = PeriodicTable.PPM(refmass, ppm);
			
			// match this mass chromatogram to those in the other samples
			Vector<IPeak> matches = new Vector<IPeak>();
			for (int id=0; id<nrsamples; ++id)
			{
				if (id == refid)
				{
					matches.add(refmasschromatogram);
					continue;
				}
				
				// retrieve the spectrum
				IPeakSet<? extends IPeak> masschromatograms = samples.get(id);
				
				// retrieve all the masschromatograms with ~ equal mass as the current refmasschromatogram
				Vector<? extends IPeak> neighbourhood = masschromatograms.getPeaksOfMass(refmass, refdelta);
				
				// select the closest in scan ??
				double bestdistance = -1;
				IPeak bestmasschromatogram = null;
				for (IPeak masschromatogram : neighbourhood)
				{
					if (masschromatogram.getPatternID() != UNPROCESSED)
						continue;
					
					double distance = compare.distance(refmasschromatogram, masschromatogram);
					if (distance < 0)
						continue;
					
					if (bestmasschromatogram==null || distance<bestdistance)
					{
						bestdistance = distance;
						bestmasschromatogram = masschromatogram;
					}
				}
				
				if (bestmasschromatogram != null)
				{
					matches.add(bestmasschromatogram);
					bestmasschromatogram.setPatternID(MATCHED);
				}
			}
			
			matchdata.add(new IPeakSet<IPeak>(matches));
		}
		
		return matchdata;
	}
	
	
	// sorting interface
	protected static class MassAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getMass() < arg1.getMass())
				return -1;
			else
			if (arg0.getMass() > arg1.getMass())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending mass. */
	public static MassAscending sort_mass_ascending = new MassAscending();
	
	protected static class MassDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getMass() < arg1.getMass())
				return 1;
			else
			if (arg0.getMass() > arg1.getMass())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending mass. */
	public static MassDescending sort_mass_descending = new MassDescending();
	
	protected static class IntensityAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getIntensity() < arg1.getIntensity())
				return -1;
			else
			if (arg0.getIntensity() > arg1.getIntensity())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending intensity. */
	public static IntensityAscending sort_intensity_ascending = new IntensityAscending();
	
	protected static class IntensityDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getIntensity() < arg1.getIntensity())
				return 1;
			else
			if (arg0.getIntensity() > arg1.getIntensity())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending intensity. */
	public static IntensityDescending sort_intensity_descending = new IntensityDescending();
	
	protected static class MeasurementIDAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getMeasurementID() < arg1.getMeasurementID())
				return -1;
			else
			if (arg0.getMeasurementID() > arg1.getMeasurementID())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending scan-id. */
	public static MeasurementIDAscending sort_measurementid_ascending = new MeasurementIDAscending();
	
	protected static class MeasurementIDDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getMeasurementID() < arg1.getMeasurementID())
				return 1;
			else
			if (arg0.getMeasurementID() > arg1.getMeasurementID())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending scan-id. */
	public static MeasurementIDDescending sort_measurementid_descending = new MeasurementIDDescending();
	
	protected static class ScanIDAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getScanID() < arg1.getScanID())
				return -1;
			else
			if (arg0.getScanID() > arg1.getScanID())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending scan-id. */
	public static ScanIDAscending sort_scanid_ascending = new ScanIDAscending();
	
	protected static class ScanIDDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getScanID() < arg1.getScanID())
				return 1;
			else
			if (arg0.getScanID() > arg1.getScanID())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending scan-id. */
	public static ScanIDDescending sort_scanid_descending = new ScanIDDescending();
	
	protected static class RetentionTimeAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getRetentionTime() < arg1.getRetentionTime())
				return -1;
			else
			if (arg0.getRetentionTime() > arg1.getRetentionTime())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending retention time. */
	public static RetentionTimeAscending sort_retentiontime_ascending = new RetentionTimeAscending();
	
	protected static class RetentionTimeDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getRetentionTime() < arg1.getRetentionTime())
				return 1;
			else
			if (arg0.getRetentionTime() > arg1.getRetentionTime())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending retention time. */
	public static RetentionTimeDescending sort_retentiontime_descending = new RetentionTimeDescending();
	
	protected static class ProfileIDAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getMeasurementID() < arg1.getMeasurementID())
				return -1;
			else
			if (arg0.getMeasurementID() > arg1.getMeasurementID())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending profile-id. */
	public static ProfileIDAscending sort_profileid_ascending = new ProfileIDAscending();
	
	protected static class ProfileIDDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getMeasurementID() < arg1.getMeasurementID())
				return 1;
			else
			if (arg0.getMeasurementID() > arg1.getMeasurementID())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending profile-id. */
	public static ProfileIDDescending sort_profileid_descending = new ProfileIDDescending();
	
	protected static class PatternIDAscending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getPatternID() < arg1.getPatternID())
				return -1;
			else
			if (arg0.getPatternID() > arg1.getPatternID())
				return 1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on ascending pattern-id. */
	public static PatternIDAscending sort_patternid_ascending = new PatternIDAscending();
	
	protected static class PatternIDDescending implements Comparator<IPeak>
	{
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.getPatternID() < arg1.getPatternID())
				return 1;
			else
			if (arg0.getPatternID() > arg1.getPatternID())
				return -1;
			else
				return 0;
		}
	}
	/** Comparator sorting a set of IPeak on descending pattern-id. */
	public static PatternIDDescending sort_patternid_descending = new PatternIDDescending();

	/**
	 * Comparator for sorting on a vector of peaks on an annotations. The comparator
	 * has knowledge of the basic types {@link Annotation.ValueType#INTEGER} and
	 * {@link Annotation.ValueType#DOUBLE}. The string representation of these
	 * types is automatically converted and used to sort.
	 */
	public static class AnnotationAscending implements Comparator<IPeak>
	{
		/**
		 * Constructs a new instance, which sorts on the given label.
		 * 
		 * @param label		The label of the annotation to sort on.
		 */
		public AnnotationAscending(String label)
		{
			this.label = label;
		}
		
		/**
		 * Compare function, which converts the data in the {@link IPeak} instances
		 * to comparable data and sorts accordingly.
		 * 
		 * @param arg0		The first argument.
		 * @param arg1		The second argument.
		 */
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.annotations==null || arg1.annotations==null)
				return 0;
			
			Annotation annotation0 = arg0.annotations.get(label);
			Annotation annotation1 = arg1.annotations.get(label);
			if (annotation0==null || annotation1==null)
				return 0;
			
			Annotation.ValueType valuetype0 = annotation0.getValueType();
			Annotation.ValueType valuetype1 = annotation1.getValueType();
			if (valuetype0==Annotation.ValueType.INTEGER && valuetype1==Annotation.ValueType.INTEGER)
			{
				int value0 = Math.abs(annotation0.getValueAsInteger());
				int value1 = Math.abs(annotation1.getValueAsInteger());
				
				if (value0 < value1)
					return -1;
				else
				if (value0 > value1)
					return 1;
				else
					return 0;
			}
			else if (valuetype0==Annotation.ValueType.DOUBLE && valuetype1==Annotation.ValueType.DOUBLE)
			{
				double value0 = Math.abs(annotation0.getValueAsDouble());
				double value1 = Math.abs(annotation1.getValueAsDouble());
				
				if (value0 < value1)
					return -1;
				else
				if (value0 > value1)
					return 1;
				else
					return 0;
			}
			else
				return annotation0.getValueAsString().compareTo(annotation1.getValueAsString());
		}
		
		protected String label;
	}
	
	/**
	 * Comparator for sorting on a vector of peaks on an annotations. The comparator
	 * has knowledge of the basic types {@link Annotation.ValueType#INTEGER} and
	 * {@link Annotation.ValueType#DOUBLE}. The string representation of these
	 * types is automatically converted and used to sort.
	 */
	public static class AnnotationDescending implements Comparator<IPeak>
	{
		/**
		 * Constructs a new instance, which sorts on the given label.
		 * 
		 * @param label		The label of the annotation to sort on.
		 */
		public AnnotationDescending(String label)
		{
			this.label = label;
		}
		
		/**
		 * Compare function, which converts the data in the {@link IPeak} instances
		 * to comparable data and sorts accordingly.
		 * 
		 * @param arg0		The first argument.
		 * @param arg1		The second argument.
		 */
		public int compare(IPeak arg0, IPeak arg1)
		{
			if (arg0.annotations==null || arg1.annotations==null)
				return 0;
			
			Annotation annotation0 = arg0.annotations.get(label);
			Annotation annotation1 = arg1.annotations.get(label);
			if (annotation0==null || annotation1==null)
				return 0;
			
			Annotation.ValueType valuetype0 = annotation0.getValueType();
			Annotation.ValueType valuetype1 = annotation1.getValueType();
			if (valuetype0==Annotation.ValueType.INTEGER && valuetype1==Annotation.ValueType.INTEGER)
			{
				int value0 = annotation0.getValueAsInteger();
				int value1 = annotation1.getValueAsInteger();
				
				if (value0 < value1)
					return 1;
				else
				if (value0 > value1)
					return -1;
				else
					return 0;
			}
			else if (valuetype0==Annotation.ValueType.DOUBLE && valuetype1==Annotation.ValueType.DOUBLE)
			{
				double value0 = annotation0.getValueAsDouble();
				double value1 = annotation1.getValueAsDouble();
				
				if (value0 < value1)
					return 1;
				else
				if (value0 > value1)
					return -1;
				else
					return 0;
			}
			else
				return annotation0.getValueAsString().compareTo(annotation1.getValueAsString());
		}
		
		protected String label;
	}
	
	
	// data
	
	
	// messy constructs for getting encoders
	protected static MessageDigest hash;
	static {
		try {
			hash = MessageDigest.getInstance("SHA1");
		} catch (Exception e) { e.printStackTrace(); }
	}
}
