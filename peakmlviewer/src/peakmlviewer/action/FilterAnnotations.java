/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakmlviewer.action;

import java.util.Vector;

import peakml.*;

public class FilterAnnotations extends Filter
{
	public static enum Relation
	{
		GREATER_THEN,
		SMALLER_THEN,
		EQUALS,
		LIKE
	}
	
	
	// constructor(s)
	public FilterAnnotations(String name, Relation relation, String value)
	{
		this.name = name;
		this.relation = relation;
		this.value = value;
	}
	
	
	// action
	public String getName()
	{
		return "Filter annotations";
	}
	
	public String getDescription()
	{
		return name + " " + relation + " " + value;
	}
	
	public Vector<IPeak> execute(Vector<IPeak> peaks)
	{
		Vector<IPeak> filtered = new Vector<IPeak>();
		for (IPeak peak : peaks)
		{
			Annotation annotation = peak.getAnnotation(name);
			if (annotation == null)
				continue;
			
			if (annotation.getValueType() == Annotation.ValueType.INTEGER)
			{
				int the_value = Integer.parseInt(value);
				int ann_value = annotation.getValueAsInteger();
				if (relation==Relation.EQUALS && the_value==ann_value)
					filtered.add(peak);
				else if (relation==Relation.GREATER_THEN && ann_value>the_value)
					filtered.add(peak);
				else if (relation==Relation.SMALLER_THEN && ann_value<the_value)
					filtered.add(peak);
			}
			else if (annotation.getValueType() == Annotation.ValueType.DOUBLE)
			{
				double the_value = Double.parseDouble(value);
				double ann_value = annotation.getValueAsDouble();
				if (relation==Relation.EQUALS && the_value==ann_value)
					filtered.add(peak);
				else if (relation==Relation.GREATER_THEN && ann_value>the_value)
					filtered.add(peak);
				else if (relation==Relation.SMALLER_THEN && ann_value<the_value)
					filtered.add(peak);
			}
			else
			{
				int compare = annotation.getValue().compareTo(value);
				if (relation==Relation.EQUALS && compare==0)
					filtered.add(peak);
				else if (relation==Relation.SMALLER_THEN && compare<0)
					filtered.add(peak);
				else if (relation==Relation.GREATER_THEN && compare>0)
					filtered.add(peak);
				else if (relation==Relation.LIKE && annotation.getValue().toLowerCase().contains(value.toLowerCase()))
					filtered.add(peak);
			}
		}
		return filtered;
	}
	
	
	// data
	protected String name;
	protected Relation relation;
	protected String value;
}
