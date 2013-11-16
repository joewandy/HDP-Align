package com.joewandy.alignmentResearch.noiseModel;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;

/**
 * Defines an interface for noise models in alignment data<br/>
 * <ul>
 * 	<li>
 * 		KeepByGroundTruthFeatures - retains only features present in ground truth, to make the problem easier. 
 * 		Use only for testing of algorithms.
 * 	</li>
 * 	<li>
 * 		GlobalRetentionShiftNoise - introduce a constant global shift noise to retention time, due to poor 
 * 		chromatographic condition. This should be a Gaussian noise with some mean & stdev.
 * 	</li>
 * 	<li>
 * 		LocalRetentionShiftNoise - introduce a local non-linear shift noise to retention time ..?
 * 	</li>
 * 	<li>
 * 		MissingPeaksNoise - introduce missing peaks with low expected raw abundance. Represents lack of sensitivity in 
 * 		the instrument, or internal noise filtering that's too strict during the generative process.
 * 	</li>
 * 	<li>
 * 		ContaminantPeaksNoise - add peaks that are produced by contaminants in the sample. These are singleton peaks that 
 * 		shouldn't really be aligned / grouped to anything else.
 * 	</li>
 * 	<li>
 * 		ExperimentalConditionNoise - represents systematic error due to variation in experimental condition
 * 	</li>
 * 	<li>
 * 		MeasurementNoise - represents instrumental measurement error, for now we use a combined multiplicative + additive
 * 		Gaussian noise.
 * 	</li>
 * </ul>
 * @author joewandy
 *
 */
public interface AlignmentNoise {

	public void addNoise(AlignmentData data);
	
}
