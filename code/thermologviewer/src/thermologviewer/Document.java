/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of ThermoLogViewer.
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



package thermologviewer;


// java
import java.util.*;

// swt
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

// peakml
import peakml.io.xrawfile.*;

// ThermoLogViewer
import thermologviewer.data.*;





/**
 * 
 */
public class Document
{
	public static final int UPDATE_INIT				= 0;
	public static final int UPDATE_ADD_FILE			= 1;
	public static final int UPDATE_LABEL_CHANGE		= 2;
	
	public static final int LOG_STATUS				= 0;
	public static final int LOG_TRAILER_EXTRA		= 1;
	
	
	// constructor(s)
	public Document(MainWnd wnd)
	{
		// save the mainwnd pointer
		this.mainwnd = wnd;
	}
	
	public void init()
	{
		finish();
		
		activelog = LOG_STATUS;
		statusloglabel = "";
	}
	
	public void finish()
	{
		rawfiles.clear();
		statusloglabels.clear();
	}
	
	
	// access
	public void addFile(String filename)
	{
		IXRawfile rawfile = new IXRawfile();
		int rtcode = rawfile.init();
		if (rtcode != IXRawfile.RTCODE_SUCCESS)
		{
			MessageBox msg = new MessageBox(mainwnd.shell, SWT.OK|SWT.ICON_ERROR);
			msg.setMessage("Could not connect to the OLE-connection of Thermo's xrawfile.dll");
			msg.open();
			return;
		}
		
		try
		{
			rawfile.open(filename);
		}
		catch (IXRawfileException e)
		{
			e.printStackTrace();
			rawfile.dispose();
			MessageBox msg = new MessageBox(mainwnd.shell, SWT.OK|SWT.ICON_ERROR);
			msg.setMessage("Failed to open:\n'" + filename + "'");
			msg.open();
			return;
		}
		
		double retentiontimes[] = null;
		HashMap<String,double[]> statuslog = new HashMap<String,double[]>();
		Vector<IXRawfile.Controller> controllers = new Vector<IXRawfile.Controller>();
		try
		{
			// retrieve the controllers
			for (int i=1; i<=rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_MS); ++i)
				controllers.add(new IXRawfile.Controller(IXRawfile.CONTROLLER_MS, i));
			for (int i=1; i<=rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_UV); ++i)
				controllers.add(new IXRawfile.Controller(IXRawfile.CONTROLLER_UV, i));
			for (int i=1; i<=rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_PDA); ++i)
				controllers.add(new IXRawfile.Controller(IXRawfile.CONTROLLER_PDA, i));
			for (int i=1; i<=rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_ANALOG); ++i)
				controllers.add(new IXRawfile.Controller(IXRawfile.CONTROLLER_ANALOG, i));
			for (int i=1; i<=rawfile.getNumberOfControllersOfType(IXRawfile.CONTROLLER_AD_CARD); ++i)
				controllers.add(new IXRawfile.Controller(IXRawfile.CONTROLLER_AD_CARD, i));
			
			// retrieve the status-log labels for all the available controllers
			for (IXRawfile.Controller controller : controllers)
			{
				if (controller.getControllerType() != IXRawfile.CONTROLLER_MS)
					continue;
				
				// retrieve the controller
				rawfile.setCurrentController(controller.getControllerType(), controller.getControllerNumber());
				
				// retrieve the status-logs
				int spectrum1 = rawfile.getFirstSpectrumNumber();
				int nrspectra = (rawfile.getLastSpectrumNumber() - rawfile.getFirstSpectrumNumber()) + 1;
				
				retentiontimes = new double[nrspectra];
				
				for (int scannumber=rawfile.getFirstSpectrumNumber(); scannumber<=rawfile.getLastSpectrumNumber(); ++scannumber)
				{
					HashMap<String,String> curstatuslog = rawfile.getStatusLogForScanNum(scannumber);
					curstatuslog.putAll(rawfile.getTrailerExtraForScanNum(scannumber));
					
					retentiontimes[scannumber-spectrum1] = rawfile.rtFromScanNum(scannumber);
					for (String key : curstatuslog.keySet())
					{
						String value = curstatuslog.get(key).trim();
						if (value==null || value.length()==0)
							continue;
						
						if (scannumber == spectrum1)
						{
							try {
								double dval = Double.parseDouble(value);
								double values[] = new double[nrspectra];
								values[0] = dval;
								for (int i=1; i<nrspectra; ++i)
									values[i] = 0;
								statuslog.put(key, values);
								if (!statusloglabels.contains(key))
									statusloglabels.add(key);
							} catch (Exception e) { System.out.println("EXCLUDING: " + key); }
						}
						else
						{
							try {
								double values[] = statuslog.get(key);
								if (values != null)
									values[scannumber-spectrum1] = Double.parseDouble(value);
							} catch (Exception e) { e.printStackTrace(); }
						}
					}
				}
			}
			
			// activate the first controller by default (always MS)
			IXRawfile.Controller controller = controllers.firstElement();
			if (controller != null)
				rawfile.setCurrentController(controller.getControllerType(), controller.getControllerNumber());
		}
		catch (IXRawfileException e)
		{
			e.printStackTrace();
			rawfile.dispose();
			MessageBox msg = new MessageBox(mainwnd.shell, SWT.OK|SWT.ICON_ERROR);
			msg.setMessage("Failed to retrieve the status-log entries for:\n'" + filename + "'");
			msg.open();
			return;
		}
		
		rawfile.dispose();
		rawfiles.add(new RawFile(filename, statuslog, controllers, retentiontimes));
	}
	
	public void removeFile(String filename)
	{
	}
	
	public Vector<RawFile> getFiles()
	{
		return rawfiles;
	}
	
	public int getActiveLog()
	{
		return activelog;
	}
	
	public void setActiveLog(int log)
	{
		activelog = log;
	}
	
	public Vector<String> getStatusLogLabels()
	{
		return statusloglabels;
	}
	
	public void setStatusLogLabel(String label)
	{
		statusloglabel = label;
	}
	
	public String getStatusLogLabel()
	{
		return statusloglabel;
	}
	
	
	// data
	protected MainWnd mainwnd;
	
	protected Vector<RawFile> rawfiles = new Vector<RawFile>();
	
	protected int activelog;
	
	protected String statusloglabel = "";
	protected Vector<String> statusloglabels = new Vector<String>();
}
