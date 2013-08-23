/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.io.mzml;


// java
import java.io.*;

import org.w3c.dom.*;

// libraries
import domsax.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.util.*;
import peakml.chemistry.*;





/**
 * 
 */
public abstract class MZMLParser
{
	/**
	 * 
	 */
	public static ParseResult parseCMS(InputStream input) throws IOException, XmlParserException
	{
		final int MASSES		= 1;
		final int INTENSITIES	= 2;
		
		final int FLOAT			= 1;
		final int INTEGER		= 2;
		
		final String header_filedescription			= "/mzML/fileDescription";
		final String header_filedescription_indexed	= "/indexedmzML/mzML/fileDescription";
		final String header_dataprocessing			= "/mzML/dataProcessingList";
		final String header_dataprocessing_indexed	= "/indexedmzML/mzML/dataProcessingList";
		final String data_spectrum					= "/mzML/run/spectrumList/spectrum";
		final String data_spectrum_indexed			= "/indexedmzML/mzML/run/spectrumList/spectrum";
		
		// XmlParser
		final Header header = new Header();
		final MeasurementInfo measurement = new MeasurementInfo(0, "");
		header.addMeasurementInfo(measurement);
		
		final ChromatographyMS<Centroid> cms = new ChromatographyMS<Centroid>();
		
		class Listener implements XmlParserListener {
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				Node parent = document.getChildNodes().item(0);
				if (xpath.equals(header_filedescription) || xpath.equals(header_filedescription_indexed))
				{
					
				}
				else if (xpath.equals(header_dataprocessing) || xpath.equals(header_dataprocessing_indexed))
				{
					
				}
				else if (xpath.equals(data_spectrum) || xpath.equals(data_spectrum_indexed))
				{
					// retrieve the default properties
					int default_array_length = Integer.parseInt(
							parent.getAttributes().getNamedItem("defaultArrayLength").getNodeValue()
						);
					
					// parse the parameters
					int msn = -1;
					Polarity polarity = Polarity.NEUTRAL;
					
					NodeList parameters = XmlTools.getNodesByTagName(parent, "cvParam");
					for (int i=0; i<parameters.getLength(); ++i)
					{
						Node node = parameters.item(i);
						NamedNodeMap attributes = node.getAttributes();
						
						Node value = attributes.getNamedItem("value");
						Node accession = attributes.getNamedItem("accession");
						if ("MS:1000511".equals(accession.getNodeValue()))
							msn = Integer.parseInt(value.getNodeValue());
						else if ("MS:1000129".equals(accession.getNodeValue()))
							polarity = Polarity.NEGATIVE;
						else if ("MS:1000130".equals(accession.getNodeValue()))
							polarity = Polarity.POSITIVE;
//						else if ("MS:1000128".equals(accession.getNodeValue()))
//							throw new XmlParserException("profile mode data is not properly supported");
					}
					
					// validity check
					if (msn == -1)
						throw new XmlParserException("ms level was not set");
					if (polarity == Polarity.NEUTRAL)
						throw new XmlParserException("polarity was not set");
					
					// parse the scanList in order to get the RT
					double retentiontime = -1;
					
					Node scan = XmlTools.getNodeByTagName(document, "scan");
					NodeList scanlist_parameters = XmlTools.getNodesByTagName(scan, "cvParam");
					for (int i=0; i<scanlist_parameters.getLength(); ++i)
					{
						Node node = parameters.item(i);
						NamedNodeMap attributes = node.getAttributes();
						
						Node value = attributes.getNamedItem("value");
						Node accession = attributes.getNamedItem("accession");
						if ("MS:1000016".equals(accession.getNodeValue()))
							retentiontime = Double.parseDouble(value.getNodeValue());
					}
					
					// retrieve the raw data
					double masses[] = null;
					double intensities[] = null;
					
					NodeList data = XmlTools.getNodesByTagName(document, "binaryDataArray");
					for (int nodeid=0; nodeid<data.getLength(); ++nodeid)
					{
						Node node = data.item(nodeid);
						
						// parse the parameters
						int type = -1;
						int datatype = -1;
						int precision = -1;
						boolean compression = true;
						
						parameters = XmlTools.getNodesByTagName(node, "cvParam");
						for (int i=0; i<parameters.getLength(); ++i)
						{
							NamedNodeMap attributes = parameters.item(i).getAttributes();
							
							Node accession = attributes.getNamedItem("accession");
							if ("MS:1000519".equals(accession.getNodeValue()))
							{
								datatype = INTEGER;
								precision = 32;
							}
							else if ("MS:1000521".equals(accession.getNodeValue()))
							{
								datatype = FLOAT;
								precision = 32;
							}
							else if ("MS:1000522".equals(accession.getNodeValue()))
							{
								datatype = INTEGER;
								precision = 64;
							}
							else if ("MS:1000523".equals(accession.getNodeValue()))
							{
								datatype = FLOAT;
								precision = 64;
							}
							else if ("MS:1000514".equals(accession.getNodeValue()))
								type = MASSES;
							else if ("MS:1000515".equals(accession.getNodeValue()))
								type = INTENSITIES;
							else if ("MS:1000576".equals(accession.getNodeValue()))
								compression = false;
						}
						
						// validity checks
						if (type == -1)
							throw new XmlParserException("the type of the data was not set");
						if (precision == -1)
							throw new XmlParserException("the precision of the data was not set");
						if (compression == true)
							throw new XmlParserException("compression of data is not supported");
						if (datatype==INTEGER && precision==64)
							throw new XmlParserException("combination of INTEGER and 64 bit is not supported");
						
						// extract the data
						Node binary = XmlTools.getNodeByTagName(node, "binary");
						double array[] = ByteArray.toDoubleArray(
								Base64.decode(binary.getTextContent()),
								ByteArray.ENDIAN_LITTLE,
								precision
							);
						if (type == MASSES)
							masses = array;
						else if (type == INTENSITIES)
							intensities = array;
					}
					
					// validity check
					if (masses==null || masses.length!=default_array_length)
						throw new XmlParserException("corrupt mass data");
					if (intensities==null || intensities.length!=default_array_length)
						throw new XmlParserException("corrupt intensity data");
					
					// create the spectrum
					PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, default_array_length);
					for (int i=0; i<default_array_length; ++i)
						peakdata.set(i, -1, -1, masses[i], intensities[i]);
					Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata, polarity);
					spectrum.setRetentionTime(retentiontime);
					
					if (msn == 1)
					{
						cms.getScans().add(spectrum);
						measurement.addScanInfo(new ScanInfo(retentiontime, polarity));
					}
					else
						cms.getScans().lastElement().addMSnSpectrum(spectrum);
				}
			}
		}
		
		Listener listener = new Listener();
		XmlParser xmlparser = new XmlParser();
		
		// header
		xmlparser.addListener(listener, header_filedescription);
		xmlparser.addListener(listener, header_filedescription_indexed);
		xmlparser.addListener(listener, header_dataprocessing);
		xmlparser.addListener(listener, header_dataprocessing_indexed);
		// data
		xmlparser.addListener(listener, data_spectrum);
		xmlparser.addListener(listener, data_spectrum_indexed);
		
		xmlparser.parse(input);
		
		// update the scan/retentiontimes
		int scanid = 0;
		for (Spectrum<Centroid> scan : cms.getScans())
		{
			for (Peak peak : scan.getPeaks())
			{
				peak.setScanID(scanid);
				peak.setMeasurementID(0);
				peak.setRetentionTime(measurement.getScanInfo(scanid).getRetentionTime());
			}
			scanid++;
		}
		
		return new ParseResult(header, cms);
	}
}
