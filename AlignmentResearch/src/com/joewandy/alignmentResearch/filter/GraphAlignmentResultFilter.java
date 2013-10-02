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

public class GraphAlignmentResultFilter implements AlignmentResultFilter {

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
	public GraphAlignmentResultFilter(List<AlignmentFile> alignmentDataList,
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

		/*
		 * Edge list also contains the vertices etc. Inside constructEdgeList(),
		 * members of rows will be modified to contain their corresponding
		 * alignment pairs
		 */
		System.out.println("Constructing edge list");
		List<AlignmentEdge> edgeList = edgeConstructor.constructEdgeList(rows, dmz, drt);

		/*
		 * Construct the actual graph here, based on the complete edgeList of
		 * all edges. When the graph is constructed and scores (edge weight)
		 * are assigned to alignmentPairs inside each edges, the same 
		 * aligmentPair in each row would also have their corresponding scores
		 */
		int dataFileCount = alignmentDataList.size();
		System.out.println("Creating graph view");
		CombineGraphView combineGraphView = new CombineGraphView(edgeList,
				false, dataFileCount, this.threshold);

		System.out.println("Computing graph result");
		AlignmentExpResult graphResult = combineGraphView.computeStatistics();

		String label = "mygraph";
		final String myLayout = CombineGraphView.LAYOUT_SPRING;
		final String msg = "graph filtering";
		// combineGraphView.visualiseGraph(label, msg, 1000, 700,
		// combineGraphView.getAlignmentGraph(), myLayout);

		List<AlignmentPair> allAlignmentPairs = graphResult.getAlignmentPairs();
		List<AlignmentPair> removedAlignmentPairs = graphResult
				.getRemovedEdgesAlignmentPairs();
		
		// remove alignment of 1 peak only

		RemovalMethod removalMethod = new PairRemovalMethod(allAlignmentPairs, removedAlignmentPairs);
		// RemovalMethod removalMethod = new FeatureRemovalMethod(allAlignmentPairs);
		// RemovalMethod removalMethod = new RowRemovalMethod(rows);
		remRes = remove(alignmentList, graphResult, label, removalMethod);

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

	private RemovalResult remove(AlignmentList alignmentList,
			AlignmentExpResult graphResult, String label,
			RemovalMethod removalMethod) {
		Set<Feature> allFeatures = removalMethod.findFeatures(this.graphFilter,
				this.threshold);
		AlignmentExpParam param = new AlignmentExpParam(label);
		removalMethod.printStatistics(param, graphResult, allFeatures);
		RemovalResult result = removalMethod.filterRows(alignmentList);
		return result;
	}

}
