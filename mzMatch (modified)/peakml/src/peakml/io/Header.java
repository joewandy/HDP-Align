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
 * Central header class, collecting all information about the header stored in a mass
 * spectrometry related file. It acts as a repository for instances of {@link SetInfo},
 * {@link MeasurementInfo}, {@link FileInfo}, and {@link SampleInfo}. For keeping track
 * of this information convenience functions are provided. Additionally, the class tracks
 * the number of peaks, owner, and annotation at the file level.
 */
public class Header extends Annotatable
{
	// constructor(s)
	/**
	 * Constructs a new, empty instance of the header class.
	 */
	public Header()
	{
	}
	
	
	// general access
	/**
	 * Returns the number of peaks associated with this header. This value is mostly used
	 * for sending progress information with the classes {@link ParserProgressListener} and
	 * {@link WriterProgressListener}.
	 * 
	 * @return				The number of peaks.
	 */
	public int getNrPeaks()
	{
		return nrpeaks;
	}
	
	/**
	 * Sets the number of peaks associated with this header. See {@link Header#getNrPeaks()}
	 * for more information.
	 * 
	 * @param nrpeaks		The number of peaks.
	 */
	public void setNrPeaks(int nrpeaks)
	{
		this.nrpeaks = nrpeaks;
	}
	
	/**
	 * Returns the date at which the file was created.
	 * 
	 * @return				The date at which the file was created.
	 */
	public Date getDate()
	{
		return date;
	}
	
	/**
	 * Returns the date at which the file was created in string format.
	 * See {@link Header#setDate(Date)} for a description of the format used.
	 * 
	 * @return				The date as string.
	 */
	public String getDateAsString()
	{
		return dateformat.format(date);
	}
	
	/**
	 * Sets the new date of the file to the given value.
	 * 
	 * @param date			The new date at which the file was created.
	 */
	public void setDate(Date date)
	{
		this.date = date;
	}
	
	/**
	 * Sets the new date of the file to the given value. The string represention
	 * of the date needs to be in the US (long) locale, which means it needs to
	 * be in the format 'September 12, 2008'. This locale is also used in the
	 * file format for storing dates and numbers.
	 * <p />
	 * When the string representation is not in the US locale a ParseException
	 * is thrown.
	 * 
	 * @param date			String representation of the new date
	 * @throws ParseException
	 * 						Thrown when the string is not encoded in the US (long) locale
	 */
	public void setDate(String date) throws java.text.ParseException
	{
		this.date = dateformat.parse(date);
	}
	
	/**
	 * Returns the owner of the file.
	 * 
	 * @return				The owner of the PeakML file.
	 */
	public String getOwner()
	{
		return owner;
	}
	
	/**
	 * Sets the new owner of the file.
	 * 
	 * @param owner			The new owner of the PeakML file.
	 */
	public void setOwner(String owner)
	{
		this.owner = owner;
	}
	
	/**
	 * Returns an optional description for the information stored in the file.
	 * 
	 * @return				Optional description of the file contents.
	 */
	public String getDescription()
	{
		return description;
	}
	
	/**
	 * Sets an optional description for the information stored in the file.
	 * 
	 * @param description	Optional description of the file contents.
	 */
	public void setDescription(String description)
	{
		this.description = description;
	}
	
	
	// vocabulary access
	/**
	 * Returns the vocabulary of the given namespace. When the namespace has not been
	 * registered with the header null is returned.
	 * 
	 * @param namespace		The namespace of the vocabulary to be retrieved.
	 * @return				The vocabulary with the given namespace.
	 */
	public Vocabulary getVocabulary(String namespace)
	{
		for (Vocabulary vocabulary : vocabularies)
			if (vocabulary.getDefaultNamespace().equals(namespace)) return vocabulary;
		return null;
	}
	
	/**
	 * Returns the number of vocabularies associated with the header.
	 * 
	 * @return				The number of vocabularies associated with the header.
	 */
	public int getNrVocabularies()
	{
		return vocabularies.size();
	}
	
	/**
	 * Returns a list of all vocabularies associated with the header.
	 * 
	 * @return				The vocabularies associated with the header.
	 */
	public Vector<Vocabulary> getVocabularies()
	{
		return vocabularies;
	}
	
