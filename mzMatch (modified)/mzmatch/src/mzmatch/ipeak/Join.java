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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import mzmatch.util.Tool;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.FileInfo;
import peakml.io.Header;
import peakml.io.MeasurementInfo;
import peakml.io.ParseResult;
import peakml.io.SetInfo;
import peakml.io.peakml.PeakMLParser;
import peakml.io.peakml.PeakMLWriter;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;






@SuppressWarnings("unchecked")
public class Join
{
	// implementation
	// we add the 1e6 here to prevent an overrun (unlikely that many files are combined)
	//public static final int id_padding = 1000000;
	/*
	public static Vector<IPeak> unpack(IPeak peak)
	{
		Vector<IPeak> peaks = new Vector<IPeak>();
		if (IPeakSet.class.equals(peak.getClass()))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				peaks.addAll(unpack(p));
		}
		else
			peaks.add(peak);
		return peaks;
	}
	*/
	public static void setMeasurementIDs(IPeak peak, int oldid, int newid)
	{
		if (peak.getMeasurementID() == oldid)
			peak.setMeasurementID(newid);
		
		if (IPeakSet.class.equals(peak.getClass()))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				setMeasurementIDs(p, oldid, newid);
		}
	}
	/*
	public static void updateSetInfo(SetInfo setinfo, HashMap<Integer,Integer> mids)
	{
		// replace the measurementid's
		Vector<Integer> measurementids = setinfo.getMeasurementIDs();
		for (int i=0; i<measurementids.size(); ++i)
			measurementids.set(i, mids.get(measurementids.get(i)));
		
		// do the children
		for (SetInfo child : setinfo.getChildren())
			updateSetInfo(child, mids);
	}
	*/
	/*
	public static void removePadding(IPeak peak)
	{
		if (peak.getMeasurementID() >= id_padding)
			peak.setMeasurementID(peak.getMeasurementID() - id_padding);
		
		if (IPeakSet.class.equals(peak.getClass()))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				removePadding(p);
		}
	}
	
	*/
	/**
	 * This method copies the measurement information from the read file, assigns a new
	 * id to the measurement and updates all the data with the new measurement id.
	 * 
	 * @param header		The header to add the new entries to.
	 * @param result		The data read from an original file.
	 * @param filename		The filename of the original file.
	 */
	public static int updateHeader(Header header, SetInfo set, ParseResult result, String filename, int newMeasurementId)
	{
		// for all the measurementinfo's in the read file
		HashMap<Integer,Integer> mids = new HashMap<Integer,Integer>();
		for (MeasurementInfo minfo : result.header.getMeasurementInfos())
		{
			int measurementid = header.getNrMeasurementInfos();
			
			// copy all the information from the measurementinfo
			MeasurementInfo minfo_new = new MeasurementInfo(measurementid, minfo);
			// save the old measurement-id
			int oldid = minfo.getID();
			System.err.println("Mapping " + oldid + " to " + newMeasurementId);
			mids.put(oldid, newMeasurementId);
			//MeasurementInfo mold = header.getMeasurementInfo(oldid);
			//if ( mold == null ) {
				header.addMeasurementInfo(minfo_new);
			//}
			
			// add the filename to the list of files
			File file = new File(filename);
			minfo_new.addFileInfo(new FileInfo(file.getName(), file.getName(), file.getParent()));
			
			// reset all the measurement id's
			setMeasurementIDs((IPeakSet<IPeak>) result.measurement, oldid, newMeasurementId);
			newMeasurementId++;
		}
		
		//removePadding((IPeakSet<IPeak>) result.measurement);
		
		// create the set-info
		
		/*
		if (result.header.getNrSetInfos() == 0)
		{
			for (MeasurementInfo minfo : result.header.getMeasurementInfos())
				set.addMeasurementID(mids.get(minfo.getID()));
		}
		else
		{
			for (SetInfo oldsetinfo : result.header.getSetInfos())
			{
				SetInfo newsetinfo = new SetInfo(oldsetinfo);
				updateSetInfo(newsetinfo, mids);
				header.addSetInfo(newsetinfo);
			}
		}*/
		return newMeasurementId;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "Join";
	@OptionsClass(name=application, version=version, author="Ronan Daly (Ronan.Daly@glasgow.ac.uk)",
		description=
		"Joins two or more PeakML files together, such that the output contains all the peaks " +
		"of all the files"
		,
		example=""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input files. Multiple files can be passed by separating them " +
			"with a comma (ie ,) or the use of a name with a wildcard (eg samples_*hrs.xml). The " +
			"only allowed file format is PeakML containing either mass chromatograms or backgroundions " +
			"at the lowest level (ie the result of another Combine can be used).")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the ouput file. The resulting matches are written to this file in the " +
			"PeakML file format." +
			"\n" +
			"When this option has not been set the output is written to the standard output. Be sure " +
			"to unset the verbose option when setting up a pipeline reading and writing from the " +
			"standard in- and outputs.")
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
			final Options options = new Options();
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
				// if the output dir does not exist, create it
				if (options.output != null)
					Tool.createFilePath(options.output, true);
			}
			
			
			// open the streams 
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// the header
			Header header = new Header();
			
			// load the data
			/*
			String setid = options.label!=null ? options.label : options.output;
			SetInfo set = new SetInfo(setid, combination);
			header.addSetInfo(set);
			*/
			Vector<Header> headers = new Vector<Header>();
			Vector<IPeakSet<IPeak>> peaksets = new Vector<IPeakSet<IPeak>>();
			if (options.verbose)
				System.out.println("Loading:");
			int newMeasurementId = 0;
			for (int i=0; i<options.input.size(); ++i)
			{
				String input = options.input.get(i);
				if (options.verbose)
					System.out.println("- " + input);
				ParseResult result = PeakMLParser.parse(new FileInputStream(input), true);
				
				// check whether we have loaded something we want
				if (!result.measurement.getClass().equals(IPeakSet.class))
				{
					System.err.println("[ERROR]: the contents of the file was not stored as an IPeakSet.");
					System.exit(0);
				}
				/*
				// save the label
				result.header.addAnnotation("label",
						options.labels.size()!=0 ? options.labels.get(i) : new File(input).getName()
					);
				*/
				// create the MeasurementInfo entries, set the appropriate measurement-id's and update the sets
				newMeasurementId = updateHeader(header, null, result, input, newMeasurementId);
				
				// store the data
				headers.add(result.header);
				peaksets.add((IPeakSet<IPeak>) result.measurement);
			}
			/*
			if (set.getNrMeasurementIDs()==0 && set.getNrChildren()==0)
				header.getSetInfos().remove(set);
			*/
			// match
			if (options.verbose)
				System.out.println("Joining data");
			Vector<IPeak> data = new Vector<IPeak>();
			//for (IPeakSet<IPeak> peaks : data) {
			for (int i = 0; i < peaksets.size(); ++i) {
				IPeakSet<IPeak> peaks = peaksets.get(i);
				//final String label = options.labels.get(i);
				for (IPeak peak: peaks) {
				//	peak.addAnnotation("peaktype", label);
					data.add(peak);
				}
			}
			
			// write the result
			if (options.verbose)
				System.out.println("Writing the results");
			PeakMLWriter.write(header, data, null, new GZIPOutputStream(output), null);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
