package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.MatrixEntry;

import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.UndirectedOrderedSparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

public class PathGrowing {

	private List<AlignmentRow> men;
	private List<AlignmentRow> women;
	private Graph<AlignmentRow, MyLink> graph;
	private int edgeCount;

	public PathGrowing(Matrix scoreArr, 
			List<AlignmentRow> men, 
			List<AlignmentRow> women,
			double massTol, double rtTol) {

		this.men = men;
		this.women = women;
		this.graph = getGraph(scoreArr);

	}

	public List<MatchResult> execute() {
		
		// as per the Path Growing algorithm in http://www.or.uni-bonn.de/~hougardy/paper/maxmatch.pdf
		Graph<AlignmentRow, MyLink> M1 = new UndirectedOrderedSparseMultigraph<AlignmentRow, MyLink>();
		Graph<AlignmentRow, MyLink> M2 = new UndirectedOrderedSparseMultigraph<AlignmentRow, MyLink>();
		int i = 1;		
		
		// while there's an edge
		while (graph.getEdgeCount() > 0) {
			
			List<MyLink> allEdges = new ArrayList<MyLink>(graph.getEdges());
			
			// choose any vertex of degree at least 1
			MyLink firstEdge = allEdges.get(0);
			Pair<AlignmentRow> endPoints = graph.getEndpoints(firstEdge);
			AlignmentRow x = endPoints.getFirst();
			
			// while x has a neighbour
			while (graph.getNeighborCount(x) > 0) {

				// let {x, y} be the heaviest edge incident to x
				MyLink xy = getHeaviestIncidentEdge(x);
				AlignmentRow y = graph.getOpposite(x, xy);
				
				// add {x, y} to Mi
				if (i == 1) {
					M1.addEdge(xy, x, y, EdgeType.UNDIRECTED);
				} else if (i == 2) {
					M2.addEdge(xy, x, y, EdgeType.UNDIRECTED);					
				}
				
				// alternate i between M1 and M2
				i = 3 - i;

				// remove x from G
				graph.removeVertex(x);
				
				// x := y
				x = y;
				
			}
			
		}
		
		// pick the max between M1 and M2
		double m1Weight = getGraphWeight(M1);
		double m2Weight = getGraphWeight(M2);		
		Graph<AlignmentRow, MyLink> maxMatching = null;
		if (m1Weight > m2Weight) {
			maxMatching = M1;
		} else {
			maxMatching = M2;
		}
		
		// convert matching to row mapping of men -> women
		List<MatchResult> matches = new ArrayList<MatchResult>();
		for (MyLink link : maxMatching.getEdges()) {
			Pair<AlignmentRow> pair = maxMatching.getEndpoints(link);
			if (men.contains(pair.getFirst())) {
				// pair is {man, woman}
				MatchResult res = new MatchResult(pair.getFirst(), pair.getSecond(), link.getWeight());
				matches.add(res);
			} else {
				// pair is {woman, man}
				MatchResult res = new MatchResult(pair.getSecond(), pair.getFirst(), link.getWeight());
				matches.add(res);
			}
		}
		
		return matches;

	}

	public List<MatchResult> executeGreedy() {
		
		// as per the Path Growing algorithm in http://www.or.uni-bonn.de/~hougardy/paper/maxmatch.pdf
		Graph<AlignmentRow, MyLink> M = new UndirectedOrderedSparseMultigraph<AlignmentRow, MyLink>();
		
		// while there's an edge
		while (graph.getEdgeCount() > 0) {
			
			// find the heaviest edge in graph
			MyLink heaviest = getHeaviestEdge(graph);
			
			// add e to M
			Pair<AlignmentRow> endPoints = graph.getEndpoints(heaviest);
			M.addEdge(heaviest, endPoints.getFirst(), endPoints.getSecond());
			
			// remove e and all edges adjacent to e from graph
			List<MyLink> allIncident = new ArrayList<MyLink>();
			allIncident.add(heaviest);
			allIncident.addAll(graph.getIncidentEdges(endPoints.getFirst()));
			allIncident.addAll(graph.getIncidentEdges(endPoints.getSecond()));
			for (MyLink incident : allIncident) {
				graph.removeEdge(incident);
			}
			
		}
		
		// convert matching to row mapping of men -> women
		Graph<AlignmentRow, MyLink> maxMatching = M;
		List<MatchResult> matches = new ArrayList<MatchResult>();
		for (MyLink link : maxMatching.getEdges()) {
			Pair<AlignmentRow> pair = maxMatching.getEndpoints(link);
			if (men.contains(pair.getFirst())) {
				// pair is {man, woman}
				MatchResult res = new MatchResult(pair.getFirst(), pair.getSecond(), link.getWeight());
				matches.add(res);
			} else {
				// pair is {woman, man}
				MatchResult res = new MatchResult(pair.getSecond(), pair.getFirst(), link.getWeight());
				matches.add(res);
			}
		}
		
		return matches;

	}
	
	private double getGraphWeight(Graph<AlignmentRow, MyLink> m) {
		double totalWeight = 0;
		for (MyLink link : m.getEdges()) {
			totalWeight += link.getWeight();
		}
		return totalWeight;
	}

	private MyLink getHeaviestEdge(Graph<AlignmentRow, MyLink> g) {
		List<MyLink> edges = new ArrayList<MyLink>(graph.getEdges());
		Collections.sort(edges, new MyLinkComparator());
		MyLink maxEdge = edges.get(0);		
		return maxEdge;
	}
	
	private MyLink getHeaviestIncidentEdge(AlignmentRow x) {
		Set<MyLink> incidentEdges = new HashSet<MyLink>(graph.getIncidentEdges(x));
		double maxWeight = 0;
		MyLink maxEdge = null;
		for (MyLink edge : incidentEdges) {
			if (edge.getWeight() > maxWeight) {
				maxWeight = edge.getWeight();
				maxEdge = edge;
			}
		}
		return maxEdge;
	}

	private Graph<AlignmentRow, MyLink> getGraph(Matrix scoreArr) {
		
		Graph<AlignmentRow, MyLink> graph = new UndirectedOrderedSparseMultigraph<AlignmentRow, MyLink>();
		for (MatrixEntry entry : scoreArr) {
			
			// if score exists between these two rows
			double val = entry.get();
			if (val > 0) {

				int i = entry.row();
				int j = entry.column();

				AlignmentRow man = men.get(i);
				AlignmentRow woman = women.get(j);
				
				// then add the edge in graph
				MyLink link = new MyLink(val);
				graph.addEdge(link, man, woman, EdgeType.UNDIRECTED);					
				
			}
		}
		
		return graph;
	
	}
	
	class MyLink {
		
		private double weight;
		private int id;

		public MyLink(double weight) {
			this.id = edgeCount++; // This is defined in the outer class.
			this.weight = weight;
		}

		public double getWeight() {
			return weight;
		}

		public int getId() {
			return id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			MyLink other = (MyLink) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (id != other.id)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "MyLink [weight=" + weight + ", id=" + id + "]";
		}

		private PathGrowing getOuterType() {
			return PathGrowing.this;
		}
		
	}
	
	private class MyLinkComparator implements Comparator<MyLink> {

		public int compare(MyLink o1, MyLink o2) {
			return - Double.compare(o1.getWeight(), o2.getWeight());
		}
		
	}

}