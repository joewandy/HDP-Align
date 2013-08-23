package mzmatch.ipeak.sort;

import java.io.PrintStream;

public class Parameters {
	final double rtWindow;
	final double p1;
	final double p0;
	final double alpha;
	final int numSamples;
	final int burnIn;
	final double retentionTimeSD;
	public final boolean debug;
	public final PrintStream out;
	
	public Parameters(final double rtWindow, final double p1, final double p0, final double alpha,
			final int numSamples, final int burnIn, final double retentionTimeSD, final boolean debug, final PrintStream out) {
		this.rtWindow = rtWindow;
		this.p1 = p1;
		this.p0 = p0;
		this.alpha = alpha;
		this.numSamples = numSamples;
		this.burnIn = burnIn;
		this.retentionTimeSD = retentionTimeSD;
		this.debug = debug;
		this.out = out;
	}
}
