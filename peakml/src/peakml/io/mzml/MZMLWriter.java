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

// libraries
import domsax.*;

// peakML
import peakml.*;
import peakml.io.*;
import peakml.math.*;
import peakml.util.*;
import peakml.chemistry.*;






/**
 * 
 */
public abstract class MZMLWriter
{
	// static write interface
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
		
		// create the writer
		XmlWriter xml = new XmlWriter(new OutputStreamWriter(out), XmlWriter.Encoding.ISO_8859_1);
		
		// open mzML
		xml.writeTag("mzML", XmlWriter.Tag.OPEN,
				new XmlAttribute("version", "1.1.0")
			);
		
		// write the fileDescription
		xml.writeTag("fileDescription", XmlWriter.Tag.OPEN);
		xml.writeTag("fileContent", XmlWriter.Tag.OPEN);
		cvParam(xml, "MS", "MS:1000580", "MSn spectrum", "");
		xml.writeTag("fileContent", XmlWriter.Tag.CLOSE);
		xml.writeTag("fileDescription", XmlWriter.Tag.CLOSE);
		
		// write the sourceFileList
		xml.writeTag("sourceFileList", XmlWriter.Tag.OPEN,
				new XmlAttribute("count", Integer.toString(measurement.getNrFileInfos()))
			);
		for (FileInfo fileinfo : measurement.getFileInfos())
		{
			xml.writeTag("sourceFile", XmlWriter.Tag.OPEN,
					new XmlAttribute("id", fileinfo.getLabel()),
					new XmlAttribute("name", fileinfo.getName()),
					new XmlAttribute("location", fileinfo.getLocation())
				);
			// TODO write information about the machine model
			xml.writeTag("sourceFile", XmlWriter.Tag.CLOSE);
		}
		xml.writeTag("sourceFileList", XmlWriter.Tag.CLOSE);
		
		// write the softwareList
		xml.writeTag("softwareList", XmlWriter.Tag.OPEN,
				new XmlAttribute("count", Integer.toString(header.getNrApplicationInfos()))
			);
		for (ApplicationInfo application : header.getApplicationInfos())
		{
			xml.writeTag("softwareList", XmlWriter.Tag.OPEN,
					new XmlAttribute("id", application.getName()),
					new XmlAttribute("version", application.getVersion())
				);
			xml.writeTag("softwareList", XmlWriter.Tag.CLOSE);
		}
		xml.writeTag("softwareList", XmlWriter.Tag.CLOSE);
		
		// gimme some air
		xml.writeBreak();
		
