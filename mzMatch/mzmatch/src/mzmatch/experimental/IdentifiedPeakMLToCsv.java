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
package mzmatch.experimental;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Vector;

import mzmatch.ipeak.sort.AnnotationSampleHandler.IdentifyingAnnotationSampleHandler;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;
import au.com.bytecode.opencsv.CSVWriter;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

public class IdentifiedPeakMLToCsv {
	
	// defined inside Identify
	public static String IdentificationAnnotation = "simpleIdentification";
	
	static class IdentificationResultComparator implements Comparator<IdentificationResult> {

		public int compare(IdentificationResult o1, IdentificationResult o2) {
			return - Double.compare(o1.getIntensity(), o2.getIntensity());
		}
		
	}
	
	public static void process(IPeakSet<IPeakSet<IPeak>> peaksets,
			OutputStream output, final double minProb, final double minIntensity) throws IOException {

		List<IdentificationResult> results = new ArrayList<IdentificationResult>();
		int seqNo = 0;
		for (int i = 0; i < peaksets.size(); ++i) {

			IPeakSet<IPeak> peakset = peaksets.get(i);

			// try with MetAssign annotation first
			Annotation annot = peakset
					.getAnnotation(IdentifyingAnnotationSampleHandler.filteredAnnotation);
			if (annot != null) {
				
				String annotStr = annot.getValue();

				// split identification annotation by ;
				String[] singleAnnots = annotStr.split(";");
				String singleAnnot = singleAnnots[0]; // take the one with highest posterior probability

				// split each single annotation by , to get its parts
				String[] tokens = singleAnnot.split(",");
				
				// e.g. StdMix1_37, Ethanolamine phosphate, M+Na, [12C]2[1H]8[14N]1[16O]3[18O]1[31P]1[23Na]1, 0.87500
				if (tokens.length == 5) {
					String id = tokens[0].trim();
					String adduct = tokens[2].trim();
					String isotope = tokens[3].trim();
					double prob = Double.parseDouble(tokens[4]);
					double mass = peakset.getMass();
					double rt = peakset.getRetentionTime();
					double intensity = peakset.getIntensity();
					String identification = id + ";" + adduct + ";" + isotope;
					IdentificationResult res = new IdentificationResult(seqNo, mass, rt, intensity, identification, prob);
					seqNo++;
					results.add(res);
				}

			} else {
				
				// then try with Identify's identification
				annot = peakset.getAnnotation(
						IdentifiedPeakMLToCsv.IdentificationAnnotation);
				if (annot != null) {

					String annotStr = annot.getValue();

					// split each single annotation by , to get its parts
					String[] tokens = annotStr.split(",");

					// e.g. StdMix1_37, Ethanolamine phosphate, M+Na, [12C]2[1H]8[14N]1[16O]3[18O]1[31P]1[23Na]1, 0.87500
					if (tokens.length == 5) {
						String id = tokens[0].trim();
						String adduct = tokens[2].trim();
						String isotope = tokens[3].trim();
						double prob = Double.parseDouble(tokens[4]);
						double mass = peakset.getMass();
						double rt = peakset.getRetentionTime();
						double intensity = peakset.getIntensity();
						String identification = id + ";" + adduct + ";" + isotope;
						IdentificationResult res = new IdentificationResult(seqNo, mass, rt, intensity, identification, prob);
						seqNo++;
						results.add(res);
					}
					
				}
				
			}

		}
		
		// count how many peak identifications per formula
		Map<String, PriorityQueue<IdentificationResult>> counter = new HashMap<String, PriorityQueue<IdentificationResult>>();
		for (IdentificationResult res : results) {
			String key = res.getIdentification();
			if (counter.containsKey(key)) {
				PriorityQueue<IdentificationResult> currentValues = counter.get(key);
				currentValues.add(res);
			} else {
				PriorityQueue<IdentificationResult> currentValues = new PriorityQueue<IdentificationResult>(1, new IdentificationResultComparator());
				currentValues.add(res);				
				counter.put(key, currentValues);
			}
		}
		
		Iterator<IdentificationResult> iter = results.iterator();
		int keepCount = 0;
		int removeCount = 0;
		while (iter.hasNext()) {

			IdentificationResult res = iter.next();

			// remove everything lower than threshold probability or intensity
			boolean belowThreshold = false;
			if (res.getProb() < minProb || res.getIntensity() < minIntensity) {
				belowThreshold = true;
			}
			
			// remove everything with multiple counts
			boolean multiplePeaks = false;
			String key = res.getIdentification();
			PriorityQueue<IdentificationResult> values = counter.get(key);
			if (values.size() > 1) {
				// but keep only the highest intensity peak
				if (!values.peek().equals(res)) {
					multiplePeaks = true;					
				} 
			}
			
			if (belowThreshold || multiplePeaks) {
				removeCount++;
				iter.remove();
			} else {
				keepCount++;
				System.out.println(res);
			}
			
		}
		
		System.out.println("keepCount = " + keepCount);
		System.out.println("removeCount = " + removeCount);
		
		// write header to csv
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(output), ',',
				CSVWriter.NO_QUOTE_CHARACTER);
		String[] csvHeader = new String[] { "m/z", "RT", "Intensity",
				"Identification", "Prob"};
		writer.writeNext(csvHeader);

