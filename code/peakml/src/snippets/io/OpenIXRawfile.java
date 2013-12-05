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



package snippets.io;

// java
import java.util.*;

// peakml
import peakml.math.*;
import peakml.io.xrawfile.*;



public class OpenIXRawfile
{
	public static void main(String args[])
	{
		try
		{
			IXRawfile raw = new IXRawfile();
			raw.init();
			raw.open("F:/data/leishmania-longterm-stability_2009-12-12/quality_ctrl_tech/RAW/2009_11_24_AD70_W0_1.RAW");
			raw.setCurrentController(IXRawfile.CONTROLLER_MS, 1);
			
			for(int i=1; i<=100000; i++)
			{
				System.out.println(i);
				raw.getLabelData(100);
			}
			System.exit(0);
			
			Vector<IXRawfile.Label> labels = raw.getLabelData(453);
			for (IXRawfile.Label label : labels)
				System.out.println(label);
			
			Vector<IXRawfile.Noise> noises = raw.getNoiseData(452);
			for (IXRawfile.Noise noise : noises)
				System.out.println(noise);
			
			
			System.out.println("Filename: " + raw.getFileName());
			System.out.println("Created by: " + raw.getCreatorID());
			System.out.println("File version: " + raw.getVersionNumber());
			System.out.println("Created: " + raw.getCreationDate());
			
			System.out.println("Number MS controllers:     " + raw.getNumberOfControllersOfType(IXRawfile.CONTROLLER_MS));
			System.out.println("Number Analog controllers: " + raw.getNumberOfControllersOfType(IXRawfile.CONTROLLER_ANALOG));
			System.out.println("Number A/D controllers:    " + raw.getNumberOfControllersOfType(IXRawfile.CONTROLLER_AD_CARD));
			System.out.println("Number PDA controllers:    " + raw.getNumberOfControllersOfType(IXRawfile.CONTROLLER_PDA));
			System.out.println("Number UV controllers:     " + raw.getNumberOfControllersOfType(IXRawfile.CONTROLLER_UV));
			
			System.out.println("Controllers:");
			for (int i=0; i<raw.getNumberOfControllers(); ++i)
				System.out.println("* " + raw.getControllerType(i));
			raw.setCurrentController(IXRawfile.CONTROLLER_MS, 1);
			IXRawfile.Controller current = raw.getCurrentController();
			System.out.println("Controller: " + current.getControllerNumber() + ", " + current.getControllerType());
			
			System.out.println("Number spectra: " + raw.getNumSpectra());
			System.out.println("Number status log: " + raw.getNumStatusLog());
			System.out.println("Number error log: " + raw.getNumErrorLog());
			System.out.println("Number tune data: " + raw.getNumTuneData());
			System.out.println("Number trailer extra: " + raw.getNumTrailerExtra());
			System.out.println("Mass resolution: " + raw.getMassResolution());
			System.out.println("Mass range: " + raw.getLowMass() + " - " + raw.getHighMass());
			System.out.println("Time range: " + raw.getStartTime() + " - " + raw.getEndTime());
			System.out.println("Max intensity: " + raw.getMaxIntensity());
			System.out.println("Spectrum numbers: " + raw.getFirstSpectrumNumber() + " - " + raw.getLastSpectrumNumber());
			System.out.println("Instrument ID: " + raw.getInstrumentID());
			System.out.println("Inlet ID: " + raw.getInletID());
			System.out.println("Instrument name: " + raw.getInstName());
			System.out.println("Instrument model: " + raw.getInstModel());
			System.out.println("Instrument s/n: " + raw.getInstSerialNumber());
			System.out.println("Instrument software version: " + raw.getInstSoftwareVersion());
			System.out.println("Instrument hardware version: " + raw.getInstHardwareVersion());
			System.out.println("Instrument flags: " + raw.getInstFlags());
			
			System.out.println("Number of channels: " + raw.getInstNumChannelLabels());
			for (int i=0; i<raw.getInstNumChannelLabels(); ++i)
				System.out.println(raw.getInstChannelLabel(i));
			
			System.out.println("Filters:");
			for (String filter : raw.getFilters())
				System.out.println("* " + filter);
			
			System.out.println("Scan number for RT 1: " + raw.scanNumFromRT(1));
			System.out.println("RT for Scan number 114: " + raw.rtFromScanNum(114));
			
			System.out.println("Filters for scans:");
			for (int i=1; i<5; ++i)
				System.out.println("* " + raw.getFilterForScanNum(i));
			
			System.out.println("Filters for retention times:");
			for (double rt : new double[] { 1,2,3,4 })
				System.out.println("* " + raw.getFilterForScanRT(rt));
			
			System.out.println("Scan data:");
			for (int i=1; i<10; ++i)
			{
				double massintensity[] = raw.getMassListFromScanNum(i, true);
				double stats[] = Statistical.stats(massintensity);
				System.out.println(
						stats[Statistical.MINIMUM]
						+ " - " +
						stats[Statistical.MAXIMUM]
						+ " - " +
						stats[Statistical.MEAN]
					);
			}
			
			raw.getPrecursorInfoFromScanNum(180);
			
			System.out.println("Is profile:  " + raw.isProfileScanForScanNum(180));
			System.out.println("Is centroid: " + raw.isCentroidScanForScanNum(180));
			
			IXRawfile.ScanHeaderInfo header = raw.getScanHeaderInfoForScanNum(180);
			System.out.println(
					"ScanHeaderInfo {\n" +
					"  " + header.getScanNumber() + ",\n" +
					"  " + header.getNumberPackets() + ",\n" +
					"  " + header.getStartTime() + ",\n" +
					"  " + header.getLowMass() + ",\n" +
					"  " + header.getHighMass() + ",\n" +
					"  " + header.getTIC() + ",\n" +
					"  " + header.getBasePeakMass() + ",\n" +
					"  " + header.getBasePeakIntensity() + ",\n" +
					"  " + header.getNumberChannels() + ",\n" +
					"  " + header.getUniformTime() + ",\n" +
					"  " + header.getFrequency() + "\n" +
					"}"
				);
			
			String statusloglabels[] = raw.getStatusLogLabelsForScanNum(180);
			System.out.println("Status log labels {");
			for (String label : statusloglabels)
				System.out.println("  " + label);
			System.out.println("}");
			
			System.out.println(raw.getStatusLogValueForScanNum(180, "Source Voltage (kV):"));
			
			HashMap<String,String> statuslog = raw.getStatusLogForScanNum(180);
			System.out.println("Status log {");
			for (String key : statuslog.keySet())
				System.out.println("  " + key + ": " + statuslog.get(key));
			System.out.println("}");
			
			HashMap<String,String> trailerextra = raw.getTrailerExtraForScanNum(180);
			System.out.println("Trailer extra {");
			for (String key : trailerextra.keySet())
				System.out.println("  " + key + ": " + trailerextra.get(key));
			System.out.println("}");
			
//			HashMap<String,String> tunedata = raw.getTuneData(0);
//			System.out.println("Tune data {");
//			for (String key : tunedata.keySet())
//				System.out.println("  " + key + ": " + tunedata.get(key));
//			System.out.println("}");
			
			for (int i=0; i<raw.getNumInstMethods(); ++i)
				System.out.println("  " + raw.GetInstMethod(i));
			
			raw.close();
			raw.dispose();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
