package com.joewandy.alignmentResearch.grouping;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import mzmatch.ipeak.sort.Clusterer;
import mzmatch.ipeak.sort.CorrelationClusterer;
import mzmatch.ipeak.sort.CorrelationMeasure;
import mzmatch.ipeak.sort.CorrelationParameters;
import mzmatch.ipeak.sort.Data;
import mzmatch.ipeak.sort.IdentifyPeaksets;
import mzmatch.ipeak.sort.MetAssign;
import mzmatch.ipeak.sort.MetAssign.Options;
import mzmatch.ipeak.sort.MetAssign.TestOptions;
import mzmatch.ipeak.sort.PeakClusteringSamplerHandler;
import mzmatch.ipeak.sort.PeakComparer;
import mzmatch.ipeak.sort.PeakLikelihoodScorer;
import mzmatch.ipeak.sort.PeakPosteriorScorer;
import mzmatch.ipeak.sort.RelatedPeaks;
import mzmatch.ipeak.sort.RetentionTimeClusteringScorer;
import mzmatch.ipeak.sort.SampleHandler;
import mzmatch.ipeak.sort.SimpleClustering;
import no.uib.cipr.matrix.Matrix;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;

import com.joewandy.alignmentResearch.main.MultiAlignConstants;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.FeatureGroup;

import domsax.XmlParserException;

public class MetAssignFeatureGroupingMethod extends BaseFeatureGroupingMethod implements FeatureGroupingMethod {

	private static final int BURN_IN = 100;
	private static final int NUM_DRAWS = 100;
	private static final int MASS_TOLERANCE_PPM = 3;
	private String groupingMethod;
	private double rtTolerance;
	private boolean usePeakShape;
	
	/**
	 * Creates a simple grouper
	 * @param groupingMethod 
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 */
	public MetAssignFeatureGroupingMethod(String groupingMethod, double rtTolerance, boolean usePeakShape) {
		this.groupingMethod = groupingMethod;
		this.rtTolerance = rtTolerance;
		this.usePeakShape = usePeakShape;
	}
	
	@Override
	public List<FeatureGroup> group(List<AlignmentFile> dataList) {
		System.out.println("============ Grouping = " + dataList.size() + " files ============");
		List<FeatureGroup> groups = new ArrayList<FeatureGroup>();
		int counter = 0;
		for (AlignmentFile data : dataList) {
			List<FeatureGroup> fileGroups = this.group(data);
			groups.addAll(fileGroups);							
			counter++;
		}
		return groups;
	}

