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



package mzmatch.ipeak.normalisation;


// java
import java.io.*;
import java.awt.*;

import java.util.*;
import java.util.zip.*;

// jfreechart
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;

import org.jfree.data.category.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.math.*;
import peakml.graphics.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.util.*;





@SuppressWarnings("unchecked")
public class VanDeSompele
{
	// implementation
	/**
	 * Counts all non IPeakSet instances hidden in the given IPeak instance.
	 * 
	 * @param peak		The IPeak instance to evaluate.
	 * @return			Number of non IPeakSet instances.
	 */
	public static int count(IPeak peak)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			int cnt = 0;
			for (IPeak p : (IPeakSet<IPeak>) peak)
				cnt += count(p);
			return cnt;
		}
		
		return 1;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "VanDeSompele";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Applies a basic normalisation scheme to the PeakML file resulting from the combine process. " +
		"The method looks for house-hold metabolites not expected to change. These metabolites are " +
		"defined as the metabolites showing the least changes compared to the detections in all " +
		"of the other measurements. This is reflected by a stability score (stored as an annotation " +
		"for each of the entries labeled 'stability factor'). These stability scores are calculated " +
		"from all the metabolites that could be identified with the given database, within the given " +
		"ppm-range. The identified metabolites need to have been identified in all of the used " +
		"measurements." +
		"\n\n" +
		"From the top 10% (at least 10) most stable, identifed metabolites the normalisation factors " +
		"are calculated and applied to the rest of the data.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.normalisaton.VanDeSompele -v -i combined.peakml -o normalized.peakml -selection selection.peakml -ppm 3 \n",
		references=
		"1. Vandesompele J, De Preter K, Pattyn F, Poppe B, Van Roy N, De Paepe A, Speleman F. Accurate normalization of real-time quantitative RT-PCR data by geometric averaging of multiple internal control genes. Genome Biol. 2002."
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input file. The only allowed format is PeakML and when it is " +
			"not set the input is read from standard in. The tool expects a combination " +
			"of peaks from different sets and will exit when this is not encountered.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the ouput file. The file is written in the PeakML file format and " +
			"contains all the peaks with normalized intensities. When this option is not " +
			"set the output is written to the standard out. Be sure to unset the verbose " +
			"option when setting up a pipeline reading and writing from the standard in- " +
			"and outputs.")
		public String output = null;
		@Option(name="selection", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the file where the un-normalized selection of peaks is written.")
		public String selection = null;
		@Option(name="selection_normalized", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the file where the normalizaed selection of peaks is written.")
		public String selection_normalized = null;
		@Option(name="img", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the file where a graph of the normalization factors is written. This " +
			"file is in PDF format.")
		public String img = null;
		@Option(name="factors", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the file where the normalization factors are written.")
		public String factors = null;
		
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The accuracy of the measurement in parts-per-milion. This value is used for " +
			"matching the masses to those found in the supplied databases. This value is " +
			"obligitory.")
		public double ppm = -1;
		@Option(name="database", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the molecule databases to match the contents of the input file to. " +
			"This file should adhere to the compound-xml format.")
		public String database = null;
		 
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
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
			
			// check the command-line parameters
			{
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, true);
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading data");
			ParseResult result = PeakMLParser.parse(new FileInputStream(options.input), true);
			
			Header header = result.header;
			IPeakSet<IPeakSet<? extends IPeak>> peaksets = (IPeakSet<IPeakSet<? extends IPeak>>) result.measurement;
			
			int nrmeasurements = header.getNrMeasurementInfos();
			
			// remove the stability factor annotation
			for (IPeak peak : peaksets)
				peak.removeAnnotation("stability factor");
			
			// load the database
			if (options.verbose)
				System.out.println("Loading the molecule database");
			HashMap<String,Molecule> database = MoleculeIO.parseXml(new FileInputStream(options.database));
			
			// filter the set to include only identifiable metabolites
			if (options.verbose)
				System.out.println("Creating selection");
			Vector<IPeakSet<? extends IPeak>> selection = new Vector<IPeakSet<? extends IPeak>>();
			for (Molecule molecule : database.values())
			{
				double mass = molecule.getMass(Mass.MONOISOTOPIC);
				double delta = PeriodicTable.PPM(mass, options.ppm);
				
				// get the most intense peak containing all the measurements
				Vector<IPeakSet<? extends IPeak>> neighbourhoud = peaksets.getPeaksInMassRange(mass-delta, mass+delta);
				Collections.sort(neighbourhoud, IPeak.sort_intensity_descending);
				for (IPeakSet<? extends IPeak> neighbour : neighbourhoud)
					if (count(neighbour) == nrmeasurements)
					{
						selection.add(neighbour);
						break;
					}
			}
			
			// calculate the stability factor for each peak in the selection
			if (options.verbose)
				System.out.println("Calculating stability factors");
			for (int peakid1=0; peakid1<selection.size(); ++peakid1)
			{
				double stddeviations[] = new double[selection.size()];
				
				IPeakSet<? extends IPeak> peakset1 = selection.get(peakid1);
				for (int peakid2=0; peakid2<selection.size(); ++peakid2)
				{
					IPeakSet<? extends IPeak> peakset2 = selection.get(peakid2);
					
					double values[] = new double[nrmeasurements];
					for (int measurementid=0; measurementid<nrmeasurements; ++measurementid)
					{
						int measurementid1 = peakset1.get(measurementid).getMeasurementID();
						int setid1 = header.indexOfSetInfo(header.getSetInfoForMeasurementID(measurementid1));
						int measurementid2 = peakset2.get(measurementid).getMeasurementID();
						int setid2 = header.indexOfSetInfo(header.getSetInfoForMeasurementID(measurementid2));
						if (setid1!=setid2 || measurementid1!=measurementid2)
							System.err.println("[WARNING]: differing setid or spectrumid for comparison");
						
						values[measurementid] = Math.log(peakset1.get(measurementid).getIntensity()/peakset2.get(measurementid).getIntensity()) / Math.log(2);
					}
					stddeviations[peakid2] = Statistical.stddev(values);
				}
				
				peakset1.addAnnotation("stability factor", Statistical.mean(stddeviations));
			}
			
			// sort on the stability factor
			Collections.sort(selection, new IPeak.AnnotationAscending("stability factor"));
			
			// take the top 10% and calculate the geometric mean
			if (options.verbose)
				System.out.println("Calculating normalisation factors");
			int nrselected = (int) (0.1*selection.size());
			if (nrselected < 10)
				nrselected = (10<selection.size() ? 10 : selection.size());
			double normalization_factors[] = new double[nrmeasurements];
			for (int measurementid=0; measurementid<nrmeasurements; ++measurementid)
			{
				double values[] = new double[nrselected];
				for (int i=0; i<nrselected; ++i)
				{
					IPeak peak = selection.get(i).get(measurementid);
					values[i] = peak.getIntensity();
				}
				normalization_factors[measurementid] = Statistical.geomean(values);
			}
			
			// scale the found normalization factors
			double maxnf = Statistical.max(normalization_factors);
			for (int sampleid=0; sampleid<nrmeasurements; ++sampleid)
				normalization_factors[sampleid] /= maxnf;
			
			// write the selection if needed
			if (options.selection != null)
			{
				if (options.verbose)
					System.out.println("Writing original selection data");
				
				PeakMLWriter.write(
						result.header, selection, null,
						new GZIPOutputStream(new FileOutputStream(options.selection)), null
					);
			}
			
			// normalize all the peaks
			if (options.verbose)
				System.out.println("Normalizing all the entries");
			for (IPeakSet<? extends IPeak> peakset : peaksets)
			{
				for (int measurementid=0; measurementid<nrmeasurements; ++measurementid)
				{
					// TODO why did I do this again ?
					int id = 0;
					int setid = 0;
					int spectrumid = 0;
					for (int i=0; i<header.getNrSetInfos(); ++i)
					{
						SetInfo set = header.getSetInfos().get(i);
						
						if (id+set.getNrMeasurementIDs() > measurementid)
						{
							setid = i;
							spectrumid = measurementid - id;
							break;
						}
						else
							id += set.getNrMeasurementIDs();
					}
					
					MassChromatogram<Peak> masschromatogram = null;
					for (IPeak p : peakset)
					{
						int mymeasurementid = p.getMeasurementID();
						int mysetid = header.indexOfSetInfo(header.getSetInfoForMeasurementID(mymeasurementid));
						if (mysetid==setid && mymeasurementid==spectrumid)
						{
							masschromatogram = (MassChromatogram<Peak>) p;
							break;
						}
					}
					if (masschromatogram == null)
						continue;
					
					for (IPeak peak : masschromatogram.getPeaks())
						peak.setIntensity(peak.getIntensity() / normalization_factors[measurementid]);
				}
			}
			
			// write the selection if needed
			if (options.selection_normalized != null)
			{
				if (options.verbose)
					System.out.println("Writing the normalized selection data");
				
				PeakMLWriter.write(
						result.header, selection, null,
						new GZIPOutputStream(new FileOutputStream(options.selection_normalized)),
						null
					);
			}
			
			// write the factors if needed
			if (options.factors != null)
			{
				if (options.verbose)
					System.out.println("Writing the normalization factors");
				
				PrintStream out = new PrintStream(options.factors);
				for (int measurementid=0; measurementid<nrmeasurements; ++measurementid)
					out.println(header.getMeasurementInfo(measurementid).getLabel() + "\t" + normalization_factors[measurementid]);
			}
			
			// write the plot if needed
			if (options.img != null)
			{
				if (options.verbose)
					System.out.println("Writing the graph");
				
				DefaultCategoryDataset dataset = new DefaultCategoryDataset();
				JFreeChart linechart = ChartFactory.createLineChart(
						null, "measurement", "normalization factor", dataset, PlotOrientation.VERTICAL,
						false, // legend
						false, // tooltips
						false  // urls
					);
				
				CategoryPlot plot = (CategoryPlot) linechart.getPlot();
				CategoryAxis axis = (CategoryAxis) plot.getDomainAxis();
				axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
				LineAndShapeRenderer renderer = (LineAndShapeRenderer) plot.getRenderer();
				
				renderer.setSeriesShapesFilled(0, true);
				renderer.setSeriesShapesVisible(0, true);
				
				linechart.setBackgroundPaint(Color.WHITE);
				linechart.setBorderVisible(false);
				linechart.setAntiAlias(true);
				
				plot.setBackgroundPaint(Color.WHITE);
				plot.setDomainGridlinesVisible(true);
				plot.setRangeGridlinesVisible(true);
				
				// create the datasets
				for (int measurementid=0; measurementid<nrmeasurements; ++measurementid)
					dataset.addValue(normalization_factors[measurementid], "", header.getMeasurementInfo(measurementid).getLabel());
				JFreeChartTools.writeAsPDF(new FileOutputStream(options.img), linechart, 800, 500);
			}
			
			// write the normalized values
			if (options.verbose)
				System.out.println("Writing the normalized data");
			PeakMLWriter.write(
					result.header, peaksets.getPeaks(), null,
					new GZIPOutputStream(new FileOutputStream(options.output)), null
				);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
