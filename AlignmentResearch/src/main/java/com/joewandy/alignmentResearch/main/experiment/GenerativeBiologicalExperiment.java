package com.joewandy.alignmentResearch.main.experiment;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGenerator;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.AlignmentDataGeneratorFactory;
import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.model.AlignmentFile;

public class GenerativeBiologicalExperiment extends GenerativeExperiment implements MultiAlignExperiment {

	private AlignmentDataGenerator dataGenerator;
	
	public GenerativeBiologicalExperiment(MultiAlignCmdOptions options) {
		this.dataGenerator = AlignmentDataGeneratorFactory
				.getAlignmentDataGenerator(options);		
	}
	
	protected AlignmentData getData(MultiAlignCmdOptions options, int[] fileIndices, int currentIter)
			throws FileNotFoundException {

		AlignmentData data = dataGenerator.generateByIteration(currentIter);
		for (AlignmentFile file : data.getAlignmentDataList()) {
			System.out.println("test on " + file.getFilename());
		}
		return data;

	}
	
	@Override
	protected void setCommonExperimentalSettings(MultiAlignCmdOptions options) {
		options.dataType = AlignmentDataGeneratorFactory.GENERATIVE_MODEL_DATA;
		options.generativeParams.setExpType(GenerativeModelParameter.ExperimentType.BIOLOGICAL);
//		double[] as = {1.0, 0.75, 0.5, 0.25};
//		options.generativeParams.setAs(as);
//		options.generativeParams.setA(0.5);
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
//		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN);
//		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_OPENMS);		
//		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA);	
//		methods.add(AlignmentMethodFactory.ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_HIERARCHICAL);				
		return methods;
	}

}
