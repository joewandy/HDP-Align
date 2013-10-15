package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.List;
import java.util.concurrent.BlockingQueue;

import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class ExtensionLibraryBuilder implements Runnable {

	private BlockingQueue<ExtendedLibrary> queue;

	private ExtendedLibrary combinedLibrary;
	private int counter;
	private AlignmentFile data1;
	private AlignmentFile data2;
	private AlignmentFile data3;
	private double dmz;
	private double drt;

	public ExtensionLibraryBuilder(BlockingQueue<ExtendedLibrary> queue, 
			ExtendedLibrary combinedLibrary, int counter,
			AlignmentFile data1, AlignmentFile data2, AlignmentFile data3,
			double dmz, double drt) {
		this.queue = queue;
		this.combinedLibrary = combinedLibrary;
		this.counter = counter;
		this.data1 = data1;
		this.data2 = data2;
		this.data3 = data3;
		this.dmz = dmz;
		this.drt = drt;
	}
	
	@Override
	public void run() {
		try {
			queue.put(produceTripletLibrary());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private ExtendedLibrary produceTripletLibrary() {
		
		System.out.println("Set #" + counter + " (" +
				data1.getFilenameWithoutExtension() + ", " + 
				data2.getFilenameWithoutExtension() + ", " + 
				data3.getFilenameWithoutExtension() + ")");

		ExtendedLibrary result = new ExtendedLibrary(dmz, drt);
		List<Feature> features1 = data1.getFeatures();
		List<Feature> features2 = data2.getFeatures();
		List<Feature> features3 = data3.getFeatures();
		for (Feature f1 : features1) {
			for (Feature f2 : features2) {

				// if we find an alignment between feature1 and feature2
				if (combinedLibrary.exist(f1, f2)) {
					
					// store its score
					double score12 = combinedLibrary.getEntryScore(f1, f2);

					for (Feature f3 : features3) {
					
						// then if we find an alignment between feature2 and feature3
						if (combinedLibrary.exist(f2, f3)) {
							
							// store its score too
							double score23 = combinedLibrary.getEntryScore(f2, f3);
							
							/* 
							 * From the triplet inspection, now we know there's 
							 * an alignment between feature1 and feature3. The score is
							 * the minimum of alignment scores for (feature1, feature2) and
							 * (feature2, feature3).
							 */
							double minScore = Math.min(score12, score23);
							
							/*
							 * Set the alignment score between (feature1, feature3) 
							 * to minScore if it doesn't exist. Otherwise add it to 
							 * the existing score
							 */
							result.putEntry(f1, f3, minScore, 0);
							
						}
					}
				}
			}
		}
		
		return result;

	}	

}
