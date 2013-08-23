package com.joewandy.alignmentResearch.alignmentMethod;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.custom.BaselineAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.ConsistencyAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.CustomJoinAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.GroupingInfoAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.MatchingAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.MzMineJoinAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.MzMineRansacAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.SimaAlignment;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignmentOptions;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;

public class AlignmentMethodFactory {

	// a simple greedy alignment algorithm as baseline
	public static final String ALIGNMENT_METHOD_BASELINE = "baseline";

	// my own join aligner
	public static final String ALIGNMENT_METHOD_CUSTOM_JOIN = "customJoin";
	public static final String ALIGNMENT_METHOD_GROUPING_INFO = "groupingInfo";
	
	// calls mzMine Join aligner
	public static final String ALIGNMENT_METHOD_MZMINE_JOIN = "join";
	
	// calls mzMine RANSAC aligner
	public static final String ALIGNMENT_METHOD_MZMINE_RANSAC = "ransac";
	
	// calls SIMA alignment
	public static final String ALIGNMENT_METHOD_SIMA = "sima";

	// calls consistency-based alignment
	public static final String ALIGNMENT_METHOD_CONSISTENCY = "consistency";

	// calls matching-based alignment
	public static final String ALIGNMENT_METHOD_MATCHING = "matching";
	
	public static AlignmentMethod getAlignmentMethod(FeatureXMLAlignmentOptions options, AlignmentData data) {

		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		
		AlignmentMethod aligner = null; 
		if (AlignmentMethodFactory.ALIGNMENT_METHOD_BASELINE.equals(options.method)) {				
			aligner = new BaselineAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_CUSTOM_JOIN.equals(options.method)) {
			aligner = new CustomJoinAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_GROUPING_INFO.equals(options.method)) {
			aligner = new GroupingInfoAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.equals(options.method)) {
			aligner = new MzMineRansacAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.equals(options.method)) {
			aligner = new MzMineJoinAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.equals(options.method)) {
			aligner = new SimaAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_CONSISTENCY.equals(options.method)) {
			aligner = new ConsistencyAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow, 
					FeatureXMLAlignment.RTWINDOW_MULTIPLY);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MATCHING.equals(options.method)) {
			aligner = new MatchingAlignment(alignmentDataList, options.alignmentPpm, options.alignmentRtwindow);
		}
		
		return aligner;

	}
	
}
