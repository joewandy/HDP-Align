package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import static com.joewandy.alignmentResearch.util.ArrayMathUtil.addArray;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.append;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.computeLogLikelihood;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.expArray;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.logArray;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.max;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.normalise;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.sample;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.subsArray;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.sum;
import static com.joewandy.alignmentResearch.util.ArrayMathUtil.toDouble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.math3.random.RandomData;
import org.apache.commons.math3.random.RandomDataImpl;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethodParam;
import com.joewandy.alignmentResearch.model.AlignmentFile;
import com.joewandy.alignmentResearch.model.DpResult;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.HDPAnnotation;
import com.joewandy.alignmentResearch.model.HDPClustering;
import com.joewandy.alignmentResearch.model.HDPClusteringParam;
import com.joewandy.alignmentResearch.model.HDPFile;
import com.joewandy.alignmentResearch.model.HDPMassCluster;
import com.joewandy.alignmentResearch.model.HDPMetabolite;

public class HDPMassRTClustering implements HDPClustering {

	private HDPClusteringParam hdpParam;			// parameters for HDP method
	private List<HDPFile> hdpFiles;					// input files to be processed
	private List<HDPMetabolite> hdpMetabolites;		// inferred metabolites
	private int hdpMetaboliteId;					// sequence ID for metabolites
	private final RandomData randomData;			// random data generator	
	private HDPSampleHandler sampleHandler;		// stores the samples obtained from Gibbs sampling
	private HDPSampleProcessor sampleProcessor;	// process the samples after Gibbs sampling is done
	private String hdpClusteringResultsPath;		// path to previous clustering results to load, if any

	// experimental hack to ignore peaks during gibbs update
	private Map<Feature, Integer> singletonCount;		
	private Set<Feature> ignoreSet; 	
	
	// TODO: move all these into HDPMetabolite
	private int I;									// how many metabolites are there?
	private List<Integer> fi;						// no. of RT clusters in each metabolite
	private List<Double> ti;						// RT value of each metabolite
	private List<Double> si;						// sum of RT clusters' time in each metabolite
			
	/**
	 * Constructs an instance of HDP clustering by mass and RT
	 * @param dataList The list of files to be processed
	 * @param methodParam Alignment method parameters
	 */
	public HDPMassRTClustering(List<AlignmentFile> dataList, AlignmentMethodParam methodParam) {

		this.randomData = new RandomDataImpl();
		this.singletonCount = new HashMap<Feature, Integer>();
		this.ignoreSet = new HashSet<Feature>();
		
		// set HDP parameters
		this.hdpParam = new HDPClusteringParam();
		setHdpParam(dataList, methodParam);
		this.hdpClusteringResultsPath = methodParam.getHdpClusteringResultsPath();
		
		// assign a sequential ID to all peaks to store the result later
		initialiseSequenceID(dataList);
		this.hdpMetabolites = new ArrayList<HDPMetabolite>();
		
		// put all peaks into 1 RT cluster, 1 metabolite, 1 mass cluster
		initialiseGibbsSampling(dataList);

		// setup sample handler, call this only after gibbs sampling has been initialised
		int totalPeaks = 0;
		for (AlignmentFile file : dataList) {
			totalPeaks += file.getFeaturesCount();
		}
		this.sampleHandler = new HDPSampleHandler(totalPeaks, hdpMetabolites);
		
		// setup sample processor
		double massTol = methodParam.getHdpMassTol();
		String idDatabase = methodParam.getIdentificationDatabase();
		String mode = methodParam.getMode();
		this.sampleProcessor = new HDPSampleProcessor(massTol, idDatabase, mode);

	}

	/**
	 * Calls Gibbs sampling and collects all the resulting samples
	 */
	public void runClustering() {
		
		// if path to previous clustering results is provided, then try to load it
		boolean loadSuccess = false;
		if (hdpClusteringResultsPath != null) {			
			loadSuccess = sampleHandler.initialiseResultsFromPath(hdpClusteringResultsPath);
		}
		
		if (!loadSuccess) {
			
			// need to run a new clustering here
			double totalTime = 0;
			for (int s = 0; s < hdpParam.getNsamps(); s++) {
				
				long startTime = System.currentTimeMillis();
				int peaksProcessed = assignPeakMassRt();
				updateParametersMassRt();
				long endTime = System.currentTimeMillis();
				double timeTaken = (endTime - startTime) / 1000.0;
				totalTime += timeTaken;
				
				// process the sample
				boolean last = false;
				if (s == hdpParam.getNsamps()-1) {
					last = true;
				}
				sampleHandler.storeSample(s, peaksProcessed, timeTaken, hdpParam, last);
				
			}
			System.out.println(String.format("TOTAL TIME = %5.2fs", totalTime));

			// if path is provided, then try to save this clustering results
			if (hdpClusteringResultsPath != null) {
				sampleHandler.persistResultsToPath(hdpClusteringResultsPath);
			}
			
		}
		
		// process the results, regardless of whether it's loaded or new clustering results
		HDPAllSamples resultsList = sampleHandler.getSamplingResults();
		sampleProcessor.processSample(resultsList);
		
	}
	
