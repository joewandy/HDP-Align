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



package peakml.io.peakml;


// java
import java.io.*;
import java.util.*;
import java.util.zip.*;

import org.w3c.dom.*;

//libraries
import domsax.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.util.*;
import peakml.chemistry.*;





/**
 * Central class for parsing PeakML type files. All access to the parser is handled
 * with static access functions, so no instance should be made (and indeed can be
 * made, as the default constructor is hidden). As a rule all access functions return an
 * instance of the {@link ParseResult} filled with the header and peak information. Also,
 * each of the parse-functions can be plugged with {@link ParserProgressListener})
 * instance, which is used to update on the progress of parsing. Only
 * {@link PeakMLParser#parse(InputStream, ParserProgressListener, boolean)}
 * forms the exception to these rules, as this function utilizes the
 * {@link PeakMLProgressListener} as the mechanism to return the results to the user.
 * <p />
 * The parser defines specialized access functions to load PeakML files filled with
 * only a single {@link IPeak} subclass (e.g. {@link PeakMLParser#parseCentroids(InputStream, ParserProgressListener)}).
 * Use these functions with caution and only when you are sure the file in
 * question contains only these types of peaks or you want to enforce this to be
 * the case. When such an access-function encounters a different {@link IPeak} subclass
 * than expected and exception is thrown.
 * <p />
 * The alternative is to use one of the parse-functions (i.e. the name of the function
 * is parse), which will handle all types of {@link IPeak} subclasses and return
 * the peaks as IPeak (with the class information mechanism of Java one can find
 * out the exact type). These functions also allow you to define whether to load
 * all data associated to an {@link IPeak} subclass, or only to load the general
 * {@link IPeak} data. When only the data {@link IPeak} data is needed it will
 * yield a performance boost in memory consumption.
 */
public class PeakMLParser
{
	// static access
	public static Header parseHeader(InputStream in) throws IOException, XmlParserException
	{
		final ParseResult result = new ParseResult();
		class myListener implements XmlParserListener
		{
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				if (xpath.equals(XPATH_HEADER))
				{
					result.header = parseHeader(document.getFirstChild());
					throw new XmlParserException("finished");
				}
			}			
		}
		try {
			run(in, new myListener());
		} catch (XmlParserException e) { ; }

