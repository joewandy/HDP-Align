package com.joewandy.alignmentResearch.main.experiment;

import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;

public class GenerativeBiologicalExperiment extends GenerativeExperiment implements MultiAlignExperiment {

	@Override
	protected void setCommonExperimentalSettings(MultiAlignCmdOptions options) {
		options.dataType = AlignmentDataGeneratorFactory.GENERATIVE_MODEL_DATA;
		options.generativeParams.setExpType(GenerativeModelParameter.ExperimentType.BIOLOGICAL);
		options.generativeParams.setA(0.5);
	}

	@Override
	protected void setGroupingSettings(MultiAlignCmdOptions options) {
		options.useGroup = true;
		options.groupingMethod = MultiAlignConstants.GROUPING_METHOD_GREEDY;
		options.groupingRtWindow = 2;
		options.alpha = 0.3;
	}
	
	@Override
	protected List<String> getAllMethods() {
		List<String> methods = new ArrayList<String>();
		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN);
		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_OPENMS);		
		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA);	
//		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_HIERARCHICAL);				
		return methods;
	}

}
