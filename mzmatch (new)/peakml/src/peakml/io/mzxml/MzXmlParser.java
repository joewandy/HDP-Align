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



package peakml.io.mzxml;


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
 * Implementation of a parser for the mzXML file format, with additional, hidden support
 * for the mzData file format. As both formats are very comparable a single parse function
 * is provided, which results in an instance of type {@link ChromatographyMS} in the
 * {@link ParseResult} 
 */
public abstract class MzXmlParser
{
	/**
	 * Parse function for loading the data stored in a mzXML or mzData file. Only
	 * 2-dimensional data is supported and the resulting {@link Measurement} is of
	 * type {@link ChromatographyMS}.
	 * 
	 * @param input			The input stream to read from.
	 * @param minintensity	Minimal intensity for a data-point to be taken into account (when set to 0 all are parsed).
	 * @return				The resulting data ({@link ChromatographyMS}) and header.
	 * 
	 * @throws IOException	Thrown when I/O related problems occur 
	 * @throws XmlParserException
	 * 						Thrown when an interpretation error of the data occurs.
	 */
	public static ParseResult parseCMS(InputStream input) throws IOException, XmlParserException
	{
		final Header header = new Header();
		final ChromatographyMS<Centroid> cms = new ChromatographyMS<Centroid>();
		final MeasurementInfo measurement = new MeasurementInfo(0, "");
		
		header.addMeasurementInfo(measurement);
		
		
		// XmlParser
		class Listener implements XmlParserListener {
			public double minmass = -1;
			public double maxmass = 0;
			public double minintensity = -1;
			public double maxintensity = 0;
			
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				// mzXml interface
				if (xpath.equals("/mzXML/msRun/parentFile"))
				{
//					Node node = document.getChildNodes().item(0);
//					NamedNodeMap attributes = node.getAttributes();
//					
//					// fileSha1
//					File f = new File(attributes.getNamedItem("filename").getNodeValue());
//					measurement.addFileInfo(new FileInfo(f.getName(), f.getName(), f.getParent()));
				}
				else if (xpath.equals("/mzXML/msRun/msInstrument"))
				{
					
				}
				else if (xpath.equals("/mzXML/msRun/scan"))
				{
					// retrieve the scan-info
					NodeList nodes = XmlTools.getNodesByTagName(document, "scan");
					NodeList nodes_peaks = XmlTools.getNodesByTagName(document, "peaks");
					
					for (int i=0; i<1/*nodes.getLength()*/; ++i)
					{
						Node node = nodes.item(i);
						NamedNodeMap attributes = node.getAttributes();
						
						double retentiontime = 0;
						Polarity polarity = (
								attributes.getNamedItem("polarity").getNodeValue().equals("+") ? Polarity.POSITIVE : Polarity.NEGATIVE
							);
						
						if (attributes.getNamedItem("retentionTime") != null)
						{
							String str = attributes.getNamedItem("retentionTime").getNodeValue();
							retentiontime = Double.parseDouble(str.substring(2, str.indexOf('S'))); // PT0.7128S ????
						}
						else if (i == 0)
							retentiontime = cms.getNrScans();
						
						// retrieve the mass/intensity information
						Node node_peaks = nodes_peaks.item(i);
						NamedNodeMap attributes_peaks = node_peaks.getAttributes();
						
						double[] massintensity = ByteArray.toDoubleArray(
								Base64.decode(node_peaks.getTextContent()),
								(attributes_peaks.getNamedItem("byteOrder").getNodeValue().equals("network") ? ByteArray.ENDIAN_BIG : ByteArray.ENDIAN_LITTLE),
								Integer.parseInt(attributes_peaks.getNamedItem("precision").getNodeValue())
							);
						
						// create the spectrum
						int size = massintensity.length/2;
						PeakData<Centroid> peakdata = new PeakData<Centroid>(new Centroid.Factory(), size);
						int index = 0;
						for (int j=0; j<massintensity.length; j+=2)
							peakdata.set(index++, -1, -1, massintensity[j], massintensity[j+1]);
							
						Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata, (i==0?Spectrum.Type.MS1:Spectrum.Type.MSn), polarity, -1);
						spectrum.setRetentionTime(retentiontime);
						if (i == 0)
						{
							cms.getScans().add(spectrum);
							measurement.addScanInfo(new ScanInfo(retentiontime, polarity));
						}
						else
							cms.getScans().lastElement().addMSnSpectrum(spectrum);
						
						// calculate the stats
						for (int j=0; j<peakdata.size(); ++j)
						{
							minmass = (minmass==-1 ? peakdata.getMass(j) : Math.min(minmass, peakdata.getMass(j)));
							maxmass = Math.max(maxmass, peakdata.getMass(j));
							minintensity = (minintensity==-1 ? peakdata.getIntensity(j) : Math.min(minintensity, peakdata.getIntensity(j)));
							maxintensity = Math.max(maxintensity, peakdata.getIntensity(j));
						}
					}
				}
				// mzData interface
				else if (xpath.equals("/mzData/spectrumList/spectrum"))
				{
					// still to be done
//					int msn = 1;
					double retentiontime = 0;
					Polarity polarity = Polarity.NEUTRAL;
					
					// retrieve the instrument settings
					Node node_instrument = XmlTools.getNodeByTagName(document, "spectrumInstrument");
					NodeList nodes_parameters = node_instrument.getChildNodes();
					for (int i=0; i<nodes_parameters.getLength(); ++i)
					{
						Node node_parameter = nodes_parameters.item(i);
						if (!node_parameter.getNodeName().equals("cvParam"))
							continue;
						
						NamedNodeMap attributes = node_parameter.getAttributes();
						String name = attributes.getNamedItem("name").getNodeValue();
						String value = attributes.getNamedItem("value").getNodeValue();
						if (name.equals("TimeInMinutes"))
							retentiontime = Double.parseDouble(value) * 60.;
						else if (name.equals("Polarity"))
							polarity = (value.equals("Positive") ? Polarity.POSITIVE : Polarity.NEGATIVE);
					}
					
					// retrieve the masses
					Node node_masses = XmlTools.getNodeByTagName(document, "mzArrayBinary");
					Node node_masses_data = XmlTools.getNodeByTagName(node_masses, "data");
					NamedNodeMap attributes_masses = node_masses_data.getAttributes();
					
					double[] masses = ByteArray.toDoubleArray(
							Base64.decode(node_masses_data.getTextContent()),
							(attributes_masses.getNamedItem("endian").getNodeValue().equals("little") ? ByteArray.ENDIAN_LITTLE : ByteArray.ENDIAN_BIG),
							Integer.parseInt(attributes_masses.getNamedItem("precision").getNodeValue())
						);
					
					// retrieve the intensities
					Node node_intensities = XmlTools.getNodeByTagName(document, "intenArrayBinary");
					Node node_intensities_data = XmlTools.getNodeByTagName(node_intensities, "data");
					NamedNodeMap attributes_intensities = node_intensities_data.getAttributes();
					
					double[] intensities = ByteArray.toDoubleArray(
							Base64.decode(node_intensities_data.getTextContent()),
							(attributes_intensities.getNamedItem("endian").getNodeValue().equals("little") ? ByteArray.ENDIAN_LITTLE : ByteArray.ENDIAN_BIG),
							Integer.parseInt(attributes_intensities.getNamedItem("precision").getNodeValue())
						);
					
					// sanity check
					if (masses.length != intensities.length)
						System.err.print("Strange, the masses differ in length from the intensities"); // should be exceptino
					
					// create the spectrum
					PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, masses.length);
					for (int i=0; i<masses.length; ++i)
						peakdata.set(i, -1, -1, masses[i], intensities[i]);
					
					Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata, polarity);
					spectrum.setRetentionTime(retentiontime);
					cms.getScans().add(spectrum);
					measurement.addScanInfo(new ScanInfo(retentiontime, polarity));
					
