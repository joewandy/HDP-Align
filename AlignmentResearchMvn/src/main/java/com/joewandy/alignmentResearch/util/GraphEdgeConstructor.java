package com.joewandy.alignmentResearch.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joewandy.alignmentResearch.objectModel.AlignmentEdge;
import com.joewandy.alignmentResearch.objectModel.AlignmentEntry;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.AlignmentVertex;
import com.joewandy.alignmentResearch.objectModel.Feature;
import com.joewandy.alignmentResearch.objectModel.FeatureGroup;

public class GraphEdgeConstructor {

	public List<AlignmentEdge> constructEdgeList(List<AlignmentRow> rows, double dmz, double drt) {
		
		List<AlignmentEdge> edgeList = new ArrayList<AlignmentEdge>();
		Map<String, AlignmentVertex> allVertices = new HashMap<String, AlignmentVertex>();
		
		// for every row of features that have been grouped and aligned together ..
		for (AlignmentRow row : rows) {
			
			// skip corner case of singleton feature in row
			if (row.getFeaturesCount() == 1) {
				continue;
			}
			
			List<AlignmentVertex> vertices = new ArrayList<AlignmentVertex>();
			List<Feature> alignedPeaks = new ArrayList<Feature>();			
			for (Feature feature : row.getFeatures()) {
				
				// get the first group
				int sourcePeakset = feature.getData().getId();
				FeatureGroup group = feature.getFirstGroup();
				if (group == null) {
					continue;
				}
				
				// initialise a vertex in the graph based on the grouping information
				int groupId = group.getGroupId();
				AlignmentEntry currEntry = new AlignmentEntry(
						row.getRowId(),			// alignment cluster id
						0,						// bin id, unused
						sourcePeakset,			// peakset id (the input file)
						feature.getPeakID(),	// peak id
						groupId,				// related peaks group id			
						feature.getMass(),		// peak mass
						feature.getRt(),		// peak retention time
						feature.getIntensity()	// peak intensity
				);
				AlignmentVertex current = buildVertex(currEntry, group, allVertices);
				feature.setVertex(current);

				// store vertices created for this row
				vertices.add(current);	
				alignedPeaks.add(feature);				
				
			}
			
			// link all vertices in the row to each other
			// this forms a clique
			for (int i = 0; i < vertices.size(); i++) {
				for (int j = i; j < vertices.size(); j++) {
					AlignmentVertex v1 = vertices.get(i);
					AlignmentVertex v2 = vertices.get(j);	
					Feature f1 = alignedPeaks.get(i);
					Feature f2 = alignedPeaks.get(j);

					// the bipartite assumption
					// assert(f1.getData() != f2.getData());
					// if (!v1.equals(v2)) {

					// NEW: don't link the features aligned but from the same files
					if (f1.getData() != f2.getData() && !v1.equals(v2)) {
						AlignmentEdge edge = new AlignmentEdge(v1, v2);
						AlignmentPair pair = new AlignmentPair(f1, f2, dmz, drt, edge);
						edge.addAlignmentPair(pair);
						edgeList.add(edge);		
						// also store this alignment pair inside the row itself
						row.addPair(pair);
					}
					
				}
			}
						
		}
		return edgeList;
	}

	private AlignmentVertex buildVertex(AlignmentEntry alignmentEntry,
			FeatureGroup group,
			Map<String, AlignmentVertex> allVertices) {

		// construct a new vertex object or retrieve existing ones
		String key = AlignmentVertex.formatId(alignmentEntry.getSourcePeakSet(), alignmentEntry.getGroupId());
		AlignmentVertex vertex = allVertices.get(key);
		if (vertex == null) {
			vertex = new AlignmentVertex(alignmentEntry.getSourcePeakSet(), alignmentEntry.getGroupId());			
			// store features in vertices
			Set<Feature> members = group.getFeatures();
			vertex.setFeatures(members);
		} else {
			allVertices.put(key, vertex);
		}
				
		return vertex;
	
	}
	
}
