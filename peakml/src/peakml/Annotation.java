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



package peakml;


// java





/**
 * Implementation of a label-value pair. This class is used in the PeakML file format to
 * include information in the file that is not defined in the schema, without breaking
 * the format. The label encodes the name of the annotation and the value encodes the
 * actual value.
 */
public class Annotation
{
	/**
	 * This enum defines the different types of annotations that can occur. This
	 * will help in the interpretation of the data stored in a label-value
	 * annotation pair.
	 */
	public enum ValueType
	{
		/** The annotation value is represented by an integer */
		INTEGER,
		/** The annotation value is represented by a double */
		DOUBLE,
		/** The annotation value is represented by a string */
		STRING,
		/** The annotation label is actualy an ontology term. It is up to the user to interpret this. */
		ONTOLOGY;
	}
	
	/** System annotation for relation id's of derivatives */
	public static final String relationid = "relation.id";
	/** System annotation for relationships of derivatives */
	public static final String relationship = "relation.ship";
	/** System annotation for identification tags */
	public static final String identification = "identification";
	/** System annotation for storing the raw retention time in the ScanInfo */
	public static final String raw_retentiontime = "rt.raw";
	/** System annotation for storing adduct relationship to the molecule */
	public static final String adduct = "adduct";
	/** Evaluation annotation - Joe */
	public static final String db = "db";
	public static final String formula = "formula";
	/** Model-based alignment - Joe */
	public static final String binId = "binId";
	public static final String sourcePeakset = "sourcePeakset";
	public static final String peakId = "peakId";
	
	
	// constructor(s)
	/**
	 * Constructs a new annotation instance, with the given label and value. The type
	 * of the constructed annotation is automatically set to string.
	 * 
	 * @param label		The label of the annotation.
	 * @param value		The value of the annotation.
	 */
	public Annotation(String label, String value)
	{
		this(label, value, ValueType.STRING);
	}
	
	/**
	 * Constructs a new annotation instance, with the given label, value, and type.
	 * 
	 * @param label		The label of the annotation.
	 * @param value		The value of the annotation.
	 * @param valuetype	The type of the annotation.
	 */
	public Annotation(String label, String value, ValueType valuetype)
	{
		this.label = label;
		this.value = value;
		this.valuetype = valuetype;
	}
	
	/**
	 * Constructs a new annotation instance, with the given label, value, and type.
	 * 
	 * @param label		The label of the annotation.
	 * @param value		The value of the annotation.
	 */
	public Annotation(String label, int value)
	{
		this.label = label;
		this.value = Integer.toString(value);
		this.valuetype = ValueType.INTEGER;
	}
	
	/**
	 * Constructs a new annotation instance, with the given label, value, and type.
	 * 
	 * @param label		The label of the annotation.
	 * @param value		The value of the annotation.
	 */
	public Annotation(String label, double value)
	{
		this.label = label;
		this.value = Double.toString(value);
		this.valuetype = ValueType.DOUBLE;
	}
	
	
	// access
	/**
	 * Retrieves the label of this annotation.
	 * 
	 * @return		The label of the annotation.
	 */
	public String getLabel()
	{
		return label;
	}
	
	/**
	 * Returns the type of the annotation.
	 * 
	 * @return		The type of the annotation.
	 */
	public ValueType getValueType()
	{
		return valuetype;
	}
	
	/**
	 * Returns the value of this annotation as a string. This method will always work
	 * because the label is stored as a string in the annotation instance.
	 * 
	 * @return		The value of the annotation.
	 */
	public String getValue()
	{
		return value;
	}
	
	/**
	 * See {@link Annotation#getValue()}.
	 * 
	 * @return		The value of the annotation.
	 */
	public String getValueAsString()
	{
		return value;
	}
	
	/**
	 * Returns the value of the annotation as an integer. Beware that the string is
	 * converted to an integer and this method will throw an exception if the string
	 * does not represent an integer.
	 * 
	 * @return		The value of the annotation as an integer.
	 * @throws NumberFormatException
	 * 				Thrown by {@link Integer#parseInt(String)} and passed on to the calling method.
	 */
	public int getValueAsInteger() throws NumberFormatException
	{
		return Integer.parseInt(value);
	}
	
	/**
	 * Returns the value of the annotation as a double. Beware that the string is
	 * converted to a double and this method will throw an exception if the string
	 * does not represent a double.
	 * 
	 * @return		The value of the annotation as a double.
	 * @throws NumberFormatException
	 * 				Thrown by {@link Double#parseDouble(String)} and passed on to the calling method.
	 */
	public double getValueAsDouble() throws NumberFormatException
	{
		return Double.parseDouble(value);
	}
	
	/**
	 * Sets the given value.
	 * 
	 * @param value			The new value.
	 */
	public void setValue(String value)
	{
		this.value = value;
	}
	
	/**
	 * Sets the given value.
	 * 
	 * @param value			The new value.
	 */
	public void setValue(int value)
	{
		this.value = Integer.toString(value);
	}
	
	/**
	 * Sets the given value.
	 * 
	 * @param value			The new value.
	 */
	public void setValue(double value)
	{
		this.value = Double.toString(value);
	}
	
	/**
	 * Gets the unit.
	 * 
	 * @return				The unit.
	 */
	public String getUnit()
	{
		return unit;
	}
	
	/**
	 * Sets the given unit.
	 * 
	 * @param unit			The new unit.
	 */
	public void setUnit(String unit)
	{
		this.unit = unit;
	}
	
	/**
	 * Sets the ontology-ref (eg GO:0051458) for the annotation. This method
	 * only works when the value-type for the annotation has been set to
	 * ontology, otherwise an exception is thrown.
	 * 
	 * @param ontologyref	The ontology-ref.
	 * @throws RuntimeException
	 * 						Thrown when the annotation is not of value-type ontology.
	 */
	public void setOntologyRef(String ontologyref) throws RuntimeException
	{
		if (valuetype != ValueType.ONTOLOGY)
			throw new RuntimeException("Annotation needs to be a ontology-term.");
		this.ontologyref = ontologyref;
	}
	
	/**
	 * Returns the ontology-ref (eg GO:0051458) for the annotation. This method
	 * only works when the value-type for the annotation has been set to
	 * ontology, otherwise an exception is thrown.
	 * 
	 * @return				The ontology-ref.
	 * @throws RuntimeException
	 * 						Thrown when the annotation is not of value-type ontology.
	 */
	public String getOntologyRef() throws RuntimeException
	{
		if (valuetype != ValueType.ONTOLOGY)
			throw new RuntimeException("Annotation needs to be a ontology-term.");
		return ontologyref;
	}
	
	
	// data
	protected String label;
	protected String value;
	protected String unit = null;
	protected ValueType valuetype;
	protected String ontologyref = null;
}
