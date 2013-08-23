package mzmatch.ipeak.sort;

import java.io.PrintStream;

public class FormulaParameters extends Parameters {
	final double minDistributionValue;
	final int maxValues;
	final double massPrecisionPPM;
	final double rho;
	
	public FormulaParameters(final double rtWindow, final double p1, final double p0, final double alpha,
			final int numSamples, final int burnIn, final boolean debug, final PrintStream out, final double minDistributionValue, final int maxValues,
			final double massPrecisionPPM, final double rho, final double retentionTimeSD) {
		super(rtWindow, p1, p0, alpha, numSamples, burnIn, retentionTimeSD, debug, out);
		this.minDistributionValue = minDistributionValue;
		this.maxValues = maxValues;
		this.massPrecisionPPM = massPrecisionPPM;
		this.rho = rho;
	}
}
