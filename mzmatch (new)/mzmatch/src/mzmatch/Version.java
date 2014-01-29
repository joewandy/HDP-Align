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



package mzmatch;


// java





/**
 * This object contains the version of the project.
 */
public class Version
{
	public static enum Status
	{
		DEBUG,
		RELEASE
	}
	
	/** Indicates the status of the library. When set to {@link Status#DEBUG} information is printed to the standard error about errors. */
	public static final Status status = Status.DEBUG;
	
	/** The major part of the version, which is changed for major interface changes */
	public static final int major = 1;
	/** The minor part of the version, which is changed when interfaces have been added */
	public static final int minor = 0;
	/** The maintenance part of the version, which is changed for bug-fixes */
	public static final int maintenance = 2;
	
	
	// access
	/**
	 * This method converts the static version to a string. 
	 * 
	 * @return The string representation of the version.
	 */
	public static String convertToString()
	{
		return major + "." + minor + "." + maintenance;
	}
}
