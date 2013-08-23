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
import peakml.io.mzml.*;
import peakml.io.mzxml.*;
import peakml.io.xrawfile.*;

// mzmatch
import mzmatch.util.*;





public class ThermoToML
{
	// implementation
	public static Polarity getPolarity(String filter)
	{
		if (filter.contains("+"))
			return Polarity.POSITIVE;
		else if (filter.contains("-"))
			return Polarity.NEGATIVE;
		return Polarity.NEUTRAL;
	}
	
	public static Vector<Annotation> getMachineAnnotations(IXRawfile rawfile, Vocabulary vocabulary) throws IXRawfileException
	{
		Vector<Annotation> annotations = new Vector<Annotation>();
		
		// get the model
		String model = rawfile.getInstModel();
		for (Vocabulary.Term term : vocabulary)
			if (term.name.equals(model))
			{
				annotations.add(new Annotation(term.id, term.name));
				break;
			}
		
		// get the ionisation
		
		// get analyzer
		// check the tune data
	
		return annotations;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "ThermoToML";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Converts the contents of a Thermo RAW file to one or more open standard files " +
		"(currently only mzXML and mzML are supported). Thermo RAW files can contain multiple " +
		"controllers and multiple methods (separated by filterline). These are all split into " +
		"separate files, distinguished by directories with the controller-number and the " +
		"filterline (i.e. the original filename is maintained, so tools working with a filename " +
		"database will continue to work correctly). When only one controller is present in the " +
		"file, the controller-number is omited in the directory naming." +
		"\n\n" +
		"The data can be centroided by this application, for which the functionality in the " +
		"Thermo RAWfile DLL is used. To do so, specify the commandline parameter centroid." +
		"\n\n" +
		"WARNINGS:\n" +
		"* This tool makes direct use of the Thermo Rawfile DLL and can only be used on Microsoft Windows platforms.\n" +
		"* Due to a bug in the Thermo DLL no more than 8 files can be processed in a single run",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.convert.ThermoToMzML -v -i test.RAW -o test\\ -centroid -format mzML",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"A list of valid Thermo RAW files to be converted to the chosen format.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The directory where to output the files. For each filterline encountered in the RAW " +
			"file, a new sub-directory will be created. In the case of multiple controllers, the " +
			"controller-number will also be inserted in the path.")
		public String output = null;

		@Option(name="centroid", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the data will be centroided. When the data is already in centroid " +
			"mode this will have no effect.")
		public boolean centroid = false;
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
				if (options.input.size() == 0)
				{
					System.err.println("[ERROR]: input is not set");
					System.exit(0);
				}
				if (options.output == null)
				{
					System.err.println("[ERROR]: output is not set");
					System.exit(0);
				}
				File directory = new File(options.output);
				if (directory.exists() && !directory.isDirectory())
				{
					System.err.println("[ERROR]: output is not set to a directory");
					System.exit(0);
				}
				Tool.createFilePath(options.output, false);
				
