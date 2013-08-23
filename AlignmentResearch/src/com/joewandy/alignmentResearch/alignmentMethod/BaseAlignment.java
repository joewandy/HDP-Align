package com.joewandy.alignmentResearch.alignmentMethod;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public abstract class BaseAlignment implements AlignmentMethod {

	private static final boolean ALIGN_BY_RELATIVE_MASS_TOLERANCE = false;
	
	protected double massTolerance;
	protected boolean usePpm;
	protected double rtTolerance;

	protected List<AlignmentFile> dataList;	
	protected List<AlignmentResultFilter> filters;
	protected List<AlignmentRow> filteredResult;
	
	/**
	 * Initialise our aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public BaseAlignment(List<AlignmentFile> dataList, double massTolerance, double rtTolerance) {		

		this.dataList = dataList;		
		
		this.massTolerance = massTolerance;
		 // we use absolute mass tolerance for now to be the same as Lange, et al. (2008)
		this.usePpm = BaseAlignment.ALIGN_BY_RELATIVE_MASS_TOLERANCE;		
		this.rtTolerance = rtTolerance;
		
		this.filters = new ArrayList<AlignmentResultFilter>();
		this.filteredResult = new ArrayList<AlignmentRow>();				
	
	}
	
	public List<AlignmentRow> align() {
				
		// match features provided by subclasses implementations
		AlignmentList alignmentResult = this.matchFeatures();
		
		// filter the alignment results sequentially, if necessary
		// System.out.println("Aligned rows " + alignmentResult.getRowsCount() + " with " + this.getClass().getName());
		List<AlignmentRow> filteredRows = alignmentResult.getRows();
		for (AlignmentResultFilter filter : filters) {
			System.out.println("Applying " + filter.getLabel());
			filteredRows = filter.filter(filteredRows);	
			System.out.println("Filtered rows " + filteredRows.size());
		}
		
		this.filteredResult = filteredRows;
		return filteredRows;
		
	}
		
	public void addFilter(AlignmentResultFilter sizeFilter) {
		filters.add(sizeFilter);
	}
	
	public List<AlignmentRow> getAlignmentResult() {
		return filteredResult;
	}
	
	public void writeAlignmentResult(PrintStream alignmentOutput) {
		for (AlignmentRow row : filteredResult) {
			alignmentOutput.println(printRow(row));
		}
	}	
		
	/**
	 * Implemented by subclasses to do the actual peak matching
	 * @return a list of aligned features in every row
	 */
	protected abstract AlignmentList matchFeatures();
	
	private String printRow(AlignmentRow row) {
		StringBuilder sb = new StringBuilder();
		for (Feature feature : row.getFeatures()) {
			sb.append(printFeature(feature));			
		}
		return sb.toString();		
	}
	
	private String printFeature(Feature feature) {
		return feature.getIntensity() + " " + feature.getRt() + " " + feature.getMass() + " ";
	}
	
}
