// Copyright GBiC (http://gbic.biol.rug.nl), 2007

package peakml.io.peakml;


// java
import java.io.*;
import java.util.*;
import java.text.*;

// libraries
import domsax.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.util.*;





/**
 * Central class for writing PeakML type files. All access to the writer is handled
 * with static access functions, so no instance should be made (and indeed can be
 * made, as the default constructor is hidden). As a rule each write-function can
 * be plugged with a {@link WriterProgressListener}) instance, which is used
 * to update on the progress of writing.
 * <p />
 * The parser defines specialized access functions to write PeakML files filled with
 * only a single {@link IPeak} subclass (e.g.
 * {@link PeakMLWriter#writeCentroids(Header, Vector, WriterProgressListener, OutputStream, String)}).
 * When a mix-model is required or it is unknown which types of peaks are contained in
 * the vector of peaks to be written, the generic function
 * {@link PeakMLWriter#write(Header, Vector, WriterProgressListener, OutputStream, String)})
 * can be used. This method determines the type of each peak and writes it to the
 * file accordingly.
 */
public class PeakMLWriter
{
	// static access
	/**
	 * Generic method for writing a set of {@link IPeak} elements (the real class unknown). The method
	 * will find out for each element its real type and write the data accordingly.
	 * 
	 * @param header			The header to be written to the file.
	 * @param peaks				The peaks to be written to the file.
	 * @param listener			The listener for tracking the progress of the write-process.
	 * @param out				The output-stream to write to.
	 * @param stylesheet		The stylesheet to include (filename, can be null).
	 * @throws IOException		Thrown when an I/O error is encountered.
	 */
	@SuppressWarnings("unchecked")
	public static void write(Header header, List<? extends IPeak> peaks, WriterProgressListener listener, OutputStream out, String stylesheet) throws IOException
	{
		if (header != null)
			header.setNrPeaks(peaks.size());
		
		int index = 0;
		PeakMLWriter writer = new PeakMLWriter(header, out, stylesheet);
		for (IPeak peak : peaks)
		{
			if (listener != null)
				listener.update((100.*index++)/peaks.size());
			
			Class<? extends IPeak> c = peak.getClass();
			if (c.equals(Peak.class))
				writer.write((Peak) peak, true);
			else if (c.equals(MassChromatogram.class))
				writer.write((MassChromatogram) peak, true);
			else if (c.equals(BackgroundIon.class))
				writer.write((BackgroundIon) peak, true);
			else if (c.equals(IPeakSet.class))
				writer.write((IPeakSet<? extends IPeak>) peak, true);
			else
				throw new IOException("Unknown IPeak-type: " + c.getName());
		}
		writer.close();
	}
	
	/**
	 * Generic method for writing a set of {@link IPeak} elements (the real class unknown). The method
	 * will find out for each element its real type and write the data accordingly.
	 * 
	 * @param header			The header to be written to the file.
	 * @param peaks				The peaks to be written to the file.
	 * @param listener			The listener for tracking the progress of the write-process.
	 * @param out				The output-stream to write to.
	 * @param stylesheet		The stylesheet to include (filename, can be null).
	 * @throws IOException		Thrown when an I/O error is encountered.
	 */
	public static void writePeaks(Header header, Vector<Peak> peaks, WriterProgressListener listener, OutputStream out, String stylesheet) throws IOException
	{
		if (header != null)
			header.setNrPeaks(peaks.size());
		
		int index = 0;
		PeakMLWriter writer = new PeakMLWriter(header, out, stylesheet);
		for (Peak peak : peaks)
		{
			if (listener != null)
				listener.update((100.*index++)/peaks.size());
			writer.write(peak, true);
		}
		writer.close();
	}
	
