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
import java.io.*;
import java.util.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;



public class OpenPeakML
{
	public static void recursive(IPeak peak)
	{
		Class<? extends IPeak> cls = peak.getClass();
		if (cls.equals(IPeakSet.class))
		{
			for (IPeak p : (IPeakSet<IPeak>) peak)
				recursive(p);
		}
		else if (cls.equals(MassChromatogram.class))
			;
		else if (cls.equals(BackgroundIon.class))
			;
	}
	
	public static void main(String args[])
	{
		try
		{
			ParseResult result = PeakMLParser.parse(new FileInputStream("[FILENAME].peakml"), true);
			Header header = result.header;
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;
			
			// determine the container type
			if (peakset.getContainerClass().equals(IPeakSet.class))
				System.out.println("IPeakSet");
			else if (peakset.getContainerClass().equals(BackgroundIon.class))
				System.out.println("BackgroundIon");
			else if (peakset.getContainerClass().equals(MassChromatogram.class))
				System.out.println("MassChromatogram");
			
			// unpack IPeakSet's in the IPeakSet
			Vector<IPeak> unpacked = IPeak.unpack(peakset);
			
			// traverse the data recursively
			recursive(peakset);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