	/**
	 * Adds the given vocabulary to the list of vocabularies associated with the header. The
	 * given vocabulary is registered with its namespace (can be retrieved
	 * {@link Vocabulary#getDefaultNamespace()}). If the namespace has already been registered
	 * the old vocabulary is overwritten.
	 * 
	 * @param vocabulary	The vocabulary to be added to the header.
	 */
	public void addVocabulary(Vocabulary vocabulary)
	{
		Vocabulary old = getVocabulary(vocabulary.getDefaultNamespace());
		if (old != null)
			vocabularies.remove(old);
		vocabularies.add(vocabulary);
	}
	
	
	// sample description access
	/**
	 * Returns the sample information with the given id. When no such id is registered with
	 * the header null is returned.
	 * 
	 * @param id			The id of the sample information.
	 * @return				The sample information.
	 */
	public SampleInfo getSampleInfo(String id)
	{
		for (SampleInfo sampleinfo : sampleinfos)
			if (sampleinfo.getID().equals(id)) return sampleinfo;
		return null;
	}
	
	/**
	 * Returns the number of sample information instances associated with the header.
	 * 
	 * @return				The number of sample information instances.
	 */
	public int getNrSampleInfos()
	{
		return sampleinfos.size();
	}
	
	/**
	 * Returns a list of all the sample information instances associated with the header.
	 * 
	 * @return				A list of all the sample information instances.
	 */
	public Vector<SampleInfo> getSampleInfos()
	{
		return sampleinfos;
	}
	
	/**
	 * Adds the given sample information instance to the header. When the id of the sample
	 * information ({@link SampleInfo#getID()}) has already been registered, the old instance
	 * is overwritten.
	 * 
	 * @param sample		The sample information to be stored.
	 */
	public void addSampleInfo(SampleInfo sample)
	{
		SampleInfo old = getSampleInfo(sample.getID());
		if (old != null)
			sampleinfos.remove(old);
		sampleinfos.add(sample);
	}
	
	
	// measurement description access
	/**
	 * Returns the measurement information with the given id.
	 * 
	 * @param id			The id of the measurement information to be retrieved.
	 */
	public MeasurementInfo getMeasurementInfo(int id)
	{
		for (MeasurementInfo measurement : measurementinfos)
			if (measurement.getID() == id) return measurement;
		return null;
	}
	
	/**
	 * Returns the index of the measurement information with the given id. When the id cannot
	 * be found -1 is returned.
	 * 
	 * @param id			The id of the measurement information.
	 * @return				The index of the measurement information with the given id.
	 */
	public int indexOfMeasurementInfo(int id)
	{
		for (int i=0; i<measurementinfos.size(); ++i)
			if (measurementinfos.get(i).getID() == id) return i;
		return -1;
	}
	
	/**
	 * Returns the index of the measurement information with the given label. When the label cannot
	 * be found -1 is returned.
	 * 
	 * @param label			The label of the measurement information.
	 * @return				The index of the measurement information with the given label.
	 */
	public int indexOfMeasurementInfo(String label)
	{
		for (int i=0; i<measurementinfos.size(); ++i)
			if (label.equals(measurementinfos.get(i).getLabel())) return i;
		return -1;
	}
	
	/**
	 * Returns the number of measurement informations associated with the header.
	 * 
	 * @return				The index of the measurement information.
	 */
	public int getNrMeasurementInfos()
	{
		return measurementinfos.size();
	}
	
	/**
	 * Returns the list of measurement informations associated with the header.
	 * 
	 * @return				The list of measurement informations associated with the header.
	 */
	public Vector<MeasurementInfo> getMeasurementInfos()
	{
		return measurementinfos;
	}
	
	/**
	 * Returns the list of measurement informations associated to the given set.
	 */
	public Vector<MeasurementInfo> getMeasurementInfos(SetInfo set)
	{
		Vector<MeasurementInfo> infos = new Vector<MeasurementInfo>();
		for (Integer id : set.measurementids)
			infos.add(getMeasurementInfo(id));
		return infos;
	}
	
	/**
	 * Adds the given measurement information to the header. When the id of the measurement
	 * information has already been registered the old instance is overwritten.
	 * 
	 * @param measurement	The measurement information to be added.
	 */
	public void addMeasurementInfo(MeasurementInfo measurement)
	{
		MeasurementInfo old = getMeasurementInfo(measurement.getID());
		if (old != null)
			measurementinfos.remove(old);
		measurementinfos.add(measurement);
	}
	
