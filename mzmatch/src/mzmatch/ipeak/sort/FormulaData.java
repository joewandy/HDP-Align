package mzmatch.ipeak.sort;

import mzmatch.ipeak.util.GeneralMassSpectrumDatabase;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.io.Header;

public class FormulaData extends Data {
	public final GeneralMassSpectrumDatabase theoreticalSpectrums;
	public final int numFormulae;
	
	public FormulaData(final Header header, final IPeakSet<IPeak> peakset, final GeneralMassSpectrumDatabase theoreticalSpectrums) {
		super(header, peakset);
		this.theoreticalSpectrums = theoreticalSpectrums;
		numFormulae = theoreticalSpectrums.size();
	}
}
