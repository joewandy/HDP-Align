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
import peakml.util.*;





/**
 * 
 */
public class DACFunctionInfo
{
	// function type
	public static final String TYPE_SCAN					= "Scan";
	public static final String TYPE_SIR						= "SIR";
	public static final String TYPE_PARENTS					= "Parents";
	public static final String TYPE_DAUGHTERS				= "Daughters";
	public static final String TYPE_NEUTRAL_LOSS			= "Neutral Loss";
	public static final String TYPE_NEUTRAL_GAIN			= "Neutral Gain";
	public static final String TYPE_MRM						= "MRM";
	public static final String TYPE_Q1F						= "Q1F";
	public static final String TYPE_MS2						= "MS2";
	public static final String TYPE_DIODE_ARRAY				= "Diode Array";
	public static final String TYPE_TOF						= "TOF";
	public static final String TYPE_TOF_PSD					= "TOF PSD";
	public static final String TYPE_TOF_SURVEY				= "TOF Survey";
	public static final String TYPE_TOF_DAUGHTER			= "TOF Daughter";
	public static final String TYPE_MALDI_TOF				= "Maldi TOF";
	public static final String TYPE_TOF_MS					= "TOF MS";
	public static final String TYPE_TOF_PARENT				= "TOF Parent";
	public static final String TYPE_VOLTAGE_SCAN			= "Voltage Scan";
	public static final String TYPE_MAGNETIC_SCAN			= "Magnetic Scan";
	public static final String TYPE_VOLTAGE_SIR				= "Voltage SIR";
	public static final String TYPE_MAGNETIC_SIR			= "Magnetic SIR";
	public static final String TYPE_AUTO_DAUGHTERS			= "Auto Daughters";
	public static final String TYPE_AUTOSPEC_BE_SCAN		= "AutoSpec B/E Scan";
	public static final String TYPE_AUTOSPEC_BE2_SCAN		= "AutoSpec B^2/E Scan";
	public static final String TYPE_AUTOSPEC_CNL_SCAN		= "AutoSpec CNL Scan";
	public static final String TYPE_AUTOSPEC_MIKES_SCAN		= "AutoSpec MIKES Scan";
	public static final String TYPE_AUTOSPEC_MRM			= "AutoSpec MRM";
	public static final String TYPE_AUTOSPEC_NRMS_SCAN		= "AutoSpec NRMS Scan";
	public static final String TYPE_AUTOSPECQ_MRM_SCAN		= "AutoSpec-Q MRM Scan";
	
	
	// DACFunctionInfo mapping
	/**
	 * 
	 * 
	 * @param filename		The file to open.
	 * @param functionnr	The function to open.
	 * @return				RTCODE_SUCCESS when all is ok, RTCODE_FILE_NOT_FOUND when the file is not found.
	 */
	public native int open(String filename, int functionnr);
	
	/**
	 * 
	 * 
	 * @return
	 */
	public native static int getNumberOfFunctions(String filename);
	
	
	// access
	public String getFunctionType()
	{
		return FunctionType;
	}
	
	public double getStartRT()
	{
		return StartRT;
	}
	
	public double getEndRT()
	{
		return EndRT;
	}
	
	public int getNumScans()
	{
		return NumScans;
	}
	
	public double getFunctionSetMass()
	{
		return FunctionSetMass;
	}
	
	public int getNumSegments()
	{
		return NumSegments;
	}
	
	public double[] getSIRChannels()
	{
		return SIRChannels;
	}
	
	public double[] getMRMParents()
	{
		return MRMParents;
	}
	
	public double[] getMRMDaughters()
	{
		return MRMDaughters;
	}
	
	
	// Object overrides
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("DACFunctionInfo {\n");
		str.append("  FunctionType:    " + FunctionType + "\n");
		str.append("  StartRT:         " + StartRT + "\n");
		str.append("  EndRT:           " + EndRT + "\n");
		str.append("  NumScans:        " + NumScans + "\n");
		str.append("  FunctionSetMass: " + FunctionSetMass + "\n");
		str.append("  NumSegments:     " + NumSegments + "\n");
		str.append("  SIRChannels:     " + DataTypes.toString(SIRChannels) + "\n");
		str.append("  MRMParents:      " + DataTypes.toString(MRMParents) + "\n");
		str.append("  MRMDaughters:    " + DataTypes.toString(MRMDaughters) + "\n");
		str.append("}\n");
		
		return str.toString();
	}
	
	
	// data
	protected String FunctionType;
	protected double StartRT;
	protected double EndRT;
	protected int NumScans;
	protected double FunctionSetMass;
	protected int NumSegments;
	protected double SIRChannels[];
	protected double MRMParents[];
	protected double MRMDaughters[];
}
