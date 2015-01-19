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
import java.util.zip.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.math.*;

import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;






@SuppressWarnings("unchecked")
public class Normalisation
{
	// implementation
	enum Type
	{
		UNKNOWN,
		QUANTILE,
		ZNORMALIZATION
	}
	
	public static Type getType(String type)
	{
		String lowercase = type.toLowerCase();
		if (lowercase.equals("quantile"))
			return Type.QUANTILE;
		else if (lowercase.equals("znormalization"))
			return Type.ZNORMALIZATION;
		else
			return Type.UNKNOWN;
	}
	
	public static void normalize(IPeak peak, double newmaxintensity)
	{
		if (peak.getClass().equals(MassChromatogram.class))
		{
			MassChromatogram<? extends Peak> masschromatogram = (MassChromatogram<? extends Peak>) peak;
			for (Peak p : masschromatogram.getPeaks())
				p.setIntensity(p.getIntensity() * (newmaxintensity / masschromatogram.getIntensity()));
		}
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "Normalisation";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"General tool to perform basic normalisation on a PeakML file resulting from the " +
		"Combine process (ie each entry is the combination of peaks from multiple measurements). " +
		"The commonly used z-score and quantile normalisation procedures have been implemented, " +
		"which adjust the intensity-values according to the factors they find.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM normalize the contents of a combined file\n" +
		"%JAVA% mzmatch.ipeak.normalisation.Normalisation -v -i combined.peakml -o normalised.peakml -type zscore\n",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input file. The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in. The tool expects a combination " +
			"of peaks from different sets and will exit when this is not encountered.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the ouput file. The file is written in the PeakML file format and " +
			"peaks that passed the defined filter are saved here. When this option is not " +
			"set the output is written to the standard out. Be sure to unset the verbose " +
			"option when setting up a pipeline reading and writing from the standard in- " +
			"and outputs.")
		public String output = null;
		
		@Option(name="type", param="see description", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"- znormalization\n" +
			"  Z-score normalization, scales the intensity values to 0 mean and 1 stddev." +
			"- quantile" +
			"  Quantile normalisation, essentially rank-based normalisation.")
		public String type = null;
		 
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
			
			// parse the type of normalization
			Type type = getType(options.type);
			
			// check the command-line parameters
			{
				if (type == Type.UNKNOWN)
				{
					System.err.println("[ERROR]: Unknown normalization type: '" + options.type + "'");
					System.exit(0);
				}
				
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, true);
			}
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			
			// load the data
			if (options.verbose)
				System.out.println("loading data");
			ParseResult result = PeakMLParser.parseIPeakSet(input, null);
			IPeakSet<IPeakSet<? extends IPeak>> peaks = (IPeakSet<IPeakSet<? extends IPeak>>) result.measurement;
			
			// create a matrix out of the whole data
			if (options.verbose)
				System.out.println("calculating the normalisation factors");
			DataFrame.Double data = new DataFrame.Double(peaks.size(), result.header.getNrMeasurementInfos());
			
			for (int i=0; i<result.header.getNrMeasurementInfos(); ++i)
				data.setColName(i, result.header.getMeasurementInfo(i).getLabel());
			
			for (int i=0; i<peaks.size(); ++i)
			{
				IPeakSet<? extends IPeak> peakset = peaks.get(i);
				
				data.setRowName(i, Double.toString(peakset.getMass()));
				for (IPeak peak : peakset)
					data.set(i, result.header.indexOfMeasurementInfo(peak.getMeasurementID()), peak.getIntensity());
			}
			
			// quantile normalization
			if (type == Type.QUANTILE)
			{
				// get the ranks for each column
				double ranks[][] = Statistical.rank(data.getMatrix(), true, Statistical.COLUMN);
				
				// TODO move the .5 to real integers
				
				// create the mean values for each rank
				double mean[] = new double[data.getNrRows()];
				for (int rank=0; rank<data.getNrRows(); ++rank)
				{
					double values[] = new double[data.getNrColumns()];
					for (int row=0; row<data.getNrRows(); ++row)
						for (int col=0; col<data.getNrColumns(); ++col)
						{
							int upperrank = (int) Math.ceil(ranks[row][col]);
							int lowerrank = (int) Math.floor(ranks[row][col]);
							if (lowerrank==rank || upperrank==rank)
								values[col] = data.get(row, col);
						}
					
					mean[rank] = Statistical.mean(values);
				}
				
				// apply the normalization factors
				for (int setid=0; setid<peaks.size(); ++setid)
				{
					IPeakSet<? extends IPeak> peakset = peaks.get(setid);
					
					for (int peakid=0; peakid<peakset.size(); ++peakid)
					{
						int rank = (int) Math.ceil(ranks[setid][peakid]);
						
						// prevent race-condition where Math.ceil makes index out of bounds
						rank = Math.max(0, rank);
						rank = Math.min(mean.length-1, rank);
						normalize(peakset.get(peakid), mean[rank]);
					}
				}
			}
			else if (type == Type.ZNORMALIZATION)
			{
				double scaled[][] = Statistical.scale(data.getMatrix(), Statistical.ROW);
				for (int setid=0; setid<peaks.size(); ++setid)
				{
					IPeakSet<? extends IPeak> peakset = peaks.get(setid);
					for (int peakid=0; peakid<peakset.size(); ++peakid)
						normalize(peakset.get(peakid), scaled[setid][peakid]);
				}
			}
			
			// write the results
			if (options.verbose)
				System.out.println("writing the results");
			PeakMLWriter.write(
					result.header, peaks.getPeaks(), null,
					new GZIPOutputStream(output),
					null
				);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
