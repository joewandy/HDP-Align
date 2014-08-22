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
 * Collection of values of the calibration used for the data.
 */
public class DACCalibrationInfo
{
	// 
	public static class StaticFunction
	{
		// constructor(s)
		public StaticFunction(String function)
		{
			String tokens[] = function.split(",");
			coefficients = new double[tokens.length-1];
			for (int i=0; i<tokens.length-1; i++)
				coefficients[i] = Double.parseDouble(tokens[i]);
			functiontype = tokens[tokens.length-1];
		}
		
		// access
		public String getFunctionType()
		{
			return functiontype;
		}
		
		public double[] getCoefficients()
		{
			return coefficients;
		}
		
		// data
		protected String functiontype;
		protected double coefficients[];
	}
	
	public static class StaticParams
	{
		// constructor(s)
		public StaticParams(String params)
		{
			String tokens[] 	= params.split(",");
			lowmass				= Double.parseDouble(tokens[0]);
			highmass			= Double.parseDouble(tokens[1]);
			dwelltime			= Double.parseDouble(tokens[2]);
			span				= Double.parseDouble(tokens[3]);
			lowmassresolution	= Double.parseDouble(tokens[4]);
			highmassresolution	= Double.parseDouble(tokens[5]);
			ionenergy			= Double.parseDouble(tokens[6]);
			referencefile		= tokens.length>7 ? tokens[7] : "";
			acquisitionfile		= tokens.length>8 ? tokens[8] : "";
		}
		
		// access
		public double getLowMass()
		{
			return lowmass;
		}
		
		public double getHighMass()
		{
			return highmass;
		}
		
		public double getDwellTime()
		{
			return dwelltime;
		}
		
		public double getSpan()
		{
			return span;
		}
		
		public double getLowMassResolution()
		{
			return lowmassresolution;
		}
		
		public double getHighMassResolution()
		{
			return highmassresolution;
		}
		
		public double getIonEnergy()
		{
			return ionenergy;
		}
		
		public String getReferenceFile()
		{
			return referencefile;
		}
		
		public String getAcquisitionFile()
		{
			return acquisitionfile;
		}
		
		// data
		protected double lowmass;
		protected double highmass;
		protected double dwelltime;
		protected double span;
		protected double lowmassresolution;
		protected double highmassresolution;
		protected double ionenergy;
		protected String referencefile;
		protected String acquisitionfile;
	}
	
	public static class DynamicParams
	{
		// constructor(s)
		public DynamicParams(String params)
		{
			String tokens[] 	= params.split(",");
			lowmass				= Double.parseDouble(tokens[0]);
			highmass			= Double.parseDouble(tokens[1]);
			scantime			= Double.parseDouble(tokens[2]);
			interscandelay		= Double.parseDouble(tokens[3]);
			lowmassresolution	= Double.parseDouble(tokens[4]);
			highmassresolution	= Double.parseDouble(tokens[5]);
			ionenergy			= Double.parseDouble(tokens[6]);
			referencefile		= tokens.length>7 ? tokens[7] : "";
			acquisitionfile		= tokens.length>8 ? tokens[8] : "";
		}
		
		// access
		public double getLowMass()
		{
			return lowmass;
		}
		
		public double getHighMass()
		{
			return highmass;
		}
		
		public double getScanTime()
		{
			return scantime;
		}
		
		public double getInterScanDelay()
		{
			return interscandelay;
		}
		
		public double getLowMassResolution()
		{
			return lowmassresolution;
		}
		
		public double getHighMassResolution()
		{
			return highmassresolution;
		}
		
		public double getIonEnergy()
		{
			return ionenergy;
		}
		
		public String getReferenceFile()
		{
			return referencefile;
		}
		
		public String getAcquisitionFile()
		{
			return acquisitionfile;
		}
		
		// data
		protected double lowmass;
		protected double highmass;
		protected double scantime;
		protected double interscandelay;
		protected double lowmassresolution;
		protected double highmassresolution;
		protected double ionenergy;
		protected String referencefile;
		protected String acquisitionfile;
	}
	
