/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakmlviewer;


// java
import java.io.*;

import java.util.*;
import java.util.zip.*;

// libraries
import domsax.*;

// eclipse
import org.eclipse.swt.*;

import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// metabolome
import peakml.*;
import peakml.graphics.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;
import peakml.io.chemistry.*;

// timecourse
import peakmlviewer.util.*;
import peakmlviewer.action.*;
import peakmlviewer.dialog.*;





/**
 * 
 */
public class Document
{
	public static final int UPDATE_INIT				= 0;
	public static final int UPDATE_SETTINGS			= 1;
	public static final int UPDATE_ACTIONS			= 2;
	public static final int UPDATE_FILE_LOAD		= 3;
	public static final int UPDATE_INDEX_CHANGED	= 4;
	
	
	// constructor(s)
	public Document(MainWnd wnd)
	{
		// save the mainwnd pointer
		this.mainwnd = wnd;
	}
	
	public void init()
	{
		index = -1;
		data = null;
		filename = null;
		data_filtered = null;
		ipeaksort = null;
		
		actions.clear();
		
		try {
			if (Settings.databases.size()!=0 && database==null)
			{
				database = new HashMap<String,Molecule>();
				for (String file : Settings.databases)
					database.putAll(MoleculeIO.parseXml(new FileInputStream(file)));
			}
		} catch (Exception e) { e.printStackTrace(); }
		
		mainwnd.update(Document.UPDATE_INIT);
	}
	
