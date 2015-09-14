package com.joewandy.alignmentResearch.alignmentMethod;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.joewandy.alignmentResearch.filter.AlignmentResultFilter;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;
import com.joewandy.alignmentResearch.model.Feature;

public abstract class BaseAlignment implements AlignmentMethod {

	protected double massTolerance;
	protected boolean usePpm;
	protected double rtTolerance;
	protected boolean verbose;

	protected List<AlignmentFile> dataList;	
	protected List<AlignmentResultFilter> filters;
	protected List<AlignmentRow> filteredResult;
	
	protected Path parentPath;
	protected int seed;
	
		
	/**
	 * Initialise our aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public BaseAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {		

		this.dataList = dataList;	
		AlignmentFile firstFile = dataList.get(0);
		Path parentPath = Paths.get(firstFile.getParentPath());
		this.parentPath = parentPath.toAbsolutePath();
		
		this.massTolerance = param.getMassTolerance();
		this.usePpm = param.isUsePpm();
		this.rtTolerance = param.getRtTolerance();
		this.verbose = param.isVerbose();
		this.seed = param.getSeed();

		this.filters = new ArrayList<AlignmentResultFilter>();
		this.filteredResult = new ArrayList<AlignmentRow>();	
			
	}
			
	public AlignmentList align() {
				
		// match features provided by subclasses implementations
		AlignmentList alignmentResult = this.matchFeatures();
		if (alignmentResult == null) {
			return null;
		}
		
		// filter the alignment results sequentially, if necessary
		// System.out.println("Aligned rows " + alignmentResult.getRowsCount() + " with " + this.getClass().getName());
		if (filters.isEmpty()) {
			this.filteredResult = alignmentResult.getRows();
			return alignmentResult;
		}
		
		for (AlignmentResultFilter filter : filters) {

			System.out.println("Applying " + filter.getLabel() + ", initial size = " 
					+ alignmentResult.getRowsCount());
						
			filter.process(alignmentResult);	
			List<AlignmentRow> accepted = filter.getAccepted();			
			List<AlignmentRow> combined = new ArrayList<AlignmentRow>();
			combined.addAll(accepted);			
			// find last row id
			int lastRowId = alignmentResult.getLastRowId() + 1;
			// rejected got broken down into individual features
			List<AlignmentRow> rejected = filter.getRejected();
			for (AlignmentRow rej : rejected) {
				for (Feature f : rej.getFeatures()) {
					AlignmentRow newRow = new AlignmentRow(rej.getParent(), lastRowId);
					newRow.addFeature(f);
					lastRowId++;
					combined.add(newRow);
				}
			}			
			alignmentResult.setRows(combined);
			this.filteredResult = combined;
					
		}		
		return alignmentResult;
		
	}
		
	public void addFilter(AlignmentResultFilter sizeFilter) {
		filters.add(sizeFilter);
	}
	
	public List<AlignmentRow> getAlignmentResult() {
		return filteredResult;
	}
		
	/**
	 * Implemented by subclasses to do the actual peak matching
	 * @return a list of aligned features in every row
	 */
	protected abstract AlignmentList matchFeatures();
	
    protected String getExecutablePath() {
    	
        String className = this.getClass().getName().replace('.', '/');
        String classJar =  
            this.getClass().getResource("/" + className + ".class").toString();
        if (classJar.startsWith("jar:")) {
            String jarFilePath = BaseAlignment.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            File jarFile = new File(jarFilePath);
            return jarFile.getParent();
        } else {
        	return System.getProperty("user.home") + "/scripts";
        }

    }
    
}
