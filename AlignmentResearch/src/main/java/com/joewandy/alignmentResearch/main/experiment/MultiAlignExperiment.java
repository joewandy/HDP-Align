package com.joewandy.alignmentResearch.main.experiment;

import java.util.List;

import com.joewandy.alignmentResearch.main.MultiAlignCmdOptions;

public interface MultiAlignExperiment {
	
	public static final String EXPERIMENT_TYPE_M1 = "M1";		
	public static final String EXPERIMENT_TYPE_P1P2 = "P1P2";	
	public static final String EXPERIMENT_TYPE_GENERATIVE_TECHNICAL_REPLICATES = "GT";		
	public static final String EXPERIMENT_TYPE_GENERATIVE_BIOLOGICAL_REPLICATES = "GB";		
	public static final String EXPERIMENT_TYPE_STANDARD = "standard";		
	public static final String EXPERIMENT_TYPE_GLYCO = "glyco";		
	public static final String EXPERIMENT_TYPE_PROTEO = "proteo";		

	public static final String EXPERIMENT_TYPE_HDP = "hdp";		
	
	public List<MultiAlignExpResult> performExperiment(MultiAlignCmdOptions options) throws Exception;

	public void printTestingResult(List<MultiAlignExpResult> results);
	
}