	public void finish()
	{
	}
	
	
	// access
	@SuppressWarnings("unchecked")
	public void loadFile(final String filename) throws IOException, XmlParserException
	{
		// check whether the file exists
		File file = new File(filename);
		if (file.exists() == false)
			throw new IOException("File '" + filename + "' does not exist.");
		
		init();
		
		// save the filename
		this.index = 0;
		this.filename = file.getName();
		
		// load the data
		final ProgressDialog dlg_progress = new ProgressDialog(
				mainwnd.getShell(),
				"Loading data ...");
		
		// create the task
		Runnable task = new Runnable() {
			public void run()
			{
				ParserProgressListener listener = new ParserProgressListener() {
					public void update(double percentage)
					{
						dlg_progress.setProgress(percentage);
					}
				};
				
				try {
					data = PeakMLParser.parse(new FileInputStream(filename), listener, false);
				} catch (Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
				
				dlg_progress.dispose();
			}
		};
		
		// execute the task
		dlg_progress.setTask(task);
		dlg_progress.open();
		
		// if the header is missing
		data_peaks = (IPeakSet<IPeak>) data.measurement;
		if (data.header == null)
			data.header = new Header();
		
		// set the pattern id to 0
		for (IPeak peak : data_peaks)
			peak.setPatternID(0);
		
		// create nice colors for the sets
		setcolors.clear();
		
		int i = 0;
		int nrsets = data.header.getNrSetInfos()-1;
		Colormap colormap = new Colormap(Colormap.RAINBOW);
		for (SetInfo set : data.header.getSetInfos())
		{
			int index = (int) (((double) i++/nrsets) * (colormap.getNrColors()-1));
			
			setcolors.put(
					set.getID(),
					colormap.getColor(index)
				);
		}
	}
	
	public void saveAs(final String filename) throws IOException
	{
		// create the task
		final ProgressDialog dlg_progress = new ProgressDialog(
				mainwnd.getShell(),
				"Saving data ...");
		Runnable task = new Runnable() {
			public void run()
			{
				WriterProgressListener listener = new WriterProgressListener() {
					public void update(double percentage)
					{
						dlg_progress.setProgress(percentage);
					}
				};
				
				try {
					PeakMLWriter.write(
							data.header,
							data_filtered!=null ? data_filtered : data_peaks.getPeaks(),
							listener,
							new GZIPOutputStream(new FileOutputStream(filename)),
							""
						);
				} catch (Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
				
				dlg_progress.dispose();
			}
		};
		
		// execute the task
		dlg_progress.setTask(task);
		dlg_progress.open();
	}
	
	@SuppressWarnings("unchecked")
	public void exportCSV(final String filename) throws IOException
	{
		// unpack the peaks
		Vector<IPeak> unpacked = IPeak.unpack(getCurrentPeak());
		
		// determine the maximum size
		int maxsize = 0;
		Vector<PeakData> peakdatas = new Vector<PeakData>();
		for (IPeak peak : unpacked)
		{
			if (peak.getClass().equals(BackgroundIon.class))
			{
				PeakData<Peak> peakdata = ((BackgroundIon<Peak>) peak).getPeakData();
				peakdatas.add(peakdata);
				maxsize = Math.max(maxsize, peakdata.size());
			}
			else if (peak.getClass().equals(MassChromatogram.class))
			{
				PeakData<Peak> peakdata = ((MassChromatogram<Peak>) peak).getPeakData();
				peakdatas.add(peakdata);
				maxsize = Math.max(maxsize, peakdata.size());
			}
		}
		
		// create the matrix and init with the values
		double matrix[][] = new double[maxsize][3*unpacked.size()];
		for (int i=0; i<peakdatas.size(); ++i)
		{
			PeakData<Peak> peakdata = peakdatas.elementAt(i);
			for (int j=0; j<peakdata.size(); j++)
			{
				matrix[j][i*3 + 0] = peakdata.getRetentionTime(j);
				matrix[j][i*3 + 1] = peakdata.getMass(j);
				matrix[j][i*3 + 2] = peakdata.getIntensity(j);
			}
			for (int j=peakdata.size(); j<maxsize; ++j)
			{
				matrix[j][i*3 + 0] = -1;
				matrix[j][i*3 + 1] = -1;
				matrix[j][i*3 + 2] = -1;
			}
		}
		
		// write the matrix
		PrintStream out = new PrintStream(filename);
		
		// write the header
		for (IPeak peak : unpacked)
		{
			MeasurementInfo measurementinfo = data.header.getMeasurementInfo(peak.getMeasurementID());
			out.print(measurementinfo.getLabel() + ",,,");
		}
		out.println();
		for (int i=0; i<unpacked.size(); ++i)
			out.print("RT,MASS,INTENSITY,");
		out.println();
		
		// write the data
		for (int i=0; i<maxsize; ++i)
		{
			for (int j=0; j<unpacked.size(); ++j)
			{
				if (matrix[i][j*3] != -1)
					out.print(
							matrix[i][j*3 + 0] + "," +
							matrix[i][j*3 + 1] + "," +
							matrix[i][j*3 + 2] + ","
						);
				else
					out.print(",,,");
			}
			out.println();
		}
		out.flush();
		out.close();
	}
	
	public void exportChecked(final String filename) throws IOException
	{
		final Vector<IPeak> peaks = new Vector<IPeak>();
		for (IPeak peak : (data_filtered!=null ? data_filtered : data_peaks))
			if (peak.getPatternID()==1)
				peaks.add(peak);
		
		// create the task
		final ProgressDialog dlg_progress = new ProgressDialog(
				mainwnd.getShell(),
				"Saving data ...");
		Runnable task = new Runnable() {
			public void run()
			{
				WriterProgressListener listener = new WriterProgressListener() {
					public void update(double percentage)
					{
						dlg_progress.setProgress(percentage);
					}
				};
				
				try {
					PeakMLWriter.write(
							data.header,
							peaks,
							listener,
							new GZIPOutputStream(new FileOutputStream(filename)),
							""
						);
				} catch (Exception e) { System.out.println(e.getMessage()); e.printStackTrace(); }
				
				dlg_progress.dispose();
			}
		};
		
		// execute the task
		dlg_progress.setTask(task);
		dlg_progress.open();
	}
	
	public void selection(boolean select)
	{
		for (IPeak peak : data_filtered)
			peak.setPatternID(select==true ? 1 : 0);
		mainwnd.update(Document.UPDATE_FILE_LOAD);
	}
	
	public void invertSelection()
	{
		for (IPeak peak : data_filtered)
			peak.setPatternID(peak.getPatternID()==0 ? 1 : 0);
		mainwnd.update(Document.UPDATE_FILE_LOAD);
	}
	
	public void mergeSelected()
	{
		if (data_peaks == null)
			return;
		
		Vector<IPeak> all = data_peaks.getPeaks();
		Vector<IPeak> filtered = data_filtered;
		if (filtered == null)
			filtered = all;
		
		// grab the selection
		Vector<IPeak> unpacked = new Vector<IPeak>();
		Vector<IPeak> selection = new Vector<IPeak>();
		for (IPeak peak : data_filtered)
			if (peak.getPatternID() == 1)
			{
				unpacked.addAll(IPeak.unpack(peak));
				selection.add(peak);
			}
		if (selection.size() < 2)
		{
			MessageBox msg = new MessageBox(mainwnd.shell, SWT.OK|SWT.ICON_ERROR);
			msg.setMessage("Two or more peaks are needed for a merge.");
			msg.open();
			return;
		}
		
		// determine the index where to insert the merger
		int indx_all = all.indexOf(selection.firstElement());
		int indx_filtered = filtered.indexOf(selection.firstElement());
		
		// check whether we have duplicate measurements
		boolean duplicates = false;
		for (int i1=0; i1<unpacked.size(); ++i1)
		{
			int measurementid1 = unpacked.get(i1).getMeasurementID();
			for (int i2=i1+1; i2<unpacked.size(); ++i2)
			{
				System.out.println(measurementid1 + " == " + unpacked.get(i2).getMeasurementID());
				if (measurementid1 == unpacked.get(i2).getMeasurementID()) duplicates = true;
			}
		}
		
		if (duplicates == true)
		{
			MessageBox msg = new MessageBox(mainwnd.shell, SWT.OK|SWT.CANCEL|SWT.ICON_WARNING);
			msg.setMessage("There are duplicate measurements.\nDo you wish to continue?");
			if (msg.open() == SWT.CANCEL)
				return;
		}
		else
		{
			MessageBox msg = new MessageBox(mainwnd.shell, SWT.OK|SWT.CANCEL|SWT.ICON_INFORMATION);
			msg.setMessage("This operation cannot be reverted.\nDo you wish to continue?");
			if (msg.open() == SWT.CANCEL)
				return;
		}
		
		// merge into logical sets
		Vector<IPeak> peaks = new Vector<IPeak>();
		peaks.addAll(unpacked);
		IPeakSet<IPeak> peakset = new IPeakSet<IPeak>(peaks);
		
		for (IPeak selected : selection)
		{
			HashMap<String,Annotation> annotations = selected.getAnnotations();
			if (annotations == null)
				continue;
			for (Annotation annotation : annotations.values())
			{
				if (annotation.getLabel().equals(Annotation.relationid))
					continue;
				
				Annotation ps_annotation = peakset.getAnnotation(annotation.getLabel());
				if (ps_annotation != null)
					ps_annotation.setValue(ps_annotation.getValue() + ", " + annotation.getValue());
				else
					peakset.addAnnotation(annotation);
			}
		}
		
		if (selection.firstElement().getAnnotation(Annotation.relationid) != null)
			peakset.addAnnotation(selection.firstElement().getAnnotation(Annotation.relationid));
		
		all.removeAll(selection);
		all.add(indx_all, peakset);
		if (all != filtered)
		{
			filtered.removeAll(selection);
			filtered.add(indx_filtered, peakset);
		}
		mainwnd.update(Document.UPDATE_FILE_LOAD);
	}
	
	public HashMap<String,Molecule> getDatabase()
	{
		return database;
	}
	
	public String getFilename()
	{
		return filename;
	}
	
	public int getNrPeaks()
	{
		return data_peaks.size();
	}
	
	public int getTotalNrPeaks()
	{
		int size = 0;
		for (IPeak peak : getFilteredPeaks())
			size += getTotalNrPeaks(peak);
		return size;
	}
	@SuppressWarnings("unchecked")
	private int getTotalNrPeaks(IPeak peak)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			int size = 0;
			for (IPeak p : (IPeakSet<IPeak>) peak)
				size += getTotalNrPeaks(p);
			return size;
		}
		return 1;
	}
	
