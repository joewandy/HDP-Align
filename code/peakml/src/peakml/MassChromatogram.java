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
import peakml.math.filter.SavitzkyGolayFilter;





/**
 * Implementation of a mass chromatogram, usable for LC-MS, GC-MS, or any 2 dimensional
 * mass spectrometry method. This class maintains a list of peaks (either {@link Centroid}
 * or {@link Profile}), which make up the mass chromatogram. When a list of {@link Centroid}
 * is maintained the mass chromatogram is 2D, but if a list of {@link Profile} is
 * maintained the mass chromatogram is 3D. A mass chromatogram is expected to display
 * a normal or log-normal distribution.
 * <p />
 * The constructor of this class automatically calculates a number of properties, like:
 * mass, intensity and scan out of the peaks associated with the mass chromatogram. A
 * constructor is supplied, which allows the calculation of the mass with a different
 * method than the standard weighted mean, which assumes that mass measurements with
 * low intensity have a low accuracy.
 * 
 * @param <gPeak>	The type of peaks stored in this mass chromatogram.
 */
public class MassChromatogram<gPeak extends Peak> extends IPeak implements Iterable<gPeak>
{
	// interface for a different mass calculator
	/**
	 * In order to accommodate for a different calculation of the mass for a
	 * mass chromatogram, implement this interface. The peaks associated to
	 * the mass chromatogram are passed to the getMass function.
	 * 
	 * @param <Type>	The type of peaks offered to the calculator.
	 */
	public interface MassCalculator<Type extends Peak>
	{
		/**
		 * This method calculates the mass for the given peaks.
		 * 
		 * @param peaks		The peaks to calculate the mass from.
		 * @return			The calculated mass.
		 */
		public double getMass(Vector<Type> peaks);
	}
	
	/** Standard implementation of the mass calculator, which reduces memory requirements. */
	public static MassCalculator<Peak> standard_masscalculator = new MassCalculator<Peak>() {
		public double getMass(Vector<Peak> peaks)
		{
			// calculate the min/max intensity
			Collections.sort(peaks, IPeak.sort_intensity_ascending);
			
			double minintensity = peaks.firstElement().getIntensity();
			double maxintensity = peaks.lastElement().getIntensity();
			double intensityrange = maxintensity - minintensity;
			
			// calculate the weighted mean
			double n = 0;
			double meanmass = 0;
			for (Peak peak : peaks)
			{
				// the weight should end up between 1 and 10
				double weight = ((peak.getIntensity()-minintensity)/intensityrange) * 9 + 1;
				
				n += weight;
				meanmass += weight*peak.getMass();
			}
			return meanmass / n;
		}
	};
	
	
	// constructor(s)
	/**
	 * Constructs a new mass chromatogram instance out of the given set of
	 * peaks (either {@link Centroid} or {@link Profile}). The calculations of the properties
	 * are done in {@link MassChromatogram#MassChromatogram(PeakData, peakml.MassChromatogram.MassCalculator)},
	 * where the mass calculation has been implemented to calculate a weighted
	 * mean, where the intensity is used as the weight so that peaks of
	 * low intensity have a low impact on the mean. This makes sense as for
	 * most mass spectrometers it's assumed that a low intensity peak has a
	 * high deviation from the true mass.
	 * 
	 * @param peakdata	The {@link PeakData} instance containing the data of this mass chromatogram.
	 */
	@SuppressWarnings("unchecked")
	public MassChromatogram(PeakData<gPeak> peakdata)
	{
		this(peakdata, (MassCalculator<gPeak>) standard_masscalculator);
	}
	
