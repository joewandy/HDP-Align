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
 * Interface acting as a progress listener for the parser classes. By implementing
 * this interface and passing a reference to one of the parse-methods in this class, the
 * progress can be monitored. Each parse-function calculates the current position in the
 * file based on the result of {@link Header#getNrPeaks()}.
 */
public interface ParserProgressListener
{
	/**
	 * This method is called by parse-functions in the parser classes to indicate the progress
	 * of the parsing. A percentage is returned, which lies between 0 and 100.
	 * 
	 * @param percentage		The percentage of the PeakML already parsed.
	 */
	public void update(double percentage);
}
