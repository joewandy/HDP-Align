package snippets;

import java.util.*;
import peakml.*;


public class PeakDataUsage
{
	public static class MC<gPeak extends Peak> implements Iterable<gPeak>
	{
		public MC(PeakData<gPeak> peakdata)
		{
			this.peakdata = peakdata;
		}
		public Iterator<gPeak> iterator()
		{
			return peakdata.iterator();
		}
		protected PeakData<gPeak> peakdata;
	}
	
	public static void main(String args[])
	{
		try
		{
			// create a PeakData instance for 100 trace points
			PeakData<Centroid> peakdata = new PeakData<Centroid>(new Centroid.Factory(), 100);
			
			// fill the peakdata instance with a for-loop
			for (int i=0; i<peakdata.size(); ++i)
			{
				peakdata.set(
						i, // index in the peakdata arrays
						i, // scanid
						i, // retentiontime
						i, // mass
						i  // intensity
					);
			}
			
			// create a PeakData instance from already allocated arrays
			int scanids[] = new int[100];
			int patternids[] = new int[100];
			int measurementids[] = new int[100];
			double retentiontimes[] = new double[100];
			double masses[] = new double[100];
			double intensities[] = new double[100];
			
			PeakData<Centroid> peakdata2 = new PeakData<Centroid>(
					new Centroid.Factory(), 100,
					scanids,
					patternids,
					measurementids,
					retentiontimes,
					masses,
					intensities
				);
			
			// iterate through the data
			for (Centroid c : peakdata)
				System.out.println(c.getMass() + " - " + c.getIntensity());
			
			// create the MC instance and iterate it
			MC<Centroid> mc = new MC<Centroid>(peakdata);
			for (Centroid c : mc)
				System.out.println(c.getMass() + " - " + c.getIntensity());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
