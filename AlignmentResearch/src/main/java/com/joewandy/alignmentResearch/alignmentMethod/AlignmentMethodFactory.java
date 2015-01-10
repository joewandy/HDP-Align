package com.joewandy.alignmentResearch.alignmentMethod;

import java.util.List;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.alignmentMethod.external.HdpAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.MzMineJoinAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.MzMineRansacAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.OpenMSAlignment;
import com.joewandy.alignmentResearch.alignmentMethod.external.PythonMW;
import com.joewandy.alignmentResearch.alignmentMethod.external.SimaAlignment;
import com.joewandy.alignmentResearch.model.AlignmentFile;

public class AlignmentMethodFactory {

	// MZMatch current greedy alignment method
	public static final String ALIGNMENT_METHOD_GREEDY = "greedy";
	
	// my own aligners
	public static final String ALIGNMENT_METHOD_MY_HDP_ALIGNMENT = "myHdp";
	public static final String ALIGNMENT_METHOD_GROUP_ONLY = "groupOnly";
	public static final String ALIGNMENT_METHOD_PYTHON_MW = "MW";
	
	// calls mzMine Join aligner
	public static final String ALIGNMENT_METHOD_MZMINE_JOIN = "join";
	
	// calls mzMine RANSAC aligner
	public static final String ALIGNMENT_METHOD_MZMINE_RANSAC = "ransac";
	
	// calls SIMA alignment
	public static final String ALIGNMENT_METHOD_SIMA = "sima";

	// calls OpenMS alignment
	public static final String ALIGNMENT_METHOD_OPENMS = "openMS";	
	
	public static AlignmentMethod getAlignmentMethod(String method, AlignmentMethodParam param, 
			AlignmentData data) {

		method = method.toLowerCase();
		List<AlignmentFile> alignmentDataList = data.getAlignmentDataList();
		AlignmentMethod aligner = null; 
		if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.toLowerCase().equals(method)) {
			aligner = new MzMineRansacAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.toLowerCase().equals(method)) {
			aligner = new MzMineJoinAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.toLowerCase().equals(method)) {
			aligner = new SimaAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_OPENMS.toLowerCase().equals(method)) {
			aligner = new OpenMSAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_HDP_ALIGNMENT.toLowerCase().equals(method)) {
			aligner = new HdpAlignment(alignmentDataList, param);
		} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_PYTHON_MW.toLowerCase().equals(method)) {
			aligner = new PythonMW(alignmentDataList, param);
		}

		return aligner;

	}
	
}
