package mzmatch.ipeak.sort;

import java.util.Vector;

import mzmatch.ipeak.util.Common;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.Peak;
import peakml.chemistry.Polarity;
import peakml.io.Header;
import peakml.math.Signal;

public class Data {
	public final int numReplicates;
	public final int numPeaksets;
	public final double[][] masses;
	public final double[][] intensities;
	public final Polarity[] polarities;
	public final Signal[][] signals;
	public final double[][] retentionTimes;
	public final String[] ids;

	public Data(final Header header, final IPeakSet<IPeak> peakset) {
		numReplicates = header.getNrMeasurementInfos();
		numPeaksets = peakset.size();

		masses = new double[numReplicates][numPeaksets];
		intensities = new double[numReplicates][numPeaksets];
		polarities = new Polarity[numPeaksets];
		signals = new Signal[numReplicates][numPeaksets];
		retentionTimes = new double[numReplicates][numPeaksets];
		ids = new String[numPeaksets];

		
		final Vector<IPeak> peaks = peakset.getPeaks();
		for (int j = 0; j < numPeaksets; ++j) {
			final IPeak peak1 = peaks.get(j);
			if ( peakset.getContainerClass().equals(MassChromatogram.class) ) {
				masses[0][j] = Math.log(peak1.getMass());
				intensities[0][j] = peak1.getIntensity();
				polarities[j] = Common.getPeakPolarity(peak1, header);
				@SuppressWarnings("unchecked")
				MassChromatogram<Peak> mc = (MassChromatogram<Peak>)peak1;
				signals[0][j] = mc.getSignal();
				retentionTimes[0][j] = peak1.getRetentionTime();
				ids[j] = Common.getId(peak1);
			} else {
				@SuppressWarnings("unchecked")
				final IPeakSet<MassChromatogram<Peak>> peakset1 = (IPeakSet<MassChromatogram<Peak>>) peak1;
				for (int i = 0; i < numReplicates; ++i) {
					final IPeak mc1 = ShapeCorrelations.getPeak(peakset1, header, i);
					intensities[i][j] = mc1 == null ? Double.NaN : mc1.getIntensity();
					masses[i][j] = mc1 == null ? Double.NaN : Math.log(mc1.getMass());
					signals[i][j] = mc1 == null ? null : mc1.getSignal();
					retentionTimes[i][j] = mc1 == null ? Double.NaN : peak1.getRetentionTime();
				}
				polarities[j] = Common.getPeakPolarity(peakset1.get(0), header);
				ids[j] = Common.getId(peakset1);
			}
		}
	}
	
	public boolean isMissing(final int rep, final int peak) {
		return Double.isNaN(masses[rep][peak]);
	}
	
	public String toString() {
		return "numReplicates: " + numReplicates + " numPeaksets: " + numPeaksets;
	}
}
