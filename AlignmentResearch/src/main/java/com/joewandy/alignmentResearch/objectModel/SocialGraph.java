package com.joewandy.alignmentResearch.objectModel;

import java.util.HashSet;
import java.util.Set;

public class SocialGraph {

	private Set<SocialGraphEdge> edges;
	
	public SocialGraph() {
		this.edges = new HashSet<SocialGraphEdge>();
	}
	
	public void addEdge(SocialGraphEdge e) {
		edges.add(e);
	}
	
	public boolean containsEdge(SocialGraphEdge e) {
		return edges.contains(e);
	}
	
	public int getEdgeCount() {
		return edges.size();
	}
	
}
