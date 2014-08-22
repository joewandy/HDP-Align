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

// libraries
import cmdline.*;

// peakml
import peakml.*;

import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;





public class Example
{
	// implementation
	/*
	All methods needed locally for the tool, which are not general enough to make
	it to the library are located here.
	*/
	
	
	// main entrance
	/*
	Because the version and the application-name are used in multiple places, it
	is more convenient to declare the name static in this place.
	*/
	final static String version = "1.0.0";
	final static String application = "Example";
	@OptionsClass(name=application, version=version, author="", description=
		"General description of the function of the tool.")
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Descriptive text for the option.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Descriptive text for the option.")
		public String output = null;
		
		/*
		The following options are obligatory for each tool. The -h causes the help
		to be shown and the -v causes the tool to show progress information.
		*/
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
			/*
			Initializes the tool, which means that all the generic loggers used by
			the internal libraries are turned off.
			*/
			Tool.init();
			
			// parse the commandline options
			/*
			Retrieves the options set by the user.
			*/
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
				/*
				This is the place to check all the set options. For example when a
				ppm-value is obligatory it should be checked here. A warning is to be
				printed to the standard error in the following form:
				   '[ERROR]: descriptive text'
				upon which the application should exit.
				*/
			}
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// load the data
			ParseResult result = PeakMLParser.parse(input, true);
			
			// cast the measurement to something we can access
			IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			
			// process the data
			
			// write the data
			PeakMLWriter.write(
					result.header,		/* we re-use the header here, some adaption is probably needed */
					peaks.getPeaks(),	/* the peaks we want to write to the file */
					null,				/* room for a progress listener we're not using */
					output,				/* the output-stream, it's allowed to make this a GZIPOutputStream */
					null				/* for an associated stylesheet, usually not needed */
				);
		}
		catch (Exception e)
		{
			/*
			This is a catch-all for unexpected exceptions. In principle this
			should never occur as error-checking mechanisms should be in place.
			The method below creates a error-report plus a notification to
			the user where to find the report and where to send it.
			*/
			Tool.unexpectedError(e, application);
		}
	}
}
