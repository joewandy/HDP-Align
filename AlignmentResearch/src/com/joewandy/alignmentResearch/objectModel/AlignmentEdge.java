package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.List;


public class AlignmentEdge {

	private double weight;
	private AlignmentVertex left;
	private AlignmentVertex right;
	private String leftId;
	private String rightId;
	private List<AlignmentPair> pairs;
	
	public AlignmentEdge(AlignmentVertex vertexLeft, AlignmentVertex vertexRight, AlignmentPair pair) {
		
		this.left = vertexLeft;
		this.right = vertexRight;
		this.weight = 1;
		this.leftId = left.getId();
		this.rightId = right.getId();
		
		pairs = new ArrayList<AlignmentPair>();
		pairs.add(pair);

	}
	
	public String getId() {
		return "[" + leftId + "-" + rightId + "]";
	}
	
	public double getWeight() {
		return weight;
	}
	
	public double getProbWeight(Feature f1, Feature f2) {

		assert(f1.getData() != f2.getData());

		double allWeight = 0;
		double[][] ZZProb1 = f1.getZZProb();
		double[][] ZZProb2 = f2.getZZProb();
		int f1Idx = f1.getPeakID();
		int f2Idx = f2.getPeakID();
		for (AlignmentPair pair : pairs) {
			
			Feature n1 = pair.getFeature1();
			Feature n2 = pair.getFeature2();
			if (f1.getData() != n1.getData()) {
				n1 = pair.getFeature2();
				n2 = pair.getFeature1();
			}

			assert(f1.getData() == n1.getData());
			assert(f2.getData() == n2.getData());
			
			int n1Idx = n1.getPeakID();
			int n2Idx = n2.getPeakID();
			double prob1 = ZZProb1[f1Idx][n1Idx];
			double prob2 = ZZProb2[f2Idx][n2Idx];
			double pairWeight = prob1 * prob2;
			allWeight += pairWeight;
			
		}
		return allWeight;
	}
	
	public void addWeight(double increment) {
		this.weight += increment;
	}

	public void incrementWeight() {
		this.addWeight(1);
	}
	
	public void addAlignmentPair(List<AlignmentPair> pair) {
		pairs.addAll(pair);
	}

	public void updateAlignmentPairScore() {
		for (AlignmentPair pair : pairs) {
			pair.setScore(this.getWeight());
		}
	}
	
	public List<AlignmentPair> getAlignmentPairs() {
		for (AlignmentPair pair : pairs) {
			pair.setScore(this.weight);
		}
		return pairs;
	}
	
	public AlignmentVertex getLeft() {
		return left;
	}

	public AlignmentVertex getRight() {
		return right;
	}
	
	public int getLeftGroupSize() {
		if (!this.left.getPeaks().isEmpty()) {
			return this.left.getPeaks().size();			
		} else {
			return this.left.getFeatures().size();
		}
	}

	public int getRightGroupSize() {
		if (!this.right.getPeaks().isEmpty()) {
			return this.right.getPeaks().size();			
		} else {
			return this.right.getFeatures().size();
		}
	}
	
	public int getTotalGroupSize() {
		return getLeftGroupSize() + getRightGroupSize();
	}
	
	public double getIntensitySumSquareError() {
		double sqError = 0;
		for (AlignmentPair pair : pairs) {
			sqError += pair.getIntensitySquareError();
		}
		return sqError;
	}
	
	public double getIntensityMeanSquareError() {
		double result = this.getIntensitySumSquareError() / this.pairs.size();
		return result;
	}

	public double getIntensityRootMeanSquareError() {
		double result = Math.pow(this.getIntensityMeanSquareError(), 0.5);
		return result;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((leftId == null) ? 0 : leftId.hashCode());
		result = prime * result + ((rightId == null) ? 0 : rightId.hashCode());
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
		AlignmentEdge other = (AlignmentEdge) obj;
		if (leftId == null) {
			if (other.leftId != null)
				return false;
		} else if (!leftId.equals(other.leftId))
			return false;
		if (rightId == null) {
			if (other.rightId != null)
				return false;
		} else if (!rightId.equals(other.rightId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "E" + "[weight=" + weight + ":" + left.toString() + "-" + right.toString() + "]";
	}
	
}
