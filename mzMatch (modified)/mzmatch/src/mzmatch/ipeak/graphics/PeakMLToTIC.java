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



package mzmatch.ipeak.graphics;


// java
import java.io.*;
import java.util.*;

import java.awt.*;
import java.awt.image.*;

import javax.imageio.ImageIO;

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
public class PeakMLToTIC
{
	// implementation
	public static BufferedImage processIPeaks(Options options, IPeakSet<? extends IPeak> peaks, Header header)
	{
		// retrieve the maximum scan-id
		int maxscan = 0;
		for (IPeak peak : peaks)
		{
			if (peak.getClass().equals(BackgroundIon.class))
				maxscan = Math.max(maxscan, ((BackgroundIon<Peak>) peak).getMaxScanID());
			else if (peak.getClass().equals(MassChromatogram.class))
				maxscan = Math.max(maxscan, ((MassChromatogram<Peak>) peak).getMaxScanID());
			else
				maxscan = Math.max(maxscan, peak.getScanID());
		}
		
		// create the tic
		double tic[] = new double[maxscan+1];
		for (int i=0; i<maxscan; ++i)
			tic[i] = 0;
		
		for (IPeak peak : peaks)
		{
			if (peak.getClass().equals(BackgroundIon.class))
				for (Peak p : (BackgroundIon<Peak>) peak)
					tic[p.getScanID()] += p.getIntensity();
			else if (peak.getClass().equals(MassChromatogram.class))
				for (Peak p : (BackgroundIon<Peak>) peak)
					tic[p.getScanID()] += p.getIntensity();
			else
				tic[peak.getScanID()] += peak.getIntensity();
		}
		
		// create the graph image
		BufferedImage img = new BufferedImage(options.width, options.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		
		g.drawImage(
				new Signal(tic).createGraphImage(options.input, "scans", "total intensity", options.width, options.height),
				0, options.height, null
			);
		
		return img;
	}
	
	public static BufferedImage processIPeakSets(Options options, IPeakSet<IPeakSet<? extends IPeak>> peaksets, Header header)
	{
		// retrieve the maximum scan-id
		int maxscan = 0;
		for (IPeakSet<? extends IPeak> peakset : peaksets)
		{
			for (IPeak peak : peakset)
			{
				if (peak.getClass().equals(BackgroundIon.class))
					maxscan = Math.max(maxscan, ((BackgroundIon<Peak>) peak).getMaxScanID());
				else if (peak.getClass().equals(MassChromatogram.class))
					maxscan = Math.max(maxscan, ((MassChromatogram<Peak>) peak).getMaxScanID());
				else
					maxscan = Math.max(maxscan, peak.getScanID());
			}
		}
		
		// create the containers for the tics
		Vector<double[]> tics = new Vector<double[]>();
		int a = 0;
		while (a++ < header.getNrMeasurementInfos())
		{
			double tic[] = new double[maxscan];
			for (int i=0; i<maxscan; ++i)
				tic[i] = 0;
			tics.add(tic);
		}
		
		// add the intensity values to the tic containers
		for (IPeak _peakset : peaksets)
		{
			IPeakSet<? extends IPeak> peakset = (IPeakSet<? extends IPeak>) _peakset;
			for (IPeak peak : peakset)
			{
				double[] tic = tics.get(header.indexOfMeasurementInfo(peak.getMeasurementID()));
				if (peak.getClass().equals(BackgroundIon.class))
					for (Peak p : (BackgroundIon<Peak>) peak)
						tic[p.getScanID()] += p.getIntensity();
				else if (peak.getClass().equals(MassChromatogram.class))
					for (Peak p : (BackgroundIon<Peak>) peak)
						tic[p.getScanID()] += p.getIntensity();
				else
					tic[peak.getScanID()] += peak.getIntensity();
			}
		}
		
		// create the image
		BufferedImage img = new BufferedImage(options.width, tics.size()*options.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = img.createGraphics();
		
		for (int i=0; i<tics.size(); ++i)
		{
			Signal tic = new Signal(tics.get(i));
			g.drawImage(
					tic.createGraphImage(header.getMeasurementInfos().get(i).getLabel(), "scans", "total intensity", options.width, options.height),
					0, i*options.height, null
				);
		}
		
		return img;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "PeakMLToTIC";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Creates Total Ion Current graphs from the given PeakML file. The contents is analyzed " +
		"and correctly summed to get to the real TIC. When the file contains multiple measurements, " +
		"a seperate TIC is made for each measurement.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM create a TIC overview\n" +
		"%JAVA% mzmatch.ipeak.graphics.PeakMLToTIC -v -i file.peakml -o image.pdf\n",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The input file in the PeakML format. When this option is not set, the input is read from " +
			"the standard input.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The output file for the graph(s). This option is required to be set.")
		public String output = null;

		@Option(name="width", param="integer", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The width of a graph.")
		public int width = 800;
		@Option(name="height", param="integer", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The height of a graph.")
		public int height = 300;
		
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
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, true);
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading data.");
			ParseResult result = PeakMLParser.parse(new FileInputStream(options.input), true);
			
			Header header = result.header;
			IPeakSet<? extends IPeak> peaks = (IPeakSet<? extends IPeak>) result.measurement;
			
			// process
			if (options.verbose)
				System.out.println("Processing data.");
			BufferedImage img = null;
			if (peaks.getContainerClass().equals(IPeakSet.class))
				img = processIPeakSets(options, (IPeakSet<IPeakSet<? extends IPeak>>) peaks, header);
			else
				img = processIPeaks(options, peaks, header);
			
			// write the data
			if (options.verbose)
				System.out.println("Writing data.");
			ImageIO.write(img, "png", new File(options.output));
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
