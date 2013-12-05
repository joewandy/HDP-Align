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



package peakml.io;


// java
import java.util.*;

// peakml





/**
 * Central point for collecting all information about a set. A set combines multiple
 * measurements with a logical relationship (eg technical replicates). In order to do
 * this, measurement id's are stored in the class. The {@link Header} class provides
 * access methods for easily retrieval of the {@link MeasurementInfo} instances.
 */
public class SetInfo
{
	/** Type indicating that this set is used to combine other sets */
	public static int SET						= 0;
	/** Type indicating that this set is used to combine technical replicates (either files or sets) */
	public static int TECHNICAL_REPLICATES		= 1;
	/** Type indicating that this set is used to combine biological replicates (either files or sets) */
	public static int BIOLOGICAL_REPLICATES		= 2;
	
	
	// constructor(s)
	/**
	 * Constructs a new set with the given id, type and measurement id's.
	 * 
	 * @param id				The id of the set.
	 * @param type				The type of the set.
	 * @param measurementids	The measurement id's to associate to the set.
	 */
	public SetInfo(String id, int type, int... measurementids)
	{
		this.id = id;
		this.type = type;
		this.visible = true;
		
		for (int measurementid : measurementids)
			this.measurementids.add(measurementid);
	}
	
	/**
	 * Copy constructor, which makes a deep copy of the given {@link SetInfo} instance.
	 * 
	 * @param setinfo			The instance to copy.
	 */
	public SetInfo(SetInfo setinfo)
	{
		this.id = setinfo.id;
		this.type = setinfo.type;
		this.visible = true;
		
		for (int measurementid : setinfo.measurementids)
			this.measurementids.add(measurementid);
	}
	
	
	// access
	/**
	 * Returns the id of this set.
	 * 
	 * @return					The id of the set.
	 */
	public String getID()
	{
		return id;
	}
	
	/**
	 * Returns the type of this set.
	 * 
	 * @return					The type of the set.
	 */
	public int getType()
	{
		return type;
	}
	
	/**
	 * Convenience function for user interface applications to keep track off whether to show
	 * this set or not.
	 * 
	 * @param b					Boolean indicating show or not.
	 */
	public void setVisible(boolean b)
	{
		visible = b;
	}
	
	/**
	 * For more information see {@link SetInfo#setVisible(boolean)}.
	 * 
	 * @return					Boolean indicating show or not.
	 */
	public boolean getVisible()
	{
		return visible;
	}
	
	
	// tree functionality
	/**
	 * Adds the given set as a child to this set.
	 * 
	 * @param child				The set to be added as a child.
	 */
	public void addChild(SetInfo child)
	{
		children.add(child);
	}
	
	/**
	 * Returns the number of children in this set.
	 * 
	 * @return					The number of children in this set.
	 */
	public int getNrChildren()
	{
		return children.size();
	}
	
	/**
	 * Returns the child at the given index. When the index is not applicable to the
	 * list of children an index out of bounds exception is thrown.
	 * 
	 * @param index				The index where the child is located.
	 * @return					The child at the given index.
	 * @throws IndexOutOfBoundsException
	 * 							Thrown when the given index is out of bounds for the list of children.
	 */
	public SetInfo getChild(int index) throws IndexOutOfBoundsException
	{
		return children.get(index);
	}
	
	/**
	 * Returns the list with all the children.
	 * 
	 * @return					The list with all the children.
	 */
	public Vector<SetInfo> getChildren()
	{
		return children;
	}
	
	
	// measurements
	/**
	 * Adds the given measurement id to the list of id's.
	 * 
	 * @param id				The integer measurement id to be added to this set.
	 */
	public void addMeasurementID(int id)
	{
		measurementids.add(id);
	}
	
	/**
	 * Returns a list of all the measurement id's associated with this set (excluding
	 * all the child sets).
	 * 
	 * @return					The list of the measurement id's associated to this set.
	 */
	public Vector<Integer> getMeasurementIDs()
	{
		return measurementids;
	}
	
	/**
	 * Returns the measurement id at the given index in this set information.
	 * 
	 * @param index				The index of the measurement info.
	 * @return					The measurement id at the given index.
	 */
	public int getMeasurementID(int index)
	{
		return measurementids.get(index);
	}
	
	/**
	 * Returns the number of measurement id's associated to this set.
	 * 
	 * @return					The number of measurement id's.
	 */
	public int getNrMeasurementIDs()
	{
		return measurementids.size();
	}
	
	/**
	 * Returns a list of all the measurement id's associated with this set (including
	 * all the child sets).
	 * 
	 * @return					The list of the measurement id's associated to this set.
	 */
	public Vector<Integer> getAllMeasurementIDs()
	{
		Vector<Integer> ids = new Vector<Integer>();
		ids.addAll(measurementids);
		for (SetInfo child : children)
			ids.addAll(child.getAllMeasurementIDs());
		return ids;
	}
	
	/**
	 * Returns true when the given id is associated to this set (including all the
	 * child sets).
	 * 
	 * @param id				The measurement id to check for.
	 * @return					True when the id is associated to this set, false otherwise.
	 */
	public boolean containsMeasurementID(int id)
	{
		for (Integer myid : measurementids)
			if (myid == id) return true;
		
		for (SetInfo set : children)
			if (set.containsMeasurementID(id)) return true;
		
		return false;
	}
	
	
	// data	
	protected int type;
	protected String id;
	protected boolean visible;
	protected Vector<SetInfo> children = new Vector<SetInfo>();
	protected Vector<Integer> measurementids = new Vector<Integer>();
}
