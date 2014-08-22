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

import java.util.HashMap;
import java.util.Map;


// java





/**
 * The polarity setting of the instrument used when collecting the data.
 */
public enum Polarity
{
	/** The MS data was acquired in negative ionisation mode. */
	NEGATIVE("-", "NEGATIVE"),
	/** The data was acquired without ionisation or has been corrected (can also be used for {@link Spectrum.Type.PDA} data). */
	NEUTRAL("", "NEUTRAL"),
	/** The MS data was acquired in positive ionisation mode. */
	POSITIVE("+", "POSITIVE"),
	/** The data was acquired in both negative as well as positive ionisation mode. Used for {@link ChromatographyMS}, which can contain spectra with both ionization settings. */
	POSITIVE_NEGATIVE("+-", "POSITIVE_NEGATIVE");
	
	private final String symbol;
	private final String representation;
	
	private static final Map<String, Polarity> stringToEnum = new HashMap<String,Polarity>();
	private static final Map<String, Polarity> symbolToEnum = new HashMap<String,Polarity>();
	
	static {
		for ( Polarity p : values() ) {
			stringToEnum.put(p.getRepresentation(), p);
			symbolToEnum.put(p.toString(), p);
		}
	}
	
	Polarity(final String symbol, final String representation) {
		this.symbol = symbol;
		this.representation = representation;
	}
	
	public String getRepresentation() {
		return this.representation;
	}
	
	@Override
	public String toString() {
		return getRepresentation();
	}
	
	public static Polarity fromString(String representation) {
		return stringToEnum.get(representation);
	}
	
	public static Polarity fromSymbol(String symbol) {
		return symbolToEnum.get(symbol);
	}
}
