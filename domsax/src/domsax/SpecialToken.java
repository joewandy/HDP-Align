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
 * ...
 * 
 * @author RA Scheltema
 */
public class SpecialToken
{
	// static interface
	/**
	 * 
	 */
	public static final SpecialToken tokens[] =
	{
		// this one needs to be first for the reference-function (otherwise the other references will contaminate)
		new SpecialToken("&",	"&amp;"),
		// the remaining references
		new SpecialToken("\'",	"&apos;"),
		new SpecialToken("\"",	"&quot;"),
		new SpecialToken("<",	"&lt;"),
		new SpecialToken(">",	"&gt;"),
	};
	
	/**
	 * 
	 */
	public static String reference(String text)
	{
		StringBuilder builder = new StringBuilder(text);
		
		for (int i=0; i<tokens.length; ++i)
		{
			int index = 0;
			while (true)
			{
				index = builder.indexOf(tokens[i].token, index);
				if (index == -1)
					break;
				
				builder.deleteCharAt(index);
				builder.insert(index, tokens[i].reference);
				
				index++;
			}
		}
		
		return builder.toString();
	}
	
	/**
	 * 
	 */
	public static String dereference(String text)
	{
		StringBuilder builder = new StringBuilder(text);
		
		for (int i=0; i<tokens.length; ++i)
		{
			int index = 0;
			while (true)
			{
				index = builder.indexOf(tokens[i].reference, index);
				if (index == -1)
					break;
				
				builder.delete(index, index + tokens[i].reference.length());
				builder.insert(index, tokens[i].token);
				
				index++;
			}
		}
		
		return builder.toString();
	}
	
	
	
	// constructor(s)
	/**
	 * 
	 */
	public SpecialToken(String token, String reference)
	{
		this.token = token;
		this.reference = reference;
	}
	
	
	// data
	/** */
	protected String token;
	/** */
	protected String reference;
}


