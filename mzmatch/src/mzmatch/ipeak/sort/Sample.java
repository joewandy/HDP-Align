package mzmatch.ipeak.sort;

import peakml.util.Pair;

public class Sample<T> extends Pair<T,Double> {
	Sample(final T sample, final double probability) {
		super(sample, probability);
	}
	
	public T sample() {
		return this.v1;
	}
	
	public double probability() {
		return this.v2;
	}
}
