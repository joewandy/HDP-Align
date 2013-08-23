package mzmatch.ipeak.sort;

import peakml.math.Signal;

public class SignalsAndRetentionTimes {
	public final Signal[][] signals;
	public final double[][] retentionTimes;
	public final String[] ids;

	public SignalsAndRetentionTimes(final Signal[][] signals, final double[][] retentionTimes, final String[] ids ) {
		this.signals = signals;
		this.retentionTimes = retentionTimes;
		this.ids = ids;
	}
}