	/**
	 * Returns the probabilities of aligned features set
	 */
	public HDPAlignmentResults getAlignmentResults() {		
		return sampleProcessor.getAlignmentResults();
	}
	
	/**
	 * Returns the number of samples taken
	 */
	public int getSamplesTaken() {
		return sampleProcessor.getSamplesTaken();
	}

	/**
	 * Returns annotations on features for ionisation products
	 */
	public HDPAnnotation<Feature> getIonisationProductFeatureAnnotations() {
		return sampleProcessor.getIonisationProductFeatureAnnotations();
	}

	/**
	 * Returns annotations on features for metabolites
	 */
	public HDPAnnotation<Feature> getMetaboliteFeatureAnnotations() {
		return sampleProcessor.getMetaboliteFeatureAnnotations();
	}

	/**
	 * Returns annotations on features for metabolites
	 */
	public HDPAnnotation<HDPMetabolite> getMetaboliteAnnotations() {
		return sampleProcessor.getMetaboliteAnnotations();
	}
	
	/**
	 * Returns annotations on features for isotope types
	 */
	public HDPAnnotation<Feature> getIsotopeFeatureAnnotations() {
		return sampleProcessor.getIsotopeFeatureAnnotations();
	}
	
	/**
	 * Returns last HDP sample
	 */
	public HDPSingleSample getLastSample() {
		return sampleProcessor.getLastSample();
	}
			
	/**
	 * Initialises HDP parameters
	 * @param dataList
	 * @param methodParam
	 */
	private void setHdpParam(List<AlignmentFile> dataList,
			AlignmentMethodParam methodParam) {
				
		hdpParam.setMu_0(getRTMean(dataList));
		hdpParam.setPsi_0(getMassMean(dataList));
		hdpParam.setSigma_0_prec(1.0/5E6);
		hdpParam.setRho_0_prec(1.0/5E6);
				
		hdpParam.setNsamps(methodParam.getGroupingNSamples());
		hdpParam.setBurnIn(methodParam.getGroupingBurnIn());
		hdpParam.setAlpha_rt(methodParam.getHdpAlphaRt());
		hdpParam.setAlpha_mass(methodParam.getHdpAlphaMass());
		hdpParam.setTop_alpha(methodParam.getHdpTopAlpha());
		
		double globalRtClusterStdev = methodParam.getHdpGlobalRtClusterStdev();
		double globalRtClusterPrec = 1.0 / (globalRtClusterStdev*globalRtClusterStdev);
		hdpParam.setDelta_prec(globalRtClusterPrec);
		
		double localRtClusterStdev = methodParam.getHdpLocalRtClusterStdev();
		double localRtClusterPrec = 1.0 / (localRtClusterStdev*localRtClusterStdev);
		hdpParam.setGamma_prec(localRtClusterPrec);
				
		double massTol = methodParam.getHdpMassTol();
		double massPrec = getMassPrec(massTol);
		hdpParam.setRho_prec(massPrec);

		hdpParam.setSpeedUpHacks(methodParam.isHdpSpeedUp());
		hdpParam.setSpeedUpNumSample(methodParam.getHdpSpeedUpNumSample());
		hdpParam.setRefFileIdx(methodParam.getHdpRefFileIdx());
				
	}
	
	/**
	 * Assigns a sequence number to peaks across files.
	 * Used when constructing the similarity matrix.
	 * @param dataList The list of input files
	 * @return The total number of peaks across files
	 */
	private int initialiseSequenceID(List<AlignmentFile> dataList) {
		
		int sequenceID = 0;
		int totalPeaks = 0;
		for (int j=0; j < dataList.size(); j++) {
						
			AlignmentFile alignmentFile = dataList.get(j);
			totalPeaks += alignmentFile.getFeaturesCount();
			for (Feature f : alignmentFile.getFeatures()) {
				f.setSequenceID(sequenceID);
				sequenceID++;
			}
			
		}

		return totalPeaks;
	
	}
	
