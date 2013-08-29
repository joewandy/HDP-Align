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

package com.joewandy.mzmatch;

// java
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import mzmatch.util.Tool;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

public class PeakMLToFeatureXML {
	
	// main entrance
	final static String version = "1.0";
	final static String application = "PeakMLToFeatureXML";

	@OptionsClass(name = application, version = version, author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", 
			description = "Convert PeakML to a XML format that can be imported by MzMine (via the Import XML function)"
	)
	public static class Options {

		@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option for the input file. The only allowed file format is PeakML and no "
				+ "limitations are set to its contents. When this is not set the input is read "
				+ "from the standard in.")
		public String input = null;
		
		@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, usage = "Option for the output file. This file is writen in the XML format that MzMine " +
				"import function can understand")
		public String output = null;
		
		@Option(name="polarity", param="string", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Polarity of this input file. Used for file-naming purpose only"
		)
		public String polarity = null;
		
		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, usage = "When this is set, the help is shown.")
		public boolean help = false;
		
		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, usage = "When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	
	}

	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
		
		try {

			Tool.init();

			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);

			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help) {
				Tool.printHeader(System.out, application, version);

				cmdline.printUsage(System.out, "");
				return;
			}

			if (options.verbose) {
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}

			// open the streams
			InputStream input = System.in;
			if (options.input != null) {
				input = new FileInputStream(options.input);
			}
			OutputStream output = System.out;
			if (options.output != null) {
				output = new FileOutputStream(options.output);
			}

			// load the data
			if (options.verbose) {
				System.out.println("Loading data");
			}
			ParseResult result = PeakMLParser.parse(input, true);
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;

			// process the data
			if (options.verbose) {
				System.out.println("Exporting peaks");
			}
			String prefix = new File(options.input).getName();
			prefix = prefix.substring(0, prefix.lastIndexOf('.'));
			prefix += "." + options.polarity;
			PeakMLToFeatureXMLWriter.write(prefix, result.header, peakset.getPeaks(), null, output, null);

		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}
		
	}
	
}
