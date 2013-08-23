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



package mzmatch.ipeak.filter;


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





public class BlankFilter
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "BlankFilter";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)", description=
		"With this tool a blank measurement can be subtracted from a normal measurement. Both " +
		"measurements should be from the same batch and preferably the last blank run before " +
		"the normal measurement should be subtracted. Peaks from the measurements are automatically " +
		"matched together with the ppm-setting. When the difference in intensity level is less than " +
		"3-fold two peaks are considered a match and the peak from the normal measurement is " +
		"rejected." +
		"\n\n" +
		"With the rejected option all the rejected peaks can be stored in a separate file. Both " +
		"the normal measurement peaks as the matched blank peaks are stored in a set in this file " +
		"for inspection.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM process a single file\n" +
		"%JAVA% mzmatch.ipeak.filter.SimpleFilter -blank blank.peakml -i input.peakml -o output.peakml -ppm 3",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The input file (in PeakML format) containing the peaks from the normal measurement. " +
			"When this option is not set the input is read from the standard input.")
		public Vector<String> input = new Vector<String>();
		@Option(name="blank", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The blank file (in PeakML format) containing the peaks from the blank measurement. This " +
			"option is required.")
		public String blank = null;
		
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The output file (in PeakML format) containing all the peaks from the normal measurement, " +
			"which did not have a match in the blank measurement. When this option is not set the " +
			"output is written to the standard output.")
		public String output = null;
		@Option(name="rejected", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional file (in PeakML format) containing all the rejected peaks from the normal " +
			"measurement plus their match from the blank measurement. This file can be used " +
			"for inspection and scavenging purposes.")
		public String rejected = null;
	
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The precision of the data in parts-per-million. This accuracy value is used for " +
			"matching the peaks from the blank to the peaks in the measurement.")
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
		final int BLANK = 0;
		final int SAMPLE = 1;
		
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
				// verify whether the ppm was set
				if (options.ppm == -1)
				{
					System.err.println("[ERROR]: ppm-value has not been set.");
					System.exit(0);
				}
				
				// verify that the blank was set and exists
				if (options.blank == null)
				{
					System.err.println("[ERROR]: no file for the blank has been set.");
					System.exit(0);
				}
				else if (!(new File(options.blank).exists()))
				{
					System.err.println("[ERROR]: the selected blank file does not exist.");
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
					if (options.rejected!=null && new File(options.output).compareTo(new File(options.rejected))==0)
					{
						System.err.println("[ERROR]: with multiple input the output destination and the rejected destination cannot be the same.");
						System.exit(0);
					}
				}
				
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, options.input.size()==1);
				if (options.rejected != null)
					Tool.createFilePath(options.rejected, options.input.size()==1);
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading blank");
			ParseResult res_blank = PeakMLParser.parse(new FileInputStream(options.blank), true);
			IPeakSet<IPeak> blank = (IPeakSet<IPeak>) res_blank.measurement;
			if (!blank.getContainerClass().equals(MassChromatogram.class))
			{
				System.err.println("[ERROR]: Blank file expected to be a single measurement.");
				System.exit(0);
			}
			for (IPeak peak : blank)
				peak.setMeasurementID(BLANK);
			
			// start processing
			for (String filename : options.input)
			{
				// open the streams
				InputStream input = System.in;
				if (filename != null)
					input = new FileInputStream(filename);
				
				OutputStream out_rejected = null;
				OutputStream out_output = System.out;
				if (options.input.size() == 1)
				{
					if (options.output != null)
						out_output = new FileOutputStream(options.output);
					if (options.rejected != null)
						out_rejected = new FileOutputStream(options.rejected);
				}
				else
				{
					String name = new File(filename).getName();
					out_output = new FileOutputStream(options.output + "/" + name);
					if (options.rejected != null)
						out_rejected = new FileOutputStream(options.rejected + "/" + name);
				}
				
				if (options.verbose)
					System.out.println("Loading sample");
				ParseResult res_sample = PeakMLParser.parse(input, true);
				IPeakSet<IPeak> sample = (IPeakSet<IPeak>) res_sample.measurement;
				for (IPeak peak : sample)
					peak.setMeasurementID(SAMPLE);
				
				// both of the files should contain only one measurement
				if (res_sample.header.getNrMeasurementInfos()!=1 || res_blank.header.getNrMeasurementInfos()!=1)
				{
					System.err.println("[ERROR]: both the blank as the input file can only contain a single measurement.");
					System.exit(0);
				}
				
				// make the match
				Vector<IPeakSet<IPeak>> spectra = new Vector<IPeakSet<IPeak>>();
				spectra.add(blank);
				spectra.add(sample);
				
				if (options.verbose)
					System.out.println("Matching the blank to the sample");
				Vector<IPeakSet<IPeak>> matches = IPeak.match(spectra, options.ppm, new IPeak.MatchCompare<IPeak>() {
					public double distance(IPeak peak1, IPeak peak2) {
						if (Math.abs(peak1.getRetentionTime()-peak2.getRetentionTime()) > 20)
							return -1;
						
						double minintensity = Math.min(peak1.getIntensity(), peak2.getIntensity());
						double maxintensity = Math.max(peak1.getIntensity(), peak2.getIntensity());
						if (maxintensity / minintensity > 3)
							return -1;
						
						MassChromatogram<Peak> mc1 = (MassChromatogram<Peak>) peak1;
						MassChromatogram<Peak> mc2 = (MassChromatogram<Peak>) peak2;
						return mc1.getSignal().compareTo(mc2.getSignal());
					}
				});
				
				// dump only the stuff we want to keep
				if (options.verbose)
					System.out.println("Filter out the matches");
				Vector<IPeak> peaks = new Vector<IPeak>();
				Vector<IPeakSet<IPeak>> rejects = new Vector<IPeakSet<IPeak>>();
				for (IPeakSet<IPeak> peakset : matches)
				{
					boolean add = true;
					for (IPeak peak : peakset)
					{
						if (peak.getMeasurementID() == BLANK)
							add = false;
					}
					
					if (add == true)
						peaks.add(peakset.getPeaks().firstElement());
					else
						rejects.add(peakset);
				}
				
				// dump all the matches
				if (out_rejected != null)
				{
					if (options.verbose)
						System.out.println("Writing the rejects");
					
					// general properties
					Header header = new Header();
					header.setNrPeaks(rejects.size());
					
					// measurement properties
					MeasurementInfo measurement_blank = new MeasurementInfo(BLANK, res_blank.header.getMeasurementInfo(0));
					measurement_blank.setLabel("blank");
					measurement_blank.addFileInfo(new FileInfo("blank", options.blank));
					
					MeasurementInfo measurement_sample = new MeasurementInfo(SAMPLE, res_sample.header.getMeasurementInfo(0));
					measurement_sample.setLabel("sample");
					measurement_sample.addFileInfo(new FileInfo("measurement", filename));
					
					// set properties
					SetInfo set =  new SetInfo("", SetInfo.SET);
					set.addChild(new SetInfo("blank", SetInfo.SET, BLANK));
					set.addChild(new SetInfo("sample", SetInfo.SET, SAMPLE));
					header.addSetInfo(set);
					
					// write the data
					PeakMLWriter.writeIPeakSets(header, rejects, null, new GZIPOutputStream(out_rejected), null);
				}
				
				// dump the results
				if (options.verbose)
					System.out.println("Writing the results");
				
				res_sample.header.setNrPeaks(peaks.size());
				for (IPeak peak : peaks)
					peak.setMeasurementID(0);
				PeakMLWriter.write(res_sample.header, peaks, null, new GZIPOutputStream(out_output), null);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
