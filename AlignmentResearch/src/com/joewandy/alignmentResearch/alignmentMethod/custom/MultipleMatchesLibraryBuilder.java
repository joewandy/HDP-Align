package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;

public class MultipleMatchesLibraryBuilder extends PairwiseLibraryBuilderImpl implements Runnable, PairwiseLibraryBuilder {

	public MultipleMatchesLibraryBuilder(BlockingQueue<AlignmentLibrary> queue,
			int libraryID, double massTolerance, double rtTolerance, AlignmentFile data1, AlignmentFile data2) {
		super(queue, libraryID, massTolerance, rtTolerance, data1, data2);
	}

	@Override
	protected AlignmentMethod getAlignmentMethod(List<AlignmentFile> files,
			AlignmentMethodParam param) {
		AlignmentMethod pairwiseAligner = new MultipleMatchesAlignment(files, param);
		return pairwiseAligner;
	}



}
