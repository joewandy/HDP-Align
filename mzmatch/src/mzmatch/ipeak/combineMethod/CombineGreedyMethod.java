package mzmatch.ipeak.combineMethod;

import java.util.List;

import mzmatch.ipeak.Combine.Options;
import peakml.IPeak;
import peakml.IPeak.MatchCompare;
import peakml.IPeakSet;
import peakml.math.Signal;

public class CombineGreedyMethod extends CombineBaseMethod implements CombineMethod {

	@Override
	protected List<IPeakSet<IPeak>> getMatches(
			List<IPeakSet<IPeak>> peaksets, Options options) {
		
		List<IPeakSet<IPeak>> matches = IPeak.match(peaksets, options.ppm, 
				new PeakMatchCompare<IPeak>(options.rtwindow));						
		return matches;

	}
	
	private class PeakMatchCompare<T extends IPeak> implements MatchCompare<T> {

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
