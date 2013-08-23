package com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator;

import com.joewandy.alignmentResearch.main.FeatureXMLAlignmentOptions;


public class AlignmentDataGeneratorFactory {

	// generate alignment data from benchmark data
	public static final String ALIGNMENT_DATA_BENCHMARK = "benchmark";

	// generate alignment data from proteomic simulator
	public static final String ALIGNMENT_DATA_PROTEOMIC = "proteomic";

	// generate alignment data from metabolomic simulator
	public static final String ALIGNMENT_DATA_METABOLOMIC = "metabolomic";

	// generate alignment data from real data
	public static final String ALIGNMENT_DATA_REAL = "real";
	
	public static AlignmentDataGenerator getAlignmentDataGenerator(FeatureXMLAlignmentOptions options) {

		AlignmentDataGenerator generator = null; 
		if (AlignmentDataGeneratorFactory.ALIGNMENT_DATA_BENCHMARK.equals(options.dataType)) {				
			generator = new BenchmarkDataGenerator(options.inputDirectory, options.gt);
		}
		
		return generator;

	}
	
}
