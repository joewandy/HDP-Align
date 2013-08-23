package mzmatch.ipeak.sort;

public class CorrelationParameters extends Parameters {
	public final int initialNumClusters;
	
	public CorrelationParameters(final double rtWindow, final double p1, final double p0, final double alpha,
			final int numSamples, final int burnIn, final double retentionTimeSD, final boolean debug, final int initialNumClusters) {
		super(rtWindow, p1, p0, alpha, numSamples, burnIn, retentionTimeSD, debug, System.err);
		this.initialNumClusters = initialNumClusters;
	}
}
