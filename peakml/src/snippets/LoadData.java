package snippets;

import java.io.FileInputStream;

import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;

@SuppressWarnings("unchecked")
public class LoadData
{
	public static int count(IPeak peak)
	{
		if (peak.getClass().equals(MassChromatogram.class))
		{
			return 1;
		}
		else if (peak.getClass().equals(IPeakSet.class))
		{
			int size = 0;
			for (IPeak p : (IPeakSet<IPeak>) peak)
				size += count(p);
			return size;
		}
		else
		{
			System.err.println("Unexpected class-type: " + peak.getClass().getName());
			System.exit(0);
		}
		
		return Integer.MIN_VALUE;
	}
	
	public static void main(String args[])
	{
		try
		{
			// base count
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem0 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			// load the data
			long t = System.currentTimeMillis();
			ParseResult result = PeakMLParser.parse(
					new FileInputStream("F:/redo/saskia/memtest_x10.peakml"), 
					true
				);
			// "F:/redo/saskia/memtest_x10.peakml"
			System.out.println((System.currentTimeMillis()-t) / 1000.);
			
			// base count
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			System.gc(); System.gc(); System.gc(); System.gc();
			long mem1 = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
			
			System.out.println("memory used: " + (mem1-mem0) + " bytes (" + (mem1-mem0)/(1024*1024) + "Mb)");
			
			System.out.println(result.header.getNrMeasurementInfos() + ", " + count((IPeakSet<IPeak>) result.measurement));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
