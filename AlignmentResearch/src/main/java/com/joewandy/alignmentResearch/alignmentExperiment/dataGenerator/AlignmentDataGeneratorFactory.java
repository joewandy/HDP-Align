package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;


public class AlignmentDataGeneratorFactory {

	// generate alignment data from benchmark dataset
	public static final String FEATURE_XML_DATA = "featureXml";

	// generate alignment data from sima data format
	public static final String SIMA_DATA = "simaFormat";
	
	// generate alignment data from generative model
	public static final String GENERATIVE_MODEL_DATA = "generativeModel";

	// generate alignment data from peakml data
	public static final String PEAKML_DATA = "peakML";
	
	public static AlignmentDataGenerator getAlignmentDataGenerator(MultiAlignCmdOptions options) {

		AlignmentDataGenerator generator = null; 
		if (AlignmentDataGeneratorFactory.FEATURE_XML_DATA.equals(options.dataType)) {				
			generator = new FeatureXMLDataGenerator(options.inputDirectory, options.gt);
		} else if (AlignmentDataGeneratorFactory.SIMA_DATA.equals(options.dataType)) {				
				generator = new SimaFormatDataGenerator(options.inputDirectory, options.gt);
		} else if (AlignmentDataGeneratorFactory.GENERATIVE_MODEL_DATA.equals(options.dataType)) {				
			GenerativeModelParameter params = options.generativeParams;
			generator = new GenerativeModelDataGenerator(options.inputDirectory, params);
		}
		
		return generator;

	}
	
}
