/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package mzmatch.ipeak;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Random;
import java.util.Vector;

import mzmatch.ipeak.combineMethod.CombineCustomJoinMethod;
import mzmatch.ipeak.combineMethod.CombineGreedyMethod;
import mzmatch.ipeak.combineMethod.CombineGroupingInfoMethod;
import mzmatch.ipeak.combineMethod.CombineMethod;
import mzmatch.ipeak.combineMethod.CombineMzMineJoinMethod;
import mzmatch.ipeak.combineMethod.CombineMzMineRANSACMethod;
import mzmatch.ipeak.combineMethod.CombineSIMAMethod;
import mzmatch.ipeak.sort.CorrelationMeasure;
import mzmatch.ipeak.sort.RelatedPeaks.CosineMeasure;
import mzmatch.ipeak.sort.RelatedPeaks.PearsonMeasure;
import mzmatch.util.Tool;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.FileInfo;
import peakml.io.Header;
import peakml.io.MeasurementInfo;
import peakml.io.ParseResult;
import peakml.io.SetInfo;
import peakml.io.peakml.PeakMLParser;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;
import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;

@SuppressWarnings("unchecked")
public class Combine {

	// implementation
	// we add the 1e6 here to prevent an overrun (unlikely that many files are
	// combined)
	public static final int id_padding = 1000000;

	public static void setMeasurementIDs(IPeak peak, int oldid, int newid) {
		if (peak.getMeasurementID() == oldid)
			peak.setMeasurementID(newid);

		if (IPeakSet.class.equals(peak.getClass())) {
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				setMeasurementIDs(p, oldid, newid);
		}
	}

	public static void updateSetInfo(SetInfo setinfo,
			HashMap<Integer, Integer> mids) {
		// replace the measurementid's
		Vector<Integer> measurementids = setinfo.getMeasurementIDs();
		for (int i = 0; i < measurementids.size(); ++i)
			measurementids.set(i, mids.get(measurementids.get(i)));

		// do the children
		for (SetInfo child : setinfo.getChildren())
			updateSetInfo(child, mids);
	}

	public static void removePadding(IPeak peak) {
		if (peak.getMeasurementID() >= id_padding)
			peak.setMeasurementID(peak.getMeasurementID() - id_padding);

		if (IPeakSet.class.equals(peak.getClass())) {
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				removePadding(p);
		}
	}

	/**
	 * This method copies the measurement information from the read file,
	 * assigns a new id to the measurement and updates all the data with the new
	 * measurement id.
	 * 
	 * @param header
	 *            The header to add the new entries to.
	 * @param result
	 *            The data read from an original file.
	 * @param filename
	 *            The filename of the original file.
	 */
	public static void updateHeader(Header header, SetInfo set,
			ParseResult result, String filename, int combination) {
		// for all the measurementinfo's in the read file
		HashMap<Integer, Integer> mids = new HashMap<Integer, Integer>();
		for (MeasurementInfo minfo : result.header.getMeasurementInfos()) {
			int measurementid = header.getNrMeasurementInfos();

			// copy all the information from the measurementinfo
			MeasurementInfo minfo_new = new MeasurementInfo(measurementid,
					minfo);
			header.addMeasurementInfo(minfo_new);

			// save the old measurement-id
			int oldid = minfo.getID();
			mids.put(oldid, measurementid);

			// add the filename to the list of files
			File file = new File(filename);
			minfo_new.addFileInfo(new FileInfo(file.getName(), file.getName(),
					file.getParent()));

			// reset all the measurement id's
			setMeasurementIDs((IPeakSet<IPeak>) result.measurement, oldid,
					measurementid + id_padding);
		}

		removePadding((IPeakSet<IPeak>) result.measurement);

		// create the set-info
		if (result.header.getNrSetInfos() == 0) {
			for (MeasurementInfo minfo : result.header.getMeasurementInfos())
				set.addMeasurementID(mids.get(minfo.getID()));
		} else {
			for (SetInfo oldsetinfo : result.header.getSetInfos()) {
				SetInfo newsetinfo = new SetInfo(oldsetinfo);
				updateSetInfo(newsetinfo, mids);
				header.addSetInfo(newsetinfo);
			}
		}
	}

	// main entrance
	final static String version = "1.0.0";
	final static String application = "Combine";

