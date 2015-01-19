package com.joewandy.alignmentResearch.rtPrediction;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.distribution.NormalDistribution;

import peakml.chemistry.Molecule;

import com.joewandy.alignmentResearch.alignmentExperiment.dataGenerator.GenerativeModelParameter;
import com.joewandy.alignmentResearch.model.Feature;
import com.joewandy.alignmentResearch.model.GenerativeFeatureGroup;
import com.joewandy.alignmentResearch.model.GenerativeMolecule;

public class RTPredictor {

	private GenerativeModelParameter params;
	private Map<String, Double> molPredResults;
	
	public RTPredictor(GenerativeModelParameter params) {
		this.params = params;
		this.molPredResults = new HashMap<String, Double>();
	}

	/**
	 * Predict RT for a molecule
	 * @param molecule the molecule
	 * @return the predicted RT
	 */
	public double predict(Molecule molecule) {
		String key = molecule.getDatabaseID();
		if (molPredResults.containsKey(key)) {
			// to make sure that we return the same results for each molecule every time
			double rt = molPredResults.get(key);
			return rt;
		} else {
			double mean = params.getD();
			double stdev = params.getE();
			NormalDistribution dist = new NormalDistribution(mean, stdev);
			double rt = dist.sample();
			molPredResults.put(key, rt);
			return rt;			
		}
	}

	/**
	 * Predict RT for a cluster in replicate
	 * @param group the cluster
	 * @param replicate the replicate index
	 * @return the predicted RT
	 */
	public double predict(GenerativeFeatureGroup group, int replicate) {
		
		RetentionTimeWarping warping = new RetentionTimeWarping(params);
		
		// get parent's predicted RT
		GenerativeMolecule parent = group.getParent();
		double originalRT = parent.getPredictedRT();
		
		// warp it
		double warpedRT = warping.getWarpedRT(originalRT, replicate);

		double mean = warpedRT;
		double stdev = params.getSigma_c();
		NormalDistribution dist = new NormalDistribution(mean, stdev);
		double rt = dist.sample();
		return rt;
		
	}
	
	public double predict(Feature feature, Map<Integer, GenerativeFeatureGroup> groupIDMap) {
		
		// get the cluster's predicted RT
		Integer groupID = feature.getGroupID();
		GenerativeFeatureGroup cluster = groupIDMap.get(groupID);
		double clusterRT = cluster.getWarpedRT();
		
		double mean = clusterRT;
		double stdev = params.getSigma_t();
		NormalDistribution dist = new NormalDistribution(mean, stdev);
		double rt = dist.sample();
		return rt;
		
	}
	
}
