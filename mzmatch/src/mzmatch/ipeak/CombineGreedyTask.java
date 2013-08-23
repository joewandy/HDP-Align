package mzmatch.ipeak;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import mzmatch.ipeak.Combine.Options;
import mzmatch.ipeak.sort.CorrelationMeasure;
import peakml.Annotation;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLWriter;

public class CombineGreedyTask extends CombineBaseTask implements CombineTask {

	public void process(final Options options, Header header,
			Vector<IPeakSet<IPeak>> peaksets, ParseResult[] results,
			final Random random, CorrelationMeasure measure, float rangeMin,
			float rangeMax, int totalPeaks, OutputStream output) throws IOException, FileNotFoundException {

		// greedily match peaks across files
		List<IPeakSet<IPeak>> matches = IPeak.match(peaksets, options.ppm, new PeakMatchCompare<IPeak>(options.rtwindow));						
		
		// unpack potential sub-peaksets
		List<IPeakSet<IPeak>> data = new ArrayList<IPeakSet<IPeak>>();
		int alignmentClusterIdx = 0;
		
		int peaksInThreeClusters = 0;
		int peaksInTwoClusters = 0;

		List<double[]> intensesAll = new ArrayList<double[]>();
		
		for (IPeakSet<IPeak> match : matches) {
								
			// debugging only
			IPeakSet<IPeak> alignmentCluster = new IPeakSet<IPeak>(unpack(match));
			
			int size = alignmentCluster.size();
			System.out.println("Cluster #" + alignmentClusterIdx + " has " + size + " peaks");
			double intenses[] = new double[peaksets.size()];
			for (IPeak p : alignmentCluster) {
			
				if (size == 2) {
					peaksInTwoClusters++;
				} else if (size == 3) {
					peaksInThreeClusters++;
				}
				
				int sourcePeakset = p.getAnnotation(Annotation.sourcePeakset).getValueAsInteger();
				Annotation relationIdAnnotation = p.getAnnotation(Annotation.relationid);
				int groupId = 0;
				if (relationIdAnnotation != null) {
					groupId = relationIdAnnotation.getValueAsInteger();
				}
				if (size == 2) {
					intenses[sourcePeakset] = p.getIntensity();							
				}
				
			}						
			
			alignmentClusterIdx++;
			data.add(alignmentCluster);
			if (size == 2) {
				intensesAll.add(intenses);
			}
			
		}

		evaluateResult(peaksets.size(), totalPeaks, peaksInThreeClusters,
				peaksInTwoClusters, intensesAll);
		
		// write the result
		if (options.verbose)
			System.out.println("Writing the results");
		PeakMLWriter.write(header, data, null, new GZIPOutputStream(output), null);
		
	}
	
	
}