	@OptionsClass(name = application, version = version, author = "RA Scheltema (r.a.scheltema@rug.nl)", 
			description = "Combines the contents of a set of PeakML files, containing either mass chromatograms "
			+ "or backgroundions at the lowest level (the signal of which is needed in order to make "
			+ "a correct assesment of similarness). The approach starts from the most intense, "
			+ "unprocessed peak in the complete set signals, covering all the measurements, and attempts "
			+ "to find all those signals from the other measurements falling within the mass window "
			+ "(option 'ppm') and the retention time window (option 'rtwindow'). The correct match from "
			+ "each measurement to the currently most intense signal is consequently identified by "
			+ "optimizing on the difference in area under the curve. Signals caused by the same analyte "
			+ "are expected to roughly have a similar shape and retention time. All matched signals are "
			+ "then clustered for the output and marked as processed, after which a new iteration is "
			+ "begun."
			+ "\n\n"
			+ "This tool can be used to iteratively compound files. For example, when analyzing a time series "
			+ "experiment, firstly the biological replicates of each timepoint can first be combined into "
			+ "a single file, the set of which can be labeled as biological replicates with the option "
			+ "'combination'. Additional filtering operations can be applied in order to the timepoint "
			+ "combinations before proceeding to combine all the timepoints in a final set.", example = "Windows batch-file:\n"
			+ "SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n"
			+ "\n"
			+ "REM extract all the mass chromatograms\n"
			+ "%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i raw\\*.mzXML -o peaks\\ -ppm 3\n"
			+ "\n"
			+ "REM combine the individual timepoints\n"
			+ "%JAVA% mzmatch.ipeak.Combine -v -i peaks\\24hr_*.peakml -o 24hr.peakml -ppm 3 -rtwindow 30 -combination biological\n"
			+ "%JAVA% mzmatch.ipeak.Combine -v -i peaks\\28hr_*.peakml -o 28hr.peakml -ppm 3 -rtwindow 30 -combination biological\n"
			+ "%JAVA% mzmatch.ipeak.Combine -v -i peaks\\32hr_*.peakml -o 32hr.peakml -ppm 3 -rtwindow 30 -combination biological\n"
			+ "\n"
			+ "REM combine all timepoints in a single file\n"
			+ "%JAVA% mzmatch.ipeak.Combine -v -i *hr.peakml -o timeseries.peakml -ppm 3 -rtwindow 30 -combination set\n")
	
	public static class Options {
		
		/*
		 * BASIC OPTIONS
		 */
		
		@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Option for the input files. Multiple files can be passed by separating them "
				+ "with a comma (ie ,) or the use of a name with a wildcard (eg samples_*hrs.xml). The "
				+ "only allowed file format is PeakML containing either mass chromatograms or backgroundions "
				+ "at the lowest level (ie the result of another Combine can be used).")
		public Vector<String> input = new Vector<String>();

