package snippets;

// java
import java.io.*;

// peakml
import peakml.*;

import peakml.io.*;
import peakml.io.peakml.*;



public class TestData
{
	public static void main(String args[])
	{
		try
		{
			// load the data
			ParseResult result = PeakMLParser.parse(
					new FileInputStream("F:/test.peakml"),
					true
				);
			
			// we know this is a file with mass chromatograms
			IPeakSet<MassChromatogram<Peak>> mcs =
				(IPeakSet<MassChromatogram<Peak>>) result.measurement;
			
			// go through the mass chromatograms
			for (MassChromatogram<Peak> mc : mcs)
			{
				// print the mass
				System.out.print(mc.getMass());
				
				// print the intensity values
				for (Peak peak : mc)
					System.out.print("\t" + peak.getIntensity());
				System.out.println();
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
