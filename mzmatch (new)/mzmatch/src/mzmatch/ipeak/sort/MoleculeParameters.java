package mzmatch.ipeak.sort;

import java.io.PrintStream;

import mzmatch.ipeak.sort.MetAssign.TestOptions;

public class MoleculeParameters extends FormulaParameters {
	public final double retentionTimePredictSD;
	
	public MoleculeParameters(final double rtWindow, final double p1, final double p0, final double alpha,
			final int numSamples, final int burnIn, final boolean debug, final PrintStream out, final double minDistributionValue,
			final int maxValues, final double massPrecisionPPM, final double rho, final double retentionTimeSD,
			final double retentionTimePredictSD, final TestOptions testOptions) {
		super(rtWindow, p1, p0, alpha, numSamples, burnIn, debug, out, minDistributionValue, maxValues,
			massPrecisionPPM, rho,retentionTimeSD, testOptions);
		this.retentionTimePredictSD = retentionTimePredictSD;
	}
}
