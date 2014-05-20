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

package com.joewandy.alignmentResearch.main;

public class MultiAlignConstants {
		
	// supposed to be used to determine whether the mass tolerance is absolute or relative value
	public static final boolean ALIGN_BY_RELATIVE_MASS_TOLERANCE = false;
	
	public static final String GROUPING_METHOD_GREEDY = "greedy";
	public static final String GROUPING_METHOD_MIXTURE = "mixture";
	public static final String GROUPING_METHOD_POSTERIOR = "posterior";
	public static final String GROUPING_METHOD_METASSIGN_MIXTURE = "metAssignMixture";
	public static final String GROUPING_METHOD_METASSIGN_POSTERIOR = "metAssignPosterior";
	
	public static final double GROUPING_METHOD_RT_TOLERANCE = 5;
	public static final double GROUPING_METHOD_ALPHA = 1;
	public static final int GROUPING_METHOD_NUM_SAMPLES = 20;
	public static final int GROUPING_METHOD_BURN_IN = 10;
	
	// use Lange, et al. (2008) measure
	public static final String PERFORMANCE_MEASURE_LANGE = "lange";

	// use my own performance measure
	public static final String PERFORMANCE_MEASURE_JOE = "joe";
	
}