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
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.util.*;






public class SimpleFilter
{
	// implementation
	@SuppressWarnings("unchecked")
	public static int numberDetections(IPeak peak)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			int nrdetections = 0;
			for (IPeak p : (IPeakSet<IPeak>) peak)
				nrdetections += numberDetections(p);
			return nrdetections;
		}
		return 1;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "SimpleFilter";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)", description=
		"Simple filter tool for PeakML files. The filter works on the top-most data of an entry (in the " +
		"case of a peak-set the compounded information of that set is used) and can be used to " +
		"filter out entries not conforming to the specified filter(s). As the filter only works on top " +
		"level data, the easiest filters are based on the top level properties: " +
		"mass, intensity, retention time and scanid. For each of these properties the appropriate " +
		"filters have been defined. Additionally, as we have access to mass information, the tool " +
		"also allows the user to load databases (in moleculedb format) and set a ppm-value to " +
		"filter out those entries that match to a molecule in the databases." +
		"\n\n" +
		"For peaksets the filter for minimum number of detections removes all peaksets with a " +
		"number of peaks below the set threshold." +
		"\n\n" +
		"Please note that the filter-commands are stacked when multiple are given. For example, when " +
		"setting a minimum intensity and mindetections are both set, all entries not qualifying " +
		"both criteria are removed.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM process a single file\n" +
		"%JAVA% mzmatch.ipeak.filter.SimpleFilter -i input.peakml -o output.peakml -minintensity 100000",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input file(s). The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in. When multiple input files are set " +
			"the output needs to be set to a directory.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the ouput file. The file is written in the PeakML file format and " +
			"peaks that passed the defined filter are saved here. When this option is not " +
			"set the output is written to the standard out. Be sure to unset the verbose " +
			"option when setting up a pipeline reading and writing from the standard in- " +
			"and outputs.")
		public String output = null;
		@Option(name="rejected", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the reject file. The file is written in the PeakML file format and " +
			"peaks that have not passed the defined filter are saved here.")
		public String rejected = null;
		
		@Option(name="databases", param="[filename]", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional file containing molecules to filter on. The file should be in a tab-delimited " +
			"format with name \t formula. The mass is automatically calculated from the formula " +
			"and used, together with the ppm option, to find all mass chromatograms within the " +
			"mass window.")
		public Vector<String> databases = new Vector<String>();
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional ppm-value, which is used in conjunction with the molecule-file. For each " +
			"molecule the ppm-value is used to determine the mass window in which to look for " +
			"mass chromatograms matching the molecule.")
		public double ppm = 0;
		
		@Option(name="n", param="integer", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value indicating the maximum number peaks to filter from the file. When this " +
			"value is not set all peaks are taken into acount.")
		public int n = -1;
		@Option(name="offset", param="integer", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for skipping the first number of peaks. When this value is not set the " +
			"application starts at the beginning.")
		public int offset = 0;
		
		@Option(name="mindetections", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Works only for peak-sets; checks whether a set contains the minimum number of peaks.")
		public int mindetections = -1;
		
		@Option(name="minscanid", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks above the given minimum scanid. " +
			"When this value has not been set all peaks are taken into account.")
		public double minscanid = -1;
		@Option(name="maxscanid", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks below the given maximum scanid. " +
			"When this value has not been set all peaks are taken into account.")
		public double maxscanid = -1;
		@Option(name="minretentiontime", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks above the given minimum retentiontime. " +
			"When this value has not been set all peaks are taken into account.")
		public double minretentiontime = -1;
		@Option(name="maxretentiontime", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks below the given maximum retentiontime. " +
			"When this value has not been set all peaks are taken into account.")
		public double maxretentiontime = -1;
		@Option(name="minmass", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks above the given minimum mass. " +
			"When this value has not been set all peaks are taken into account.")
		public double minmass = -1;
		@Option(name="maxmass", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks below the given maximum mass. " +
			"When this value has not been set all peaks are taken into account.")
		public double maxmass = -1;
		@Option(name="minintensity", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks above the given minimum intensity. " +
			"When this value has not been set all peaks are taken into account.")
		public double minintensity = -1;
		@Option(name="maxintensity", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks below the given maximum intensity. " +
			"When this value has not been set all peaks are taken into account.")
		public double maxintensity = -1;
		@Option(name="annotations", param="label", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional value for taking into account only those peaks containing the given annotations.")
		public Vector<String> annotations = new Vector<String>();
		
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
				if (options.databases.size()!=0 && options.ppm==0)
				{
					System.err.println("[ERROR]: The ppm-value needs to be set when filtering on molecules.");
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
			
			
			// if needed, load the databases
			HashMap<String,Molecule> molecules = null;
			if (options.databases.size() != 0)
			{
				molecules = new HashMap<String,Molecule>();
				for (String file : options.databases)
					molecules.putAll(MoleculeIO.parseXml(new FileInputStream(file)));
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
				
				// load the data
				if (options.verbose)
					System.out.println("reading the input");
				ParseResult result = PeakMLParser.parse(input, true);
				
				// check whether the contents is what we expect
				if (!result.measurement.getClass().equals(IPeakSet.class))
				{
					System.err.println("[ERROR]: The contents of the input file was not a set of peaks.");
					System.exit(0);
				}
				
				// cast the set
				IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;
				if (options.offset >= peakset.size())
				{
					System.err.println("[ERROR]: The given offset is larger than the number of peaks " + peakset.size());
					System.exit(0);
				}
				
				// create the containers
				Vector<IPeak> subset = new Vector<IPeak>();
				Vector<IPeak> rejected = new Vector<IPeak>();
				
				// when a list with molecules to check for has been provided
				if (molecules != null)
				{
					if (options.verbose)
						System.out.println("filtering on molecules");
					
					for (Molecule molecule : molecules.values())
					{
						double mass = molecule.getMass(Mass.MONOISOTOPIC);
						double delta = PeriodicTable.PPM(mass, options.ppm);
						
						Vector<IPeak> neighbourhood = peakset.getPeaksOfMass(mass, delta);
						for (IPeak peak : neighbourhood)
						{
							Annotation annotation = peak.getAnnotation("identification");
							if (annotation != null)
							{
								peak.addAnnotation(
										"identification",
										annotation.getValue() + ", " + molecule.getDatabaseID()
									);
							}
							else
							{
								peak.addAnnotation("identification", molecule.getDatabaseID());
								subset.add(peak);
							}
						}
					}
					
					peakset = new IPeakSet<IPeak>(subset);
					subset = new Vector<IPeak>();
				}
				
				// apply the filters
				if (options.verbose)
					System.out.println("applying filters");
				int endindex = (options.n!=-1 ? options.offset+options.n : peakset.size());
				if (endindex > peakset.size())
					endindex = peakset.size();
				for (int i=options.offset; i<endindex; ++i)
				{
					IPeak peak = peakset.get(i);
					
					boolean valid = true;
					if (options.minscanid!=-1 && peak.getScanID()<options.minscanid)
						valid = false;
					if (options.maxscanid!=-1 && peak.getScanID()>options.maxscanid)
						valid = false;
					if (options.minretentiontime!=-1 && peak.getRetentionTime()<options.minretentiontime)
						valid = false;
					if (options.maxretentiontime!=-1 && peak.getRetentionTime()>options.maxretentiontime)
						valid = false;
					if (options.minmass!=-1 && peak.getMass()<options.minmass)
						valid = false;
					if (options.maxmass!=-1 && peak.getMass()>options.maxmass)
						valid = false;
					if (options.minintensity!=-1 && peak.getIntensity()<options.minintensity)
						valid = false;
					if (options.maxintensity!=-1 && peak.getIntensity()>options.maxintensity)
						valid = false;
					if (options.mindetections!=-1 && numberDetections(peak)<options.mindetections)
						valid = false;
					if (options.annotations.size() != 0)
					{
						valid = false;
						if (peak.getAnnotations() != null)
						{
							for (Annotation annotation : peak.getAnnotations().values())
								if (options.annotations.contains(annotation.getLabel()))
								{
									valid = true;
									break;
								}
						}
					}
					
					if (valid)
						subset.add(peak);
					else
						rejected.add(peak);
				}
				
				// write the file
				if (options.verbose)
					System.out.println("writing the results");
				
				result.header.setNrPeaks(subset.size());
				PeakMLWriter.write(result.header, (Vector) subset, null, new GZIPOutputStream(out_output), null);
				
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
