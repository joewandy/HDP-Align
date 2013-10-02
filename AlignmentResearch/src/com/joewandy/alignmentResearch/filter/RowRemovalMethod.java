package com.joewandy.alignmentResearch.filter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.AlgoTopKRules;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.Database;
import ca.pfv.spmf.algorithms.associationrules.TopKRules_and_TNR.RuleG;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.AlgoAgrawalFaster94;
import ca.pfv.spmf.algorithms.associationrules.agrawal94_association_rules.Rules;
import ca.pfv.spmf.algorithms.frequentpatterns.fpgrowth.AlgoFPGrowth;
import ca.pfv.spmf.patterns.itemset_array_integers_with_count.Itemsets;

import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.AlignmentPair;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class RowRemovalMethod extends BaseRemovalMethod implements
		RemovalMethod {

	List<AlignmentRow> rows;

	public RowRemovalMethod(List<AlignmentRow> rows) {
		this.rows = rows;
	}

	@Override
	public Set<Feature> findFeatures(String filterMethod, double threshold) {

		Set<Feature> allFeatures = new HashSet<Feature>();
		if ("graph".equals(filterMethod)) {

			double max = Double.MIN_VALUE;
			for (AlignmentRow row : rows) {
				if (row.getPairGraphScore() > max) {
					max = row.getPairGraphScore();
				}
			}
			final double largestScore = max;

			// construct a priority queue of alignment pairs, ordered by scores
			// ascending
			PriorityQueue<AlignmentRow> scoreQueue = new PriorityQueue<AlignmentRow>(
					rows.size(), new Comparator<AlignmentRow>() {
						@Override
						public int compare(AlignmentRow arg0, AlignmentRow arg1) {
							return Double.compare(
									arg0.getNormalizedPairGraphScore(largestScore),
									arg1.getNormalizedPairGraphScore(largestScore));
						}
					});
			scoreQueue.addAll(rows);

			// remove the bottom results, up to threshold
			int n = (int) (threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = scoreQueue.poll();
				row.setDelete(true);
				counter++;
			}
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent
					+ "%) rows marked for deletion by scores");

		} else if ("association".equals(filterMethod)) {

			PrintWriter out = null;
			String filename = "/home/joewandy/temp.txt";
			List<String[]> itemSet = new ArrayList<String[]>();

			try {

				out = new PrintWriter(filename);

				int singleton = 0;
				int nonSingleton = 0;
				for (AlignmentRow row : rows) {
					if (row.getFeaturesCount() < 2) {
						singleton++;
						continue;
					}
					nonSingleton++;
					String output = "";
					for (Feature f : row.getFeatures()) {
						output += f.getFirstGroupID() + " ";
					}
					out.println(output);
					String[] items = output.split(" ");
					itemSet.add(items);
				}
				System.out.println("Singleton skipped = " + singleton);
				System.out.println("Non-singleton retained = " + nonSingleton);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} finally {
				if (out != null) {
					out.close();
				}
			}

			double minsup = 0.0001; // means a minsup of 2 transaction (we used
									// a relative support)

			// // Applying the Apriori algorithm
			// AlgoApriori apriori = new AlgoApriori();
			// try {
			// apriori.runAlgorithm(minsup, filename,
			// "/home/joewandy/output.txt");
			// apriori.printStats();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			try {

				// URL url = this.getClass().getResource(filename);
				// String input =
				// java.net.URLDecoder.decode(url.getPath(),"UTF-8");
				String input = filename;

				// Loading the binary context
				// STEP 1: Applying the FP-GROWTH algorithm to find frequent
				// itemsets
				double minsupp = 0.0001;
				AlgoFPGrowth fpgrowth = new AlgoFPGrowth();
				Itemsets patterns = fpgrowth.runAlgorithm(input, null, minsupp);
				int databaseSize = fpgrowth.getDatabaseSize();
				patterns.printItemsets(databaseSize);

				// STEP 2: Generating all rules from the set of frequent
				// itemsets (based on Agrawal & Srikant, 94)
				double minconf = 0.5;
				AlgoAgrawalFaster94 algoAgrawal = new AlgoAgrawalFaster94();
				// the next line run the algorithm.
				// Note: we pass null as output file path, because we don't want
				// to save the result to a file, but keep it into memory.
				Rules rules = algoAgrawal.runAlgorithm(patterns, null,
						databaseSize, minconf);
				rules.printRules(databaseSize);
				System.out.println("DATABASE SIZE " + databaseSize);

			} catch (IOException ioe) {
				ioe.printStackTrace();
			}

			// // the threshold
			// final double minsupp = 0.1;
			// final double minconf = 0.5;
			//
			// // Loading the transaction database
			// TransactionDatabase database = new TransactionDatabase();
			// database.loadTransactions(itemSet);
			// database.printDatabase();
			//
			// try {
			//
			// // STEP 1: Applying the AlgoAprioriTIDClose algorithm to find
			// frequent closed itemsets
			// AlgoAprioriTIDClose aclose = new AlgoAprioriTIDClose();
			// Itemsets patterns = aclose.runAlgorithm(database, minsupp, null);
			// aclose.printStats();
			//
			// // STEP 2: Generate all rules from the set of frequent itemsets
			// (based on Agrawal & Srikant, 94)
			// AlgoClosedRules algoClosedRules = new AlgoClosedRules();
			// ClosedRules rules = algoClosedRules.runAlgorithm(patterns,
			// minconf, null);
			// algoClosedRules.printStatistics();
			// rules.printRules(database.size());
			//
			// } catch (IOException e) {
			// e.printStackTrace();
			// }

			// Loading the transaction database
			Database db = new Database();
			db.loadItems(itemSet);

			int k = 100;
			double minConf = 0.5;

			AlgoTopKRules algo = new AlgoTopKRules();
			algo.runAlgorithm(k, minConf, db);
			algo.printStats();
			// algo.writeResult();
			List<RuleG> rules = algo.getRules();
			printRules(rules);

			// int delta = 2;
			// AlgoTNR algo = new AlgoTNR();
			// RedBlackTree<RuleG> kRules = algo.runAlgorithm(k, minConf, db,
			// delta );
			// List<RuleG> rules = new ArrayList<RuleG>();
			// Iterator<RuleG> iter = kRules.iterator();
			// while (iter.hasNext()) {
			// RuleG rule = (RuleG) iter.next();
			// rules.add(rule);
			// }
			// algo.printStats();
			// printRules(rules);

			// filter by rules
			int counter = 0;
			for (AlignmentRow row : rows) {
				// keep only rows that satisfy rules
				List<RuleG> rulesSatisfied = row.checkRules(rules);
				if (rulesSatisfied.isEmpty()) {
					row.setDelete(true);
					counter++;
				}
			}
			double ratio = (double) counter / rows.size();
			final String percent = String.format("%.2f", (ratio * 100));
			System.out.println(counter + " (" + percent
					+ "%) rows marked for deletion by rules filtering (k=" + k
					+ ", minConf=" + minConf + ")");

		} else if ("intensity".equals(filterMethod)) {

			// construct a priority queue of alignment pairs, ordered by scores
			// ascending
			PriorityQueue<AlignmentRow> scoreQueue = new PriorityQueue<AlignmentRow>(
					rows.size(), new Comparator<AlignmentRow>() {
						@Override
						public int compare(AlignmentRow arg0, AlignmentRow arg1) {
							return Double.compare(arg0.getPairIntensityScore(),
									arg1.getPairIntensityScore());
						}
					});
			scoreQueue.addAll(rows);

			// remove the bottom results, up to threshold
			int n = (int) (threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = scoreQueue.poll();
				row.setDelete(true);
				counter++;
			}
			final String percent = String.format("%.2f", (threshold * 100));
			System.out
					.println(counter
							+ " ("
							+ percent
							+ "%) rows marked for deletion by relative intensity error");

		} else if ("random".equals(filterMethod)) {

			// shuffle list
			List<AlignmentRow> shuffledList = new ArrayList<AlignmentRow>(rows);
			Collections.shuffle(shuffledList);

			// remove the bottom results, up to threshold
			int n = (int) (threshold * rows.size());
			int counter = 0;
			while (counter < n) {
				AlignmentRow row = shuffledList.get(counter);
				row.setDelete(true);
				counter++;
			}
			final String percent = String.format("%.2f", (threshold * 100));
			System.out.println(counter + " (" + percent
					+ " %) rows marked for deletion randomly");

		}

		return allFeatures;

	}

	public RemovalResult filterRows(AlignmentList listResult) {

		List<AlignmentRow> rows = listResult.getRows();
		RemovalResult result = new RemovalResult();

		/*
		 * filter rows based on graph representation create new alignment rows
		 * containing elements not filtered from the original row
		 */
		System.out
				.println("Filtering alignment pairs by groups - current rows "
						+ rows.size());
		List<AlignmentRow> accepted = new ArrayList<AlignmentRow>();
		List<AlignmentRow> rejected = new ArrayList<AlignmentRow>();
		int rowId = 0;
		for (AlignmentRow row : rows) {

			if (row.isDelete()) {
				for (Feature f : row.getFeatures()) {
					AlignmentRow rejectedRow = new AlignmentRow(listResult, rowId);
					rowId++;
					rejectedRow.addFeature(f);
					rejected.add(rejectedRow);
				}
			} else {
				accepted.add(row);
			}
			
			if (rowId % 1000 == 0) {
				System.out.print(".");
			}

		}

		printCounts(rows, accepted, rejected);
		result.setAccepted(accepted);
		result.setRejected(rejected);
		return result;

	}

	private void printRules(List<RuleG> rules) {
		for (RuleG rule : rules) {
			// Write the rule
			StringBuffer buffer = new StringBuffer();
			buffer.append(rule.toString());
			// write separator
			buffer.append("  sup= ");
			// write support
			buffer.append(rule.getAbsoluteSupport());
			// write separator
			buffer.append("  conf= ");
			// write confidence
			buffer.append(rule.getConfidence());
			System.out.println(buffer);
		}
	}

}
