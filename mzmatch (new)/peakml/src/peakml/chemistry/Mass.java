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



package peakml.chemistry;


// java





/**
 * In chemistry two methods for calculating the mass of a molecule are used. The
 * one used for mixtures is molecular weight, which is calculated by taking all
 * the isotopic forms of elements into account and scaling then to their natural
 * occurance. For mixtures this is the ideal weight, because molecules with
 * isotope forms occur in their natural abundances. The second is used when
 * only the most abundant isotope is taken into account.
 */
public enum Mass
{
	/** The mass is calculated with the masses of all the isotopes as they occur naturally. */
	MOLECULAR,
	/** The mass is calculated with the mass of the most common isotopes for C, O, etc. */
	MONOISOTOPIC
}