	/**
	 * Puts all peaks across datalist into 1 RT cluster, 1 metabolite, 1 mass cluster
	 * @param dataList The list of input files
	 */
	private void initialiseGibbsSampling(List<AlignmentFile> dataList) {

		// sample initial metabolite RT
		this.I = 1;
		this.fi = new ArrayList<Integer>();
		this.ti = new ArrayList<Double>();
		this.si = new ArrayList<Double>();
		for (int i = 0; i < this.I; i++) {
			double rt = randomData.nextGaussian(hdpParam.getMu_0(), 
					Math.sqrt(1/hdpParam.getSigma_0_prec()));
			appendFi(0);
			appendTi(rt);
			appendSi(0.0);
			hdpMetaboliteId = i;
			this.hdpMetabolites.add(new HDPMetabolite(hdpMetaboliteId));
		}
		
	    // assign peaks across files into 1 RT cluster per file, 1 metabolite
		hdpFiles = new ArrayList<HDPFile>();
		List<Feature> dataAll = new ArrayList<Feature>();
		int i = 0;
		for (int j=0; j < dataList.size(); j++) {
						
			HDPFile hdpFile = new HDPFile(j);
			AlignmentFile alignmentFile = dataList.get(j);
			hdpFile.addFeatures(alignmentFile.getFeatures());
			hdpFile.setK(1);
						
			dataAll.addAll(hdpFile.getFeatures());

			// 1 x N, peaks to clusters assignment
			for (int n = 0; n < hdpFile.N(); n++) {
				hdpFile.appendZ(0); // assign all features under 1 RT cluster
			}

			// 1 x K, clusters to metabolites assignment
			for (int k = 0; k < hdpFile.K(); k++) {
				hdpFile.appendTopZ(0); // assign all RT clusters under 1 metabolite 
				// 1 x K, sample initial the clusters' RT
				int parentI = hdpFile.topZ(k);
				double mu = this.ti.get(parentI);
				double prec = hdpParam.getDelta_prec();
				double tjk = sampleNewClusterRt(mu, prec, j);
				hdpFile.appendTjk(tjk);
				addSi(parentI, tjk);
				hdpFile.appendCountZ(hdpFile.N());
				hdpFile.appendSumZ(hdpFile.getRtSum());
			}
			
			addFi(i, hdpFile.K());
			hdpFiles.add(hdpFile);
			
		}
		
		// assign all peaks under 1 metabolite
		for (i = 0; i < this.I; i++) {
			
			HDPMetabolite met = hdpMetabolites.get(hdpMetaboliteId);
			hdpMetaboliteId++;
			int a = met.addMassCluster();
			for (int n = 0; n < dataAll.size(); n++) {
				Feature f = dataAll.get(n);
				met.addPeak(f, a);
			}
			
		}
		
	}

	/**
	 * Samples a new cluster RT given the parent metabolite
	 * @param mu parent metabolite RT
	 * @param prec precision
	 * @param j input file, used if we're setting a fixed reference file
	 * @return new cluster RT value
	 */
	private double sampleNewClusterRt(double mu, double prec, int j) {
		double stdev = Math.sqrt(1/prec);
		double tjk = randomData.nextGaussian(mu, stdev);
		// use reference file if file index != -1
		if (hdpParam.getRefFileIdx() != -1) {
			// set tjk | ti to have 0 standard deviation for the reference file
			if (j == hdpParam.getRefFileIdx()) {
				tjk = mu;
			}
		}
		return tjk;
	}
		
	/**
	 * Performs the actual Gibbs sampling here. Loop across all peaks in all files,
	 * remove peak from model and reassign it.
	 * @return 
	 */
	private int assignPeakMassRt() {
		
		int peaksProcessed = 0;
		
		// TODO: loop across all files randomly
		for (HDPFile hdpFile : hdpFiles) {
					
			// TODO: loop across peaks randomly
			assert(hdpFile.N() == hdpFile.Zsize());
			for (int n = 0; n < hdpFile.N(); n++) {
				
				// enable experimental hack to speed up gibbs update
				if (hdpParam.isSpeedUpHacks()) {

					Feature thisPeak = hdpFile.getFeatures().get(n);

					// we will not process anything in the ignore set
					if (ignoreSet.contains(thisPeak)) {
						continue;
					}

					int k = hdpFile.Z(n); 
					int i = hdpFile.topZ(k);
					HDPMetabolite met = hdpMetabolites.get(i);
					assert(met.vSize() == met.peakDataSize());
					
					// track how many times this peak is assigned to a singleton cluster
					boolean ignored = trackSingletonCount(thisPeak, met);
					if (ignored) {
						// if ignore, then remove peak from model, and he remains a bachelor forever
						removePeakFromModel(hdpFile, thisPeak, met, n, k, i);
					} else {							
						// otherwise perform the gibbs update as usual
						removePeakFromModel(hdpFile, thisPeak, met, n, k, i);
						reassignPeak(hdpFile, thisPeak, n);					
						peaksProcessed++;
					}
																	
				} else {
					
					// do old-fashioned update
					Feature thisPeak = hdpFile.getFeatures().get(n);
					int k = hdpFile.Z(n); 
					int i = hdpFile.topZ(k);
					HDPMetabolite met = hdpMetabolites.get(i);
					assert(met.vSize() == met.peakDataSize());					
					removePeakFromModel(hdpFile, thisPeak, met, n, k, i);
					reassignPeak(hdpFile, thisPeak, n);					
					peaksProcessed++;
					
				}
				
			} // end loop across peaks randomly
					
		} // end loop across files randomly
		
		return peaksProcessed;
		
	}
	
