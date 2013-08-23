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



package mzmatch.ipeak.sort;


// java
import java.io.*;
import java.util.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;





public class PeakSort
{
	// implementation
	public static final int SORT_UNKOWN					= 0;
	public static final int SORT_SCAN_ASCENDING			= 1;
	public static final int SORT_SCAN_DESCENDING		= 2;
	public static final int SORT_MASS_ASCENDING			= 3;
	public static final int SORT_MASS_DESCENDING		= 4;
	public static final int SORT_INTENSITY_ASCENDING	= 5;
	public static final int SORT_INTENSITY_DESCENDING	= 6;
	public static final int SORT_ANNOTATION_ASCENDING	= 7;
	public static final int SORT_ANNOTATION_DESCENDING	= 8;
	
	public static int strToSort(String label)
	{
		if (label.equals("scanascending"))
			return SORT_SCAN_ASCENDING;
		else if (label.equals("scandescending"))
			return SORT_SCAN_DESCENDING;
		else if (label.equals("massascending"))
			return SORT_MASS_ASCENDING;
		else if (label.equals("massdescending"))
			return SORT_MASS_DESCENDING;
		else if (label.equals("intensityascending"))
			return SORT_INTENSITY_ASCENDING;
		else if (label.equals("intensitydescending"))
			return SORT_INTENSITY_DESCENDING;
		else if (label.equals("annotationascending"))
			return SORT_ANNOTATION_ASCENDING;
		else if (label.equals("annotationdescending"))
			return SORT_ANNOTATION_DESCENDING;
		else
			return SORT_UNKOWN;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "PeakSort";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"This tools sorts the contents of a PeakML and writes the result to a new file. A number " +
		"of sorting mechanisms is supported: intensity, mass, scan and annotation. When sorting " +
		"on annotation the -annotation options needs to be used to indicate on which label to " +
		"sort the list.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract the log-information\n" +
		"%JAVA% mzmatch.ipeak.sort.PeakSort -i unsorted.peakml -o sorted.peakml",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input file(s). The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the ouput file. The file is written in the PeakML file format and " +
			"peaks that passed the defined filter are saved here. When this option is not " +
			"set the output is written to the standard out. Be sure to unset the verbose " +
			"option when setting up a pipeline reading and writing from the standard in- " +
			"and outputs.")
		public String output = null;
		
		@Option(name="sort", param="see description", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"- scanascending|scandescending\n" +
			"  can be used to sort the list of peaks on retention time\n" +
			"- massascending|massdescending\n" +
			"  can be used to sort the list of peaks on mass\n" +
			"- intensityascending|intensitydescending\n" + 
			"  can be used to sort the list of peaks on intensity\n" +
			"- annotationascending|annotationdescending" +
			"  can be used to sort the list of peaks on an annotation")
		public String sort = null;
		@Option(name="annotation", param="see description", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"When either annotationascending or annotationdescending has been chosen for the " +
			"sort option, this option needs to be filled in for the label of the annotation to " +
			"sort on.")
		public String annotation = null;
		
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
			
			// convert the sort label to 
			int sort = strToSort(options.sort);
			if (sort == SORT_UNKOWN)
			{
				System.err.println("[ERROR]: unknown sort label: '" + options.sort + "'");
				System.exit(1);
			}
			
			// check the command-line parameters
			{
				if ((sort==SORT_ANNOTATION_ASCENDING||sort==SORT_ANNOTATION_DESCENDING) && options.annotation == null)
				{
					System.err.println("[ERROR]: trying to sort on annotation, but no annotation label has been given.");
					System.exit(1);
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
				}
				
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, options.input.size()==1);
			}
			
			
			// process the files
			for (String filename : options.input)
			{
				// open the streams
				InputStream input = System.in;
				if (filename != null)
					input = new FileInputStream(filename);
				
				OutputStream output = System.out;
				if (options.input.size() == 1)
				{
					if (options.output != null)
						output = new FileOutputStream(options.output);
				}
				else
				{
					String name = new File(filename).getName();
					output = new FileOutputStream(options.output + "/" + name);
				}
					
				// load the data
				if (options.verbose)
					System.out.println("Loading data.");
				ParseResult result = PeakMLParser.parse(input, true);
				Vector<IPeak> peaks = ((IPeakSet<IPeak>) result.measurement).getPeaks();
				
				// sort them according to the given sort criterium
				if (options.verbose)
					System.out.println("Sorting data.");
				switch (sort)
				{
				case SORT_SCAN_ASCENDING:
					Collections.sort(peaks, IPeak.sort_scanid_ascending);
					break;
				case SORT_SCAN_DESCENDING:
					Collections.sort(peaks, IPeak.sort_scanid_descending);
					break;
				case SORT_MASS_ASCENDING:
					Collections.sort(peaks, IPeak.sort_mass_ascending);
					break;
				case SORT_MASS_DESCENDING:
					Collections.sort(peaks, IPeak.sort_mass_descending);
					break;
				case SORT_INTENSITY_ASCENDING:
					Collections.sort(peaks, IPeak.sort_intensity_ascending);
					break;
				case SORT_INTENSITY_DESCENDING:
					Collections.sort(peaks, IPeak.sort_intensity_descending);
					break;
				case SORT_ANNOTATION_ASCENDING:
					Collections.sort(peaks, new IPeak.AnnotationAscending(options.annotation));
					break;
				case SORT_ANNOTATION_DESCENDING:
					Collections.sort(peaks, new IPeak.AnnotationDescending(options.annotation));
					break;
				}
				
				// dump the output
				if (options.verbose)
					System.out.println("Writing data.");
				PeakMLWriter.write(result.header, peaks, null, output, null);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}