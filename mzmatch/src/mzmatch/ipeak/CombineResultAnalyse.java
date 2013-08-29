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

// java
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;

import com.joewandy.alignmentResearch.comparator.NaturalOrderFilenameComparator;

public class CombineResultAnalyse {
	
	private static final int OVERLAP_SIZE = 3;
	private final static String version = "1.0";
	private final static String application = "CombineResultAnalyse";

	@OptionsClass(name = application, version = version, author = "Joe Wandy (j.wandy.1@research.gla.ac.uk)", 
			description = "Analyse the results of several Combine results, produced using different matching methods"
	)
	public static class Options {

		@Option(name = "d", param = "filename", type = Option.Type.REQUIRED_ARGUMENT, level = Option.Level.USER, usage = "The directory of input files in the FeatureML file format.")
		public String inputDirectory = null;
		
		@Option(name = "h", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the help is shown.")
		public boolean help = false;

		@Option(name = "v", param = "", type = Option.Type.NO_ARGUMENT, level = Option.Level.SYSTEM, usage = "When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
		
	}

	@SuppressWarnings("unchecked")
	public static void main(String args[]) {
		
		try {

			Tool.init();

			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);

			// check whether we need to show the help
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

			// sort input file alphabetically to look nicer
			Map<String, IPeakSet<IPeak>> peaksets = new HashMap<String, IPeakSet<IPeak>>();
			File inputDirectory = new File(options.inputDirectory);
			File[] listOfFiles = inputDirectory.listFiles();
			Arrays.sort(listOfFiles, new NaturalOrderFilenameComparator()); 	
			for (int i = 0; i < listOfFiles.length; i++) {

				// load & parse the actual featureXML file here
				if (listOfFiles[i].isFile()) {
				
					File myFile = listOfFiles[i];
					String filename = myFile.getName();
					String path = myFile.getPath();
					String extension = filename.substring(filename.lastIndexOf('.')+1);
					
					// ignore other files in the directory
					if (!"peakml".equals(extension.toLowerCase())) {
						continue;
					}
										
					if (options.verbose) {
						System.out.println("Loading " + path);
					}
					ParseResult result = PeakMLParser.parse(new FileInputStream(
							path), true);
					peaksets.put(filename, (IPeakSet<IPeak>) result.measurement);
					
				}
				
			}
			
			if (options.verbose) {
				System.out.println("peaksets.size() = " + peaksets.size());				
			}
			
			processResult(peaksets);
			
		} catch (Exception e) {
			Tool.unexpectedError(e, application);
		}
		
	}

	private static void processResult(Map<String, IPeakSet<IPeak>> allAlignedPeakSets) {

		Map<String, Set<AlignedRow>> fileToAlignedRows = new HashMap<String, Set<AlignedRow>>();
		for (Entry<String, IPeakSet<IPeak>> alignedPeakSets : allAlignedPeakSets.entrySet()) {
			
			String filename = alignedPeakSets.getKey();
			System.out.println("Processing " + filename);
			System.out.println("\tNo. of aligned peakset = " + alignedPeakSets.getValue().size());
			Map<Integer, Integer> counts = new HashMap<Integer, Integer>();
			Set<AlignedRow> alignedRows = new HashSet<AlignedRow>();
			fileToAlignedRows.put(filename, alignedRows);
			for (IPeak alignedPeakSet : alignedPeakSets.getValue()) {
				@SuppressWarnings("unchecked")
				IPeakSet<IPeak> aps = (IPeakSet<IPeak>) alignedPeakSet;
				int size = aps.getPeaks().size();
				if (counts.containsKey(size)) {
					int oldValue = counts.get(size);
					counts.put(size, oldValue+1);
				} else {
					counts.put(size,  1);
				}
				if (size == CombineResultAnalyse.OVERLAP_SIZE) {
					List<IPeak> peaks = aps.getPeaks();
					AlignedRow row = new AlignedRow();
					for (IPeak peak : peaks) {
						// these annotations were set from IPeakSet.setSourcePeakset()
						int sourcePeakSet = peak.getAnnotation(Annotation.sourcePeakset)
								.getValueAsInteger();
						int peakId = peak.getAnnotation(Annotation.peakId)
								.getValueAsInteger();
						AlignedPeak origin = new AlignedPeak(sourcePeakSet, peakId, peak);
						row.add(origin);
					}
					alignedRows.add(row);
				}
			}
			System.out.println("\t" + counts);
			
		}
		System.out.println();
		
//		printIntersection(allAlignedPeakSets, fileToAlignedRows);
		printUnique(allAlignedPeakSets, fileToAlignedRows);
		
	}