	/**
	 * Removes peak from model and perform the necessary book-keeping to 
	 * delete empty mass cluster, RT cluster and metabolite.
	 * @param hdpFile The file containing the peak
	 * @param thisPeak The peak
	 * @param met The parent metabolite
	 * @param n The peak position index
	 * @param k The RT cluster position index
	 * @param i The metabolite position index
	 */
	private void removePeakFromModel(HDPFile hdpFile, Feature thisPeak,
			HDPMetabolite met, int n, int k, int i) {

		hdpFile.setZ(n, -1);
		hdpFile.decreaseCountZ(k);
		hdpFile.subsSumZ(k, thisPeak.getRt());
		HDPMassCluster massCluster = met.removePeak(thisPeak);
		
		// does this result in an empty mass cluster ?
		if (massCluster.getCountPeaks() == 0) {
			
			// delete this mass cluster indexed by a
			met.removeMassCluster(massCluster);
													
		}
		
		// does this result in an empty RT cluster ?
		if (hdpFile.countZ(k) == 0) {
			
			// delete the RT cluster
			hdpFile.decreaseK();
			hdpFile.reindexZ(k);
			
			// update for bookkeeping
			hdpFile.removeCountZ(k);
			hdpFile.removeSumZ(k);
			double tij = hdpFile.tjk(k);
			hdpFile.removeTjk(k);
			
			// remove assignment of cluster to parent metabolite
			hdpFile.removeTopZ(k);
			
			// decrease count of clusters under parent metabolite
			decreaseFi(i);
			subsSi(i, tij);
			
			// does this result in an empty metabolite ?
			if (fi(i) == 0) {
				
				// delete the metabolite across all replicates
				for (int rep = 0; rep < hdpFiles.size(); rep++) {
					
					HDPFile repFile = hdpFiles.get(rep);
					repFile.reindexTopZ(i);
					
				}
				
				// delete top-level info too
				removeFi(i);
				removeSi(i);
				removeTi(i);
				this.I--;
				
				// delete mass clusters info related to this metabolite too
				hdpMetabolites.remove(i);
				
			}
			
		}
		
		assert(met.getEmptyMassClusters().size() == 0);
		
	}

	/**
	 * Performs assignment of peak to a new RT cluster, metabolite and mass cluster
	 * @param hdpFile The parent file
	 * @param thisPeak The peak
	 * @param n The index of position of peak in file
	 * @return 
	 */
	private HDPMetabolite reassignPeak(HDPFile hdpFile, Feature thisPeak, int n) {

		// for current RT cluster, first compute the RT term
		double[] rtTermLogLike = computeCurrentClusterRTLikelihood(
				hdpFile, thisPeak);
		
		// then for every RT cluster, compute the likelihood of this peak to be in the mass clusters linked to it
		double[] metaboliteMassLike = new double[I];
		for (int metIndex = 0; metIndex < I; metIndex++) {
			HDPMetabolite thisMetabolite = hdpMetabolites.get(metIndex);
			double[] massTermLogPost = computeMassTermLogLikelihood(
					thisPeak, thisMetabolite);
			// marginalise over all the mass clusters by summing over them
			metaboliteMassLike[metIndex] = sum(expArray(massTermLogPost));				
		}								
		// % 1 by K, stores the mass log likelihood linked to each RT cluster
		double[] massTermLogLike = new double[hdpFile.K()];
		for (int thisCluster = 0; thisCluster < hdpFile.K(); thisCluster++) {
			int metIndex = hdpFile.topZ(thisCluster); // find the parent metabolite
			massTermLogLike[thisCluster] = Math.log(metaboliteMassLike[metIndex]);					
		}
		
		// finally, the likelihood of peak going into a current cluster eq #15 is the RT * mass terms
		double[] currentClusterLogLike = addArray(rtTermLogLike, massTermLogLike);
		
		// now compute the likelihood for peak going into new cluster, eq #20      
		// first, compute p( x_nj | existing metabolite ), eq #21
		double denum = sum(fiArray()) + hdpParam.getTop_alpha();
		double[] currentMetabolitePost = new double[I];
		
		for (int idx = 0; idx < I; idx++) {
			double logPrior = Math.log(fi(idx) / denum);
			// first compute the RT term
			double mu = ti(idx);
			double prec = 1/(1/hdpParam.getGamma_prec() + 1/hdpParam.getDelta_prec());
			double rtLogLikelihood = computeLogLikelihood(thisPeak.getRt(), mu, prec);
			// then compute the mass term
			double massLogLikelihood = Math.log(metaboliteMassLike[idx]);
			// multiply likelihood with prior to get the posterior p( % d_jn | existing metabolite )
			double logLikelihood = rtLogLikelihood + massLogLikelihood;
			double logPosterior = logPrior + logLikelihood;
			currentMetabolitePost[idx] = Math.exp(logPosterior);
		}
		
		// then compute p( d_jn | new metabolite ), eq #22
		double logPrior = Math.log(hdpParam.getTop_alpha()/denum);
		double mu = hdpParam.getMu_0();
		double prec = 1/(1/hdpParam.getGamma_prec() + 1/hdpParam.getDelta_prec() + 1/hdpParam.getSigma_0_prec()); 
		double rtLogLikelihood = computeLogLikelihood(thisPeak.getRt(), mu, prec);
		mu = hdpParam.getPsi_0();
		prec = 1/(1/hdpParam.getRho_prec() + 1/hdpParam.getRho_0_prec()); 
		double massLogLikelihood = computeLogLikelihood(thisPeak.getMass(), mu, prec);
		double logLikelihood = rtLogLikelihood + massLogLikelihood;
		double logNewMetabolitePost = logPrior + logLikelihood;
		double newMetabolitePost = Math.exp(logNewMetabolitePost);
		
		// sum over for eq #17
		double[] metabolitePost = append(currentMetabolitePost, newMetabolitePost);
		double newClusterLogLike = Math.log(sum(metabolitePost));
					
		// pick either existing or new RT cluster
		// set the prior
		int[] countZArr = hdpFile.countZArray();
		double[] clusterPrior = append(toDouble(countZArr), hdpParam.getAlpha_rt());
		clusterPrior = normalise(clusterPrior, sum(clusterPrior));
		double[] clusterLogLike = append(currentClusterLogLike, newClusterLogLike);
		double[] clusterLogPost = addArray(logArray(clusterPrior), clusterLogLike);
		
		// compute and sample from posterior
		double[] clusterPost = expArray(subsArray(clusterLogPost, max(clusterLogPost)));
		clusterPost = normalise(clusterPost, sum(clusterPost));
		int k = sample(clusterPost, randomData);
		if ((k+1) > hdpFile.K()) {
			assignPeakToNewRTCluster(hdpFile, thisPeak, k,
					metabolitePost);
		}
		
		// now given RT cluster k, we can assign peak to the mass clusters linked to k
		// find the parent metabolite first  
		int i = hdpFile.topZ(k);
		
		// for existing and new mass cluster
		HDPMetabolite met = hdpMetabolites.get(i);
		double[] massTermLogPost = computeMassTermLogLikelihood(
				thisPeak, met);
		
		// pick the mass cluster
		double[] post = expArray(subsArray(massTermLogPost, max(massTermLogPost)));
		post = normalise(post, sum(post));
		int a = sample(post, randomData);
		if ((a+1) > met.getA()) {
			met.addMassCluster(); // make new mass cluster										
		}
		
		// finally add peak back into model				
		addPeakToModel(hdpFile, thisPeak, met, n, k, a);
		assert(this.hdpMetabolites.size() == this.I);
		
		return met;
		
	}
	
