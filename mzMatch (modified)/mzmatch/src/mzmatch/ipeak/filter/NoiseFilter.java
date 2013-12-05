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





public class NoiseFilter
{
	// implementation
	@SuppressWarnings("unchecked")
	public static double codadw(IPeak peak)
	{
		if (peak.getClass().equals(MassChromatogram.class))
			return ((MassChromatogram<? extends Peak>) peak).codaDW();
		else if (peak.getClass().equals(IPeakSet.class))
		{
			IPeakSet<? extends IPeak> peakset = (IPeakSet<? extends IPeak>) peak;
			
			// calculate the top score
			double max = Double.MIN_VALUE;
			for (IPeak p : peakset)
				max = Math.max(codadw(p), max);
			return max;
		}
		else
			return -1;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "NoiseFilter";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Filters noise from PeakML files, containing mass chromatograms at the lowest level. When the " +
		"file contains a list of sets of mass chromatograms, the maximum score for the used method " +
		"is calculated and compared to the given threshold. This is the best approach, as we expect " +
		"that high quality can match up to low quality signals at the same mass and retention time. " +
		"Only those entries scoring above the given threshold are stored in the output file. The " +
		"rejected can be stored in a separate file (option 'rejected') for inspection or recovery." +
		"\n\n" +
		"The option 'codadw' can be used to set the threshold for the CoDA Durbin-Watson noise filtering " +
		"approach. Normally the Durbin-Watson criterion results in a value between 0 and 4, where higher means a " +
		"large amount of periodicity in the signal and lower vica versa. For mass chromatograms we expect " +
		"little periodicity in the signal, thus a lower value is preferable. However, in order to preserve " +
		"unity in our quality scores the CoDA-DW score is scaled between 0..1, where higher is better (less " +
		"periodicity in the signal). As a general rule-of-thumb, for high quality mass chromatograms a " +
		"score >0.8 is expected." +
		"\n\n" +
		"Remarks:\n" +
		"- CoDA-DW is scaled between 0..1, where higher is better mass chromatogram quality.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM process a single file\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -ppm 3 -i file.mzXML -o file.peakml\n"+
		"\n" +
		"REM remove noise\n" +
		"%JAVA% mzmatch.ipeak.filter.NoiseFilter -v -i file.peakml -o file_signals.peakml -rejected file_noise.peakml\n",
		references=
		"1. Windig W: The use of the Durbin-Watson criterion for noise and background reduction of complex liquid chromatography/mass spectrometry data and a new algorithm to determine sample differences. Chemometrics and Intelligent Laboratory Systems. 2005, 77:206-214."
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input file(s). The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the ouput file. The file is written in the PeakML file format and " +
			"peaks that passed the noise filter are saved here. When this option is not " +
			"set the output is written to the standard out.\n" +
			"Be sure to unset the verbose option when setting up a pipeline reading and writing " +
			"from the standard in- and outputs.")
		public String output = null;
		@Option(name="rejected", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the file where to write all the rejected peaksets. When this option " +
			"has not been set the rejected peaksets are not written.")
		public String rejected = null;
		
		@Option(name="codadw", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"With this option the CoDA-DW filter is activated. The CoDA algorithm calculates " +
			"a quality value for mass chromatograms (MCQ value [0..1] where higher is better) " +
			"with the Durbin-Watson statistic, which is used to remove invalid peaks. The " +
			"double value defines the hard threshold on which the mass chromatograms are removed " +
			"from the set.")
		public double codadw = -1;
		
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
				// check the coda-dw
				if (options.codadw == -1)
				{
					System.err.println("[ERROR]: the coda-dw value needs to be set.");
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
			
			
			// process the files
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
				
				// load the peaks
				if (options.verbose)
					System.out.println("Loading '" + filename + "'.");
				ParseResult result = PeakMLParser.parse(input, true);
				
				IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
				
				// keep track of all that's discarded
				Vector<IPeak> accepted = new Vector<IPeak>();
				Vector<IPeak> rejected = new Vector<IPeak>();
				
				// start filtering
				if (options.verbose)
					System.out.println("Calculating the signal-scores");
				
				// apply the CoDA Durbin Watson statistic
				for (IPeak peak : peaks)
				{
					double mcq = codadw(peak);
					peak.addAnnotation("codadw", Double.toString(mcq), Annotation.ValueType.DOUBLE);
					
					if (mcq >= options.codadw)
						accepted.add(peak);
					else
						rejected.add(peak);
				}
				
				// save the results
				if (options.verbose)
					System.out.println("Writing valid");
				PeakMLWriter.write(result.header, accepted, null, new GZIPOutputStream(out_output), "");
				
				if (out_rejected != null)
				{
					if (options.verbose)
						System.out.println("Writing rejected");
					result.header.setNrPeaks(rejected.size());
					PeakMLWriter.write(result.header, rejected, null, new GZIPOutputStream(out_rejected), "");
				}
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}