	private static void printUnique(
			Map<String, IPeakSet<IPeak>> allAlignedPeakSets,
			Map<String, Set<AlignedRow>> fileToAlignedRows) {
		
		System.out.println("Printing out unique peaks");
		for (Entry<String, IPeakSet<IPeak>> alignedPeakSets : allAlignedPeakSets.entrySet()) {
			
			String file1 = alignedPeakSets.getKey();
			Set<AlignedRow> rows1 = fileToAlignedRows.get(file1);
			System.out.println("Processing " + file1 + " rows size = " + rows1.size());
			Set<AlignedRow> difference = new HashSet<AlignedRow>(rows1);
			for (Entry<String, IPeakSet<IPeak>> alignedPeakSets2 : allAlignedPeakSets.entrySet()) {
				String file2 = alignedPeakSets2.getKey();
				if (file1.equals(file2)) {
					continue;
				}
				Set<AlignedRow> rows2 = fileToAlignedRows.get(file2);				
				difference = getDifference(file1, file2, difference, rows2);
				System.out.println("\tChecking " + file2 + ", difference = " + difference.size());
			}
			
			System.out.println("\tFinal unique aligned peaks for " + file1 + " = " + difference.size());
			for (AlignedRow row : difference) {
				System.out.println("\t\t" + row);
			}
			System.out.println();			
			
		}
		
		
		
	}

	private static void printIntersection(
			Map<String, IPeakSet<IPeak>> allAlignedPeakSets,
			Map<String, Set<AlignedRow>> fileToAlignedRows) {
		System.out.println("Checking intersection");
		for (Entry<String, IPeakSet<IPeak>> alignedPeakSets : allAlignedPeakSets.entrySet()) {
			String file1 = alignedPeakSets.getKey();
			Set<AlignedRow> rows1 = fileToAlignedRows.get(file1);
			System.out.println("\t" + file1 + " " + rows1.size());
			System.out.println();
			for (Entry<String, IPeakSet<IPeak>> alignedPeakSets2 : allAlignedPeakSets.entrySet()) {
				String file2 = alignedPeakSets2.getKey();
				if (file1.equals(file2)) {
					continue;
				}
				Set<AlignedRow> rows2 = fileToAlignedRows.get(file2);
				Set<AlignedRow> twoIntersect = getIntersect(rows1, rows2);
				int intersectCount2 = twoIntersect.size();
				System.out.println("\t" + file1 + " intersect " + file2 + " = " + intersectCount2);
				for (Entry<String, IPeakSet<IPeak>> alignedPeakSets3 : allAlignedPeakSets.entrySet()) {
					String file3 = alignedPeakSets3.getKey();
					if (file1.equals(file3) || file2.equals(file3)) {
						continue;
					}
					Set<AlignedRow> rows3 = fileToAlignedRows.get(file3);
					Set<AlignedRow> threeIntersect = getIntersect(twoIntersect, rows3);
					int intersectCount3 = threeIntersect.size();
					System.out.println("\t" + file1 + " intersect " + file2 + " intersect " + file3 + " = " + intersectCount3);
				}
				System.out.println();
			}
		}
	}