	/**
	 * Tracks how many times a peak is assigned to a singleton cluster
	 * @param thisPeak The peak to track
	 * @param parentMet The parent metabolite
	 */
	private boolean trackSingletonCount(Feature thisPeak, HDPMetabolite parentMet) {

		HDPMassCluster mc = parentMet.getMassClusterOfPeak(thisPeak);				
		assert(mc.getCountPeaks() != 0);
		Integer counter = singletonCount.get(thisPeak);
		
		if (counter == null) {
			// never encounter this peak before
			singletonCount.put(thisPeak, 1);
		} else {
		
			if (mc.getCountPeaks() == 1) {
				// increment count if this peak is in a singleton cluster
				singletonCount.put(thisPeak, counter+1);
				// if the peak stays single long enough, add him to ignore set
				if (counter >= this.hdpParam.getSpeedUpNumSample()) {
					ignoreSet.add(thisPeak);
					return true;
				}
			} else {
				// reset counter if this peak joins a non-singleton cluster
				singletonCount.put(thisPeak, 0);
			}
			
		}		
		return false;

	}

	/**
	 * Computes the posterior probability distribution of peak to be 
	 * in the internal mass clusters of this metabolite
	 * @param thisPeak The peak
	 * @param thisMetabolite The metabolite
	 * @return The posterior probability distribution (log)
	 */
	private double[] computeMassTermLogLikelihood(Feature thisPeak,
			HDPMetabolite thisMetabolite) {
		
		double x = thisPeak.getMassLog();
		double componentPrec = hdpParam.getRho_prec();
		double hyperparamMean = hdpParam.getPsi_0();
		double hyperparamPrec = hdpParam.getRho_0_prec();
		double dpAlpha = hdpParam.getAlpha_mass();
		int[] cCounts = thisMetabolite.faArray();
		double[] cSums = thisMetabolite.saArray();
		DpResult dpResult = getDpLogPriorLikelihood(x, componentPrec, hyperparamMean, hyperparamPrec, 
				dpAlpha, cCounts, cSums);
		
		// compute posterior probability
		double[] massTermLogPrior = dpResult.getLogPrior();
		double[] massTermLogLikelihood = dpResult.getLogLikelihood();
		// hack to prevent peaks going into the same mass cluster if another peak from the same file is already there
		massTermLogLikelihood = modifyTerms(thisPeak, thisMetabolite, massTermLogLikelihood);
		double[] massTermLogPost = addArray(massTermLogPrior, massTermLogLikelihood);
		
		return massTermLogPost;

	}

