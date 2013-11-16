package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.apporiented.algorithm.clustering.Cluster;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.ExtendedLibrary;

public class DendogramParser {

	private Cluster cluster;
	private ExtendedLibrary library;
	private double massTol;
	private double rtTol;
	private boolean useGroup;
	private double alpha;

	// TODO: ugly hack. Map between the filename (without extension) and the file object. 
	// should rewrite the hierarchical clustering code ourselves to deal with the alignment file object directly
	// instead of just spitting out the labels
	private Map<String, AlignmentFile> dataMap;

	public DendogramParser(Cluster cluster, Map<String, AlignmentFile> dataMap, 
			ExtendedLibrary library, double massTol, double rtTol, boolean useGroup, double alpha) {
		this.cluster = cluster;
		this.dataMap = dataMap;
		this.library = library;
		this.massTol = massTol;
		this.rtTol = rtTol;
		this.useGroup = useGroup;
		this.alpha = alpha;
	}

	public String traverse(int indent) {
		String output = "";
		for (int i = 0; i < indent; i++) {
			output += "  ";

		}
		output += cluster.getName();
		if (cluster.isLeaf()) {
			output += " (leaf) ";
		} else {
			if (cluster.getDistance() != null) {
				output += " distance: " + String.format("%.3f", cluster.getDistance());
			}
		}
		output += "\n";
		for (Cluster child : cluster.getChildren()) {
			DendogramParser parser = new DendogramParser(child, dataMap, 
					library, massTol, rtTol, useGroup, alpha);
			output += parser.traverse(indent + 1);
		}
		return output;
	}

	public AlignmentList buildAlignment() {

		String clusterName = cluster.getName();
		// System.out.println(clusterName);
		
		if (cluster.isLeaf()) {
		
			// terminal node, directly copy features inside file into alignedList then return
			AlignmentFile data = dataMap.get(clusterName);
			AlignmentList alignedList = new AlignmentList(data);
			return alignedList;
		
		} else {
		
			// non-terminal node, merge the result from aligning children into this alignedList
			AlignmentList alignedList = new AlignmentList(cluster.getName());
			
			// first, reorder children to place the intermediate results in front ahead of the leaf nodes
			List<Cluster> children = cluster.getChildren();
			List<Cluster> reordered = new ArrayList<Cluster>();
			for (Cluster child : children) {
				if (!child.isLeaf()) {
					reordered.add(child);
				}
			}
			for (Cluster child : children) {
				if (child.isLeaf()) {
					reordered.add(child);
				}
			}
			
			// now then actually do the merging
			for (Cluster child : reordered) {

				DendogramParser parser = new DendogramParser(child, dataMap, 
						library, massTol, rtTol, useGroup, alpha);
				AlignmentList childList = parser.buildAlignment();

				FeatureMatching matcher = new StableMatching(clusterName, alignedList, childList, 
						library, massTol, rtTol, useGroup, alpha);
//				FeatureMatching matcher = new MaximumWeightMatching(clusterName, alignedList, childList, 
//						library, massTol, rtTol);
//				FeatureMatching matcher = new DynamicProgrammingMatching(clusterName, alignedList, childList, 
//						library, massTol, rtTol);
//				FeatureMatching matcher = new GreedyScoreMatching(clusterName, alignedList, childList, 
//						library, massTol, rtTol);
				
				alignedList = matcher.getMatchedList();
				
			}			
			return alignedList;	
		
		}
		
	}
	
}
