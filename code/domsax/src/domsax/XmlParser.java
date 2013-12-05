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
import java.io.IOException;
import java.io.InputStream;

import java.util.Stack;
import java.util.Vector;
import java.util.StringTokenizer;

import javax.xml.parsers.*;

// xml.org
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

// w3c.org
import org.w3c.dom.*;

// invengine





/**
 * Class defining the SaxDom-parser.
 * 
 * <pre>
 * // setup the xml-parser
 * Listener l = new Listener();
 * XmlParser parser = new XmlParser();
 * 
 * // register the listener to the parser
 * parser.addListener(l, "/document-root/element");
 * 
 * // start parsing the file 'file.xml'
 * parser.parse(new FileInputStream("file.xml"));
 * </pre>
 * 
 * @author	RA Scheltema
 */
public class XmlParser extends DefaultHandler
{
	// constructor(s)
	/**
	 * Standard constructor, which initializes the state of the parser
	 * to its null-state. After this it is safe to call the function
	 * parse.
	 */
	public XmlParser()
	{
		listeners = new Vector<Listener>();
	}
	
	
	// 
	/**
	 * This call starts parsing the file. Before it is made, all the listeners
	 * need to have been added with calls to addListener. Based on the xpaths
	 * linked to the listeners the parser builds xml-documents, which are passed
	 * upon completion to the listeners.
	 * <p>
	 * This call completes when the whole file has been processed.
	 * 
	 * @param input		The input-stream to read from.
	 */
	public void parse(InputStream input) throws IOException, XmlParserException
	{
		try
		{
			// create the element-stack
			element_stack = new Stack<String>();
			
			// create the parser
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			
			// set the handler-plugins
			parser.getXMLReader().setContentHandler(this);
			
			// parse the input
			exception = null;
			parser.parse(input, this);
		}
		catch (IOException e)
		{
			throw e;
		}
		catch (Exception e)
		{
			if (exception != null)
				throw exception;
			else
				throw new XmlParserException(e);
		}
	}
	
	/**
	 * This method adds a listener plus an xpath leading to the element, which is
	 * the document-root of the repeating blocks the listener is listening for.
	 * 
	 * @param listener	Pointer to the listener to be added
	 * @param xpath		The xpath of the repeating block linked to the given listener.
	 */
	public void addListener(XmlParserListener listener, String xpath)
	{
		boolean add = true;
		for (Listener l : listeners)
		{
			// check the xpath
			if (l.xpath != xpath)
				continue;
			
			// it's already here, so no need to add it
			add = false;
			if (!l.listeners.contains(listener))
				l.listeners.add(listener);
		}
		
		if (add)
		{
			Listener l = new Listener();
			
			l.xpath = xpath;
			l.nodes = evaluate(xpath);
			l.document = null;
			l.current_element = null;
			
			l.listeners = new Vector<XmlParserListener>();
			l.listeners.add(listener);
			
			listeners.add(l);
		}
	}
	
	
	// DefaultHandler overloads
	@Override
	public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException
	{
		// up the stack
		element_stack.push(qName);
		
		// handle all the listeners
		for (Listener l : listeners)
		{
			// when the stack has not even grown to the size of this listener's node-list, don't continue
			if (l.nodes.size() > element_stack.size())
				continue;
			
			// check whether we need to create a new document
			if (l.document == null)
			{
				boolean append_to_document = true;
				for (int i=0; i<element_stack.size(); i++)
				{
					if (!l.nodes.get(i).equals(element_stack.get(i)))
					{
						append_to_document = false;
						break;
					}
				}
				try
				{
					if (append_to_document == false)
						continue;
					else
						l.document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
				}
				catch (Exception e) { ; } // will not occur
			}
				
			// create the element and add it to the document
			Element elem = l.document.createElementNS(namespaceURI, qName);
			
			if (l.current_element == null)
				l.document.appendChild(elem);
			else
				l.current_element.appendChild(elem);
			
			for (int i=0; i<atts.getLength(); i++)
				elem.setAttribute(atts.getQName(i), atts.getValue(i));
			
			l.current_element = elem;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length) throws SAXException
	{
		String str = new String(ch, start, length);
		if (str.equals("")/* || str.startsWith("\n")*/)
			return;
		
		// TODO this cleans up the string quite nicely, except that enters are removed with whitespaces tying words together
		// IDIOTIC. Why would you discard whitespace?
		//str = str.trim();
		for (Listener l : listeners)
		{
			if (l.document == null)
				continue;
			l.current_element.appendChild(l.document.createTextNode(SpecialToken.dereference(str)));
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException
	{
		try
		{
			// clean the stack of this element
			element_stack.pop();
			
			// handle all the listeners
			for (Listener l : listeners)
			{
				if (l.document == null)
					continue;
				
				if (element_stack.size() < l.nodes.size())
				{
					for (XmlParserListener listener : l.listeners)
						listener.onDocument(l.document, l.xpath);
					
					l.current_element = null;
					l.document = null;
				}
				else
				{
					// point the current-element pointer to the 
					Node parent = l.current_element.getParentNode();
					
					if (parent == l.document)
						l.current_element = null;
					else
						l.current_element = (Element) parent;
				}
			}
		}
		catch (XmlParserException e)
		{
			exception = e;
			throw new SAXException();
		}
	}
	
	
	// data
	/** The element-stack used to keep track of the elements currently processed */
	private Stack<String> element_stack;
	/** List with the listeners */
	private Vector<Listener> listeners;
	/** */
	private XmlParserException exception;
	
	/** Class containing the data for a xpath and all the listeners linked to it  */
	private class Listener
	{
		/** The xpath leading to the element defining the repeating block */
		public String xpath;
		/** The separate elements leading to the start of the repeating block */
		public Vector<String> nodes;
		/** The list with listeners linked to the xpath */
		public Vector<XmlParserListener> listeners;
		
		/** The document created for the listeners, with document-root the starting element of the repeating block */
		public Document document;
		/** The current element within the document */
		public Element current_element;
	}
	
	private static Vector<String> evaluate(String xpath)
	{
		Vector<String> nodes = new Vector<String>();
		
		StringTokenizer tokenizer = new StringTokenizer(xpath, "/.@");
		while (tokenizer.hasMoreTokens())
			nodes.add(tokenizer.nextToken());
		
		return nodes;
	}
}
