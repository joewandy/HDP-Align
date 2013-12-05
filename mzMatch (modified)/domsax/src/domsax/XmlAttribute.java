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


// java





/**
 * This class describes a single attribute for an element. It is provided as
 * a convenience for the XmlWriter, because the standard Attribute-class of
 * the DOM implementation is too cumbersome to make it clean to use. Especially
 * for the attribute-lists (...) used in the writer the DOM implementation
 * wreaks havoc on the code.
 * <p>
 * The class basically combines a name with the value and provides convenient
 * constructors and access-functions to this data.
 * 
 * @author RA Scheltema
 */
public class XmlAttribute
{
	// constructor(s)
	/**
	 * Standard constructor, which sets the name and the value to empty
	 * strings.
	 */
	public XmlAttribute()
	{
		this.name = "";
		this.value = "";
	}

	/**
	 * This constructor sets the name and the value to the given parameters.
	 * After this the complete attribute is initialized.
	 * 
	 * @param name		The name of the attribute
	 * @param value		The value of the attribute
	 */
	public XmlAttribute(String name, String value)
	{
		this.name = name;
		this.value = value;
	}

	// access
	/**
	 * Returns the name of the attribute.
	 * 
	 * @return			The name of the attribute.
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the name of the attribute.
	 * 
	 * @param name		The new name for the attribute.
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the value of the attribute.
	 * 
	 * @return			The value of the attribute.
	 */
	public String getValue()
	{
		return value;
	}

	/**
	 * Sets the value of the attribute.
	 * 
	 * @param value		The new value of the attribute.
	 */
	public void setValue(String value)
	{
		this.value = value;
	}


	// member variables
	/** The name of the attribute */
	private String name;
	/** The value of the attribute */
	private String value;
}
