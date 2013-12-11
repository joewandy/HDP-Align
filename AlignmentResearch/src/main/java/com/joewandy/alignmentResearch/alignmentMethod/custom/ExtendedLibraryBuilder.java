package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.paukov.combinatorics.Factory;
import org.paukov.combinatorics.Generator;
import org.paukov.combinatorics.ICombinatoricsVector;

import com.joewandy.alignmentResearch.main.MultiAlign;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibraryEntry;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class ExtendedLibraryBuilder {

	private List<AlignmentFile> dataList;
	private double massTolerance;
	private double rtTolerance;
	
	public ExtendedLibraryBuilder(List<AlignmentFile> dataList, double massTolerance, double rtTolerance) {
		this.dataList = dataList;
		this.massTolerance = massTolerance;
		this.rtTolerance = rtTolerance;
	}
	
	public Map<Double, List<AlignmentLibrary>> buildPrimaryLibrary() {
		
		System.out.println("BUILDING PRIMARY LIBRARIES");

		Map<Double, List<AlignmentLibrary>> metaLibraries = new HashMap<Double, List<AlignmentLibrary>>();
		
		// id must be unique to all libraries !!
		int libraryID = 0;

		// Create the initial vector
		ICombinatoricsVector<AlignmentFile> initialVector = Factory.createVector(dataList);

		// Create a simple combination generator to generate 2-combinations of
		// the initial vector
		Generator<AlignmentFile> gen = Factory.createSimpleCombinationGenerator(
				initialVector, 2);

		int noOfCombo = (int) gen.getNumberOfGeneratedObjects();
		System.out.println("============ LIBRARY : ENTRIES = " + noOfCombo + " ============");
		
		// Print all possible combinations
		BlockingQueue<AlignmentLibrary> libraryQueue = new ArrayBlockingQueue<AlignmentLibrary>(noOfCombo);
		int noOfThreads = 0;
		List<AlignmentLibrary> libraries = new ArrayList<AlignmentLibrary>();
		for (ICombinatoricsVector<AlignmentFile> combination : gen) {

			/* 
			 * pairwise align data1 and data2
			 */
			AlignmentFile data1 = combination.getValue(0);
			AlignmentFile data2 = combination.getValue(1);	
			Runnable builder = new MaxWeightLibraryBuilder(libraryQueue, libraryID, massTolerance, rtTolerance, data1, data2);

//			if (MultiAlign.PARALLEL_LIBRARY_BUILD) {
//				Thread t = new Thread(builder);
//				t.start();					
//			} else {
//				PairwiseLibraryBuilder pb = (PairwiseLibraryBuilder) builder;
//				AlignmentLibrary library = pb.producePairwiseLibrary();
//				try {
//					libraryQueue.put(library);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}	
//			}
//							
//			libraryID++;
//			noOfThreads++;
//
//			List<AlignmentLibrary> libraries = new ArrayList<AlignmentLibrary>();
//			int resultCounter = 0;
//			while (resultCounter < noOfThreads) {
//				try {
//					AlignmentLibrary library = libraryQueue.take();
//					libraries.add(library);
//					resultCounter++;
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//			System.out.println("All libraries collected");
//			
//			metaLibraries.put(rtTolerance, libraries);

			PairwiseLibraryBuilder pb = (PairwiseLibraryBuilder) builder;
			AlignmentLibrary library = pb.producePairwiseLibrary();							
			libraryID++;
			libraries.add(library);
									
		}		
		metaLibraries.put(rtTolerance, libraries);

		// store the normalised scores ?
//		double max = Double.MIN_VALUE;
//		for (AlignmentLibrary library : libraries) {
//			if (library.getWeight() > max) {
//				max = library.getWeight();
//			}
//		}
//		for (AlignmentLibrary library : libraries) {
//			libraryScores.put(library, library.getNormalisedWeight(max));
//		}
		
		// store the non-normalised scores 
//		for (AlignmentLibrary library : libraries) {
//			libraryScores.put(library, library.getWeight());
//		}
				
		return metaLibraries;

	}
	
	public ExtendedLibrary combineLibraries(
			Map<Double, List<AlignmentLibrary>> metaLibraries) {
		
		System.out.println("COMBINING PRIMARY LIBRARIES");
	
		ExtendedLibrary combinedLibrary = new ExtendedLibrary(massTolerance, rtTolerance);
		
		// for every retention time tolerance and its associated primary libraries ..
		for (Entry<Double, List<AlignmentLibrary>> entry : metaLibraries.entrySet()) {
						
			// iterate through all the primary libraries produced at varying rt tolerances and pool them
			List<AlignmentLibrary> libraries = entry.getValue();
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
				
				// add the graph too
				if (library.getGraph() != null) {
					combinedLibrary.add(library.getGraph());					
				}
				
			}
			
		}

		printLibrary(combinedLibrary);
		return combinedLibrary;

	}
	
	public ExtendedLibrary extendLibrary(ExtendedLibrary combinedLibrary) {
		
		System.out.println("CONSTRUCTING LIBRARY EXTENSION");
		
		// library now contains all pairwise alignments from two files
		ExtendedLibrary extendedLibrary = new ExtendedLibrary(combinedLibrary,
				massTolerance, rtTolerance);
		
		// now extend to triplets
		// Create the initial vector
		ICombinatoricsVector<AlignmentFile> initialSet = Factory.createVector(dataList);

		// Create an instance of the subset generator
		Generator<AlignmentFile> subsetGen = Factory.createSubSetGenerator(initialSet);

		// Print the subsets
		for (ICombinatoricsVector<AlignmentFile> subset : subsetGen) {
						
			if (subset.getSize() == 3) {
			
				System.out.println(subset);
				
				List<AlignmentFile> temp = subset.getVector();
				ICombinatoricsVector<AlignmentFile> initialVector = Factory.createVector(temp);
				Generator<AlignmentFile> permGen = Factory.createPermutationGenerator(
						initialVector);

				// Print all possible combinations of triplets
				System.out.println("No. of triplet sets = " + permGen.getNumberOfGeneratedObjects());
				BlockingQueue<ExtendedLibrary> libraryQueue = new ArrayBlockingQueue<ExtendedLibrary>(6);
				int noOfThreads = 0;
				for (ICombinatoricsVector<AlignmentFile> permutation : permGen) {

					// list all the triplets
					AlignmentFile data1 = permutation.getValue(0);
					AlignmentFile data2 = permutation.getValue(1);
					AlignmentFile data3 = permutation.getValue(2);
					Thread t = new Thread(new ExtensionLibraryBuilder(libraryQueue, combinedLibrary, 
							noOfThreads, data1, data2, data3, massTolerance, rtTolerance));
					t.start();
					noOfThreads++;
					
				}

				int resultCounter = 0;
				while (resultCounter < noOfThreads) {
					try {
						ExtendedLibrary extension = libraryQueue.take();
						extendedLibrary.add(extension);
						resultCounter++;
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				System.out.println("All libraries collected");
				
			}
			
		}
		
		printLibrary(extendedLibrary);
		return extendedLibrary;
		
	}

	private void printLibrary(ExtendedLibrary combinedLibrary) {

		System.out.println("Library size = " + combinedLibrary.getEntrySize());

		List<ExtendedLibraryEntry> entries = new ArrayList<ExtendedLibraryEntry>(combinedLibrary.getEntries());
		Collections.sort(entries);
		Collections.reverse(entries);
		// printLibraryEntries(entries);

	}
	
	private void printLibraryEntries(List<ExtendedLibraryEntry> entries) {
		
		int counter = 0;
		int lower = (entries.size()/2)-5;
		int upper = (entries.size()/2)+5;
		int end = (entries.size()) - 10;
		for (ExtendedLibraryEntry entry : entries) {
			
			boolean printEntry = false;
			boolean printDots = false;
			
			if (counter > 0 && counter < 10) {
				printEntry = true;
			} else if (counter == 10) {
				printDots = true;
			} else if (counter > lower && counter < upper) {
				printEntry = true;
			} else if (counter == upper) {
				printDots = true;
			} else if (counter > end) {
				printEntry = true;
			}

			if (printEntry) {
				System.out.println("\t" + entry);				
			} else if (printDots) {
				System.out.println("\t . . .");				
			}
			
			counter++;

		}
	}
	
}
