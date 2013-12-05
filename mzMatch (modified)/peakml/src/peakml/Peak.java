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
 * Base-class for a single peak, which can either be a single data-point
 * ({@link Centroid}), or a collection of data-points making up the peak
 * ({@link Profile}). This class has been provided to enable type-binding to
 * either {@link Centroid} or {@link Profile} for classes like
 * {@link MassChromatogram} and does not add additional properties to these
 * classes.
 */
public abstract class Peak extends IPeak
{
}
