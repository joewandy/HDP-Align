package mzmatch.ipeak.sort;

import java.io.*;
import java.util.*;

import peakml.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;

public class IdentifyPeaksets {
	public static void process(Header header, IPeakSet<IPeak> peak) throws Exception {
		//Vector<IPeakSet<IPeak>> peakSets = new Vector<IPeakSet<IPeak>>();
		identify(peak);
		Vector<IPeak> peakSets = peak.getPeaks();

		PeakMLWriter.write(header, peakSets, null, System.out, null);
	}
	
	public static void identify(IPeakSet<IPeak> peak) {
		int i = 1;
		for (IPeak p : peak) {
			p.addAnnotation("id", i++);
		}
	}

	public static void main(String args[]) {
		try {
			String inputFile = args[0];
			ParseResult result = PeakMLParser.parse(new FileInputStream(inputFile), true);
			Header header = result.header;
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) result.measurement;
			
			// determine the container type
			if (peakset.getContainerClass().equals(IPeakSet.class))
				process(header, peakset);
			else if (peakset.getContainerClass().equals(BackgroundIon.class))
				throw new Exception("Wrong type of container!");
			else if (peakset.getContainerClass().equals(MassChromatogram.class))
				process(header, peakset);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
