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



package peakml.io;


// java
import java.util.*;

// peakml
import peakml.*;





/**
 * Central point for collecting all information about a measurement. For this a collection
 * of {@link FileInfo} instances is maintained, describing all the files associated to the
 * measurement. Furthermore, a list with {@link ScanInfo} instances is maintained, describing
 * all the scans. 
 * <p />
 * For this class it has been chosen to stick to integer id's, because each {@link IPeak}
 * descendants maintain this id. A string representation would be too expensive in terms
 * of memory consumption.
 */
public class MeasurementInfo extends Annotatable
{
	// constructor(s)
	/**
	 * Constructs a new measurement information with the given id of the measurement and
	 * the given id of the sample.
	 * 
	 * @param id			The id of the measurement.
	 * @param sampleid		The id of the associated sample.
	 */
	public MeasurementInfo(int id, String sampleid)
	{
		this.id = id;
		this.sampleid = sampleid;
		this.label = sampleid;
	}
	
	/**
	 * Copy-constructor which copies the contents of the given measurement information
	 * into this instance. The given id is used as the id of this measurement.
	 * 
	 * @param id			The id of the measurement.
	 * @param other			The measurement information to be copied.
	 */
	public MeasurementInfo(int id, MeasurementInfo other)
	{
		this.id = id;
		
		this.label = other.label;
		this.sampleid = other.sampleid;
		this.addScanInfos(other.scaninfos);
		this.addFileInfos(other.fileinfos);
	}
	
	
	// access
	/**
	 * Returns the id of the measurement. This id links to all the ipeak-data associated
	 * with this measurement (the method {@link IPeak#getMeasurementID()} returns the
	 * id) and can be used to locate the measurement info instance in the {@link Header}.
	 * 
	 * @return				The id of the measurement.
	 */
	public int getID()
	{
		return id;
	}
	
	/**
	 * Returns the id of the sample associated to the measurement. This id links to
	 * the associated sample info (the method {@link Header#getSampleInfo(String)} returns
	 * the {@link SampleInfo}}.
	 * 
	 * @return				The sample info linked to this measurement.
	 */
	public String getSampleID()
	{
		return sampleid;
	}
	
	/**
	 * Sets the optional label for the measurement. This label is used for display purposes
	 * in user interface environments.
	 * 
	 * @param label			The new label.
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	/**
	 * Returns the optional label for the measurement. This label is used for display purposes
	 * in user interface environments.
	 * 
	 * @return				The optional label for the measurement.
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Convenience function for user interface applications to keep track off whether to show
	 * this measurement or not.
	 * 
	 * @param b					Boolean indicating show or not.
	 */
	public void setVisible(boolean b)
	{
		visible = b;
	}
	
	/**
	 * For more information see {@link MeasurementInfo#setVisible(boolean)}.
	 * 
	 * @return					Boolean indicating show or not.
	 */
	public boolean getVisible()
	{
		return visible;
	}
	
	
	// file description access
	/**
	 * Adds the file info instance to the list of file infos. No checking is performed to ensure
	 * that the file info is not already in the list.
	 * 
	 * @param file			The new file info instance.
	 */
	public void addFileInfo(FileInfo file)
	{
		fileinfos.add(file);
	}
	
	/**
	 * Adds the collection of file infos to the list of file infos.
	 * 
	 * @param files			The collection of file infos.
	 */
	public void addFileInfos(Collection<FileInfo> files)
	{
		for (FileInfo file : files)
			fileinfos.add(file);
	}
	
	/**
	 * Returns the file info instance at the given index.
	 * 
	 * @param index			The index of the file info to be retrieved.
	 * @return				The file info instance.
	 * @throws IndexOutOfBoundsException
	 * 						Thrown when the given index is not within the bounds of the list.
	 */
	public FileInfo getFileInfo(int index) throws IndexOutOfBoundsException
	{
		return fileinfos.get(index);
	}
	
	/**
	 * Returns all the file info instances maintained in this class.
	 * 
	 * @return				The file info instances.
	 */
	public Vector<FileInfo> getFileInfos()
	{
		return fileinfos;
	}
	
	/**
	 * Returns the number of file info instances maintained within this class.
	 * 
	 * @return				The number of file info instances.
	 */
	public int getNrFileInfos()
	{
		return fileinfos.size();
	}
	
	
	// scan description access
	/**
	 * Adds the scan info instance to the list of scan infos. No checking is performed to ensure
	 * that the scan info is not already in the list.
	 * 
	 * @param scan			The new scan info instance.
	 */
	public void addScanInfo(ScanInfo scan)
	{
		scaninfos.add(scan);
	}
	
	/**
	 * Adds the collection of scan infos to the list of scan infos.
	 * 
	 * @param scans			The collection of scan infos.
	 */
	public void addScanInfos(Collection<ScanInfo> scans)
	{
		for (ScanInfo scan : scans)
			scaninfos.add(scan);
	}
	
	/**
	 * Returns the scan info instance at the given index.
	 * 
	 * @param index			The index of the scan info to be retrieved.
	 * @return				The scan info instance.
	 * @throws IndexOutOfBoundsException
	 * 						Thrown when the given index is not within the bounds of the list.
	 */
	public ScanInfo getScanInfo(int index) throws IndexOutOfBoundsException
	{
		return scaninfos.get(index);
	}
	
	/**
	 * Returns all the scan info instances maintained in this class.
	 * 
	 * @return				The scan info instances.
	 */
	public Vector<ScanInfo> getScanInfos()
	{
		return scaninfos;
	}
	
	/**
	 * Returns the number of scan info instances maintained within this class.
	 * 
	 * @return				The number of scan info instances.
	 */
	public int getNrScanInfos()
	{
		return scaninfos.size();
	}
	
	public double getMinRetentionTime()
	{
		ScanInfo scan = scaninfos.firstElement();
		if (scan == null)
			return -1;
		return scan.retentiontime;
	}
	
	public double getMaxRetentionTime()
	{
		ScanInfo scan = scaninfos.lastElement();
		if (scan == null)
			return -1;
		return scan.retentiontime;
	}
	
	
	// data
	protected int id;
	protected String label = null;
	protected String sampleid = null;
	protected boolean visible = true;
	protected Vector<ScanInfo> scaninfos = new Vector<ScanInfo>();
	protected Vector<FileInfo> fileinfos = new Vector<FileInfo>();
}
