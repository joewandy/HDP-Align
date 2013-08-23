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

// peakml
import peakml.*;





/**
 * Central point for storing all information about a sample. The implementation only
 * supports an id and an optional label. Remaining information can be stored with the
 * controlled vocabulary and the annotations.
 */
public class SampleInfo extends Annotatable
{
	// constructor(s)
	/**
	 * Standard constructor, which sets the obligatory id-value for the sample.
	 * The label is set to null.
	 * 
	 * @param id		The id of the sample.
	 */
	public SampleInfo(String id)
	{
		this(id, null);
	}
	
	/**
	 * Standard constructor, which sets the obligatory id-value for the sample
	 * and the name.
	 * 
	 * @param id		The id of the sample.
	 * @param label		The label of the sample.
	 */
	public SampleInfo(String id, String label)
	{
		this.id = id;
		this.label = label;
	}
	
	
	// access
	/**
	 * Returns the id of the sample.
	 * 
	 * @return			The id of the sample.
	 */
	public String getID()
	{
		return id;
	}
	
	/**
	 * Sets the id of the sample.
	 * 
	 * @param id		The id of the sample.
	 */
	public void setID(String id)
	{
		this.id = id;
	}
	
	/**
	 * Returns the label of the sample.
	 * 
	 * @return			The label of the sample.
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Sets the label of the sample.
	 * 
	 * @param label		The label of the sample.
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	
	// data
	protected String id;
	protected String label;
}
