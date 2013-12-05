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
import java.io.*;

// peakml
import peakml.*;





/**
 * Central point for collecting all the information about a file. Files in peakml
 * are either the raw input files or the intermediate files, which have been processed.
 * The vocabulary and the annotations can be used to store the data about the processing
 * done on the file.
 */
public class FileInfo extends Annotatable
{
	// constructor(s)
	/**
	 * Convenience constructor, which looks up the location of the given filename in
	 * the file-system. The filename must either be as a complete path or relative
	 * from the current position.
	 * 
	 * @param label				Optional label for the file.
	 * @param filename			The filename (including the complete path).
	 */
	public FileInfo(String label, String filename)
	{
		File file = new File(filename);
		
		this.label = label;
		this.name = file.getName();
		this.location = file.getParent();
	}
	
	/**
	 * Constructs a new file info instance with the given values.
	 * 
	 * @param label				Optional label for the file.
	 * @param name				The filename (excluding the complete path).
	 * @param location			The location of the file.
	 */
	public FileInfo(String label, String name, String location)
	{
		this.label = label;
		this.name = name;
		this.location = location;
	}
	
	
	// access
	/**
	 * Returns the optional label for the file.
	 * 
	 * @return					Optional label for the file.
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Sets the optional label for the file.
	 * 
	 * @param label				Optional label for the file.
	 */
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	/**
	 * Returns the filename excluding the complete path, which can be retrieved with
	 * {@link FileInfo#getLocation()}.
	 * 
	 * @return					The filename.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the filename excluding the complete path of the file.
	 * 
	 * @param name				The filename.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns the complete path to the file.
	 * 
	 * @return					The complete path to the file.
	 */
	public String getLocation()
	{
		return location;
	}
	
	/**
	 * Sets the complete path to the file.
	 * 
	 * @param location			The complete path to the file.
	 */
	public void setLocation(String location)
	{
		this.location = location;
	}
	
	
	// data
	protected String label;
	protected String name;
	protected String location;
}
