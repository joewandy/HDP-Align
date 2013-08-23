package snippets.list;

// java
import java.util.*;

// peakml
import peakml.*;



public class SimpleLists
{
	public static void main(String args[])
	{
		Vector<IPeak> peaks = new Vector<IPeak>();
		
		// sorting on IPeak properties (descending analogous)
		Collections.sort(peaks, IPeak.sort_mass_ascending);
		Collections.sort(peaks, IPeak.sort_scanid_ascending);
		Collections.sort(peaks, IPeak.sort_intensity_ascending);
		Collections.sort(peaks, IPeak.sort_retentiontime_ascending);
		
		// sorting on annotations
		Collections.sort(peaks, new IPeak.AnnotationAscending("codadw"));
		
		// 
		IPeak rtmatch = IPeak.getBestPeakOnRT(peaks, 40);
		IPeak massmatch = IPeak.getBestPeakOnMass(peaks, 180);
	}
}
