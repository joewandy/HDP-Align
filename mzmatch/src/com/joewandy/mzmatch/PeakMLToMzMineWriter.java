// Copyright GBiC (http://gbic.biol.rug.nl), 2007

package com.joewandy.mzmatch;

// java
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Locale;
import java.util.Vector;

import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.Peak;
import peakml.PeakData;
import peakml.io.Header;
import peakml.io.WriterProgressListener;
import com.Ostermiller.util.Base64;
import peakml.util.ByteArray;
import domsax.XmlAttribute;
import domsax.XmlWriter;

// This probably should extend from PeakMLWriter ... ?
public class PeakMLToMzMineWriter {
	
	protected static final String TYPE = "type";
	protected static final String TYPE_PEAKSET = "peakset";
	protected static final String TYPE_MASSCHROMATOGRAM = "masschromatogram";
	protected static final DateFormat dateformat = DateFormat.getDateInstance(
			DateFormat.LONG, new Locale("en", "US"));

	/**
	 * Generic method for writing a set of {@link IPeak} elements (the real
	 * class unknown). The method will find out for each element its real type
	 * and write the data accordingly.
	 * 
	 * @param header
	 *            The header to be written to the file.
	 * @param peaks
	 *            The peaks to be written to the file.
	 * @param listener
	 *            The listener for tracking the progress of the write-process.
	 * @param out
	 *            The output-stream to write to.
	 * @param stylesheet
	 *            The stylesheet to include (filename, can be null).
	 * @throws IOException
	 *             Thrown when an I/O error is encountered.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void write(String prefix, Header header, Vector<? extends IPeak> peaks,
			WriterProgressListener listener, OutputStream out, String stylesheet)
			throws IOException {
		
		if (header != null) {
			header.setNrPeaks(peaks.size());
		}

		int index = 0;
		PeakMLToMzMineWriter writer = new PeakMLToMzMineWriter(prefix, header, out, stylesheet);
		for (IPeak peak : peaks) {
			
			if (listener != null) {
				listener.update((100. * index++) / peaks.size());
			}

			Class<? extends IPeak> c = peak.getClass();
			if (c.equals(MassChromatogram.class)) {
				writer.write((MassChromatogram) peak);
			} else if (c.equals(IPeakSet.class)) {
				writer.write((IPeakSet<? extends IPeak>) peak);
			} else {
				throw new IOException("Unknown IPeak-type: " + c.getName());
			}
		}
		writer.close();
		
	}

	// data
	protected XmlWriter xml;
	private int rowCounter;
	private String prefix;
	
	// implementation of the write functions
	protected PeakMLToMzMineWriter(String prefix, Header header, OutputStream out) throws IOException {
		this(prefix, header, out, null);
	}

	protected PeakMLToMzMineWriter(String prefix, Header header, OutputStream out, String stylesheet)
			throws IOException {
		
		this.prefix = prefix;
		
		xml = new XmlWriter(new BufferedOutputStream(out, 1024 * 1024),
				stylesheet);

		// write the opening peaklist tag
		xml.writeTag("peaklist", XmlWriter.Tag.OPEN);

		// write header - pl_name: mpl name to display on-screen
		xml.writeElement("pl_name", prefix + ".mpl");
		
		// write header - created
		xml.writeElement("created", dateformat.format(header.getDate()));
		
		// write header - quantity: no of peaks
		xml.writeElement("quantity", Integer.toString(header.getNrPeaks()));
		
		// write header - raw_file
		xml.writeElement("raw_file", prefix + ".mzXML");
		
	}

	protected void close() throws IOException {		
		if (xml == null) {
			return;
		}
		xml.writeTag("peaklist", XmlWriter.Tag.CLOSE);
		xml.flush();
		xml.close();
	}

	@Override
	protected void finalize() {
		// make sure this writer is properly closed
		try {
			close();
		} catch (IOException e) {
			;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void write(IPeakSet<? extends IPeak> peakset)
			throws IOException {
		
		if (xml == null) {
			throw new IOException("Writer already closed.");
		}
		
		for (int i = 0; i < peakset.size(); ++i) {
			
			IPeak peak = peakset.get(i);
			Class<? extends IPeak> c = peak.getClass();

			if (c.equals(MassChromatogram.class)) {
				write((MassChromatogram) peak);
			} else if (c.equals(IPeakSet.class)) {
				// recursively calls itself if encounter peakset in peakset
				write((IPeakSet) peak);
			}
			
		}
		
	}
	
	protected void write(MassChromatogram<? extends Peak> masschromatogram) throws IOException {
		
		if (xml == null) {
			throw new IOException("Writer already closed.");
		}

		this.rowCounter++;
		xml.writeTag("row", XmlWriter.Tag.OPEN, new XmlAttribute("id", String.valueOf(this.rowCounter)));

		// Write the information defined in IPeak...
		writeHeader(masschromatogram);
		
		// Write the information stored in the PeakData...
		writePeakData(masschromatogram.getPeakData());

		xml.writeTag("row", XmlWriter.Tag.CLOSE);
		
	}

	protected void writeHeader(MassChromatogram<? extends Peak> masschromatogram) throws IOException {

		double mass = masschromatogram.getMass();
		double rt = masschromatogram.getRetentionTime();
		double intensity = masschromatogram.getIntensity();
		int bestScan = masschromatogram.getScanID();

		xml.writeTag("peak", XmlWriter.Tag.OPEN, 
				new XmlAttribute("column_id", this.prefix + ".mzXML"),
				new XmlAttribute("mz", Double.toString(mass)),
				new XmlAttribute("rt", Double.toString(rt)),
				new XmlAttribute("height", Double.toString(intensity)),
				new XmlAttribute("status", "MANUAL"),
				new XmlAttribute("charge", "0")
		);
		xml.writeElement("best_scan", String.valueOf(bestScan));
		
	}
	
	protected void writePeakData(PeakData<? extends Peak> peakdata)
			throws IOException {
	
		xml.writeTag("mzpeaks", XmlWriter.Tag.OPEN, new XmlAttribute("quantity", Integer.toString(peakdata.size())));

		ByteArrayOutputStream byteScanStream = new ByteArrayOutputStream();
		DataOutputStream dataScanStream = new DataOutputStream(byteScanStream);

		ByteArrayOutputStream byteMassStream = new ByteArrayOutputStream();
		DataOutputStream dataMassStream = new DataOutputStream(byteMassStream);

		ByteArrayOutputStream byteHeightStream = new ByteArrayOutputStream();
		DataOutputStream dataHeightStream = new DataOutputStream(
			byteHeightStream);
		
		int[] scanNumbers = peakdata.getScanIDs();
		double[] masses = peakdata.getMasses();
		double[] intenses = peakdata.getIntensities();
		for (int i = 0; i < scanNumbers.length; i++) {			
			int scan = scanNumbers[i];
			float mass = (float) masses[i]; // losing precision here ??!?!?!
			float height = (float) intenses[i];
			dataScanStream.writeInt(scan);
		    dataScanStream.flush();
		    dataMassStream.writeFloat(mass);
		    dataMassStream.flush();
		    dataHeightStream.writeFloat(height);
		    dataHeightStream.flush();
		}
		
		byte[] bytes = Base64.encode(byteScanStream.toByteArray());
		String sbytes = new String(bytes);
		xml.writeElement("scan_id", sbytes);

		bytes = Base64.encode(byteMassStream.toByteArray());
		sbytes = new String(bytes);
		xml.writeElement("mz", sbytes);

		bytes = Base64.encode(byteHeightStream.toByteArray());
		sbytes = new String(bytes);
		xml.writeElement("height", sbytes);

		xml.writeTag("mzpeaks", XmlWriter.Tag.CLOSE);
		xml.writeTag("peak", XmlWriter.Tag.CLOSE);
		
	}

}
