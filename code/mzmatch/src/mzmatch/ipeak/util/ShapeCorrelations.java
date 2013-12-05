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



package mzmatch.ipeak.util;


// java
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import mzmatch.ipeak.sort.Data;
import mzmatch.util.Tool;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.Peak;
import peakml.io.Header;
import peakml.io.MeasurementInfo;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;
import peakml.math.Signal;
import peakml.math.Statistical;
import cmdline.CmdLineParser;
import cmdline.Option;
import cmdline.OptionsClass;
import domsax.XmlParserException;






@SuppressWarnings("unchecked")
public class ShapeCorrelations
{
	
	public static final double[] getIntensityCourse(IPeak peak, Header header)
	{
		if (peak.getClass().equals(IPeakSet.class))
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
		
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"Optional filename where the output is written. If this is not set the output is " +
			"written to the standard output.")
		public String output = null;

		@Option(name="rtwindow", param="double", type=Option.Type.REQUIRED_ARGUMENT, level=Option.Level.USER, usage=
			"The retention time window in seconds, defining the range where to look for matches.")
		public double rtwindow = -1.0d;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, level=Option.Level.SYSTEM, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
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
			
			// check the commandline parameters
			{
				if (options.rtwindow == -1.0d)
				{
					System.err.println("[ERROR]: the rtwindow value needs to be specified.");
					System.exit(1);
				}
			}
			
			
			// 
			if (options.verbose)
				System.out.println("loading data");
			
			doFile(options.input, options.output, options);
			
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
	
	private static void doFile(String inputFilename, String outputFilename, Options options)
			throws FileNotFoundException, IOException, XmlParserException {
		ParseResult result = PeakMLParser.parse(new FileInputStream(inputFilename), true);
		
		final Header header = result.header;
		final IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
		final int numReplicates = header.getNrMeasurementInfos();
		final String basename = options.input.split(".peakml")[0];
		final long rtwindowLong = Math.round(options.rtwindow);
		
		//final SignalsAndRetentionTimes sart = getSignals(header, peaks);
		final Data data = new Data(header, peaks);
		for (int i = 0; i < numReplicates; ++i) {
			//double[][] corrs = calculateCorrelations(sart.signals[i], sart.retentionTimes[i], options.rtwindow);
			//final String correlationOutputFile = basename + "_" + (char)(i + 65) + "_rt" + rtwindowLong + ".dat";
			//outputCorrelations(corrs, correlationOutputFile, sart.ids);
			//final String dataOutputFile = basename + "_" + (char)(i + 65) + ".csv";
			outputData(peaks, header, i, data.ids, outputFilename);
		}
	}
	
	private static IPeak getPeak(IPeakSet<? extends IPeak> peaks, Header header, int replicateNumber) {
		MeasurementInfo measurement = header.getMeasurementInfo(replicateNumber);
		int measurementid = measurement.getID();
		for (IPeak peak : peaks) {
			if (measurementid == peak.getMeasurementID()) {
				return peak;
			}
		}
		return null;
	}
	/*
	public static SignalsAndRetentionTimes getSignals(Header header, IPeakSet<IPeak> peakset) {
		final Vector<IPeak> peaks = peakset.getPeaks();
		final int numReplicates = header.getNrMeasurementInfos();
		final int numPeaksets = header.getNrPeaks();
		
		final Signal[][] allSignals = new Signal[numReplicates][numPeaksets];
		final double[][] retentionTimes = new double[numReplicates][numPeaksets];
		final String[] ids = CommonBak.getIds(peaks).toArray(new String[numPeaksets]);
		
		for (int j = 0; j < numPeaksets; ++j) {
			final IPeak peak1 = peaks.get(j);
			if ( peakset.getContainerClass().equals(MassChromatogram.class) ) {
				MassChromatogram<Peak> mc = (MassChromatogram<Peak>)peak1;
				if ( mc == null ) {
					allSignals[0][j] = null;
				} else {
					allSignals[0][j] = mc.getSignal();
					retentionTimes[0][j] = peak1.getRetentionTime();
				}
			} else {
				for (int i = 0; i < numReplicates; ++i) {
					
					IPeakSet<MassChromatogram<Peak>> peakset1 = (IPeakSet<MassChromatogram<Peak>>) peak1;
					IPeak mc1 = getPeak(peakset1, header, i);

					if ( mc1 == null ) {
						allSignals[i][j] = null;
					} else {
						allSignals[i][j] = mc1.getSignal();
						retentionTimes[i][j] = peak1.getRetentionTime();
					}
				}
			}
		}
		return new SignalsAndRetentionTimes(allSignals, retentionTimes, ids);
	}
	*/
		
