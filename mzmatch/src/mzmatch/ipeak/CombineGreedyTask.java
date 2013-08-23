package mzmatch.ipeak;

import java.util.List;
import java.util.Vector;

import peakml.IPeak;
import peakml.IPeakSet;

public class CombineGreedyTask extends CombineBaseTask implements CombineTask {

	@Override
	protected List<IPeakSet<IPeak>> getMatches(
			Vector<IPeakSet<IPeak>> peaksets, double massTolerance,
			double rtTolerance) {
		List<IPeakSet<IPeak>> matches = IPeak.match(peaksets, massTolerance, 
				new PeakMatchCompare<IPeak>(rtTolerance));						
		return matches;
	}
	
	
}
