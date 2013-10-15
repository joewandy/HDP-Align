package com.joewandy.alignmentResearch.objectModel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import peakml.chemistry.PeriodicTable;

import com.joewandy.alignmentResearch.alignmentMethod.custom.FeaturePairKey;
import com.joewandy.alignmentResearch.alignmentMethod.custom.RowPairKey;
import com.joewandy.alignmentResearch.main.FeatureXMLAlignment;

import edu.uci.ics.jung.graph.Graph;

public class MatchingScorer {

	private double dmz;
	private double drt;
	
	public MatchingScorer(double dmz, double drt) {
		this.dmz = dmz;
		this.drt = drt;
	}
	
	public double computeScore(AlignmentRow row1, AlignmentRow row2) {
		double dist = computeDist(row1, row2);
		double score = 1/dist;
		return score;
	}
	
	public double computeScore(Feature f1, Feature f2) {
		double dist = computeDist(f1, f2);
		double score = 1/dist;
		return score;			
	}
	
	public double computeGraphScore(Feature f1, Feature f2,
			Graph<AlignmentVertex, AlignmentEdge> graph) {
		double dist = computeDist(f1, f2);
		double weight = computeGraphWeight(f1, f2, graph);
		double score = weight * (1/dist);
		return score;			
	}

	public double computeProbScore(Feature f1, Feature f2) {
		double dist = computeDist(f1, f2);
		double weight = computeProbWeight(f1, f2);
		double score = weight * (1/dist);
		return score;			
	}
	
	private double computeDist(Feature f1, Feature f2) {

		double mass1 = f1.getMass();
		double mass2 = f2.getMass();
		double rt1 = f1.getRt();
		double rt2 = f2.getRt();
		DistanceCalculator calc = new MahalanobisDistanceCalculator(dmz, drt);
		double dist = calc.compute(mass1, mass2, rt1, rt2);		
		return dist;

	}
	
	public double computeDist(AlignmentRow row1, AlignmentRow row2) {
		
		double mass1 = row1.getAverageMz();
		double mass2 = row2.getAverageMz();
		double rt1 = row1.getAverageRt();
		double rt2 = row2.getAverageRt();
		DistanceCalculator calc = new MahalanobisDistanceCalculator(dmz, drt);
		double dist = calc.compute(mass1, mass2, rt1, rt2);						
		return dist;

	}	
	
	private double computeGraphWeight(Feature f1, Feature f2, 
			Graph<AlignmentVertex, AlignmentEdge> graph) {
		AlignmentVertex v1 = f1.getVertex();
		AlignmentVertex v2 = f2.getVertex();
		AlignmentEdge existing = graph.findEdge(
				v1, v2);
		if (existing != null) {
			double weight = existing.getWeight();
			return weight;
		} else {
			return 1;
		}
	}

	private double computeProbWeight(Feature fi, Feature fj) {
		double weight = 0;
		int fiIdx = fi.getPeakID();
		int fjIdx = fj.getPeakID();
		double[][] ZZProb1 = fi.getZZProb();
		double[][] ZZProb2 = fj.getZZProb();

		for (Feature fm : getNearbyFeatures(fi, dmz)) {
			for (Feature fn : getNearbyFeatures(fj, dmz)) {
													
				int fmIdx = fm.getPeakID();
				int fnIdx = fn.getPeakID();
				
				double prob1 = ZZProb1[fiIdx][fmIdx];
				double prob2 = ZZProb2[fjIdx][fnIdx];
				double pairWeight = prob1 * prob2;
				weight += pairWeight;

			}
		}
		return weight;
	}
	
	private Set<Feature> getNearbyFeatures(Feature feature, double dmz) {
		Set<Feature> result = new HashSet<Feature>();
		for (Feature neighbour : feature.getData().getFeatures()) {
			if (checkInMassRange(feature, neighbour, dmz)) {
				result.add(neighbour);
			}
		}
		return result;
	}	
	
	private boolean checkInMassRange(Feature feature, Feature friend, double massTolerance) {
		boolean inRange = false;
		double delta = 0;
		if (FeatureXMLAlignment.ALIGN_BY_RELATIVE_MASS_TOLERANCE) {
			delta = PeriodicTable.PPM(feature.getMass(), massTolerance);			
		} else {
			delta = massTolerance;			
		}
		double massLower = feature.getMass() - delta/2;
		double massUpper = feature.getMass() + delta/2;
		if (friend.getMass() > massLower && friend.getMass() < massUpper) {
			inRange = true;
		}
		return inRange;
	}
		
}
