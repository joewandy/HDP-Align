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

// w3c.org
import org.w3c.dom.*;





/**
 * 
 */
public class XmlTools
{
	public static Node getNodeByTagName(Document document, String tag)
	{
		NodeList nodes = document.getElementsByTagName(tag);
		if (nodes.getLength() != 1)
			return null;
		return nodes.item(0);
	}
	
	public static Node getNodeByTagName(Node node, String tag)
	{
		// retrieve all the children
		NodeList nodes = node.getChildNodes();
		
		// try to find it in the current list
		for (int i=0; i<nodes.getLength(); ++i)
		{
			Node currentnode = nodes.item(i);
			if (currentnode.getNodeName().equals(tag))
				return currentnode;
		}
		
		// couldn't find it, recurse
		for (int i=0; i<nodes.getLength(); ++i)
		{
			Node currentnode = nodes.item(i);
			Node resultnode = getNodeByTagName(currentnode, tag);
			if (resultnode != null)
				return resultnode;
		}
		
		return null;
	}
	
	/**
	 * Recursively tracks down all the elements with the given tag name.
	 * 
	 * @param document
	 * @param tag
	 * @return
	 */
	public static NodeList getNodesByTagName(Document document, String tag)
	{
		return document.getElementsByTagName(tag);
	}
	
	/**
	 * Tracks down all childeren of the given node with the give tag name. This
	 * method does not recurse into deeper levels.
	 * 
	 * @param parent
	 * @param tag
	 * @return
	 */
	public static NodeList getNodesByTagName(Node parent, String tag)
	{
		XmlNodeList list = new XmlNodeList();
		
		// retrieve all the children
		NodeList nodes = parent.getChildNodes();
		
		// try to find it in the current list
		for (int i=0; i<nodes.getLength(); ++i)
		{
			Node currentnode = nodes.item(i);
			if (currentnode.getNodeName().equals(tag))
				list.add(currentnode);
		}
		
		return list;
	}
}
