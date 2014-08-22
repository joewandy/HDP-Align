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
import java.io.*;
import java.util.*;

// w3c.org
import org.w3c.dom.*;





/**
 * This class provides the functionality for writing well-formed XML. It keeps
 * the indentation intact for elements and data and provides a complete interface
 * for writing code exporting data to XML.
 * 
 * @author RA Scheltema
 */
public class XmlWriter
{
	/**
	 * This enumeration is used for the header of the xml-file and specifically
	 * to indicate which encoding is to be used for parsing the file.
	 */
	public enum Encoding
	{
		/** */
		UTF_8,
		/** */
		UTF_16,
		/** */
		ISO_8859_1
	};
	
	/**
	 * This enumeration is used for the writeTag family of calls. The defined
	 * values indicate whether the tag should be an open-tag, close-tag or simply
	 * a single-tag (eg no data inside and the open- and close-tag are merged
	 * into one).
	 */
	public enum Tag
	{
		/** Symbolizes the open-tag for an element (eg <element>) */
		OPEN,
		/** Symbolizes the closing-tag for an element (eg </element>) */
		CLOSE,
		/** Symbolizes a single tag for an element (eg <element />) */
		SINGLE
	};


	
	// constructor(s)
	public XmlWriter(OutputStream out) throws IOException
	{
		this(new OutputStreamWriter(out));
	}
	
	public XmlWriter(OutputStream out, String stylesheet) throws IOException
	{
		this(new OutputStreamWriter(out), stylesheet);
	}
	
	/**
	 * The standard constructor. This sets the writer for the the XmlWriter to
	 * put its data to. The encoding used is UTF-8.
	 * 
	 * @param out			The output to write to.
	 * @throws NullPointerException
	 * 						When the argument out is set to null.
	 * @throws IOException	When the writer throws this exception upon writing the header.
	 */
	public XmlWriter(Writer out) throws NullPointerException, IOException
	{
		this(out, Encoding.UTF_8);
	}
	
	/**
	 * 
	 * @param out
	 * @param stylesheet
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public XmlWriter(Writer out, String stylesheet) throws NullPointerException, IOException
	{
		this(out, Encoding.UTF_8, stylesheet);
	}
	
	/**
	 * The standard constructor. This sets the writer for the the XmlWriter to
	 * put its data to plus the encoding to use for the header.
	 * 
	 * @param out			The output to write to.
	 * @param encoding		The encoding to use for the xml-file
	 * @throws NullPointerException
	 * 						When the argument out is set to null.
	 * @throws IOException	When the writer throws this exception upon writing the header.
	 */
	public XmlWriter(Writer out, Encoding encoding) throws NullPointerException, IOException
	{
		if (out == null)
			throw new NullPointerException("XmlWriter needs a Writer in order to work.");
		
		this.indent = 0;
		this.writer = out;
		
		// write the header
		String str_encoding = "";
		if (encoding == Encoding.UTF_8)
			str_encoding = "UTF-8";
		else if (encoding == Encoding.UTF_16)
			str_encoding = "UTF-16";
		else if (encoding == Encoding.ISO_8859_1)
			str_encoding = "ISO-8859-1";
		
		this.writer.write("<?xml version=\"1.0\" encoding=\"" + str_encoding + "\"?>\n\n\n");
	}
	
	/**
	 * 
	 * @param out
	 * @param encoding
	 * @param stylesheet
	 * @throws NullPointerException
	 * @throws IOException
	 */
	public XmlWriter(Writer out, Encoding encoding, String stylesheet) throws NullPointerException, IOException
	{
		this(out, encoding);
		
		if (stylesheet != null)
			this.writer.write("<?xml-stylesheet type=\"text/xml\" href=\"" + stylesheet + "\"?>\n\n");
	}

	
	
	// access
	/**
	 * This method writes the given document completely to the output. All
	 * the indentation, etc. is kept intact by the writer.
	 * 
	 * @param doc			The document to be written to the output in xml-format.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument doc is set to null.
	 */
	public void writeDocument(Document doc)  throws IOException, NullPointerException
	{
		if (doc == null)
			throw new NullPointerException("The document cannot be set to null.");
		
		writeElement(doc.getDocumentElement());
		flush();
	}
	
