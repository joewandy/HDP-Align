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



package com.joewandy.mzmatch;


// java
import java.io.*;

import java.util.*;
import java.util.zip.*;

// libraries
import cmdline.*;

// mzmatch
import mzmatch.util.*;

// peakml
import peakml.*;
import peakml.math.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;






@SuppressWarnings("unchecked")
public class CountIds
{
	// implementation
	final static double isotope_c13 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.CARBON,		1);
	final static double isotope_n15 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.NITROGEN,	1);
	final static double isotope_o18 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.OXYGEN,		1);
	final static double isotope_s34 = PeriodicTable.getIsotopeMassDifference(PeriodicTable.SULFUR,		1);
	
	final static double H2 =  2*PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(Mass.MONOISOTOPIC);
	final static double H2O = 2*PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(Mass.MONOISOTOPIC) + PeriodicTable.elements[PeriodicTable.OXYGEN].getMass(Mass.MONOISOTOPIC);
	final static double NH3 = 3*PeriodicTable.elements[PeriodicTable.HYDROGEN].getMass(Mass.MONOISOTOPIC) + PeriodicTable.elements[PeriodicTable.NITROGEN].getMass(Mass.MONOISOTOPIC);
	
	
	public static final double[] getIntensityCourse(IPeak peak, Header header)
	{
		if (peak instanceof IPeakSet)
		{
			double intensitycourse[] = new double[header.getNrMeasurementInfos()];
			for (int i=0; i<intensitycourse.length; ++i)
				intensitycourse[i] = 0;
			for (IPeak p : (IPeakSet<? extends IPeak>) peak)
				intensitycourse[header.indexOfMeasurementInfo(p.getMeasurementID())] = p.getIntensity();
			return intensitycourse;
		}
		
		return new double[]{peak.getIntensity()};
	}
	
	public static void findIsotopes(IPeak basepeak, int z, Vector<IPeak> peaks, double ppm, int i)
	{
		double basepeakmass = basepeak.getMass();
		String relation = (basepeak.getAnnotation(Annotation.relationship)==null ? "" : basepeak.getAnnotation(Annotation.relationship).getValue() + "|");
		for (IPeak peak : peaks)
		{
			double mass = peak.getMass();
			
			double _ppm = ppm;
			if (peak.getIntensity() < 100000)
				_ppm *= 2;
			
			boolean match = false;
			if (PeriodicTable.inRange(basepeakmass+i*(isotope_c13/z), mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "C13 isotope #" + i);
				match = true;
			}
			else if (PeriodicTable.inRange(basepeakmass+i*isotope_n15, mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "N15 isotope #" + i);
				match = true;
			}
			else if (PeriodicTable.inRange(basepeakmass+i*isotope_o18, mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "O18 isotope #" + i);
				match = true;
			}
			else if (PeriodicTable.inRange(basepeakmass+i*isotope_s34, mass, _ppm))
			{
				peak.addAnnotation(Annotation.relationship, relation + "S34 isotope #" + i);
				match = true;
			}
			
			if (match == true)
				findIsotopes(basepeak, z, peaks, ppm, i+1);
		}
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "RelatedPeaks";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"LC-MS experiments yield large amounts of peaks, many of which correspond to derivatives of peaks " +
		"of interest (eg, isotope peaks, adducts, fragments, multiply charged molecules), termed here as " +
		"related peaks. This tool clusters groups of related peaks together and attempts to identify their " +
		"relationship. Each cluster is given a unique id, which is stored as an annotation with the name '" +
		Annotation.relationid + "'. The relationship is stored as an annotation with the name '" +
		Annotation.relationship + "'." +
		"\n\n" +
		"The possibility is offered only to store the most intense peak of each cluster (option '" +
		"basepeak'). This is useful for cleaning up the data before attempting statistical approaches " +
		"to mine the data. However, as the peak of interest is not per definition the most intense peak " +
		"one cannot rely on this file for identification purposes." +
		"\n\n" +
		"It is advised to only run this application on a combined file containing a complex experiment (e.g. " +
		"time-series, comparison, etc), as the performance of the approach is expected to improve with " +
		"increasingly complex setups. The approach iterates through the peak list sorted on descending " +
		"intensity and works threefold for identifying all of the derivatives. (1) the most intense, not " +
		"yet processed peak is selected and all not yet processed peaks within the specified retention time " +
		"window are collected. (2) First all of the collected peaks are correlated to the most intense " +
		"peak on signal shape. For this only the peaks from the same measurement are compared, as otherwise " +
		"distorted peak shapes would unfairly reduce the score. (3) The surviving peaksets are then " +
		"correlated on intensity trend (hence the need for complex setups). This is the most informative " +
		"feature, as it is not likely that that two co-eluting peaks have the same intensity trend for " +
		"complex setups. All signals to correlate well enough to the selected, most intense signal are " +
		"then clustered with this most intense signal.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.ExtractMassChromatograms -v -i raw\\*.mzXML -o peaks\\ -ppm 3\n" +
		"\n" +
		"REM combine the individual timepoints\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\24hr_*.peakml -o 24hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\28hr_*.peakml -o 28hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i peaks\\32hr_*.peakml -o 32hr.peakml -ppm 3 -rtwindow 30 -combination biological\n" +
		"\n" +
		"REM combine all timepoints in a single file\n" +
		"%JAVA% mzmatch.ipeak.Combine -v -i *hr.peakml -o timeseries.peakml -ppm 3 -rtwindow 30 -combination set\n" +
		"\n" +
		"REM detect the related peaks\n" +
		"%JAVA% mzmatch.ipeak.sort.RelatedPeaks -v -i timeseries.peakml -o timeseries_related.peakml -ppm 3 -rtwindow 15\n",
		references=
		"1. Tautenhahn R, Bottcher C, Neumann S: Annotation of LC/ESI-MS Mass Signals. In: Bioinformatics Research and Development. Hochreiter S, Wagner R (Ed.), Springer-Verlag, Heidelberg, Germany, 371-380 (2007).\n" +
		"2. Scheltema RA, Decuypere S, Dujardin JC, Watson DG, Jansen RC, and Breitling R: A simple data reduction method for high resolution LC/MS data in metabolomics. Bioanalysis - in press."
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Option for the input file. The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in. The contents of the file is enforced " +
			"to be peaksets (result of Combine) as this tool utilizes the full information " +
			"of peaksets in order to identify related peaks.")
		public String input = null;
		
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	public static boolean correlationGreater(IPeak peak, IPeakSet<IPeak> peaks) {
		final float maxCorrSignalsParameter = 0.7f;
		
		for (IPeak p : peaks) {
			if ( p.getAnnotation("id").getValueAsString().equals(peak.getAnnotation("id").getValueAsString()) ) {
				continue;
			}
			double corr_signals = peak.getSignal().pearsonsCorrelation(p.getSignal())[Statistical.PEARSON_CORRELATION];
			if ( corr_signals > maxCorrSignalsParameter ) {
				return true;
			}
		}
		return false;
	}
	
	public static void main(String args[])
	{
		try
		{
			Tool.init();
			
			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			
			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help)
			{
				Tool.printHeader(System.out, application, version);
				
				cmdline.printUsage(System.out, "");
				return;
			}
			
			if (options.verbose)
			{
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}
			

			

			
			
			// 
			if (options.verbose)
				System.out.println("loading data");
			//ParseResult result = PeakMLParser.parseIPeakSet(new FileInputStream(options.input), null);
			ParseResult result = PeakMLParser.parse(new FileInputStream(options.input), true);
			
			final Header header = result.header;
			final IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			

			Set<String> idsCovered = new HashSet<String>();
			Set<String> bpIdsCovered = new HashSet<String>();
			Set<String> clusters = new HashSet<String>();
			List<String> eachCluster = new ArrayList<String>();
			Set<String> coveredSingletonClusters = new HashSet<String>();
			Set<String> coveredNonSingletonClusters = new HashSet<String>();
			
			for (IPeak p : peaks) {
				
			}
			
			
			
			
			for (IPeak p : peaks) {
				String clusterId = p.getAnnotation(Annotation.relationid).getValueAsString();
				clusters.add(clusterId);
				eachCluster.add(clusterId);
			}
			
			
			int numberClusters = clusters.size();
			int numberNonSingletonClusters = 0;
			
			Map<String,Integer> clusterSizeMap = new HashMap<String,Integer>();
			for (String cluster : clusters) {
				int num = Collections.frequency(eachCluster, cluster);
				if ( num > 1 ) {
					numberNonSingletonClusters++;
				}
				clusterSizeMap.put(cluster, num);
			}
			
			//int truePositive = 0;
			int falsePositive = 0;
			int trueNegative = 0;
			//int falseNegative = 0;
			Set<String> falseNegatives = new HashSet<String>();
			
			for (IPeak p : peaks) {
				String clusterId = p.getAnnotation(Annotation.relationid).getValueAsString();
				int clusterSize = clusterSizeMap.get(clusterId);
				/*
				if ( clusterSize == 1 ) {
					//System.err.println("Here");
					if ( ! correlationGreater(p, peaks)) {
						//System.err.println("There");
						continue;
					}
				}
				*/
				
				
				String type = p.getAnnotation(Annotation.relationship).getValueAsString();
				Annotation idAnnotation = p.getAnnotation(Annotation.identification);
				//System.err.println(idAnnotation);
				if ( idAnnotation != null ) {
					String molId = idAnnotation.getValueAsString();
					idsCovered.add(molId);
					if ( type.equals("bp") ) {
						//truePositive++;
						bpIdsCovered.add(molId);
						if ( clusterSize == 1 ) {
							coveredSingletonClusters.add(molId);
						} else {
							coveredNonSingletonClusters.add(molId);
						}
					} else {
						falseNegatives.add(molId);
						//falseNegative++;
					}
				} else {
					if ( type.equals("bp") ) {
						//if ( clusterSize > 1) {
							falsePositive++;
						//}
					} else {
						trueNegative++;
					}
				}
				
			}
			int numberedCovered = idsCovered.size();
			int numberCoveredAsBP = bpIdsCovered.size();
			
			//Set<String> tpfnIntersection = new HashSet<String>()
			falseNegatives.removeAll(bpIdsCovered);
			int falseNegative = falseNegatives.size();
			
			int numCoveredSingleton = coveredSingletonClusters.size();
			int numCoveredNonSingleton = coveredNonSingletonClusters.size();
			int truePositive = numberCoveredAsBP;
			//int truePositive = numCoveredNonSingleton;
			
			float tpr = (float)truePositive / (float)(truePositive + falseNegative);
			float fpr = (float)falsePositive / (float)(falsePositive + trueNegative);
			float f1 = (float)(2 * truePositive) / (float)(2 * truePositive + falsePositive + falseNegative);
			double mcc = (double)(truePositive * trueNegative - falsePositive * falseNegative) /
				Math.sqrt((double)(truePositive + falsePositive) * (truePositive + falseNegative) * (trueNegative + falsePositive) * (trueNegative + falseNegative));
			float ri = (float)(truePositive + trueNegative) / (float)(truePositive + falsePositive + trueNegative + falseNegative);
			float ba = (0.5f * truePositive) / (float)(truePositive + falseNegative) + (0.5f * trueNegative) / (float)(trueNegative + falsePositive);
			
			String[] out = new String[] {
					Integer.toString(numberedCovered),
					Integer.toString(numberCoveredAsBP),
					Integer.toString(numberClusters),
					Integer.toString(numberNonSingletonClusters),
					Integer.toString(numCoveredSingleton),
					Integer.toString(numCoveredNonSingleton),
					Integer.toString(truePositive),
					Integer.toString(falsePositive),
					Integer.toString(trueNegative),
					Integer.toString(falseNegative),
					Float.toString(tpr),
					Float.toString(fpr),
					Float.toString(f1),
					Double.toString(mcc),
					Float.toString(ri),
					Float.toString(ba)
			};

			// System.out.println(Common.join(out, ","));		
			//System.out.printf("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s", numberedCovered, numberCoveredAsBP, numberClusters)
			
			System.out.println("CountIds:" + options.input + 
					",tp=" + truePositive + 
					",fp=" + falsePositive + 
					",tn=" + trueNegative + 
					",fn=" + falseNegative + 
					",tpr=" + tpr + 
					",fpr=" + fpr + 
					",f1=" + f1 +
					",mcc=" + mcc +
					",ri=" + ri +
					",ba=" + ba + 
					",numberClusters=" + numberClusters);
			
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}