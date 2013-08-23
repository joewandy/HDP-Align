package com.joewandy.bioinfoapp.model.stringDistance;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.joewandy.bioinfoapp.model.core.Sequence;

/**
 * Simple implementation of BK-Tree as described in
 * "Some Approaches to Best-Match File Searching" by W. A. Burkhard and R. M.
 * Keller
 * 
 * Also read http://blog.notdot.net/2007/4/Damn-Cool-Algorithms-Part-1-BK-Trees
 * 
 * @author joewandy
 * 
 */
public class BkTree {

	private BkTreeNode rootNode;
	private int size;

	public BkTree(Sequence rootItem) {
		this.rootNode = new BkTreeNode(rootItem);
		this.size = 0;
	}

	public int addItem(Sequence newItem) {
		this.size += this.rootNode.addItem(newItem);
		return newItem.getSequenceString().length();
	}

	public int addItems(List<Sequence> newItems) {

		int allItems = newItems.size();
		for (Sequence s : newItems) {

			System.out.println("Adding " + s.getId() + " [" + this.size + "/"
					+ allItems + "]");
			this.size += rootNode.addItem(s);

		}

		return this.size;

	}

	public int getSize() {
		return this.size;
	}

	public Set<BkTreeNode> query(Sequence querySequence, int limit) {
		Set<BkTreeNode> results = new HashSet<BkTreeNode>();
		rootNode.query(querySequence, limit, results);
		return results;
	}

	public String printTree() {
		int indentLevel = 0;
		return rootNode.printTree(indentLevel);
	}

	@Override
	public String toString() {
		return rootNode.toString() + ": " + rootNode.getChildren();
	}

}
