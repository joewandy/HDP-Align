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



package mzmatch.ipeak.convert;


// java
import java.io.*;
import java.util.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.dac.*;
import peakml.io.mzml.*;
import peakml.io.mzxml.*;

// mzmatch
import mzmatch.util.*;





public class WatersToML
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "WatersToML";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Converts the contents of a Waters RAW file to one or more open standard files " +
		"(currently only mzXML and mzML are supported)." +
		"\n\n" +
		"WARNINGS:\n" +
		"* This tool makes direct use of the Waters DLL and can only be used on Microsoft Windows platforms.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.convert.WatersToMzML -v -i test.RAW -o test\\ -format mzML",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"A list of valid Waters RAW files to be converted to the chosen format.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The directory where to output the files.")
		public String output = null;
		
		@Option(name="format", param="mzXML|mzML", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The output format to use. The current choices are limited to mzXML and mzML as these " +
			"are the most used.")
		public String format = null;
		
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
				
			}
			
			
			// load the MS vocabulary
			Vocabulary vocabulary = Vocabulary.getVocabulary("psi-ms.obo");
			
			// open the DAC connections
			DAC.init();
			
			// process the files
			for (String filename : options.input)
			{
				if (options.verbose)
					System.out.println("Loading " + filename);
				
				// retrieve filename information
				File f = new File(filename);
				String shortname = f.getName().substring(0, f.getName().toUpperCase().indexOf(".RAW"));
				
				// load the data
				DACHeader dacheader = new DACHeader();
				dacheader.open(filename);
				
				// create a general header
				Header header = new Header();
				
				header.addApplicationInfo(
						new ApplicationInfo("MassLynx", dacheader.getVersionMajor()+"."+dacheader.getVersionMinor(), dacheader.getAcquDate(), "")
					);
				header.addApplicationInfo(
						new ApplicationInfo(WatersToML.class.getName(), version, new Date().toString(), cmdline.toString(options))
					);
				
				MeasurementInfo measurementinfo = new MeasurementInfo(0, (String) null);
				measurementinfo.setLabel(shortname);
				header.addMeasurementInfo(measurementinfo);
				
				FileInfo fileinfo = new FileInfo("shortname", filename);
				measurementinfo.addFileInfo(fileinfo);
				
				// process all the functions
				int nrfunctions = DACFunctionInfo.getNumberOfFunctions(filename);
				for (int functionnr=1; functionnr<=nrfunctions; ++functionnr)
				{
					// retrieve the general info
					DACFunctionInfo dacfunctioninfo = new DACFunctionInfo();
					dacfunctioninfo.open(filename, functionnr);
					
					// add the machine descriptions
					measurementinfo.removeAllAnnotations();
					measurementinfo.addAnnotation("dd", "dd");
					
					// clear the old scaninfos
					measurementinfo.getScanInfos().clear();
					
					// create a container for the scans
					Vector<Spectrum<Centroid>> scans = new Vector<Spectrum<Centroid>>();
					
					// retrieve the polarisation
					Polarity polarity = Polarity.POSITIVE;
					
					// retrieve the scan data
					DACSpectrum dacspectrum = new DACSpectrum();
					DACScanStats dacscanstats = new DACScanStats();
					for (int scannr=1; scannr<=dacfunctioninfo.getNumScans(); scannr++)
					{
						dacspectrum.open(filename, functionnr, 0, scannr);
						dacscanstats.open(filename, functionnr, 0, scannr);
						
						if (dacspectrum.getNumPeaks() == 0)
							continue;
						
						// get the retentiontime
						double rt = dacscanstats.getRetnTime() * 60;
						
						// add the scaninfo
						ScanInfo scaninfo = new ScanInfo(rt, polarity);
						measurementinfo.addScanInfo(scaninfo);
						
						if (dacspectrum.getMasses().length == 0)
							System.out.println(dacspectrum.getNumPeaks() + " - " + dacspectrum.getMasses().length + " - " + dacspectrum.getIntensities().length);
						
						// create the spectrum
						PeakData<Centroid> peakdata = new PeakData<Centroid>(new Centroid.Factory(), dacspectrum.getNumPeaks());
						for (int i=0; i<peakdata.size(); ++i)
							peakdata.set(i, scannr, rt, dacspectrum.getMasses()[i], dacspectrum.getIntensities()[i]);
						Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata, polarity);
						spectrum.setScanID(scans.size());
						spectrum.setMeasurementID(0);
						spectrum.setRetentionTime(rt);
						scans.add(spectrum);
					}
					
					// create the cms
					ChromatographyMS<Centroid> cms = new ChromatographyMS<Centroid>(scans);
					
					// write
					String outfile = options.output + "/" + shortname + "_" + functionnr;
					if (options.verbose)
						System.out.println("  writing the function: " + functionnr);
					
					String format = options.format.toLowerCase();
					if (format.equals("mzml"))
					{
						outfile = outfile + ".mzML";
						Tool.createFilePath(outfile, true);
						MZMLWriter.write(new FileOutputStream(outfile), header, cms);
					}
					else if (format.equals("mzxml"))
					{
						outfile = outfile + ".mzXML";
						Tool.createFilePath(outfile, true);
						MzXmlWriter.write(new FileOutputStream(outfile), header, cms);
					}
				}
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
