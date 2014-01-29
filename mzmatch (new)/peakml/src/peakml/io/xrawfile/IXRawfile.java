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



package peakml.io.xrawfile;


// java
import java.util.*;

// swt
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.ole.win32.*;
import org.eclipse.swt.internal.win32.*;





/**
 * This class provides a 1-to-1 mapping to the XRawfile DLL of Thermo, for reading the RAW file
 * format. Almost every function that is exposed in this DLL is implemented in this class and
 * has been made Java specific. This means that the awkward interface of the OLE connection 
 * is avoided and a clean interface can be used.
 * <p/>
 * The usage of the class is as follows (please read the function documentation for the details):
 * <pre>
 * IXRawfile rawfile = new IXRawfile();
 * rawfile.init();
 * int errorcode = rawfile.open("filename.RAW");
 * if (errorcode != IXRawfile.SUCCESS)
 *    System.out.println("[ERROR]: cannot open the file");
 * 
 * rawfile.setCurrentController(IXRawfile.CONTROLLER_MS, 1);
 * </pre>
 */
public class IXRawfile
{
	// static final strings
	/** The string that identifies the OLE-application in the windows registry. */
	public static final String PROGRAMID_XCALIBUR			= "XRAWFILE.XRawfileCtrl.1";
	/** The string that identifies the OLE-application in the windows registry. */
	public static final String PROGRAMID_MSFILEREADER		= "MSFileReader.XRawfileCtrl.1";
	
	
	// return codes
	/** This code indicates that the function call to the OCX was processed without error. */
	public static final int RTCODE_SUCCESS											=  1;
	/** This code indicates that a general error has occurred. This code may be returned whenever an error of indeterminate origin occurs. */
	public static final int RTCODE_FAILED											=  0;
	/** This code will be returned if no valid raw file is currently open. */
	public static final int RTCODE_RAW_FILE_INVALID									= -1;
	/** This code will be returned if no current controller has been specified. */
	public static final int RTCODE_CURRENT_CONTROLLER_INVALID						= -2;
	/** This code will be returned if the requested action is inappropriate for the currently defined controller. */
	public static final int RTCODE_OPERATION_NOT_SUPPORTED_ON_CURRENT_CONTROLLER	= -3;
	/** This code will be returned if an invalid parameter is passed in a function call to the OCX. This can occur if a parameter is out of range or initialized incorrectly. */
	public static final int RTCODE_PARAMETER_INVALID								= -4;
	/** This code will be returned if an incorrectly formatted scan filter is passed in a function call. */
	public static final int RTCODE_FILTER_FORMAT_INCORRECT							= -5;
	/** This code will be returned if an incorrectly formatted mass range is passed in a function call. */
	public static final int RTCODE_MASS_RANGE_FORMAT_INCORRECT						= -6;
	/** This code does not typically indicate an error. This code may be returned if optional data is not contained in the current raw file. */
	public static final int RTCODE_NO_DATA_PRESENT									=  2;
	
	
	// controller type
	/** This code indicates that there was no controller available. */
	public static final int CONTROLLER_NO_DEVICE									= -1;
	/** This code indicates that the controller was a mass spectrometer. */
	public static final int CONTROLLER_MS											=  0;
	/** This code final indicates that the controller was an analog device. */
	public static final int CONTROLLER_ANALOG										=  1;
	/** This code indicates that the controller was a analog-to-digital converter. */
	public static final int CONTROLLER_AD_CARD										=  2;
	/** This code indicates that the controller was a photo-diode-array device (whole range UV detector). */
	public static final int CONTROLLER_PDA											=  3;
	/** This code indicates that the controller was a UV detector. */
	public static final int CONTROLLER_UV											=  4;
	
	/**
	 * Central class for collecting all information about a controller.
	 */
	public static class Controller
	{
		// constructor(s)
		/**
		 * Standard constructor setting all the class members.
		 * 
		 * @param controllertype		The controller type.
		 * @param controllernumber		The controller number.
		 */
		public Controller(int controllertype, int controllernumber)
		{
			this.controllertype = controllertype;
			this.controllernumber = controllernumber;
		}
		
		// access
		/**
		 * Returns the controller type.
		 * 
		 * @return						The controller type.
		 */
		public int getControllerType()
		{
			return controllertype;
		}
		
		/**
		 * Returns the controller number.
		 * 
		 * @return						The controller number.
		 */
		public int getControllerNumber()
		{
			return controllernumber;
		}
		
		// Object overrides
		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer();
			str.append("ScanHeaderInfo: ");
			str.append(controllertype);
			str.append(", ");
			str.append(controllernumber);
			return str.toString();
		}
		
