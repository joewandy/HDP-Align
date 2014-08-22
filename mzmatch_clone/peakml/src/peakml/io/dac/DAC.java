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
import peakml.util.*;





/**
 * Implementation of a 1-to-1 mapping of the Waters Datafile Access Component. This is a
 * suite of Visual C++ Active Template Library (ATL) classes, allowing for relative
 * easy access to MassLynx raw data files. This suite defines a number of classes to be
 * used for data retrieval, which have been reproduced in this package
 * ({@link DACCalibrationInfo}, {@link DACExperimentInfo}, {@link DACExScanStats},
 * {@link DACFunctionInfo}, {@link DACHeader}, {@link DACProcessInfo},
 * {@link DACScanStats}, {@link DACSpectrum}).
 * <p />
 * The setup is always that the open-function must be called, which accepts the filename
 * as a parameter at a minimum (this means the file is opened at each access) and occasionally
 * more. In order to track the ionisation mode and the data format (centroid or profile) the
 * class {@link DACExperimentInfo} needs to be used. This class retrieves a free-text
 * experiment information, which is interpreted.
 * <p />
 * This implementation utilizes the associated DLL with a JNI coupling in 'dac.dll'.
 * This means that the MassLynx raw files are <b>only</b> accessible in the windows
 * environment.
 */
public class DAC
{
	// return codes
	/** The function was completed successfully */
	public static final int RTCODE_SUCCESS						= 0000;
	/** The header was not open */
	public static final int RTCODE_NOT_INITIALIZED				= 0001;
	/** Cannot find the data file specified. */
	public static final int RTCODE_FILE_NOT_FOUND				= 2001;
	/** Part of the RAW file is missing, or the process specified does not exist (could be a process "greyed out" in the history list). */
	public static final int RTCODE_MISSING_DATA					= 4010;
	/** A handle cannot not be found for the data specified (check that the scan/process number combination is valid) */
	public static final int RTCODE_NO_HANDLE					= 4003;
	/** The requested scannumber cannot be found. */
	public static final int RTCODE_SCANNUMBER_NOT_FOUND			= 2004;
	/** Could not close the data file correctly after reading it. */
	public static final int RTCODE_FILE_NOT_CLOSED				= 4006;
	/** One of the function parameters is out of its permitted range. */
	public static final int RTCODE_PARAMETER_OUT_OF_RANGE		= 8001;
	/** Memory could not be allocated to the read the required data. */
	public static final int RTCODE_OUT_OF_MEMORY				= 8002;
	/** An unrecognized chromatogram type was specified. */
	public static final int RTCODE_INVALID_CHROMATOGRAM_TYPE	= 8003;
	/** The specified process does not exist. */
	public static final int RTCODE_INVALID_PROCESS				= 8004;
	
	
	// test
	public static void init() throws IOException
	{
		final String libname = "dac";
		
		String os = System.getProperty("os.name");
		if (!os.toLowerCase().contains("windows"))
			throw new RuntimeException("The Waters DAC will only work on Microsoft Windows platforms.");
		
		// check whether the library is available
		File f = new File(libname + ".dll");
		if (!f.exists())
		{
			// export it
			InputStream input = ClassLoader.getSystemResourceAsStream(f.getName());
			if (input == null)
				throw new IOException("Cannot find resource '" + libname + "'");
			IO.copyfile(input, new FileOutputStream(libname + ".dll"));
		}
		
		// load the library
		System.loadLibrary(libname);
	}
}
