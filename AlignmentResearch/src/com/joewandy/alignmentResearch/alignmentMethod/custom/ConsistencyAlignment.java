package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.List;
import java.util.Map;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentLibrary;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.util.GraphEdgeConstructor;

public class ConsistencyAlignment extends BaseAlignment implements AlignmentMethod {

	private GraphEdgeConstructor edgeConstructor;
	private int windowMultiply;
	private double alpha;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public ConsistencyAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		
		super(dataList, param);
		this.windowMultiply = param.getRtWindowMultiply();
		this.alpha = param.getAlpha();
	}
	
	@Override
	protected AlignmentList matchFeatures() {
		
		ExtendedLibraryBuilder builder = new ExtendedLibraryBuilder(dataList, massTolerance, rtTolerance, windowMultiply);
		
		Map<Double, List<AlignmentLibrary>> metaLibraries = builder.buildPrimaryLibrary();
		
		// all these libraries align the same pairwise files, but with different RT tolerances
		ExtendedLibrary extendedLibrary = builder.combineLibraries(metaLibraries);
//		ExtendedLibrary extendedLibrary = builder.extendLibrary(builder.combineLibraries(metaLibraries));

		System.out.println("ALIGNMENT");
		AlignmentList alignedList = align(extendedLibrary);

		// printForMatlab(extendedLibrary, alignedList);

		return alignedList;
				
	}

	private void printForMatlab(ExtendedLibrary extendedLibrary,
			AlignmentList alignedList) {
		System.out.print("row_scores = [ ");
		for(AlignmentRow row : alignedList.getRows()) {
			double sum = 0;
			int count = 0;
			for (Feature f1 : row.getFeatures()) {
				for (Feature f2 : row.getFeatures()) {
					if (f1 != f2) {
						sum += extendedLibrary.getEntryScore(f1, f2);
						count++;
					}
				}
			}
			if (sum > 0) {
				sum /= count;				
			}
			System.out.print(String.format("%.3f, ", sum));
		}
		System.out.println(" ];");
		System.out.print("row_rts = [ ");
		for(AlignmentRow row : alignedList.getRows()) {
			System.out.print(row.getAbsoluteRtDiff() + ", ");
		}
		System.out.println(" ];");
	}

	private AlignmentList align(ExtendedLibrary extendedLibrary) {

		AlignmentList alignedList = new AlignmentList("");
		
		// using baseline alignment
//		BaselineAlignmentExtended baselineAlignmentExt = new BaselineAlignmentExtended(
//				dataList, massTolerance, rtTolerance, extendedLibrary, windowMultiply);
//		baselineAlignmentExt.align();
//		alignedList = baselineAlignmentExt.getAlignedList();
	
		// using guide tree and maximum bipartite matching
		DendogramBuilder builder = new DendogramBuilder(dataList, 
				extendedLibrary, massTolerance, rtTolerance, alpha);
		alignedList = builder.align();

		// bad idea .. ?
//		AlignmentFile firstFile = dataList.get(0);
//		alignedList = new AlignmentList(firstFile);
//		System.out.println("Aligned list set to " + firstFile.getFilenameWithoutExtension());
//		for (int i = 1; i < dataList.size(); i++) {
//			AlignmentFile nextFile = dataList.get(i);
//			AlignmentList childList = new AlignmentList(nextFile);
//			FeatureMatching matching = new DynamicProgrammingMatching(nextFile.getFilenameWithoutExtension(), 
//					alignedList, childList, 
//					extendedLibrary, massTolerance, rtTolerance);  
//			alignedList = matching.getMatchedList();
//		}
				
		return alignedList;

	}
		
}