	/**
	 * Adds the given measurement informations to the header. When the id of a measurement
	 * information has already been registered the old instance is overwritten.
	 * 
	 * @param measurements	The measurement informations to be added.
	 */
	public void addMeasurementInfos(Collection<MeasurementInfo> measurements)
	{
		for (MeasurementInfo measurement : measurements)
			addMeasurementInfo(measurement);
	}
	
	
	// set access
	/**
	 * Adds the given set information to the header. When the id of a set
	 * information has already been registered the old instance is overwritten.
	 * 
	 * @param set			The set information to be added.
	 */
	public void addSetInfo(SetInfo set)
	{
		SetInfo old = getSetInfo(set.getID());
		if (old != null)
			setinfos.remove(old);
		setinfos.add(set);
	}
	
	/**
	 * Returns the set information with the given id.
	 * 
	 * @return				The set information with the given id.
	 */
	public SetInfo getSetInfo(String id)
	{
		for (SetInfo set : setinfos)
			if (set.getID().equals(id)) return set;
		return null;
	}
	
	/**
	 * Returns the number of set informations stored in the header.
	 * 
	 * @return				The number of set informations.
	 */
	public int getNrSetInfos()
	{
		return setinfos.size();
	}
	
	/**
	 * Returns all the set informations stored in the header.
	 * 
	 * @return				The set informations.
	 */
	public Vector<SetInfo> getSetInfos()
	{
		return setinfos;
	}
	
	/**
	 * Returns the set information which contains the given measurement id.
	 * 
	 * @return				The set information containing the measurement id.
	 */
	public SetInfo getSetInfoForMeasurementID(int measurementid)
	{
		for (SetInfo set : setinfos)
		{
			if (set.containsMeasurementID(measurementid))
				return set;
		}
		return null;
	}
	
	/**
	 * Returns the index of the set information with the given id.
	 * 
	 * @return				The set information with the given id.
	 */
	public int indexOfSetInfo(String id)
	{
		for (int i=0; i<setinfos.size(); ++i)
			if (setinfos.get(i).getID().equals(id)) return i;
		return -1;
	}
	
	/**
	 * Returns the index of the set information with the given id.
	 * 
	 * @return				The set information with the given id.
	 */
	public int indexOfSetInfo(SetInfo setinfo)
	{
		return indexOfSetInfo(setinfo.id);
	}
	
	/**
	 * Adds the given set informations to the header. When the id of a set
	 * information has already been registered the old instance is overwritten.
	 * 
	 * @param sets			The set informations to be added.
	 */
	public void addSetInfos(Collection<SetInfo> sets)
	{
		for (SetInfo set : sets)
			addSetInfo(set);
	}
	
	
	// application access
	/**
	 * Adds the given application information to the header. When the id of a application
	 * information has already been registered the old instance is overwritten.
	 * 
	 * @param application		The application information to be added.
	 */
	public void addApplicationInfo(ApplicationInfo application)
	{
		applicationinfos.add(application);
	}
	
	/**
	 * Returns the application information with the given id.
	 * 
	 * @return				The application information at the given index.
	 */
	public ApplicationInfo getApplicationInfo(int index)
	{
		return applicationinfos.get(index);
	}
	
	/**
	 * Returns the vector with all the application information instances.
	 * 
	 * @return				The vector with all application information instances.
	 */
	public Vector<ApplicationInfo> getApplicationInfos()
	{
		return applicationinfos;
	}
	
	/**
	 * Returns the number of application information instances stored in the header.
	 * 
	 * @return				The number of application information instances.
	 */
	public int getNrApplicationInfos()
	{
		return applicationinfos.size();
	}
	
	
	// data
	protected int nrpeaks = -1;
	protected Date date = new Date();
	protected String owner = null;
	protected String description = null;
	
	protected Vector<SetInfo> setinfos = new Vector<SetInfo>();
	protected Vector<SampleInfo> sampleinfos = new Vector<SampleInfo>();
	protected Vector<Vocabulary> vocabularies = new Vector<Vocabulary>();
	protected Vector<MeasurementInfo> measurementinfos = new Vector<MeasurementInfo>();
	protected Vector<ApplicationInfo> applicationinfos = new Vector<ApplicationInfo>();
	
	protected static final java.text.DateFormat dateformat = java.text.DateFormat.getDateInstance(java.text.DateFormat.LONG, new Locale("en","US"));
}
