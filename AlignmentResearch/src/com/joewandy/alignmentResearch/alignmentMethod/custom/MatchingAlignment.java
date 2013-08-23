package com.joewandy.alignmentResearch.alignmentMethod.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.SummaryStatistics;

import com.joewandy.alignmentResearch.alignmentMethod.AlignmentMethod;
import com.joewandy.alignmentResearch.alignmentMethod.BaseAlignment;
import com.joewandy.alignmentResearch.objectModel.AlignmentFile;
import com.joewandy.alignmentResearch.objectModel.AlignmentList;
import com.joewandy.alignmentResearch.objectModel.Feature;

public class MatchingAlignment extends BaseAlignment implements AlignmentMethod {

	private Map<Key, Double> featurePairScores;
	
	/**
	 * Creates a simple aligner
	 * @param dataList List of feature data to align
	 * @param massTolerance Mass tolerance in ppm
	 * @param rtTolerance Retention time tolerance in seconds
	 * @param rtDrift 
	 */
	public MatchingAlignment(List<AlignmentFile> dataList, double massTolerance, double rtTolerance) {		
		super(dataList, massTolerance, rtTolerance);
		featurePairScores = new HashMap<Key, Double>();

	}
	
	@Override
	protected AlignmentList matchFeatures() {
		
		System.out.println("ALIGNING");
		AlignmentList alignedList = new AlignmentList("");
		
		for (AlignmentFile data1 : dataList) {
			for (AlignmentFile data2 : dataList) {
				if (data1 == data2) {
					continue;
				}
				computeFeatureDistance(data1, data2);
			}
		}
		
		return alignedList;
				
	}

	private void computeFeatureDistance(AlignmentFile data1, AlignmentFile data2) {
		
		System.out.print("\tProcessing " + data1.getFilenameWithoutExtension() + 
				" (" + data1.getFeaturesCount() + ") x " + data2.getFilenameWithoutExtension() + 
				" (" + data2.getFeaturesCount() + ") ");
		
		// calculate mean & stdev for file 1
		SummaryStatistics massStats = new SummaryStatistics();
		SummaryStatistics rtStats = new SummaryStatistics();
		for (Feature f : data1.getFeatures()) {
			massStats.addValue(f.getMass());
			rtStats.addValue(f.getRt());
		}
		double massMean1 = massStats.getMean();
		double massStd1 = massStats.getStandardDeviation();
		double rtMean1 = rtStats.getMean();
		double rtStd1 = rtStats.getStandardDeviation();		

		// calculate mean & stdev for file 2
		SummaryStatistics massStats2 = new SummaryStatistics();
		SummaryStatistics rtStats2 = new SummaryStatistics();
		for (Feature f : data2.getFeatures()) {
			massStats2.addValue(f.getMass());
			rtStats2.addValue(f.getRt());
		}
		double massMean2 = massStats2.getMean();
		double massStd2 = massStats2.getStandardDeviation();
		double rtMean2 = rtStats2.getMean();
		double rtStd2 = rtStats2.getStandardDeviation();		
		
		int counter = 0;
		for (Feature f1 : data1.getFeatures()) {
			for (Feature f2 : data2.getFeatures()) {

				if (counter % 5000000 == 0) {
					System.out.print('.');
				}
				
				double score = computeSimilarity(massMean1, massStd1,
						rtMean1, rtStd1, massMean2, massStd2, rtMean2, rtStd2,
						f1, f2);

				Key key = new Key(f1, f2);
				featurePairScores.put(key, score);
				
				counter++;
				
			}
		}
		System.out.println();
		
	}
	
	private double computeSimilarity(double massMean1, double massStd1,
			double rtMean1, double rtStd1, double massMean2, double massStd2,
			double rtMean2, double rtStd2, Feature f1, Feature f2) {

		double f1MassZScore = (f1.getMass() - massMean1) / massStd1;
		double f2MassZScore = (f2.getMass() - massMean2) / massStd2;
		double f1RtZScore = (f1.getRt() - rtMean1) / rtStd1;
		double f2RtZScore = (f2.getRt() - rtMean2) / rtStd2;				
		
		double massDist = Math.pow(f1MassZScore-f2MassZScore, 2);
		double rtDist = Math.pow(f1RtZScore-f2RtZScore, 2);
		double euclideanDist = Math.sqrt(massDist + rtDist);
		double similarity = 1/(1+euclideanDist);
		return similarity;
	
	}
	
	private class Key {
		private Feature f1;
		private Feature f2;
		public Key(Feature f1, Feature f2) {
			this.f1 = f1;
			this.f2 = f2;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result + ((f1 == null) ? 0 : f1.hashCode());
			result = prime * result + ((f2 == null) ? 0 : f2.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (f1 == null) {
				if (other.f1 != null)
					return false;
			} else if (!f1.equals(other.f1))
				return false;
			if (f2 == null) {
				if (other.f2 != null)
					return false;
			} else if (!f2.equals(other.f2))
				return false;
			return true;
		}
		private MatchingAlignment getOuterType() {
			return MatchingAlignment.this;
		}
	}
		
}