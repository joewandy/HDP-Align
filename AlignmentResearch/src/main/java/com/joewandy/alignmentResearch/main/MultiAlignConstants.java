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
	public static final String GROUPING_METHOD_MATLAB_POSTERIOR = "matlabPosterior";
	public static final String GROUPING_METHOD_METASSIGN_MIXTURE = "metAssignMixture";
	public static final String GROUPING_METHOD_METASSIGN_POSTERIOR = "metAssignPosterior";

	public static final int GROUPING_METHOD_NUM_SAMPLES = 200;
	public static final int GROUPING_METHOD_BURN_IN = 100;
	public static final double GROUPING_METHOD_RT_TOLERANCE = 5;
	public static final double GROUPING_METHOD_ALPHA = 1;
	public static final double HDP_ALPHA_RT = 10;
	public static final double HDP_ALPHA_MASS = 10;
	public static final double HDP_TOP_ALPHA = 10;
	public static final double HDP_GLOBAL_RT_CLUSTER_STDEV = 10;
	public static final double HDP_LOCAL_RT_CLUSTER_STDEV = 10;
	public static final double HDP_MASS_CLUSTER_STDEV = 0.03;
	
	public static final String SCORING_METHOD_HDP_MASS_RT_JAVA = "hdpmassrtjava";
	public static final String SCORING_METHOD_HDP_RT_JAVA = "hdprtjava";
	public static final String SCORING_METHOD_HDP_MASS_RT = "hdpmassrt";
	public static final String SCORING_METHOD_HDP_RT = "hdprt";
	public static final String SCORING_METHOD_DIST = "dist";
	
	// use Lange, et al. (2008) measure
	public static final String PERFORMANCE_MEASURE_LANGE = "lange";

	// use my own performance measure
	public static final String PERFORMANCE_MEASURE_JOE = "joe";
	public static final String PERFORMANCE_MEASURE_JOE_PR = "joePR";
	
	/**
	 * Show setup dialog or not during MZMine alignment ?
	 */
	public static final boolean SHOW_PARAM_SETUP_DIALOG = false;
	
	/** 
	 * This value sets the range, in terms of retention time, to create the model 
	 * using RANSAC and non-linear regression algorithm. 
	 * Maximum allowed retention time difference
	 */
	public static final double PARAM_RT_TOLERANCE_BEFORE_CORRECTION = 300 / 60.0;

	/**
	 * Maximum number of iterations allowed in the algorithm to find the right model 
	 * consistent in all the pairs of aligned peaks. When its value is 0, the number 
	 * of iterations (k) will be estimate automatically.
	 */
	public static final int PARAM_RANSAC_ITERATION = 50000;

	/**
	 * % of points required to consider the model valid (d).
	 */
	public static final double PARAM_MINIMUM_NO_OF_POINTS = 0.10 / 100.0;

	/**
	 * Threshold value (seconds) for determining when a data 
	 * point fits a model (t)
	 */
	public static final double PARAM_THRESHOLD_VALUE = 15 / 60.0;
	
	/**
	 * Switch between polynomial model or lineal model
	 */
	public static final boolean PARAM_LINEAR_MODEL = true;
	
	/**
	 * If checked, only rows having same charge state can be aligned
	 */
	public static final boolean PARAM_REQUIRE_SAME_CHARGE_STATE = false;	
	
	/**
	 * How much weight to allocate to the various weights
	 */
	public static final double PARAM_ALPHA = 0.5;
	
	/**
	 * Use grouping information during alignment ?
	 */
	public static final boolean USE_GROUP = false;	

	/**
	 * Exact or approximate matching
	 */
	public static final boolean EXACT_MATCH = false;	
	
	/**
	 * Use peak shape info during grouping ?
	 */
	public static final boolean USE_PEAK_SHAPE = false;	
	
	/**
	 * OpenMS parameter
	 */
	public static final double PARAM_MZ_PAIR_MAX_DISTANCE = 0.5;
	
	
}