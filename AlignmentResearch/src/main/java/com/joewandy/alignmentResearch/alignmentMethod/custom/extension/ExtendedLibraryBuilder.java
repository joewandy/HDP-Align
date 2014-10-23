package com.joewandy.alignmentResearch.alignmentMethod.custom.extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibraryEntry;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class ExtendedLibraryBuilder {

	private AlignmentMethodParam param;
	private List<AlignmentFile> dataList;
	
	public ExtendedLibraryBuilder(List<AlignmentFile> dataList, AlignmentMethodParam param) {
		this.dataList = dataList;
		this.param = param;
	}
	
	public List<AlignmentLibrary> buildPrimaryLibrary() {
		
		System.out.println("Building pairwise library ...");
		
		// id must be unique to all libraries !!
		int libraryID = 0;
		BlockingQueue<AlignmentLibrary> libraryQueue = new ArrayBlockingQueue<AlignmentLibrary>(1);
		List<AlignmentLibrary> libraries = new ArrayList<AlignmentLibrary>();
		for (int i = 0; i < dataList.size(); i++) {
			for (int j = 0; j < dataList.size(); j++) {
				if (i==j) {
					continue;
				}
				AlignmentFile data1 = dataList.get(i);
				AlignmentFile data2 = dataList.get(j);
				Runnable builder = new MaxWeightLibraryBuilder(libraryQueue, libraryID, param, data1, data2);		
				PairwiseLibraryBuilder pb = (PairwiseLibraryBuilder) builder;
				AlignmentLibrary library = pb.producePairwiseLibrary();							
				libraryID++;
				libraries.add(library);
			}			
		}
		return libraries;

	}
	
	public ExtendedLibrary combineLibraries(List<AlignmentLibrary> libraries) {
		
		System.out.println("Combining all pairwise libraries ...");
	
		ExtendedLibrary combinedLibrary = new ExtendedLibrary(param.getMassTolerance(), param.getRtTolerance());
		
		for (AlignmentLibrary library : libraries) {

			List<ExtendedLibraryEntry> alignedPairs = library.getAlignedFeatures();
			
			// get each pairwise aligned features in this primary library
			for (ExtendedLibraryEntry alignedPair : alignedPairs) {
				
				Feature feature1 = alignedPair.getFeature1();
				Feature feature2 = alignedPair.getFeature2();
				double score = alignedPair.getScore();
				double weight = alignedPair.getWeight();
				combinedLibrary.putEntry(feature1, feature2, score, weight);
				
			}
						
		}

		return combinedLibrary;

	}
		
}
