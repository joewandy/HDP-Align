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
import java.util.*;

// w3c.org
import org.w3c.dom.*;





/**
* 
*/
public class XmlNodeList implements NodeList
{
	// constructor(s)
	
	
	// access
	public void add(Node node)
	{
		nodes.add(node);
	}
	
	
	// NodeList interface
	public int getLength()
	{
		return nodes.size();
	}

	public Node item(int index)
	{
		return nodes.elementAt(index);
	}
	
	
	// data
	protected Vector<Node> nodes = new Vector<Node>();
}