		// data
		protected int controllertype;
		protected int controllernumber;
	}
	
	
	// precursor information
	/**
	 * Central class for collecting all information about a precursor scan.
	 */
	public static class PrecursorInfo
	{
		// constructor(s)
		/**
		 * Standard constructor setting all the class members.
		 * 
		 * @param isolationmass			The user defined isolation mass used to trigger the data dependent scan.
		 * @param monoisotopicmass		The monoisotopic mass as determined by the instrument firmware.
		 * @param chargestate			The charge state as determined by the instrument firmware.
		 * @param scannumber			The scannumber of the parent scan.
		 */
		public PrecursorInfo(double isolationmass, double monoisotopicmass, int chargestate, int scannumber)
		{
			this.isolationmass = isolationmass;
			this.monoisotopicmass = monoisotopicmass;
			this.chargestate = chargestate;
			this.scannumber = scannumber;
		}
		
		// access
		/**
		 * Returns the user defined isolation mass used to trigger the data dependent scan.
		 * 
		 * @return						The user defined isolation mass used to trigger the data dependent scan.
		 */
		public double getIsolationMass()
		{
			return isolationmass;
		}
		
		/**
		 * Returns the monoisotopic mass as determined by the instrument firmware.
		 * 
		 * @return						The monoisotopic mass as determined by the instrument firmware.
		 */
		public double getMonoIsotopicMass()
		{
			return monoisotopicmass;
		}
		
		/**
		 * Returns the charge state as determined by the instrument firmware.
		 * 
		 * @return						The charge state as determined by the instrument firmware.
		 */
		public int getChargeState()
		{
			return chargestate;
		}
		
		/**
		 * Returns the scannumber of the parent scan.
		 * 
		 * @return						The scannumber of the parent scan.
		 */
		public int getScanNumber()
		{
			return scannumber;
		}
		
		// Object overrides
		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer();
			str.append("PrecursorInfo: ");
			str.append(isolationmass);
			str.append(", ");
			str.append(monoisotopicmass);
			str.append(", ");
			str.append(chargestate);
			str.append(", ");
			str.append(scannumber);
			return str.toString();
		}
		
		// data
		protected double isolationmass;
		protected double monoisotopicmass;
		protected int chargestate;
		protected int scannumber;
	}
	
	
	// scan header
	/**
	 * Central class for collecting all information about a scan.
	 */
	public static class ScanHeaderInfo
	{
		// constructor(s)
		/**
		 * Standard constructor setting all the class members.
		 * 
		 * @param scannumber			The scannumber.
		 * @param numberpackets			The number of packets collected for this scan.
		 * @param starttime				The start-time of the scan.
		 * @param lowmass				The lowest detected mass of the scan.
		 * @param highmass				The highest detected mass of the scan.
		 * @param tic					The total intensity of the scan.
		 * @param basepeakmass			The mass of the most intense peak of the scan.
		 * @param basepeakintensity		The intensity of the most intense peak of the scan.
		 * @param numberchannels		The number of channels for this scan.
		 * @param uniformtime			The uniform time of the scan.
		 * @param frequency				The frequency of the scan.
		 */
		public ScanHeaderInfo(int scannumber, int numberpackets, double starttime, double lowmass, double highmass, double tic, double basepeakmass, double basepeakintensity, int numberchannels, boolean uniformtime, double frequency)
		{
			this.scannumber = scannumber;
			this.numberpackets = numberpackets;
			this.starttime = starttime;
			this.lowmass = lowmass;
			this.highmass = highmass;
			this.tic = tic;
			this.basepeakmass = basepeakmass;
			this.basepeakintensity = basepeakintensity;
			this.numberchannels = numberchannels;
			this.uniformtime = uniformtime;
			this.frequency = frequency;
		}
		
		// access
		/**
		 * Returns the scannumber of this scan.
		 * 
		 * @return						The scannumber.
		 */
		public int getScanNumber()
		{
			return scannumber;
		}
		
		/**
		 * Returns the number of packets collected for this scan.
		 * 
		 * @return						The number of packets.
		 */
		public int getNumberPackets()
		{
			return numberpackets;
		}
		
		/**
		 * Returns the start time of the scan in minutes.
		 * 
		 * @return						The start time of the scan.
		 */
		public double getStartTime()
		{
			return starttime;
		}
		
		/**
		 * Returns the lowest detected mass of the scan.
		 * 
		 * @return						The lowest detected mass.
		 */
		public double getLowMass()
		{
			return lowmass;
		}
		
		/**
		 * Returns the highest detected mass of the scan.
		 * 
		 * @return						The highest detected mass.
		 */
		public double getHighMass()
		{
			return highmass;
		}
		
		/**
		 * Returns the total detected intensity of the scan.
		 * 
		 * @return						The total intensity.
		 */
		public double getTIC()
		{
			return tic;
		}
		
		/**
		 * Returns the mass of the most intense peak.
		 * 
		 * @return						The mass of the most intense peak.
		 */
		public double getBasePeakMass()
		{
			return basepeakmass;
		}
		
		/**
		 * Returns the intensity of the most intense peak.
		 * 
		 * @return						The intensity of the most intense peak. 
		 */
		public double getBasePeakIntensity()
		{
			return basepeakintensity;
		}
		
		/**
		 * Returns the number of channels for this scan.
		 * 
		 * @return						The number of channels.
		 */
		public int getNumberChannels()
		{
			return numberchannels;
		}
		
		/**
		 * Returns the uniform time for this scan.
		 * 
		 * @return						The uniform time of this scan.
		 */
		public boolean getUniformTime()
		{
			return uniformtime;
		}
		
		/**
		 * Returns the frequency for this scan.
		 * 
		 * @return						The frequency of this scan.
		 */
		public double getFrequency()
		{
			return frequency;
		}
		
		// Object overrides
		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer();
			str.append("ScanHeaderInfo: ");
			str.append(scannumber);
			str.append(", ");
			str.append(numberpackets);
			str.append(", ");
			str.append(starttime);
			str.append(", ");
			str.append(lowmass);
			str.append(", ");
			str.append(highmass);
			str.append(", ");
			str.append(tic);
			str.append(", ");
			str.append(basepeakmass);
			str.append(", ");
			str.append(basepeakintensity);
			str.append(", ");
			str.append(numberchannels);
			str.append(", ");
			str.append(uniformtime);
			str.append(", ");
			str.append(frequency);
			return str.toString();
		}
		
		// data
		protected int scannumber;
		protected int numberpackets;
		protected double starttime;
		protected double lowmass;
		protected double highmass;
		protected double tic;
		protected double basepeakmass;
		protected double basepeakintensity;
		protected int numberchannels;
		protected boolean uniformtime;
		protected double frequency;
	}
	
	
	// Noise data
	/**
	 * Central class for collecting the data of a noise-packet.
	 */
	public static class Noise
	{
		// constructor(s)
		/**
		 * Standard constructor setting all the class members.
		 * 
		 * @param mass					The mass of the noise packet.
		 * @param noise					The noise level of this mass.
		 * @param baseline				The detected baseline for this mass.
		 */
		public Noise(double mass, double noise, double baseline)
		{
			this.mass = mass;
			this.noise = noise;
			this.baseline = baseline;
		}
		
		// access
		/**
		 * Returns the mass of this noise packet.
		 * 
		 * @return						The mass of the noise packet.
		 */
		public double getMass()
		{
			return mass;
		}
		
		/**
		 * Returns the noise level of this mass.
		 * 
		 * @return						The noise level of this mass.
		 */
		public double getNoise()
		{
			return noise;
		}
		
		/**
		 * Returns the detected baseline for this mass.
		 * 
		 * @return						The baseline for this mass.
		 */
		public double getBaseline()
		{
			return baseline;
		}
		
		// Object overrides
		public String toString()
		{
			StringBuffer str = new StringBuffer();
			str.append("Noise: ");
			str.append(mass);
			str.append(", ");
			str.append(noise);
			str.append(", ");
			str.append(baseline);
			return str.toString();
		}
		
		// data
		protected double mass;
		protected double noise;
		protected double baseline;
	}
	
	
	// Label data
	/**
	 * Central class for collecting the label data for a mass peak.
	 */
	public static class Label
	{
		// constructor(s)
		/**
		 * Standard constructor setting all the class members.
		 * 
		 * @param mass					The mass.
		 * @param intensity				The intensity.
		 * @param resolution			The resolution for this peak.
		 * @param baseline				The baseline for this peak.
		 * @param noise					The noise-level for this peak.
		 * @param charge				The detected charge for this peak.
		 * @param saturated				Whether the peak was saturated.
		 * @param fragmented			Whether the peak was fragmented.
		 * @param merged				Whether the peak was merged.
		 * @param exception				Whether the peak was an exception (?).
		 * @param reference				Whether the peak has a reference (?).
		 * @param modified				Whether the peak was modified.
		 */
		public Label(double mass, double intensity, double resolution, double baseline, double noise, int charge, boolean saturated, boolean fragmented, boolean merged, boolean exception, boolean reference, boolean modified)
		{
			this.mass = mass;
			this.intensity = intensity;
			this.resolution = resolution;
			this.baseline = baseline;
			this.noise = noise;
			this.charge = charge;
			this.saturated = saturated;
			this.fragmented = fragmented;
			this.merged = merged;
			this.exception = exception;
			this.reference = reference;
			this.modified = modified;
		}
		
		// access
		/**
		 * Returns the mass.
		 * 
		 * @return						The mass.
		 */
		public double getMass()
		{
			return mass;
		}
		
		/**
		 * Returns the intensity.
		 * 
		 * @return						The intensity.
		 */
		public double getIntensity()
		{
			return intensity;
		}
		
		/**
		 * Returns the resolution.
		 * 
		 * @return						The resolution.
		 */
		public double getResolution()
		{
			return resolution;
		}
		
		/**
		 * Returns the baseline.
		 * 
		 * @return						The baseline.
		 */
		public double getBaseline()
		{
			return baseline;
		}
		
		/**
		 * Returns the noise-level.
		 * 
		 * @return						The noise level.
		 */
		public double getNoise()
		{
			return noise;
		}
		
		/**
		 * Returns the detected charge.
		 * 
		 * @return						The charge.
		 */
		public int getCharge()
		{
			return charge;
		}
		
		/**
		 * Whether the peak was saturated.
		 * 
		 * @return						True when saturated.
		 */
		public boolean getSaturated()
		{
			return saturated;
		}
		
		/**
		 * Whether the peak was fragmented.
		 * 
		 * @return						True when fragmented.
		 */
		public boolean getFragmented()
		{
			return fragmented;
		}
		
		/**
		 * Whether the peak was merged.
		 * 
		 * @return						True when merged.
		 */
		public boolean getMerged()
		{
			return merged;
		}
		
		/**
		 * Whether the peak was an exception (?).
		 * 
		 * @return						True when exception.
		 */
		public boolean getException()
		{
			return exception;
		}
		
		/**
		 * Whether the peak has a reference (?).
		 * 
		 * @return						True when reference.
		 */
		public boolean getReference()
		{
			return reference;
		}
		
		/**
		 * Whether the peak was modified.
		 * 
		 * @return						True when modified.
		 */
		public boolean getModified()
		{
			return modified;
		}
		
		// Object overrides
		@Override
		public String toString()
		{
			StringBuffer str = new StringBuffer();
			str.append("Label: ");
			str.append(mass);
			str.append(", ");
			str.append(intensity);
			str.append(", ");
			str.append(resolution);
			str.append(", ");
			str.append(baseline);
			str.append(", ");
			str.append(noise);
			str.append(", ");
			str.append(charge);
			str.append(", ");
			str.append(saturated);
			str.append(", ");
			str.append(fragmented);
			str.append(", ");
			str.append(merged);
			str.append(", ");
			str.append(exception);
			str.append(", ");
			str.append(reference);
			str.append(", ");
			str.append(modified);
			
			return str.toString();
		}
		
		// data
		protected double mass;
		protected double intensity;
		protected double resolution;
		protected double baseline;
		protected double noise;
		protected int charge;
		protected boolean saturated;
		protected boolean fragmented;
		protected boolean merged;
		protected boolean exception;
		protected boolean reference;
		protected boolean modified;
	}
	
	
	// initialization and disposal of the com-connection
	/**
	 * Initializes the OLE connection.
	 */
	public int init()
	{
		// create a UI environment for the OLE object
		display = Display.getCurrent();
		if (display == null)
			display = new Display();
		shell = display.getActiveShell();
		if (shell == null)
			shell = new Shell(display);
		oleframe = new OleFrame(shell, SWT.NONE);

		// create and activate the client site
		clientsite = null;
		try { clientsite = new OleClientSite(oleframe, SWT.NONE, PROGRAMID_XCALIBUR); }
		catch (SWTError err) { clientsite = new OleClientSite(oleframe, SWT.NONE, PROGRAMID_MSFILEREADER); }
		if (clientsite == null)
			return RTCODE_FAILED;
		
		int rt = clientsite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
		if (rt != OLE.S_OK)
			return RTCODE_FAILED;
		
		// create the automation for invoking methods
		automation = new OleAutomation(clientsite);
		
		// done
		return RTCODE_SUCCESS;
	}
	
	/**
	 * Disposes of the OLE connection. This needs to be called to have a clean finalization
	 * of the connection.
	 */
	public int dispose()
	{
		// close file just in case
		try {
			close();
		} catch (Exception e) { ; }
		
		// clean up
		automation.dispose();
		clientsite.deactivateInPlaceClient();
		clientsite.dispose();
		oleframe.dispose();
		
		// done
		return RTCODE_SUCCESS;
	}
	
	protected void finalize() throws Throwable
	{
		dispose();
	}
	
	
	// exposed functions
	/**
	 * Returns the last error message that was recorded by the Thermo OLE DLL.
	 * 
	 * @return					The last error message.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getErrorMessage() throws IXRawfileException
	{
		final String names[] = new String[] { "GetErrorMessage", "pbstrErrorMessage" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrErrorMessage = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		automation.invoke(functionids[0], new Variant[] { pbstrErrorMessage });
		
		// convert the string and destroy the memory
		String error = pbstrErrorMessage.getString();
		OS.GlobalFree(pointer);
		
		// done
		return error;
	}
	
	/**
	 * Opens a raw file for reading only. This function must be called before attempting to
	 * read any data from the raw file.
	 * 
	 * @param filename			The fully qualified path name of the raw file to open.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public void open(String filename) throws IXRawfileException
	{
		final String names[] = new String[] { "Open", "szFileName" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		Variant szFileName = new Variant(filename);
		Variant rt = automation.invoke(functionids[0], new Variant[] { szFileName });
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
	}
	
	/**
	 * Closes a raw file and frees the associated memory.
	 * 
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public void close() throws IXRawfileException
	{
		final String names[] = new String[] { "Close" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { });
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
	}
	
	/**
	 * Returns the fully qualified path name of an open raw file.
	 * 
	 * @return					The fully qualified path name of an open raw file
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getFileName() throws IXRawfileException
	{
		final String names[] = new String[] { "GetFileName", "pbstrFileName" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrFileName = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrFileName });
		
		// convert the string and destroy the memory
		String filename = pbstrFileName.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return filename;
	}
	
	/**
	 * Returns the creator ID. The creator ID is the logon name of the user when the raw 
	 * file was acquired.
	 * 
	 * @return					The logon name of the user.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getCreatorID() throws IXRawfileException
	{
		final String names[] = new String[] { "GetCreatorID", "pbstrCreatorID" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrCreatorID = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrCreatorID });
		
		// convert the string and destroy the memory
		String creatorid = pbstrCreatorID.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return creatorid;
	}
	
	/**
	 * Returns the file format version number.
	 * 
	 * @return					The file format version number.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getVersionNumber() throws IXRawfileException
	{
		final String names[] = new String[] { "GetVersionNumber", "pnVersion" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnVersion = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnVersion });
		
		// convert the string and destroy the memory
		int versionnumber = pnVersion.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return versionnumber;
	}
	
	/**
	 * Returns the file creation date in DATE format.
	 * 
	 * @return					The file creation date in DATE format.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getCreationDate() throws IXRawfileException
	{
		final String names[] = new String[] { "GetCreationDate", "pCreationDate" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pCreationDate = new Variant(pointer, (short) (OLE.VT_DATE|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pCreationDate });
		
		// convert the string and destroy the memory
		String date = pCreationDate.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return date;
	}
	
	/**
	 * Returns the number of registered device controllers in the raw file. A device controller
	 * represents an acquisition stream such as MS data, UV data, etc. Devices that do not
	 * acquire data such are autosamplers are not registered with the raw file during acquisition.
	 * 
	 * @return					The number of registered device controllers in the raw file
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumberOfControllers() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumberOfControllers", "pnNumControllers" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumControllers = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumControllers });
		
		// convert the string and destroy the memory
		int nrcontrollers = pnNumControllers.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return nrcontrollers;
	}
	
	/**
	 * Returns the number of registered device controllers of a particular type in the raw file. See
	 * Controller Type in the Enumerated Types section for a list of the available controller types
	 * and their respective values.
	 * 
	 * @param type				The controller type for which the number of registered controllers of that type are requested.
	 * @return					The number of registered device controllers of a particular type in the raw file.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumberOfControllersOfType(int type) throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumberOfControllersOfType", "nControllerType", "pnNumControllersOfType" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nControllerType = new Variant(type);
		Variant pnNumControllers = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nControllerType, pnNumControllers });
		
		// convert the string and destroy the memory
		int nrcontrollers = pnNumControllers.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return nrcontrollers;
	}
	
	/**
	 * Returns the type of the device controller registered at the specified index
	 * position in the raw file. Index values start at 0. See Controller Type in the Enumerated
	 * Types section for a list of the available controller types and their respective values.
	 * 
	 * @param controllernumber	The index value of the controller for which the type is to be returned.
	 * @return					The type of the device controller registered at the specified index position in the raw file. 
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getControllerType(int index) throws IXRawfileException
	{
		final String names[] = new String[] { "GetControllerType", "nIndex", "pnControllerType" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nIndex = new Variant(index);
		Variant pnControllerType = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nIndex, pnControllerType });
		
		// convert the string and destroy the memory
		int type = pnControllerType.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return type;
	}
	
	/**
	 * Sets the current controller in the raw file. This function must be called before
	 * subsequent calls to access data specific to a device controller (e.g. MS or UV data)
	 * may be made. All requests for data specific to a device controller will be forwarded
	 * to the current controller until the current controller is changed. The controller
	 * number is used to indicate which device controller to use if there are more than
	 * one registered device controller of the same type (e.g. multiple UV detectors).
	 * Controller numbers for each type are numbered starting at 1. See Controller Type
	 * in the Enumerated Types section for a list of the available controller types and
	 * their respective values.
	 * 
	 * @param controllertype	The type of controller for which information will be subsequently requested.
	 * @param controllernumber	The number of the controller of the specified type (1-based).
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public void setCurrentController(int controllertype, int controllernumber) throws IXRawfileException
	{
		final String names[] = new String[] { "SetCurrentController", "nControllerType", "nControllerNumber" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// create the parameters and invoke the call
		Variant nControllerType = new Variant(controllertype);
		Variant nControllerNumber = new Variant(controllernumber);
		Variant rt = automation.invoke(functionids[0], new Variant[] { nControllerType, nControllerNumber });
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
	}
	
	/**
	 * Gets the current controller type and number for the raw file. The controller number
	 * is used to indicate which device controller to use if there are more than one
	 * registered device controller of the same type (e.g. multiple UV detectors). Controller
	 * numbers for each type are numbered starting at 1. See Controller Type in the Enumerated
	 * Types section for a list of the available controller types and their respective values.
	 * 
	 * @return					The current controller type and number for the raw file.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public Controller getCurrentController() throws IXRawfileException
	{
		final String names[] = new String[] { "GetCurrentController", "pnControllerType", "pnControllerNumber" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_type = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_number = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnControllerType = new Variant(pointer_type, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant pnControllerNumber = new Variant(pointer_number, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnControllerType, pnControllerNumber });
		
		// convert the string and destroy the memory
		int type = pnControllerType.getInt();
		int number = pnControllerNumber.getInt();
		OS.GlobalFree(pointer_type);
		OS.GlobalFree(pointer_number);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return new Controller(type, number);
	}
	
	/**
	 * Gets the number of spectra acquired by the current controller. For non-scanning
	 * devices like UV detectors, the number of readings per channel is returned.
	 * 
	 * @return					The number of spectra acquired by the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumSpectra() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumSpectra", "pnNumberOfSpectra" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumberOfSpectra = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumberOfSpectra });
		
		// convert the string and destroy the memory
		int size = pnNumberOfSpectra.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return size;
	}
	
	/**
	 * Gets the number of status log entries recorded for the current controller.
	 * 
	 * @return					The number of status log entries recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumStatusLog() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumStatusLog", "pnNumberOfStatusLogEntries" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumberOfStatusLogEntries = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumberOfStatusLogEntries });
		
		// convert the string and destroy the memory
		int size = pnNumberOfStatusLogEntries.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return size;
	}
	
	/**
	 * Gets the number of error log entries recorded for the current controller.
	 * 
	 * @return					The number of error log entries recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumErrorLog() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumErrorLog", "pnNumberOfErrorLogEntries" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumberOfErrorLogEntries = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumberOfErrorLogEntries });
		
		// convert the string and destroy the memory
		int size = pnNumberOfErrorLogEntries.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return size;
	}
	
	/**
	 * Gets the number of tune data entries recorded for the current controller. Tune Data
	 * is only supported by MS controllers. Typically, if there is more than one tune data
	 * entry, each tune data entry corresponds to a particular acquisition segment.
	 * 
	 * @return					The number of tune data entries recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumTuneData() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumTuneData", "pnNumTuneData" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumTuneData = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumTuneData });
		
		// convert the string and destroy the memory
		int size = pnNumTuneData.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return size;
	}
	
	/**
	 * Gets the trailer extra entries recorded for the current controller. Trailer extra
	 * entries are only supported for MS device controllers and is used to store instrument
	 * specific information for each scan if used.
	 * 
	 * @return					The trailer extra entries recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumTrailerExtra() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumTrailerExtra", "pnNumberOfTrailerExtraEntries" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumberOfTrailerExtraEntries = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumberOfTrailerExtraEntries });
		
		// convert the string and destroy the memory
		int size = pnNumberOfTrailerExtraEntries.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return size;
	}
	
	/**
	 * Gets the mass resolution value recorded for the current controller. The value is
	 * returned as one half of the mass resolution. For example, a unit resolution controller
	 * would return a value of 0.5. This value is only relevant to scanning controllers such
	 * as MS.
	 * 
	 * @return					The resolution value recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double getMassResolution() throws IXRawfileException
	{
		final String names[] = new String[] { "GetMassResolution", "pdMassResolution" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pdMassResolution = new Variant(pointer, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pdMassResolution });
		
		// convert the string and destroy the memory
		double resolution = pdMassResolution.getDouble();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return resolution;
	}
	
	/**
	 * Gets the lowest mass or wavelength recorded for the current controller. This value
	 * is only relevant to scanning devices such as MS or PDA.
	 * 
	 * @return					The lowest mass or wavelength recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double getLowMass() throws IXRawfileException
	{
		final String names[] = new String[] { "GetLowMass", "pdLowMass" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pdLowMass = new Variant(pointer, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pdLowMass });
		
		// convert the string and destroy the memory
		double lowmass = pdLowMass.getDouble();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return lowmass;
	}
	
	/**
	 * Gets the highest mass or wavelength recorded for the current controller. This value
	 * is only relevant to scanning devices such as MS or PDA.
	 * 
	 * @return					The highest mass or wavelength recorded for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double getHighMass() throws IXRawfileException
	{
		final String names[] = new String[] { "GetHighMass", "pdHighMass" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pdHighMass = new Variant(pointer, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pdHighMass });
		
		// convert the string and destroy the memory
		double highmass = pdHighMass.getDouble();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return highmass;
	}
	
	/**
	 * Gets the start time of the first scan or reading for the current controller. This
	 * value is typically close to zero unless the device method contains a start delay.
	 * 
	 * @return					The start time of the first scan or reading for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double getStartTime() throws IXRawfileException
	{
		final String names[] = new String[] { "GetStartTime", "pdStartTime" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pdStartTime = new Variant(pointer, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pdStartTime });
		
		// convert the string and destroy the memory
		double starttime = pdStartTime.getDouble();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return starttime;
	}
	
	/**
	 * Gets the start time of the last scan or reading for the current controller.
	 * 
	 * @return					start time of the last scan or reading for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double getEndTime() throws IXRawfileException
	{
		final String names[] = new String[] { "GetEndTime", "pdEndTime" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pdEndTime = new Variant(pointer, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pdEndTime });
		
		// convert the string and destroy the memory
		double endtime = pdEndTime.getDouble();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return endtime;
	}
	
	/**
	 * Gets the highest base peak of all the scans for the current controller. This
	 * value is only relevant to MS device controllers.
	 * <p/>
	 * Appears not to work and the int-value is weird for intensity.
	 * 
	 * @return					highest base peak of all the scans for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getMaxIntensity() throws IXRawfileException
	{
		final String names[] = new String[] { "GetMaxIntensity", "pnMaxIntensity" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnMaxIntensity = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnMaxIntensity });
		
		// convert the string and destroy the memory
		int maxintensity = pnMaxIntensity.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return maxintensity;
	}
	
	/**
	 * Gets the first scan or reading number for the current controller. This value will
	 * always be one if data has been acquired.
	 * 
	 * @return					The first scan or reading number for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getFirstSpectrumNumber() throws IXRawfileException
	{
		final String names[] = new String[] { "GetFirstSpectrumNumber", "pnFirstSpectrum" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnFirstSpectrum = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnFirstSpectrum });
		
		// convert the string and destroy the memory
		int number = pnFirstSpectrum.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return number;
	}
	
	/**
	 * Gets the last scan or reading number for the current controller.
	 * 
	 * @return					The last scan or reading number for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getLastSpectrumNumber() throws IXRawfileException
	{
		final String names[] = new String[] { "GetLastSpectrumNumber", "pnLastSpectrum" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnLastSpectrum = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnLastSpectrum });
		
		// convert the string and destroy the memory
		int number = pnLastSpectrum.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return number;
	}
	
	/**
	 * Gets the instrument ID number for the current controller. This value is typically
	 * only set for raw files converted from other files formats.
	 * 
	 * @return					The instrument ID number for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getInstrumentID() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstrumentID", "pnInstrumentID" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnInstrumentID = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnInstrumentID });
		
		// convert the string and destroy the memory
		int id = pnInstrumentID.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return id;
	}
	
	/**
	 * Gets the inlet ID number for the current controller. This value is typically only set
	 * for raw files converted from other files formats.
	 * 
	 * @return					The inlet ID number for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getInletID() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInletID", "pnInletID" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnInletID = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnInletID });
		
		// convert the string and destroy the memory
		int id = pnInletID.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return id;
	}
	
	/**
	 * Returns the instrument name, if available, for the current controller.
	 * 
	 * @return					The instrument name, if available, for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstName() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstName", "pbstrInstName" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrInstName = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrInstName });
		
		// convert the string and destroy the memory
		String name = pbstrInstName.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return name;
	}
	
	/**
	 * Returns the instrument model, if available, for the current controller.
	 * 
	 * @return					The instrument model, if available, for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstModel() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstModel", "pbstrInstModel" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrInstModel = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrInstModel });
		
		// convert the string and destroy the memory
		String model = pbstrInstModel.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return model;
	}
	
	/**
	 * Returns the serial number, if available, for the current controller.
	 * 
	 * @return					The serial number, if available, for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstSerialNumber() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstSerialNumber", "pbstrInstSerialNumber" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrInstSerialNumber = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrInstSerialNumber });
		
		// convert the string and destroy the memory
		String sn = pbstrInstSerialNumber.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return sn;
	}
	
	/**
	 * Returns the current controller software revision information, if available.
	 * 
	 * @return					The current controller software revision information, if available.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstSoftwareVersion() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstSoftwareVersion", "pbstrInstSoftwareVersion" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrInstSoftwareVersion = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrInstSoftwareVersion });
		
		// convert the string and destroy the memory
		String version = pbstrInstSoftwareVersion.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return version;
	}
	
	/**
	 * Returns the current controller hardware or firmware revision information, if available.
	 * 
	 * @return					The current controller hardware or firmware revision information, if available.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstHardwareVersion() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstHardwareVersion", "pbstrInstHardwareVersion" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrInstHardwareVersion = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrInstHardwareVersion });
		
		// convert the string and destroy the memory
		String version = pbstrInstHardwareVersion.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return version;
	}
	
	/**
	 * Returns the experiment flags, if available, for the current controller. The returned
	 * string may contain one or more fields denoting information about the type of experiment
	 * performed.
	 * 
	 * @return					The experiment flags, if available, for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstFlags() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstFlags", "pbstrInstFlags" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pbstrInstFlags = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pbstrInstFlags });
		
		// convert the string and destroy the memory
		String flags = pbstrInstFlags.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return flags;
	}
	
	/**
	 * Returns the number of channel labels specified for the current controller. This
	 * field is only relevant to channel devices such as UV detectors, A/D cards, and
	 * Analog inputs. Typically, the number of channel labels, if labels are available,
	 * is the same as the number of configured channels for the current controller.
	 * 
	 * @return					The number of channel labels specified for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getInstNumChannelLabels() throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstNumChannelLabels", "pnInstNumChannelLabels" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnInstNumChannelLabels = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnInstNumChannelLabels });
		
		// convert the string and destroy the memory
		int size = pnInstNumChannelLabels.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return size;
	}
	
	/**
	 * Returns the channel label, if available, at the specified index for the current
	 * controller. This field is only relevant to channel devices such as UV detectors,
	 * A/D cards, and Analog inputs. Channel labels indices are numbered starting at 0.
	 * 
	 * @param channelnumber		The index value of the channel number field to return.
	 * @return					channel label, if available, at the specified index for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getInstChannelLabel(int channelnumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstChannelLabel", "nChannelLabelNumber", "pbstrInstChannelLabel" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nChannelLabelNumber = new Variant(channelnumber);
		Variant pbstrInstChannelLabel = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nChannelLabelNumber, pbstrInstChannelLabel });
		
		// convert the string and destroy the memory
		String label = pbstrInstChannelLabel.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return label;
	}
	
	/**
	 * Returns the list of unique scan filters for the raw file. This function is only supported for MS
	 * device controllers. If the function succeeds, pvarFilterArray will point to an array of BSTR fields
	 * each containing a unique scan filter and pnArraySize will contain the number of scan filters in the
	 * pvarFilterArray.
	 * 
	 * @return					The list of unique scan filters for the raw file.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String[] getFilters() throws IXRawfileException
	{
		final String names[] = new String[] { "GetFilters", "pvarFilterArray", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_array = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_size = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pvarFilterArray = new Variant(pointer_array, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnArraySize = new Variant(pointer_size, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pvarFilterArray, pnArraySize });
		
		// convert the string and destroy the memory
		int size = pnArraySize.getInt();
		String filters[] = VT_ARRAY__VT_BSTR(pointer_array, size);
		VT_ARRAY__DELETE(pointer_array);
		
		OS.GlobalFree(pointer_array);
		OS.GlobalFree(pointer_size);
		
		// done
		if (filters.length != size)
			throw new IXRawfileException(RTCODE_FAILED, "Call to '" + names[0] + "' returned incorrect number of filters.");
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return filters;
	}
	
	/**
	 * Returns the closest matching scan number that corresponds to dRT for the current controller. For non
	 * scanning devices, such as UV, the closest reading number is returned. The value of dRT must be within
	 * the acquisition run time for the current controller. The acquisition run time for the current controller
	 * may be obtained by calling GetStartTime and GetEndTime.
	 * 
	 * @param retentiontime		The run time or retention time, in minutes, for which the closest scan number is to be returned.
	 * @return					The closest matching scan number that corresponds to dRT for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int scanNumFromRT(double retentiontime) throws IXRawfileException
	{
		final String names[] = new String[] { "ScanNumFromRT", "dRT", "pnScanNumber" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant dRT = new Variant(retentiontime);
		Variant pnScanNumber = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { dRT, pnScanNumber });
		
		// convert the string and destroy the memory
		int scan = pnScanNumber.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return scan;
	}
	
	/**
	 * Returns the closest matching run time or retention time that corresponds to nScanNumber for the current
	 * controller. For non scanning devices, such as UV, the nScanNumber is taken to be the reading number. The
	 * value of nScanNumber must be within the range of scans or readings for the current controller. The range
	 * of scan or readings for the current controller may be obtained by calling GetFirstScanNumber and
	 * GetLastScanNumber.
	 * 
	 * @param scannumber		The scan number for which the closest run time or retention time is to be returned.
	 * @return					The closest matching run time or retention time that corresponds to nScanNumber for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double rtFromScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "RTFromScanNum", "nScanNumber", "pdRT" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber = new Variant(scannumber);
		Variant pdRT = new Variant(pointer, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pdRT });
		
		// convert the string and destroy the memory
		double retentiontime = pdRT.getDouble();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return retentiontime;
	}
	
	/**
	 * Returns the closest matching run time that corresponds to nScanNumber for the current controller. This
	 * function is only support for MS device controllers. The value of nScanNumber must be within the range of
	 * scans for the current controller. The range of scan or readings for the current controller may be obtained
	 * by calling GetFirstScanNumber and GetLastScanNumber.
	 * 
	 * @param scannumber		The scan number for which the corresponding scan filter is to be returned (1-based).
	 * @return					The closest matching run time that corresponds to nScanNumber for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getFilterForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetFilterForScanNum", "nScanNumber", "pbstrFilter" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber = new Variant(scannumber);
		Variant pbstrFilter = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pbstrFilter });
		
		// convert the string and destroy the memory
		String filter = pbstrFilter.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return filter;
	}
	
	/**
	 * Returns the closest matching run time that corresponds to nScanNumber for the current controller. This
	 * function is only support for MS device controllers. The value of nScanNumber must be within the range of
	 * scans for the current controller. The range of scan or readings for the current controller may be obtained
	 * by calling GetFirstScanNumber and GetLastScanNumber.
	 * 
	 * @param retentiontime		The scan number for which the corresponding scan filter is to be returned.
	 * @return					The closest matching run time that corresponds to nScanNumber for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getFilterForScanRT(double retentiontime) throws IXRawfileException
	{
		final String names[] = new String[] { "GetFilterForScanRT", "dRT", "pbstrFilter" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant dRT = new Variant(retentiontime);
		Variant pbstrFilter = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { dRT, pbstrFilter });
		
		// convert the string and destroy the memory
		String filter = pbstrFilter.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return filter;
	}
	
	/**
	 * Returns the mass list array. The format of the mass list returned will be an array of double
	 * precision values in mass intensity pairs in ascending mass order (e.g. mass 1, intensity 1,
	 * mass 2, intensity 2, mass 3, intensity 3, etc.).
	 * <p />
	 * This function is only applicable to scanning devices such as MS and PDA. The scan corresponding
	 * to pnScanNumber will be returned. The requested scan number must be valid for the current
	 * controller. Valid scan number limits may be obtained by calling GetFirstSpectrumNumber and
	 * GetLastSpectrumNumber.
	 * <p />
	 * To have profile scans centroided, set centroid to TRUE. This parameter is ignored
	 * for centroid scans.
	 * 
	 * @param scannumber		The mass list array.
	 * @param centroid			Set to true to have a profile scan centroided.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public double[] getMassListFromScanNum(int scannumber, boolean centroid) throws IXRawfileException
	{
		final String names[] = new String[] { "GetMassListFromScanNum", "pnScanNumber", "szFilter", "nIntensityCutoffType", "nIntensityCutoffValue", "nMaxNumberOfPeaks", "bCentroidResult", "pvarMassList", "pvarPeakFlags", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_scannumber = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_masslist   = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_peakflags  = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_arraysize  = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnScanNumber			= new Variant(pointer_scannumber, (short) (OLE.VT_I4|OLE.VT_BYREF));
		pnScanNumber.setByRef(scannumber);
		Variant szFilter				= new Variant("");
		Variant nIntensityCutoffType	= new Variant(0);			// no cutoff
		Variant nIntensityCutoffValue	= new Variant(0);			// no cutoff
		Variant nMaxNumberOfPeaks		= new Variant(0);			// all peaks
		Variant bCentroidResult			= new Variant(centroid);
		Variant pvarMassList			= new Variant(pointer_masslist, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pvarPeakFlags			= new Variant(pointer_peakflags, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));		// not supported by xcalibur
		Variant pnArraySize				= new Variant(pointer_arraysize, (short) (OLE.VT_I4|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnScanNumber, szFilter, nIntensityCutoffType, nIntensityCutoffValue, nMaxNumberOfPeaks, bCentroidResult, pvarMassList, pvarPeakFlags, pnArraySize });
		
		// convert the string and destroy the memory
		int rt_scannumber = pnScanNumber.getInt();
		
		int size = pnArraySize.getInt();
		double massintensity[] = VT_ARRAY__VT_R8(pointer_masslist, 2*size);
		VT_ARRAY__DELETE(pointer_masslist);
		VT_ARRAY__DELETE(pointer_peakflags);
		
		OS.GlobalFree(pointer_scannumber);
		OS.GlobalFree(pointer_masslist);
		OS.GlobalFree(pointer_peakflags);
		OS.GlobalFree(pointer_arraysize);
		
		// we didn't do anything with filter, so the scannumber should still be the same
		if (scannumber != rt_scannumber)
			throw new IXRawfileException(RTCODE_FAILED, "Call to '" + names[0] + "' produced a different scannumber than was expected");
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return massintensity;
	}
	
	/**
	 * This function is used to retrieve information about the parent scans of a data dependent
	 * MSn scan.
	 * <p/>
	 * The precursor mass info is returned as a list of the {@link PrecursorInfo} instances.
	 * <p/>
	 * <b>UNTESTED!</b>
	 * 
	 * @param scannumber		The scannumber to retrieve the precursor info.
	 * @return					
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public PrecursorInfo[] getPrecursorInfoFromScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetPrecursorInfoFromScanNum", "nScanNumber", "pvarPrecursorInfo", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_infos = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_size = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber			= new Variant(scannumber);
		Variant pvarPrecursorInfos	= new Variant(pointer_infos, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnArraySize			= new Variant(pointer_size, (short) (OLE.VT_I4|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pvarPrecursorInfos, pnArraySize });

		// convert the string and destroy the memory
		int size = pnArraySize.getInt();
		double[] data = VT_ARRAY__R8_R8_I4_I4(pointer_infos, size);
		VT_ARRAY__DELETE(pointer_infos);
		
		OS.GlobalFree(pointer_infos);
		OS.GlobalFree(pointer_size);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		PrecursorInfo infos[] = new PrecursorInfo[size];
		for (int i=0; i<data.length; i+=4)
			infos[i/4] = new PrecursorInfo(data[i+0], data[i+1], (int) data[i+2], (int) data[i+3]);
		
		return infos;
	}
	
	/**
	 * 
	 * 
	 * @param scannumber
	 * @throws IXRawfileException
	 */
	public Vector<Noise> getNoiseData(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetNoiseData", "pvarNoisePacket", "pnScanNumber" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_packets = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_scannumber = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pvarNoisePacket	= new Variant(pointer_packets, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnScanNumber	= new Variant(pointer_scannumber, (short) (OLE.VT_I4|OLE.VT_BYREF));
		pnScanNumber.setByRef(scannumber);
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { pvarNoisePacket, pnScanNumber });

		// convert the string and destroy the memory
		double noise[] = VT_ARRAY__R8_R4_R4(pointer_packets);
		VT_ARRAY__DELETE(pointer_packets);
		
		OS.GlobalFree(pointer_packets);
		OS.GlobalFree(pointer_scannumber);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		Vector<Noise> data = new Vector<Noise>();
		for (int i=0; i<noise.length; i+=3)
		{
			data.add(new Noise(
					noise[i+0], noise[i+1], noise[i+2]
				));
		}
		
		return data;
	}
	
	/**
	 * Returns TRUE if the scan specified by nScanNumber is a profile scan, FALSE if the scan is
	 * a centroid scan. The value of nScanNumber must be within the range of scans or readings
	 * for the current controller. The range of scan or readings for the current controller may
	 * be obtained by calling GetFirstScanNumber and GetLastScanNumber.
	 * 
	 * @param scannumber		The scan number for which the profile data type information is to be returned.
	 * @return					TRUE if the scan specified by nScanNumber is a profile scan, FALSE if the scan is a centroid scan.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public boolean isProfileScanForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "IsProfileScanForScanNum", "nScanNumber", "pbIsProfileScan" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber			= new Variant(scannumber);
		Variant pbIsProfileScan		= new Variant(pointer, (short) (OLE.VT_BOOL|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pbIsProfileScan });
		
		// convert the string and destroy the memory
		boolean isprofile = pbIsProfileScan.getBoolean();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return isprofile;
	}
	
	/**
	 * Returns TRUE if the scan specified by nScanNumber is a centroid scan, FALSE if the scan
	 * is a profile scan. The value of nScanNumber must be within the range of scans or readings
	 * for the current controller. The range of scan or readings for the current controller may
	 * be obtained by calling GetFirstScanNumber and GetLastScanNumber.
	 * 
	 * @param scannumber		The scan number for which the profile data type information is to be returned.
	 * @return					TRUE if the scan specified by nScanNumber is a centroid scan, FALSE if the scan is a profile scan.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public boolean isCentroidScanForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "IsCentroidScanForScanNum", "nScanNumber", "pbIsCentroidScan" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber			= new Variant(scannumber);
		Variant pbIsCentroidScan	= new Variant(pointer, (short) (OLE.VT_BOOL|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pbIsCentroidScan });
		
		// convert the string and destroy the memory
		boolean isprofile = pbIsCentroidScan.getBoolean();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return isprofile;
	}
	
	/**
	 * For a given scan number, this function returns information from the scan header for the current
	 * controller. The value of nScanNumber must be within the range of scans or readings for the
	 * current controller. The range of scan or readings for the current controller may be obtained
	 * by calling GetFirstScanNumber and GetLastScanNumber.
	 * <p />
	 * The validity of these parameters depends on the current controller. For example, pdLowMass,
	 * pdHighMass, pdTIC, pdBasePeakMass, and pdBasePeakIntensity are only likely to be set on
	 * return for MS or PDA controllers. PnNumChannels is only likely to be set on return for Analog,
	 * UV, and A/D Card controllers. PdUniformTime, and pdFrequency are only likely to be set on
	 * return for UV, and A/D Card controllers and may be valid for Analog controllers. In cases
	 * where the value is not set, a value of zero is returned.
	 * 
	 * @param scannumber		The scan number for which the scan header information is to be returned.
	 * @return					The information from the scan header for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public ScanHeaderInfo getScanHeaderInfoForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetScanHeaderInfoForScanNum", "nScanNumber", "pnNumPackets", "pdStartTime", "pdLowMass", "pdHighMass", "pdTIC", "pdBasePeakMass", "pdBasePeakIntensity", "pnNumChannels", "pbUniformTime", "pdFrequency" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_numpackets			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_starttime			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_lowmass				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_highmass			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_tic					= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_basepeakmass		= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_basepeakintensity	= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_numchannels			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_uniformtime			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_frequency			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber			= new Variant(scannumber);
		Variant pnNumPackets		= new Variant(pointer_numpackets, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant pdStartTime			= new Variant(pointer_starttime, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pdLowMass			= new Variant(pointer_lowmass, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pdHighMass			= new Variant(pointer_highmass, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pdTIC				= new Variant(pointer_tic, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pdBasePeakMass		= new Variant(pointer_basepeakmass, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pdBasePeakIntensity	= new Variant(pointer_basepeakintensity, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pnNumChannels		= new Variant(pointer_numchannels, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant pbUniformTime		= new Variant(pointer_uniformtime, (short) (OLE.VT_BOOL|OLE.VT_BYREF));
		Variant pdFrequency			= new Variant(pointer_frequency, (short) (OLE.VT_R8|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pnNumPackets, pdStartTime, pdLowMass, pdHighMass, pdTIC, pdBasePeakMass, pdBasePeakIntensity, pnNumChannels, pbUniformTime, pdFrequency });
		
		// convert the string and destroy the memory
		ScanHeaderInfo header = new ScanHeaderInfo(
				nScanNumber.getInt(),
				pnNumPackets.getInt(),
				pdStartTime.getDouble(),
				pdLowMass.getDouble(),
				pdHighMass.getDouble(),
				pdTIC.getDouble(),
				pdBasePeakMass.getDouble(),
				pdBasePeakIntensity.getDouble(),
				pnNumChannels.getInt(),
				pbUniformTime.getBoolean(),
				pdFrequency.getDouble()
			);
		
		OS.GlobalFree(pointer_numpackets);
		OS.GlobalFree(pointer_starttime);
		OS.GlobalFree(pointer_lowmass);
		OS.GlobalFree(pointer_highmass);
		OS.GlobalFree(pointer_tic);
		OS.GlobalFree(pointer_basepeakmass);
		OS.GlobalFree(pointer_basepeakintensity);
		OS.GlobalFree(pointer_numchannels);
		OS.GlobalFree(pointer_uniformtime);
		OS.GlobalFree(pointer_frequency);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return header;
	}
	
	/**
	 * Returns the recorded status log entry labels and values for the current controller. The value
	 * of scannumber must be within the range of scans or readings for the current controller. The
	 * range of scan or readings for the current controller may be obtained by calling
	 * GetFirstScanNumber and GetLastScanNumber.
	 * <p />
	 * On return, the label RT will contain the retention time at which the status log entry was
	 * recorded. This time may not be the same as the retention time corresponding to the specified
	 * scan number but will be the closest status log entry to the scan time.
	 * 
	 * @param scannumber		The scan number for which status log information is to be returned.
	 * @return					The recorded status log entry labels and values for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public HashMap<String,String> getStatusLogForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetStatusLogForScanNum", "nScanNumber", "pdStatusLogRT", "pvarLabels", "pvarValues", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_rt					= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_labels				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_values				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_size				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber			= new Variant(scannumber);
		Variant pdStatusLogRT		= new Variant(pointer_rt, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pvarLabels			= new Variant(pointer_labels, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pvarValues			= new Variant(pointer_values, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnArraySize			= new Variant(pointer_size, (short) (OLE.VT_I4|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pdStatusLogRT, pvarLabels, pvarValues, pnArraySize });
		
		// convert the string and destroy the memory
		int size = pnArraySize.getInt();
		double retentiontime = pdStatusLogRT.getDouble();
		String labels[] = VT_ARRAY__VT_BSTR(pointer_labels, size);
		VT_ARRAY__DELETE(pointer_labels);
		String values[] = VT_ARRAY__VT_BSTR(pointer_values, size);
		VT_ARRAY__DELETE(pointer_values);
		
		OS.GlobalFree(pointer_rt);
		OS.GlobalFree(pointer_labels);
		OS.GlobalFree(pointer_values);
		OS.GlobalFree(pointer_size);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		HashMap<String,String> lbl_val = new HashMap<String,String>();
		lbl_val.put("RT", Double.toString(retentiontime));
		for (int i=0; i<size; ++i)
			lbl_val.put(labels[i], values[i]);
		return lbl_val;
	}
	
	/**
	 * Returns the recorded status log entry labels for the current controller. The value of
	 * nScanNumber must be within the range of scans or readings for the current controller.
	 * The range of scan or readings for the current controller may be obtained by calling
	 * GetFirstScanNumber and GetLastScanNumber.
	 * 
	 * @param scannumber		The scan number for which status log information is to be returned.
	 * @return					The recorded status log entry labels for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String[] getStatusLogLabelsForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetStatusLogLabelsForScanNum", "nScanNumber", "pdStatusLogRT", "pvarLabels", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_rt = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_labels = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_size = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber		= new Variant(scannumber);
		Variant pdStatusLogRT	= new Variant(pointer_rt, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pvarLabels		= new Variant(pointer_labels, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnArraySize		= new Variant(pointer_size, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pdStatusLogRT, pvarLabels, pnArraySize });
		
		// convert the string and destroy the memory
		int size = pnArraySize.getInt();
		String labels[] = VT_ARRAY__VT_BSTR(pointer_labels, size);
		VT_ARRAY__DELETE(pointer_labels);
		
		OS.GlobalFree(pointer_rt);
		OS.GlobalFree(pointer_labels);
		OS.GlobalFree(pointer_size);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return labels;
	}
	
	/**
	 * Returns the recorded status log parameter value for the specified status log parameter
	 * label for the current controller. The value of nScanNumber must be within the range of
	 * scans or readings for the current controller. The range of scan or readings for the current
	 * controller may be obtained by calling GetFirstScanNumber and GetLastScanNumber.
	 * <p />
	 * To obtain a list of the status log parameter labels, call GetStatusLogLabelsForScanNum.

	 * @param scannumber		The scan number for which status log information is to be returned.
	 * @param label				A string containing the label for which the status log parameter value is to be returned.
	 * @return					The recorded status log parameter value for the specified status log parameter label for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getStatusLogValueForScanNum(int scannumber, String label) throws IXRawfileException
	{
		final String names[] = new String[] { "GetStatusLogValueForScanNum", "nScanNumber", "szLabel", "pdStatusLogRT", "pvarValue" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_rt = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_value = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber		= new Variant(scannumber);
		Variant szLabel			= new Variant(label);
		Variant pdStatusLogRT	= new Variant(pointer_rt, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pvarValue		= new Variant(pointer_value, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, szLabel, pdStatusLogRT, pvarValue });
		
		// convert the string and destroy the memory
		String value = pvarValue.getString();
		
		OS.GlobalFree(pointer_rt);
		OS.GlobalFree(pointer_value);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return value;
	}
	
	/**
	 * Returns the recorded trailer extra entry labels and values for the current controller. This
	 * function is only valid for MS controllers. The value of nScanNumber must be within the range
	 * of scans or readings for the current controller. The range of scan or readings for the current
	 * controller may be obtained by calling GetFirstScanNumber and GetLastScanNumber.
	 * 
	 * @param scannumber		The scan number for which trailer extra information is to be returned.
	 * @return					The recorded trailer extra entry labels and values for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public HashMap<String,String> getTrailerExtraForScanNum(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetTrailerExtraForScanNum", "nScanNumber", "pvarLabels", "pvarValues", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_labels				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_values				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_size				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nScanNumber			= new Variant(scannumber);
		Variant pvarLabels			= new Variant(pointer_labels, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pvarValues			= new Variant(pointer_values, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnArraySize			= new Variant(pointer_size, (short) (OLE.VT_I4|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nScanNumber, pvarLabels, pvarValues, pnArraySize });
		
		// convert the string and destroy the memory
		int size = pnArraySize.getInt();
		String labels[] = VT_ARRAY__VT_BSTR(pointer_labels, size);
		VT_ARRAY__DELETE(pointer_labels);
		String values[] = VT_ARRAY__VT_BSTR(pointer_values, size);
		VT_ARRAY__DELETE(pointer_values);
		
		OS.GlobalFree(pointer_labels);
		OS.GlobalFree(pointer_values);
		OS.GlobalFree(pointer_size);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		HashMap<String,String> lbl_val = new HashMap<String,String>();
		for (int i=0; i<size; ++i)
			lbl_val.put(labels[i], values[i]);
		return lbl_val;
	}
	
	/**
	 * Returns the specified error log item information and the retention time at which the error
	 * occurred. The value of nItemNumber must be within the range of one to the number of error
	 * log items recorded for the current controller. The number of error log items for the current
	 * controller may be obtained by calling GetNumErrorLog.
	 * 
	 * @param itemnumber		The error log item number for which information is to be returned.
	 * @return					The specified error log item information and the retention time at which the error occurred.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String getErrorLogItem(int itemnumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetErrorLogItem", "nItemNumber", "pdRT", "pbstrErrorMessage" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_rt				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_error			= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nItemNumber			= new Variant(itemnumber);
		Variant pdRT				= new Variant(pointer_rt, (short) (OLE.VT_R8|OLE.VT_BYREF));
		Variant pbstrErrorMessage	= new Variant(pointer_error, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nItemNumber, pdRT, pbstrErrorMessage });
		
		// convert the string and destroy the memory
		double retentiontime = pdRT.getDouble();
		String error = pbstrErrorMessage.getString();
		
		OS.GlobalFree(pointer_rt);
		OS.GlobalFree(pointer_error);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		return "RT '" + retentiontime + "': " + error;
	}
	
	/**
	 * Returns the recorded tune parameter labels and values for the current controller. This
	 * function is only valid for MS controllers. The value of nSegmentNumber must be within the
	 * range of one to the number of tune data items recorded for the current controller. The number
	 * of tune data items for the current controller may be obtained by calling GetNumTuneData.
	 * 
	 * @param segmentnumber		The acquisition segment for which tune information is to be returned.
	 * @return					The recorded tune parameter labels and values for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public HashMap<String,String> getTuneData(int segmentnumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetTuneData", "nSegmentNumber", "pvarLabels", "pvarValues", "pnArraySize" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer_labels				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_values				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int pointer_size				= (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nSegmentNumber		= new Variant(segmentnumber);
		Variant pvarLabels			= new Variant(pointer_labels, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pvarValues			= new Variant(pointer_values, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnArraySize			= new Variant(pointer_size, (short) (OLE.VT_I4|OLE.VT_BYREF));
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { nSegmentNumber, pvarLabels, pvarValues, pnArraySize });
		
		// convert the string and destroy the memory
		int size = pnArraySize.getInt();
		String labels[] = VT_ARRAY__VT_BSTR(pointer_labels, size);
		VT_ARRAY__DELETE(pointer_labels);
		String values[] = VT_ARRAY__VT_BSTR(pointer_values, size);
		VT_ARRAY__DELETE(pointer_values);
		
		OS.GlobalFree(pointer_labels);
		OS.GlobalFree(pointer_values);
		OS.GlobalFree(pointer_size);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		HashMap<String,String> lbl_val = new HashMap<String,String>();
		for (int i=0; i<size; ++i)
			lbl_val.put(labels[i], values[i]);
		return lbl_val;
	}
	
	/**
	 * Returns the number of instrument methods contained in the raw file. Each instrument used
	 * in the acquisition for which a method was created in Instrument Setup (e.g. autosampler,
	 * LC, MS, PDA) will have its instrument method contained in the raw file.
	 * 
	 * @return					The number of instrument methods contained in the raw file.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public int getNumInstMethods() throws IXRawfileException
	{
		final String names[] = new String[] { "GetNumInstMethods", "pnNumInstMethods" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pnNumInstMethods = new Variant(pointer, (short) (OLE.VT_I4|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { pnNumInstMethods });
		
		// convert the string and destroy the memory
		int nrmethods = pnNumInstMethods.getInt();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return nrmethods;
	}
	
	/**
	 * Returns the channel label, if available, at the specified index for the current controller. This
	 * field is only relevant to channel devices such as UV detectors, A/D cards, and Analog inputs.
	 * Channel labels indices are numbered starting at 0.
	 * <p />
	 * Returns the instrument method, if available, at the index specified in methoditem. The
	 * instrument method indicies are numbered starting at 0. The number of instrument methods can be
	 * obtained by calling GetNumInstMethods.
	 * 
	 * @param methoditem		A long variable containing the index value of the instrument method to be returned.
	 * @return					The channel label, if available, at the specified index for the current controller.
	 * @throws IXRawfileException
	 * 							1 if successful; otherwise, see Error Codes.
	 */
	public String GetInstMethod(int methoditem) throws IXRawfileException
	{
		final String names[] = new String[] { "GetInstMethod", "nInstMethodItem", "pbstrInstMethod" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int pointer = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant nInstMethodItem = new Variant(methoditem);
		Variant pbstrInstMethod = new Variant(pointer, (short) (OLE.VT_BSTR|OLE.VT_BYREF));
		Variant rt = automation.invoke(functionids[0], new Variant[] { nInstMethodItem, pbstrInstMethod });
		
		// convert the string and destroy the memory
		String method = pbstrInstMethod.getString();
		OS.GlobalFree(pointer);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		return method;
	}
	
	/**
	 * This method enables you to read the FT-PROFILE labels of a scan represented by the
	 * scanNumber.
	 * 
	 * The label data contains values of mass (double), intensity (double), resolution (float),
	 * baseline (float), noise (float) and charge (int).
	 * 
	 * The flags are returned as unsigned char values. There are the flags saturated, fragmented,
	 * merged, exception, reference and modified.

	 * @param scannumber		The scan number for which the corresponding label data are to be returned.
	 * @return
	 */
	public Vector<Label> getLabelData(int scannumber) throws IXRawfileException
	{
		final String names[] = new String[] { "GetLabelData", "pvarLabels", "pvarFlags", "pnScanNumber" };
		final int functionids[] = automation.getIDsOfNames(names);
		
		// allocate memory for the return-value(s)
		int ptr_labels = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int ptr_flags = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		int ptr_scannumber = (int) OS.GlobalAlloc(OS.GMEM_FIXED|OS.GMEM_ZEROINIT, Variant.sizeof);
		
		// create the parameters and invoke the call
		Variant pvarLabels = new Variant(ptr_labels, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pvarFlags = new Variant(ptr_flags, (short) (OLE.VT_VARIANT|OLE.VT_BYREF));
		Variant pnScanNumber = new Variant(ptr_scannumber, (short) (OLE.VT_I4|OLE.VT_BYREF));
		pnScanNumber.setByRef(scannumber);
		
		Variant rt = automation.invoke(functionids[0], new Variant[] { pvarLabels, pvarFlags, pnScanNumber });
		
		// convert the string and destroy the memory
		double labels[] = VT_ARRAY__R8_R8_R4_R4_R4_I4(ptr_labels);
		VT_ARRAY__DELETE(ptr_labels);
		boolean flags[] = VT_ARRAY__BOOL_BOOL_BOOL_BOOL_BOOL_BOOL(ptr_flags);
		VT_ARRAY__DELETE(ptr_flags);
		
		OS.GlobalFree(ptr_labels);
		OS.GlobalFree(ptr_flags);
		OS.GlobalFree(ptr_scannumber);
		
		// done
		if (rt.getInt() != RTCODE_SUCCESS)
			throw new IXRawfileException(rt.getInt(), "Call to '" + names[0] + "' failed with error-code: " + rt.getInt() + "\n'" + getErrorMessage() + "'");
		
		Vector<Label> result = new Vector<Label>();
		for (int i=0; i<labels.length; i+=6)
		{
			result.add(new Label(
					labels[i+0], labels[i+1], labels[i+2], labels[i+3], labels[i+4], (int) labels[i+5],
					flags[i+0], flags[i+1], flags[i+2], flags[i+3], flags[i+4], flags[i+5]
				));
		}
		
		return result;
	}

	
	
	// data
	private Shell shell = null;
	private Display display = null;
	private OleFrame oleframe = null;
	private OleClientSite clientsite = null;
	private OleAutomation automation = null;
	
	
	// helpers
	private static void VT_ARRAY__DELETE(int ptr)
	{
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		if (ptr_safearray[0] == 0)
			return;
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		OS.GlobalFree(ptr_data[0]);
	}
	
	private static boolean[] VT_ARRAY__BOOL_BOOL_BOOL_BOOL_BOOL_BOOL(int ptr)
	{
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		short size[] = new short[1];				// retrieve the size from the safe-array struct
		OS.MoveMemory(size, ptr_safearray[0]+16, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		int pos = ptr_data[0];
		byte val[] = new byte[1];
		boolean data[] = new boolean[6*size[0]];
		for (int i=0; i<size[0]; ++i)
		{
			OS.MoveMemory(val, pos+ 0, 1);
			data[6*i + 0] = val[0]!=0;
			OS.MoveMemory(val, pos+ 1, 1);
			data[6*i + 1] = val[0]!=0;
			OS.MoveMemory(val, pos+ 2, 1);
			data[6*i + 2] = val[0]!=0;
			OS.MoveMemory(val, pos+ 3, 1);
			data[6*i + 3] = val[0]!=0;
			OS.MoveMemory(val, pos+ 4, 1);
			data[6*i + 4] = val[0]!=0;
			OS.MoveMemory(val, pos+ 5, 1);
			data[6*i + 5] = val[0]!=0;
			pos += 6;
		}
		
		return data;
	}
	
	private static double[] VT_ARRAY__R8_R8_R4_R4_R4_I4(int ptr)
	{
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		short size[] = new short[1];				// retrieve the size from the safe-array struct
		OS.MoveMemory(size, ptr_safearray[0]+16, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		int pos = ptr_data[0];
		double val[] = new double[1];
		double data[] = new double[6*size[0]];
		for (int i=0; i<size[0]; ++i)
		{
			OS.MoveMemory(val, pos+ 0, 8);
			data[6*i + 0] = val[0];
			OS.MoveMemory(val, pos+ 8, 8);
			data[6*i + 1] = val[0];
			OS.MoveMemory(val, pos+16, 8);
			data[6*i + 2] = val[0];
			OS.MoveMemory(val, pos+24, 8);
			data[6*i + 3] = val[0];
			OS.MoveMemory(val, pos+32, 8);
			data[6*i + 4] = val[0];
			OS.MoveMemory(val, pos+40, 8);
			data[6*i + 5] = val[0];
			pos += 48;
		}
		
		return data;
	}
	
	private static double[] VT_ARRAY__R8_R4_R4(int ptr)
	{
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		short size[] = new short[1];				// retrieve the size from the safe-array struct
		OS.MoveMemory(size, ptr_safearray[0]+16, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		int pos = ptr_data[0];
		double val[] = new double[1];
		double data[] = new double[3*size[0]];
		for (int i=0; i<size[0]; ++i)
		{
			OS.MoveMemory(val, pos+ 0, 8);
			data[3*i + 0] = val[0];
			OS.MoveMemory(val, pos+ 8, 8);
			data[3*i + 1] = val[0];
			OS.MoveMemory(val, pos+16, 8);
			data[3*i + 2] = val[0];
			pos += 24;
		}
		
		return data;
	}
	
	private static double[] VT_ARRAY__R8_R8_I4_I4(int ptr, int size)
	{
		if (size == 0)
			return new double[0];
		
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		// move the data from the safe array in our own array
		int chargestate[]=new int[1], scannumber[]=new int[1];
		double isolationmass[]=new double[1], monoisotopicmass[]=new double[1];
		
		int pos = ptr_data[0];
		double data[] = new double[size*4];
		for (int i=0; i<data.length; i+=4)
		{
			OS.MoveMemory(isolationmass, pos+ 0, 8);
			OS.MoveMemory(monoisotopicmass, pos+ 8, 8);
			OS.MoveMemory(chargestate, pos+16, 4);
			OS.MoveMemory(scannumber, pos+20, 4);
			pos += 24;
			data[i+0] = isolationmass[0];
			data[i+1] = monoisotopicmass[0];
			data[i+2] = chargestate[0];
			data[i+3] = scannumber[0];
		}
		
		return data;
	}
	
	/*
	private static float[] VT_ARRAY__VT_R4(int ptr, int size)
	{
		if (size == 0)
			return new float[0];
		
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		// move the data from the safe array in our own array
		float data[] = new float[size];
		OS.MoveMemory(data, ptr_data[0], 4*size);
		
		return data;
	}
	*/
	
	private static double[] VT_ARRAY__VT_R8(int ptr, int size)
	{
		if (size == 0)
			return new double[0];
		
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		// move the data from the safe array in our own array
		double data[] = new double[size];
		OS.MoveMemory(data, ptr_data[0], 8*size);
		
		return data;
	}
	
	private static String[] VT_ARRAY__VT_BSTR(int ptr, int size)
	{
		if (size == 0)
			return new String[0];
		
		// read the meta-data
		short type[] = new short[1];				// read the type of the variant
		OS.MoveMemory(type, ptr, 2);
		int ptr_safearray[] = new int[1];			// retrieve the pointer to the actual safe-array struct
		OS.MoveMemory(ptr_safearray, ptr+8, 4);
		int ptr_data[] = new int[1];				// retrieve the pointer to the data from the safe-array struct
		OS.MoveMemory(ptr_data, ptr_safearray[0]+12, 4);
		
		// extract the strings from the safe array
		String strings[] = new String[/*dims[0]*/size];
		for (int i=0; i</*dims[0]*/size; ++i)
		{
			int ptr_bstr[] = new int[1];				// retrieve the pointer to the current bstr
			OS.MoveMemory(ptr_bstr, ptr_data[0]+(i*4), 4);
			
			int length[] = new int[1];
			OS.MoveMemory(length, ptr_bstr[0]-4, 4);
			
			char string[] = new char[length[0]];
			OS.MoveMemory(string, ptr_bstr[0], length[0]);
			
			int reallength = 0;
			for (int j=0; j<length[0]; ++j)
			{
				if (string[j] != '\0')
					reallength++;
				else
					break;
			}
			strings[i] = new String(string, 0, reallength);
		}
		
		return strings;
	}
} // when ... will ... hurt ... STOP?!