	/**
	 * This method writes the given element (including its attributes) and all the
	 * sub-elements below to the output. 
	 * 
	 * @param element		The element to be written to the output in xml-format.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument element is set to null.
	 */
	public void writeElement(Element element) throws IOException, NullPointerException
	{
		if (element == null)
			throw new NullPointerException("The element cannot be set to null.");
		
		NodeList nodes = element.getChildNodes();
		
		// retrieve all the information in the right format
		Vector<Element> children = new Vector<Element>();
		Vector<XmlAttribute> attributes = new Vector<XmlAttribute>();
		
		for (int i=0; i<nodes.getLength(); i++)
		{
			Node node = nodes.item(i);
			
			switch (node.getNodeType())
			{
			case Node.ELEMENT_NODE:
				children.add((Element) node);
				break;
			}
		}
		
		NamedNodeMap atts = element.getAttributes();
		for (int i=0; i<atts.getLength(); i++)
		{
			Attr att = (Attr) atts.item(i);
			attributes.add(new XmlAttribute(att.getName(), att.getValue()));
		}
		
		// do the printing
		if (children.size() == 0)
		{
			writeElement(element.getNamespaceURI(), element.getNodeName(), element.getTextContent(), attributes);
		}
		else
		{
			writeTag(element.getTagName(), Tag.OPEN, attributes);
			for (Element node : children)
				writeElement((Element) node);
			writeTag(element.getTagName(), Tag.CLOSE);
		}
	}
	
