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

// libraries
import domsax.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.math.*;
import peakml.util.*;
import peakml.chemistry.*;





/**
 * 
 */
public class MzXmlWriter
{
	// static interface
	/**
	 * 
	 * @param out
	 * @param header
	 * @param ms
	 * @throws IOException
	 */
	public static void write(OutputStream out, Header header, ChromatographyMS<Centroid> cms) throws IOException
	{
		MeasurementInfo measurement = header.getMeasurementInfos().firstElement();
		if (measurement == null)
			throw new RuntimeException("The header contains no MeasurementInfo");
		Vocabulary vocabulary = Vocabulary.getVocabulary("psi-ms.obo");
		
		// create the writer
		XmlWriter xml = new XmlWriter(new OutputStreamWriter(out), XmlWriter.Encoding.ISO_8859_1);
		
		// open the mzXML
		xml.writeTag("mzXML", XmlWriter.Tag.OPEN,
				new XmlAttribute("xmlns", "http://sashimi.sourceforge.net/schema_revision/mzXML_3.1"),
				new XmlAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance"),
				new XmlAttribute("xsi:schemaLocation", "http://sashimi.sourceforge.net/schema_revision/mzXML_3.1 http://sashimi.sourceforge.net/schema_revision/mzXML_3.1/mzXML_idx_3.1.xsd")
			);
		
		// open the msRun
		xml.writeTag("msRun", XmlWriter.Tag.OPEN,
				new XmlAttribute("scanCount", Integer.toString(cms.getNrScans())),
				new XmlAttribute("startTime", "PT" + cms.getStartTime() + "S"),
				new XmlAttribute("endTime", "PT" + cms.getEndTime() + "S")
			);
		
		// write the parent files
		for (FileInfo fileinfo : measurement.getFileInfos())
		{
			xml.writeTag("parentFile", XmlWriter.Tag.SINGLE,
					new XmlAttribute("fileName", fileinfo.getLocation() + "/" + fileinfo.getName()),
					new XmlAttribute("fileType", "RAWData"),
					new XmlAttribute("fileSha1", "")
				);
		}
		
		// write the instrument data
		Vocabulary.Term term_thermofinnigan = vocabulary.getTerm("MS:1000125");
		Vocabulary.Term term_thermoscientific = vocabulary.getTerm("MS:1000494");
		for (Annotation annotation : measurement.getAnnotations().values())
		{
			Vocabulary.Term term = vocabulary.getTerm(annotation.getLabel());
			
			// the manufacturer
			boolean modelterm = false;
			if (vocabulary.isTermChildOf(term, term_thermoscientific))
			{
				modelterm = true;
				xml.writeTag("msManufacturer", XmlWriter.Tag.SINGLE,
						new XmlAttribute("category", "msManufacturer"),
						new XmlAttribute("value", "Thermo Scientific")
					);
			}
			else if (vocabulary.isTermChildOf(term, term_thermofinnigan))
			{
				modelterm = true;
				xml.writeTag("msManufacturer", XmlWriter.Tag.SINGLE,
						new XmlAttribute("category", "msManufacturer"),
						new XmlAttribute("value", "Thermo Finnigan")
					);
			}
			
			// model
			if (modelterm == true)
			{
				xml.writeTag("msModel", XmlWriter.Tag.SINGLE,
						new XmlAttribute("category", "msModel"),
						new XmlAttribute("value", term.name)
					);
			}
		}
		
		// write the dataProcessing
		xml.writeTag("dataProcessing", XmlWriter.Tag.OPEN);
		for (ApplicationInfo applicationinfo : header.getApplicationInfos())
			xml.writeTag("software", XmlWriter.Tag.SINGLE,
					new XmlAttribute("type", "processing"),	// we cannot distinguish between this and conversion
					new XmlAttribute("name", applicationinfo.getName()),
					new XmlAttribute("version", applicationinfo.getVersion())
				);
		xml.writeTag("dataProcessing", XmlWriter.Tag.CLOSE);
		
		// gimme some air
		xml.writeBreak();
		
		// write the scans
		for (Spectrum<Centroid> scan : cms.getScans())
		{
			// TODO include ms>1
			Polarity polarity = scan.getPolarity();
			double proton = (polarity==Polarity.NEGATIVE ? PeriodicTable.proton : (polarity==Polarity.POSITIVE ? -PeriodicTable.proton : 0));
			PeakData<Centroid> peakdata = scan.getPeakData();
			
			int indx_maxintensity = Statistical.indexOfMax(peakdata.getIntensities());
			double massstats[] = Statistical.stats(peakdata.getMasses());
			
			double array[] = new double[2 * scan.getNrPeaks()];
			for (int i=0; i<scan.getNrPeaks(); ++i)
			{
				array[i*2] = peakdata.getMass(i) - proton; // compensate the Spectrum behaviour
				array[i*2+1] = peakdata.getIntensity(i);
			}
			
			// open the scan
			xml.writeTag("scan", XmlWriter.Tag.OPEN,
					new XmlAttribute("num", Integer.toString(scan.getScanID()+1)),
					new XmlAttribute("msLevel", "1"),
					new XmlAttribute("peaksCount", Integer.toString(scan.getNrPeaks())),
					new XmlAttribute("polarity", polarity==Polarity.POSITIVE ? "+" : (polarity==Polarity.NEGATIVE ? "-" : "any")),
					new XmlAttribute("scanType", "Full"),
//					new XmlAttribute("filterLine", "FTMS + c ESI Full ms [50.00-1200.00]"),
					new XmlAttribute("retentionTime", "PT" + scan.getRetentionTime() + "S"),
//					new XmlAttribute("startMz", ""),
//					new XmlAttribute("endMz", ""),
					new XmlAttribute("lowMz", Double.toString(massstats[Statistical.MINIMUM] - proton)),
					new XmlAttribute("highMz", Double.toString(massstats[Statistical.MAXIMUM] - proton)),
					new XmlAttribute("basePeakMz", indx_maxintensity==-1?"0":Double.toString(peakdata.getMass(indx_maxintensity) - proton)),
					new XmlAttribute("basePeakIntensity", indx_maxintensity==-1?"0":Double.toString(peakdata.getIntensity(indx_maxintensity))),
					new XmlAttribute("totIonCurrent", Double.toString(Statistical.sum(peakdata.getIntensities())))
				);
			
			// dump the precursorMz for ms>1 spectra
			
			// dump the peaks
			xml.writeElement(
					"peaks",
					Base64.encodeBytes(ByteArray.toByteArray(array, ByteArray.ENDIAN_BIG, 32), Base64.DONT_BREAK_LINES),
					new XmlAttribute("precision", "32"),
					new XmlAttribute("byteOrder", "network"),
					new XmlAttribute("pairOrder", "m/z-int")
				);
			
			// close the scan
			xml.writeTag("scan", XmlWriter.Tag.CLOSE);
		}
		
		// close the msRun
		xml.writeTag("msRun", XmlWriter.Tag.CLOSE);
		
		// close the mzML
		xml.writeTag("mzXML", XmlWriter.Tag.CLOSE);
		
		// close
		xml.flush();
		xml.close();
	}
}