	/**
	 * Computes the log likelihood of peak to be in the current RT
	 * clusters in hdpFile
	 * @param hdpFile The file containing the RT clusters
	 * @param thisPeak The peak
	 * @return The log likelihood distribution
	 */
	private double[] computeCurrentClusterRTLikelihood(HDPFile hdpFile,
			Feature thisPeak) {
		
		double[] tijArr = hdpFile.tjkArray();
		double prec = hdpParam.getGamma_prec();
		double[] rtTermLogLike = new double[tijArr.length];
		assert(hdpFile.K()==rtTermLogLike.length);

		for (int idx = 0; idx < tijArr.length; idx++) {
			double tij = tijArr[idx];
			double logLikelihood = computeLogLikelihood(thisPeak.getRt(), tij, prec);
			rtTermLogLike[idx] = logLikelihood;
		}
		
		return rtTermLogLike;

	}

	/**
	 * Assigns peak to a new RT cluster k, then assigns that RT cluster k
	 * (and the peak) to either an existing metabolite or a new metabolite.
	 * @param hdpFile The file containing the peak
	 * @param thisPeak The peak
	 * @param k The new cluster index
	 * @param metabolitePost The posterior probability distribution of peak
	 * 		to be in existing metabolites and a new metabolite
	 */
	private void assignPeakToNewRTCluster(HDPFile hdpFile, Feature thisPeak,
			int k, double[] metabolitePost) {

		// new cluster
		hdpFile.increaseK();
		
		// resize peak-cluster assignment to include the new cluster
		hdpFile.appendCountZ(0);
		hdpFile.appendSumZ(0.0);

		// resize cluster-metabolite assignment to include the new cluster
		hdpFile.appendTopZ(0);
		
		// decide which metabolite to assign the new cluster to
		metabolitePost = normalise(metabolitePost, sum(metabolitePost));
		int i = sample(metabolitePost, randomData);		
		if ((i+1) <= this.I) {
			assignPeakToCurrentMetabolite(hdpFile, k, i);						
		} else {
			assignPeakToNewMetabolite(hdpFile, thisPeak, k, i);
		}
		
		// generate tij given ti and data RT
		double prec = hdpParam.getGamma_prec() + hdpParam.getDelta_prec();
		double mu = 1/prec * (hdpParam.getGamma_prec()*thisPeak.getRt() + hdpParam.getDelta_prec()*ti(i));
		double newTij = randomData.nextGaussian(mu, Math.sqrt(1/prec));
		hdpFile.appendTjk(newTij);
		addSi(i, newTij);
		
	}

	/**
	 * Assigns an RT cluster k to an existing metabolite i
	 * @param hdpFile The file containing k
	 * @param k The RT cluster index k
	 * @param i The metabolite index i
	 */
	private void assignPeakToCurrentMetabolite(HDPFile hdpFile, int k, int i) {
		hdpFile.setTopZ(k, i);
		increaseFi(i);
	}

	/**
	 * Creates a new metabolite i and assigns an RT cluster k (and also the peak) to it
	 * @param hdpFile The file containing k
	 * @param thisPeak The peak
	 * @param k The rt cluster index k
	 * @param i The metabolite index i
	 */
	private void assignPeakToNewMetabolite(HDPFile hdpFile, Feature thisPeak, int k,
			int i) {
		
		// new metabolite
		this.I++;
		
		// assign the cluster under this new metabolite
		hdpFile.setTopZ(k, i);
		appendFi(1);
		appendSi(0.0);
		
		// generate ti given data RT
		double temp = 1 / (1/hdpParam.getGamma_prec() + 1/hdpParam.getDelta_prec());
		double prec = temp + hdpParam.getSigma_0_prec();
		double mu = 1/prec * (temp*thisPeak.getRt() + hdpParam.getSigma_0_prec()*hdpParam.getMu_0());
		double newTi = randomData.nextGaussian(mu, Math.sqrt(1/prec));
		appendTi(newTi);
		
		// create empty mass cluster data structures for use later
		hdpMetabolites.add(new HDPMetabolite(hdpMetaboliteId));
		hdpMetaboliteId++;

	}
	
	/**
	 * Assigns a peak n to newly computed mass cluster index a, metabolite and 
	 * RT cluster k
	 * @param hdpFile The file 
	 * @param thisPeak The peak
	 * @param met The metabolite (whether existing or new)
	 * @param n The peak position index
	 * @param k The RT cluster position index
	 * @param a The mass cluster position index
	 */
	private void addPeakToModel(HDPFile hdpFile, Feature thisPeak,
			HDPMetabolite met, int n, int k, int a) {

		// assign the peak under mass cluster a
		met.addPeak(thisPeak, a);
		
		// assign the peak under RT cluster k
		hdpFile.setZ(n, k);
		hdpFile.increaseCountZ(k);
		hdpFile.addSumZ(k, thisPeak.getRt());
		
		HDPMassCluster mc = met.getMassClusterOfPeak(thisPeak);
		assert(mc.getCountPeaks() != 0);

	}

