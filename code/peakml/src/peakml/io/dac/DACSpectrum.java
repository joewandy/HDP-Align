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

// peakml
import peakml.math.*;





/**
 * 
 */
public class DACSpectrum
{
	// DACSpectrum mapping
	/**
	 * 
	 * 
	 * @param filename		The file to open.
	 * @param functionnr	The function to open.
	 * @return				RTCODE_SUCCESS when all is ok, RTCODE_FILE_NOT_FOUND when the file is not found.
	 */
	public native int open(String filename, int functionnr, int processnr, int scannr);
	
	
	// access
	public int getNumPeaks()
	{
		return NumPeaks;
	}
	
	public double[] getMasses()
	{
		return Masses;
	}
	
	public double[] getIntensities()
	{
		return Intensities;
	}
	
	
	// Object overrides
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("DACSpectrum {\n");
		str.append("  Nr peaks:        " + NumPeaks + "\n");
		str.append("  Mass range:      " + Statistical.min(Masses) + " - " + Statistical.max(Masses) + "\n");
		str.append("  Intensity range: " + Statistical.min(Intensities) + " - " + Statistical.max(Intensities) + "\n");
		str.append("}\n");
		
		return str.toString();
	}
	
	
	// data
	protected int NumPeaks;
	protected double Masses[];
	protected double Intensities[];
}