					// calculate the stats
					for (int j=0; j<peakdata.size(); ++j)
					{
						minmass = (minmass==-1 ? peakdata.getMass(j) : Math.min(minmass, peakdata.getMass(j)));
						maxmass = Math.max(maxmass, peakdata.getMass(j));
						minintensity = (minintensity==-1 ? peakdata.getIntensity(j) : Math.min(minintensity, peakdata.getIntensity(j)));
						maxintensity = Math.max(maxintensity, peakdata.getIntensity(j));
					}
				}
			}
		}
		
		Listener listener = new Listener();
		XmlParser xmlparser = new XmlParser();
		// mzXml
		xmlparser.addListener(listener, "/mzXML/msRun/scan");
		xmlparser.addListener(listener, "/mzXML/msRun/parentFile");
		xmlparser.addListener(listener, "/mzXML/msRun/msInstrument");
		xmlparser.addListener(listener, "/mzXML/msRun/dataProcessing");
		// mzData
		xmlparser.addListener(listener, "/mzData/spectrumList/spectrum");
		xmlparser.parse(input);
		
		// normalize the relative intensity values
//		cms.lowmass			= listener.minmass;
//		cms.highmass		= listener.maxmass;
//		cms.lowintensity	= listener.minintensity;
//		cms.highintensity	= listener.maxintensity;
		
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
