package com.joewandy.bioinfoapp.model.stringDistance;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.googlecode.charts4j.AxisLabels;
import com.googlecode.charts4j.AxisLabelsFactory;
import com.googlecode.charts4j.AxisStyle;
import com.googlecode.charts4j.AxisTextAlignment;
import com.googlecode.charts4j.Color;
import com.googlecode.charts4j.Data;
import com.googlecode.charts4j.Fills;
import com.googlecode.charts4j.GCharts;
import com.googlecode.charts4j.Line;
import com.googlecode.charts4j.LineChart;
import com.googlecode.charts4j.LineStyle;
import com.googlecode.charts4j.LinearGradientFill;
import com.googlecode.charts4j.Plots;
import com.joewandy.bioinfoapp.model.core.Sequence;
import com.joewandy.bioinfoapp.model.core.io.FastaReader;
import com.joewandy.bioinfoapp.model.core.io.FastaReader.FastaFileContent;

/**
 * Computes the cumulative frequency graph between the ALU elements loaded into
 * <code>aluNucleotideSeq</code> and the <code>querySeq</code>, which is also
 * another ALU element picked at random. All the ALU elements are stored into a
 * data structure called the Burkhard-Keller tree, which allows easy retrieval
 * of elements in metric space by distances.<br/>
 * <br/>
 * The variable <code>maxDiff</code> contains the maximum possible difference
 * between all elements in <code>aluNucleotideSeq</code> against the
 * <code>querySeq</code>. In this case, <code>maxDiff</code> is determined by
 * adding the maximum element length in <code>aluNucleotideSeq</code> with the
 * length of <code>querySeq</code>.<br/>
 * <br/>
 * The cumulative frequency graph is then determined by repeatedly querying the
 * BK-Tree for elements with edit distance 0..maxDiff from the
 * <code>querySeq</code>.<br/>
 * 
 * @author joewandy
 * 
 */
public class BkTreeEditDist {

	/**
	 * The maximum number of bases of differences that will be used when
	 * checking the cumulative frequency distribution of the edit distance of
	 * dataSeq to the querySeq. Limited to 1000 for performance reason and
	 * because that's the maximum data points that can be passed to Charts4j for
	 * plotting.
	 */
	private static final int MAXIMUM_BASES_DIFFERENCE = 1000;

	/** The query sequence */
	private Sequence querySeq;

	/** The list of all other data elements */
	private List<Sequence> dataSeq;

	/**
	 * The BK-Tree containing both querySeq as root, and elements inside
	 * aluNucleotideSeq
	 */
	private BkTree bkTree;

	/** The maximum possible edit distance */
	private int maxDiff;

	/** The statistics results */
	private double[] queryResults;

	public BkTreeEditDist(Sequence querySeq, List<Sequence> dataSeq,
			BkTree bkTree) {
		this.querySeq = querySeq;
		this.dataSeq = dataSeq;
		this.bkTree = bkTree;
	}

	public BkTreeEditDist(String querySeqFile, String dataSeqFile) {

		// load query sequence from fasta file
		FastaReader reader = new FastaReader(FastaFileContent.DNA_SEQUENCE,
				querySeqFile);
		reader.processFile();
		Sequence querySeq = reader.getFirstSequence();
		if (querySeq == null) {
			return;
		} else {
			this.querySeq = querySeq;
		}

		// load the remaining sequences from fasta file
		BkTree bkTree = new BkTree(querySeq);
		BkTreeLoader loader = new BkTreeLoader(bkTree, dataSeqFile);
		loader.loadTree(FastaFileContent.DNA_SEQUENCE);
		List<Sequence> dataSeq = loader.getLoadedSequence();
		System.out.println(dataSeq.size() + " sequence(s) loaded from "
				+ dataSeqFile);

		this.dataSeq = dataSeq;
		this.bkTree = bkTree;

	}

	/**
	 * Perform a simple analysis on BK-Tree. <br/>
	 * <br/>
	 * First, create a query sequence in <code>querySeq</code> of a short
	 * sequence which as been picked at random. Then load all the remaining
	 * short sequences from file. Add them all into a BK-Tree and proceed to
	 * test out querying the trees.
	 */
	public void bkTreeAnalysis(String outputFile) throws MalformedURLException,
			IOException {

		// generate retrieval statistics
		this.computeStatistics();

		// plot data
		String chartUrl = this.plotData();
		System.out.println("Cumulative frequency graph ready at " + chartUrl);

		// download the chart
		this.downloadChart(chartUrl, outputFile);
		System.out.println("Chart downloaded to " + outputFile);

	}

