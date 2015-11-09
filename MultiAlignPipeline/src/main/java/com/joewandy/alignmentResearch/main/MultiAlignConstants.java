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

import java.util.ArrayList;
import java.util.List;

public class MultiAlignConstants {
		
	// false if the mass tolerance is absolute, true if it's relative value (in parts-per-million)
	public static final boolean USE_PPM = false;
	public static final String MASS_FORMAT = "%.5f";
	
	public static final String GROUPING_METHOD_GREEDY = "greedy";
	public static final String GROUPING_METHOD_MIXTURE = "mixture";
	public static final String GROUPING_METHOD_POSTERIOR = "posterior";
	public static final String GROUPING_METHOD_MATLAB_POSTERIOR = "matlabPosterior";
	public static final String GROUPING_METHOD_METASSIGN_MIXTURE = "metAssignMixture";
	public static final String GROUPING_METHOD_METASSIGN_POSTERIOR = "metAssignPosterior";

	public static final int GROUPING_METHOD_NUM_SAMPLES = 200;
	public static final int GROUPING_METHOD_BURN_IN = 100;
	public static final boolean ALWAYS_RECLUSTER = false;
	public static final double GROUPING_METHOD_RT_TOLERANCE = 5;
	public static final double GROUPING_METHOD_ALPHA = 1;
	public static final double GROUPING_MIN_CORR_SIGNAL = 0.90;
	
	public static final double HDP_ALPHA_RT = 10;
	public static final double HDP_ALPHA_MASS = 100;
	public static final double HDP_TOP_ALPHA = 10;
	public static final double HDP_GLOBAL_RT_CLUSTER_STDEV = 20;
	public static final double HDP_LOCAL_RT_CLUSTER_STDEV = 2;
	public static final double HDP_MASS_TOLERANCE = 2;
	public static final boolean HDP_SPEED_UP = false;
	public static final int HDP_SPEED_UP_NUM_SAMPLE = 100;
	public static final int HDP_REF_FILE_IDX = -1; // not using any reference file

	public static final String SCORING_METHOD_HDP_MASS_RT_JAVA = "hdpmassrtjava";
	public static final String SCORING_METHOD_HDP_RT_JAVA = "hdprtjava";
	public static final String SCORING_METHOD_HDP_MASS_RT = "hdpmassrt";
	public static final String SCORING_METHOD_HDP_RT = "hdprt";

	public static final String IONISATION_MODE_POSITIVE = "pos";
	public static final String IONISATION_MODE_NEGATIVE = "neg";	
	public static final List<String> adductListPositive = new ArrayList<String>();
	static {
		adductListPositive.add("M+2H");
		adductListPositive.add("M+H");
		adductListPositive.add("M+ACN+H");
		adductListPositive.add("2M+Na");
		adductListPositive.add("M+H+NH4");
		adductListPositive.add("M+NH4");
		adductListPositive.add("M+ACN+Na");
		adductListPositive.add("2M+ACN+H");
		adductListPositive.add("M+ACN+2H");
		adductListPositive.add("M+Na");
		adductListPositive.add("M+2ACN+H");
		adductListPositive.add("M+2ACN+2H");
		adductListPositive.add("M+CH3OH+H");
		adductListPositive.add("2M+H");
	}

	public static final List<String> adductListNegative = new ArrayList<String>();
	static {
		adductListNegative.add("M-H2O-H");
		adductListNegative.add("M+K-2H");
		adductListNegative.add("M-H");
		adductListNegative.add("M+FA-H");
		adductListNegative.add("M+Na-2H");
		adductListNegative.add("2M-H");
		adductListNegative.add("M+Cl");
		adductListNegative.add("2M+FA-H");
	}
	
	// use Lange, et al. (2008) measure
	public static final String PERFORMANCE_MEASURE_LANGE = "lange";

	// use my own performance measure
	public static final String PERFORMANCE_MEASURE_COMBINATION = "combination";
	
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

	// precursor clustering parameters
	public static final double PRECURSOR_WITHIN_FILE_MASS_TOL = 1.0;
	public static final double PRECURSOR_WITHIN_FILE_RT_TOL = 5.0;
	public static final double PRECURSOR_ACROSS_FILE_MASS_TOL = 4;
	public static final double PRECURSOR_ACROSS_FILE_RT_TOL = 10;
	public static final double PRECURSOR_ALPHA_MASS = 1.0;
	public static final double PRECURSOR_ALPHA_RT = 100.0;
	public static final double PRECURSOR_T = 0;
	public static final int PRECURSOR_MASS_CLUSTERING_NO_ITERS = 100;
	public static final int PRECURSOR_RT_CLUSTERING_NSAMPS = 200;
	public static final int PRECURSOR_RT_CLUSTERING_BURNIN = 100;
	public static final int PRECURSOR_MATCH_MODE = 0;
	public static final int SEED = -1;
	
	
}