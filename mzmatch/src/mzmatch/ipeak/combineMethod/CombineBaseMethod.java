package mzmatch.ipeak.combineMethod;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import mzmatch.ipeak.CombineOptions;
import mzmatch.ipeak.sort.Clusterer;
import mzmatch.ipeak.sort.CorrelationClusterer;
import mzmatch.ipeak.sort.CorrelationMeasure;
import mzmatch.ipeak.sort.CorrelationParameters;
import mzmatch.ipeak.sort.Data;
import mzmatch.ipeak.sort.IdentifyPeaksets;
import mzmatch.ipeak.sort.PeakLikelihoodScorer;
import mzmatch.ipeak.sort.PeakPosteriorScorer;
import mzmatch.ipeak.sort.RelatedPeaks.PeakComparer;
import mzmatch.ipeak.sort.RetentionTimeClusteringScorer;
import mzmatch.ipeak.sort.SampleHandler;
import mzmatch.ipeak.sort.SimpleClustering;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.WriterProgressListener;
import peakml.io.peakml.PeakMLWriter;

import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentRow;
import com.joewandy.alignmentResearch.objectModel.Feature;

public abstract class CombineBaseMethod implements CombineMethod {

	public void process(final CombineOptions options, Header header,
			Vector<IPeakSet<IPeak>> peaksets, ParseResult[] results,
			final Random random, CorrelationMeasure measure, float rangeMin,
			float rangeMax, int totalPeaks, OutputStream output) throws IOException, FileNotFoundException {

		// do some preprocessing, mostly storing temporary sequence IDs
		for (IPeakSet<IPeak> peaks : peaksets) {
			preprocess(peaks);			
		}
		
		// match peaks across files
		List<IPeakSet<IPeak>> matches = getMatches(peaksets, options);						
		
		// unpack potential sub-peaksets
		List<IPeakSet<IPeak>> data = new ArrayList<IPeakSet<IPeak>>();		
		for (IPeakSet<IPeak> match : matches) {								
			IPeakSet<IPeak> alignmentCluster = new IPeakSet<IPeak>(unpack(match));
			data.add(alignmentCluster);
		}

		// write the result
		if (options.verbose)
			System.out.println("Writing the results");
		PeakMLWriter.write(header, data, null, new GZIPOutputStream(output), null);
		System.out.println("Done !");
		
	}
	
	protected abstract List<IPeakSet<IPeak>> getMatches(List<IPeakSet<IPeak>> peaksets, CombineOptions options);
	
	protected void preprocess(IPeakSet<IPeak> peaks) {

		// the annotation result is used where later ?
		IdentifyPeaksets.identify(peaks);

		// annotate the peaks with an id for the hashing
		int id = 0;
		for (IPeak peak : peaks) {
			peak.setPatternID(id);
			id++;
		}

	}
	
	protected Vector<IPeak> unpack(IPeak peak) {

		Vector<IPeak> peaks = new Vector<IPeak>();
		if (IPeakSet.class.equals(peak.getClass())) {
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			for (IPeak p : peakset)
				peaks.addAll(unpack(p));
		} else
			peaks.add(peak);
		return peaks;

	}
	
	/*
	 * ===================================================
	 * METHODS FOR CALLING EXTERNAL ALIGNMENT ALGORITHMS
	 * ===================================================
	 */
	
	protected List<IPeakSet<IPeak>> mapAlignmentResult(
			List<IPeakSet<IPeak>> peaksets, List<AlignmentFile> dataList,
			List<AlignmentRow> result) {
		
		// map data file to sample
		Map<AlignmentFile, IPeakSet<IPeak>> dataToSampleMap = new HashMap<AlignmentFile, IPeakSet<IPeak>>();
		for (int i = 0; i < dataList.size(); i++) {
			AlignmentFile file = dataList.get(i);
			IPeakSet<IPeak> sample = peaksets.get(i);
			dataToSampleMap.put(file, sample);
		}

		// map alignment row to ipeakset
		List<IPeakSet<IPeak>> matches = new ArrayList<IPeakSet<IPeak>>();
		for (AlignmentRow row : result) {
			List<IPeak> alignedPeaks = mapAlignmentRowToIPeakList(row, dataToSampleMap);
			IPeakSet<IPeak> peakset = new IPeakSet<IPeak>(alignedPeaks);
			matches.add(peakset);
		}
		return matches;

	}

	private List<IPeak> mapAlignmentRowToIPeakList(AlignmentRow row, Map<AlignmentFile, IPeakSet<IPeak>> dataToSampleMap) {
		
		List<IPeak> match = new ArrayList<IPeak>();
		
		for (Feature feature : row.getFeatures()) {
			
			// find out which data file this feature comes from ?
			AlignmentFile file = feature.getData();
			
			// find out which sample it is ?
			IPeakSet<IPeak> sample = dataToSampleMap.get(file);
			
			// retrieve the actual IPeak object, and add to matching result
			int index = feature.getPeakID();
			IPeak peak = sample.get(index);
			match.add(peak);
			
		}
		
		return match;
		
	}

