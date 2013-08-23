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



package mzmatch.ipeak;


// java
import java.io.*;

import java.util.*;
import java.util.zip.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;

import peakml.math.*;
import peakml.math.function.*;

// mzmatch
import mzmatch.util.*;





public class ExtractMassChromatograms
{
	// implementation
	public static Vector<MassChromatogram<? extends Peak>> extractMassChromatograms(ChromatographyMS<Peak> cms, double ppm, double gapsize)
	{
		class ScanIndex
		{
			public ScanIndex(int scanid, int index)
			{
				this.scanid = scanid;
				this.index = index;
			}
			public int scanid;
			public int index;
		}
		
		// calculate the index-tables for the intensity sorted (ascending) arrays
		int[][] scans_intensity_indextables = new int[cms.getNrScans()][];
		for (int scanid=0; scanid<cms.getNrScans(); ++scanid)
		{
			Spectrum<Peak> scan = cms.getScan(scanid);
			PeakData<Peak> peakdata = scan.getPeakData();
			
			// create and sort a double index table in ascending order
			double _indextable[] = new double[scan.getNrPeaks()];
			double intensities[] = new double[scan.getNrPeaks()];
			System.arraycopy(peakdata.getIntensities(), 0, intensities, 0, scan.getNrPeaks());
			for (int i=0; i<scan.getNrPeaks(); ++i)
				_indextable[i] = i;
			Statistical.qsort(intensities, _indextable);
			
			// create and sort the int index table in descending order
			int indextable[] = new int[scan.getNrPeaks()];
			for (int i=0; i<scan.getNrPeaks(); ++i)
				indextable[i] = (int) _indextable[(scan.getNrPeaks() - i) - 1];
			scans_intensity_indextables[scanid] = indextable;
			
			// while we're here, reset all the patternid's in the scan to 0 (used to keep track of which peaks have already been used)
			for (int i=0; i<scan.getNrPeaks(); ++i)
				peakdata.setPatternID(i, 0);
		}
		
		// create the arrays to maintain the current index for each scan (used to track down the most intense not yet used peak)
		int current_indices[] = new int[cms.getNrScans()];
		for (int scanid=0; scanid<cms.getNrScans(); ++scanid)
			current_indices[scanid] = 0;
		
		// start grabbing the peaks
		Vector<MassChromatogram<? extends Peak>> mcs = new Vector<MassChromatogram<? extends Peak>>();
		while (true)
		{
			// locate the most intense peak
			int bestscanid = -1;
			int bestrealindex = -1;
			double bestintensity = -1;
			for (int scanid=0; scanid<cms.getNrScans(); ++scanid)
			{
				// retrieve the peakdata
				PeakData<Peak> peakdata = cms.getScan(scanid).getPeakData();
				
				// move to the next unprocessed peak of this scan
				while (current_indices[scanid]<peakdata.size())
				{
					int realindex = scans_intensity_indextables[scanid][current_indices[scanid]];
					int patternid = peakdata.getPatternID(realindex);
					if (patternid == -1)
						current_indices[scanid]++;
					else
						break;
				}
				if (current_indices[scanid] >= peakdata.size())
					continue;
				
				// retrieve the real index in the peakdata of the current peak
				int realindex = scans_intensity_indextables[scanid][current_indices[scanid]];
				
				// retrieve info of the current peak and check whether this is the most intense, unprocessed peak so far
				int patternid = peakdata.getPatternID(realindex);
				double intensity = peakdata.getIntensity(realindex);
				if (patternid!=-1 && (bestintensity==-1 || intensity>bestintensity))
				{
					bestscanid = scanid;
					bestrealindex = realindex;
					bestintensity = intensity;
				}
			}
			if (bestscanid == -1)	// nothing left
				break;
			
			// move the index of the best scan one up
			current_indices[bestscanid]++;
			
			// mark the selected peak as processed
			cms.getScan(bestscanid).getPeakData().setPatternID(bestrealindex, -1);
			
			// save the mass-info
			double mass = cms.getScan(bestscanid).getPeakData().getMass(bestrealindex);
			double delta = PeriodicTable.PPM(mass, ppm);
			
			// sideways peak growing
			int prevscanid;
			Vector<ScanIndex> selected_indices = new Vector<ScanIndex>();
			selected_indices.add(new ScanIndex(bestscanid, bestrealindex));
			
			// grow it side-ways (left)
			prevscanid = bestscanid;
			for (int scanid=prevscanid-1; scanid>=0 && prevscanid-scanid<gapsize; --scanid)
			{
				PeakData<? extends Peak> peakdata = cms.getScan(scanid).getPeakData();
				
				// get the indices for the mass-range of the found peak
				int neighbourhood[][] = peakdata.getIndicesInMassRange(mass-delta, mass+delta);
				
				// select the most intense, unprocessed peak
				int bestneighbour = -1;
				double bestneighbourintensity = -1;
				for (int neighbour=0; neighbour<neighbourhood.length; ++neighbour)
				{
					int patternid = peakdata.getPatternID(neighbourhood[neighbour][PeakData.INDEX_REAL]);
					double intensity = mass-peakdata.getMass(neighbourhood[neighbour][PeakData.INDEX_REAL]);
					if (patternid!=-1 && (bestintensity==-1 || intensity>bestneighbourintensity))
					{
						bestneighbour = neighbourhood[neighbour][PeakData.INDEX_REAL];
						bestneighbourintensity = intensity;
					}
				}
				
				if (bestneighbour != -1)
				{
					prevscanid = scanid;
					peakdata.setPatternID(bestneighbour, -1);
					selected_indices.add(0, new ScanIndex(scanid, bestneighbour));
				}
			}
			
			// grow it side-ways (right)
			prevscanid = bestscanid;
			for (int scanid=prevscanid+1; scanid<cms.getNrScans() && scanid-prevscanid<gapsize; ++scanid)
			{
				PeakData<? extends Peak> peakdata = cms.getScan(scanid).getPeakData();
				
				// get the indices for the mass-range of the found peak
				int neighbourhood[][] = peakdata.getIndicesInMassRange(mass-delta, mass+delta);
				
				// select the most intense, unprocessed peak
				int bestneighbour = -1;
				double bestneighbourintensity = -1;
				for (int neighbour=0; neighbour<neighbourhood.length; ++neighbour)
				{
					int patternid = peakdata.getPatternID(neighbourhood[neighbour][PeakData.INDEX_REAL]);
					double intensity = peakdata.getIntensity(neighbourhood[neighbour][PeakData.INDEX_REAL]);
					if (patternid!=-1 && (bestintensity==-1 || intensity>bestneighbourintensity))
					{
						bestneighbour = neighbourhood[neighbour][PeakData.INDEX_REAL];
						bestneighbourintensity = intensity;
					}
				}
				
				if (bestneighbour != -1)
				{
					prevscanid = scanid;
					peakdata.setPatternID(bestneighbour, -1);
					selected_indices.add(new ScanIndex(scanid, bestneighbour));
				}
			}
			
			// create the mass chromatogram
			if (selected_indices.size() >= 5)
			{
				PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, selected_indices.size());
				for (int i=0; i<selected_indices.size(); ++i)
				{
					ScanIndex scanindex = selected_indices.get(i);
					PeakData<? extends Peak> scan = cms.getScan(scanindex.scanid).getPeakData();
					
					peakdata.set(
							i,
							scanindex.scanid,
							scan.getRetentionTime(scanindex.index),
							scan.getMass(scanindex.index),
							scan.getIntensity(scanindex.index)
						);
				}
				mcs.add(new MassChromatogram<Centroid>(peakdata));
			}
		}
		
