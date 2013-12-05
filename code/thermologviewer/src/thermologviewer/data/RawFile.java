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



package thermologviewer.data;


// java
import java.util.*;

// peakml
import peakml.io.xrawfile.*;





/**
 * 
 */
public class RawFile
{
	// constructor(s)
	public RawFile(String filename, HashMap<String,double[]> statuslog, Vector<IXRawfile.Controller> controllers, double retentiontimes[])
	{
		this.color = 0;
		this.filename = filename;
		this.statuslog = statuslog;
		this.controllers = controllers;
		this.retentiontimes = retentiontimes;
	}
	
	
	// access
	public String getFilename()
	{
		return filename;
	}
	
	public Vector<IXRawfile.Controller> getControllers()
	{
		return controllers;
	}
	
	public HashMap<String,double[]> getStatusLog()
	{
		return statuslog;
	}
	
	public double[] getRetentionTimes()
	{
		return retentiontimes;
	}
	
	public int getColor()
	{
		return color;
	}
	
	public void setColor(int color)
	{
		this.color = color;
	}
	
	
	// data
	protected int color;
	protected String filename;
	protected HashMap<String,double[]> statuslog;
	protected Vector<IXRawfile.Controller> controllers;
	protected double retentiontimes[];
}
