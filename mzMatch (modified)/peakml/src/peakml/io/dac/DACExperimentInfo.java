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
import java.util.*;

// peakml
import peakml.*;
import peakml.chemistry.*;





/**
 * Collection of parameters and other information on the experiment. 
 */
public class DACExperimentInfo
{
	// 
	public static class ExFunctionInfo
	{
		// access
		public Polarity getPolarity()
		{
			return polarity;
		}
		
		public Spectrum.DataFormat getDataFormat()
		{
			return dataformat;
		}
		
		// data
		protected Polarity polarity = Polarity.POSITIVE;
		protected Spectrum.DataFormat dataformat = Spectrum.DataFormat.CENTROID;
	}
	
	
	// DACExperimentInfo mapping
	/**
	 * 
	 * 
	 * @param			The file to open.
	 * @return			RTCODE_SUCCESS when all is ok, RTCODE_FILE_NOT_FOUND when the file is not found.
	 */
	public native int open(String filename);
	
	
	// access
	/**
	 * Returns the free-text experiment text.
	 * 
	 * @return				The free-text experiment text
	 */
	public String getExperimentText()
	{
		return ExperimentText;
	}
	
	/**
	 * Returns the number of functions (or data-streams) associated to the given file. This
	 * number should match that of {@link DACFunctionInfo#getNumberOfFunctions(String)}; if
	 * this is not the case, the interpretation of the free-text has failed.
	 * 
	 * @return				The number of functions.
	 */
	public int getNumFunctions()
	{
		return functions.size();
	}
	
	/**
	 * 
	 * @param functionid	The function-id to collect the extended information for.
	 * @return
	 */
	public ExFunctionInfo getExFunctionInfo(int functionid)
	{
		return functions.get(functionid-1);
	}
	
	public Vector<ExFunctionInfo> getExFunctionInfos()
	{
		return functions;
	}
	
	
	// Object overrides
	@Override
	public String toString()
	{
		return ExperimentText;
	}
	
	
	// data interpretation
	protected void init()
	{
		final int STATE_NONE			= 0;
		final int STATE_FUNCTION		= 1;
		
		functions.clear();
		if (ExperimentText==null || ExperimentText.length()==0)
			return;
		
		try
		{
			int state = STATE_NONE;
			
			String line;
			BufferedReader in = new BufferedReader(new StringReader(ExperimentText));
			while ((line = in.readLine().trim()) != null)
			{
				if (line.length() == 0)
					state = STATE_NONE;
				else if (line.startsWith("Function Parameters - Function"))
				{
					state = STATE_FUNCTION;
					functions.add(new ExFunctionInfo());
				}
				else if (state == STATE_FUNCTION)
				{
					String tokens[] = line.split("\\s+");
					if (tokens[0].equals("Data Format"))
						functions.lastElement().dataformat = tokens[1].equals("Centroid") ? Spectrum.DataFormat.CENTROID : Spectrum.DataFormat.PROFILE;
					else if (tokens[0].equals("Polarity"))
						functions.lastElement().polarity = tokens[1].equals("Positive") ? Polarity.POSITIVE : Polarity.NEGATIVE;
				}
			}
		}
		catch (Exception e)
		{
			; // this can't happen.
		}
	}
	
	
	// data
	protected String ExperimentText;
	protected Vector<ExFunctionInfo> functions = new Vector<ExFunctionInfo>();
}