	/**
	 * Constructs a new mass chromatogram instance out of the given set of
	 * peaks (either {@link Centroid} or {@link Profile}). All the properties
	 * associated to a mass chromatogram are calculated here. This means that
	 * min and max scan, min and max intensity, and the total intensity of
	 * the peak are calculated. The parameter mass can be used to
	 * plug a new mass calculation {@link MassChromatogram.MassCalculator}
	 * class, which can be suited to different needs than the standard
	 * weighted mean.
	 * 
	 * @param peakdata	The {@link PeakData} instance containing the data of this mass chromatogram.
	 * @param mass		An instance of {@link MassChromatogram.MassCalculator}
	 */
	public MassChromatogram(PeakData<gPeak> peakdata, MassCalculator<gPeak> mass)
	{
		// copy the peaks
		this.peakdata = peakdata;
		Vector<gPeak> peaks = peakdata.getPeaks();
		
		// calculate the min and max-mass
		Collections.sort(peaks, IPeak.sort_mass_ascending);
		this.minmass = peaks.firstElement().getMass();
		this.maxmass = peaks.lastElement().getMass();
		
		// calculate the min and max-intensity
		Collections.sort(peaks, IPeak.sort_intensity_ascending);
		this.peakscan = peaks.lastElement().getScanID();
		this.peakretentiontime = peaks.lastElement().getRetentionTime();
		this.minintensity = peaks.firstElement().getIntensity();
		this.maxintensity = peaks.lastElement().getIntensity();
		
		// calculate the mass
		this.mass = mass.getMass(peaks);
		
		// retrieve the minscan and maxscan numbers, the peaks-vector is kept sorted like that
		Collections.sort(peaks, IPeak.sort_scanid_ascending);
		this.minscan = peaks.firstElement().getScanID();
		this.maxscan = peaks.lastElement().getScanID();
		Collections.sort(peaks, IPeak.sort_retentiontime_ascending);
		this.minretentiontime = peaks.firstElement().getRetentionTime();
		this.maxretentiontime = peaks.lastElement().getRetentionTime();
		
		// calculate the intensity values
		// TODO probably best to subtract the min-intensity ???
		totalintensity = 0;
		int prevscan = peaks.firstElement().getScanID();
		double previntensity = peaks.firstElement().getIntensity();
		for (int i=1; i<peaks.size(); ++i)
		{
			gPeak peak = peaks.get(i);
			
			int nextscan = peak.getScanID();
			double nextintensity = peak.getIntensity();
			int width = nextscan - prevscan;
			
			// calculate the triangle
			totalintensity += width * Math.abs(previntensity-nextintensity);
			// calculate the rectangle
			totalintensity += width * Math.min(previntensity, nextintensity);
			
			// save the current values
			prevscan = nextscan;
			previntensity = nextintensity;
		}
	}
	
	
	// IPeak overrides
	@Override
	public double getMass()
	{
		return mass;
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
		return peakscan;
	}
	
	@Override
	public void setScanID(int scan)
	{
	}
	
	@Override
	public double getRetentionTime()
	{
		return peakretentiontime;
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
		for (int i=0; i<peakdata.size(); ++i)
			peakdata.setMeasurementID(i, id);
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
		StringBuffer str = new StringBuffer();
		
		str.append(measurementid);
		str.append(patternid);
		for (gPeak peak : peakdata)
			str.append(peak.getSha1Data());
		
		return str.toString().getBytes();
	}
	
	@Override
	public Signal getSignal()
	{
		// TODO a 3d representation is needed for MassSpectrometry<Profile>
		double[] xvals = new double[peakdata.size()];
		double[] yvals = new double[peakdata.size()];
		for (int i=0; i<peakdata.size(); ++i)
		{
			xvals[i] = peakdata.getRetentionTime(i);
			yvals[i] = peakdata.getIntensity(i);
		}
		return new Signal(xvals, yvals);
	}
	
	
	// access
	/**
	 * Returns the vector filled with all the peaks associated to this mass
	 * chromatogram. The reference points to the same vector as used inside
	 * this class, so do not remove anything from this vector. Sorting is
	 * OK as all the properties have already been calculated in the constructor.
	 * However, this is not recommended as the peaks are kept sorted on scan,
	 * which for access with {@link MassChromatogram#getPeak(int)} makes
	 * more sense.
	 * 
	 * @return		The vector containing all the peaks associated to this mass chromatogram.
	 */
	public Vector<gPeak> getPeaks()
	{
		return peakdata.getPeaks();
	}
	
	/**
	 * Returns the {@link PeakData} instance associated to this MassChromatogram,
	 * containing all the peak-information.
	 * 
	 * @return		The PeakData instance.
	 */
	public PeakData<gPeak> getPeakData()
	{
		return peakdata;
	}
	
