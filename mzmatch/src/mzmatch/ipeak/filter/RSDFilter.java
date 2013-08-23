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

// metabolomics
import peakml.*;
import peakml.math.*;

import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;





public class RSDFilter
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "RSDFilter";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Filters all the non-reproducibly detected signals from the given PeakML file. The approach loads " +
		"the PeakML file and determines based on the set-information, which of the sets of peaks " +
		"contain signals that were not reproducibly detected in the replicates. The reproducability measure " +
		"is defined as the Reproducibility Standard Deviation (RSD; stddev/mean), which is calculated " +
		"from all the intensity values in a set. When the PeakML file contains multiple sets (in the " +
		"header) the RSD is evaluated for each of the sets, and an entry is discarded when all of the " +
		"sets fail the set threshold.",
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
		"%JAVA% mzmatch.ipeak.Combine -v -i *hr_rsd.peakml -o timeseries.peakml -ppm 3 -rtwindow 30 -combination set\n",
		references=
		"1. Shah V, Midha K, Findlay J, et al: Bioanalytical Method Validation—A Revisit with a Decade of Progress. Pharmaceutical Research. 2000, 17:1551-1557"
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input file(s). The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the ouput file(s). The file is written in the PeakML file format and " +
			"peaks that passed the defined filter are saved here. When this option is not " +
			"set the output is written to the standard out. Be sure to unset the verbose " +
			"option when setting up a pipeline reading and writing from the standard in- " +
			"and outputs. When multiple input files are defined this option needs to point " +
			"to a directory.")
		public String output = null;
		@Option(name="rejected", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the file where to write all the rejected peaksets. When this option " +
			"has not been set the rejected peaksets are not written.")
		public String rejected = null;

		@Option(name="rsd", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The RSD value as a percentage between [0..1]. This percentage indicates the maximum " +
			"an individual measurement can deviate from the mean intensity of all the peaks in " +
			"the set.")
		public double rsd = -1;

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
			final Options options = new Options();
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
				if (options.rsd<0 || options.rsd>1)
				{
					System.err.println("[ERROR]: The RSD value needs to be set between 0 and 1.");
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
				
				// load the data, it needs to be sets of data otherwise this tool is useless!
				if (options.verbose)
					System.out.println("Loading data");
				ParseResult result = PeakMLParser.parseIPeakSet(input, null);
				
				if (result.header.getNrSetInfos() == 0)
				{
					System.err.println("[ERROR]: the file '" + filename + "' contains no sets.");
					System.exit(0);
				}
				
				// cast the measurement to the correct type
				IPeakSet<IPeakSet<? extends IPeak>> peaksets = (IPeakSet<IPeakSet<? extends IPeak>>) result.measurement;
				
				// process the data and select and reject the peaks
				if (options.verbose)
					System.out.println("Processing data");
				Vector<IPeak> selectedpeaks = new Vector<IPeak>();
				Vector<IPeak> rejectedpeaks = new Vector<IPeak>();
				
				// create an array which will contain all the intensity values
				for (IPeakSet<? extends IPeak> peakset : peaksets)
				{
					boolean stable = false;
					for (SetInfo set : result.header.getSetInfos())
					{
						// initialize the array to 0
						double values[] = new double[set.getNrMeasurementIDs()];
						for (int i=0; i<values.length; ++i)
							values[i] = 0;
							
						for (IPeak peak : peakset)
							if (set.containsMeasurementID(peak.getMeasurementID()))
								values[set.getMeasurementIDs().indexOf(peak.getMeasurementID())] = peak.getIntensity();
						
						double mean = Statistical.mean(values);
						double stddev = Statistical.stddev(values);
						
						peakset.addAnnotation("rsd", Double.toString(stddev/mean), Annotation.ValueType.DOUBLE);
						
						if (stddev/mean <= options.rsd)
							stable = true;
					}
					
					if (stable)
						selectedpeaks.add(peakset);
					else
						rejectedpeaks.add(peakset);
				}
				
				// write the results to the output streams
				if (options.verbose)
					System.out.println("Writing data");
				if (out_rejected != null)
				{
					result.header.setNrPeaks(rejectedpeaks.size());
					PeakMLWriter.write(result.header, rejectedpeaks, null, new GZIPOutputStream(out_rejected), null);
				}
				
				result.header.setNrPeaks(selectedpeaks.size());
				PeakMLWriter.write(result.header, selectedpeaks, null, new GZIPOutputStream(out_output), null);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
