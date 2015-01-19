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
import java.util.zip.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;

import peakml.io.*;
import peakml.io.peakml.*;

// mzmatch
import mzmatch.util.*;





@SuppressWarnings("unchecked")
public class BreakupRelated
{
	// implementation
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "BreakupRelated";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Splits the derivative clusters found by mzmatch.ipeak.sort.RelatedPeaks into separate " +
		"files. Each file is named with the relation.id found in the annotations.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i related.peakml -o derivatives\\\n",
		references=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The input file (in PeakML format), resulting from the tool mzmatch.ipeak.sort.RelatedPeaks. " +
			"When this option is not set the input is read from the standard input.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Specifies the output directory where all the PeakML files are written with the clusters " +
			"of derivatives.")
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
				if (options.output == null)
				{
					System.err.print("[ERROR]: output directory is not set.");
					System.exit(0);
				}
				
				File directory = new File(options.output);
				if (directory.exists() && !directory.isDirectory())
				{
					System.err.print("[ERROR]: output directory is not set.");
					System.exit(0);
				}
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading data.");
			ParseResult result = PeakMLParser.parse(new FileInputStream(options.input), true);
			
			IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			
			// break it up
			int current_relationid = -1;
			Vector<IPeak> selection = new Vector<IPeak>();
			for (IPeak peak : peaks)
			{
				Annotation ann_relationid = peak.getAnnotation(IPeak.relationid);
				if (ann_relationid == null)
				{
					System.err.println("[ERROR]: Missing '" + IPeak.relationid + "' annotation");
					System.exit(0);
				}
				int relationid = ann_relationid.getValueAsInteger();
				
				// write and then empty the peaks-list for a new relationid
				if (current_relationid != relationid)
				{
					if (selection.size() > 0)
					{
						String filename = options.output + "/" + current_relationid + "_";
						Annotation ann_basepeak = selection.firstElement().getAnnotation(Annotation.relationid);
						if (ann_basepeak != null)
							filename = filename + ann_basepeak.getValue();
						else
							filename = filename + selection.firstElement().getMass();
						filename = filename + ".peakml";
						
						PeakMLWriter.write(
								result.header, selection, null,
								new GZIPOutputStream(new FileOutputStream(filename)),
								null
							);
					}
					
					selection.clear();
					current_relationid = relationid;
				}
				
				// fill the list
				selection.add(peak);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
