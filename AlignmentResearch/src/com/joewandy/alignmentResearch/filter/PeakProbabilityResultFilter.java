package com.joewandy.alignmentResearch.filter;

import java.util.List;
import java.util.Set;

import com.joewandy.alignmentResearch.alignmentMethod.custom.CombineGraphView;
import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpParam;
import com.joewandy.alignmentResearch.objectModel.AlignmentExpResult;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.util.GraphEdgeConstructor;

public class PeakProbabilityResultFilter implements AlignmentResultFilter {

	private List<AlignmentFile> alignmentDataList;
	private String graphFilter;
	private double threshold;
	private GraphEdgeConstructor edgeConstructor;
	private RemovalResult remRes;
	private double dmz;
	private double drt;

	/**
	 * Constructs a new graph alignment result filter
	 * 
	 * @param alignmentDataList
	 *            The aligment input files
	 * @param graphFilter
	 *            Which filtering method to use to remove edges, for experiments
	 *            only
	 * @param threshold
	 *            Percentile above which we will retain the edges, goes from 0
	 *            .. 1
	 */
	public PeakProbabilityResultFilter(List<AlignmentFile> alignmentDataList,
			String graphFilter, double threshold, double dmz, double drt) {
		this.alignmentDataList = alignmentDataList;
		this.graphFilter = graphFilter;
		this.threshold = threshold;
		this.edgeConstructor = new GraphEdgeConstructor();
		this.remRes = new RemovalResult();
		this.dmz = dmz;
		this.drt = drt;
	}

	@Override
	public void process(AlignmentList alignmentList) {

		List<AlignmentRow> rows = alignmentList.getRows();

		// build graph
		

	}

	public List<AlignmentRow> getAccepted() {
		return remRes.getAccepted();
	}

	public List<AlignmentRow> getRejected() {
		return remRes.getRejected();
	}

	@Override
	public String getLabel() {
		return "alignment result filtering by peak group information";
	}

}
