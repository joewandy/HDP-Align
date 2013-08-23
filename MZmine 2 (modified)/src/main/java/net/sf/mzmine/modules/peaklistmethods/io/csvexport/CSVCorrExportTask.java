/*
 * Copyright 2006-2012 The MZmine 2 Development Team
 * 
 * This file is part of MZmine 2.
 * 
 * MZmine 2 is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * MZmine 2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * MZmine 2; if not, write to the Free Software Foundation, Inc., 51 Franklin St,
 * Fifth Floor, Boston, MA 02110-1301 USA
 */

package net.sf.mzmine.modules.peaklistmethods.io.csvexport;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import net.sf.mzmine.data.ChromatographicPeak;
import net.sf.mzmine.data.DataPoint;
import net.sf.mzmine.data.PeakList;
import net.sf.mzmine.data.PeakListRow;
import net.sf.mzmine.data.PeakStatus;
import net.sf.mzmine.data.RawDataFile;
import net.sf.mzmine.data.Scan;
import net.sf.mzmine.parameters.ParameterSet;
import net.sf.mzmine.taskcontrol.AbstractTask;
import net.sf.mzmine.taskcontrol.TaskStatus;
import peakml.math.Signal;
import peakml.math.Statistical;

class CSVCorrExportTask extends AbstractTask {

	private PeakList peakList;
	private int processedRows = 0, totalRows = 0;

	// parameter values
	private File outputDir;
	private String fieldSeparator;
	private Double rtWindow;

	CSVCorrExportTask(ParameterSet parameters) {

		this.peakList = parameters.getParameter(
				CSVCorrExportParameters.peakList).getValue()[0];
		this.outputDir = parameters.getParameter(
				CSVCorrExportParameters.outputDir).getValue();
		this.fieldSeparator = parameters.getParameter(
				CSVCorrExportParameters.fieldSeparator).getValue();
		this.rtWindow = parameters.getParameter(
				CSVCorrExportParameters.rtWindow).getValue();
		
	}

	public double getFinishedPercentage() {
		if (totalRows == 0) {
			return 0;
		}
		return (double) processedRows / (double) totalRows;
	}

	public String getTaskDescription() {
		return "Exporting peak list " + peakList + " to " + outputDir;
	}

	public void run() {

		setStatus(TaskStatus.PROCESSING);
		RawDataFile rawDataFiles[] = peakList.getRawDataFiles();
		totalRows += peakList.getNumberOfRows() * peakList.getNumberOfRows() * rawDataFiles.length;
		for (RawDataFile dataFile : rawDataFiles) {
			
			// Cancel task ?
			if (isCanceled()) {
				return;
			}			
			exportPeakList(peakList, dataFile);
			
		}

		if (getStatus() == TaskStatus.PROCESSING)
			setStatus(TaskStatus.FINISHED);

	}

	private void exportPeakList(PeakList peakList, RawDataFile dataFile) {

		try {

			// Open file
			File outputFile = new File(outputDir, dataFile.getName() + ".csv");
			PrintWriter out = new PrintWriter(outputFile);
			
			// for the first peak
			PeakListRow[] rows = peakList.getRows();
			for (PeakListRow peakListRow1 : rows) {
				
				ChromatographicPeak p1 = peakListRow1.getPeak(dataFile);
				int i = 0;
				
				// for the second peak
				for (PeakListRow peakListRow2 : rows) {
					ChromatographicPeak p2 = peakListRow2.getPeak(dataFile);

					// print extra info on the first, second and third column
					if (p1 != null && i == 0) {
						out.print(String.format("%.4f", p1.getMZ()) + fieldSeparator);
						out.print(String.format("%.4f", p1.getRT()) + fieldSeparator);
						out.print(String.format("%.4f", p1.getHeight()) + fieldSeparator);
					}
					
					// if have peaks
					if (p1 != null && p2 != null) {

						// check in the same retention time range
						double rt1 = p1.getRT();
						double rt2 = p2.getRT();
						double top = rt1 + this.rtWindow;
						double bottom = rt1 - this.rtWindow;
						float corr = 0;
						// if (rt2 >= bottom && rt2 <= top) {
						if (Double.compare(rt2, bottom) >= 0 && Double.compare(rt2, top) <= 0) {
							corr = computeCorrelation(p1, p2, dataFile);							
							out.print(String.format("%.4f", corr) + fieldSeparator);
						} else {
							out.print("NaN" + fieldSeparator);							
						}
						
					} else {
						// out.print(PeakStatus.UNKNOWN + fieldSeparator);
						out.print("-99" + fieldSeparator);
					}
					i++;
					processedRows++;
				}				
				out.println();
				
			}			
			out.close();

		} catch (IOException e) {
			setStatus(TaskStatus.ERROR);
			errorMessage = "Could not write output: " + e.getMessage();
			return;
		}

	}

	public Object[] getCreatedObjects() {
		return null;
	}
	
	private float computeCorrelation(ChromatographicPeak p1, ChromatographicPeak p2, RawDataFile dataFile) {		
		Signal s1 = getSignal(p1, dataFile);
		Signal s2 = getSignal(p2, dataFile);
		float corr = (float) s1.pearsonsCorrelation(s2)[Statistical.PEARSON_CORRELATION];
		return corr;
	}
	
	private Signal getSignal(ChromatographicPeak p, RawDataFile dataFile) {

		int[] scanNumbers = p.getScanNumbers();
		double[] rtValues = new double[scanNumbers.length];
		double[] intensityValues = new double[scanNumbers.length];
		
		for (int i = 0; i < scanNumbers.length; i++) {

			final Scan scan = dataFile.getScan(scanNumbers[i]);			
			final DataPoint dp = p.getDataPoint(scanNumbers[i]);
			if (dp == null) {
				continue;
			}
			rtValues[i] = scan.getRetentionTime();
			intensityValues[i] = dp.getIntensity();
			
		}
	
		return new Signal(rtValues, intensityValues);

	}
	
}