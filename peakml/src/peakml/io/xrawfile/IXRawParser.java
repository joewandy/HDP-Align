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



package peakml.io.xrawfile;


// java

// peakml
import peakml.*;
import peakml.io.*;
import peakml.chemistry.*;





/**
 * 
 */
public abstract class IXRawParser
{
	/**
	 * 
	 * 
	 * @param filename
	 * @return
	 * @throws IXRawfileException
	 */
	public static ParseResult parse(String filename) throws IXRawfileException
	{
		// create the storage-space for the meta-information
		Header header = new Header();
		ChromatographyMS<Centroid> cms = new ChromatographyMS<Centroid>();
		MeasurementInfo measurement = new MeasurementInfo(0, "");
		
		header.addMeasurementInfo(measurement);
		
		// open the file connection
		IXRawfile rawfile = new IXRawfile();
		int rtcode = rawfile.init();
		if (rtcode != IXRawfile.RTCODE_SUCCESS)
			throw new IXRawfileException(IXRawfile.RTCODE_FAILED, "Could not connect to the XRawfile OLE-service.");
		rawfile.open(filename);
		
		// we're only interested in MS data
		int nrmscontrollers = rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_MS);
		if (nrmscontrollers == 0)
			throw new IXRawfileException(IXRawfile.RTCODE_NO_DATA_PRESENT, "No mass spectrometry data present in '" + filename + "'");
		
		// TODO: extract all MS data
		rawfile.setCurrentController(IXRawfile.CONTROLLER_MS, 1);
		
		// retrieve the data
		int minscanid = rawfile.getFirstSpectrumNumber();
		int maxscanid = rawfile.getLastSpectrumNumber();
		for (int scannumber=minscanid; scannumber<maxscanid; ++scannumber)
		{
			// retrieve the correct retention time
			double retentiontime = rawfile.rtFromScanNum(scannumber);
			
			// determine the polarity
			String filter = rawfile.getFilterForScanNum(scannumber);
			Polarity polarity = filter.contains("+") ? Polarity.POSITIVE : Polarity.NEGATIVE;
			
			// retrieve the centroided mass list
			double masslist[] = rawfile.getMassListFromScanNum(scannumber, true);
			
			// copy the data
			PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, masslist.length/2);
			for (int i=0; i<masslist.length/2; i++)
				peakdata.set(i, scannumber-minscanid, retentiontime, masslist[2*i + 0], masslist[2*i + 1]);
			
			Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata);
			spectrum.setRetentionTime(retentiontime);
			cms.getScans().add(spectrum);
			measurement.addScanInfo(new ScanInfo(retentiontime, polarity));
		}
		
		// close the file connection
		rawfile.dispose();
		
		// return the result
		return new ParseResult(header, cms);
	}
}