		return result.header;
	}
	
	/**
	 * Parse function for blindly loading data from a PeakML file. This method calls
	 * {@link PeakMLParser#parse(InputStream, ParserProgressListener, boolean)} with
	 * the parameter listener set to null.
	 * 
	 * @param in				The input-stream to load the data from.
	 * @param loadall			If set to false only the data known to IPeak is loaded as class Peak.
	 * @return					The header and peak information stored in the file.
	 * @throws IOException		Thrown on an IOException.
	 * @throws XmlParserException
	 * 							Thrown when an unknown IPeak object is encountered.
	 */
	public static ParseResult parse(InputStream in, boolean loadall) throws IOException, XmlParserException
	{
		return parse(in, (ParserProgressListener) null, loadall);
	}
	
	/**
	 * Parse function for blindly loading data from a PeakML file. This method loads the
	 * data in a PeakML file as it encounters it in the file. This means that a mix-model
	 * is essentially possible for peak-data stored in a PeakML file. The resulting
	 * {@link ParseResult} instance is parameterized with {@link IPeak}. The class-information
	 * made available through the Java language can be used to determine the original
	 * type of the peak. The function employs a callback mechanism with
	 * {@link ParserProgressListener} to return information about the progress through
	 * the file. This is calculated with the information returned by
	 * {@link Header#getNrPeaks()}.
	 * <p />
	 * The loadall parameter can be used to restrict the amount of data actually being
	 * loaded by the function. If this is set to false only data known by the {@link IPeak}
	 * class is loaded (in this implementation this means that an instance of the
	 * {@link Centroid} is made). The class information cannot be used to determine the
	 * original type of the peak when loadall is set to false.
	 * 
	 * @param in				The input-stream to load the data from.
	 * @param listener			The progress listener.
	 * @param loadall			If set to false only the data known to IPeak is loaded as class Peak.
	 * @return					The header and peak information stored in the file.
	 * @throws IOException		Thrown on an IOException.
	 * @throws XmlParserException
	 * 							Thrown when an unknown IPeak object is encountered.
	 */
	public static ParseResult parse(InputStream in, ParserProgressListener listener, boolean loadall) throws IOException, XmlParserException
	{
//		final boolean _loadall = loadall;
		final ParserProgressListener _listener = listener;
		
		final ParseResult result = new ParseResult();
		final Vector<IPeak> peaks = new Vector<IPeak>();
		
		class myListener implements XmlParserListener
		{
			int index = 0;
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				if (xpath.equals(XPATH_IPEAK))
				{
					Node node = document.getChildNodes().item(0);
					
					// check whether we're getting the correct ipeak
					Node typeattribute = node.getAttributes().getNamedItem(PeakMLWriter.TYPE);
					if (typeattribute == null)
						throw new XmlParserException("Failed to locate a type attribute.");
					
					// ...
//					IPeak peak = (_loadall ? parseIPeak(node) : parseCentroid(node));
					IPeak peak = parseIPeak(node);
					if (peak != null)
						peaks.add(peak);
					
					//
					if (_listener!=null && result.header!=null && result.header.getNrPeaks()!=0)
						_listener.update((100.*index++)/result.header.getNrPeaks());
				}
				else if (xpath.equals(XPATH_HEADER))
				{
					result.header = parseHeader(document.getFirstChild());
				}
			}
		}
		run(in, new myListener());
		
		result.measurement = new IPeakSet<IPeak>(peaks);
		return result;
	}
	
	/**
	 * Parse function for blindly loading data from a PeakML file. This method is
	 * essentially the same as {@link PeakMLParser#parse(InputStream, PeakMLProgressListener, boolean)}
	 * except that it uses the {@link PeakMLProgressListener} interface as a callback
	 * mechanism to return the header and peak-information. As all data is returned
	 * through this the method does not have a return-value.
	 * 
	 * @param in				The input-stream to load the data from.
	 * @param listener			The parser listener, which is used as a callback mechanism to deliver header and peak information.
	 * @param loadall			If set to false only the data known to IPeak is loaded as class Peak.
	 * @throws IOException		Thrown on an IOException.
	 * @throws XmlParserException
	 * 							Thrown when an unknown IPeak object is encountered.
	 */
	public static void parse(InputStream in, PeakMLProgressListener listener, boolean loadall) throws IOException, XmlParserException
	{
//		final boolean _loadall = loadall;
		final PeakMLProgressListener _listener = listener;
		
		class myListener implements XmlParserListener
		{
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				if (xpath.equals(XPATH_IPEAK))
				{
					Node node = document.getChildNodes().item(0);
					
					// check whether we're getting the correct ipeak
					Node typeattribute = node.getAttributes().getNamedItem(PeakMLWriter.TYPE);
					if (typeattribute == null)
						throw new XmlParserException("Failed to locate a type attribute.");
					
					// ...
//					IPeak peak = (_loadall ? parseIPeak(node) : parseCentroid(node));
					IPeak peak = parseIPeak(node);
					if (peak != null)
						_listener.onIPeak(peak);
				}
				else if (xpath.equals(XPATH_HEADER))
				{
					Header header = parseHeader(document.getFirstChild());
					_listener.onHeader(header);
				}
			}
		}
		run(in, new myListener());
		
		listener.onFinish();
	}
	
	/**
	 * Parse function for loading a PeakML file containing only masschromatogram entries. When an entry
	 * of another type is encountered an {@link XmlParserException} is thrown. The resulting
	 * {@link ParseResult} instance is type-bound to {@link MassChromatogram} to force only masschromatogram types.
	 * 
	 * @param in				The input-stream to load the data from.
	 * @return					The header and peak information stored in the file.
	 * @throws IOException		Thrown on an IOException.
	 * @throws XmlParserException
	 * 							Thrown when an unknown IPeak object is encountered.
	 */
	public static ParseResult parseMassChromatograms(InputStream in, ParserProgressListener listener) throws IOException, XmlParserException
	{
		final ParseResult result = new ParseResult();
		final Vector<MassChromatogram<? extends Peak>> peaks = new Vector<MassChromatogram<? extends Peak>>();
		final ParserProgressListener _listener = listener;
		
		class myListener implements XmlParserListener
		{
			int index = 0;
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				if (xpath.equals(XPATH_IPEAK))
				{
					Node node = document.getChildNodes().item(0);
					
					// check whether we're getting the correct ipeak
					Node typeattribute = node.getAttributes().getNamedItem(PeakMLWriter.TYPE);
					if (typeattribute==null)
						throw new XmlParserException("Failed to locate the type attribute.");
					if (!typeattribute.getNodeValue().equals(PeakMLWriter.TYPE_MASSCHROMATOGRAM))
						throw new XmlParserException("IPeak (" + typeattribute.getNodeValue() + ") is not of type: '" + PeakMLWriter.TYPE_MASSCHROMATOGRAM + "'");
					
					// parse this node as a mass chromatogram
					MassChromatogram<? extends Peak> masschromatogram = parseMassChromatogram(node);
					if (masschromatogram != null)
						peaks.add(masschromatogram);
					
					//
					if (_listener!=null && result.header!=null && result.header.getNrPeaks()!=0)
						_listener.update((100.*index++)/result.header.getNrPeaks());
				}
				else if (xpath.equals(XPATH_HEADER))
				{
					result.header = parseHeader(document.getFirstChild());
				}
			}
		}
		run(in, new myListener());
		
		result.measurement = new IPeakSet<MassChromatogram<? extends Peak>>(peaks);
		return result;
	}
	
	/**
	 * Parse function for loading a PeakML file containing only backgroundion entries. When an entry
	 * of another type is encountered an {@link XmlParserException} is thrown. The resulting
	 * {@link ParseResult} instance is type-bound to {@link BackgroundIon} to force only backgroundion types.
	 * 
	 * @param in				The input-stream to load the data from.
	 * @return					The header and peak information stored in the file.
	 * @throws IOException		Thrown on an IOException.
	 * @throws XmlParserException
	 * 							Thrown when an unknown IPeak object is encountered.
	 */
	public static ParseResult parseBackgroundIons(InputStream in, ParserProgressListener listener) throws IOException, XmlParserException
	{
		final ParseResult result = new ParseResult();
		final Vector<BackgroundIon<? extends Peak>> peaks = new Vector<BackgroundIon<? extends Peak>>();
		final ParserProgressListener _listener = listener;
		
		class myListener implements XmlParserListener
		{
			int index = 0;
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				if (xpath.equals(XPATH_IPEAK))
				{
					Node node = document.getFirstChild();
					
					// check whether we're getting the correct ipeak
					Node typeattribute = node.getAttributes().getNamedItem(PeakMLWriter.TYPE);
					if (typeattribute==null)
						throw new XmlParserException("Failed to locate the type attribute.");
					if (!typeattribute.getNodeValue().equals(PeakMLWriter.TYPE_BACKGROUNDION))
						throw new XmlParserException("IPeak (" + typeattribute.getNodeValue() + ") is not of type: '" + PeakMLWriter.TYPE_BACKGROUNDION + "'");
					
					// parse this node as a mass chromatogram
					BackgroundIon<? extends Peak> backgroundion = parseBackgroundIon(node);
					if (backgroundion != null)
						peaks.add(backgroundion);
					
					//
					if (_listener!=null && result.header!=null && result.header.getNrPeaks()!=0)
						_listener.update((100.*index++)/result.header.getNrPeaks());
				}
				else if (xpath.equals(XPATH_HEADER))
				{
					result.header = parseHeader(document.getFirstChild());
				}
			}
		}
		run(in, new myListener());
		
		result.measurement = new IPeakSet<BackgroundIon<? extends Peak>>(peaks);
		return result;
	}
	
	/**
	 * Parse function for loading a PeakML file containing only peakset entries (containing other
	 * types of entries). When an entry of another type is encountered an {@link XmlParserException}
	 * is thrown. The resulting {@link ParseResult} instance is type-bound to {@link IPeakSet} to force
	 * only peakset types.
	 * 
	 * @param in				The input-stream to load the data from.
	 * @return					The header and peak information stored in the file.
	 * @throws XmlParserException
	 * 							Thrown when an unknown IPeak object is encountered.
	 */
	public static ParseResult parseIPeakSet(InputStream in, ParserProgressListener listener) throws IOException, XmlParserException
	{
		final ParseResult result = new ParseResult();
		final Vector<IPeakSet<? extends IPeak>> peaks = new Vector<IPeakSet<? extends IPeak>>();
		final ParserProgressListener _listener = listener;
		
		class myListener implements XmlParserListener
		{
			int index = 0;
			public void onDocument(Document document, String xpath) throws XmlParserException
			{
				if (xpath.equals(XPATH_IPEAK))
				{
					Node node = document.getChildNodes().item(0);
					
					// check whether we're getting the correct ipeak
					Node typeattribute = node.getAttributes().getNamedItem(PeakMLWriter.TYPE);
					if (typeattribute==null || !typeattribute.getNodeValue().equals(PeakMLWriter.TYPE_PEAKSET))
						throw new XmlParserException("Failed to locate a type attribute.");
					
					// parse this node as a mass chromatogram
					IPeakSet<? extends IPeak> peakset = parsePeakSet(node);
					if (peakset != null)
						peaks.add(peakset);
					
					//
					if (_listener!=null && result.header!=null && result.header.getNrPeaks()!=0)
						_listener.update((100.*index++)/result.header.getNrPeaks());
				}
				else if (xpath.equals(XPATH_HEADER))
				{
					result.header = parseHeader(document.getFirstChild());
				}
			}
		}
		run(in, new myListener());
		
		result.measurement = new IPeakSet<IPeakSet<? extends IPeak>>(peaks);
		return result;
	}
	
	
	// implementation of the parse functions
	private PeakMLParser()
	{
	}
	
	private static void run(InputStream in, XmlParserListener listener) throws IOException, XmlParserException
	{
		XmlParser parser = new XmlParser();
		parser.addListener(listener, XPATH_IPEAK);
		parser.addListener(listener, XPATH_HEADER);
		
		parser.parse(detect(in));
	}
	
	private static InputStream detect(InputStream input) throws IOException
	{
		BufferedInputStream reader = new BufferedInputStream(input, 1024*1024);
		reader.mark(1000);
		try {
			return new GZIPInputStream(reader);
		} catch (Exception e) { ; }
		reader.reset();
		return reader;
	}
	
	private static Header parseHeader(Node parent) throws XmlParserException
	{
		Header header = new Header();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			try {
				if (element.getTagName().equals("nrpeaks"))
					header.setNrPeaks(Integer.parseInt(element.getTextContent()));
				else if (element.getTagName().equals("date"))
					header.setDate(element.getTextContent());
				else if (element.getTagName().equals("owner"))
					header.setOwner(element.getTextContent());
				else if (element.getTagName().equals("description"))
					header.setDescription(element.getTextContent());
				else if (element.getTagName().equals("sets"))
					header.addSetInfos(parseSets(element));
				else if (element.getTagName().equals("measurements"))
					header.addMeasurementInfos(parseMeasurements(element));
				else if (element.getTagName().equals("annotations"))
				{
					Vector<Annotation> annotations = parseAnnotations(element);
					if (annotations != null)
						for (Annotation annotation : annotations) header.addAnnotation(annotation);
				}
			}
			catch (Exception e) {
				throw new XmlParserException("Invalid value in header (" + element.getTagName() + "): '" + e.getMessage() + "'.");
			}
		}
		
		return header;
	}
	
	private static Vector<SetInfo> parseSets(Node parent) throws XmlParserException
	{
		Vector<SetInfo> setinfos = new Vector<SetInfo>();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("set"))
				setinfos.add(parseSet(node));
		}
		
		return setinfos;
	}
	
	private static SetInfo parseSet(Node parent) throws XmlParserException
	{
		String id = "";
		String type = "";
		String measurementids = null;
		
		NodeList nodes = parent.getChildNodes();
		Vector<SetInfo> sets = new Vector<SetInfo>();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("id"))
				id = element.getTextContent();
			else if (element.getTagName().equals("set"))
				sets.add(parseSet(element));
			else if (element.getTagName().equals("type"))
				type = element.getTextContent();
			else if (element.getTagName().equals("measurementids"))
				measurementids = element.getTextContent();
		}
		
		// create the set
		SetInfo set = new SetInfo(id, Integer.parseInt(type));
		if (measurementids != null)
		{
			int mids[] = ByteArray.toIntArray(Base64.decode(measurementids), ByteArray.ENDIAN_LITTLE, 32);
			for (int mid : mids)
				set.addMeasurementID(mid);
		}
		
		// add the children
		for (SetInfo s : sets)
			set.addChild(s);
		
		return set;
	}
	
	private static Vector<MeasurementInfo> parseMeasurements(Node parent) throws XmlParserException
	{
		Vector<MeasurementInfo> measurements = new Vector<MeasurementInfo>();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("measurement"))
				measurements.add(parseMeasurement(node));
		}
		
		return measurements;
	}
	
	private static MeasurementInfo parseMeasurement(Node parent) throws XmlParserException
	{
		String id = "";
		String label = "";
		String sampleid = "";
		Vector<FileInfo> files = null;
		Vector<ScanInfo> scans = null;
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("id"))
				id = element.getTextContent();
			else if (element.getTagName().equals("label"))
				label = element.getTextContent();
			else if (element.getTagName().equals("sampleid"))
				sampleid = element.getTextContent();
			else if (element.getTagName().equals("scans"))
				scans = parseScans(node);
			else if (element.getTagName().equals("files"))
				files = parseFiles(node);
		}
		
		MeasurementInfo measurement = new MeasurementInfo(Integer.parseInt(id), sampleid);
		measurement.setLabel(label);
		measurement.addFileInfos(files);
		if (scans != null)
			measurement.addScanInfos(scans);
		
		return measurement;
	}
	
	private static Vector<ScanInfo> parseScans(Node parent) throws XmlParserException
	{
		Vector<ScanInfo> scans = new Vector<ScanInfo>();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("scan"))
				scans.add(parseScan(node));
		}
		
		return scans;
	}
	
	private static ScanInfo parseScan(Node parent) throws XmlParserException
	{
		double retentiontime = 0;
		Polarity polarity = Polarity.NEUTRAL;
		Vector<Annotation> annotations = null;
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("polarity"))
				polarity = Polarity.valueOf(element.getTextContent());
			else if (element.getTagName().equals("retentiontime"))
				retentiontime = Double.parseDouble(element.getTextContent());
			else if (element.getTagName().equals("annotations"))
				annotations = parseAnnotations(element);
		}
		
		ScanInfo scan = new ScanInfo(retentiontime, polarity);
		if (annotations != null)
			scan.addAnnotations(annotations);
		return scan;
	}
	
	private static Vector<FileInfo> parseFiles(Node parent) throws XmlParserException
	{
		Vector<FileInfo> files = new Vector<FileInfo>();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("file"))
				files.add(parseFile(node));
		}
		
		return files;
	}
	
	private static FileInfo parseFile(Node parent) throws XmlParserException
	{
		String label = "";
		String name = "";
		String location = "";
		Vector<Annotation> annotations = null;
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("label"))
				label = element.getTextContent();
			else if (element.getTagName().equals("name"))
				name = element.getTextContent();
			else if (element.getTagName().equals("location"))
				location = element.getTextContent();
			else if (element.getTagName().equals("annotations"))
				annotations = parseAnnotations(node);
		}
		
		FileInfo file = new FileInfo(label, name, location);
		if (annotations != null)
			file.addAnnotations(annotations);
		return file;
	}
	
	private static IPeak parseIPeak(Node parent) throws XmlParserException
	{
		Node type = parent.getAttributes().getNamedItem(PeakMLWriter.TYPE);
		if (type == null)
			return null;
		
		else if (type.getNodeValue().equals(PeakMLWriter.TYPE_PEAKSET))
			return parsePeakSet(parent);
		else if (type.getNodeValue().equals(PeakMLWriter.TYPE_BACKGROUNDION))
			return parseBackgroundIon(parent);
		else if (type.getNodeValue().equals(PeakMLWriter.TYPE_MASSCHROMATOGRAM))
			return parseMassChromatogram(parent);
		else
			return null;
	}
	
	private static void parseIPeak(Node parent, IPeak peak) throws XmlParserException
	{
		// retrieve all the properties
		int scan = -1;
		double retentiontime = -1;
		double mass = -1;
		double intensity = -1;
		int patternid = -1;
		int measurementid = -1;
//		String sha1 = null;
		Vector<Annotation> annotations = null;
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("patternid"))
				patternid = Integer.parseInt(element.getTextContent());
			else if (element.getTagName().equals("measurementid"))
				measurementid = Integer.parseInt(element.getTextContent());
			else if (element.getTagName().equals("annotations"))
				annotations = parseAnnotations(element);
			else if (element.getTagName().equals("scan"))
				scan = Integer.parseInt(element.getTextContent());
			else if (element.getTagName().equals("retentiontime"))
				retentiontime = Double.parseDouble(element.getTextContent());
			else if (element.getTagName().equals("mass"))
				mass = Double.parseDouble(element.getTextContent());
			else if (element.getTagName().equals("intensity"))
				intensity = Double.parseDouble(element.getTextContent());