	@Override
	public List<FeatureGroup> group(AlignmentFile data) {

		List<FeatureGroup> fileGroups = new ArrayList<FeatureGroup>();
		
		final String filename = data.getFilenameWithoutExtension() + ".filtered.peakml";	
		final String fullPath = data.getParentPath() + "/" + filename;	

		try {

			ParseResult result = PeakMLParser.parseIPeakSet(new FileInputStream(fullPath), null);
			final Header header = result.header;
			final IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement;
			IdentifyPeaksets.identify(peaks);
			
			// annotate the peaks with an id for the hashing
			int id = 0;
			for (IPeak peak : peaks) {
				peak.setPatternID(id++);
			}
			
			// match the peaks
			MetAssign.Options options = new MetAssign.Options();
			options.ppm = MASS_TOLERANCE_PPM;
			options.numDraws = NUM_DRAWS;
			options.burnIn = BURN_IN;
			if (usePeakShape) {
				options.corrClustering = true;
				options.rtClustering = true;
			} else {
				options.corrClustering = false;
				options.rtClustering = true;
			}
			options.verbose = true;
			options.rtwindow = 0;
			options.retentionTimeSD = 1;
			options.alpha0 = 2.0;
			options.alpha1 = 10.0;
			options.p0 = 0.97;
			options.p1 = 0.001;
			
			final Random random = new Random();
			long seed = -1;
			seed = options.seed == -1 ? random.nextLong() : options.seed;
			random.setSeed(seed);
			//if (options.verbose)
			System.err.println("Random seed is: " + seed);
			CorrelationMeasure measure = null;
			if ( "pearson".equals(options.measure) ) {
				measure = new PeakComparer.PearsonMeasure();
			} else if ( "cosine".equals(options.measure) ) {
				measure = new PeakComparer.CosineMeasure();
			}
			assert measure != null;
			
			List<IPeak> basePeaks = modelBasedClustering(data, options, peaks, header, random, measure);
			assert basePeaks != null;
			RelatedPeaks.labelRelationships(peaks, true, MASS_TOLERANCE_PPM);
			
			// the group ids must be unique across all input files ?!
			int groupId = 1;			
			for (IPeak basePeak : basePeaks) {

				// find related peaks to this basePeak (including itself)
				Set<Feature> relatedFeatures = new HashSet<Feature>();
				int basePeakCluster = basePeak.getAnnotation(IPeak.relationid).getValueAsInteger();
				for (IPeak relatedPeak : peaks) {
					int relatedPeakCluster = relatedPeak.getAnnotation(IPeak.relationid).getValueAsInteger();
					if (basePeakCluster == relatedPeakCluster) {
						int patternId = relatedPeak.getPatternID();
						Feature relatedFeature = data.getFeatureByIndex(patternId);
						relatedFeatures.add(relatedFeature);
					}
				}				
				
				FeatureGroup group = new FeatureGroup(groupId);
				groupId++;
				group.addFeatures(relatedFeatures);
				fileGroups.add(group);
				
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlParserException e) {
			e.printStackTrace();
		}

		System.out.println("fileGroups.size() = " + fileGroups.size());
		return fileGroups;
	
	}

	private List<IPeak> modelBasedClustering(AlignmentFile file, Options options,
			IPeakSet<IPeak> peaks, Header header, Random random,
			CorrelationMeasure measure) {
		
		System.err.println("No database, so only performing clustering");
		final Data data = new Data(header, peaks);
		TestOptions test = new TestOptions();
		System.err.println(test);
		final CorrelationParameters parameters = new CorrelationParameters(options.rtwindow, options.p1, options.p0,
				options.alpha, options.numDraws, options.burnIn, options.retentionTimeSD, options.debug,
				options.initialNumClusters, test);
		
		final float rangeMin = -1.0f;
		final float rangeMax = 1.0f;
		
		Clusterer.LikelihoodScorer inScorer = new Clusterer.BetaInLikelihoodScorer(options.p1, options.alpha1, rangeMin, rangeMax);
		Clusterer.LikelihoodScorer outScorer = new Clusterer.BetaOutLikelihoodScorer(options.p0, options.alpha0, rangeMin, rangeMax);

		final List<PeakLikelihoodScorer<SimpleClustering>> scorers = new ArrayList<PeakLikelihoodScorer<SimpleClustering>>();
		if ( options.corrClustering ) {
			scorers.add(new CorrelationClusterer.ClusteringScorer<SimpleClustering>(data, parameters, inScorer, outScorer, measure));
		}
		if ( options.rtClustering ) {
			scorers.add(new RetentionTimeClusteringScorer<SimpleClustering>(data, parameters));
		}
		final PeakPosteriorScorer<SimpleClustering> scorer = new PeakPosteriorScorer<SimpleClustering>(scorers, parameters);
		Clusterer<Data,SimpleClustering> clusterer = new CorrelationClusterer(data, parameters, random,
				inScorer, outScorer, measure, scorer, options.verbose);

		final List<SampleHandler<Data,SimpleClustering>> handlers = new ArrayList<SampleHandler<Data,SimpleClustering>>();

		final int n = peaks.size();
		PeakClusteringSamplerHandler peakClusteringHandler = new PeakClusteringSamplerHandler(n);
		handlers.add(peakClusteringHandler);		
		List<IPeak> basepeaks = Clusterer.findRelatedPeaks(peaks, clusterer, random, handlers);
		
		Matrix ZZprob = null;
		if (MultiAlignConstants.GROUPING_METHOD_METASSIGN_MIXTURE.equals(groupingMethod)) { 
			ZZprob = peakClusteringHandler.getLastZZ();
		} else if (MultiAlignConstants.GROUPING_METHOD_METASSIGN_POSTERIOR.equals(groupingMethod)) {
			ZZprob = peakClusteringHandler.getZZprob();
		}
		file.setZZProb(ZZprob);
		
		return basepeaks;
		
	}
	
}