	/**
	 * Returns the number of peaks associated with this mass chromatogram.
	 * 
	 * @return		The number of peaks associated to this mass chromatogram.
	 */
	public int getNrPeaks()
	{
		return peakdata.size();
	}
	
	/**
	 * Retrieves the peak at position pos. An IndexOutOfBoundsException is thrown
	 * when the pos value is either smaller then 0 or larger than the number of
	 * peaks.
	 * 
	 * @param pos	The position of the peak to retrieve.
	 * @return		The peak at position pos.
	 */
	public gPeak getPeak(int pos)
	{
		return peakdata.getPeak(pos);
	}
	
	/**
	 * Returns the minimum mass of all the peaks associated with this mass chromatogram.
	 * 
	 * @return		The minimum mass of all the peaks.
	 */
	public double getMinMass()
	{
		return minmass;
	}
	
	/**
	 * Returns the maximum mass of all the peaks associated with this mass chromatogram.
	 * 
	 * @return		The maximum mass of all the peaks.
	 */
	public double getMaxMass()
	{
		return maxmass;
	}
	
	/**
	 * Returns the minimum intensity of all the peaks associated with this mass chromatogram.
	 * 
	 * @return		The minimum intensity of all the peaks.
	 */
	public double getMinIntensity()
	{
		return minintensity;
	}
	
	/**
	 * Returns the maximum intensity of all the peaks associated with this mass chromatogram.
	 * 
	 * @return		The maximum intensity of all the peaks.
	 */
	public double getMaxIntensity()
	{
		return maxintensity;
	}
	
	/**
	 * Returns the total intensity of the mass chromatogram. This basically is the area under
	 * the curve of the trace and should be the most accurate intensity value possible. The
	 * area under the curve represents all the measured intensity of the compound in
	 * question.
	 * 
	 * @return		The total intensity.
	 */
	public double getTotalIntensity()
	{
		return totalintensity;
	}
	
	/**
	 * Returns the minimum scan of all the peaks associated with this mass chromatogram.
	 * 
	 * @return		The minimum scan of all the peaks.
	 */
	public int getMinScanID()
	{
		return minscan;
	}
	
	/**
	 * Returns the maximum scan of all the peaks associated with this mass chromatogram.
	 * 
	 * @return		The maximum scan of all the peaks.
	 */
	public int getMaxScanID()
	{
		return maxscan;
	}
	
	/**
	 * Returns the scan where the mass chromatogram is most intense. This is the scan
	 * which should be used for the retention time of the peak and is returned by the
	 * {@link IPeak#getScanID()} method.
	 * 
	 * @return		The scan where the mass chromatogram is most intense.
	 */
	public int getPeakScanID()
	{
		return peakscan;
	}
	
	/**
	 * Returns the retention time where the mass chromatogram begins (e.g. the first scan
	 * where a data point for this mass chromatogram is recorded).
	 * 
	 * @return		The minimum retention time for this mass chromatogram.
	 */
	public double getMinRetentionTime()
	{
		return minretentiontime;
	}
	
	/**
	 * Returns the retention time where the mass chromatogram ends (e.g. the last scan
	 * where a data point for this mass chromatogram is recorded).
	 * 
	 * @return		The maximum retention time for this mass chromatogram.
	 */
	public double getMaxRetentionTime()
	{
		return maxretentiontime;
	}
	
	/**
	 * Returns the retention time where the mass chromatogram is most intense.
	 * 
	 * @return		The retention time where the mass chromatogram is most intense.
	 */
	public double getPeakRetentionTime()
	{
		return peakretentiontime;
	}
	
	
	// smoothing
	public void savitzkyGolay()
	{
		savitzkyGolay(SavitzkyGolayFilter.Points.TWENTYTHREE);
	}
	
