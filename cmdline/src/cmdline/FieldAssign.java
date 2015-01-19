// File:		util/FieldAssign.java
// Copyright:	GBIC 2005, all rights reserved
//
// Changelog:
// 2005-11-17; 1.0.0; RA Scheltema;
//	Creation.
//


package cmdline;


// jdk
import java.util.*;
import java.lang.reflect.*;





/**
 * ...
 * 
 * @author RA Scheltema
 * @version 1.0.0
 */
public class FieldAssign
{
	// constructor / destructor
	/**
	 * ...
	 * 
	 * @param f ...
	 * @throws NullPointerException When the field f is null.
	 */
	public FieldAssign(final Field f) throws NullPointerException
	{
		if (f == null)
			throw new NullPointerException("Parameter field cannot be null");

		this.field = f;
	}

	// public methods
	/**
	 * This method applies the given value to the field in the given object. It
	 * checks whether the field is a primitive and converts the value
	 * accordingly.
	 * 
	 * @param obj The object to set.
	 * @param value The value to set the obj to.
	 * @throws NullPointerException When obj or value are null.
	 * @throws IllegalAccessException When the obj cannot be set to the given value.
	 */
	@SuppressWarnings({"unchecked"})
	public void assign(final Object obj, final Object value) throws IllegalAccessException
	{
		if (obj == null || value == null)
			throw new NullPointerException("The parameters obj and value cannot be null.");

		Class c = field.getType();
		if (!c.isPrimitive())
		{
			try {
				// TODO this is a crappy construction ...
				// TODO we're only able to do Strings in the collection ...
				if (Collection.class.isInstance(field.getType().newInstance()))
				{
					Collection<String> collection = (Collection<String>) field.get(obj);
					for (String v : value.toString().split(","))
						collection.add(v);
				}
				else
					field.set(obj, value);
			} catch (Exception e) { ; }
		}
		else
		{
			String name = c.getSimpleName();
			if (name.equals("int"))
				field.setInt(obj, Integer.valueOf(value.toString()));
			else if (name.equals("short"))
				field.setLong(obj, Short.valueOf(value.toString()));
			else if (name.equals("long"))
				field.setLong(obj, Long.valueOf(value.toString()));
			else if (name.equals("boolean"))
				field.setBoolean(obj, Boolean.valueOf(value.toString()));
			else if (name.equals("float"))
				field.setFloat(obj, Float.valueOf(value.toString()));
			else if (name.equals("double"))
				field.setDouble(obj, Double.valueOf(value.toString()));
			else if (name.equals("char"))
				field.setChar(obj, value.toString().charAt(0));
			else if (name.equals("Byte"))
				field.setByte(obj, Byte.valueOf(value.toString()));
		}
	}


	// private members
	/** Reference to the field. */
	Field field;
}
