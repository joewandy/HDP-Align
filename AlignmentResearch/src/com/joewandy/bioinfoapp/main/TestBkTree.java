package com.joewandy.bioinfoapp.main;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import com.joewandy.bioinfoapp.model.core.PlainText;
import com.joewandy.bioinfoapp.model.core.Sequence;
import com.joewandy.bioinfoapp.model.stringDistance.BkTree;
import com.joewandy.bioinfoapp.model.stringDistance.BkTreeEditDist;
import com.joewandy.bioinfoapp.model.stringDistance.BkTreeNode;

/**
 * Codes to test the BK-Tree implementation.
 * 
 * The method <code>simpleBkTree</code> demonstrates how to insert and query
 * terms by distance from the BK-Tree.
 * 
 * The next part of the main() method shows a more elaborate program that (1)
 * Builds a BK-Tree based on a sequence of roughly 300 ALU repeat elements
 * loaded from a FASTA file. The program will then determine the edit distance
 * of all ALU elements relative to the query sequence. The result is shown as a
 * cumulative frequency distribution chart. The data is obtained from
 * http://www.ncbi.nlm.nih.gov/staff/tao/URLAPI/blastdb.html.
 * 
 * Next, (2) perform the same analysis but with a larger data set of short
 * sequences taken from Illumina paired-end sequencing of the Human HapMap data.
 * The data is obtained from http://trace.ncbi.nlm.nih.gov/Traces/sra
 * 
 * @author joewandy
 * 
 */
public class TestBkTree {

	/** path to test data */
	private static final String PATH = "/Users/joewandy/git/Blog/BioinfoApp/src/com/"
			+ "joewandy/bioinfoapp/main/testData/";

	/** file name of query ALU sequence */
	private static final String ALU_QUERY_FILE = "alu_query.fasta";

	/** file name of other ALU data */
	private static final String ALU_DATA_FILE = "alu_data.fasta";

	/** file name of query sequence read */
	private static final String SRA_QUERY_FILE = "sra_query.fasta";

	/** file name of other sequence reads */
	private static final String SRA_DATA_FILE = "sra_data.fasta";

	/**
	 * The main method
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		// simple test just to make sure BK-Tree implementation is correct
		System.out.println("~~ simpleBkTree ~~");
		TestBkTree.simpleBkTree();

		// (1) more elaborate test of BK-Tree
		System.out.println("\n~~ bkTreeAnalysis - ALU ~~");

		// query sequence is an ALU element picked randomly
		String querySeqFile = TestBkTree.PATH + TestBkTree.ALU_QUERY_FILE;

		// data file points to a FASTA file of other ALU elements
		String dataFilePath = TestBkTree.PATH + TestBkTree.ALU_DATA_FILE;

		// load query and data
		BkTreeEditDist editDist = new BkTreeEditDist(querySeqFile, dataFilePath);

		// path to generate the cumulative frequency distribution chart file
		String chartFilePath = TestBkTree.PATH + TestBkTree.ALU_DATA_FILE
				+ ".chart.png";
		try {
			editDist.bkTreeAnalysis(chartFilePath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// (2) another test using SRA data
		System.out.println("\n~~ bkTreeAnalysis - SRA ~~");
		querySeqFile = TestBkTree.PATH + TestBkTree.SRA_QUERY_FILE;

		// location of FASTA file of other SRA sequences
		dataFilePath = TestBkTree.PATH + TestBkTree.SRA_DATA_FILE;

		// load query and data
		editDist = new BkTreeEditDist(querySeqFile, dataFilePath);

		// path to output file
		chartFilePath = TestBkTree.PATH + TestBkTree.SRA_DATA_FILE
				+ ".chart.png";
		try {
			editDist.bkTreeAnalysis(chartFilePath);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Simple test of BK-Tree with lemon-like words
	 */
	private static final void simpleBkTree() {

		// create a bk-tree of lemon-like words
		Sequence root = new PlainText("lemon");
		BkTree bkTree = new BkTree(root);
		bkTree.addItem(new PlainText("limon"));
		bkTree.addItem(new PlainText("lime"));
		bkTree.addItem(new PlainText("limun"));
		bkTree.addItem(new PlainText("lemony"));
		bkTree.addItem(new PlainText("lemontree"));
		bkTree.addItem(new PlainText("lemontea"));
		bkTree.addItem(new PlainText("lima"));
		bkTree.addItem(new PlainText("limetree"));

		// print the BK-tree out
		System.out.println("bkTree: " + bkTree.printTree());

		// query the tree using "lemon" by edit distance 0..4
		Sequence querySeq = new PlainText("lemonade");
		System.out.println("query: " + querySeq);
		for (int i = 0; i < 5; i++) {
			Set<BkTreeNode> results = bkTree.query(querySeq, i);
			System.out.println("distance: " + i + ", results: " + results);
		}

	}

}
