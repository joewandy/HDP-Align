package com.joewandy.alignmentResearch.alignmentMethod.custom;

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
	private double alpha;

	// TODO: ugly hack. Map between the filename (without extension) and the file object. 
	// should rewrite the hierarchical clustering code ourselves to deal with the alignment file object directly
	// instead of just spitting out the labels
	private Map<String, AlignmentFile> dataMap;

	public DendogramParser(Cluster cluster, Map<String, AlignmentFile> dataMap, 
			ExtendedLibrary library, double massTol, double rtTol, double alpha) {
		this.cluster = cluster;
		this.dataMap = dataMap;
		this.library = library;
		this.massTol = massTol;
		this.rtTol = rtTol;
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
					library, massTol, rtTol, alpha);
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
		
			// non-terminal node, merge the result with children then return
			AlignmentList alignedList = new AlignmentList(cluster.getName());
			for (Cluster child : cluster.getChildren()) {

				DendogramParser parser = new DendogramParser(child, dataMap, 
						library, massTol, rtTol, alpha);
				AlignmentList childList = parser.buildAlignment();

				FeatureMatching matcher = new StableMatching(clusterName, alignedList, childList, 
						library, massTol, rtTol, alpha);
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
