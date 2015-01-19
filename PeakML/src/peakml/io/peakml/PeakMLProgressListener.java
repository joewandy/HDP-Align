package peakml.io.peakml;


// java

// peakml
import peakml.*;
import peakml.io.*;





/**
 * 
 */
public interface PeakMLProgressListener
{
	public void onHeader(Header header);
	public void onIPeak(IPeak peak);
	public void onFinish();
}
