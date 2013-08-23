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

import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;





@SuppressWarnings("unchecked")
public class ExtractSets
{
	// implementation
	public static void deconvolute(IPeak peak, Vector<Integer> measurementids, Vector<IPeak> peaks)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			for (IPeak p : (IPeakSet<IPeak>) peak)
				deconvolute(p, measurementids, peaks);
		}
		else if (measurementids.contains(peak.getMeasurementID()))
			peaks.add(peak);
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "ExtractSets";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"This tool extracts the entries of the given setnames (stored in the header) from the given " +
		"PeakML file and stores the result in the output file.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract the sets combinationa & combinationc\n" +
		"%JAVA% mzmatch.ipeak.util.ExtractSets -i combined.peakml -setnames \"conditiona,conditionc\" -o combined_ac.peakml",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The input file (in PeakML format). " +
			"When this option is not set the input is read from the standard input.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The output file (in PeakML format). When this option is not set the " +
			"output is written to the standard output.")
		public String output = null;
		
		@Option(name="setnames", param="[string]", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The list of set names to extract.")
		public Vector<String> setnames = new Vector<String>();
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}

	public static void main(String args[])
	{
		try
		{
			Tool.init();
			
			// parse the commandline options
			final Options options = new Options();
			CmdLineParser cmd = new CmdLineParser(options);
			
			// check whether we need to show the help
			cmd.parse(args);
			if (options.help)
			{
				Tool.printHeader(System.out, application, version);
				cmd.printUsage(System.out, "");
				return;
			}
			
			if (options.verbose)
			{
				Tool.printHeader(System.out, application, version);
				cmd.printOptions();
			}
			
			// check the commandline parameters
			{
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading data.");
			ParseResult result = PeakMLParser.parse(new FileInputStream(options.input), true);
			
			Header header = result.header;
			IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			
			// collect all the measurement ids we need
			Vector<SetInfo> sets = new Vector<SetInfo>();
			Vector<Integer> measurementids = new Vector<Integer>();
			for (SetInfo set : header.getSetInfos())
				if (options.setnames.contains(set.getID()))
				{
					sets.add(set);
					measurementids.addAll(set.getAllMeasurementIDs());
				}
			
			header.getSetInfos().clear();
			header.addSetInfos(sets);
			
			// collect the peaks we need
			if (options.verbose)
				System.out.println("Extracting sets");
			Vector<IPeakSet<IPeak>> peaksets = new Vector<IPeakSet<IPeak>>();
			for (IPeak peak : peaks)
			{
				Vector<IPeak> ps = new Vector<IPeak>();
				deconvolute(peak, measurementids, ps);
				if (ps.size() > 0)
					peaksets.add(new IPeakSet<IPeak>(ps));
			}
			
			// write the data
			if (options.verbose)
				System.out.println("Writing data");
			PeakMLWriter.write(
					header,
					peaksets,
					null, new GZIPOutputStream(new FileOutputStream(options.output)), null
				);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
