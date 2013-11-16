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
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;

public class MzMineRansacAlignment extends MzMineAlignment implements
		AlignmentMethod, TaskListener {

	private MZmineProcessingModule alignerModule;
	private double rtToleranceBeforeMinute;
	private double rtToleranceAfterMinute;
	private int ransacIteration;
	private double nMinPoints;
	private double threshold;
	private boolean linearModel;
	private boolean sameChargeRequired;

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
	public MzMineRansacAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {
		super(dataList, param);
		this.alignerModule = new RansacAlignerModule();
		this.rtToleranceBeforeMinute = param.getRansacRtToleranceBeforeMinute();
		this.rtToleranceAfterMinute = param.getRansacRtToleranceAfterMinute();
		this.ransacIteration = param.getRansacIteration();
		this.nMinPoints = param.getRansacNMinPoints();
		this.threshold = param.getRansacThreshold();
		this.linearModel = param.isRansacLinearModel();
		this.sameChargeRequired = param.isRansacSameChargeRequired();
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
		RTTolerance rtToleranceBefore = new RTTolerance(absolute, rtToleranceBeforeMinute);
		RTToleranceParameter rtToleranceBeforeParam = params.getParameter(RansacAlignerParameters.RTToleranceBefore);
		RTTolerance rtToleranceAfter = new RTTolerance(absolute, rtToleranceAfterMinute);
		RTToleranceParameter rtToleranceAfterParam = params.getParameter(RansacAlignerParameters.RTToleranceAfter);
		
		rtToleranceBeforeParam.setValue(rtToleranceBefore);
		rtToleranceAfterParam.setValue(rtToleranceAfter);
		
		// pre-initialise other ransac parameters
		
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