//			else if (element.getTagName().equals("sha1sum"))
//				sha1 = element.getTextContent();
		}
		
		// check whether obligatory values are missing
		if (mass==-1 || intensity==-1)
			throw new XmlParserException("Mass and/or intensity information is missing for IPeak.");
		
		peak.setScanID(scan);
		peak.setRetentionTime(retentiontime);
		peak.setMass(mass);
		peak.setIntensity(intensity);
		peak.setPatternID(patternid);
		peak.setMeasurementID(measurementid);
		
		if (annotations != null)
			for (Annotation annotation : annotations) peak.addAnnotation(annotation);
		
		// check whether 
//		if (sha1!=null && !sha1.equals(peak.sha1()))
//			throw new XmlParserException("SHA1-sum for individual ipeak element does not match.");
	}
	
	
	@SuppressWarnings("unchecked")
	private static MassChromatogram<? extends Peak> parseMassChromatogram(Node parent) throws XmlParserException
	{
		// retrieve the separate peaks
		PeakData<? extends Peak> peakdata = null;
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("peakdata"))
			{
				peakdata = parsePeakData(element);
			}
		}
		
		// create the bugger
		MassChromatogram<? extends Peak> masschromatogram = null;
		if (peakdata.getFactory().getPeakClass().equals(Centroid.class))
			masschromatogram = new MassChromatogram<Centroid>((PeakData<Centroid>) peakdata);
		parseIPeak(parent, masschromatogram);
		return masschromatogram;
	}
	
	@SuppressWarnings("unchecked")
	private static BackgroundIon<? extends Peak> parseBackgroundIon(Node parent) throws XmlParserException
	{
		// retrieve the separate peaks
		PeakData<? extends Peak> peakdata = null;
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("peakdata"))
			{
				peakdata = parsePeakData(element);
			}
		}
		
		// create the bugger
		BackgroundIon<? extends Peak> backgroundion = null;
		if (peakdata.getFactory().getPeakClass().equals(Centroid.class))
			backgroundion = new BackgroundIon<Centroid>((PeakData<Centroid>) peakdata);
		parseIPeak(parent, backgroundion);
		return backgroundion;
	}
	
	private static IPeakSet<? extends IPeak> parsePeakSet(Node parent) throws XmlParserException
	{
		// retrieve all the properties
		Vector<IPeak> peaks = new Vector<IPeak>();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("peaks"))
			{
				NodeList nodes2 = node.getChildNodes();
				for (int nodeid2=0; nodeid2<nodes2.getLength(); ++nodeid2)
				{
					Node node2 = nodes2.item(nodeid2);
					if (node2.getNodeType() != Node.ELEMENT_NODE)
						continue;
					
					IPeak peak = parseIPeak(node2);
					if (peak != null)
						peaks.add(peak);
				}				
			}
		}
		
		// create the bugger
		IPeakSet<IPeak> peakset = new IPeakSet<IPeak>(peaks);
		parseIPeak(parent, peakset);
		return peakset;
	}
	
	private static PeakData<? extends Peak> parsePeakData(Node parent) throws XmlParserException
	{
		// get the attributes
		Node typeattribute = parent.getAttributes().getNamedItem(PeakMLWriter.TYPE);
		if (typeattribute == null)
			throw new XmlParserException("Failed to locate a type attribute.");
		Node sizeattribute = parent.getAttributes().getNamedItem(PeakMLWriter.SIZE);
		if (sizeattribute == null)
			throw new XmlParserException("Failed to locate a size attribute.");
		
		int size = Integer.parseInt(sizeattribute.getNodeValue());
		String type = typeattribute.getNodeValue();
		
		// create the arrays
		int scanids[]					= null;
		int patternids[]				= null;
		int measurementids[]			= null;
		double masses[]					= null;
		double intensities[]			= null;
		double retentiontimes[]			= null;
		
		// retrieve all the data
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("scanids"))
				scanids = ByteArray.toIntArray(Base64.decode(element.getTextContent()), ByteArray.ENDIAN_LITTLE, 32);
			else if (element.getTagName().equals("patternids"))
				patternids = ByteArray.toIntArray(Base64.decode(element.getTextContent()), ByteArray.ENDIAN_LITTLE, 32);
			else if (element.getTagName().equals("measurementids"))
				measurementids = ByteArray.toIntArray(Base64.decode(element.getTextContent()), ByteArray.ENDIAN_LITTLE, 32);
			else if (element.getTagName().equals("masses"))
				masses = ByteArray.toDoubleArray(Base64.decode(element.getTextContent()), ByteArray.ENDIAN_LITTLE, 32);
			else if (element.getTagName().equals("intensities"))
				intensities = ByteArray.toDoubleArray(Base64.decode(element.getTextContent()), ByteArray.ENDIAN_LITTLE, 32);
			else if (element.getTagName().equals("retentiontimes"))
				retentiontimes = ByteArray.toDoubleArray(Base64.decode(element.getTextContent()), ByteArray.ENDIAN_LITTLE, 32);
		}
		
		// create the PeakData instance
		if (type.equals("centroid"))
			return new PeakData<Centroid>(Centroid.factory, size, scanids, patternids, measurementids, masses, intensities, retentiontimes);
		return null;
	}
	
	private static Vector<Annotation> parseAnnotations(Node parent) throws XmlParserException
	{
		Vector<Annotation> annotations = new Vector<Annotation>();
		
		NodeList nodes = parent.getChildNodes();
		for (int nodeid=0; nodeid<nodes.getLength(); ++nodeid)
		{
			Node node = nodes.item(nodeid);
			if (node.getNodeType() != Node.ELEMENT_NODE)
				continue;
			
			Element element = (Element) node;
			if (element.getTagName().equals("annotation"))
			{
				String label=null, value=null, valuetype=null, unit=null;
				NodeList annotation_nodes = element.getChildNodes();
				for (int annotationid=0; annotationid<annotation_nodes.getLength(); ++annotationid)
				{
					Node annotation_node = annotation_nodes.item(annotationid);
					if (annotation_node.getNodeType() != Node.ELEMENT_NODE)
						continue;
					
					Element annotation_element = (Element) annotation_node;
					if (annotation_element.getTagName().equals("label"))
						label = annotation_element.getTextContent();
					else if (annotation_element.getTagName().equals("value"))
						value = annotation_element.getTextContent();
					else if (annotation_element.getTagName().equals("valuetype"))
						valuetype = annotation_element.getTextContent();
				}
				
				if (label==null || value==null || valuetype==null)
					throw new XmlParserException("Annotation is missing either: label, value or valuetype");
				
				Annotation annotation = new Annotation(label, value, Annotation.ValueType.valueOf(valuetype));
				annotation.setUnit(unit);
				if (annotation.getValueType() == Annotation.ValueType.ONTOLOGY)
					annotation.setOntologyRef(element.getAttribute("ontologyref"));
				if (element.getAttribute("unit") != null)
					annotation.setUnit(element.getAttribute("unit"));
				annotations.add(annotation);
			}
		}
		
		return annotations;
	}
	
	
	// fixed names
	private static final String XPATH_IPEAK		= "/peakml/peaks/peak";
	private static final String XPATH_HEADER	= "/peakml/header";
	
	
	// automatic version check
	static {
		if (domsax.Version.major < 1)
			throw new RuntimeException("The DomSAX-library is too old.");
	}
}