	/**
	 * Plots the cumulative frequency graph of the number of elements retrieved
	 * by their edit distance from the specified query sequence.
	 * 
	 * @param querySeq
	 *            The query sequence
	 * @param dataSeq
	 *            The list of all sequences in the BK-Tree
	 * @param bkTree
	 *            The BK-Tree containing all sequences
	 * @return The url to cumulative frequency graph chart
	 */
	private void computeStatistics() {

		System.out.println("\n~~ Computing statistics ~~");

		/*
		 * totalSeqs = +1 from noOfLoadedSeqs because including the initial
		 * query sequence as the root
		 */
		int noOfLoadedSeqs = dataSeq.size();
		int totalSeqs = 1 + noOfLoadedSeqs;

		/*
		 * The maximum possible differences between the length of selected query
		 * sequence to the maximum length of an element found in the ALU
		 * sequences. This number is also used as the no. of iterations when
		 * querying the BK-Tree (from 0 .. maxDiff) in the queryBkTree static
		 * method.
		 */
		int maxElemLength = findMaxLength(dataSeq);
		System.out.println("Maximum element length found: " + maxElemLength
				+ " bases.");
		int querySeqLength = querySeq.getSequenceString().length();

		// maxDiff is limited to MAXIMUM_BASES_DIFFERENCE
		maxDiff = querySeqLength + maxElemLength;
		if (maxDiff > BkTreeEditDist.MAXIMUM_BASES_DIFFERENCE) {
			maxDiff = BkTreeEditDist.MAXIMUM_BASES_DIFFERENCE;
		}

		// query the BK-Tree
		System.out.println("Querying BK-Tree");
		double[] results = queryBkTree(querySeq, bkTree, maxDiff);
		queryResults = normalizeResults(totalSeqs, results);
		System.out.println("Query done.");

	}

	/**
	 * Plots a cumulative frequency distribution graph.
	 * 
	 * @return The url containing the graph.
	 */
	private String plotData() {

		Data plotData = Data.newData(queryResults);
		Line line1 = Plots.newLine(plotData, Color.newColor("CA3D05"),
				"% Results/Distance");
		line1.setLineStyle(LineStyle.newLineStyle(3, 1, 0));

		// Defining chart.
		LineChart chart = GCharts.newLineChart(line1);
		chart.setSize(600, 450);
		chart.setTitle("% Result Retrieval by Edit Distance to Query Sequence",
				Color.BLACK, 14);
		chart.addVerticalRangeMarker(0, 25, Color.newColor(Color.GREEN, 30));
		chart.addVerticalRangeMarker(25, 50, Color.newColor(Color.YELLOW, 30));
		chart.addVerticalRangeMarker(50, 75, Color.newColor(Color.ORANGE, 30));
		chart.addVerticalRangeMarker(75, 100, Color.newColor(Color.RED, 30));
		chart.setGrid(25, 25, 3, 2);

		// Defining axis info and styles
		AxisStyle axisStyle = AxisStyle.newAxisStyle(Color.BLACK, 12,
				AxisTextAlignment.CENTER);

		int[] xIntervals = { 0, maxDiff * 1 / 4, maxDiff * 2 / 4,
				maxDiff * 3 / 4, maxDiff };
		AxisLabels xAxis1 = AxisLabelsFactory.newAxisLabels(
				String.valueOf(xIntervals[0]), String.valueOf(xIntervals[1]),
				String.valueOf(xIntervals[2]), String.valueOf(xIntervals[3]),
				String.valueOf(xIntervals[4]));
		xAxis1.setAxisStyle(axisStyle);
		AxisLabels xAxis2 = AxisLabelsFactory.newAxisLabels(
				"Edit Distance to Query Sequence", 50.0);
		xAxis2.setAxisStyle(AxisStyle.newAxisStyle(Color.BLACK, 14,
				AxisTextAlignment.CENTER));
		AxisLabels xAxis3 = AxisLabelsFactory.newAxisLabels("", "Q1", "Q2",
				"Q3", "Q4", "");
		xAxis3.setAxisStyle(axisStyle);

		AxisLabels yAxis1 = AxisLabelsFactory.newAxisLabels("0", "25", "50",
				"75", "100");
		yAxis1.setAxisStyle(axisStyle);

		AxisLabels yAxis2 = AxisLabelsFactory.newAxisLabels("% Results", 50.0);
		yAxis2.setAxisStyle(AxisStyle.newAxisStyle(Color.BLACK, 14,
				AxisTextAlignment.CENTER));
		yAxis2.setAxisStyle(axisStyle);

		// Adding axis info to chart.
		chart.addXAxisLabels(xAxis1);
		chart.addXAxisLabels(xAxis2);
		chart.addXAxisLabels(xAxis3);
		chart.addYAxisLabels(yAxis1);
		chart.addYAxisLabels(yAxis2);

		// Defining background and chart fills.
		chart.setBackgroundFill(Fills.newSolidFill(Color.WHITE));
		LinearGradientFill fill = Fills.newLinearGradientFill(0,
				Color.newColor("363433"), 100);
		fill.addColorAndOffset(Color.newColor("2E2B2A"), 0);
		chart.setAreaFill(fill);
		String url = chart.toURLString();
		return url;

	}

