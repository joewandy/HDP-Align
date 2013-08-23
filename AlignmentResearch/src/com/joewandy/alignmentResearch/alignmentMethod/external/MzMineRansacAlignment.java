package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.util.List;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.peaklistmethods.alignment.join.JoinAlignerParameters;
import net.sf.mzmine.modules.peaklistmethods.alignment.ransac.RansacAlignerModule;
import net.sf.mzmine.modules.peaklistmethods.alignment.ransac.RansacAlignerParameters;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.MZTolerance;
import net.sf.mzmine.parameters.parametertypes.MZToleranceParameter;
import net.sf.mzmine.parameters.parametertypes.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.RTTolerance;
import net.sf.mzmine.parameters.parametertypes.RTToleranceParameter;
import net.sf.mzmine.taskcontrol.TaskListener;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;

public class MzMineRansacAlignment extends MzMineAlignment implements
		AlignmentMethod, TaskListener {

	/** 
	 * This value sets the range, in terms of retention time, to create the model 
	 * using RANSAC and non-linear regression algorithm. 
	 * Maximum allowed retention time difference
	 */
	private static final int PARAM_RT_TOLERANCE_BEFORE_CORRECTION = 300;

	/**
	 * Maximum number of iterations allowed in the algorithm to find the right model 
	 * consistent in all the pairs of aligned peaks. When its value is 0, the number 
	 * of iterations (k) will be estimate automatically.
	 */
	private static final int PARAM_RANSAC_ITERATION = 50000;
//	private static final int PARAM_RANSAC_ITERATION = 0;

	/**
	 * % of points required to consider the model valid (d).
	 */
	private static final double PARAM_MINIMUM_NO_OF_POINTS = 0.10;

	/**
	 * Threshold value (minutes) for determining when a data 
	 * point fits a model (t)
	 */
	private static final int PARAM_THRESHOLD_VALUE = 15;
	
	/**
	 * Switch between polynomial model or lineal model
	 */
	private static final boolean PARAM_LINEAR_MODEL = true;
	
	/**
	 * If checked, only rows having same charge state can be aligned
	 */
	private static final boolean PARAM_REQUIRE_SAME_CHARGE_STATE = false;	

	private MZmineProcessingModule alignerModule;

	/**
	 * Creates a simple join aligner
	 * 
	 * @param dataList
	 *            List of feature data to align
	 * @param massTolerance
	 *            Mass tolerance in ppm
	 * @param rtTolerance
	 *            Retention time tolerance in seconds
	 * @param rtDrift
	 */
	public MzMineRansacAlignment(List<AlignmentFile> dataList,
			double massTolerance, double rtTolerance) {

		super(dataList, massTolerance, rtTolerance);
		this.alignerModule = new RansacAlignerModule();
		
	}
	
	protected ParameterSet prepareParameterSet(PeakList[] peakLists) {
		
		ParameterSet params = new RansacAlignerParameters();

		// pre-initialise mass tolerance
		
		double toleranceMZ = 0;
		double tolerancePpm = 0;
		if (this.usePpm) {
			tolerancePpm = this.massTolerance;
		} else {
			toleranceMZ = this.massTolerance;
		}

		MZTolerance mzTolerance = new MZTolerance(toleranceMZ, tolerancePpm);
		MZToleranceParameter mzToleranceParam = params.getParameter(RansacAlignerParameters.MZTolerance);		
		mzToleranceParam.setValue(mzTolerance);
		
		// pre-initialise rt tolerance before & after parameters

		final boolean absolute = true;
		final double rtToleranceBeforeMinute = MzMineRansacAlignment.PARAM_RT_TOLERANCE_BEFORE_CORRECTION / 60.0;
		final double rtToleranceAfterMinute = this.rtTolerance / 60.0;
		
		RTTolerance rtToleranceBefore = new RTTolerance(absolute, rtToleranceBeforeMinute);
		RTToleranceParameter rtToleranceBeforeParam = params.getParameter(RansacAlignerParameters.RTToleranceBefore);
		RTTolerance rtToleranceAfter = new RTTolerance(absolute, rtToleranceAfterMinute);
		RTToleranceParameter rtToleranceAfterParam = params.getParameter(RansacAlignerParameters.RTToleranceAfter);
		
		rtToleranceBeforeParam.setValue(rtToleranceBefore);
		rtToleranceAfterParam.setValue(rtToleranceAfter);
		
		// pre-initialise other ransac parameters
		
		final int ransacIteration = MzMineRansacAlignment.PARAM_RANSAC_ITERATION;
		final double nMinPoints = MzMineRansacAlignment.PARAM_MINIMUM_NO_OF_POINTS/100.0;
		final double threshold = MzMineRansacAlignment.PARAM_THRESHOLD_VALUE;
		final boolean linearModel = MzMineRansacAlignment.PARAM_LINEAR_MODEL;
		final boolean sameChargeRequired = MzMineRansacAlignment.PARAM_REQUIRE_SAME_CHARGE_STATE;

		params.getParameter(RansacAlignerParameters.Iterations).setValue(ransacIteration);
		params.getParameter(RansacAlignerParameters.NMinPoints).setValue(nMinPoints);
		params.getParameter(RansacAlignerParameters.Margin).setValue(threshold);		
		params.getParameter(RansacAlignerParameters.Linear).setValue(linearModel);
		params.getParameter(RansacAlignerParameters.SameChargeRequired).setValue(sameChargeRequired);		
				
		// set the peaklist to align
		PeakListsParameter plp = params
				.getParameter(JoinAlignerParameters.peakLists);
		plp.setValue(peakLists);
		return params;

	}	
	
	protected MZmineProcessingModule getAlignerModule() {
		return this.alignerModule;
	}

}
