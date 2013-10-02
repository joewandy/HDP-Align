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

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodFactory;

@SuppressWarnings("unchecked")
public class Combine {

	// implementation
	// we add the 1e6 here to prevent an overrun (unlikely that many files are
	// combined)
	public static final int id_padding = 1000000;

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

	private static void setMeasurementIDs(IPeak peak, int oldid, int newid) {
		if (peak.getMeasurementID() == oldid)
			peak.setMeasurementID(newid);

		if (IPeakSet.class.equals(peak.getClass())) {
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				setMeasurementIDs(p, oldid, newid);
		}
	}

	private static void updateSetInfo(SetInfo setinfo,
			HashMap<Integer, Integer> mids) {
		
		// replace the measurementid's
		Vector<Integer> measurementids = setinfo.getMeasurementIDs();
		for (int i = 0; i < measurementids.size(); ++i)
			measurementids.set(i, mids.get(measurementids.get(i)));

		// do the children
		for (SetInfo child : setinfo.getChildren())
			updateSetInfo(child, mids);

	}

	private static void removePadding(IPeak peak) {
		if (peak.getMeasurementID() >= id_padding)
			peak.setMeasurementID(peak.getMeasurementID() - id_padding);

		if (IPeakSet.class.equals(peak.getClass())) {
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				removePadding(p);
		}
	}
	
	public static void main(String args[]) {
		
		try {
			
			Tool.init();

			// parse the commandline options
			final CombineOptions options = new CombineOptions();
			CmdLineParser cmdline = new CmdLineParser(options);
			cmdline.parse(args);			
			CombineOptionsValidator validator = new CombineOptionsValidator(options, cmdline);
			validator.validateOptions();

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
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_JOIN.equals(options.method)) {
				task = new CombineCustomJoinMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_JOIN.equals(options.method)) {
				task = new CombineMzMineJoinMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MZMINE_RANSAC.equals(options.method)) {
				task = new CombineMzMineRANSACMethod();
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_SIMA.equals(options.method)) {
				task = new CombineSIMAMethod();				
			} else if (AlignmentMethodFactory.ALIGNMENT_METHOD_MY_STABLE_MARRIAGE.equals(options.method)) {
				task = new CombineGroupingInfoMethod();				
			}
			task.process(options, header, peaksets, results, random, measure,
					rangeMin, rangeMax, totalPeaks, output);
			System.exit(0);

		} catch (Exception e) {
			Tool.unexpectedError(e, CombineOptions.APPLICATION);
		}
		
	}

}