	private static Set<AlignedRow> getIntersect(Set<AlignedRow> rows1,
			Set<AlignedRow> rows2) {
		Set<AlignedRow> intersection = new HashSet<AlignedRow>();
		for (AlignedRow row1 : rows1) {
			for (AlignedRow row2 : rows2) {
				if (row1.size() != CombineResultAnalyse.OVERLAP_SIZE || 
						row2.size() != CombineResultAnalyse.OVERLAP_SIZE) {
					continue;
				}
				if (row1.isIntersect(row2, CombineResultAnalyse.OVERLAP_SIZE)) {
					intersection.add(row1);
				}
			}
		}
		return intersection;
	}

	private static Set<AlignedRow> getDifference(String file1, String file2, Set<AlignedRow> rows1,
			Set<AlignedRow> rows2) {
	
		Set<AlignedRow> difference = new HashSet<AlignedRow>();
		for (AlignedRow row1 : rows1) {
	
			boolean found = false;
			for (AlignedRow row2 : rows2) {
				AlignedPeak entry1 = row1.getAlignedPeak(2, 620);
				AlignedPeak entry2 = row2.getAlignedPeak(2, 620);
				if (file1.equals("combined.greedy.peakml") && 
						file2.equals("combined.sima.peakml") && 
						entry1 != null && 
						entry2 != null) {
					 System.out.println(entry1);
				}				
				if (row1.size() != CombineResultAnalyse.OVERLAP_SIZE || 
						row2.size() != CombineResultAnalyse.OVERLAP_SIZE) {
					continue;
				}
				if (row1.isIntersect(row2, CombineResultAnalyse.OVERLAP_SIZE)) {
					found = true;
				}
			}
			if (!found) {
				difference.add(row1);
			}
			
		}
		return difference;
	}
	
}

class AlignedPeak {
	private IPeak peak;
	private int sourcePeakSet;
	private int peakId;
	public AlignedPeak(int sourcePeakSet, int peakId, IPeak peak) {
		this.sourcePeakSet = sourcePeakSet;
		this.peakId = peakId;			
		this.peak = peak;
	}
	public IPeak getPeak() {
		return peak;
	}

	public int getSourcePeakSet() {
		return sourcePeakSet;
	}

	public int getPeakId() {
		return peakId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + peakId;
		result = prime * result + sourcePeakSet;
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
		AlignedPeak other = (AlignedPeak) obj;
		if (peakId != other.peakId)
			return false;
		if (sourcePeakSet != other.sourcePeakSet)
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "AlignedPeak [sourcePeakSet=" + sourcePeakSet
				+ ", peakId=" + peakId + ", peak=(" 
				+ String.format("%.5f", peak.getMass()) + ", " 
				+ String.format("%.2f", peak.getRetentionTime()) + ", " 
				+ String.format("%.2f", peak.getIntensity()) + ")]";
	}
}

class AlignedRow {
	private Set<AlignedPeak> alignedPeaks;
	public AlignedRow() {
		alignedPeaks = new HashSet<AlignedPeak>();
	}
	public void add(AlignedPeak p) {
		alignedPeaks.add(p);
	}
	public int size() {
		return alignedPeaks.size();
	}
	public AlignedPeak getAlignedPeak(int sourcePeakSet, int peakId) {
		for (AlignedPeak peak : alignedPeaks) {
			if (peak.getSourcePeakSet() == sourcePeakSet && peak.getPeakId() == peakId) {
				return peak;
			}
		}
		return null;
	}
	public boolean isIntersect(AlignedRow another, int size) {
		Set<AlignedPeak> intersection = new HashSet<AlignedPeak>(alignedPeaks);
		intersection.retainAll(another.alignedPeaks);
		if (intersection.size() == size) {
			return true;
		}
		return false;
	}
	@Override
	public String toString() {
		String output = "Row:\n";
		for (AlignedPeak p : alignedPeaks) {
			output += "\t\t\t" + p + "\n";
		}
		return output;
	}
}