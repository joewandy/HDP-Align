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





public class FilterOnRelationIDs
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "FilterOnRelationIDs";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)", description=
		"Retrieves all entries annotated with one of the given list of relation-ids. This list can " +
		"either be specified with a text-file, where each line contains a single relation-id, or " +
		"a PeakML file, where each entry containing a relation-id annotation is used to build up " +
		"the relation-id list." +
		"\n\n" +
		"This tool is very practical when manualy selecting strong peaks from the extracted set of " +
		"peaks. By running 'mzmatch.ipeak.sort.RelatedPeaks' two files can be generated. The result " +
		"file containing all the entries, and the basepeaks file containing the most intense entry " +
		"of each cluster of related derivatives. The basepeaks file is a drastical reduction of the " +
		"complete file and can more easily be manually filtered with for example the PeakML Viewer. " +
		"The resulting file can then be used as input to this tool, in order to retrieve the complete " +
		"clusters of interest.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM process a single file\n" +
		"%JAVA% mzmatch.ipeak.filter.FilterOnRelationIDs -relationids ids.txt -i input.peakml -o output.peakml",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input file(s). The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in.")
		public Vector<String> input = new Vector<String>();
		@Option(name="relationids", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Either a text-file with the relation-id's (1 id per line) or a PeakML file " +
			"with entries containing the id's to filter on.")
		public String relationids = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the ouput file(s). The file is written in the PeakML file format and " +
			"peaks that passed the defined filter are saved here. When this option is not " +
			"set the output is written to the standard out. Be sure to unset the verbose " +
			"option when setting up a pipeline reading and writing from the standard in- " +
			"and outputs. When multiple input files are defined this option needs to point " +
			"to a directory.")
		public String output = null;
		@Option(name="rejected", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the reject file. The file is written in the PeakML file format and " +
			"peaks that have not passed the defined filter are saved here.")
		public String rejected = null;
		
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
				if (options.relationids == null)
				{
					System.err.println("[ERROR]: the relation-ids filename was not set.");
					System.exit(0);
				}
				if (!new File(options.relationids).exists())
				{
					System.err.println("[ERROR]: the relation-ids file '" + options.relationids + "' does not exist.");
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
			
			
			// open the streams
			for (String filename : options.input)
			{
				// open the streams
				InputStream input = System.in;
				if (filename != null)
					input = new FileInputStream(filename);
				
				OutputStream output = System.out;
				OutputStream out_rejected = null;
				if (options.input.size() == 1)
				{
					if (options.output != null)
						output = new FileOutputStream(options.output);
					if (options.rejected != null)
						out_rejected = new FileOutputStream(options.output);
				}
				else
				{
					String name = new File(filename).getName();
					output = new FileOutputStream(options.output + "/" + name);
					if (options.rejected != null)
						out_rejected = new FileOutputStream(options.rejected + "/" + name);
				}
				
				// load the data
				if (options.verbose)
					System.out.println("Loading input data");
				ParseResult result = PeakMLParser.parse(input, true);
				
				// retrieve the relation
				if (options.verbose)
					System.out.println("Loading the relation ids");
				Vector<Integer> relationids = new Vector<Integer>();
				try
				{
					// try whether its a peakml file
					ParseResult res_relationids = PeakMLParser.parse(
							new FileInputStream(options.relationids), false
						);
					
					if (options.verbose)
						System.out.println("- relation-ids found to be peakml format");
					
					for (IPeak peak : (IPeakSet<IPeak>) res_relationids.measurement)
					{
						Annotation annotation = peak.getAnnotation(IPeak.relationid);
						if (annotation != null)
							relationids.add(annotation.getValueAsInteger());
					}
				}
				catch (Exception e)
				{
					// apparently its text
					if (options.verbose)
						System.out.println("- relation-ids found to be text format");
					
					String line;
					BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(options.relationids)));
					while ((line = in.readLine()) != null)
						relationids.add(Integer.parseInt(line));
				}
				
				// process the data
				if (options.verbose)
					System.out.println("Filtering");
				Vector<IPeak> peaks = new Vector<IPeak>();
				Vector<IPeak> rejected = new Vector<IPeak>();
				for (IPeak peak : (IPeakSet<IPeak>) result.measurement)
				{
					Annotation annotation = peak.getAnnotation(IPeak.relationid);
					if (annotation!=null && relationids.contains(annotation.getValueAsInteger()))
						peaks.add(peak);
					else
						rejected.add(peak);
				}
				
				// write the data
				if (options.verbose)
					System.out.println("Writing output");
				PeakMLWriter.write(result.header, peaks, null, new GZIPOutputStream(output), null);
				
				if (options.rejected != null)
				{
					result.header.setNrPeaks(rejected.size());
					PeakMLWriter.write(result.header, (Vector) rejected, null, new GZIPOutputStream(out_rejected), null);
				}
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