		@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Option for the ouput file. The resulting matches are written to this file in the "
				+ "PeakML file format."
				+ "\n"
				+ "When this option has not been set the output is written to the standard output. Be sure "
				+ "to unset the verbose option when setting up a pipeline reading and writing from the "
				+ "standard in- and outputs.")
		public String output = null;

		@Option(name = "label", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Optional label for the set being made. The labels are stored in the header "
				+ "of the resulting file and used for display purposes.")
		public String label = null;

		@Option(name = "labels", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Optional labels for the input files. When these are used make sure to give "
				+ "as many labels as there are input files. The labels are stored in the header "
				+ "of the resulting file and used for display purposes.")
		public Vector<String> labels = new Vector<String>();

		@Option(name = "ppm", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "The accuracy of the measurement in parts-per-milion. This value is used for the "
				+ "matching of mass chromatogram (collections) and needs to bereasonable for the equipment "
				+ "used to make the measurement (the LTQ-Orbitrap manages approximately 3 ppm).")
		public double ppm = -1;

		@Option(name = "rtwindow", param = "double", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "The retention time window in seconds, defining the range where to look for matches.")
		public double rtwindow = -1;
		
		@Option(name = "combination", param = "see description", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "- set\n"
				+ "  The files are to be combined as a true set.\n"
				+ "- technical\n"
				+ "  The files are to be combined as technical replicates.\n"
				+ "- biological\n"
				+ "  The files are to be combined as biological replicates.\n")
		public String combination = "";

		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, 
				usage = "When this is set, the help is shown.")
		public boolean help = false;
		
		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, 
				usage = "When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;

		/*
		 * OPTIONS FOR VARIOUS OTHER ALIGNMENT ALGORITHMS THAT CAN BE CALLED
		 */
		
		@Option(name = "method", param = "string", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Which method to use to perform the alignment of matching peaksets. " +
						"Valid options are greedy, join, ransac, sima and groupingInfo (experimental).")
		public String method = "greedy";
		
		@Option(name = "ransacRtToleranceBeforeMinute", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Ransac parameter.")
		public double ransacRtToleranceBeforeMinute = AlignmentMethodParam.PARAM_RT_TOLERANCE_BEFORE_CORRECTION;

		@Option(name = "ransacIteration", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Ransac parameter.")
		public int ransacIteration = AlignmentMethodParam.PARAM_RANSAC_ITERATION;
		
		@Option(name = "ransacNMinPoints", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Ransac parameter.")
		public double ransacNMinPoints = AlignmentMethodParam.PARAM_MINIMUM_NO_OF_POINTS;

		@Option(name = "ransacThreshold", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Ransac parameter.")
		public double ransacThreshold = AlignmentMethodParam.PARAM_THRESHOLD_VALUE;
		
		@Option(name="ransacLinearModel", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
				usage="Ransac parameter")
		public boolean ransacLinearModel = AlignmentMethodParam.PARAM_LINEAR_MODEL;

		@Option(name="ransacSameChargeRequired", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
				usage="Ransac parameter")
		public boolean ransacSameChargeRequired = AlignmentMethodParam.PARAM_REQUIRE_SAME_CHARGE_STATE;
		
		/*
		 * OPTIONS FOR PEAK GROUPING
		 */

		@Option(name = "measure", param = "string", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "The measure of correlations between peaks. Valid options are pearson and cosine.")
		public String measure = "pearson";

		@Option(name = "p1", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for mixture model clustering.")
		public double p1 = 0.001f;

		@Option(name = "p0", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for mixture model clustering.")
		public double p0 = 0.97f;

		@Option(name = "alpha", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for mixture model clustering.")
		public double alpha = 1.0f;

		@Option(name = "alpha0", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for BetaBeta mixture model clustering.")
		public float alpha0 = 2.0f;

		@Option(name = "alpha1", param = "float", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for BetaBeta mixture model clustering.")
		public float alpha1 = 10.0f;

		@Option(name = "numSamples", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for mixture model clustering.")
		public int numSamples = 20;

		@Option(name = "initialNumClusters", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for mixture model clustering.")
		public int initialNumClusters = 10;

		@Option(name = "compoundDatabases", param = "int", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for optional compound database to match against during mixture model clustering.")
		public Vector<String> compoundDatabases = new Vector<String>();

		@Option(name = "seed", param = "long", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, 
				usage = "Parameter for mixture model clustering.")
		public long seed = -1;
		
		@Option(name="burnIn", param="int", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, 
				usage="The amount of initial samples to discard."
		)
		public int burnIn = 30;
		
		@Option(name="retentionTimeSD", param="float", type=Option.Type.REQUIRED_ARGUMENT, 
				usage="The ratio of the maximum peak distribution to the baseline distribution"
		)
		public double retentionTimeSD = 2.5;
		
		@Option(name="rtClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
				usage="A flag to specify whether clustering using retention time should be used"
		)
		public boolean rtClustering = true;
		
		@Option(name="corrClustering", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, 
				usage="A flag to specify whether clustering using peak shape correlations should be used"
		)
		public boolean corrClustering = true;		

		@Option(name="debug", param="boolean", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, 
				usage="Should debugging information be printed."
		)
		public boolean debug = true;		
		
	}

	public static void main(String args[]) {
		try {
			
			Tool.init();

			// parse the commandline options
			final Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			cmdline.parse(args);

			// check whether we need to show the help
			if (options.help) {
				Tool.printHeader(System.out, application, version);
				cmdline.printUsage(System.out, "");
				System.exit(0);
			}

			if (options.verbose) {
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}

			// check the command-line parameters

			// are the required options set
			if (options.ppm == -1) {
				System.err
						.println("[ERROR]: the ppm value needs to be set");
				System.exit(0);
			}
			if (options.rtwindow == -1) {
				System.err
						.println("[ERROR]: the rtwindow value needs to be set");
				System.exit(0);
			}			
			// are there enough labels?
			if (options.labels.size() > 0
					&& (options.labels.size() != options.input.size())) {
				System.err
						.println("[ERROR]: the number of labels does not equal the number of input files");
				System.exit(0);
			}

			// convert the combination option
			int combination = -1;
			if (options.combination.equals("set"))
				combination = SetInfo.SET;
			else if (options.combination.equals("technical"))
				combination = SetInfo.TECHNICAL_REPLICATES;
			else if (options.combination.equals("biological"))
				combination = SetInfo.BIOLOGICAL_REPLICATES;
			else {
				System.err
						.println("[ERROR]: the combination option was not set correctly '"
								+ options.combination + "'.");
				System.exit(0);
			}

			// if the output dir does not exist, create it
			if (options.output != null) {
				Tool.createFilePath(options.output, true);
			}
			
			if (!AlignmentMethodFactory.ALIGNMENT_METHOD_GREEDY.equals(options.method)
					&& !AlignmentMethodFactory.ALIGNMENT_METHOD_CUSTOM_JOIN.equals(options.method)
					&& !AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.equals(options.method)
					&& !AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.equals(options.method)
					&& !AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.equals(options.method)
					&& !AlignmentMethodFactory.ALIGNMENT_METHOD_GROUPING_INFO.equals(options.method)) {
				System.err
						.println("[ERROR]: valid options for method are 'greedy', 'join', 'ransac', 'sima' or 'groupingInfo'.");
				System.exit(1);
			}

			if (!"pearson".equals(options.measure)
					&& !"cosine".equals(options.measure)) {
				System.err
						.println("[ERROR]: valid options for method are 'pearson' and 'cosine'.");
				System.exit(1);
			}

			// open the streams
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);

			// the header
			Header header = new Header();

			// load the data
			String setid = options.label != null ? options.label
					: options.output;
			SetInfo set = new SetInfo(setid, combination);
			header.addSetInfo(set);

			Vector<Header> headers = new Vector<Header>();
			Vector<IPeakSet<IPeak>> peaksets = new Vector<IPeakSet<IPeak>>();
			if (options.verbose) {
				System.out.println("Loading:");
			}
			
			ParseResult[] results = new ParseResult[options.input.size()];
			for (int i = 0; i < options.input.size(); ++i) {
				
				String input = options.input.get(i);
				if (options.verbose) {
					System.out.println("sourcePeakSet " + i + " - " + input);
				}
				ParseResult result = PeakMLParser.parse(new FileInputStream(
						input), true);
				results[i] = result;

				// check whether we have loaded something we want
				if (!result.measurement.getClass().equals(IPeakSet.class)) {
					System.err
							.println("[ERROR]: the contents of the file was not stored as an IPeakSet.");
					System.exit(0);
				}

				// save the label
				result.header.addAnnotation("label",
						options.labels.size() != 0 ? options.labels.get(i)
								: new File(input).getName());

				// create the MeasurementInfo entries, set the appropriate
				// measurement-id's and update the sets
				updateHeader(header, set, result, input, combination);

				// store the data
				headers.add(result.header);
				peaksets.add((IPeakSet<IPeak>) result.measurement);
				
			}

			if (set.getNrMeasurementIDs() == 0 && set.getNrChildren() == 0) {
				header.getSetInfos().remove(set);
			}

			// initialize random seed
			final Random random = new Random();
			long seed = options.seed == -1 ? random.nextLong() : options.seed;
			random.setSeed(seed);
			// if (options.verbose)
			System.out.println("Random seed is: " + seed);

			// set correlations measures
			CorrelationMeasure measure = null;
			float rangeMin = -1.0f;
			float rangeMax = 1.0f;

			if ("pearson".equals(options.measure)) {
				measure = new PearsonMeasure();
				rangeMin = -1.0f;
				rangeMax = 1.0f;
			} else if ("cosine".equals(options.measure)) {
				measure = new CosineMeasure();
				rangeMin = 0.0f;
				rangeMax = 1.0f;
			}
			assert measure != null;

			// debugging only
			int totalPeaks = 0;
			for (int i = 0; i < peaksets.size(); i++) {
				IPeakSet<IPeak> ps = peaksets.get(i);
				ps.setSourcePeakset(i);
				totalPeaks += ps.getPeaks().size();
			}

			CombineMethod task = null;
			if (AlignmentMethodFactory.ALIGNMENT_METHOD_GREEDY.equals(options.method)) {
				task = new CombineGreedyMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_CUSTOM_JOIN.equals(options.method)) {
				task = new CombineCustomJoinMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.equals(options.method)) {
				task = new CombineMzMineJoinMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.equals(options.method)) {
				task = new CombineMzMineRANSACMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.equals(options.method)) {
				task = new CombineSIMAMethod();				
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_GROUPING_INFO.equals(options.method)) {
				task = new CombineGroupingInfoMethod();				
			}
			task.process(options, header, peaksets, results, random, measure,
					rangeMin, rangeMax, totalPeaks, output);
			System.exit(0);

		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}
		
	}

}
