package mzmatch.ipeak.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.primitives.Doubles;

public class NewMassSpectrum {
	private final Map<Double,Double> map = new HashMap<Double,Double>();
	private final int maxEntries;
	
	public NewMassSpectrum() {
		this(Integer.MAX_VALUE);
	}
	
	public NewMassSpectrum(final int maxEntries) {
		this.maxEntries = maxEntries;
	}
	
	public void add(final double mass, final double intensity) {
		assert map.size() <= maxEntries;
		map.put(mass, intensity);
		if ( map.size() == maxEntries + 1 ) {
			final Collection<Entry<Double,Double>> entries = map.entrySet();
			Double smallestKey = null;
			Double smallestValue = Double.MAX_VALUE;
			
			for (Entry<Double,Double> entry : entries) {
				if ( entry.getValue() < smallestValue ) {
					smallestKey = entry.getKey();
					smallestValue = entry.getValue();
				}
			}
			assert smallestKey != null;
			map.remove(smallestKey);
			assert map.size() == maxEntries;
		}
		assert map.size() <= maxEntries;
	}
	
	public double[] getMasses() {
		return Doubles.toArray(map.keySet());
	}
	
	public double[] getDistribution() {
		return Doubles.toArray(map.values());
	}
	
	public String toString() {
		return Common.join(map.values(), " ") + "\n" + Common.join(map.keySet(), " ");
	}
}