	public void setIPeakSort(Comparator<IPeak> sort)
	{
		ipeaksort = sort;
	}
	
	public Vector<Action> getActions()
	{
		return actions;
	}
	
	public Action getAction(int index)
	{
		if (index<0 || index>actions.size())
			throw new IndexOutOfBoundsException("Number of actions is " + actions.size());
		return actions.get(index);
	}
	
	public void addAction(Action action)
	{
		if (action != null)
		{
			data_filtered = null;
			actions.add(action);
		}
	}
	
	public void removeAction(Action action)
	{
		data_filtered = null;
		actions.remove(action);
	}
	
	public void removeActions(Vector<Action> actions)
	{
		data_filtered = null;
		this.actions.removeAll(actions);
	}
	
	public int getNrActions()
	{
		return actions.size();
	}
	
	public Vector<IPeak> getFilteredPeaks()
	{
		if (data == null)
			return new Vector<IPeak>();
		
		if (data_filtered == null)
		{
			data_filtered = data_peaks.getPeaks();
			for (Action action : actions)
				data_filtered = action.execute(data_filtered);
		}
		
		if (ipeaksort != null)
			Collections.sort(data_filtered, ipeaksort);
		
		return data_filtered;
	}
	
	public int getIndex()
	{
		return index;
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public IPeak getCurrentPeak()
	{
		Vector<IPeak> peaks = getFilteredPeaks();
		
		if (peaks==null || index<0 || index>=peaks.size())
			return null;
		return peaks.get(index);
	}
	
	/**
	 * Checks how many samples are collapsed into the given peak.
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static int getNrSamples(IPeak peak)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			int nrsamples = 0;
			for (IPeak p : (IPeakSet<IPeak>) peak)
				nrsamples += getNrSamples(p);
			return nrsamples;
		}
		
		return 1;
	}
	
	public Header getHeader()
	{
		if (data == null)
			return null;
		return data.header;
	}
	
	public int getNrTimePoints()
	{
		if (data == null)
			return 0;
		return data.header.getSetInfos().size();
	}
	
	public Vector<String> getSetNames()
	{
		Vector<String> sets = new Vector<String>();
		if (data!=null && data.header!=null)
		{
			for (SetInfo set : data.header.getSetInfos())
				sets.add(set.getID());
		}
		
		return sets;
	}
	
	public int getSetColor(String setname)
	{
		if (setcolors.get(setname) != null)
			return setcolors.get(setname);
		return 255;
	}
	
	public HashMap<String,Integer> getSetColors()
	{
		return setcolors;
	}
	
	public void setSetColor(String setname, RGB color)
	{
		setcolors.put(setname, ((color.red&255)<<16) | ((color.green&255)<<8) | (color.blue&255));
	}
	
	public Vector<String> getSetProfiles(String setname)
	{
		Vector<String> profiles = new Vector<String>();
		if (data!=null)
		{
			SetInfo set = data.header.getSetInfo(setname);
			if (set != null)
				for (int id : set.getAllMeasurementIDs())
					profiles.add(data.header.getMeasurementInfo(id).getLabel());
		}
		
		return profiles;
	}
	
	public String getSetName(IPeak peak)
	{
		if (data!=null && data.header!=null)
		{
			for (SetInfo set : data.header.getSetInfos())
			{
				if (set.containsMeasurementID(peak.getMeasurementID()))
					return set.getID();
			}
		}
		
		return Integer.toString(peak.getMeasurementID());
	}
	
	
	// data
	protected MainWnd mainwnd;
	
	protected String filename;
	protected ParseResult data;
	protected IPeakSet<IPeak> data_peaks;
	protected Vector<IPeak> data_filtered;
	protected int index;
	protected Comparator<IPeak> ipeaksort = null;
	
	protected HashMap<String,Integer> setcolors = new HashMap<String,Integer>();
	
	protected HashMap<String,Molecule> database = null;
	
	protected Vector<Action> actions = new Vector<Action>();
	
	protected String lastdirectory = null;
}
