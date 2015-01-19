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



package peakmlviewer.util;


// java
import java.io.*;
import java.util.*;

import org.w3c.dom.*;

// libraries
import domsax.*;





/**
 * This is the central class keeping track of all the settings. For now this
 * is a static approach, so do not make an instance out of this. Also for now,
 * we're keeping track of very simple settings, however it's intended to
 * enlarge this to more complex items. For example, the smooth-parameter
 * indicates whether to smooth the curves, however there are multiple approaches
 * to smoothing and each has its own settings.
 */
public class Settings
{
	/** indicates whether to smooth */
	public static boolean smooth = true;
	/** */
	public static boolean smooth_show_original = false;
	
	public static Vector<String> databases = new Vector<String>();
	
	
	// load and save facilities
	public static void load(InputStream in) throws IOException, XmlParserException
	{
		XmlParser xml = new XmlParser();
		xml.addListener(new XmlParserListener() {
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				NodeList nodes = document.getChildNodes().item(0).getChildNodes();
				for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
				{
					Node node = nodes.item(nodeid);
					if (node.getNodeType() != Node.ELEMENT_NODE)
						continue;
					
					Element element = (Element) node;
					if (element.getTagName().equals("smooth"))
						Settings.smooth = Boolean.parseBoolean(element.getTextContent());
					else if (element.getTagName().equals("databases"))
						loadDatabases(element);
				}
			}
		}, "/peakmlviewer/settings");
		
		xml.parse(in);
	}
	
	private static void loadDatabases(Element parent)
	{
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("database"))
				Settings.databases.add(element.getTextContent());
		}
	}
	
	public static void save(OutputStream out) throws IOException
	{
		XmlWriter xml = new XmlWriter(out);
		
		xml.writeTag("peakmlviewer", XmlWriter.Tag.OPEN);
		
		xml.writeTag("settings", XmlWriter.Tag.OPEN);
		xml.writeElement("smooth", Boolean.toString(smooth));
		xml.writeTag("databases", XmlWriter.Tag.OPEN);
		for (String database : databases)
			xml.writeElement("database", database);
		xml.writeTag("databases", XmlWriter.Tag.CLOSE);
		xml.writeTag("settings", XmlWriter.Tag.CLOSE);
		
		xml.writeTag("peakmlviewer", XmlWriter.Tag.CLOSE);
		xml.close();
	}
}
