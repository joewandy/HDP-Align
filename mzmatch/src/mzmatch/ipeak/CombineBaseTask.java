package mzmatch.ipeak;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

import mzmatch.ipeak.Combine.Options;
import mzmatch.ipeak.sort.Clusterer;
import mzmatch.ipeak.sort.Clustering;
import mzmatch.ipeak.sort.CorrelationClusterer;
import mzmatch.ipeak.sort.CorrelationMeasure;
import mzmatch.ipeak.sort.CorrelationParameters;
import mzmatch.ipeak.sort.Data;
import mzmatch.ipeak.sort.IdentifyPeaksets;
import mzmatch.ipeak.sort.PeakLikelihoodScorer;
import mzmatch.ipeak.sort.PeakPosteriorScorer;
import mzmatch.ipeak.sort.RetentionTimeClusteringScorer;
import mzmatch.ipeak.sort.SimpleClustering;
import mzmatch.ipeak.sort.RelatedPeaks.PeakComparer;
import mzmatch.ipeak.sort.SampleHandler;
import peakml.IPeak;
import peakml.IPeak.MatchCompare;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.math.Signal;

public abstract class CombineBaseTask implements CombineTask {

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
	
	/**
	 * TODO: don't directly pass the options, instead encapsulate the relevant attributes into another object ?
	 * @param options
	 * @param header
	 * @param peaksets
	 * @param random
	 * @param measure
	 * @param rangeMin
	 * @param rangeMax
	 * @param method
	 * @return
	 */
	protected List<Map<Integer, List<IPeak>>> groupPeaks(final Options options,
			Header header, Vector<IPeakSet<IPeak>> peaksets,
			final Random random, CorrelationMeasure measure, float rangeMin,
			float rangeMax, String method) {
		
		List<Map<Integer, List<IPeak>>> clusterToPeaksMap = new ArrayList<Map<Integer, List<IPeak>>>();
		for (int i = 0; i < peaksets.size(); i++) {	

			IPeakSet<IPeak> peaks = peaksets.get(i);
			preprocess(peaks);

			System.out.println("Grouping peaks from sourcePeakSet " + i);

			List<IPeak> basepeaks = null;
			if (CombineTask.GREEDY_GROUPING.equals(method)) {

				/* 
				 * group peaks in a greedy manner
				 * each peak will have the annotation "relationid" for the group it belongs to
				 */
				basepeaks = greedyGrouping(options, header, measure, peaks);
				
			} else if (CombineTask.CORR_GROUPING.equals(method)) {
			
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

	protected Map<Integer, List<IPeak>> mapPeaksToGroups(IPeakSet<IPeak> peaks) {
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
	protected List<IPeak> greedyGrouping(final Options options, Header header,
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
	protected List<IPeak> correlationGrouping(final Options options,
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

	/**
	 * Compares list by their size In descending order
	 * 
	 * @author joewandy
	 * 
	 */
	public static class ListSizeComparator<T> implements Comparator<List<T>> {
		@Override
		public int compare(List<T> o1, List<T> o2) {
			int size1 = o1.size();
			int size2 = o2.size();
			return -Integer.compare(size1, size2);
		}
	}

	public static class PeakMatchCompare<T extends IPeak> implements MatchCompare<T> {

		private double rtWindow;
		
		public PeakMatchCompare(double rtWindow) {
			this.rtWindow = rtWindow;
		}
		
		public double distance(IPeak peak1, IPeak peak2) {
		
			double diff = Math.abs(peak1.getRetentionTime()
					- peak2.getRetentionTime());
			
			if (diff > rtWindow) {
				return -1;
			}

			Signal signal1 = peak1.getSignal();
			Signal signal2 = peak2.getSignal();
			if (diff > 30) {
				double min1 = signal1.getMinX();
				double max1 = signal1.getMaxX();
				double min2 = signal2.getMinX();
				double max2 = signal2.getMaxX();
				if (min1 < min2 && max1 < min2) {
					return -1;
				}
				if (min2 < min1 && max2 < min1) {
					return -1;
				}
			}

			return signal1.compareTo(signal2);

		}

	}

}