	public static class FastParams
	{
		// constructor(s)
		public FastParams(String params)
		{
			String tokens[] 	= params.split(",");
			lowmass				= Double.parseDouble(tokens[0]);
			highmass			= Double.parseDouble(tokens[1]);
			scantime			= Double.parseDouble(tokens[2]);
			interscandelay		= Double.parseDouble(tokens[3]);
			lowmassresolution	= Double.parseDouble(tokens[4]);
			highmassresolution	= Double.parseDouble(tokens[5]);
			ionenergy			= Double.parseDouble(tokens[6]);
			referencefile		= tokens.length>7 ? tokens[7] : "";
			acquisitionfile		= tokens.length>8 ? tokens[8] : "";
		}
		
		// access
		public double getLowMass()
		{
			return lowmass;
		}
		
		public double getHighMass()
		{
			return highmass;
		}
		
		public double getScanTime()
		{
			return scantime;
		}
		
		public double getInterScanDelay()
		{
			return interscandelay;
		}
		
		public double getLowMassResolution()
		{
			return lowmassresolution;
		}
		
		public double getHighMassResolution()
		{
			return highmassresolution;
		}
		
		public double getIonEnergy()
		{
			return ionenergy;
		}
		
		public String getReferenceFile()
		{
			return referencefile;
		}
		
		public String getAcquisitionFile()
		{
			return acquisitionfile;
		}
		
		// data
		protected double lowmass;
		protected double highmass;
		protected double scantime;
		protected double interscandelay;
		protected double lowmassresolution;
		protected double highmassresolution;
		protected double ionenergy;
		protected String referencefile;
		protected String acquisitionfile;
	}
	
	
	// DACCalibrationInfo mapping
	/**
	 * 
	 * 
	 * @param			The file to open.
	 * @return			RTCODE_SUCCESS when all is ok, RTCODE_FILE_NOT_FOUND when the file is not found.
	 */
	public native int open(String filename);
	
	
	// access
	public DynamicParams getMS1DynamicParams()
	{
		return new DynamicParams(MS1DynamicParams);
	}
	
	public FastParams getMS1FastParams()
	{
		return new FastParams(MS1FastParams);
	}
	
	public StaticFunction getMS1StaticFunction()
	{
		return new StaticFunction(MS1StaticFunction);
	}
	
	public StaticParams getMS1StaticParams()
	{
		return new StaticParams(MS1StaticParams);
	}
	
	public DynamicParams getMS2DynamicParams()
	{
		return new DynamicParams(MS2DynamicParams);
	}
	
	public FastParams getMS2FastParams()
	{
		return new FastParams(MS2FastParams);
	}
	
	public StaticFunction getMS2StaticFunction()
	{
		return new StaticFunction(MS2StaticFunction);
	}
	
	public StaticParams getMS2StaticParams()
	{
		return new StaticParams(MS2StaticParams);
	}
	
	public String getCalTime()
	{
		return CalTime;
	}
	
	public String getCalDate()
	{
		return CalDate;
	}
	
	public int getNumCalFunctions()
	{
		return NumCalFunctions;
	}
	
	public String[] getCalFunctions()
	{
		return CalFunctions;
	}
	
	
	// Object overrides
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("DACCalibrationInfo {\n");
		str.append("  MS1DynamicParams:  " + MS1DynamicParams + "\n");
		str.append("  MS1FastParams:     " + MS1FastParams + "\n");
		str.append("  MS1StaticFunction: " + MS1StaticFunction + "\n");
		str.append("  MS1StaticParams:   " + MS1StaticParams + "\n");
		str.append("  MS2DynamicParams:  " + MS2DynamicParams + "\n");
		str.append("  MS2FastParams:     " + MS2FastParams + "\n");
		str.append("  MS2StaticFunction: " + MS2StaticFunction + "\n");
		str.append("  MS2StaticParams:   " + MS2StaticParams + "\n");
		str.append("  CalTime:           " + CalTime + "\n");
		str.append("  CalDate:           " + CalDate + "\n");
		str.append("  NumCalFunctions:   " + NumCalFunctions + "\n");
		str.append("  CalFunctions:\n");
		for (String function : CalFunctions)
			str.append("  * " + function + "\n");
		str.append("}\n");
		
		return str.toString();
	}
	
	
	// data
	protected String MS1DynamicParams;
	protected String MS1FastParams;
	protected String MS1StaticFunction;
	protected String MS1StaticParams;
	protected String MS2DynamicParams;
	protected String MS2FastParams;
	protected String MS2StaticFunction;
	protected String MS2StaticParams;
	protected String CalTime;
	protected String CalDate;
	protected int NumCalFunctions;
	protected String CalFunctions[];
}