	/**
	 * Method for writing a set of {@link Centroid} peaks to a PeakML file.
	 * 
	 * @param header			The header to be written to the file.
	 * @param peaks				The peaks to be written to the file.
	 * @param listener			The listener for tracking the progress of the write-process.
	 * @param out				The output-stream to write to.
	 * @param stylesheet		The stylesheet to include (filename, can be null).
	 * @throws IOException		Thrown when an I/O error is encountered.
	 */
	public static void writeCentroids(Header header, Vector<Centroid> peaks, WriterProgressListener listener, OutputStream out, String stylesheet) throws IOException
	{
		if (header != null)
			header.setNrPeaks(peaks.size());
		
		int index = 0;
		PeakMLWriter writer = new PeakMLWriter(header, out, stylesheet);
		for (Centroid peak : peaks)
		{
			if (listener != null)
				listener.update((100.*index++)/peaks.size());
			writer.write(peak, true);
		}
		writer.close();
	}
	
	/**
	 * Method for writing a set of {@link MassChromatogram} peaks to a PeakML file.
	 * 
	 * @param header			The header to be written to the file.
	 * @param masschromatograms	The peaks to be written to the file.
	 * @param listener			The listener for tracking the progress of the write-process.
	 * @param out				The output-stream to write to.
	 * @param stylesheet		The stylesheet to include (filename, can be null).
	 * @throws IOException		Thrown when an I/O error is encountered.
	 */
	public static void writeMassChromatograms(Header header, Vector<MassChromatogram<? extends Peak>> masschromatograms, WriterProgressListener listener, OutputStream out, String stylesheet) throws IOException
	{
		if (header != null)
			header.setNrPeaks(masschromatograms.size());
		
		int index = 0;
		PeakMLWriter writer = new PeakMLWriter(header, out, stylesheet);
		for (MassChromatogram<? extends Peak> masschromatogram : masschromatograms)
		{
			if (listener != null)
				listener.update((100.*index++)/masschromatograms.size());
			writer.write(masschromatogram, true);
		}
		writer.close();
	}
	
	/**
	 * Method for writing a set of {@link BackgroundIon} peaks to a PeakML file.
	 * 
	 * @param header			The header to be written to the file.
	 * @param backgroundions	The peaks to be written to the file.
	 * @param listener			The listener for tracking the progress of the write-process.
	 * @param out				The output-stream to write to.
	 * @param stylesheet		The stylesheet to include (filename, can be null).
	 * @throws IOException		Thrown when an I/O error is encountered.
	 */
	public static void writeBackgroundIons(Header header, Vector<BackgroundIon<? extends Peak>> backgroundions, WriterProgressListener listener, OutputStream out, String stylesheet) throws IOException
	{
		if (header != null)
			header.setNrPeaks(backgroundions.size());
		
		int index = 0;
		PeakMLWriter writer = new PeakMLWriter(header, out, stylesheet);
		for (BackgroundIon<? extends Peak> backgroundion : backgroundions)
		{
			if (listener != null)
				listener.update((100.*index++)/backgroundions.size());
			writer.write(backgroundion, true);
		}
		writer.close();
	}
	
	/**
	 * Method for writing a set of {@link IPeakSet} peaks to a PeakML file.
	 * 
	 * @param header			The header to be written to the file.
	 * @param peaksets			The peaks to be written to the file.
	 * @param listener			The listener for tracking the progress of the write-process.
	 * @param out				The output-stream to write to.
	 * @param stylesheet		The stylesheet to include (filename, can be null).
	 * @throws IOException		Thrown when an I/O error is encountered.
	 */
	public static void writeIPeakSets(Header header, Vector<IPeakSet<IPeak>> peaksets, WriterProgressListener listener, OutputStream out, String stylesheet) throws IOException
	{
		if (header != null)
			header.setNrPeaks(peaksets.size());
		
		int index = 0;
		PeakMLWriter writer = new PeakMLWriter(header, out, stylesheet);
		for (IPeakSet<? extends IPeak> peakset : peaksets)
		{
			if (listener != null)
				listener.update((100.*index++)/peaksets.size());
			writer.write(peakset, true);
		}
		writer.close();
	}
	
	
	// implementation of the write functions
	protected PeakMLWriter(Header header, OutputStream out) throws IOException
	{
		this(header, out, null);
	}
	
