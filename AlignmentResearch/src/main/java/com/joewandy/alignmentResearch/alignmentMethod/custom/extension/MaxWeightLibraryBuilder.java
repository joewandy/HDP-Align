package com.joewandy.alignmentResearch.alignmentMethod.custom.extension;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.custom.maxWeight.MyMaximumMatchingAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;

public class MaxWeightLibraryBuilder extends PairwiseLibraryBuilderImpl implements Runnable, PairwiseLibraryBuilder {

	public MaxWeightLibraryBuilder(BlockingQueue<AlignmentLibrary> queue,
			int libraryID, AlignmentMethodParam param, AlignmentFile data1, AlignmentFile data2) {
		super(queue, libraryID, param, data1, data2);
	}

	@Override
	protected AlignmentMethod getAlignmentMethod(List<AlignmentFile> files) {
		AlignmentMethod pairwiseAligner = new MyMaximumMatchingAlignment(files, param);
		return pairwiseAligner;
	}



}
