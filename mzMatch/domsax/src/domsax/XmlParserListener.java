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

//w3c.org
import org.w3c.dom.*;





/**
 * Listener interface for the XmlParser-class. This interface defines a single
 * method onDocument, which is called by the parser as soon as a sub-document
 * of the complete document has been completed. With this function the
 * application can receive the documents.
 * 
 * @author RA Scheltema
 */
public interface XmlParserListener
{
	// overloadables
	/**
	 * This method is called by the parser when it has completed a document.
	 * 
	 * @param document	The document.
	 * @param xpath		The xpath indicating the root-element within the complete document of the passed document.
	 */
	public void onDocument(Document document, String xpath) throws XmlParserException;
}
