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
public class DACProcessInfo
{
	// DACProcessInfo mapping
	public native int open(String filename, int functionnr);
	
	
	// access
	public int getNumProcesses()
	{
		return NumProcesses;
	}
	
	public String[] getProcessDescs()
	{
		return ProcessDescs;
	}
	
	
	// Object overrides
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("DACProcessInfo {\n");
		for (String process : ProcessDescs)
			str.append("  " + process + "\n");
		str.append("}\n");
		
		return str.toString();
	}
	
	
	// data
	protected int NumProcesses;
	protected String[] ProcessDescs;
}