		for (IdentificationResult res : results) {
			writer.writeNext(res.toArray());
			writer.flush();			
		}
		
		writer.close();

	}

	// main entrance
	final static String version = "1.0";
	final static String application = "IdentifiedPeakMLToCsv";

	@OptionsClass(name = application, version = version, author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", 
			description = "Converts peakML files identified by MetAsign to CSV format")
	public static class Options {

		@Option(name = "i", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, 
				level = Option.Level.USER, usage = "The input file in the PeakML file format. " +
						"Input file should have been identified (annotated) using the MetAssign tool.")
		public Vector<String> input = new Vector<String>();

		@Option(name = "o", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, 
				level = Option.Level.USER, usage = "The output file in CSV format.")
		public String output = null;

		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, 
				level = Option.Level.SYSTEM, usage = "When this is set, the help is shown.")
		public boolean help = false;

		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, 
				level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
		
		@Option(name="minProb", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
				"The minimum posterior probability of peak annotation.")
		public double minProb = 0.90;

		@Option(name="minIntensity", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
				"The minimum intensity of peak annotation.")
		public double minIntensity = 10000;		
		
	}

	public static void main(String args[]) {

		try {

			Tool.init();
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			cmdline.parse(args);
			if (options.help) {
				Tool.printHeader(System.out, application, version);
				cmdline.printUsage(System.out, "");
				return;
			}
			if (options.verbose) {
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}

			// if the output directories do not exist, create them
			if (options.output != null) {
				Tool.createFilePath(options.output, options.input.size() == 1);
			}

			for (String filename : options.input) {

				InputStream input = System.in;
				if (filename != null) {
					input = new FileInputStream(filename);
				}

				OutputStream out_output = System.out;
				if (options.input.size() == 1) {
					if (options.output != null)
						out_output = new FileOutputStream(options.output);
				} else {
					String name = new File(filename).getName();
					out_output = new FileOutputStream(options.output + "/"
							+ name);
				}

				// load the data
				if (options.verbose)
					System.out.println("Loading data");
				ParseResult result = PeakMLParser.parse(input, true);
				IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;

				// process and write
				if (options.verbose)
					System.out.println("Processing data");
				if (peaks.getContainerClass().equals(MassChromatogram.class)) {
					;
				} else if (peaks.getContainerClass().equals(IPeakSet.class)) {
					process((IPeakSet<IPeakSet<IPeak>>) result.measurement, out_output, options.minProb, options.minIntensity);
				} else {
					System.err.println("Unknown IPeak type: "
							+ peaks.getContainerClass().getClass().getName());
				}
				
				System.out.println("DONE !");

			}

		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}

	}
	
}
