/* Copyright (C) 2006, RA Scheltema
 * This file is part of DomSax.
 * 
 * DomSax is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * DomSax is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with DomSax; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package domsax;


//java





/**
 * This object contains the version of the project.
 * 
 * @author RA Scheltema
 */
public class Version
{
	/**
	 * This method converts the static version to a string. 
	 * 
	 * @return The string representation of the version.
	 */
	public static String convertToString()
	{
		return "" + major + "." + minor + "." + maintenance;
	}

	
	/** The major part of the version, which is changed for major interface changes */
	public static int major = 1;
	/** The minor part of the version, which is changed when interfaces have been added */
	public static int minor = 1;
	/** The maintenance part of the version, which is changed for bug-fixes */
	public static int maintenance = 0;
}