	public void savitzkyGolay(SavitzkyGolayFilter.Points p)
	{
		System.arraycopy(
				new SavitzkyGolayFilter(p).filter(peakdata.getRetentionTimes(), peakdata.getIntensities()),
				0,
				peakdata.intensities,
				0,
				peakdata.size
			);
	}
	
	
	// mass chromatogram quality
	/**
	 * Simplistic implementation of the CoDA algorithm working on individual mass
	 * chromatograms. The CoDA returns an MCQ (mass chromatogram quality) value
	 * for the mass chromatogram, indicating how well defined it is (a larger
	 * value indicates a better quality [0-1]). This MCQ is calculated by 
	 * correlating the original signal and a smoothed version
	 * (@see {@link Signal#savitzkyGolaySmooth()} of the original signal. In
	 * order to remove tails on both sides the smoothed version is mean
	 * subtracted. The idea of CoDA is that noisy signals will be different
	 * to their smoothed version (spikes are gone causing a low correlation). The
	 * correlation value is then adjusted to fall between 0 and 1.
	 * 
	 * @return		The MCQ value for this mass chromatogram [0..1], higher is better.
	 */
	public double coda()
	{
		// smooth the signal
		Signal signal = getSignal();
		Signal smooth = signal.savitzkyGolaySmooth();
		
		// mean subtraction
		double rts[] = new double[getNrPeaks()];
		double intensities[] = new double[getNrPeaks()];
		for (int i=0; i<getNrPeaks(); ++i)
		{
			rts[i] = smooth.getX()[i];
			intensities[i] = smooth.getY()[i];
		}
		
		double mean = Statistical.mean(intensities);
		for (int i=0; i<getNrPeaks(); ++i)
			intensities[i] = Math.max(0, intensities[i]-mean);
		
		Signal smooth_mean = new Signal(rts, intensities);
		
		// normalize
		signal.normalize();
		smooth_mean.normalize();
		
		// mcq
		if (smooth_mean.getMaxY() == 0)
			return 0;
		double correlation = signal.pearsonsCorrelation(smooth_mean)[Statistical.PEARSON_CORRELATION];
		if (Double.isNaN(correlation))
			return 0;
		return (correlation+1.) / 2.;
	}
	
	/**
	 * Implementation of a CoDA-like approach for evalution of an individual mass
	 * chromatogram based on the Durbin-Watson statistic. The Durbin-Watson statistic
	 * is a value between 0 and 4 where smaller is better. In order to keep in line
	 * with the normal coda approach this value is rescaled between [0..1], where
	 * higher is better.
	 * 
	 * @return		The MCQ value for this mass chromatogram [0..1], higher is better.
	 */
	public double codaDW()
	{
		double values[] = new double[getNrPeaks()];
		for (int i=0; i<getNrPeaks(); ++i)
			values[i] = peakdata.getIntensity(i) / maxintensity;
		return 1 - (Statistical.durbinWatson(values)/4.);
	}
	
	/**
	 * Implementation of a CoDA-like approach for evaluation of an individual mass
	 * chromatogram based on the Shapiro-Wilk test. A mass chromatogram is expected
	 * to have a log-normal distribution, which can be made normal by taking the log
	 * of all the intensity values. The Shapiro-Wilk test implemented in
	 * {@link Statistical#shapiroWilk(double[])} is then used to evaluate whether
	 * the signal is normally distributed.
	 * <p />
	 * The p-value of the test is used for the indicator and is scaled between
	 * [0..1] (higher is better), in order to keep in line with the MCQ value
	 * calculated by {@link MassChromatogram#coda()}.
	 * 
	 * @return		The MCQ value for this mass chromatogram [0..1], higher is better.
	 */
	public double codaSW()
	{
		double values[] = new double[getNrPeaks()];
		for (int i=0; i<getNrPeaks(); ++i)
			values[i] = Math.log(peakdata.getIntensity(i) / maxintensity);
		return Math.max(0, 1-Statistical.shapiroWilk(values)[Statistical.SHAPIRO_WILK_PVALUE]);
	}
	
	
	// Iterable overrides
	public Iterator<gPeak> iterator()
	{
		return peakdata.iterator();
	}
	
	
	// data
	protected int minscan;
	protected int maxscan;
	protected int peakscan;
	
	protected double minretentiontime;
	protected double maxretentiontime;
	protected double peakretentiontime;
	
	protected double mass;
	protected double minmass;
	protected double maxmass;
	
	protected double minintensity;
	protected double maxintensity;
	protected double totalintensity;
	
	protected int patternid = -1;
	protected int measurementid = -1;
	
	protected PeakData<gPeak> peakdata;
}
