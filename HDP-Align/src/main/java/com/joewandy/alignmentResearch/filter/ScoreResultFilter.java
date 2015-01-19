package com.joewandy.alignmentResearch.filter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

import com.joewandy.alignmentResearch.alignmentExperiment.AlignmentData;
import com.joewandy.alignmentResearch.model.AlignmentList;
import com.joewandy.alignmentResearch.model.AlignmentRow;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.GroundTruth;

public class ScoreResultFilter implements AlignmentResultFilter {

	private double threshold;
	private List<AlignmentRow> accepted;
	private List<AlignmentRow> rejected;
	private AlignmentData data;
	private GroundTruth gt;
	
	public ScoreResultFilter(double threshold, AlignmentData data) {
		this.threshold = threshold;
		this.accepted = new ArrayList<AlignmentRow>();
		this.rejected = new ArrayList<AlignmentRow>();
		this.data = data;
		this.gt = data.getGroundTruth();
	}
	
	public void process(AlignmentList result) {
		// put all into priority queue
		Queue<AlignmentRow> pq = new PriorityQueue<AlignmentRow>(10, new AlignmentScoreComparator());
		for (AlignmentRow row : result.getRows()) {
			if (row.getFeaturesCount() >= 2) {
				// only include rows with alignment
				pq.add(row);
			} else {
				rejected.add(row);
			}
		}
		int limit = (int) ((1-threshold) * pq.size());
		System.out.println("Queue size = " + pq.size());
		System.out.println("Limit = " + limit);
		int counter = 0;
		while (!pq.isEmpty()) {
			AlignmentRow row = pq.remove();
			if (counter < limit) {
				accepted.add(row);
				System.out.println("ACCEPT " + row);				
			} else {
				rejected.add(row);
				System.out.println("REJECT " + row);				
				// remove from ground truth as well
//				for (Feature f : row.getFeatures()) {
//					gt.clearFeature(f);
//				}				
			}
			counter++;			
		}
//		gt.buildPairwise();
		System.out.println("accepted.size() = " + accepted.size());
		System.out.println("rejected.size() = " + rejected.size());
	}

	public List<AlignmentRow> getAccepted() {
		return accepted;
	}

	public List<AlignmentRow> getRejected() {
		return rejected;
	}
	
	public String getLabel() {
		return "alignment result filtering by score " + this.threshold;
	}
	
	class AlignmentScoreComparator implements Comparator<AlignmentRow> {
	    public int compare(AlignmentRow x, AlignmentRow y) {
	        return -Double.compare(x.getScore(), y.getScore());
	    }
	}

}
