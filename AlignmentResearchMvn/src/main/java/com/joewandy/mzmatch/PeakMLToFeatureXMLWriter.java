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
public class PeakMLToFeatureXMLWriter {
	
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
		PeakMLToFeatureXMLWriter writer = new PeakMLToFeatureXMLWriter(header.getNrPeaks(), out, stylesheet);
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
	
	protected PeakMLToFeatureXMLWriter(int noOfPeaks, OutputStream out, String stylesheet)
			throws IOException {
				
		xml = new XmlWriter(new BufferedOutputStream(out, 1024 * 1024),
				stylesheet);
		
		// write root element: featureMap
		xml.writeTag("featureMap", XmlWriter.Tag.OPEN);
		
		// write the opening featureList tag
		xml.writeTag("featureList", XmlWriter.Tag.OPEN, new XmlAttribute("count", String.valueOf(noOfPeaks)));		
		
	}

	protected void close() throws IOException {		
		if (xml == null) {
			return;
		}
		xml.writeTag("featureList", XmlWriter.Tag.CLOSE);
		xml.writeTag("featureMap", XmlWriter.Tag.CLOSE);
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
		xml.writeTag("feature", XmlWriter.Tag.OPEN, new XmlAttribute("id", String.valueOf(this.rowCounter)));

		// Write the information defined in IPeak...
		writeFeature(masschromatogram);
		
		xml.writeTag("feature", XmlWriter.Tag.CLOSE);
		
	}

	protected void writeFeature(MassChromatogram<? extends Peak> masschromatogram) throws IOException {

		double mass = masschromatogram.getMass();
		double rt = masschromatogram.getRetentionTime();
		double intensity = masschromatogram.getIntensity();

		xml.writeElement("position", String.valueOf(rt), new XmlAttribute("dim", "0"));
		xml.writeElement("position", String.valueOf(mass), new XmlAttribute("dim", "1"));
		xml.writeElement("intensity", String.valueOf(intensity));
		
	}
	
}
