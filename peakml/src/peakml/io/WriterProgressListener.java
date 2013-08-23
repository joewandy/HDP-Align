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





/**
 * Interface acting as a progress listener for the writer classes. By implementing
 * this interface and passing a reference to one of the write-methods in this class, the
 * progress can be monitored. Each write-function calculates the current position in the
 * the list of peaks and passes the percentage.
 */
public interface WriterProgressListener
{
	/**
	 * This method is called by write-functions in the writer classes to indicate the progress
	 * of the writing. A percentage is returned, which lies between 0 and 100.
	 * 
	 * @param percentage		The percentage of the PeakML already written.
	 */
	public void update(double percentage);
}
