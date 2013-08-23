package com.joewandy.mzmatch;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Vector;

import mzmatch.ipeak.util.Identify;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.chemistry.Molecule;
import peakml.io.ParseResult;
import peakml.io.chemistry.MoleculeIO;
import peakml.io.peakml.PeakMLParser;
import cmdline.CmdLineException;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

/**
 * A simple class to count the votes of a formula, given annotated peaklist
 * 
 * @author joewandy
 * 
 */
public class CountVotes {

	public static final double DEFAULT_VOTES_THRESHOLD = 1;
	public static final double DEFAULT_PROB_THRESHOLD = 0.5;
	public static final int LEVEL_EXPECTED_VALUE = 0;
	
	private Map<FormulaScore, FormulaScore> votes;
	private boolean prettyPrint;
	
	public CountVotes() {
		this(false);
	}

	public CountVotes(boolean prettyPrint) {
		this.votes = new HashMap<FormulaScore, FormulaScore>();
		this.prettyPrint = prettyPrint;
	}
	
	@OptionsClass(name = Options.APPLICATION, version = Options.VERSION, author = Options.AUTHOR, description = Options.DESCRIPTION)
	public static class Options {

		static final String VERSION = "1.0.0";
		static final String APPLICATION = "FormulaVoting";
		static final String AUTHOR = "Joe Wandy (j.wandy.1@research.gla.ac.uk)";
		static final String DESCRIPTION = "A simple class to count the votess of a formula, given annotated peaklist in mzMatch";

		@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "Input filename")
		public String input = null;
		
		@Option(name="databases", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Option for the molecule databases to match the contents of the input file to. " +
				"These files should adhere to the compound-xml format.")
		public Vector<String> databases = new Vector<String>();
		
		@Option(name="extradatabases", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Option for the extra molecule databases for performance evaluation. " +
				"These files should adhere to the compound-xml format.")
		public Vector<String> extraDatabases = new Vector<String>();

		@Option(name="alpha", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Parameter for mixture model clustering."
		)
		public double alpha = 1.0;
		
