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



package mzmatch.ipeak.db;


// java
import java.io.*;

import java.util.*;

// libraries
import cmdline.*;

// peakml
import peakml.chemistry.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.util.*;





public class DBToText
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "DBToText";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Exports the given moleculedb database to an excel readable text file. Only the " +
		"general name is exported (ie the synonyms are not exported)")
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Input file of the database")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Output file for the tab-separated version of the database")
		public String output = null;
		
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
			
			
			// read the file
			HashMap<String,Molecule> database = MoleculeIO.parseXml(new FileInputStream(options.input));
			
			// open the output stream and dump
			PrintStream output = new PrintStream(options.output);
			for (Molecule m : database.values())
				output.println(
						m.getDatabaseID()
						+ "\t" +
						m.getName()
						+ "\t" +
						m.getFormula()
						+ "\t" +
						m.getInChi()
						+ "\t" +
						m.getDescription()
					);
			
			output.flush();
			output.close();
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