	/*
	 * ===================================================
	 * METHODS FOR GROUPING
	 * TODO: remove them out to another class later ..
	 * ===================================================
	 */
	
	protected List<Map<Integer, List<IPeak>>> groupPeaks(final CombineOptions options,
			Header header, Vector<IPeakSet<IPeak>> peaksets,
			final Random random, CorrelationMeasure measure, float rangeMin,
			float rangeMax, String method) {
		
		List<Map<Integer, List<IPeak>>> clusterToPeaksMap = new ArrayList<Map<Integer, List<IPeak>>>();
		for (int i = 0; i < peaksets.size(); i++) {	

			IPeakSet<IPeak> peaks = peaksets.get(i);
			preprocess(peaks);

			System.out.println("Grouping peaks from sourcePeakSet " + i);

			List<IPeak> basepeaks = null;
			if (CombineMethod.GROUPING_GREEDY.equals(method)) {

				/* 
				 * group peaks in a greedy manner
				 * each peak will have the annotation "relationid" for the group it belongs to
				 */
				basepeaks = greedyGrouping(options, header, measure, peaks);
				
			} else if (CombineMethod.GROUPING_MIXTURE.equals(method)) {
			
				/* 
				 * group peaks by correlations
				 * each peak will have the annotation "relationid" for the group it belongs to
				 */
				basepeaks = correlationGrouping(options,
						header, random, measure, rangeMin, rangeMax, peaks);
				
			}			
			System.out.println("Found " + basepeaks.size() + " related peaks clusters");					
			
			// map each group id to peaks within
			System.out.println("Mapping peaks to groups");
			Map<Integer, List<IPeak>> peaksMap = mapPeaksToGroups(peaks);		

			// the list of all maps for each replicate (sample)
			clusterToPeaksMap.add(peaksMap);
			
		}
		
		return clusterToPeaksMap;

	}
		
	/**
	 * For constructing the null distribution, DO NOT USE in real code
	 * @param peaks
	 * @return
	 */
	@Deprecated
	protected void randomiseGroupIds(List<IPeak> peaks) {

		// get all group ids
		List<Integer> groupIds = new ArrayList<Integer>();
		for (IPeak peak : peaks) {
			int groupId = peak.getAnnotation(IPeak.relationid).getValueAsInteger();
			groupIds.add(groupId);
		}

		// shuffle them
		Collections.shuffle(groupIds);
		
		// reassign back to peaks
		for (int j = 0; j < peaks.size(); j++) {
			IPeak peak = peaks.get(j);
			int newGroupId = groupIds.get(j);
			peak.removeAnnotation(IPeak.relationid);
			peak.addAnnotation(IPeak.relationid, newGroupId);
		}		
		
	}		
	
	@Deprecated
	protected void evaluateResult(int noOfReplicates, int totalPeaks,
			int peaksInThreeClusters, int peaksInTwoClusters,
			List<double[]> intensesAll) {

		double n2r = peaksInTwoClusters * 100 / totalPeaks;
		double n3r = peaksInThreeClusters * 100 / totalPeaks;

//		System.out.println("K = " + CombineTask.NO_OF_ALIGNMENT_CLUSTERS);
//		System.out.println("Total peaks = " + totalPeaks);
//		System.out.println("N2R = " + peaksInTwoClusters + "("
//				+ String.format("%.2f", n2r) + "%)");
//		System.out.println("N3R = " + peaksInThreeClusters + "("
//				+ String.format("%.2f", n3r) + "%)");

		for (int sourcePeakset = 0; sourcePeakset < noOfReplicates; sourcePeakset++) {
			double[] intenses = new double[intensesAll.size()];
			for (int i = 0; i < intensesAll.size(); i++) {
				intenses[i] = intensesAll.get(i)[sourcePeakset];
			}
//			System.out.println("intenses_" + sourcePeakset + " = " + Arrays.toString(intenses) + ";");
		}

	}
	
	private Map<Integer, List<IPeak>> mapPeaksToGroups(IPeakSet<IPeak> peaks) {
		Map<Integer, List<IPeak>> peaksMap = new HashMap<Integer, List<IPeak>>();
		for (int j = 0; j < peaks.size(); j++) {

			IPeak peak = peaks.get(j);

			// here, cluster id is actually the same as the base peak id
			int groupId = peak.getAnnotation(IPeak.relationid).getValueAsInteger();
			
			// store a map between a cluster id and its corresponding bin form
			if (peaksMap.containsKey(groupId)) {
				peaksMap.get(groupId).add(peak);
			} else {
				List<IPeak> clusterPeaks = new ArrayList<IPeak>();
				clusterPeaks.add(peak);
				peaksMap.put(groupId, clusterPeaks);
			}
									
		}
		return peaksMap;
	}	
			
