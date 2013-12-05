package mzmatch.ipeak.sort;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.qsar.descriptors.molecular.ALOGPDescriptor;
import org.openscience.cdk.qsar.result.IDescriptorResult;

public class RetentionTimeCalculation {
	final ALOGPDescriptor logp;
	
	public RetentionTimeCalculation() {
		try {
			logp = new ALOGPDescriptor();
		} catch (CDKException e) {
			// WHY would a default constructor throw an exception!
			throw new RuntimeException(e);
		}
	}
	/*
	public double calculateOctWatPartCoef(final Molecule m) {
		final DescriptorValue v = logp.calculate(m);
		final IDescriptorResult result = v.getValue();
		//result.
	}
	*/
}
