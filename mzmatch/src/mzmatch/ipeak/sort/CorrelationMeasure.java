package mzmatch.ipeak.sort;

import peakml.math.Signal;

public interface CorrelationMeasure {
	double correlation(Signal signal1, Signal signal2);
}
