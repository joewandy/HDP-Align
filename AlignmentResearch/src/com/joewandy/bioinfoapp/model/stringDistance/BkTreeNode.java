package com.joewandy.bioinfoapp.model.stringDistance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.joewandy.bioinfoapp.model.core.Sequence;

/**
 * A simple BK-Tree node implementation. Containing only string item.
 * 
 * @author joewandy
 * 
 */
public class BkTreeNode {

	private Sequence sequence;
	private Map<Integer, BkTreeNode> children;

	/**
	 * Creates an instance of BkTreeNode. Set current sequence and create empty
	 * children.
	 * 
	 * @param item
	 *            The sequence item of this node
	 */
	public BkTreeNode(Sequence sequence) {
		this.sequence = sequence;
		children = new HashMap<Integer, BkTreeNode>();
	}

	/**
	 * Get sequence id of this node
	 * 
	 * @return The sequence id of this node
	 */
	public String getSequenceId() {
		return sequence.getId();
	}

	/**
	 * Get sequence string of this node
	 * 
	 * @return The sequence content of this node
	 */
	public String getSequenceContent() {
		return sequence.getSequenceString();
	}

	/**
	 * Get the children of this node
	 * 
	 * @return A map representing the children of this node
	 */
	public Map<Integer, BkTreeNode> getChildren() {
		return children;
	}

	/**
	 * Add a new string sequence into this node
	 * 
	 * @param newSequenceString
	 *            The string sequence to be added
	 */
	public int addItem(Sequence newSequence) {

		// get the edit distance between this node to the new item
		EditDistance distanceMetric = new LevenshteinDistance(
				getSequenceContent(), newSequence.getSequenceString());
		int distance = distanceMetric.getDistance();

		// check if any child with the same distance
		BkTreeNode child = getChildren().get(distance);
		if (child != null) {

			// if yes then add newSequenceString under that child
			return child.addItem(newSequence);

		} else {

			// if no then add newItem as child under this node
			BkTreeNode newNode = new BkTreeNode(newSequence);
			getChildren().put(distance, newNode);
			return 1;

		}

	}

	/**
	 * Query this node for the query term within the specified limit. The query
	 * results are returned in the Set results.
	 * 
	 * @param querySequence
	 *            The query sequence
	 * @param limit
	 *            The limit of distance
	 * @param results
	 *            The query results of nodes within distance limit from query
	 *            term
	 */
	public void query(Sequence querySequence, int limit, Set<BkTreeNode> results) {

		// get the edit distance between this node to the query term
		EditDistance distanceMetric = new LevenshteinDistance(
				getSequenceContent(), querySequence.getSequenceString());
		int distance = distanceMetric.getDistance();

		// add this node to results while still within limit
		if (distance <= limit) {
			results.add(this);
		}

		// recurrence termination when reached the limit
		if (distance > limit) {
			return;
		}

		// query all children by distance within the range distance +- limit
		int lower = distance - limit;
		int upper = distance + limit;
		for (int i = lower; i <= upper; i++) {
			BkTreeNode children = getChildren().get(i);
			if (children != null) {
				children.query(querySequence, limit, results);
			}
		}

	}

	public String printTree(int indentLevel) {
		String output = "";
		String tabs = "";
		for (int i = 0; i < indentLevel; i++) {
			tabs += "\t";
		}
		tabs += "|-";
		output += String.format("\n" + tabs + getSequenceContent());
		for (Map.Entry<Integer, BkTreeNode> e : getChildren().entrySet()) {
			output += e.getValue().printTree(indentLevel++);
		}
		return output;
	}

	@Override
	public String toString() {
		return this.sequence.toString();
	}

}
