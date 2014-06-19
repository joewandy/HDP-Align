package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;

public class BaselineMethodLibraryBuilder extends PairwiseLibraryBuilderImpl implements Runnable, PairwiseLibraryBuilder {

	public BaselineMethodLibraryBuilder(BlockingQueue<AlignmentLibrary> queue,
			int libraryID, AlignmentMethodParam param, AlignmentFile data1, AlignmentFile data2) {
		super(queue, libraryID, param, data1, data2);
	}

	@Override
	protected AlignmentMethod getAlignmentMethod(List<AlignmentFile> files) {
		AlignmentMethod pairwiseAligner = new BaselineAlignment(files, param);
		return pairwiseAligner;
	}



}