	protected PeakMLWriter(Header header, OutputStream out, String stylesheet) throws IOException
	{
		xml = new XmlWriter(new BufferedOutputStream(out, 1024*1024), stylesheet);
		
		// write the opening tag
		xml.writeTag("peakml", XmlWriter.Tag.OPEN, new XmlAttribute("version", "1.0.0"));
		
		// write the header
		if (header != null)
		{
			xml.writeTag("header", XmlWriter.Tag.OPEN);
			xml.writeElement("nrpeaks",		Integer.toString(header.getNrPeaks()));
			xml.writeElement("date",		dateformat.format(header.getDate()));
			xml.writeElement("owner",		header.getOwner());
			xml.writeElement("description",	header.getDescription());
			
			if (header.getNrSetInfos() > 0)
			{
				xml.writeTag("sets", XmlWriter.Tag.OPEN);
				for (SetInfo set : header.getSetInfos())
					writeSet(set);
				xml.writeTag("sets", XmlWriter.Tag.CLOSE);
			}
			if (header.getNrMeasurementInfos() > 0)
			{
				xml.writeTag("measurements", XmlWriter.Tag.OPEN);
				for (MeasurementInfo measurement : header.getMeasurementInfos())
				{
					xml.writeTag("measurement", XmlWriter.Tag.OPEN);
					xml.writeElement("id", Integer.toString(measurement.getID()));
					xml.writeElement("label", measurement.getLabel());
					xml.writeElement("sampleid", measurement.getSampleID());
					
					xml.writeTag("scans", XmlWriter.Tag.OPEN);
					for (ScanInfo scan : measurement.getScanInfos())
					{
						xml.writeTag("scan", XmlWriter.Tag.OPEN);
						xml.writeElement("polarity", scan.getPolarity().toString());
						xml.writeElement("retentiontime", Double.toString(scan.getRetentionTime()));
						if (scan.getAnnotations() != null)
							writeAnnotations(scan.getAnnotations().values());
						xml.writeTag("scan", XmlWriter.Tag.CLOSE);
					}
					xml.writeTag("scans", XmlWriter.Tag.CLOSE);
					
					xml.writeTag("files", XmlWriter.Tag.OPEN);
					for (FileInfo file : measurement.getFileInfos())
					{
						xml.writeTag("file", XmlWriter.Tag.OPEN);
						xml.writeElement("label", file.getLabel());
						xml.writeElement("name", file.getName());
						xml.writeElement("location", file.getLocation());
						xml.writeTag("file", XmlWriter.Tag.CLOSE);
					}
					xml.writeTag("files", XmlWriter.Tag.CLOSE);
					xml.writeTag("measurement", XmlWriter.Tag.CLOSE);
				}
				xml.writeTag("measurements", XmlWriter.Tag.CLOSE);
			}
			
			if (header.getAnnotations() != null)
				writeAnnotations(header.getAnnotations().values());
			
			xml.writeTag("header", XmlWriter.Tag.CLOSE);
		}
		
		xml.writeBreak();
		xml.writeTag("peaks", XmlWriter.Tag.OPEN);
//		xml.flush();
	}
	
	protected void finalize()
	{
		// make sure this writer is properly closed
		try {
			close();
		} catch (IOException e) { ; }
	}
	
	protected void close() throws IOException
	{
		if (xml == null)
			return;
		
		xml.writeTag("peaks", XmlWriter.Tag.CLOSE);
		xml.writeTag("peakml", XmlWriter.Tag.CLOSE);
		xml.flush();
		xml.close();
	}
	
