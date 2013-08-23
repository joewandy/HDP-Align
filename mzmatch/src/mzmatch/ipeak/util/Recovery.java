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



package mzmatch.ipeak.util;


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

// mzmatch
import mzmatch.util.*;





/* HISTORY:
 */
public class Recovery
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "Recovery";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Complements missing signals to a combination file produced by mzmatch.ipeak.Combine (or " +
		"another combine tool for that matter), containing sets of signals (mass chromatograms or " +
		"backgroundions) produced by the same analyte in different measurements. It is possible " +
		"that the combiner incorrectly missed signals, or was offered files lacking the " +
		"missing signals (eg signals from a timepoint removed by the tool mzmatch.filter.RSDFilter, " +
		"which can be linked to a stable signal from another timepoint)." +
		"\n\n" +
		"For this application to function correctly it is required that the files containing the " +
		"missing signals are generated from the same measurement, as the id's for the measurement " +
		"needs to correspond.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i raw\\*.mzXML -o peaks\\ -ppm 3\n" +
		"\n" +
		"REM combine the extracted mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\24hr_*.peakml -o 24hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\28hr_*.peakml -o 28hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\32hr_*.peakml -o 32hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"\n" +
		"REM remove unstable signals\n" +
		"%JAVA% mzmatch.ipeak.filter.RSDFilter -v -rsd 0.35 -i 24hr.peakml -o 24hr_rsd.peakml -rejected rsd\\24hr.peakml\n" +
		"%JAVA% mzmatch.ipeak.filter.RSDFilter -v -rsd 0.35 -i 28hr.peakml -o 28hr_rsd.peakml -rejected rsd\\28hr.peakml\n" +
		"%JAVA% mzmatch.ipeak.filter.RSDFilter -v -rsd 0.35 -i 32hr.peakml -o 32hr_rsd.peakml -rejected rsd\\32hr.peakml\n" +
		"\n" +
		"REM combine the extracted mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i *hr_rsd.peakml -o timeseries.peakml -ppm 3 -rtwindow 30 -combination set\n" +
		"\n" +
		"REM recover mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.util.Recovery -v -i timeseries.peakml -bins rsd\\*.peakml -o timeseries_recovered.peakml -ppm 3 -rtwindow 30\n"
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input files. The only allowed file format is PeakML containing " +
			"either mass chromatograms or backgroundions at the lowest level (ie the result " +
			"of another Combine can be used).")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the ouput file. The resulting matches are written to this file in the " +
			"PeakML file format." +
			"\n" +
			"When this option has not been set the output is written to the standard output. Be sure " +
			"to unset the verbose option when setting up a pipeline reading and writing from the " +
			"standard in- and outputs.")
		public String output = null;
		@Option(name="recovered", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional output file where the all the sets that have been complemented with missing " +
			"signals are written.")
		public String recovered = null;
		
		@Option(name="bins", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the files containing the missing signals. The only allowed file format " +
			"is PeakML containing either mass chromatograms or backgroundions at the lowest level " +
			"(ie the result of another Combine can be used).")
		public Vector<String> bins = new Vector<String>();
		
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The accuracy of the measurement in parts-per-milion. This value is used for the " +
			"matching of mass chromatogram (collections) and needs to bereasonable for the equipment " +
			"used to make the measurement (the LTQ-Orbitrap manages approximately 3 ppm).")
		public double ppm = -1;
		@Option(name="rtwindow", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The retention time window in seconds, defining the range where to look for matches.")
		public double rtwindow = -1;
		
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
				if (options.ppm == -1)
				{
					System.err.println("[ERROR]: options 'ppm' needs to be set");
					System.exit(0);
				}
				if (options.rtwindow == -1)
				{
					System.err.println("[ERROR]: options 'rtwindow' needs to be set");
					System.exit(0);
				}
				if (options.bins.size() == 0)
				{
					System.err.println("[ERROR]: at least 1 bin needs to be specified");
					System.exit(0);
				}
				if (!Tool.filesExist(options.bins))
				{
					System.err.println("[ERROR]: one of the specified bin files does not exist");
					System.exit(0);
				}
			}
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// load the input data - safe cast as parseIPeakSet forces peaksets
			if (options.verbose)
				System.out.println("Loading data");
			ParseResult result = PeakMLParser.parseIPeakSet(input, null);
			
			Header header = result.header;
			IPeakSet<IPeakSet<? extends IPeak>> peaksets = (IPeakSet<IPeakSet<? extends IPeak>>) result.measurement;
			
			// load all the bins
			if (options.verbose)
				System.out.println("Loading bins");
			Vector<ParseResult> bins = new Vector<ParseResult>();
			for (String filename : options.bins)
			{
				if (options.verbose)
					System.out.println("- " + filename);
				
				// save the peaks
				bins.add(PeakMLParser.parseIPeakSet(new FileInputStream(filename), null));
			}
			
			// retrieve the missing peaks from the bins
			Vector<IPeakSet<? extends IPeak>> recovered = new Vector<IPeakSet<? extends IPeak>>();
			for (IPeakSet<? extends IPeak> peakset : peaksets)
			{
				// locate the missing peaks
				boolean availability[] = new boolean[header.getNrMeasurementInfos()];
				for (int i=0; i<availability.length; ++i)
					availability[i] = false;
				for (IPeak peak : peakset)
					availability[header.indexOfMeasurementInfo(peak.getMeasurementID())] = true;
				
				// check the bins
				boolean recovery = false;
				for (ParseResult bin : bins)
				{
					Header binheader = bin.header;
					IPeakSet<IPeak> binpeakset = (IPeakSet<IPeak>) bin.measurement;
					
					// check whether this bin contains any of the missing measurements
					HashMap<Integer,Integer> idtranslator = new HashMap<Integer,Integer>();
					for (MeasurementInfo minfo : binheader.getMeasurementInfos())
					{
						int index = header.indexOfMeasurementInfo(minfo.getLabel());
						if (availability[index] == false)
							idtranslator.put(minfo.getID(), index);
					}
					if (idtranslator.size() == 0)
						continue;
					
					// locate best peak in the neighbourhood
					IPeak binmatch = IPeak.getBestPeakOnRT(
							binpeakset.getPeaksOfMass(peakset.getMass(), PeriodicTable.PPM(peakset.getMass(), options.ppm)),
							peakset.getRetentionTime()
						);
					if (binmatch==null || Math.abs(binmatch.getRetentionTime()-peakset.getRetentionTime())>options.rtwindow)
						continue;
					
					// shove the missing peaks into the peakset
					recovery = true;
					if (binmatch.getClass().equals(IPeakSet.class))
					{
						for (IPeak p : (IPeakSet<? extends IPeak>) binmatch)
						{
							if (idtranslator.get(p.getMeasurementID()) == null)
								continue;
							int index = idtranslator.get(p.getMeasurementID());
							if (availability[index] == false)
							{
								availability[index] = true;
								
								p.setMeasurementID(index);
								((Vector<IPeak>) peakset.getPeaks()).add(p);
							}
						}
					}
					else // only a single instance - IPeakSet is the only list construct supported here
					{
						int index = idtranslator.get(binmatch.getMeasurementID());
						if (availability[index] == false)
						{
							availability[index] = true;
							
							binmatch.setMeasurementID(index);
							((Vector<IPeak>) peakset.getPeaks()).add(binmatch);
						}
					}
				}
				
				// store the recovered peakset
				if (recovery)
				{
					Collections.sort(peakset.getPeaks(), IPeak.sort_measurementid_ascending);
					recovered.add(peakset);
				}
			}
			
			// write the results
			if (options.verbose)
				System.out.println("Writing data");
			PeakMLWriter.write(header, peaksets.getPeaks(), null, new GZIPOutputStream(output), null);
			
			if (options.recovered != null)
				PeakMLWriter.write(header, recovered, null, new GZIPOutputStream(new FileOutputStream(options.recovered)), null);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
