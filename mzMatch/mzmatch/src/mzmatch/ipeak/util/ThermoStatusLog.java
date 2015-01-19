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

// libraries
import cmdline.*;

// peakml
import peakml.io.xrawfile.*;

// mzmatch
import mzmatch.util.*;





public class ThermoStatusLog
{
	// implementation
	
	
	// entry point
	final static String version = "1.0.0";
	final static String application = "ThermoStatusLog";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Extracts the status-logs from the given Thermo RAW files (for convenience it also " +
		"includes the trailer-extra logs) and outputs the logs of each controller in a separate " +
		"file stored in the RAW file into a separate tab-delimited text-file. The columns contain " +
		"the different types of logs stored and the rows the values for each scan.  The fact that " +
		"a single RAW file can contain multiple controllers, necessitates that the output is a " +
		"directory where multiple files can be written." +
		"\n\n" +
		"No warrenties are given about the contents, as this seems to be largely dependent on " +
		"which machine was used.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract the log-information\n" +
		"%JAVA% mzmatch.ipeak.util.ThermoStatusLog -i measurement.RAW -o measurement.log",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input file(s). These should be in the Thermo Raw file format produced " +
			"by Xcalibur.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the output destination. This is required to be a directory where all of " +
			"the generated  files are written. When the directory (or path leading to the directory " +
			"does not exist, it is created). For the filename convention used for the ouput files, " +
			"see the general description.")
		public String output = null;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
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
				// check whether we have an valid files
				if (options.input.size() == 0)
				{
					System.err.println("[ERROR]: at least one input file needs to be defined.");
					System.exit(0);
				}
				if (options.output == null)
				{
					System.err.println("[ERROR]: an output destination needs to be defined.");
					System.exit(0);
				}
				File outdir = new File(options.output);
				if (outdir.exists() && !outdir.isDirectory())
				{
					System.err.println("[ERROR]: output destination is not a directory.");
					System.exit(0);
				}
				
				// create the file paths
				Tool.createFilePath(options.output, false);
			}
			
			
			// start processing
			IXRawfile raw = new IXRawfile();
			int rtcode = raw.init();
			if (rtcode != IXRawfile.RTCODE_SUCCESS)
			{
				System.err.println("[ERROR]: could not connect to the Thermo xrawfile OLE connection.");
				System.exit(0);
			}
			
			for (String filename : options.input)
			{
				if (options.verbose)
					System.out.println("Processing '" + filename + "'");
				
				// open the raw-file
				raw.open(filename);
				
				// get the status log's for all the controllers
				String name = new File(filename).getName();
				for (int controllernumber=0; controllernumber<raw.getNumberOfControllers(); controllernumber++)
				{
					// retrieve the controller-type and activate it
					int controllertype = raw.getControllerType(controllernumber);	// 0-based
					raw.setCurrentController(controllertype, controllernumber+1);	// 1-based - the wankers
					
					String type = "unknown";
					if (controllertype == IXRawfile.CONTROLLER_MS)
						type = "ms";
					else if (controllertype == IXRawfile.CONTROLLER_UV)
						type = "uv";
					else if (controllertype == IXRawfile.CONTROLLER_PDA)
						type = "pda";
					else if (controllertype == IXRawfile.CONTROLLER_ANALOG)
						type = "analog";
					else if (controllertype == IXRawfile.CONTROLLER_AD_CARD)
						type = "a/d card";
					
					// open the output stream
					PrintStream out = new PrintStream(
							options.output
							+ "/" +
							name.substring(0, name.lastIndexOf('.')) + "__" + controllernumber + "_" + type + ".log"
						);
					
					// get min/max scannumber
					int minscannumber = raw.getFirstSpectrumNumber();
					int maxscannumber = raw.getLastSpectrumNumber();
					
					// retrieve the status-log
					Vector<String> keys = new Vector<String>();
					Vector<HashMap<String,String>> logs = new Vector<HashMap<String,String>>();
					for (int scannumber=minscannumber; scannumber<=maxscannumber; ++scannumber)
					{
						HashMap<String,String> statuslog = raw.getStatusLogForScanNum(scannumber);
						HashMap<String,String> trailerextra = raw.getTrailerExtraForScanNum(scannumber);
						if (statuslog==null || !statuslog.containsKey("RT"))
							continue;
						statuslog.putAll(trailerextra);
						
						// store the status-log
						logs.add(statuslog);
						
						// determine the unique keys
						for (String key : statuslog.keySet())
							if (!keys.contains(key)) keys.add(key);
					}
					
					// dump the header
					for (String key : keys)
						out.print("\t" + key);
					out.println();
					
					// dump the values
					for (int scannumber=minscannumber; scannumber<=maxscannumber; ++scannumber)
					{
						HashMap<String,String> log = logs.get(scannumber-minscannumber);
						
						out.print(scannumber);
						for (String key : keys)
							out.print("\t" + log.get(key));
						out.println();
					}
				}
				
				// dispose of the raw-file
				raw.close();
			}
			raw.dispose();
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