		return mcs;
	}
	
	
	
	
	
	
	
	// implementation - mass trace extraction
	public final static int PROCESSED = 0;
	public final static int UNPROCESSED = 1;
	
	public static void growSideways(ChromatographyMS<Peak> cms, double mass, double delta, int side, int scanid, int scanindices[])
	{
		if ((scanid==0&&side==-1) || (scanid==cms.getNrScans()-1&&side==1))
			return;
		
		// collect information
		int nrscans = cms.getNrScans();
		
		// start processing
		int currentscanid = scanid;
		do
		{
			// move to the side
			currentscanid += side;
			
			// collect the peakdata for the current scan
			PeakData<? extends Peak> peakdata = cms.getScan(currentscanid).getPeakData();
			
			// get the indices for the mass-range of the found peak
			int neighbourhood[][] = peakdata.getIndicesInMassRange(mass-delta, mass+delta);
			
			// select the most intense, unprocessed peak
			int bestneighbour = -1;
			double bestneighbourintensity = -1;
			for (int neighbour=0; neighbour<neighbourhood.length; ++neighbour)
			{
				int patternid = peakdata.getPatternID(neighbourhood[neighbour][PeakData.INDEX_REAL]);
				double intensity = peakdata.getIntensity(neighbourhood[neighbour][PeakData.INDEX_REAL]);
				if (patternid!=PROCESSED && intensity>bestneighbourintensity)
				{
					bestneighbour = neighbourhood[neighbour][PeakData.INDEX_REAL];
					bestneighbourintensity = intensity;
				}
			}
			
			if (bestneighbour != -1)
			{
				peakdata.setPatternID(bestneighbour, PROCESSED);
				scanindices[currentscanid] = bestneighbour;
			}
			
		} while (currentscanid>1 && currentscanid<nrscans-1);
	}
	
	public static Vector<MassChromatogram<Centroid>> extractMassChromatograms2(ChromatographyMS<Peak> cms, double ppm)
	{
		// reset patternid's and create a table for tracking the most intense, not yet processed peaks
		int current_indices[] = new int[cms.getNrScans()];
		for (int scanid=0; scanid<cms.getNrScans(); ++scanid)
		{
			PeakData<Peak> peakdata = cms.getScan(scanid).getPeakData();
			for (int i=0; i<peakdata.size(); ++i)
				peakdata.setPatternID(i, UNPROCESSED);
			current_indices[scanid] = peakdata.size()-1;
		}
		
		// create a place for storing the indices of the grouped peaks
		int scanindices[] = new int[cms.getNrScans()];
		
		// start grabbing the peaks
		Vector<MassChromatogram<Centroid>> mcs = new Vector<MassChromatogram<Centroid>>();
		while (true)
		{
			// locate the most intense peak of all scans
			int bestscanid = -1;
			int bestrealindex = -1;
			double bestintensity = -1;
			for (int scanid=0; scanid<cms.getNrScans(); ++scanid)
			{
				PeakData<Peak> peakdata = cms.getScan(scanid).getPeakData();
				double indextable_ascending_intensity[] = peakdata.getIndexTableAscendingIntensity();
				
				// move to the next unprocessed peak of this scan
				int realindex = -1;
				while (current_indices[scanid] > 0)
				{
					realindex = (int) indextable_ascending_intensity[current_indices[scanid]];
					if (peakdata.getPatternID(realindex) == PROCESSED)
						current_indices[scanid]--;
					else
						break;
				}
				if (realindex == -1)
					continue;
				
				// when this peak has a higher intensity save it
				double intensity = peakdata.getIntensity(realindex);
				if (intensity > bestintensity)
				{
					bestrealindex = realindex;
					bestscanid = scanid;
					bestintensity = intensity;
				}
			}
			
			// quite when nothing was found
			if (bestscanid == -1)
				break;
			
			// setup the scanindices used for sideways growing
			for (int i=0; i<scanindices.length; ++i)
				scanindices[i] = -1;
			scanindices[bestscanid] = bestrealindex;
			
			// get mass/delta properties and set the selected data-point to processed.
			PeakData<Peak> peakdata = cms.getScan(bestscanid).getPeakData();
			peakdata.setPatternID(bestrealindex, PROCESSED);
			double mass = peakdata.getMass(bestrealindex);
			double delta = PeriodicTable.PPM(mass, ppm);
			
			// grow sideways
			growSideways(cms, mass, delta, -1, bestscanid, scanindices); // left
			growSideways(cms, mass, delta,  1, bestscanid, scanindices); // right
			
			// create a mass chromatogram
			int size = 0;
			for (int scanid=0; scanid<scanindices.length; ++scanid)
				if (scanindices[scanid] != -1) size++;
			if (size < 5)
				continue;
			
			PeakData<Centroid> mc_peakdata = new PeakData<Centroid>(Centroid.factory, size);
			int index = 0;
			for (int scanid=0; scanid<scanindices.length; ++scanid)
				if (scanindices[scanid] != -1)
				{
					peakdata = cms.getScan(scanid).getPeakData();
					mc_peakdata.set(
							index++, scanid,
							peakdata.getRetentionTime(scanindices[scanid]),
							peakdata.getMass(scanindices[scanid]),
							peakdata.getIntensity(scanindices[scanid])
						);
				}
			
			MassChromatogram<Centroid> mc = new MassChromatogram<Centroid>(mc_peakdata);
			if (!Double.isNaN(mc.getMass()) && !Double.isNaN(mc.getIntensity()))
				mcs.add(mc);
		}
		
		return mcs;
	}
	
	
	// implementation - mass trace breakup
	public static class ROI
	{
		public ROI(int lft, int rgt)
		{
			this.lft = lft;
			this.rgt = rgt;
			this.length = rgt - lft + 1;
		}
		
		public final int lft;
		public final int rgt;
		public final int length;
	}
	
	public static ROI findROI(double observed[], double cutoff, int minscans)
	{
		// sanity check - <minscans cannot be a signal
		if (observed.length < minscans)
			return null;
		if (Statistical.rsd(observed) < 0.35)
			return null;
		
		// determine the properties of the maximum peak in this data
		double max = Statistical.max(observed);
		double threshold = cutoff * max;
		int indx_max = Statistical.indexOfMax(observed);
		
		// calculate the boundries
		int lft=indx_max, rgt=indx_max;
		while (lft>0 && observed[lft]>threshold) lft--;
		while (rgt<observed.length-2 && observed[rgt]>threshold) rgt++;
		
		return new ROI(lft, rgt);
	}
	
	public static double mse(double retentiontimes[], double intensities[])
	{
		final peakml.math.filter.SavitzkyGolayFilter filter =
			new peakml.math.filter.SavitzkyGolayFilter(peakml.math.filter.SavitzkyGolayFilter.Points.FIFTEEN);
		
		try
		{
			double _intensities[] = new double[intensities.length];
			System.arraycopy(intensities, 0, _intensities, 0, intensities.length);
			
			if (_intensities.length > 10)
				_intensities = filter.filter(retentiontimes, _intensities);
			
			double min = Statistical.min(_intensities);
			double max = Statistical.max(_intensities);
			for (int i=0; i<_intensities.length; ++i)
				_intensities[i] = (_intensities[i]-min) / (max-min);
			
			Function f = LinearFunction.fit(retentiontimes, _intensities);
			double maxdiff = Math.abs(_intensities[0] - f.getY(retentiontimes[0]));
			for (int i=1; i<_intensities.length; ++i)
				maxdiff = Math.max(maxdiff, Math.abs(_intensities[i] - f.getY(retentiontimes[i])));
			return maxdiff;
		}
		catch (Exception e)
		{
			return 0;
		}
	}
	
	public static void breakup(PeakData<Centroid> peakdata, int lft, int rgt, double cutoff, int minscans, Vector<MassChromatogram<? extends Peak>> mcs)
	{
		// extract the area we're supposed to look at
		double intensities[] = new double[rgt-lft+1];
		System.arraycopy(peakdata.getIntensities(), lft, intensities, 0, rgt-lft+1);
		
		// locate the region of interest
		ROI roi = findROI(intensities, cutoff, minscans);
		if (roi == null) 
			return;
		
		if (roi.length >= minscans)
		{
			// create the mass chromatogram
			double roi_retentiontimes[] = new double[roi.length];
			System.arraycopy(peakdata.getRetentionTimes(), lft+roi.lft, roi_retentiontimes, 0, roi.length);
			double roi_intensities[] = new double[roi.length];
			System.arraycopy(peakdata.getIntensities(), lft+roi.lft, roi_intensities, 0, roi.length);
			
			double dw = Statistical.durbinWatson(roi_intensities);
			double mse = mse(roi_retentiontimes, roi_intensities);
			int indx_max = Statistical.indexOfMax(roi_intensities);
			
			if (dw<=3 && mse>0.2 && indx_max>1 && indx_max<roi_intensities.length-2)
			{
				int roi_scanids[] = new int[roi.length];
				System.arraycopy(peakdata.getScanIDs(), lft+roi.lft, roi_scanids, 0, roi.length);
				int roi_patternids[] = new int[roi.length];
				int roi_measurementids[] = new int[roi.length];
				double roi_masses[] = new double[roi.length];
				System.arraycopy(peakdata.getMasses(), lft+roi.lft, roi_masses, 0, roi.length);
				
				MassChromatogram<Centroid> mc = new MassChromatogram<Centroid>(new PeakData<Centroid>(
						Centroid.factory, roi.length,
						roi_scanids,
						roi_patternids,
						roi_measurementids,
						roi_masses,
						roi_intensities,
						roi_retentiontimes
					));
				mc.setMeasurementID(0);
				mcs.add(mc);
			}
		}
		
		// continue with the sides
		if (roi.lft > minscans)
			breakup(peakdata, lft, lft+roi.lft, cutoff, minscans, mcs);
		if (rgt - (lft+roi.rgt) > minscans)
			breakup(peakdata, lft+roi.rgt, rgt, cutoff, minscans, mcs);
	}
	
	public static void breakup(MassChromatogram<Centroid> masschromatogram, double cutoff, int minscans, Vector<MassChromatogram<? extends Peak>> mcs)
	{
		PeakData<Centroid> peakdata = masschromatogram.getPeakData();
		breakup(peakdata, 0, peakdata.size()-1, cutoff, minscans, mcs);
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "ExtractMassChromatograms";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Extracts mass chromatograms (x-axis: RT; y-axis: Intensity) from 2D mass spectrometry data " +
		"(LC/MS or GC/MS). The raw data is loaded from the open standard file formats (mzML, " +
		"mzXML or mzData) and all of the individidual mass traces (M/Z +/- ppm over the whole scan " +
		"range) are retrieved. When the option 'threshold' is defined, the individual mass traces are " +
		"broken up into individual mass chromatograms (ie the isomers are separated). This is " +
		"achieved by cutting peaks out of the mass trace where the threshold is reached (as a " +
		"percentage of the most intense portion of the mass trace). This is an iterative process, " +
		"where the sides are then analyzed in the same fashion." +
		"\n\n" +
		"The method employed here for retrieving mass chromatograms is greedy and extracts " +
		"everything (although a modicum of noise reduction is applied to reduce the amount of " +
		"fragments from broken up mass chromatograms). In order to reduce the resulting noise " +
		"patterns, tools like 'mzmatch.filter.NoiseFilter' and 'mzmatch.filter.RSDFilter' " +
		"can be employed." +
		"\n\n" +
		"The resulting output file is in PeakML-format, containing a list of all the extracted " +
		"mass chromatograms. When the cutoff value has been selected, one can also specify a file " +
		"(option 'masstraces') where the extracted mass chromatograms are overlaid on the mass " +
		"traces they have been cut from." +
		"\n\nRemarks\n" +
		"1. At this time only centroid data is supported.\n" +
		"2. NetCDF is not supported as it misses necessary meta-information\n" +
		"3. Direct injection data will not yield correct results\n",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM process a single file\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -ppm 3 -i file.mzXML -o file.peakml\n" +
		"REM process multiple files and separate isomers\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -threshold 0.02 -ppm 3 -i raw\\*.mzXML -o peaks\\ -masstraces peaks\\traces\\"
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input files, which should be in one of the open standard file formats " +
			"(mzML, mzXML or mzData) and contain data from a 2D mass spectrometry setup (LC/MS or " +
			"GC/MS)." +
			"\n" +
			"When this option has not been set, the input is read from the stdin (allowing for " +
			"pipeline building). When a single input file is defined, the output '-o' should " +
			"contain the output filename. When multiple input files are defined, the output " +
			"'-o' should define an output directory." +
			"\n" +
			"For now only centroid input data is supported.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the ouput file(s); refer to the option input '-i' for a description of " +
			"behaviours with regards to multiple input files. The extracted mass chromatograms are " +
			"written here in the PeakML format." +
			"\n" +
			"When this option has not been set the output is written to the standard output (works " +
			"only when there is a single input file).Be sure to unset the verbose option when setting " +
			"up a pipeline reading and writing from the standard in- and outputs.")
		public String output = null;
		@Option(name="masstraces", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional output file where the mass traces are written (only useful when the " +
			"option 'threshold' has been defined), which can be used to debug the mass trace " +
			"breakup approach.")
		public String masstraces = null;
		
		@Option(name="label", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional label for the file, which will be stored in the header of the resulting " +
			"file. The label is used for display purposes in UI environments.")
		public String label = null;
		
		@Option(name="threshold", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The percentage threshold value for breaking the mass traces up, as a percentage of " +
			"the most intense portion of a mass trace. The threshold value is a percentage and " +
			"required to be between 0 and 1.")
		public double threshold = -1;
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The accuracy of the measurement in parts-per-milion. This value is used for the " +
			"collection of the data-points belonging to a mass trace and needs to be " +
			"reasonable for the equipment used to make the measurement (the LTQ-Orbitrap manages " +
			"approximatetly 3 ppm).")
		public double ppm = -1;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String args[])
	{
		try
		{
			Tool.init();
			
			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			
			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help)
			{
				Tool.printHeader(System.out, application, version);
				
				cmdline.printUsage(System.out, "");
				return;
			}
			
			if (options.verbose)
			{
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}
			
			// check the command-line parameters
			{
				// check whether the ppm-value has been set
				if (options.ppm == -1)
				{
					System.err.println("[ERROR]: no ppm-value set.");
					System.exit(0);
				}
				
				// check whether the user gave a list of files or a single file
				if (options.input.size() == 0)
				{
					// we're inputting from stdin
					options.input.add(null);
				}
				else if (options.input.size() == 1)
				{
					// a single file
					File inputfile = new File(options.input.firstElement());
					if (!inputfile.exists())
					{
						System.err.println("[ERROR]: the input-file '" + options.input.firstElement() + "' does not exist.");
						System.exit(0);
					}
				}
				else
				{
					// multiple input files
					for (String filename : options.input)
					{
						File inputfile = new File(filename);
						if (!inputfile.exists())
						{
							System.err.println("[ERROR]: the input-file '" + filename + "' does not exist.");
							System.exit(0);
						}
					}
					
					// check whether we have an output destination
					if (options.output == null)
					{
						System.err.println("[ERROR]: multiple input files defined without an output destination.");
						System.exit(0);
					}
					
					// check that when the rejected output has been set, it's not the same directory
					if (options.masstraces!=null && new File(options.output).compareTo(new File(options.masstraces))==0)
					{
						System.err.println("[ERROR]: with multiple input the output destination and the comparison destination cannot be the same.");
						System.exit(0);
					}
				}
				
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, options.input.size()==1);
				if (options.masstraces != null)
					Tool.createFilePath(options.masstraces, options.input.size()==1);
			}

			
			// process all the files
			for (String filename : options.input)
			{
				// open the streams
//				InputStream input = System.in;
//				if (filename != null)
//					input = new FileInputStream(filename);
				
//				OutputStream out_comparison = null;
				OutputStream out_output = System.out;
				if (options.input.size() == 1)
				{
					if (options.output != null)
						out_output = new FileOutputStream(options.output);
//					if (options.masstraces != null)
//						out_comparison = new FileOutputStream(options.output);
				}
				else
				{
					String name = new File(filename).getName();
					name = name.substring(0, name.lastIndexOf('.')) + ".peakml";
					out_output = new FileOutputStream(options.output + "/" + name);
//					if (options.masstraces != null)
//						out_comparison = new FileOutputStream(options.masstraces + "/" + name);
				}
				

				// load the data
				if (options.verbose)
					System.out.println("Loading '" + filename + "'.");
				ParseResult result = ParseResult.parse(filename);
				if (!result.measurement.getClass().equals(ChromatographyMS.class))
				{
					System.err.println("[ERROR]: file '" + filename + "' does not contain XC/MS data.");
					continue;
				}
				ChromatographyMS<Peak> cms = (ChromatographyMS<Peak>) result.measurement;
				if (options.verbose)
				{
					int nrdatapoints = 0;
					for (Spectrum<? extends Peak> spectrum : cms)
						nrdatapoints += spectrum.getNrPeaks();
					
					MeasurementInfo measurement = result.header.getMeasurementInfo(0);
					
					System.out.println("#scans:       " + cms.getNrScans());
					System.out.println("#data points: " + nrdatapoints);
					System.out.println("#rt range:    " + ScanInfo.rtToString(measurement.getMinRetentionTime()) + "-" + ScanInfo.rtToString(measurement.getMaxRetentionTime())); 
				}
				
				// extract the peak information
				if (options.verbose)
					System.out.println("Extracting mass chromatograms.");
				if (options.verbose)
					System.out.println("- locating mass traces");
				Vector<MassChromatogram<Centroid>> masstraces = extractMassChromatograms2(cms, 2*options.ppm);
				for (MassChromatogram<Centroid> mc : masstraces)
					mc.setMeasurementID(0);
//				if (options.verbose)
//					System.out.println("- breaking up the mass traces");
//				
//				Vector<MassChromatogram<? extends Peak>> masschromatograms = new Vector<MassChromatogram<? extends Peak>>();
//				Vector<IPeakSet<MassChromatogram<? extends IPeak>>> peaksets = new Vector<IPeakSet<MassChromatogram<? extends IPeak>>>();
//				for (MassChromatogram<Centroid> masstrace : masstraces)
//				{
//					Vector<MassChromatogram<? extends Peak>> mcs = new Vector<MassChromatogram<? extends Peak>>();
//					breakup(masstrace, 0.02, 10, mcs);
//					masstrace.setMeasurementID(1);
//					
//					masschromatograms.addAll(mcs);
//					
//					if (mcs.size() == 0)
//					{
//						Vector<MassChromatogram<? extends IPeak>> peakset = new Vector<MassChromatogram<? extends IPeak>>();
//						peakset.add(masstrace);
//						peaksets.add(new IPeakSet<MassChromatogram<? extends IPeak>>(peakset));
//					}
//					else
//					{
//						for (MassChromatogram<? extends IPeak> mc : mcs)
//						{
//							Vector<MassChromatogram<? extends IPeak>> peakset = new Vector<MassChromatogram<? extends IPeak>>();
//							peakset.add(masstrace);
//							peakset.add(mc);
//							peaksets.add(new IPeakSet<MassChromatogram<? extends IPeak>>(peakset));
//						}
//					}
//				}
				
				// write the peak information
				if (options.verbose)
					System.out.println("Writing results.");
				
				File file = new File(filename);
				File parentfile = file.getParentFile();
				
				Header header = result.header;
				header.setNrPeaks(masstraces.size());
				
				MeasurementInfo measurement0 = header.getMeasurementInfo(0);
				measurement0.setLabel(options.label!=null ? options.label : file.getName());
				measurement0.addFileInfo(
						new FileInfo(null, file.getName(), parentfile!=null ? parentfile.getAbsolutePath() : "")
					);
				
				PeakMLWriter.write(header, masstraces, null, new GZIPOutputStream(out_output), null);
				
//				if (options.comparison != null)
//				{
//					header.addMeasurementInfo(new MeasurementInfo(1, measurement0));
//					
//					measurement0.setLabel("extracted");
//					
//					header.addSetInfo(new SetInfo("original", SetInfo.SET, 1));
//					header.addSetInfo(new SetInfo("extracted", SetInfo.SET, 0));
//					
//					PeakMLWriter.write(header, peaksets, null, new GZIPOutputStream(out_comparison), "");
//				}
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
