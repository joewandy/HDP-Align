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
import peakml.math.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.ipeak.util.Identify.Options;
import mzmatch.util.*;




public class IonSuppression
{
	// implementation
	public static final double[] getIntensityCourse(IPeak peak, Header header)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			double intensitycourse[] = new double[header.getNrMeasurementInfos()];
			for (int i=0; i<intensitycourse.length; ++i)
				intensitycourse[i] = 0;
			for (IPeak p : (IPeakSet<? extends IPeak>) peak)
				intensitycourse[header.indexOfMeasurementInfo(p.getMeasurementID())] = p.getIntensity();
			return intensitycourse;
		}
		
		return new double[]{peak.getIntensity()};
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "IonSuppression";
	@OptionsClass(name=application, version=version,  author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"This tool attempts to locate ion suppression in a combined file. The method used " +
		"is similar to that used in the RelatedPeaks, but then reversed. Based on intensity " +
		"trends that are reversed an estimation is made of how likely the suppression is.",
		example=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input file.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the output file.")
		public String output = null;
		
		@Option(name="rtwindow", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The retention time window in seconds, defining the range where to look for matches.")
		public double rtwindow = -1;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String args[])
	{
		final int PROCESSED = 0;
		final int UNPROCESSED = 1;
		
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
			}
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// load the data
			if (options.verbose)
				System.out.println("Loading data");
			ParseResult result = PeakMLParser.parse(input, true);
			IPeakSet<IPeakSet<IPeak>> peaksets = (IPeakSet<IPeakSet<IPeak>>) result.measurement;
			
			// reset the pattern-id's
			Collections.sort(peaksets.getPeaks(), IPeak.sort_intensity_descending);
			for (IPeakSet<IPeak> peakset : peaksets)
				peakset.setPatternID(UNPROCESSED);
			
			// 
			int id = 0;
			Vector<IPeakSet<IPeak>> data = new Vector<IPeakSet<IPeak>>();
			for (IPeakSet<IPeak> peakset : peaksets)
			{
				if (peakset.getPatternID() == PROCESSED)
					continue;
				peakset.setPatternID(PROCESSED);
				
				// create the signal for the current
				double signal[] = getIntensityCourse(peakset, result.header);
				
				// calculate whether this is RSD>0.2 (e.g. is something happening here?)
				if (Statistical.rsd(signal) < 0.2)
					continue;
				
				// collect the neighbours
				Vector<IPeakSet<IPeak>> neighbourhood = peaksets.getPeaksInRetentionTimeRange(
						peakset.getRetentionTime()-options.rtwindow,
						peakset.getRetentionTime()+options.rtwindow
					);
				
				// process the neighbours
				Vector<IPeakSet<IPeak>> collection = new Vector<IPeakSet<IPeak>>();
				collection.add(peakset);
				for (IPeakSet<IPeak> neighbour : neighbourhood)
				{
					if (neighbour.getPatternID() == PROCESSED)
						continue;
					
					double correlation = Statistical.pearsonsCorrelation(
							signal, getIntensityCourse(neighbour, result.header)
						)[Statistical.PEARSON_CORRELATION];
					if (correlation < -0.7)
					{
						collection.add(neighbour);
						neighbour.addAnnotation(Annotation.relationid, id);
						neighbour.addAnnotation(Annotation.relationship, Double.toString(correlation));
						neighbour.setPatternID(PROCESSED);
					}
				}
				
				// add this to the big collection
				if (collection.size() > 1)
				{
					peakset.addAnnotation(Annotation.relationid, id++);
					data.addAll(collection);
				}
			}
			
			// write the data
			if (options.verbose)
				System.out.println("Writing results");
			PeakMLWriter.write(result.header, data, null, new GZIPOutputStream(output), null);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
