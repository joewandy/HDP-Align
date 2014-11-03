package com.joewandy.alignmentResearch.alignmentMethod;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.custom.baseline.BaselineAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.join.MyJoinAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.maxWeight.HierarchicalAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.custom.maxWeight.MyMaximumMatchingAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.MzMineJoinAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.MzMineRansacAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.OpenMSAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.PythonMW;
import com.joewandy.alignmentResearch.alignmentMethod.external.SimaAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.HdpAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class AlignmentMethodFactory {

	// MZMatch current greedy alignment method
	public static final String ALIGNMENT_METHOD_GREEDY = "greedy";
	
	// a simple greedy alignment algorithm as baseline
	public static final String ALIGNMENT_METHOD_BASELINE = "baseline";

	// my own aligners
	public static final String ALIGNMENT_METHOD_MY_JOIN = "myJoin";
	public static final String ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_REFERENCE = "myMaxWeightRef";
	public static final String ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_HIERARCHICAL = "myMaxWeight";
	public static final String ALIGNMENT_METHOD_MY_HDP_ALIGNMENT = "myHdp";

	// same but implemented in python
	public static final String ALIGNMENT_METHOD_PYTHON_MW = "pythonMW";
	
	// calls mzMine Join aligner
	public static final String ALIGNMENT_METHOD_MZMINE_JOIN = "join";
	
	// calls mzMine RANSAC aligner
	public static final String ALIGNMENT_METHOD_MZMINE_RANSAC = "ransac";
	
	// calls SIMA alignment
	public static final String ALIGNMENT_METHOD_SIMA = "sima";

	// calls OpenMS alignment
	public static final String ALIGNMENT_METHOD_OPENMS = "openMS";
	
	
	public static AlignmentMethod getAlignmentMethod(final String method, AlignmentMethodParam param, 
			AlignmentData data, ExtendedLibrary library) {

		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		AlignmentMethod aligner = null; 
		if (AlignmentMethodFactory.ALIGNMENT_METHOD_BASELINE.equals(method)) {				
			aligner = new BaselineAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_JOIN.equals(method)) {
			aligner = new MyJoinAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_REFERENCE.equals(method)) {
			aligner = new MyMaximumMatchingAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.equals(method)) {
			aligner = new MzMineRansacAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.equals(method)) {
			aligner = new MzMineJoinAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.equals(method)) {
			aligner = new SimaAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_OPENMS.equals(method)) {
			aligner = new OpenMSAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_MAXIMUM_WEIGHT_MATCHING_HIERARCHICAL.equals(method)) {
			aligner = new HierarchicalAlignment(alignmentDataList, param, library);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_HDP_ALIGNMENT.equals(method)) {
			aligner = new HdpAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_PYTHON_MW.equals(method)) {
			aligner = new PythonMW(alignmentDataList, param);
		}		

		return aligner;

	}
	
}
