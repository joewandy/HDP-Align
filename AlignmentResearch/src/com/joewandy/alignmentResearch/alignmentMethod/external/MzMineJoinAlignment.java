package com.joewandy.alignmentResearch.alignmentMethod.external;

import java.util.List;

import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.modules.MZmineProcessingModule;
import net.sf.mzmine.modules.peaklistmethods.alignment.join.JoinAlignerModule;
import net.sf.mzmine.modules.peaklistmethods.alignment.join.JoinAlignerParameters;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.parameters.parametertypes.BooleanParameter;
import net.sf.mzmine.parameters.parametertypes.DoubleParameter;
import net.sf.mzmine.parameters.parametertypes.MZTolerance;
import net.sf.mzmine.parameters.parametertypes.MZToleranceParameter;
import net.sf.mzmine.parameters.parametertypes.OptionalModuleParameter;
import net.sf.mzmine.parameters.parametertypes.PeakListsParameter;
import net.sf.mzmine.parameters.parametertypes.RTTolerance;
import net.sf.mzmine.parameters.parametertypes.RTToleranceParameter;
import net.sf.mzmine.taskcontrol.TaskListener;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;

public class MzMineJoinAlignment extends MzMineAlignment implements
		AlignmentMethod, TaskListener {

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
	public MzMineJoinAlignment(List<AlignmentFile> dataList,
			double massTolerance, double rtTolerance) {

		super(dataList, massTolerance, rtTolerance);
		this.alignerModule = new JoinAlignerModule();
		
	}
	
	protected ParameterSet prepareParameterSet(PeakList[] peakLists) {
		
		ParameterSet params = new JoinAlignerParameters();

		// pre-initialise mass tolerance & weight parameters
		
		double toleranceMZ = 0;
		double tolerancePpm = 0;
		if (this.usePpm) {
			tolerancePpm = this.massTolerance;
		} else {
			toleranceMZ = this.massTolerance;
		}
		final double mzWeight = 1;

		MZTolerance mzTolerance = new MZTolerance(toleranceMZ, tolerancePpm);
		MZToleranceParameter mzToleranceParam = params.getParameter(JoinAlignerParameters.MZTolerance);
		DoubleParameter mzWeightParam = params.getParameter(JoinAlignerParameters.MZWeight);
		
		mzToleranceParam.setValue(mzTolerance);
		mzWeightParam.setValue(mzWeight);
		
		// pre-initialise rt tolerance & weight parameters

		final boolean absolute = true;
		final double rtToleranceMinute = this.rtTolerance / 60.0;
		final double rtWeight = 1;
		
		RTTolerance rtTolerance = new RTTolerance(absolute, rtToleranceMinute);
		RTToleranceParameter rtToleranceParam = params.getParameter(JoinAlignerParameters.RTTolerance);
		DoubleParameter rtWeightParam = params.getParameter(JoinAlignerParameters.RTWeight);
		
		rtToleranceParam.setValue(rtTolerance);
		rtWeightParam.setValue(rtWeight);
		
		// misc parameters
		
		final boolean sameCharge = false;
		final boolean sameId = false;
		final boolean compareIsotope = false;
		
		BooleanParameter sameChargeParam = params.getParameter(JoinAlignerParameters.SameChargeRequired);
		BooleanParameter sameIdParam = params.getParameter(JoinAlignerParameters.SameIDRequired);
		OptionalModuleParameter compareIsotopeParam = params.getParameter(JoinAlignerParameters.compareIsotopePattern);
		
		sameChargeParam.setValue(sameCharge);
		sameIdParam.setValue(sameId);
		compareIsotopeParam.setValue(compareIsotope);
				
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
