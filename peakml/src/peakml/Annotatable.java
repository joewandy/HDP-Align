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
import java.util.*;





/**
 * Base-class for all classes that support annotations.
 */
public abstract class Annotatable
{
	// annotations
	/**
	 * Returns the hashmap containing all the annotations. This is a reference to the
	 * hashmap maintained within the class and should not be altered, but only used for
	 * access purposes.
	 * 
	 * @return					The hashmap with all the annotations associated to this peak.
	 */
	public final HashMap<String,Annotation> getAnnotations()
	{
		return annotations;
	}
	
	/**
	 * Returns the annotation with the given name, stored in this peak. When a
	 * non-existing name is passed the method returns null.
	 * 
	 * @param name				The name of the annotation.
	 * @return					The annotation with the given name, null when non-existing.
	 */
	public Annotation getAnnotation(String name)
	{
		if (annotations == null)
			return null;
		return annotations.get(name);
	}
	
	/**
	 * Adds the given annotation instance to the hashmap. When the label of this annotation
	 * is already present in the hashmap, the old annotation is overwritten.
	 * 
	 * @param annotation		The new annotation.
	 */
	public void addAnnotation(Annotation annotation)
	{
		if (annotations == null)
			annotations = new HashMap<String,Annotation>();
		annotations.put(annotation.getLabel(), annotation);
	}
	
	/**
	 * Adds the list of annotations to the hashmap. Annotations with the same name are
	 * overwritten.
	 * 
	 * @param list				The list with annotations.
	 */
	public void addAnnotations(Collection<Annotation> list)
	{
		for (Annotation annotation : list)
			addAnnotation(annotation);
	}
	
	/**
	 * Creates a new annotation with the given label and value, where the valuetype
	 * is automatically set to STRING. The new annotation is consequently added
	 * to the hashmap with {@link IPeak#addAnnotation(Annotation annotation)}.
	 * 
	 * @param label				The label of the annotation.
	 * @param value				The value of the annotation.
	 */
	public void addAnnotation(String label, String value)
	{
		addAnnotation(new Annotation(label, value));
	}
	
	/**
	 * Creates a new annotation with the given label and value, where the valuetype
	 * is automatically set to INTEGER. The new annotation is consequently added
	 * to the hashmap with {@link IPeak#addAnnotation(Annotation annotation)}.
	 * 
	 * @param label				The label of the annotation.
	 * @param value				The value of the annotation.
	 */
	public void addAnnotation(String label, int value)
	{
		addAnnotation(new Annotation(label, Integer.toString(value), Annotation.ValueType.INTEGER));
	}
	
	/**
	 * Creates a new annotation with the given label and value, where the valuetype
	 * is automatically set to DOUBLE. The new annotation is consequently added
	 * to the hashmap with {@link IPeak#addAnnotation(Annotation annotation)}.
	 * 
	 * @param label				The label of the annotation.
	 * @param value				The value of the annotation.
	 */
	public void addAnnotation(String label, double value)
	{
		addAnnotation(new Annotation(label, Double.toString(value), Annotation.ValueType.DOUBLE));
	}
	
	/**
	 * Creates a new annotation with the given label, value, and valuetype.
	 * The new annotation is consequently added to the hashmap with
	 * {@link IPeak#addAnnotation(Annotation annotation)}.
	 * 
	 * @param label				The label of the annotation.
	 * @param value				The value of the annotation.
	 * @param valuetype			The type of the annotation.
	 */
	public void addAnnotation(String label, String value, Annotation.ValueType valuetype)
	{
		addAnnotation(new Annotation(label, value, valuetype));
	}
	
	/**
	 * Removes annotation with the given label from the list.
	 * 
	 * @param label				The label of the annotation to be removed.
	 */
	public void removeAnnotation(String label)
	{
		if (annotations == null)
			return;
		annotations.remove(label);
	}
	
	/**
	 * Removes all annotations from the list.
	 */
	public void removeAllAnnotations()
	{
		if (annotations != null)
			annotations.clear();
	}
	
	
	// data
	protected HashMap<String,Annotation> annotations = null;
}