	/**
	 * Computes log prior and likelihood of a value x in a DP mixture
	 * with some mean, a fixed precision, and concentration parameter dpAlpha.
	 * @param x The value
	 * @param componentPrec Mixture component precision
	 * @param hyperparamMean Hyperparameter mean of the mixture component mean
	 * @param hyperparamPrec Hyperparameter precision of the mixture component mean
	 * @param dpAlpha DP concentration parameter
	 * @param cCounts Arrays of counts of existing data points in current mixture components
	 * @param cSums Sums of values of existing data points in current mixture components
	 * @return
	 */
	private DpResult getDpLogPriorLikelihood(double x, double componentPrec, 
			double hyperparamMean, double hyperparamPrec, double dpAlpha, 
			int[] cCounts, double[] cSums) {

		assert(cCounts.length == cSums.length);
		int numFinite = cCounts.length; // count of finite clusters
		int numInfinite = numFinite + 1; // count of finite cluster + the new one
		int lastIndex = numInfinite - 1; // index of last array element
		
		// compute prior probability for K existing table and new table
		double[] prior = new double[numInfinite]; 
		for (int i = 0; i < numFinite; i++) {
			prior[i] = cCounts[i];
		}
		prior[lastIndex] = dpAlpha;
		prior = normalise(prior, sum(prior));
		
		// for current k
		double[] paramBeta = new double[numInfinite];
		double[] paramAlpha = new double[numInfinite];
		for (int i = 0; i < numFinite; i++) {
			paramBeta[i] = hyperparamPrec + (componentPrec*cCounts[i]);
			paramAlpha[i] = (1/paramBeta[i]) * ((hyperparamPrec*hyperparamMean) + (componentPrec*cSums[i]));
		}
		
		// for new k
		double lastParamBeta = hyperparamPrec;
		double lastParamAlpha = hyperparamMean;
		paramBeta[lastIndex] = lastParamBeta;
		paramAlpha[lastIndex] = lastParamAlpha;
		
		// compute log likelihood
		double[] prec = new double[numInfinite];
		double[] logLikelihood = new double[numInfinite];
		double[] logPrior = new double[numInfinite];
		for (int i = 0; i < numInfinite; i++) {
			prec[i] = 1/(1/paramBeta[i]+1/componentPrec);
			logLikelihood[i] = computeLogLikelihood(x, paramAlpha[i], prec[i]);
			logPrior[i] = Math.log(prior[i]);
		}
		
		DpResult dpResult = new DpResult(logPrior, logLikelihood);		
		return dpResult;

	}
	
	/**
	 * Experimental hack to modify the likelihood to prevent a peak from
	 * entering a mass cluster if another peak from the same file is already there first.
	 * This is used to improve alignment performance, since we don't want to align peaks
	 * from the same file together, so we don't want them to be in the same mass cluster.
	 * @param thisPeak The peak
	 * @param thisMetabolite The metabolite
	 * @param logDistribution The original likelihood
	 * @return The distribution The modified likelihood
	 */
	private double[] modifyTerms(Feature thisPeak,
			HDPMetabolite thisMetabolite, double[] logDistribution) {

		// get the indicator array, entries are either 0 or 1
		int[] indicator = thisMetabolite.getMassClusterIndicator(thisPeak);					
		assert(indicator.length == logDistribution.length);
		for (int a = 0; a < indicator.length; a++) {
			if (indicator[a] == 0) {
				// we set the likelihood = 0, and log(0) = -infinity
				logDistribution[a] = Double.NEGATIVE_INFINITY;
			}
		}		
		return logDistribution;
	
	}
	