		@Option(name="dbSize", param="int", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"No. of molecules to read from additional database.")
		public int dbSize = 0;		

		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "Display help")
		public boolean help = false;

		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "Be verbose")
		public boolean verbose = false;
		
		@Option(name="th", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
				"Threshold for positive identification.")
		public double threshold = CountVotes.DEFAULT_VOTES_THRESHOLD;

		@Option(name = "prettyPrint", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "Pretty print some output")
		public boolean prettyPrint = false;
		
	}

	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
	
		try {
			
			Tool.init();

			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			showHelp(args, options, cmdline);

			// load file
			displayMessage(options, "Loading data");
			ParseResult result = PeakMLParser.parse(new FileInputStream(
					options.input), true);

			// load the standard molecule-data
			if (options.verbose)
				System.out.println("Loading molecule data");
			HashMap<String,Molecule> loadedMolecules = new HashMap<String,Molecule>();
			Map<String,Molecule> standards = new HashMap<String,Molecule>();			
			for (String file : options.databases) {
				HashMap<String,Molecule> standardMolecules = MoleculeIO.parseXml(new FileInputStream(file));
				for (Entry<String, Molecule> e : standardMolecules.entrySet()) {
					e.getValue().setSourceDb(Identify.DB_SOURCE_STANDARD);
				}
				loadedMolecules.putAll(standardMolecules);
				standards.putAll(standardMolecules);
			}
			
			// load additional molecule-data
			if (options.verbose) {
				System.out.println("Loading additional molecule data");
			}
			for (String file : options.extraDatabases) {
				
				// use only the top-n entries in this loaded file
				HashMap<String,Molecule> extraMolecules = MoleculeIO.parseXml(new FileInputStream(file));
				int count = 0;
				for (Entry<String, Molecule> e : extraMolecules.entrySet()) {
					if (count >= options.dbSize) {
						break;
					}
					e.getValue().setSourceDb(Identify.DB_SOURCE_EXTRA);
					loadedMolecules.put(e.getKey(), e.getValue());
					count++;
				}

			}

			// combine everything together (the standard db + the extra db entries)
			List<String> standardKeys = new ArrayList<String>(loadedMolecules.keySet());
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			for (String key : standardKeys) {
				molecules.put(key, loadedMolecules.get(key));
			}		

			// final Header header = result.header;
			CountVotes formulaVoting = new CountVotes(options.prettyPrint);
			formulaVoting.initFromDb(molecules);
			formulaVoting.countVotesFromPeaks((IPeakSet<IPeak>) result.measurement);
			
			// the last two argument (idLevel, iterId) are not used here ...
			formulaVoting.printStatistics(options.input, options.alpha, options.threshold, options.dbSize, 0, 0);
			formulaVoting.printRocData(options.input, options.alpha, options.dbSize, 0, 0);
			
		}

		catch (Exception e) {
			Tool.unexpectedError(e, Options.APPLICATION);
		}
		
	}
	
	public void keepTrack(FormulaScore vote) {
		this.votes.put(vote, vote);
	}	
	
	public void printStatistics(String input, double alpha, double threshold, int dbSize, int idLevel, int iterId) {
				
		List<FormulaScore> sortedVotes = new ArrayList<FormulaScore>(this.votes.values());
		Collections.sort(sortedVotes, new FormulaScoreDescComparator());

		System.out.println();
		System.out.println("==================================");
		System.out.println("CountVotes.printStatistics():");
		System.out.println("Threshold is " + threshold);
		System.out.println("alpha is " + alpha);
		System.out.println("dbSize is " + dbSize);
		System.out.println("idLevel is " + idLevel);
		System.out.println("==================================");
		
		int tp = 0;
		int fp = 0;
		int tn = 0;
		int fn = 0;

		for (FormulaScore f : sortedVotes) {
			
			System.out.println(f);
			if (f.getScore().compareTo(threshold) == 0 || f.getScore().compareTo(threshold) > 0) {

				// check formula votes >= threshold, then it's positive
				if (Identify.DB_SOURCE_STANDARD.equals(f.getDbId())) {

					// in the standard database, so it's true positive
					tp++;						
				
				} else {
				
					// otherwise it's a false positive
					fp++;
				
				}

			} else {
			
				// otherwise below threshold, then it's negative
				if (Identify.DB_SOURCE_STANDARD.equals(f.getDbId())) {
				
					// in the standard database, so it's false negative
					fn++;
				
				} else {
				
					// otherwise it's a true negative
					tn++;
			
				}
				
			}
			
		}
		
		ExperimentResult expResult = new ExperimentResult(input, alpha, iterId, dbSize, idLevel);
		expResult.storeSingleResult(tp, fp, tn, fn, threshold);
		expResult.printSingleResult();
		
	}	

	public void printRocData(String input, double alpha, int dbSize, int idLevel, int iterId) {
				
		List<FormulaScore> sortedVotes = new ArrayList<FormulaScore>(this.votes.values());
		Collections.sort(sortedVotes, new FormulaScoreAscComparator());

		System.out.println();
		System.out.println("==================================");
		System.out.println("CountVotes.printRocData():");
		System.out.println("dbSize is " + dbSize);
		System.out.println("idLevel is " + idLevel);
		System.out.println("==================================");
		
		boolean[] labels = new boolean[sortedVotes.size()];
		double[] scores = new double[sortedVotes.size()];		
		
		for (int i = 0; i < sortedVotes.size(); i++) {		
			FormulaScore f = sortedVotes.get(i);
			labels[i] = Identify.DB_SOURCE_STANDARD.equals(f.getDbId());
			scores[i] = f.getScore();
		}
				
		ExperimentResult expResult = new ExperimentResult(input, alpha, iterId, dbSize, idLevel);
		
		double[] tprArr = new double[sortedVotes.size()];
		double[] fprArr = new double[sortedVotes.size()];
		for (int threshold = 0; threshold < labels.length; threshold++) {

			FormulaScore thElem = sortedVotes.get(threshold);
			
			int tp = 0;
			int fp = 0;
			int tn = 0;
			int fn = 0;

			for (int i = 0; i < labels.length; i++) {
				if (i >= threshold) {
					if (labels[i]) {
						tp++;
					} else {
						fp++;
					}
				} else {
					if (labels[i]) {
						fn++;
					} else {
						tn++;
					}
				}
			}	

			RocData rocData = expResult.storeRocData(tp, fp, tn, fn);
			double tpr = rocData.getTpr();
			double fpr = rocData.getFpr();
			double f1 = rocData.getF1();
			
			String thresholdStr = String.format("%.2f", thElem.getScore());
			String dbSizeStr = String.format("%d", dbSize);	
			String idLevelStr = String.format("%d", idLevel);	
			String alphaStr = String.format("%.2f", alpha);
			
			String tprStr = String.format("%.4f", tpr);
			String fprStr = String.format("%.4f", fpr);
			String f1Str = String.format("%.4f", f1);
			
			tprArr[threshold] = tpr;
			fprArr[threshold] = fpr;
			
			System.out.println("RocData:" + 
					input + "," +
					alphaStr + "," +					
					iterId + "," + 
					thresholdStr + "," +
					dbSizeStr + "," +
					idLevelStr + "," +
					tp + "," + 
					fp + "," + 
					tn + "," + 
					fn + "," + 
					tprStr + "," + 
					fprStr + "," + 
					f1Str);		
						
		}	
		
		System.out.println();
		expResult.printRocData(labels, scores);
		
	}	
	
	/**
	 * Initialize empty votes for everything in the standard db. 
	 * Need this to correctly compute the negatives.
	 * @param standards
	 */
	private void initFromDb(HashMap<String, Molecule> standards) {
		for (Entry<String, Molecule> e : standards.entrySet()) {
			Molecule mol = e.getValue();
			String molId = mol.getDatabaseID();
			String molFormula = mol.getPlainFormula();
			String dbId = mol.getSourceDb();
			FormulaScore emptyVote = new FormulaScore(molId, dbId, molFormula);
			this.votes.put(emptyVote, emptyVote);
		}
	}
	
	/**
	 * Check whether we need to show help
	 * @param args
	 * @param options
	 * @param cmdline
	 * @throws CmdLineException
	 */
	private static void showHelp(String[] args, Options options,
			CmdLineParser cmdline) throws CmdLineException {
		
		cmdline.parse(args);
		if (options.help) {
			Tool.printHeader(System.out, Options.APPLICATION,
					Options.VERSION);
			cmdline.printUsage(System.out, "");
			System.exit(0);
		}

		if (options.verbose) {
			Tool.printHeader(System.out, Options.APPLICATION,
					Options.VERSION);
			cmdline.printOptions();
		}

	}

	/**
	 * Display message when verbose option is true
	 * @param options
	 * @param msg
	 */
	private static void displayMessage(Options options, String msg) {
		if (options.verbose) {
			System.out.println(msg);
		}
	}

	private void countVotesFromPeaks(IPeakSet<IPeak> peaks) {
		
		int maxNo = 0;
		IPeak maxPeak = null;
		for (IPeak p : peaks) {
			
			Annotation ids = p.getAnnotation(Annotation.identification);
			Annotation formulas = p.getAnnotation(Annotation.formula);
			Annotation dbs = p.getAnnotation(Annotation.db);
			if (ids != null) {

				/*
				 * these two parallel arrays keep track of annotated 
				 * molecule ids and the originating database
				 */
				String[] molIds = ids.getValueAsString().split(",");
				String[] molDbs = dbs.getValueAsString().split(",");
				String[] molFs = formulas.getValueAsString().split(",");
				assert(molIds.length == molDbs.length);
				assert(molIds.length == molFs.length);
				
				if (molIds.length > maxNo) {
					maxNo = molIds.length;
					maxPeak = p;
				}
				
				int length = molIds.length;								
				for (int i = 0; i < length; i++) {
					
					String molId = molIds[i].trim();
					String molFormula = molFs[i].trim();
					String dbId = molDbs[i].trim();
					FormulaScore lookup = new FormulaScore(molId, dbId, molFormula);
					
					if (this.votes.containsKey(lookup)) {
						/* 
						 * increase votes count of formula if it's already there:
						 * 	for each peak, a list of molecules (of size length) are annotated to it
						 *  we compute a new increment, i = 1/length, and add this to existing votes
						 */
						this.votes.get(lookup).incrementScoreByVote(length);
					} else {
						// otherwise, initialize new votes
						lookup.newScoreByVote(length);
						this.votes.put(lookup, lookup);
					}
				
					// System.out.println("#" + counter + ":\t" + lookup);						

				}
				
			}
		}
		
		Annotation ids = maxPeak.getAnnotation(Annotation.identification);
		String[] molIds = ids.getValueAsString().split(",");		
		System.out.println("Most annotated peak has " + molIds.length + " annotations");
		
	}
		
}
