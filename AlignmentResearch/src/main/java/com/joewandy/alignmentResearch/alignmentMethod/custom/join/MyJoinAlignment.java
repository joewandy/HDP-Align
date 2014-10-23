package com.joewandy.alignmentResearch.alignmentMethod.custom.join;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.RowVsRowScore;

public class MyJoinAlignment extends BaseAlignment implements AlignmentMethod {

	private List<AlignmentList> featureList;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public MyJoinAlignment(List<AlignmentFile> dataList, AlignmentMethodParam param) {

		super(dataList, param);
		
		featureList = new ArrayList<AlignmentList>();
		for (AlignmentFile data : dataList) {
			// construct list of features in rows, based on data
			AlignmentList newList = new AlignmentList(data);
			featureList.add(newList);
		}
				
	}
	
	/**
	 * Clone of joinAlign implemented in mzMine
	 * @return
	 */
	public AlignmentList matchFeatures() {

		AlignmentList masterList = new AlignmentList("");
		int newRowId = 0;

		for (int i = 0; i < featureList.size(); i++) {

			AlignmentList peakList = featureList.get(i);
			System.out.println("Aligning #" + (i+1) + ": " + peakList);
			
			/*
			 * 1. Calculate scores for every entry in this peaklist against our master list.
			 */
			
            PriorityQueue<RowVsRowScore> scoreQueue = new PriorityQueue<RowVsRowScore>();
            List<AlignmentRow> allRows = peakList.getRows();
            for (AlignmentRow reference : allRows) {

            	/*
            	 * Get all rows of the aligned peaklist within parameter limits,
            	 * calculate scores and store them.
            	 * 
            	 * If this is the first iteration of the outermost loop (the first file aligned),
            	 * then candidateRows is always empty .. so effectively we're skipping the code
            	 * below this comment.
            	 * 
            	 * Remember that candidateRows come from the master list, so later 
            	 * we're going to successively merge entries (rows) in this featureList to the 
            	 * candidateRows in the master list.
            	 */
            	Set<AlignmentRow> candidateRows = masterList.getRowsInRange(reference, 
            			this.massTolerance, this.rtTolerance, this.usePpm);
            	for (AlignmentRow candidate : candidateRows) {
            		RowVsRowScore score = new RowVsRowScore(reference, candidate, this.massTolerance/2, this.rtTolerance/2);
            		scoreQueue.add(score);
            	}
            	
            }

            /*
             * 2. Next, maps between rows and their scores. For the first iteration,
             * since nothing is inside scoreQueue, nothing will happen.
             */
            
            Map<AlignmentRow, AlignmentRow> alignmentMapping = mapping(scoreQueue);

            /*
             * 3. Here, we actually align using the mapping created above.
             * if we cannot find the mapping, then add this row 
             */
            
            // Align all rows using mapping
            for (AlignmentRow row : allRows) {
            	
            	AlignmentRow referenceRow = alignmentMapping.get(row);
            	
                // If we have no mapping for this row, add as new entry to masterList
                if (referenceRow == null) {
                    referenceRow = new AlignmentRow(masterList, newRowId);
                    newRowId++;
                    masterList.addRow(referenceRow);
                }
                
                // Add all peaks from the original row to the aligned row
                referenceRow.addFeatures(row.getFeatures());
            	
            }
            
		}
		
		return masterList;
		
	}

	/**
	 * Greedy mapping, basically ...
	 * @param scoreQueue
	 * @return
	 */
	protected Map<AlignmentRow, AlignmentRow> mapping(
			PriorityQueue<RowVsRowScore> scoreQueue) {
		
		// Create a table of mappings for best scores
		Map<AlignmentRow, AlignmentRow> alignmentMapping = new HashMap<AlignmentRow, AlignmentRow> ();

		// Iterate scores by ascending order, lower score (lower difference) is better match
		Iterator<RowVsRowScore> scoreIterator = scoreQueue.iterator();
		while (scoreIterator.hasNext()) {

		    RowVsRowScore score = scoreIterator.next();

		    // Check if the row is already mapped
		    if (alignmentMapping.containsKey(score.getReference()))
		        continue;

		    // Check if the aligned row is already filled
		    if (alignmentMapping.containsValue(score.getMasterListCandidate()))
		        continue;

		    // put into mapping only the first occurence (best matching) of reference row in this peaklist
		    alignmentMapping.put(score.getReference(),
		            score.getMasterListCandidate());

		}
		
		return alignmentMapping;

	}
		
}