	protected void write(IPeak peak, boolean sha1) throws IOException
	{
		if (peak.getScanID() != -1)
			xml.writeElement("scan", Integer.toString(peak.getScanID()));
		if (peak.getRetentionTime() != -1)
			xml.writeElement("retentiontime", Double.toString(peak.getRetentionTime()));
		xml.writeElement("mass", Double.toString(peak.getMass()));
		xml.writeElement("intensity", Double.toString(peak.getIntensity()));
		if (peak.getPatternID() != -1)
			xml.writeElement("patternid", Integer.toString(peak.getPatternID()));
		if (peak.getMeasurementID() != -1)
			xml.writeElement("measurementid", Integer.toString(peak.getMeasurementID()));
		
		// for top-level ipeaks we write the sha1-hash
		if (sha1 == true)
			xml.writeElement("sha1sum", peak.sha1());
		
		if (peak.getAnnotations() != null)
			writeAnnotations(peak.getAnnotations().values());
	}
	
	protected void write(Peak peak, boolean sha1) throws IOException
	{
		if (peak.getClass().equals(Centroid.class))
			write((Centroid) peak, sha1);
//		else if (peak.getClass().equals(Profile.class))
//			write((Profile) peak, sha1);
	}
	
	protected void write(Centroid centroid, boolean sha1) throws IOException
	{
		if (xml == null)
			throw new IOException("Writer already closed.");
		
		xml.writeTag("peak", XmlWriter.Tag.OPEN, new XmlAttribute(TYPE, TYPE_CENTROID));
		write((IPeak) centroid, sha1);
		xml.writeTag("peak", XmlWriter.Tag.CLOSE);
//		xml.flush();
	}
	
	protected void write(MassChromatogram<? extends Peak> masschromatogram, boolean sha1) throws IOException
	{
		if (xml == null)
			throw new IOException("Writer already closed.");
		
		xml.writeTag("peak", XmlWriter.Tag.OPEN, new XmlAttribute(TYPE, TYPE_MASSCHROMATOGRAM));
		
		// Write the information defined in IPeak...
		write((IPeak) masschromatogram, sha1);
		// Write the information stored in the PeakData...
		writePeakData(masschromatogram.getPeakData());
		
		xml.writeTag("peak", XmlWriter.Tag.CLOSE);
//		xml.flush();
	}
	
	protected void write(BackgroundIon<? extends Peak> backgroundion, boolean sha1) throws IOException
	{
		if (xml == null)
			throw new IOException("Writer already closed.");
		
		xml.writeTag("peak", XmlWriter.Tag.OPEN, new XmlAttribute(TYPE, TYPE_BACKGROUNDION));
		
		// Write the information defined in IPeak...
		write((IPeak) backgroundion, sha1);
		// Write the information stored in the PeakData...
		writePeakData(backgroundion.getPeakData());
		
		xml.writeTag("peak", XmlWriter.Tag.CLOSE);
//		xml.flush();
	}
	
	@SuppressWarnings("unchecked")
	protected void write(IPeakSet<? extends IPeak> peakset, boolean sha1) throws IOException
	{
		if (xml == null)
			throw new IOException("Writer already closed.");
		
		xml.writeTag("peak", XmlWriter.Tag.OPEN, new XmlAttribute(TYPE, TYPE_PEAKSET));
		write((IPeak) peakset, sha1);
		xml.writeTag("peaks", XmlWriter.Tag.OPEN);
		for (int i=0; i<peakset.size(); ++i)
		{
			IPeak peak = peakset.get(i);
			Class<? extends IPeak> c = peak.getClass();
			
			if (c.equals(MassChromatogram.class))
				write((MassChromatogram) peak, false);
			else if (c.equals(BackgroundIon.class))
				write((BackgroundIon) peak, false);
			else if (c.equals(IPeakSet.class))
				write((IPeakSet) peak, false);
			else
			{
				xml.writeTag("peak", XmlWriter.Tag.OPEN);
				write(peak, false);
				xml.writeTag("peak", XmlWriter.Tag.CLOSE);
			}
		}
		xml.writeTag("peaks", XmlWriter.Tag.CLOSE);
		xml.writeTag("peak", XmlWriter.Tag.CLOSE);
//		xml.flush();
	}
	