		// write the run
		xml.writeTag("run", XmlWriter.Tag.OPEN,
				new XmlAttribute("id", "0"),
				new XmlAttribute("defaultInstrumentConfigurationRef", "0")
			);
		xml.writeTag("spectrumList", XmlWriter.Tag.OPEN,
				new XmlAttribute("count", Integer.toString(cms.getNrScans())),
				new XmlAttribute("defaultDataProcessingRef", "0")
			);
		// TODO export the ms>1 spectra too!
		for (Spectrum<Centroid> spectrum : cms)
		{
			Polarity polarity = spectrum.getPolarity();
			double proton = (polarity==Polarity.NEGATIVE ? PeriodicTable.proton : (polarity==Polarity.POSITIVE ? -PeriodicTable.proton : 0));
			
			double masses[] = spectrum.getPeakData().getMasses();
			// compensate the Spectrum behaviour
			for (int i=0; i<masses.length; ++i)
				masses[i] -= proton;
			byte massdata[] = ByteArray.toByteArray(masses, ByteArray.ENDIAN_LITTLE, 64);
			double intensities[] = spectrum.getPeakData().getIntensities();
			byte intensitydata[] = ByteArray.toByteArray(intensities, ByteArray.ENDIAN_LITTLE, 64);
			
			double stats_masses[] = Statistical.stats(masses);
			
			int indx_maxintensity = Statistical.indexOfMax(intensities);
			
			// open the spectrum and its properties
			xml.writeTag("spectrum", XmlWriter.Tag.OPEN,
					new XmlAttribute("index", Integer.toString(spectrum.getScanID())),
					new XmlAttribute("id", Integer.toString(spectrum.getScanID())),
					new XmlAttribute("defaultArrayLength", Integer.toString(spectrum.getNrPeaks()))
				);
			cvParam(xml, "MS", "MS:1000511", "ms level", "1");
			cvParam(xml, "MS", "MS:1000528", "lowest observed m/z", Double.toString(stats_masses[Statistical.MINIMUM]));
			cvParam(xml, "MS", "MS:1000527", "highest observed m/z", Double.toString(stats_masses[Statistical.MAXIMUM]));
			cvParam(xml, "MS", "MS:1000504", "base peak m/z", Double.toString(masses[indx_maxintensity]));
			cvParam(xml, "MS", "MS:1000505", "base peak intensity", Double.toString(intensities[indx_maxintensity]));
			cvParam(xml, "MS", "MS:1000285", "total ion current", Double.toString(Statistical.sum(intensities)));
			if (spectrum.getPolarity() == Polarity.POSITIVE)
				cvParam(xml, "MS", "MS:1000130", "positive scan", "");
			else if (spectrum.getPolarity() == Polarity.NEGATIVE)
				cvParam(xml, "MS", "MS:1000129", "negative scan", "");
			xml.writeTag("binaryDataArrayList", XmlWriter.Tag.OPEN,
					new XmlAttribute("count", "2")
				);
			
			// scaninfo
			xml.writeTag("scanList", XmlWriter.Tag.OPEN,
					new XmlAttribute("count", "1")
				);
			xml.writeTag("scan", XmlWriter.Tag.OPEN);
			cvParam(xml, "MS", "MS:1000016", "scan start time", Double.toString(spectrum.getRetentionTime()));
			xml.writeTag("scan", XmlWriter.Tag.CLOSE);
			xml.writeTag("scanList", XmlWriter.Tag.CLOSE);
			
			// masses
			xml.writeTag("binaryDataArray", XmlWriter.Tag.OPEN,
					new XmlAttribute("encodedLength", Integer.toString(massdata.length))
				);
			cvParam(xml, "MS", "MS:1000523", "64-bit float", "");
			cvParam(xml, "MS", "MS:1000576", "no compression", "");
			cvParam(xml, "MS", "MS:1000514", "m/z array", "");
			xml.writeElement("binary", Base64.encodeBytes(massdata, Base64.DONT_BREAK_LINES));
			xml.writeTag("binaryDataArray", XmlWriter.Tag.CLOSE);
			
			// intensities
			xml.writeTag("binaryDataArray", XmlWriter.Tag.OPEN,
					new XmlAttribute("encodedLength", Integer.toString(intensitydata.length))
				);
			cvParam(xml, "MS", "MS:1000523", "64-bit float", "");
			cvParam(xml, "MS", "MS:1000576", "no compression", "");
			cvParam(xml, "MS", "MS:1000515", "intensity array", "");
			xml.writeElement("binary", Base64.encodeBytes(intensitydata, Base64.DONT_BREAK_LINES));
			xml.writeTag("binaryDataArray", XmlWriter.Tag.CLOSE);
			
			// close the spectrum and its properties
			xml.writeTag("binaryDataArrayList", XmlWriter.Tag.CLOSE);
			xml.writeTag("spectrum", XmlWriter.Tag.CLOSE);
			
			// decompensate for Spectrum's behaviour
			for (int i=0; i<masses.length; ++i)
				masses[i] += proton;
		}
		xml.writeTag("spectrumList", XmlWriter.Tag.CLOSE);
		xml.writeTag("run", XmlWriter.Tag.CLOSE);
		
		// close mzML
		xml.writeTag("mzML", XmlWriter.Tag.CLOSE);
		
		// close off
		xml.flush();
		xml.close();
	}
	
	
	// static helpers
	private static void cvParam(XmlWriter xml, String cvRef, String accession, String name, String value) throws IOException
	{
		xml.writeTag("cvParam", XmlWriter.Tag.SINGLE,
				new XmlAttribute("cvRef", cvRef),
				new XmlAttribute("accession", accession),
				new XmlAttribute("name", name),
				new XmlAttribute("value", value)
			);
	}
}