	/**
	 * Updates component values based on existing data
	 */
	private void updateParametersMassRt() {
		
		// update local RT clusters        
		for (int j = 0; j < hdpFiles.size(); j++) {

			HDPFile hdpFile = hdpFiles.get(j);
			for (int k = 0; k < hdpFile.K(); k++) {
				
				// find parent metabolite of this cluster
				int i = hdpFile.topZ(k);
				double ti = ti(i);
				double sumXn = hdpFile.sumZ(k);
				int countPeaks = hdpFile.countZ(k);
				
				// draw new tjk
	            double prec = hdpParam.getDelta_prec() + countPeaks*hdpParam.getGamma_prec();
	            double mu = (1/prec) * ( ti*hdpParam.getDelta_prec() + hdpParam.getGamma_prec()*sumXn );
	            double tjk = sampleNewClusterRt(mu, prec, j);
				hdpFile.setTjk(k, tjk);
				
			}
			
		}
		
		// update metabolite RT
		for (int i = 0; i < hdpMetabolites.size(); i++) {
		
			double sum_clusters = si(i);
			double count_clusters = fi(i);
			
			// draw new ti given the cluster RTs
			double prec = hdpParam.getSigma_0_prec() + count_clusters*hdpParam.getDelta_prec();
	        double mu = (1/prec) * ( hdpParam.getMu_0()*hdpParam.getSigma_0_prec() + hdpParam.getDelta_prec()*sum_clusters );
	        double new_ti = randomData.nextGaussian(mu, Math.sqrt(1/prec));
	        setTi(i, new_ti);	        
	        
	        // also update all the mass clusters linked to this metabolite		
	        HDPMetabolite met = hdpMetabolites.get(i);		
	        for (int a = 0; a < met.getA(); a++) {		
	        	
//	        	prec = hdpParam.getRho_0_prec() + (hdpParam.getRho_prec() + met.fa(a));		
//	        	mu = (1/prec) * ( (hdpParam.getPsi_0()*hdpParam.getRho_0_prec()) + (hdpParam.getRho_prec() * met.sa(a)) );		
//	        	double newTheta = randomData.nextGaussian(mu, Math.sqrt(1/prec)); 	

	        	double sumMasses = 0;
	        	int count = 0;
	        	for (Feature f : met.getPeaksInMassCluster(a)) {
	        		sumMasses += f.getMass();
	        		count++;
	        	}
	        	double newTheta = sumMasses / count;
	        	met.setTheta(a, Math.log(newTheta));
	        	
	        }		
	        
		}
				
	}
			
	/**
	 * Computes mass precision given the required precision in parts-per-million (ppm).
	 * @param massTol The precision in ppm
	 * @return The precision, such that 2 stdev = the precision in ppm
	 */
	private double getMassPrec(double massTol) {

		double logOnePpm = Math.log(1000001) - Math.log(1000000);
		double logDiff = logOnePpm * massTol; 
		double stdev = logDiff/2; // assume 2 stdev = logDiff
		double prec = 1.0 / (stdev*stdev);
		System.out.println("massPrec = " + prec);
		
		return prec;			
	
	}
	
	/**
	 * Computes the mean of RT values of peaks across files
	 * @param dataList The files
	 * @return The mean of RT values
	 */
	private double getRTMean(List<AlignmentFile> dataList) {
	
		double sum = 0;
		int count = 0;
		for (AlignmentFile file : dataList) {
			for (Feature f : file.getFeatures()) {
				sum += f.getRt();
				count++;
			}
		}
		
		double mean = sum / count;
		return mean;
	
	}

	/**
	 * Computes the mean of mass values of peaks across files
	 * @param dataList The files
	 * @return The mean of mass values, in log
	 */
	private double getMassMean(List<AlignmentFile> dataList) {
	
		double sum = 0;
		int count = 0;
		for (AlignmentFile file : dataList) {
			for (Feature f : file.getFeatures()) {
				sum += Math.exp(f.getMassLog());
				count++;
			}
		}

		double mean = sum / count;
		return Math.log(mean);
	
	}
	
	private int fi(int i) {
		return this.fi.get(i);
	}
	
	private void setFi(int i, int newFi) {
		this.fi.set(i, newFi);
	}
	
	private void addFi(int i, int amount) {
		int fi = this.fi.get(i);
		fi += amount;
		setFi(i, fi);
	}
	
	private void increaseFi(int i) {
		int fi = this.fi.get(i);
		fi++;
		this.fi.set(i, fi);
	}
	
	private void decreaseFi(int i) {
		int fi = this.fi.get(i);
		fi--;
		this.fi.set(i, fi);
	}

	private void appendFi(int count) {
		this.fi.add(count);
	}
	
	private void removeFi(int i) {
		this.fi.remove(i);
	}
	
	public int[] fiArray() {
		Integer[] temp = fi.toArray(new Integer[fi.size()]);
		return ArrayUtils.toPrimitive(temp);
	}	
	
	private double ti(int i) {
		return this.ti.get(i);
	}
	
	private void setTi(int i, double ti) {
		this.ti.set(i, ti);
	}
		
	private void appendTi(double ti) {
		this.ti.add(ti);
	}
	
	private void removeTi(int i) {
		this.ti.remove(i);
	}
	
	private void setSi(int i, double si) {
		this.si.set(i, si);
	}
	
	private double si(int i) {
		return this.si.get(i);
	}
	
	private void addSi(int i, double amount) {
		double si = this.si.get(i);
		si += amount;
		setSi(i, si);
	}

	private void subsSi(int i, double amount) {
		double si = this.si.get(i);
		si -= amount;
		setSi(i, si);
	}

	private void appendSi(double amount) {
		this.si.add(amount);
	}
	
	private void removeSi(int i) {
		this.si.remove(i);
	}
	
}