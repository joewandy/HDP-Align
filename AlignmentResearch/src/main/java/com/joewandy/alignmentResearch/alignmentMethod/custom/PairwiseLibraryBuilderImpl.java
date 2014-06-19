package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.DistanceCalculator;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.MahalanobisDistanceCalculator;

public abstract class PairwiseLibraryBuilderImpl implements Runnable, PairwiseLibraryBuilder {

	protected BlockingQueue<AlignmentLibrary> queue;

	protected int libraryID;
	protected AlignmentMethodParam param;
	protected AlignmentFile data1;
	protected AlignmentFile data2;

	public PairwiseLibraryBuilderImpl(BlockingQueue<AlignmentLibrary> queue,
			int libraryID, AlignmentMethodParam param, AlignmentFile data1, AlignmentFile data2) {
		this.queue = queue;
		this.libraryID = libraryID;
		this.param = param;
		this.data1 = data1;
		this.data2 = data2;
	}

	public void run() {
		try {
			queue.put(producePairwiseLibrary());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public AlignmentLibrary producePairwiseLibrary() {
		
		System.out.println("PairwiseLibraryBuilder #" + libraryID + " running");
		
		List<AlignmentFile> files = new ArrayList<AlignmentFile>();
		files.add(data1);
		files.add(data2);
		AlignmentMethod pairwiseAligner = getAlignmentMethod(files);

		AlignmentList result = pairwiseAligner.align();
		List<AlignmentRow> rows = result.getRows();

		AlignmentLibrary library = new AlignmentLibrary(libraryID, data1, data2);
		int count = 0;
		for (AlignmentRow row : rows) {

			// TODO: hack .. mark all features as unaligned, necessary when doing the final alignment later
			// since we're not processing any features that have been aligned
			for (Feature f : row.getFeatures()) {
				f.setAligned(false);
			}

			if (row.getScore() > 0) {
				count++;
			}

			// consider only rows containing pairwise alignment
			if (row.getFeaturesCount() == 2) {
				Feature[] features = row.getFeatures().toArray(new Feature[0]);
				Feature f1 = features[0];
				Feature f2 = features[1];
				double score = row.getScore();
				library.addAlignedPair(f1, f2, score, 1);					
			}
			
		}

		// just to make sure that scores set correctly ..
//		assert(count>0);		
		return library;
		
	}

	protected abstract AlignmentMethod getAlignmentMethod(List<AlignmentFile> files);

}
