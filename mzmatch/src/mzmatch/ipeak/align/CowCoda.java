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



package mzmatch.ipeak.align;


//java
import java.io.*;

import java.util.*;
import java.util.zip.*;

// jfreechart
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;

// libraries
import cmdline.*;

// peakml
import peakml.*;
import peakml.graphics.*;

import peakml.io.*;
import peakml.io.peakml.*;

import peakml.math.*;
import peakml.math.function.*;

// mzmatch
import mzmatch.util.*;






public class CowCoda
{
	// implementation
	/**
	 * 
	 * @param peak
	 * @return
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static double maxRT(IPeak peak) throws RuntimeException
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			double maxrt = Double.MIN_VALUE;
			for (IPeak p : peakset)
				maxrt = Math.max(maxrt, maxRT(p));
			return maxrt;
		}
		else if (peak.getClass().equals(MassChromatogram.class))
			return ((MassChromatogram<Peak>) peak).getMaxRetentionTime();
		throw new RuntimeException("Only type MassChromatogram or PeakSet are supported");
	}
	
	/**
	 * 
	 * @param peak
	 * @return
	 * @throws RuntimeException
	 */
	@SuppressWarnings("unchecked")
	public static double codaDW(IPeak peak) throws RuntimeException
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			double codadw = Double.MIN_VALUE;
			for (IPeak p : peakset)
				codadw = Math.max(codadw, codaDW(p));
			return codadw;
		}
		else if (peak.getClass().equals(MassChromatogram.class))
			return ((MassChromatogram<Peak>) peak).codaDW();
		throw new RuntimeException("Only type MassChromatogram or PeakSet are supported");
	}
	
	/**
	 * 
	 * @param peak
	 * @param function
	 */
	@SuppressWarnings("unchecked")
	public static void align(IPeak peak, Function function)
	{
		if (peak.getClass().equals(IPeakSet.class))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				align(p, function);
		}
		else if (peak.getClass().equals(MassChromatogram.class))
		{
			MassChromatogram<Peak> masschromatogram = (MassChromatogram<Peak>) peak;
			for (Peak p : masschromatogram.getPeaks())
			{
				double rt = p.getRetentionTime() - function.getY(p.getRetentionTime());
				if (rt<0)
					rt = 0;
				if (p.getScanID() == p.getRetentionTime())
					p.setScanID((int) rt);
				p.setRetentionTime(rt);
			}
		}
	}
	
	/**
	 * Calculates the offset in retention time, which best aligns the two given mass chromatograms.
	 * The best alignment is defined as the shift in retention time, within the maximum retention time
	 * shift either way, needed to achieve the best correlation.
	 * 
	 * @param peak1			The first mass chromatogram.
	 * @param peak2			The second mass chromatogram.
	 * @param maxrt			The maximum retention time shift in seconds allowed.
	 * @return				The best shift in retention time.
	 */
	public static double bestOffSet(IPeak peak1, IPeak peak2, double maxrt)
	{
		Signal signal1 = peak1.getSignal();
		
		// we're aligning peak2 towards peaks
		double bestoffset = 0;
		double bestcorrelation = -1;
		
		double offset = peak1.getRetentionTime() - peak2.getRetentionTime();
		for (int i=-30; i<30; ++i)
		{
			Signal signal2 = peak2.getSignal();
			for (int j=0; j<signal2.getSize(); ++j)
				signal2.getX()[j] += (offset + i);
			
			double correlation = signal1.pearsonsCorrelation(signal2)[Statistical.PEARSON_CORRELATION];
			if (correlation > bestcorrelation)
			{
				bestoffset = (offset+i);
				bestcorrelation = correlation;
			}
		}
		
		return bestoffset;
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "CowCoda";
	@OptionsClass(name=application, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"Performs retention time alignment on the given set of peaks. The tool uses the CowCoDA " +
		"algorithm, which looks for strong peaks in the set based on the CoDA algorithm. This " +
		"particular approach uses the Durbin-Watson statistic for finding the best peaks. The " +
		"best scoring peaks are then used to match over the different measurements and calculate " +
		"the retention time shift. This data is consequently used to fit a polynomial function " +
		"with the given order, which is used to align the data.",
		example=
		"Windows batch-file:\n" +
		"SET JAVA=java -cp mzmatch.jar -da -dsa -Xmn1g -Xms1425m -Xmx1425m -Xss128k -XX:+UseParallelGC -XX:ParallelGCThreads=10\n" +
		"\n" +
		"REM extract all the mass chromatograms\n" +
		"%JAVA% mzmatch.ipeak.align.CowCoda -v -i peaks/*.peakml -o peaks_aligned/ -ppm 3 -order 5 -codadw 0.85\n",
		references=
		"Christin C, Smilde AK, Hoefsloot HC, Suits F, Bischoff R, Horvatovich PL. Optimized time alignment algorithm for LC-MS data: correlation optimized warping using component detection algorithm-selected mass chromatograms. 2008."
	)
	public static class Options
	{
		@Option(name="i", param="filenames", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the input files, fow which the only allowed format is PeakML. Either a list " +
			"of files (comma-separated) or a wild-card can be entered here.")
		public Vector<String> input = new Vector<String>();
		@Option(name="o", param="directory", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the output directory, where the retention time aligned files are written.")
		public String output = null;
		
		@Option(name="ppm", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The precision of the data in parts-per-million. This accuracy value is used for " +
			"matching the peaks from all of the measurement.")
		public double ppm = -1;
		@Option(name="codadw", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The minimum mass chromatogram quality value a mass chromatogram needs have in order to " +
			"qualify for the alignment process. This value is [0..1], where a higher value " +
			"is better. The standard value is set to 0.8, which appears to give good results.")
		public double codadw = 0.8;
		@Option(name="order", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The order of the polynomial fitted on the found deviations of the retention times. The " +
			"higher this order is, the more flexible the polynomial will be, but less likely to " +
			"give good results in low population areas.")
		public int order = -1;
		@Option(name="maxrt", param="double", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"The maximum retention time shift allowed in seconds.")
		public double maxrt = -1;

		@Option(name="image", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for a graph with all the used peaks and their deviation in seconds. The fitted " +
			"polynomials are also displayed.")
		public String image = null;
		@Option(name="selection", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Option for the selection of peaks used to calculate the shifts in retention times.")
		public String selection = null;

		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	@SuppressWarnings("unchecked")
	public static void main(String args[])
	{
		final String lbl_mcq = "mcq";
		
		try
		{
			Tool.init();
			
			// parse the commandline options
			final Options options = new Options();
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
			int filetype = JFreeChartTools.PDF;
			{
				if (options.ppm == -1)
				{
					System.err.println("[ERROR]: the ppm-value needs to be set.");
					System.exit(0);
				}
				if (options.order == -1)
				{
					System.err.println("[ERROR]: the order for the polynomial fit needs to be set.");
					System.exit(0);
				}
				if (options.maxrt == -1)
				{
					System.err.println("[ERROR]: the maximum retention time shift is not set.");
					System.exit(0);
				}
				
				if (options.image != null)
				{
					String extension = options.image.substring(options.image.lastIndexOf('.')+1);
					if (extension.toLowerCase().equals("png"))
						filetype = JFreeChartTools.PNG;
					else if (extension.toLowerCase().equals("pdf"))
						filetype = JFreeChartTools.PDF;
					else
					{
						System.err.println("[ERROR]: file extension of the image file needs to be either PDF or PNG.");
						System.exit(0);
					}
				}
				
				// if the output directories do not exist, create them
				if (options.output != null)
					Tool.createFilePath(options.output, true);
				if (options.image != null)
					Tool.createFilePath(options.image, true);
				if (options.selection != null)
					Tool.createFilePath(options.selection, true);
			}
			
			
			// load the data
			if (options.verbose)
				System.out.println("Loading the data");
			double maxrt = 0;
			Vector<ParseResult> data = new Vector<ParseResult>();
			Vector<IPeakSet<IPeak>> matchdata = new Vector<IPeakSet<IPeak>>();
			for (String file : options.input)
			{
				System.out.println("- " + new File(file).getName());
				
				// load the mass chromatogram data
				ParseResult result = PeakMLParser.parse(new FileInputStream(file), true);
				data.add(result);
				
				// select the best mass chromatograms
				Vector<IPeak> selection = new Vector<IPeak>();
				for (IPeak peak : (IPeakSet<IPeak>) result.measurement)
				{
					maxrt = Math.max(maxrt, maxRT(peak));
					
					double mcq = codaDW(peak);
					peak.addAnnotation(lbl_mcq, Double.toString(mcq), Annotation.ValueType.DOUBLE);
					if (mcq >= options.codadw)
						selection.add(peak);
				}
				
				// keep track of the selected mass chromatograms
				int id = options.input.indexOf(file);
				IPeakSet<IPeak> peakset = new IPeakSet<IPeak>(selection);
				peakset.setMeasurementID(id);
				for (IPeak mc : peakset)
					mc.setMeasurementID(id);
				matchdata.add(peakset);
			}
			
			// match the selection together
			if (options.verbose)
				System.out.println("Matching the data");
			Vector<IPeakSet<IPeak>> matches = IPeak.match((Vector) matchdata, options.ppm, new IPeak.MatchCompare<IPeak>() {
				public double distance(IPeak peak1, IPeak peak2) {
					double diff = Math.abs(peak1.getRetentionTime() - peak2.getRetentionTime());
					if (diff > options.maxrt)
						return -1;
					
					Signal signal1 = new Signal(peak1.getSignal());
					signal1.normalize();
					Signal signal2 = new Signal(peak2.getSignal());
					signal2.normalize();
					
					double offset = bestOffSet(peak1, peak2, options.maxrt);
					for (int i=0; i<signal2.getSize(); ++i)
						signal2.getX()[i] += offset;
					
					double correlation = signal2.pearsonsCorrelation(signal1)[Statistical.PEARSON_CORRELATION];
					if (correlation < 0.5)
						return -1;
					
					// the match-function optimizes toward 0 (it's a distance)
					return 1 - correlation;
				}
			});

			// filter out all incomplete sets
			Vector<IPeakSet<IPeak>> valids = new Vector<IPeakSet<IPeak>>();
			for (IPeakSet<IPeak> set : matches)
			{
				if (set.size() < options.input.size())
					continue;
				valids.add((IPeakSet) set);
			}
			
			// calculate the alignment factors
			if (options.verbose)
				System.out.println("Calculating the alignment factors");
			double medians[] = new double[valids.size()+2];
			DataFrame.Double dataframe = new DataFrame.Double(valids.size()+2, options.input.size());
			
			medians[0] = 0;
			medians[medians.length-1] = maxrt;
			for (int i=0; i<options.input.size(); ++i)
			{
				dataframe.set(0, i, 0.1);
				dataframe.set(dataframe.getNrRows()-1, i, 0);
			}
			
			for (int matchid=0; matchid<valids.size(); ++matchid)
			{
				IPeakSet<IPeak> match = valids.get(matchid);
				
				// find the most central
				double offsets[][] = new double[match.size()][match.size()];
				for (int i=0; i<match.size(); ++i)
					for (int j=i+1; j<match.size(); ++j)
					{
						offsets[i][j] = bestOffSet(match.get(i), match.get(j), options.maxrt);
						offsets[j][i] = -offsets[i][j];
					}
				
				int besti = 0;
				double bestabssum = Double.MAX_VALUE;
				for (int i=0; i<match.size(); ++i)
				{
					double abssum = 0;
					for (int j=0; j<match.size(); ++j)
						abssum += Math.abs(offsets[i][j]);
					if (abssum < bestabssum)
					{
						besti = i;
						bestabssum = abssum;
					}
				}
				
				for (int i=0; i<match.size(); ++i)
					dataframe.set(matchid+1, match.get(i).getMeasurementID(), (i==besti ? 0 : offsets[i][besti]));
				
				medians[matchid+1] = match.get(besti).getRetentionTime();
				dataframe.setRowName(matchid, Double.toString(match.get(besti).getRetentionTime()));
			}
			double minmedian = Statistical.min(medians);
			double maxmedian = Statistical.max(medians);
			
			// calculate for each profile the correction function
			PolynomialFunction functions[] = new PolynomialFunction[valids.size()];
			for (int i=0; i<options.input.size(); ++i)
				functions[i] = PolynomialFunction.fit(options.order, medians, dataframe.getCol(i));
			
			// make a nice plot out of the whole thing
			if (options.verbose)
				System.out.println("Writing results");
			if (options.image != null)
			{
				org.jfree.data.xy.XYSeriesCollection dataset = new org.jfree.data.xy.XYSeriesCollection();
				JFreeChart linechart = ChartFactory.createXYLineChart(
						null, "Retention Time (seconds)", "offset", dataset, PlotOrientation.VERTICAL,
						true, // legend
						false, // tooltips
						false  // urls
					);
				
				// setup the colorkey
				Colormap colormap = new Colormap(Colormap.EXCEL);
				
				// get the structure behind the graph
				XYPlot plot = (XYPlot) linechart.getPlot();
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
				
				// setup the plot area
				linechart.setBackgroundPaint(java.awt.Color.WHITE);
				linechart.setBorderVisible(false);
				linechart.setAntiAlias(true);
				
				plot.setBackgroundPaint(java.awt.Color.WHITE);
				plot.setDomainGridlinesVisible(true);
				plot.setRangeGridlinesVisible(true);
				
				// create the datasets
				for (int i=0; i<options.input.size(); ++i)
				{
					org.jfree.data.xy.XYSeries series = new org.jfree.data.xy.XYSeries(dataframe.getColName(i));
					org.jfree.data.xy.XYSeries function = new org.jfree.data.xy.XYSeries(dataframe.getColName(i) + "-function");
					dataset.addSeries(series);
					dataset.addSeries(function);
					
					renderer.setSeriesPaint(dataset.getSeriesCount()-1, new java.awt.Color(colormap.getColor(i)));
					renderer.setSeriesPaint(dataset.getSeriesCount()-2, new java.awt.Color(colormap.getColor(i)));
					
					renderer.setSeriesLinesVisible(dataset.getSeriesCount()-2, false);
					renderer.setSeriesShapesVisible(dataset.getSeriesCount()-2, true);
					
					// add the data-points
					for (int j=0; j<valids.size(); ++j)
						series.add(medians[j], dataframe.get(j, i));
					for (double x=minmedian; x<maxmedian; ++x)
						function.add(x, functions[i].getY(x));
				}
				
				dataset.removeAllSeries();
				for (int i=0; i<options.input.size(); ++i)
				{
					Function function = functions[i];
					
					org.jfree.data.xy.XYSeries series = new org.jfree.data.xy.XYSeries(dataframe.getColName(i));
					dataset.addSeries(series);
					
					renderer.setSeriesPaint(i, new java.awt.Color(colormap.getColor(i)));
					renderer.setSeriesLinesVisible(i, false);
					renderer.setSeriesShapesVisible(i, true);
					
					// add the data-points
					for (int j=0; j<valids.size(); ++j)
						series.add(medians[j], dataframe.get(j, i)-function.getY(medians[j]));
				}
				
				JFreeChartTools.writeAs(filetype, new FileOutputStream(options.image), linechart, 800, 500);
			}
			
			// save the selected
			if (options.selection != null)
			{
				Header header = new Header();
				
				// set the number of peaks to be stored
				header.setNrPeaks(valids.size());
				
				// create a set for the measurements
				SetInfo set = new SetInfo("", SetInfo.SET);
				header.addSetInfo(set);
				
				// create the measurement infos
				for (int i=0; i<options.input.size(); ++i)
				{
					String file = options.input.get(i);
					
					// create the measurement info
					MeasurementInfo measurement = new MeasurementInfo(i, data.get(i).header.getMeasurementInfo(0));
					measurement.addFileInfo(new FileInfo(file, file));
					
					header.addMeasurementInfo(measurement);
					
					// add the file to the set
					set.addChild(new SetInfo(file, SetInfo.SET, i));
				}
				
				// write the data
				PeakMLWriter.write(
						header,
						(Vector) valids,
						null,
						new GZIPOutputStream(new FileOutputStream(options.selection)),
						null
					);
			}
			
			// correct the values with the found function and save them
			for (int i=0; i<options.input.size(); ++i)
			{
				Function function = functions[i];
				ParseResult result = data.get(i);
				
				IPeakSet<MassChromatogram<Peak>> peakset = (IPeakSet<MassChromatogram<Peak>>) result.measurement;
				for (IPeak peak : peakset)
					align(peak, function);
				
				File filename = new File(options.input.get(i));
				String name = filename.getName();
				
				PeakMLWriter.write(
						result.header,
						(Vector) peakset.getPeaks(),
						null,
						new GZIPOutputStream(new FileOutputStream(options.output + "/" + name)),
						null
					);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
