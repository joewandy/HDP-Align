package mzmatch.experimental;

import java.util.Comparator;

import peakml.IPeak;


/**
 * Compares unique peaks when combining them across different clusters
 * @author joewandy
 *
 */
public class CombinePeakComparator implements Comparator<IPeak> {

	public int compare(IPeak p1, IPeak p2) {

		int i = Double.compare(p1.getMass(), p2.getMass());
		if (i != 0) {
			return i;
		}
		
		i = Double.compare(p1.getRetentionTime(), p2.getRetentionTime());
		if (i != 0) {
			return i;
		}
		
		return Double.compare(p1.getIntensity(), p2.getIntensity());

	}

}
