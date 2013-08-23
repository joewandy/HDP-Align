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

// peakml
import peakml.io.dac.*;



public class OpenDAC
{
	public static void main(String args[])
	{
		try
		{
			DAC.init();
			
			final String filename = "G:\\data\\masslynx-urine_2009-12-15\\METAB_7_00201.RAW";

			DACHeader hdr = new DACHeader();
			hdr.open(filename);
			System.out.println(hdr);
			
			DACSpectrum spectrum = new DACSpectrum();
			spectrum.open(filename, 1, 0, 120);
			System.out.println(spectrum);
			
			DACCalibrationInfo cal = new DACCalibrationInfo();
			cal.open(filename);
			System.out.println(cal);
			
			DACScanStats scanstats = new DACScanStats();
			scanstats.open(filename, 1, 0, 1);
			System.out.println(scanstats);
			
			DACExScanStats exscanstats = new DACExScanStats();
			exscanstats.open(filename, 1, 0, 50);
			System.out.println(exscanstats);
			
			int nrfunctions = DACFunctionInfo.getNumberOfFunctions(filename);
			for (int functionnr=1; functionnr<=nrfunctions; ++functionnr)
			{
				DACFunctionInfo function = new DACFunctionInfo();
				function.open(filename, functionnr);
				System.out.println(function);
			}
			
			DACExperimentInfo exp = new DACExperimentInfo();
			exp.open(filename);
			for (DACExperimentInfo.ExFunctionInfo exfunction : exp.getExFunctionInfos())
				System.out.println(exfunction.getPolarity() + " - " + exfunction.getDataFormat());

			DACProcessInfo processinfo = new DACProcessInfo();
			processinfo.open(filename, 1);
			System.out.println(processinfo);
			
			System.out.println("\n\n\n");
			System.out.println(exp);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