	/**
	 * Downloads the chart specified at urlPath to filePath
	 * 
	 * @param urlPath
	 *            The url path to download
	 * @param filePath
	 *            The path to file to save to
	 * @throws MalformedURLException
	 *             Thrown if urlPath is malformed
	 * @throws IOException
	 *             Thrown if any problem writing to filePath
	 */
	private void downloadChart(String urlPath, String filePath)
			throws MalformedURLException, IOException {
		File fileOut = new File(filePath);
		URL urlIn = new URL(urlPath);
		FileUtils.copyURLToFile(urlIn, fileOut);
	}

	/**
	 * Find the maximum length of the elements inside aluNucleotideSeq
	 * 
	 * @param aluNucleotideSeq
	 *            The list of ALU element sequences
	 * @return The length of the maximum element
	 */
	private int findMaxLength(List<Sequence> aluNucleotideSeq) {
		int maxElemLength = 0;
		for (Sequence s : aluNucleotideSeq) {
			int seqLength = s.getSequenceString().length();
			if (seqLength > maxElemLength) {
				maxElemLength = seqLength;
			}
		}
		return maxElemLength;
	}

	/**
	 * Repeatedly query for elements in the BK-Tree by their edit distance from
	 * the querySeq. The queries are done for all edit distances from
	 * 0..maxEditDistance. The numbers of each element obtained by the query for
	 * each iteration in the method are returned as an array.
	 * 
	 * @param querySeq
	 *            The query sequence
	 * @param bkTree
	 *            The BK-Tree containing both query sequence as root, and other
	 *            ALU elements
	 * @param maxEditDistance
	 *            Used as the number of iterations of queries, from
	 *            0..maxEditDistance
	 * @return The number of elements obtained for each iteration of the query
	 */
	private double[] queryBkTree(Sequence querySeq, BkTree bkTree,
			int maxEditDistance) {

		int size = this.bkTree.getSize();
		double[] results = new double[maxEditDistance];

		/*
		 * first, initialize all values in results array to the maximum number
		 * of elements retrieved, which is basically the size of the whole
		 * BK-Tree
		 */
		Arrays.fill(results, size);

		/*
		 * next, iterate for every edit distance 0..maxEditDistance until the
		 * number of elements retrieved is the whole BK-Tree
		 */
		for (int editDistance = 0; editDistance < maxEditDistance; editDistance++) {

			// no of ALU elements retrieved by edit distance from querySeq
			Set<BkTreeNode> resultSet = bkTree.query(querySeq, editDistance);
			results[editDistance] = resultSet.size() - 1;

			// for debugging only
			System.out.println("Querying editDistance [" + editDistance + "/"
					+ maxEditDistance + "], elements retrieved ["
					+ (int) results[editDistance] + "/" + size + "]");

			/*
			 * short-circuit condition: all elements retrieved. No need to
			 * proceed with the loop anymore. The remaining elements should have
			 * the value of size already, as per Arrays.fill() above.
			 */
			if (results[editDistance] == size) {
				System.out.println("Whole BK-Tree retrieved.");
				break;
			}

		}

		return results;

	}

	/**
	 * Normalize the values in <code>results</code> from 0..100 %
	 * 
	 * @param totalElements
	 *            The total number of elements
	 * @param results
	 *            The array containing query results
	 * @return The normalized results, as percentage of totalElements
	 */
	private double[] normalizeResults(int totalElements, double[] results) {
		double[] normalizedResults = new double[results.length];
		for (int i = 0; i < results.length; i++) {
			normalizedResults[i] = results[i] / totalElements * 100;
		}
		return normalizedResults;
	}

}