	/**
	 * TODO: extract options & hardcoded values into another class
	 * @param options
	 * @param header
	 * @param measure
	 * @param peaks
	 * @return
	 */
	private List<IPeak> greedyGrouping(final CombineOptions options, Header header,
			CorrelationMeasure measure, IPeakSet<IPeak> peaks) {
		
		/*
		 * See all the hardcoded values below ...
		 */
		
		List<IPeak> basepeaks;
		final HashMap<Integer,double[]> intensity_courses = new HashMap<Integer,double[]>();
		final boolean ignoreIntensity = false;
		final double minCorrSignals = 0.75;
		final double minrt = -1;
		final double rtWindow = 30;
		
		final PeakComparer comparer = new PeakComparer(intensity_courses, header, measure, ignoreIntensity,
				minCorrSignals);
		basepeaks = IPeak.findRelatedPeaks(peaks.getPeaks(), minrt, rtWindow, comparer);
		return basepeaks;

	}	

	/**
	 * TODO: extract options into another class
	 * @param options
	 * @param header
	 * @param random
	 * @param measure
	 * @param rangeMin
	 * @param rangeMax
	 * @param peaks
	 * @return
	 */
	private List<IPeak> correlationGrouping(final CombineOptions options,
			Header header, final Random random, CorrelationMeasure measure,
			float rangeMin, float rangeMax, IPeakSet<IPeak> peaks) {

		/*
		 * See all the hardcoded values below ...
		 */
		
		/* final double rtWindow = 30;
		
		Clusterer.LikelihoodScorer inScorer = new Clusterer.BetaInLikelihoodScorer(
				options.p1, options.alpha1, rangeMin, rangeMax);
		Clusterer.LikelihoodScorer outScorer = new Clusterer.BetaOutLikelihoodScorer(
				options.p0, options.alpha0, rangeMin, rangeMax);

		assert inScorer != null;
		assert outScorer != null;

		// TODO: hack -- joe !! is this correct ?
		header.setNrPeaks(peaks.size());

		final Data data = new Data(header, peaks);
		final CorrelationClusterer c = new CorrelationClusterer(data,
				rtWindow, random, options.p1, options.p0, inScorer,
				outScorer, options.alpha, options.numSamples,
				options.initialNumClusters, measure);
		Clusterer<Clustering> clusterer = new CorrelationClusterer(data,
				rtWindow, random, options.p1, options.p0, inScorer,
				outScorer, options.alpha, options.numSamples,
				options.initialNumClusters, measure);
		final List<SampleHandler<Clustering>> handlers = new ArrayList<SampleHandler<Clustering>>();
		List<IPeak> basepeaks = Clusterer.findRelatedPeaks(peaks, clusterer,
				random, handlers);

		return basepeaks; */

		Clusterer.LikelihoodScorer inScorer = new Clusterer.BetaInLikelihoodScorer(options.p1, options.alpha1, rangeMin, rangeMax);
		Clusterer.LikelihoodScorer outScorer = new Clusterer.BetaOutLikelihoodScorer(options.p0, options.alpha0, rangeMin, rangeMax);
		
		final Data data = new Data(header, peaks);
		final CorrelationParameters parameters = new CorrelationParameters(options.rtwindow, options.p1, options.p0,
				options.alpha, options.numSamples, options.burnIn, options.retentionTimeSD, options.debug, options.initialNumClusters);
		final List<PeakLikelihoodScorer<SimpleClustering>> scorers = new ArrayList<PeakLikelihoodScorer<SimpleClustering>>();
		if ( options.corrClustering ) {
			scorers.add(new CorrelationClusterer.ClusteringScorer<SimpleClustering>(data, parameters, inScorer, outScorer, measure));
		}
		if ( options.rtClustering ) {
			scorers.add(new RetentionTimeClusteringScorer<SimpleClustering>(data, parameters));
		}
		final PeakPosteriorScorer<SimpleClustering> scorer = new PeakPosteriorScorer<SimpleClustering>(scorers, parameters);
		Clusterer<Data,SimpleClustering> clusterer = new CorrelationClusterer(data, parameters, random,
				inScorer, outScorer, measure, scorer);
		final List<SampleHandler<Data,SimpleClustering>> handlers = new ArrayList<SampleHandler<Data,SimpleClustering>>();
		List<IPeak> basepeaks = Clusterer.findRelatedPeaks(peaks, clusterer, random, handlers);		
		
		return basepeaks;

	}
	
}
