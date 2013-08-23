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

import org.w3c.dom.*;

// libraries
import domsax.*;
import cmdline.*;

// mzmatch
import mzmatch.util.*;





public class ReadWSplit
{
	// implementation
	public static String getAttribute(Node node, String name)
	{
		NamedNodeMap attributes = node.getAttributes();
		Node attribute = attributes.getNamedItem(name);
		if (attribute == null)
			return null;
		return attribute.getNodeValue();
	}
	
	public static void setAttribute(Node node, String name, String value)
	{
		NamedNodeMap attributes = node.getAttributes();
		Node attribute = attributes.getNamedItem(name);
		if (attribute != null)
			attribute.setNodeValue(value);
	}
	
	
	// entry point
	final static String version = "1.0.0";
	final static String application = "ReadWSplit";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"This tool handles the mess ReadW makes of the new RAW-file versions (as of Xcalibur 4.2). " +
		"With these new version Thermo is giving access to the both the lockmass corrected and the " +
		"original data. With the advent of the exactive there can now possibly also be both " +
		"negative as well as positive scans in a single file. In order to circumvent problems " +
		"downstream of current pipeline software, each file is split into all of its different " +
		"contents based on the FilterLine (used in Xcalibur to differentiate between the different " +
		"data streams)." +
		"\n\n" +
		"The input file is split up into all of the unique filter-lines (Xcalibur specific). In the " +
		"case of the exactive there are 4 different lines: Full/neg, Full/pos, Lock/neg and Lock/pos, " +
		"which represent the different datastreams (Full=collected data; Lock=lockmass corrected " +
		"data). These are then written to separate files (Lock_neg_'input filename', etc). For analysis " +
		"it is advised to only use the Lock-files, as these will provide the best mass accuracy." +
		"\n\n" +
		"Remark\n" +
		"This tool was specifically made for files produced with the Orbitrap Exactive, but may " +
		"have application beyond this platform.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM split all the mzXML-files and write to raw_split\\\n" +
		"%JAVA% mzmatch.ipeak.util.ReadWSplit -v -i raw\\*.mzXML -o raw_split\\\n"
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input files. These files should be in the mzXML file format produced " +
			"by ReadW together with Xcalibur >=4.2.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the output destination. This is required to be a directory where all of " +
			"the generated  files are written. When the directory (or path leading to the directory " +
			"does not exist, it is created). For the filename convention used for the ouput files, " +
			"see the general description.")
		public String output = null;
		
		@Option(name="fullms", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the 'full lock ms' and 'full ms' scans are combined in a single file. " +
			"This is specific for exactive platforms.")
		public boolean fullms = false;
		
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
				// check whether we have an valid files
				if (options.input.size() == 0)
				{
					System.err.println("[ERROR]: at least one input file needs to be defined.");
					System.exit(0);
				}
				if (options.output == null)
				{
					System.err.println("[ERROR]: an output destination needs to be defined.");
					System.exit(0);
				}
				
				// create the file paths
				Tool.createFilePath(options.output, false);
			}
			
			
			// parse the file
			final boolean fullms = options.fullms;
			final Vector<Document> header = new Vector<Document>();
			final HashMap<String,Vector<Document>> scans = new HashMap<String,Vector<Document>>();
			
			class Listener implements XmlParserListener {
				public void onDocument(Document document, String xpath) throws XmlParserException
				{
					try
					{
						if (xpath.equals("/mzXML/msRun/parentFile"))
							header.add(document);
						else if (xpath.equals("/mzXML/msRun/msInstrument"))
							header.add(document);
						else if (xpath.equals("/mzXML/msRun/dataProcessing"))
							header.add(document);
						else if (xpath.equals("/mzXML/msRun/scan"))
						{
							Node node = document.getChildNodes().item(0);
							NamedNodeMap attributes = node.getAttributes();
							
							// get the filter line
							Node att_filterline = attributes.getNamedItem("filterLine");
							String filterline = att_filterline.getNodeValue();
							if (fullms && filterline.toLowerCase().contains("lock"))
							{
								filterline =
									filterline.substring(0, filterline.toLowerCase().indexOf("lock"))
									+
									filterline.substring(filterline.toLowerCase().indexOf("lock") + 5);
							}
							
							Vector<Document> vector = scans.get(filterline);
							if (vector == null)
							{
								vector = new Vector<Document>();
								scans.put(filterline, vector);
							}
							vector.add(document);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			
			// start processing
			for (String filename : options.input)
			{
				scans.clear();
				header.clear();
				
				if (options.verbose)
					System.out.println("Processing '" + filename + "'");
					
				Listener listener = new Listener();
				XmlParser xmlparser = new XmlParser();
	
				xmlparser.addListener(listener, "/mzXML/msRun/scan");
				xmlparser.addListener(listener, "/mzXML/msRun/parentFile");
				xmlparser.addListener(listener, "/mzXML/msRun/msInstrument");
				xmlparser.addListener(listener, "/mzXML/msRun/dataProcessing");
				
				xmlparser.parse(new FileInputStream(filename));
				
				// dump the input
				for (String key : scans.keySet())
				{
					// general properties
					String polarity = key.contains("+") ? "+" : "-";
					String mslevel = key.contains("ms2") ? "2" : "1";
					String scantype = key.contains("Full lock ms") ? "Lock" : "Full";
					
					// create the filename
					String file = new File(filename).getName();
					file = options.output + "/" + key + "/" + file.substring(0, file.lastIndexOf('.')) + ".mzXML";
					Tool.createFilePath(file, true);
					
					// 
					if (options.verbose)
						System.out.println("- writing: " + file);
					XmlWriter xml = new XmlWriter(new OutputStreamWriter(new FileOutputStream(file)), XmlWriter.Encoding.ISO_8859_1);
					
					Vector<Document> myscans = scans.get(key);
					
					// 
					xml.writeTag("mzXML", XmlWriter.Tag.OPEN,
						new XmlAttribute("xmlns", "http://sashimi.sourceforge.net/schema_revision/mzXML_3.1"),
						new XmlAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"),
						new XmlAttribute("xsi:schemaLocation", "http://sashimi.sourceforge.net/schema_revision/mzXML_3.1 http://sashimi.sourceforge.net/schema_revision/mzXML_3.1/mzXML_idx_3.1.xsd")
					);
					xml.writeTag("msRun", XmlWriter.Tag.OPEN,
							new XmlAttribute("scanCount", Integer.toString(myscans.size())),
							new XmlAttribute("startTime",	getAttribute(myscans.firstElement().getChildNodes().item(0),	"retentionTime")),
							new XmlAttribute("endTime",		getAttribute(myscans.lastElement().getChildNodes().item(0),		"retentionTime"))
						);
					
					// write the header
					for (Document doc : header)
						xml.writeDocument(doc);
					
					// add our little tool
					xml.writeTag("dataProcessing", XmlWriter.Tag.OPEN);
					xml.writeTag("software", XmlWriter.Tag.SINGLE,
							new XmlAttribute("name", "mzmatch." + application),
							new XmlAttribute("type", "conversion"),
							new XmlAttribute("version", version)
						);
					xml.writeTag("dataProcessing", XmlWriter.Tag.CLOSE);
					
					// write the scans
					for (int i=0; i<myscans.size(); ++i)
					{
						Document doc = myscans.get(i);
						Node scan = doc.getChildNodes().item(0);
						setAttribute(scan, "num", Integer.toString(i+1));
						setAttribute(scan, "polarity", polarity);
						setAttribute(scan, "msLevel", mslevel);
						setAttribute(scan, "scanType", scantype);
						
						// convert the attribute of the scan to our own structure
						NamedNodeMap atts = scan.getAttributes();
						Vector<XmlAttribute> attributes = new Vector<XmlAttribute>();
						for (int j=0; j<atts.getLength(); ++j)
						{
							Node att = atts.item(j);
							XmlAttribute attribute = new XmlAttribute(att.getNodeName(), att.getNodeValue());
							if (attribute.getName().equals("num"))
								attributes.add(0, attribute);
							else
								attributes.add(attribute);
						}
						
						// write the scan
						xml.writeTag("scan", XmlWriter.Tag.OPEN, attributes);
						NodeList children = scan.getChildNodes();
						for (int j=0; j<children.getLength(); ++j)
						{
							Node child = children.item(j);
							if (child.getNodeType() != Node.ELEMENT_NODE)
								continue;
							xml.writeElement((Element) child);
						}
						xml.writeTag("scan", XmlWriter.Tag.CLOSE);
					}
					
					//
					xml.writeTag("msRun", XmlWriter.Tag.CLOSE);
					xml.writeTag("mzXML", XmlWriter.Tag.CLOSE);
					xml.flush();
				}
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
