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





/**
 * Collects information on a single application applied to the data.
 */
public class ApplicationInfo
{
	// constructor(s)
	/**
	 * Standard constructor, which fills the class-members with the given values. 
	 * 
	 * @param name				The name of the application.
	 * @param version			The version of the application.
	 * @param date				The date of running the application.
	 * @param parameters		The parameters used for the application.
	 */
	public ApplicationInfo(String name, String version, String date, String parameters)
	{
		this.name = name;
		this.version = version;
		this.date = date;
		this.parameters = parameters;
	}
	
	
	// access
	/**
	 * Returns the name of the application.
	 * 
	 * @return					The name of the application.
	 */
	public String getName()
	{
		return name;
	}
	
	/**
	 * Sets the name of the application.
	 * 
	 * @param name				The name of the application.
	 */
	public void setName(String name)
	{
		this.name = name;
	}
	
	/**
	 * Returns the version of the application.
	 * 
	 * @return					The version of the application.
	 */
	public String getVersion()
	{
		return version;
	}
	
	/**
	 * Sets the version of the application.
	 * 
	 * @param version			The version of the application.
	 */
	public void setVersion(String version)
	{
		this.version = version;
	}
	
	/**
	 * Returns the date of running the application.
	 * 
	 * @return					The date of running the application.
	 */
	public String getDate()
	{
		return date;
	}
	
	/**
	 * Sets the date of running the application.
	 * 
	 * @param date				The date of running the application.
	 */
	public void setDate(String date)
	{
		this.date = date;
	}
	
	/**
	 * Returns the parameters used for the application.
	 * 
	 * @return					The parameters used for the application.
	 */
	public String getParameters()
	{
		return parameters;
	}
	
	/**
	 * Sets the parameters used for the application.
	 * 
	 * @param parameters		The parameters used for the application.
	 */
	public void setParameters(String parameters)
	{
		this.parameters = parameters;
	}
	
	
	// data
	protected String name;
	protected String version;
	protected String date;
	protected String parameters;
}
