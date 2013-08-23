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





/**
 * 
 */
public class DACScanStats
{
	// DACScanStats mapping
	/**
	 * 
	 * 
	 * @param filename		The file to open.
	 * @param functionnr	The function to open.
	 * @return				RTCODE_SUCCESS when all is ok, RTCODE_FILE_NOT_FOUND when the file is not found.
	 */
	public native int open(String filename, int functionnr, int processnr, int scannr);
	
	
	// access
	public int getAccurateMass()
	{
		return AccurateMass;
	}
	
	public double getBPI()
	{
		return BPI;
	}
	
	public double getBPM()
	{
		return BPM;
	}
	
	public int getCalibrated()
	{
		return Calibrated;
	}
	
	public boolean getContinuum()
	{
		return Continuum;
	}
	
	public double getHiMass()
	{
		return HiMass;
	}
	
	public double getLoMass()
	{
		return LoMass;
	}
	
	public int getMolecularMass()
	{
		return MolecularMass;
	}
	
	public int getOverload()
	{
		return Overload;
	}
	
	public int getPeaksInScan()
	{
		return PeaksInScan;
	}
	
	public double getRetnTime()
	{
		return RetnTime;
	}
	
	public int getSegment()
	{
		return Segment;
	}
	
	public double getTIC()
	{
		return TIC;
	}
	
	
	// Object overrides
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("DACScanStats {\n");
		str.append("  AccurateMass:  " + AccurateMass + "\n");
		str.append("  BPI:           " + BPI + "\n");
		str.append("  BPM:           " + BPM + "\n");
		str.append("  Calibrated:    " + Calibrated + "\n");
		str.append("  Continuum:     " + Continuum + "\n");
		str.append("  HiMass:        " + HiMass + "\n");
		str.append("  LoMass:        " + LoMass + "\n");
		str.append("  MolecularMass: " + MolecularMass + "\n");
		str.append("  Overload:      " + Overload + "\n");
		str.append("  PeaksInScan:   " + PeaksInScan + "\n");
		str.append("  RetnTime:      " + RetnTime + "\n");
		str.append("  Segment:       " + Segment + "\n");
		str.append("  TIC:           " + TIC + "\n");
		str.append("}\n");
		
		return str.toString();
	}
	
	
	// data
	protected int AccurateMass;
	protected double BPI;
	protected double BPM;
	protected int Calibrated;
	protected boolean Continuum;
	protected double HiMass;
	protected double LoMass;
	protected int MolecularMass;
	protected int Overload;
	protected int PeaksInScan;
	protected double RetnTime;
	protected int Segment;
	protected double TIC;
}
