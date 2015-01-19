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



package mzmatch.ipeak.normalisation;


// java
import java.io.*;

import java.util.*;
import java.util.zip.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.math.*;

import peakml.chemistry.PeriodicTable;
import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;





public class RelativeIntensityNormalize
{
	// implementation
	public static IPeak getPeakOfMeasurement(IPeak peak, int measurementid)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			for (IPeak p : (IPeakSet<IPeak>) peak)
			{
				IPeak pp = getPeakOfMeasurement(p, measurementid);
				if (pp != null)
					return pp;
			}
			return null;
		}
		else if (peak.getMeasurementID() == measurementid)
			return peak;
		return null;
	}
	
	public static Vector<MassChromatogram<Centroid>> getMassChromatograms(IPeakSet<MassChromatogram<Centroid>> peaksets, SetInfo setinfo)
	{
		Vector<MassChromatogram<Centroid>> masschromatograms = new Vector<MassChromatogram<Centroid>>();
		for (int measurementid : setinfo.getAllMeasurementIDs())
		{
			MassChromatogram<Centroid> masschromatogram = (MassChromatogram<Centroid>) getPeakOfMeasurement(peaksets, measurementid);
			if (masschromatogram != null)
				masschromatograms.add(masschromatogram);
		}
		return masschromatograms;
	}
	
	public static Vector<Integer> unique(Vector<Integer> values)
	{
		Vector<Integer> unique = new Vector<Integer>();
		for (Integer value : values)
			if (!unique.contains(value))
				unique.add(value);
		return unique;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "RelativeIntensityNormalize";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM normalize the contents of a combined file\n",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"")
		public String output = null;
		
		@Option(name="sets", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"for each set a reference")
		public Vector<String> sets = new Vector<String>();
		 
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
	}
	
	
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
				if (options.output != null)
					Tool.createFilePath(options.output, true);
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading '" + options.input + "'");
			ParseResult result = PeakMLParser.parse(new FileInputStream(options.input), true);
			
			Header header = result.header;
			IPeakSet<IPeakSet<MassChromatogram<Centroid>>> peaksets = (IPeakSet<IPeakSet<MassChromatogram<Centroid>>>) result.measurement;
			
			// validity check
			if (options.sets.size() != header.getNrSetInfos())
			{
				System.err.println("[ERROR]: The parameter sets does not match the number of sets in the header.");
				System.exit(0);
			}
			
			Vector<Integer> sets = new Vector<Integer>();
			for (String set : options.sets)
				sets.add(Integer.parseInt(set));
			
			// process the data
			if (options.verbose)
				System.out.println("Processing data");
			Vector<Integer> unique = unique(sets);
			System.out.println(unique);
			for (int setid : unique)
			{
				// grab all the measurements from this set
				int setsize = 0;
				Vector<SetInfo> setinfos = new Vector<SetInfo>();
				for (int i=0; i<options.sets.size(); ++i)
					if (sets.get(i) == setid)
					{
						SetInfo setinfo = header.getSetInfos().get(i);
						setinfos.add(setinfo);
						//if (setinfos.size()==1 || setinfos.size()==4)
							setsize += setinfo.getNrMeasurementIDs();
					}
				
				// process all the peaksets
				for (IPeakSet<MassChromatogram<Centroid>> peakset : peaksets)
				{
					// grab the mc's in the sets we're currently looking at
					Vector<MassChromatogram<Centroid>> mcs = new Vector<MassChromatogram<Centroid>>();
					for (MassChromatogram<Centroid> mc : peakset)
					{
						int setpos = -1;
						for (int i=0; i<setinfos.size(); ++i)
						{
							SetInfo setinfo = setinfos.get(i);
							if (setinfo.containsMeasurementID(mc.getMeasurementID()))
							{
								setpos = i;
								break;
							}
						}
						if (setpos != -1)
						{
							mc.setPatternID(1);//setpos==1||setpos==4 ? 1 : 0);
							mcs.add(mc);
						}
					}
					if (mcs.size() == 0)
						continue;
					
					// calculate the factor
					int factor = 0;
					for (MassChromatogram<Centroid> mc : mcs)
						if (mc.getPatternID() == 1)
							factor += mc.getIntensity();
					factor /= setsize;
					
					// apply the normalisation
					for (MassChromatogram<Centroid> masschromatogram : mcs)
					{
						for (Centroid centroid : masschromatogram)
							centroid.setIntensity(centroid.getIntensity() / factor);
					}
				}
			}
			
			// write 
			if (options.verbose)
				System.out.println("Writing '" + options.output + "'");
			PeakMLWriter.write(
					header, peaksets.getPeaks(),
					null,
					new GZIPOutputStream(new FileOutputStream(options.output)),
					null
				);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