	private static double[][] calculateCorrelations(Signal[] signals, double[] retentionTimes, double rtwindow) {
		final int numSignals = signals.length;
		System.err.println("Looking at " + numSignals + " signals");
		final double[][] correlations = new double[numSignals][numSignals];
		
		for (int i = 0; i < numSignals; ++i) {
			final double signal1rt = retentionTimes[i];
			//System.err.println("rttime: " + signal1rt);
			final Signal signal1 = signals[i];
			if ( signal1 == null ) {
				continue;
			}
			for (int j = 0; j < numSignals; ++j) {
				final double signal2rt = retentionTimes[j];
				final Signal signal2 = signals[j];
				if ( signal2 == null ) {
					continue;
				}
				if ( Math.abs(signal1rt - signal2rt) > rtwindow ) {
					continue;
				}
				correlations[i][j] = signal1.pearsonsCorrelation(signal2)[Statistical.PEARSON_CORRELATION];
			}
		}
		for (int i = 0; i < numSignals; ++i) {
		    for (int j = 0; j < numSignals; ++j) {
			if (Double.isNaN(correlations[i][j])) {
			    System.err.println("NaN!");
			    System.err.println(signals[i]);
			    System.err.println(signals[j]);			    
			}
			assert ! Double.isNaN(correlations[i][j]);
		    }
		}
		System.err.println("Calculated " + numSignals + " signals");
		return correlations;
	}
	
	private static void outputCorrelations(double[][] correlations, String filename, String[] ids) throws IOException {
		System.err.println("Outputing correlations to " + filename);
		final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		
		final int size = correlations.length;
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				final double value = correlations[i][j];
				if ( value == 0.0d ) {
					continue;
				}
				String iId = ids[i];
				String jId = ids[j];
				writer.write(iId);
				//writer.write(Integer.toString(i + 1));
				writer.write(' ');
				//writer.write(Integer.toString(j + 1));
				writer.write(jId);
				writer.write(' ');
				writer.write(Double.toString(correlations[i][j]));
				writer.newLine();
			}
		}
		if ( correlations[size - 1][size - 1] == 0.0d ) {
			writer.write(Integer.toString(size));
			writer.write(' ');
			writer.write(Integer.toString(size));
			writer.write(" 0");
			writer.newLine();
		}
		writer.flush();
		writer.close();
	}
	
	private static void outputData(IPeakSet<IPeak> peakset, Header header, int replicateNumber, String[] ids, String filename) throws IOException {
		final Vector<IPeak> peaks = peakset.getPeaks();
		assert peaks.size() == ids.length;
		System.err.println("Outputing data " + filename);
		final BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
		writer.write("\"mass\",\"retention_time\",\"intensity\",\"id\"");
		writer.newLine();
		for (int i = 0; i < peaks.size(); ++i) {
			IPeak peak = null;
			if ( peakset.getContainerClass().equals(MassChromatogram.class) ) {
				peak = peaks.get(i);
			} else {
				IPeakSet<IPeak> ps = (IPeakSet<IPeak>)peaks.get(i);
				peak = getPeak(ps, header, replicateNumber);
			}
			if ( peak != null ) {
				writer.write(Double.toString(peak.getMass()));
				writer.write(",");
				writer.write(Double.toString(peak.getRetentionTime()));
				writer.write(",");
				writer.write(Double.toString(peak.getIntensity()));
				writer.write(",");
				writer.write(ids[i]);
				writer.newLine();
			}
		}
		writer.flush();
		writer.close();
	}
}