	/**
	 * This method writes a single element (open-tag, data and closing-tag) to the
	 * output.
	 * 
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param data			The data of the element to be written to the output in xml-format.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name is set to null.
	 */
	public void writeElement(String name, String data) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		
		if (data==null || data.equals(""))
		{
			writeTag(name, Tag.SINGLE);
		}
		else
		if (data.indexOf('\n') == -1)
		{
			StringBuffer buf = new StringBuffer();
			
			for (int i=0; i<indent; i++)
				buf.append('\t');
			buf.append('<'); buf.append(name); buf.append('>');
			buf.append(SpecialToken.reference(data));
			buf.append("</"); buf.append(name); buf.append('>');
			buf.append('\n');
			
			writer.write(buf.toString());
		}
		else
		{
			writeTag(name, Tag.OPEN);
			writeData(data);
			writeTag(name, Tag.CLOSE);
		}
	}
	
	/**
	 * This method writes a single element (open-tag, data and closing-tag) to the
	 * output.
	 * 
	 * @param namespace		The namespace.
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param data			The data of the element to be written to the output in xml-format.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name is set to null.
	 */
	public void writeElement(String namespace, String name, String data) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		
		writeElement((namespace!=null&&namespace!="" ? namespace+':'+name : name), data);
	}
	
	/**
	 * This method writes a single element (open-tag including attributes, data and closing-tag)
	 * to the output.
	 * 
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param data			The data of the element to be written to the output in xml-format.
	 * @param attributes	The attributes of the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeElement(String name, String data, List<XmlAttribute> attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		if (data==null || data.equals(""))
		{
			writeTag(name, Tag.SINGLE, attributes);
		}
		else
		if (data.indexOf('\n') == -1)
		{
			StringBuffer buf = new StringBuffer();
			
			for (int i=0; i<indent; i++)
				buf.append('\t');
			buf.append('<');
			buf.append(name);
			for (XmlAttribute attribute : attributes)
				buf.append(" " + attribute.getName() + "=\"" + attribute.getValue() + "\"");
			buf.append('>');
			buf.append(SpecialToken.reference(data));
			buf.append("</"); buf.append(name); buf.append('>');
			buf.append('\n');
			
			writer.write(buf.toString());
		}
		else
		{
			writeTag(name, Tag.OPEN);
			writeData(data);
			writeTag(name, Tag.CLOSE);
		}
	}
	
	/**
	 * This method writes a single element (open-tag including attributes, data and closing-tag)
	 * to the output.
	 * 
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param data			The data of the element to be written to the output in xml-format.
	 * @param attributes	The attributes of the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeElement(String name, String data, XmlAttribute... attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		Vector<XmlAttribute> atts = new Vector<XmlAttribute>();
		
		for (XmlAttribute att : attributes)
			atts.add(att);
		writeElement(name, data, atts);
	}
	
	/**
	 * This method writes a single element (open-tag including attributes, data and closing-tag)
	 * to the output.
	 * 
	 * @param namespace		The namespace.
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param data			The data of the element to be written to the output in xml-format.
	 * @param attributes	The attributes of the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeElement(String namespace, String name, String data, List<XmlAttribute> attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		writeElement((namespace!=null&&namespace!="" ? namespace+':'+name : name), data, attributes);
	}
	
	/**
	 * This method writes a single element (open-tag including attributes, data and closing-tag)
	 * to the output.
	 * 
	 * @param namespace		The namespace.
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param data			The data of the element to be written to the output in xml-format.
	 * @param attributes	The attributes of the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeElement(String namespace, String name, String data, XmlAttribute... attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		Vector<XmlAttribute> atts = new Vector<XmlAttribute>();
		
		for (XmlAttribute att : attributes)
			atts.add(att);
		writeElement(namespace, name, data, atts);
	}
	
	/**
	 * This method writes a single tag (name + attributes) to the output. The type of
	 * the tag is indicated with the parameter tag.
	 * 
	 * @param namespace		The namespace.
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param tag			The tag of the element to be written to the output in xml-format.
	 * @param attributes	The attributes of the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeTag(String namespace, String name, Tag tag, List<XmlAttribute> attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		writeTag((namespace!=null&&namespace!="" ? namespace+':'+name : name), tag, attributes);
	}
	
	/**
	 * This method writes a single tag (name + attributes) to the output. The type of
	 * the tag is indicated with the parameter tag.
	 * 
	 * @param namespace		The namespace.
	 * @param name			The name of the element to be written to the output in xml-format.
	 * @param tag			The tag of the element to be written to the output in xml-format.
	 * @param attributes	The attributes of the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeTag(String namespace, String name, Tag tag, XmlAttribute... attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		Vector<XmlAttribute> atts = new Vector<XmlAttribute>();
		
		for (XmlAttribute att : attributes)
			atts.add(att);
		writeTag(namespace, name, tag, atts);
	}
	
	/**
	 * This method writes a single tag (name + attributes) to the output. The type of
	 * the tag is indicated with the parameter tag.
	 * 
	 * @param name			The name of the tag.
	 * @param tag			The type of tag (@see XmlWriter.Tag).
	 * @param attributes	The attributes of the tag.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeTag(String name, Tag tag, List<XmlAttribute> attributes) throws IOException, NullPointerException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		StringBuffer str = new StringBuffer();

		if (tag == Tag.CLOSE)
			indent--;

		for (int i = 0; i < indent; i++)
			str.append('\t');

		str.append(tag == Tag.CLOSE ? "</" : "<");
		str.append(name);

		for (XmlAttribute attribute : attributes)
			str.append(" " + attribute.getName() + "=\"" + attribute.getValue() + "\"");

		str.append(tag == Tag.SINGLE ? " />" : ">");
		str.append('\n');

		if (tag == Tag.OPEN)
			indent++;

		// dump the output
		writer.write(str.toString());
	}

	/**
	 * This method writes a single tag (name + attributes) to the output. The type of
	 * the tag is indicated with the parameter tag.
	 * 
	 * @param name			The name of the tag.
	 * @param tag			The type of tag (@see XmlWriter.Tag).
	 * @param attributes	The attributes of the tag.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the argument name or attributes is set to null.
	 */
	public void writeTag(String name, Tag tag, XmlAttribute... attributes) throws IOException
	{
		if (name == null)
			throw new NullPointerException("The name cannot be set to null");
		if (attributes == null)
			throw new NullPointerException("The attributes cannot be set to null");

		StringBuffer str = new StringBuffer();

		if (tag == Tag.CLOSE)
			indent--;

		for (int i = 0; i < indent; i++)
			str.append('\t');

		str.append(tag == Tag.CLOSE ? "</" : "<");
		str.append(name);

		for (XmlAttribute attribute : attributes)
			str.append(" " + attribute.getName() + "=\"" + attribute.getValue() + "\"");

		str.append(tag == Tag.SINGLE ? " />" : ">");
		str.append('\n');

		if (tag == Tag.OPEN)
			indent++;

		// dump the output
		writer.write(str.toString());
	}

	/**
	 * Writes a single break (newline) to the output. This optionally makes the output
	 * a bit more readable.
	 * 
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 */
	public void writeBreak() throws IOException
	{
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < indent; i++)
			str.append('\t');
		str.append('\n');

		// dump the output
		writer.write(str.toString());
	}
	
	/**
	 * Writes a comment to the output.
	 * 
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 */
	public void writeComment(String comment) throws IOException
	{
		StringBuffer str = new StringBuffer();

		for (int i = 0; i < indent; i++)
			str.append('\t');
		str.append("<!-- ");
		str.append(comment);
		str.append(" -->\n");

		// dump the output
		writer.write(str.toString());
	}
	
	/**
	 * This method writes the given data-string to the output. When it contains newline
	 * characters, the method keeps the indentation intact.
	 * 
	 * @param data			The string containing the data for the element.
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 * @throws NullPointerException
	 * 						When the data is set to null
	 */
	public void writeData(String data) throws IOException, NullPointerException
	{
		String text = SpecialToken.reference(data);
		StringBuffer str = new StringBuffer();
		
		int prev = 0;
		int next = 0;
		do
		{
			next = text.indexOf('\n', prev);
			
			// write the tabs
			for (int i = 0; i < indent; i++)
				str.append('\t');
			
			// 
			if (next != -1)
				str.append(text.substring(prev, next));
			else
				str.append(text.substring(prev));
			str.append('\n');
			
			prev = next + 1;
		} while (next != -1);
		
		// dump the output
		writer.write(str.toString());
	}

	/**
	 * Flushes the output. Very useful for making application toolchains.
	 * 
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 */
	public void flush() throws IOException
	{
		writer.flush();
	}
	
	/**
	 * Closes the output. Very useful for making application toolchains.
	 * 
	 * @throws IOException	Thrown by the writer when something is wrong with the stream.
	 */
	public void close() throws IOException
	{
		writer.close();
	}


	// member variables
	/** Keeps track of current indentation */
	private int indent;
	/** Writer to write data to */
	private Writer writer;
}