	protected void writePeakData(PeakData<? extends Peak> peakdata) throws IOException
	{
		PeakData.PeakFactory<? extends Peak> factory = peakdata.getFactory();
		
		xml.writeTag("peakdata", XmlWriter.Tag.OPEN,
				new XmlAttribute("type", factory.getName()),
				new XmlAttribute("size", Integer.toString(peakdata.size()))
			);
		xml.writeElement("scanids",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getScanIDs(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeElement("retentiontimes",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getRetentionTimes(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeElement("masses",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getMasses(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeElement("intensities",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getIntensities(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeElement("relativeintensities",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getIntensities(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeElement("patternids",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getPatternIDs(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeElement("measurementids",
				Base64.encodeBytes(ByteArray.toByteArray(peakdata.getMeasurementIDs(), ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES));
		xml.writeTag("peakdata", XmlWriter.Tag.CLOSE);
	}
	
	protected void writeAnnotations(Collection<peakml.Annotation> annotations) throws IOException
	{
		if (annotations!=null && annotations.size()!=0)
		{
			xml.writeTag("annotations", XmlWriter.Tag.OPEN);
			for (peakml.Annotation annotation : annotations)
			{
				Vector<XmlAttribute> attributes = new Vector<XmlAttribute>();
				if (annotation.getUnit() != null)
					attributes.add(new XmlAttribute("unit", annotation.getUnit()));
				if (annotation.getValueType() == peakml.Annotation.ValueType.ONTOLOGY)
					attributes.add(new XmlAttribute("ontologyref", annotation.getOntologyRef()));
				
				if (attributes.size() != 0)
					xml.writeTag("annotation", XmlWriter.Tag.OPEN, attributes);
				else
					xml.writeTag("annotation", XmlWriter.Tag.OPEN);
				
				xml.writeElement("label", annotation.getLabel());
				xml.writeElement("value", annotation.getValue());
				xml.writeElement("valuetype", annotation.getValueType().toString());
				
				xml.writeTag("annotation", XmlWriter.Tag.CLOSE);
			}
			xml.writeTag("annotations", XmlWriter.Tag.CLOSE);
		}
	}
	
	protected void writeSet(SetInfo set) throws IOException
	{
		int measurementids[] = new int[set.getNrMeasurementIDs()];
		for (int i=0; i<set.getNrMeasurementIDs(); ++i)
			measurementids[i] = set.getMeasurementID(i);
		
		xml.writeTag("set", XmlWriter.Tag.OPEN);
		xml.writeElement("id", set.getID());
		xml.writeElement("type", Integer.toString(set.getType()));
		xml.writeElement("measurementids",
				Base64.encodeBytes(ByteArray.toByteArray(measurementids, ByteArray.ENDIAN_LITTLE, 32), Base64.DONT_BREAK_LINES)
			);
		for (SetInfo s : set.getChildren())
			writeSet(s);
		xml.writeTag("set", XmlWriter.Tag.CLOSE);
	}
	
	
	// data
	protected XmlWriter xml;
	
	protected static final String TYPE						= "type";
	protected static final String SIZE						= "size";
	protected static final String TYPE_CENTROID				= "centroid";
	protected static final String TYPE_PROFILE				= "profile";
	protected static final String TYPE_PEAKSET				= "peakset";
	protected static final String TYPE_BACKGROUNDION		= "backgroundion";
	protected static final String TYPE_MASSCHROMATOGRAM		= "masschromatogram";
	
	protected static final DateFormat dateformat = DateFormat.getDateInstance(DateFormat.LONG, new Locale("en","US"));
}