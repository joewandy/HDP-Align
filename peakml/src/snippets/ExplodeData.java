package snippets;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Vector;
import java.util.zip.GZIPOutputStream;

import peakml.Centroid;
import peakml.IPeak;
import peakml.IPeakSet;
import peakml.MassChromatogram;
import peakml.PeakData;
import peakml.io.Header;
import peakml.io.ParseResult;
import peakml.io.peakml.PeakMLParser;
import peakml.io.peakml.PeakMLWriter;

@SuppressWarnings("unchecked")
public class ExplodeData
{
	public static IPeak duplicate(IPeak peak)
	{
		if (peak.getClass().equals(MassChromatogram.class))
		{
			MassChromatogram<Centroid> mc = (MassChromatogram<Centroid>) peak;
			MassChromatogram<Centroid> cpy = new MassChromatogram<Centroid>(new PeakData<Centroid>(mc.getPeakData()));
			cpy.setMeasurementID(mc.getMeasurementID());
			return cpy;
		}
		else if (peak.getClass().equals(IPeakSet.class))
		{
			IPeakSet<IPeak> peakset = (IPeakSet<IPeak>) peak;
			Vector<IPeak> peaks = new Vector<IPeak>();
			for (IPeak p : peakset)
				peaks.add(duplicate(p));
			return new IPeakSet<IPeak>(peaks);
		}
		else
		{
			System.err.println("Unexpected class-type: " + peak.getClass().getName());
			System.exit(0);
		}
		
		return null;
	}
	
	public static void unpack(Vector<IPeak> packed, Vector<IPeak> unpacked)
	{
		for (IPeak peak : packed)
		{
			if (peak.getClass().equals(MassChromatogram.class))
				unpacked.add(peak);
			else if (peak.getClass().equals(IPeakSet.class))
			{
				unpack(((IPeakSet<IPeak>) peak).getPeaks(), unpacked);
			}
			else
			{
				System.err.println("Unexpected class-type: " + peak.getClass().getName());
				System.exit(0);
			}
		}
	}
	
	public static void main(String args[])
	{
		try
		{
			int mul = 10;
			
			// load the data
			System.out.println("loading data");
			ParseResult result = PeakMLParser.parse(new FileInputStream("E:/saskia/allpeaks_selection_nomedium_related.peakml"), true);
			
			Header header = result.header;
			IPeakSet<IPeak> peaks = (IPeakSet<IPeak>) result.measurement; 
			
			
			// 
			System.out.println("exploding data");
			Vector<IPeak> dataset = new Vector<IPeak>();
			
			for (IPeak peak : peaks)
			{
				Vector<IPeak> duplicates = new Vector<IPeak>();
				for (int i=0; i<mul; ++i)
					duplicates.add(duplicate(peak));
				
				Vector<IPeak> unpacked = new Vector<IPeak>();
				unpack(duplicates, unpacked);
				dataset.add(new IPeakSet<IPeak>(unpacked));
			}
			
			// write the data
			System.out.println("writing data");
			PeakMLWriter.write(header, dataset, null, new GZIPOutputStream(new FileOutputStream("E:/saskia/memtest_x10.peakml")), null);
			
			// add measurements and sets to the header
//			Vector<SetInfo> sets = new Vector<SetInfo>(header.getSetInfos());
//			Vector<MeasurementInfo> measurements = new Vector<MeasurementInfo>(header.getMeasurementInfos());
//			for (int i=0; i<mul; ++i)
//			{
//				for (SetInfo set : sets)
//				{
//					SetInfo s = new SetInfo(set);
//					for (Integer mid : s.getAllMeasurementIDs())
//						mid = mid
//				}
//			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