				if (options.format == null)
				{
					System.err.println("[ERROR]: format is not set");
					System.exit(0);
				}
				String format = options.format.toLowerCase();
				if (!format.equals("mzxml") && !format.equals("mzml"))
				{
					System.err.println("[ERROR]: unknown output format: " + options.format);
					System.exit(0);
				}
			}
			
			
			
			// load the MS vocabulary
			Vocabulary vocabulary = Vocabulary.getVocabulary("psi-ms.obo");
			
			// process the files
			for (String filename : options.input)
			{
				if (options.verbose)
					System.out.println("Loading " + filename);
				
				// load the data
				int rtcode;
				IXRawfile rawfile = new IXRawfile();
				
				rtcode = rawfile.init();
				if (rtcode != IXRawfile.RTCODE_SUCCESS)
				{
					System.err.println("[ERROR]: could not open the connection to Thermo's raw DLL.");
					System.exit(0);
				}
				
				// retrieve filename information
				File f = new File(filename);
				String shortname = f.getName().substring(0, f.getName().toUpperCase().indexOf(".RAW"));
				
				// open the file
				try {
					rawfile.open(filename);
				} catch (IXRawfileException e) {
					System.err.println("[ERROR]: could not open the file '" + filename + "'.");
					System.exit(0);
				}
				
				// create a general header
				Header header = new Header();
				
				header.addApplicationInfo(
						new ApplicationInfo("XCalibur", Integer.toString(rawfile.getVersionNumber()), rawfile.getCreationDate(), "")
					);
				header.addApplicationInfo(
						new ApplicationInfo(ThermoToML.class.getName(), version, new Date().toString(), cmdline.toString(options))
					);
				
				MeasurementInfo measurementinfo = new MeasurementInfo(0, (String) null);
				measurementinfo.setLabel(shortname);
				header.addMeasurementInfo(measurementinfo);
				
				FileInfo fileinfo = new FileInfo("shortname", filename);
				measurementinfo.addFileInfo(fileinfo);
				
				// process all of the MS controllers
				int nrcontrollers = rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_MS);
				for (int controllernumber=1; controllernumber<=nrcontrollers; ++controllernumber)
				{
					if (options.verbose)
						System.out.println("- processing controller: " + controllernumber);
					
					// set the controller
					rawfile.setCurrentController(IXRawfile.CONTROLLER_MS, controllernumber);
					
					// add the machine descriptions
					measurementinfo.removeAllAnnotations();
					measurementinfo.addAnnotations(getMachineAnnotations(rawfile, vocabulary));
					
					// clear the old scaninfos
					measurementinfo.getScanInfos().clear();
					
					// create a container for the scans
					HashMap<String,Vector<Spectrum<Centroid>>> filters = new HashMap<String,Vector<Spectrum<Centroid>>>();
					
					// retrieve the general properties
					int minscannumber = rawfile.getFirstSpectrumNumber();
					int maxscannumber = rawfile.getLastSpectrumNumber();
					for (int scannumber=minscannumber; scannumber<=maxscannumber; ++scannumber)
					{
						// retrieve the filter-line
						String filter = rawfile.getFilterForScanNum(scannumber);
						Polarity polarity = getPolarity(filter);
						
						Vector<Spectrum<Centroid>> scans;
						if (!filters.containsKey(filter))
							filters.put(filter, scans = new Vector<Spectrum<Centroid>>());
						else
							scans = filters.get(filter);
						
						// retrieve the data
						double rt = 60*rawfile.rtFromScanNum(scannumber);
						double scandata[] = rawfile.getMassListFromScanNum(scannumber, options.centroid);
						
						// add the scaninfo
						ScanInfo scaninfo = new ScanInfo(rt, polarity);
						measurementinfo.addScanInfo(scaninfo);
						
						// create a spectrum
						PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, scandata.length/2);
						for (int i=0; i<scandata.length/2; ++i)
						{
							// for the masses we compensate for the polarity behaviour of Spectrum
							peakdata.setMeasurementID(i, 0);
							peakdata.set(i, scans.size(), rt, scandata[i*2], scandata[i*2+1]);
						}
						
						// create and store the spectrum
						Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata, polarity);
						spectrum.setScanID(scannumber-minscannumber);
						spectrum.setMeasurementID(0);
						spectrum.setRetentionTime(rt);
						scans.add(spectrum);
					}
					
					// create the ChromatographyMS
					for (String filter : filters.keySet())
					{
						if (options.verbose)
							System.out.println("  writing the filter: " + filter);
						
						// create the cms
						ChromatographyMS<Centroid> cms = new ChromatographyMS<Centroid>(filters.get(filter));
						
						// write
						String outfile;
						if (nrcontrollers == 1)
							outfile = options.output + "/" + filter + "/" + shortname;
						else
							outfile = options.output + "/" + filter + "/" + controllernumber + "/" + shortname;
						
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
						
//						// verify
//						ParseResult result = ParseResult.parse(outfile);
//						ChromatographyMS<Centroid> newcms = (ChromatographyMS<Centroid>) result.measurement;
//						
//						if (cms.getNrScans() != newcms.getNrScans())
//							System.err.println("different number of scans '" + cms.getNrScans() + " != " + newcms.getNrScans());
//						for (int scanid=0; scanid<newcms.getNrScans(); ++scanid)
//						{
//							Spectrum<Centroid> scan = cms.getScan(scanid);
//							PeakData<Centroid> peakdata = scan.getPeakData();
//							Spectrum<Centroid> newscan = newcms.getScan(scanid);
//							PeakData<Centroid> newpeakdata = newscan.getPeakData();
//							
//							if (scan.getNrPeaks() != newscan.getNrPeaks())
//								System.err.println("different number of peaks in scan '" + scanid + "'");
//							for (int i=0; i<newscan.getNrPeaks(); ++i)
//							{
//								if (peakdata.getMass(i) != newpeakdata.getMass(i))
//									System.err.println("mass difference at scan '" + scanid + "' and datapoint '" + i + "': " + peakdata.getMass(i) + "!=" + newpeakdata.getMass(i));
//								if (peakdata.getIntensity(i) != newpeakdata.getIntensity(i))
//									System.err.println("intensity difference at scan '" + scanid + "' and datapoint '" + i + "': " + peakdata.getIntensity(i) + "!=" + newpeakdata.getIntensity(i));
//							}
//						}
					}
				}
				
				// close the file
				try {
					rawfile.close();
				} catch (IXRawfileException e) {
					System.err.println("[ERROR]: could not close the file '" + filename + "'.");
					System.exit(0);
				}
				
				// cleanup
				rawfile.dispose();
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
