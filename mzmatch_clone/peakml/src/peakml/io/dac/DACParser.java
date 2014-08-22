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



package peakml.io.dac;


// java
import java.io.*;

// peakml
import peakml.*;
import peakml.io.*;
import peakml.chemistry.*;





/**
 * 
 */
public abstract class DACParser
{
	/**
	 * 
	 * 
	 * @param filename
	 * @return
	 * @throws IXRawfileException
	 */
	public static ParseResult parse(String filename) throws IOException
	{
		DAC.init();
		
		// create the storage-space for the meta-information
		Header header = new Header();
		ChromatographyMS<Centroid> cms = new ChromatographyMS<Centroid>();
		MeasurementInfo measurement = new MeasurementInfo(0, "");
		
		header.addMeasurementInfo(measurement);
		
		// open the file connection
		DACFunctionInfo dac_function = new DACFunctionInfo();
		int nrfunctions = DACFunctionInfo.getNumberOfFunctions(filename);
		if (nrfunctions == 0)
			throw new IOException("DAC-file has no functions defined");
		dac_function.open(filename, 1); // we take the first function for now
		
//		DACExperimentInfo dac_expinfo = new DACExperimentInfo();
//		dac_expinfo.open(filename);
//		DACExperimentInfo.ExFunctionInfo dac_exfuncinfo = dac_expinfo.getExFunctionInfo(0);
		
		// retrieve the data
		int nrscans = dac_function.getNumScans();
		for (int scannumber=1; scannumber<=nrscans; ++scannumber)
		{
			DACSpectrum dac_spectrum = new DACSpectrum();
			dac_spectrum.open(filename, 1, 0, scannumber);
			
			DACScanStats dac_scanstats = new DACScanStats();
			dac_scanstats.open(filename, 1, 0, scannumber);
			
			// retrieve the correct retention time
			double retentiontime = dac_scanstats.getRetnTime();
			
			// determine the polarity
			Polarity polarity = Polarity.NEUTRAL;//dac_exfuncinfo.getPolarity();
			
			// retrieve the centroided mass list
			double masses[] = dac_spectrum.getMasses();
			double intensities[] = dac_spectrum.getIntensities();
			
			// copy the data
			PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, masses.length);
			for (int i=0; i<masses.length; i++)
				peakdata.set(i, scannumber-1, retentiontime, masses[i], intensities[i]);
			
			Spectrum<Centroid> spectrum = new Spectrum<Centroid>(peakdata);
			spectrum.setRetentionTime(retentiontime);
			cms.getScans().add(spectrum);
			measurement.addScanInfo(new ScanInfo(retentiontime, polarity));
		}
		
		// return the result
		return new ParseResult(header, cms);
	}
}
