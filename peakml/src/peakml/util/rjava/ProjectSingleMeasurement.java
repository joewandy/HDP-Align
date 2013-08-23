/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakml.util.rjava;


// java
import java.io.*;

import java.util.*;
import java.util.zip.*;

// libraries
import domsax.*;

// peakml
import peakml.*;
import peakml.chemistry.*;

import peakml.io.*;
import peakml.io.peakml.*;
import peakml.math.Statistical;
import peakml.util.DataTypes;





/**
 * 
 */
@SuppressWarnings("unchecked")
public class ProjectSingleMeasurement
{
	// constructor(s)
	public ProjectSingleMeasurement()
	{
	}
	
	public ProjectSingleMeasurement(String measurements, String filenames)
	{
		
		// create the peaksets vector
		peaksets = new Vector<IPeakSet<MassChromatogram<Centroid>>>();
		masschromatograms = new Vector<MassChromatogram<Centroid>>();
		
		// create the header
		header = new Header();
		
		// add the measurements to the header
		
			File file = new File(filenames);
			FileInfo fileinfo = new FileInfo(file.getName(), file.getName());
			fileinfo.setLocation(file.getParent());
			
			MeasurementInfo measurementinfo = new MeasurementInfo(0, measurements);
			measurementinfo.addFileInfo(fileinfo);
			
			header.addMeasurementInfo(measurementinfo);
		
		
		// add the sets
		
			//SetInfo set = null;
			//if (set == null)
			//{
			//	set = new SetInfo(sets, SetInfo.SET);
			//	header.addSetInfo(set);
			//}
			
			//set.addMeasurementID(0);
		
	}
	
	// header interface
	public void addHeaderAnnotation(String label, String value)
	{
		header.addAnnotation(label, value);
	}
	
	public String getHeaderAnnotation(String label)
	{
		Annotation annotation = header.getAnnotation(label);
		if (annotation == null)
			return null;
		return annotation.getValue();
	}
	
	public void addScanInfo(int measurementid, double retentiontime, String polarity)
	{
		Polarity p = Polarity.NEUTRAL;
		if (polarity.equals("positive"))
			p = Polarity.POSITIVE;
		else if (polarity.equals("negative"))
			p = Polarity.NEGATIVE;
		header.getMeasurementInfo(measurementid).addScanInfo(new ScanInfo(retentiontime, p));
	}
	
	public void addScanAnnotation(int measurementid, int scanid, String label, String value)
	{
		MeasurementInfo measurementinfo = header.getMeasurementInfo(measurementid);
		ScanInfo scaninfo = measurementinfo.getScanInfo(scanid);
		scaninfo.addAnnotation(label, value);
	}
	
	public String getScanAnnotation(int measurementid, int scanid, String label)
	{
		MeasurementInfo measurementinfo = header.getMeasurementInfo(measurementid);
		ScanInfo scaninfo = measurementinfo.getScanInfo(scanid);
		Annotation annotation = scaninfo.getAnnotation(label);
		if (annotation == null)
			return null;
		return annotation.getValue();
	}
	
	public int getNrScans(int measurementid)
	{
		MeasurementInfo measurementinfo = header.getMeasurementInfo(measurementid);
		return measurementinfo.getNrScanInfos();
	}
	
	public String getIonisation(int measurementid, int scanid)
	{
		MeasurementInfo measurementinfo = header.getMeasurementInfo(measurementid);
		ScanInfo scaninfo = measurementinfo.getScanInfo(scanid);
		
		if (scaninfo.getPolarity() == Polarity.POSITIVE)
			return "positive";
		else if (scaninfo.getPolarity() == Polarity.NEGATIVE)
			return "negative";
		else
			return "neutral";
	}
	
	// write interface
	/**
	 * 
	 * @param measurementid			
	 * @param scanids				
	 * @param retentiontimes		
	 * @param masses				
	 * @param intensities			
	 * @param ionisation			
	 */
	public void addMassChromatogram(int measurementid, int scanids[], double retentiontimes[], double masses[], double intensities[], String ionisation)
	{
		int length = scanids.length;
		if (retentiontimes.length!=length || masses.length!=length || intensities.length!=length)
			throw new RuntimeException("[ERROR]: for 'Project.addMassChromatograms' all arrays need to be off equal length.");
		// calculate the real length
		int reallength = 0;
		for (int i=0; i<length; ++i)
			if (masses[i]!=-1 && intensities[i]!=-1) reallength++;
		
		if (reallength == 0)
		{
			masschromatograms.add(null);
			return;
		}
		
		// create the data container - we copy in order to prevent data corruption due to re-use of the arrays
		int j = 0;
		PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, reallength);
		for (int i=0; i<length; ++i)
			if (masses[i]!=-1 && intensities[i]!=-1)
				peakdata.set(j++, (int) scanids[i], retentiontimes[i], masses[i], intensities[i]);
		
		// create and store the mass chromatogram
		MassChromatogram<Centroid> masschromatogram = new MassChromatogram<Centroid>(peakdata);
		masschromatogram.setMeasurementID(measurementid);
		masschromatograms.add(masschromatogram);
	}
	
	public int getNrMassChromatograms()
	{
		return masschromatograms.size();
	}
	
	public void addPeakSet(int indices)
	{
		addPeakSet(new int[] { indices });
	}
	
	public void addPeakSet(int indices[])
	{
		Vector<MassChromatogram<Centroid>> mcs = new Vector<MassChromatogram<Centroid>>();
		for (int index : indices)
		{
			if (index<0 || index>=masschromatograms.size())
				continue;
			mcs.add(masschromatograms.get(index));
		}
		
		Vector<MassChromatogram<Centroid>> mergedmcs = new Vector<MassChromatogram<Centroid>>();
		for (int measurementid=0; measurementid<header.getNrMeasurementInfos(); ++measurementid)
		{
			// collect all mc's
			int minscan = Integer.MAX_VALUE;
			int maxscan = Integer.MIN_VALUE;
			Vector<MassChromatogram<Centroid>> measurementmcs = new Vector<MassChromatogram<Centroid>>();
			for (MassChromatogram<Centroid> mc : mcs)
				if (mc!=null && mc.getMeasurementID()==measurementid)
				{
					measurementmcs.add(mc);
					minscan = Math.min(minscan, mc.getMinScanID());
					maxscan = Math.max(maxscan, mc.getMaxScanID());
				}
			if (measurementmcs.size() == 0)
				continue;
			
			if (measurementmcs.size() == 1)
			{
				mergedmcs.add(measurementmcs.firstElement());
				continue;
			}
			
			// locate the winning centroids
			Centroid scans[] = new Centroid[maxscan-minscan+1];
			for (int i=0; i<scans.length; ++i)
				scans[i] = null;
			for (MassChromatogram<Centroid> mc : measurementmcs)
				for (Centroid centroid : mc)
				{
					int index = centroid.getScanID() - minscan;
					if (scans[index]==null || centroid.getIntensity()>scans[index].getIntensity())
						scans[index] = centroid;
				}
			
			// create the peakdata and fill
			int size = 0;
			for (int i=0; i<scans.length; ++i)
				if (scans[i] != null) size++;
			
			int i = 0;
			PeakData<Centroid> peakdata = new PeakData<Centroid>(Centroid.factory, size);
			for (Centroid centroid : scans)
			{
				if (centroid != null)
					peakdata.set(i++, centroid.getScanID(), centroid.getRetentionTime(), centroid.getMass(), centroid.getIntensity());
			}
			
			// create the mass chromatogram
			MassChromatogram<Centroid> mc = new MassChromatogram<Centroid>(peakdata);
			mc.setMeasurementID(measurementid);
			
			// there where multiple fragments, gather the annotation data
			for (MassChromatogram<Centroid> fragment : measurementmcs)
			{
				if (fragment.getAnnotations() == null)
					continue;
				for (Annotation annotation : fragment.getAnnotations().values())
				{
					String key = annotation.getLabel();
					if (mc.getAnnotations()!=null && mc.getAnnotations().containsKey(key))
						mc.addAnnotation(key, mc.getAnnotation(key).getValue() + ";" + annotation.getValue());
					else
						mc.addAnnotation(annotation);
				}
			}
			
			// finished
			mergedmcs.add(mc);
		}
		if (mergedmcs.size() == 0)
			peaksets.add(null);
		else
			peaksets.add(new IPeakSet<MassChromatogram<Centroid>>(new Vector<MassChromatogram<Centroid>>(mergedmcs)));
	}
	
	public int getNrPeaksets()
	{
		return peaksets.size();
	}
	
	public void write(String filename) throws IOException
	{
		Vector<IPeakSet<MassChromatogram<Centroid>>> s = new Vector<IPeakSet<MassChromatogram<Centroid>>>();
		for (IPeakSet<MassChromatogram<Centroid>> p : peaksets)
			if (p != null) s.add(p);
		
		PeakMLWriter.write(header, s, null, new java.util.zip.GZIPOutputStream(new FileOutputStream(filename)), null);
	}
	
	public void writeMeasurements(String filename) throws IOException
	{
		for (MeasurementInfo measurement : header.getMeasurementInfos())
		{
			Vector<MassChromatogram<Centroid>> mcs = new Vector<MassChromatogram<Centroid>>();
			for (MassChromatogram<Centroid> mc : masschromatograms)
			{
				if (mc!=null && mc.getMeasurementID()==measurement.getID())
					mcs.add(mc);
			}
			
			Header hdr = new Header();
			hdr.setNrPeaks(mcs.size());
			hdr.addMeasurementInfo(measurement);
			
			FileOutputStream out = new FileOutputStream(filename);
			PeakMLWriter.writeMassChromatograms(hdr, (Vector) masschromatograms, null, new GZIPOutputStream(out), null);
		}
	}
	
	
	// read interface
	public String[] getSetNames()
	{
		String setnames[] = new String[header.getNrSetInfos()];
		for (int i=0; i<header.getNrSetInfos(); ++i)
		{
			SetInfo setinfo = header.getSetInfos().get(i);
			
			setnames[i] = setinfo.getID();
		}
		
		return setnames;
	}
	
	public String[] getFileNames()
	{
		String filenames[] = new String[header.getNrMeasurementInfos()];
		for (int i=0; i<header.getNrMeasurementInfos(); ++i)
		{
			MeasurementInfo measurement = header.getMeasurementInfo(i);
			FileInfo file = measurement.getFileInfo(0);
			
			filenames[i] = file.getLocation() + "/" + file.getName();
		}
		
		return filenames;
	}
	
	public String[] getMeasurementNames()
	{
		String measurementnames[] = new String[header.getNrMeasurementInfos()];
		for (int i=0; i<header.getNrMeasurementInfos(); ++i)
		{
			MeasurementInfo measurement = header.getMeasurementInfo(i);
			
			measurementnames[i] = measurement.getLabel();
		}
		
		return measurementnames;
	}
	
	public double[] getMeasurementRetentionTimes(int measurementid)
	{
		MeasurementInfo measurementinfo = header.getMeasurementInfo(measurementid);
		if (measurementinfo == null)
			return null;
		double rts[] = new double[measurementinfo.getNrScanInfos()];
		for (int i=0; i<measurementinfo.getNrScanInfos(); ++i)
			rts[i] = measurementinfo.getScanInfo(i).getRetentionTime();
		return rts;
	}
	
	public double[] getMeasurementRetentionTimes(String measurementname)
	{
		for (MeasurementInfo measurementinfo : header.getMeasurementInfos())
		{
			if (!measurementname.equals(measurementinfo.getLabel()))
				continue;
			double rts[] = new double[measurementinfo.getNrScanInfos()];
			for (int i=0; i<measurementinfo.getNrScanInfos(); ++i)
				rts[i] = measurementinfo.getScanInfo(i).getRetentionTime();
			return rts;
		}
		return null;
	}
	
	public double[][] getMassChromatograms()
	{
		final int MINRT			=  0;
		final int MAXRT			=  1;
		final int AVGRT			=  2;
		final int MINSCAN		=  3;
		final int MAXSCAN		=  4;
		final int MINMZ			=  5;
		final int MAXMZ			=  6;
		final int AVGMZ			=  7;
		final int INTENSITY		=  8;
		final int SUMINTENSITY	=  9;
		final int MEASUREMENTID	= 10;
		final int SETID			= 11;
		final int GROUPID		= 12;
		final int SIZE			= 13;
		
		double data[][] = new double[masschromatograms.size()][SIZE];
		for (int i=0; i<masschromatograms.size(); ++i)
		{
			MassChromatogram<Centroid> mc = masschromatograms.get(i);
			SetInfo setinfo = header.getSetInfoForMeasurementID(mc.getMeasurementID());
			
			data[i][MINRT]			= mc.getMinRetentionTime();
			data[i][MAXRT]			= mc.getMaxRetentionTime();
			data[i][AVGRT]			= Statistical.mean(mc.getPeakData().getRetentionTimes());
			data[i][MINSCAN]		= mc.getMinScanID();
			data[i][MAXSCAN]		= mc.getMaxScanID();
			data[i][MINMZ]			= mc.getMinMass();
			data[i][MAXMZ]			= mc.getMaxMass();
			data[i][AVGMZ]			= Statistical.mean(mc.getPeakData().getMasses());
			data[i][INTENSITY]		= mc.getMaxIntensity();
			data[i][SUMINTENSITY]	= mc.getTotalIntensity();
			data[i][MEASUREMENTID]	= mc.getMeasurementID();
			data[i][SETID]			= header.indexOfSetInfo(setinfo.getID());
			data[i][GROUPID]		= mc.getPatternID();
		}
		
		return data;
	}
	
	public int getMeasurementID(int index)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		return mc.getMeasurementID();
	}
	
	public int[] getScanIDs(int index)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		return mc.getPeakData().getScanIDs();
	}
	
	public double[] getRetentionTimes(int index)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		return mc.getPeakData().getRetentionTimes();
	}
	
	public double[] getMasses(int index)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		return mc.getPeakData().getMasses();
	}
	
	public double[] getIntensities(int index)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		return mc.getPeakData().getIntensities();
	}
	
	public String[] getAnnotationLabels(int index)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		HashMap<String,Annotation> annotations = mc.getAnnotations();
		if (annotations != null)
			return (String[]) annotations.keySet().toArray();
		return null;
	}
	
	public String getAnnotation(int index, String name)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		Annotation annotation = mc.getAnnotation(name);
		if (annotation == null)
			return null;
		return annotation.getValue();
	}
	
	public void addAnnotation(int index, String label, String value)
	{
		MassChromatogram<Centroid> mc = masschromatograms.elementAt(index);
		if (mc != null)
			mc.addAnnotation(label, value);
	}
	
	public String[] getGroupAnnotationLabels(int groupid)
	{
		IPeakSet<MassChromatogram<Centroid>> peakset = peaksets.elementAt(groupid);
		if (peakset == null)
			return null;
		HashMap<String,Annotation> annotations = peakset.getAnnotations();
		if (annotations != null)
			return (String[]) annotations.keySet().toArray();
		return null;
	}
	
	public String getGroupAnnotation(int groupid, String name)
	{
		IPeakSet<MassChromatogram<Centroid>> peakset = peaksets.elementAt(groupid);
		if (peakset == null)
			return null;
		Annotation annotation = peakset.getAnnotation(name);
		if (annotation == null)
			return null;
		return annotation.getValue();
	}
	
	public void addGroupAnnotation(int groupid, String label, String value)
	{
		IPeakSet<MassChromatogram<Centroid>> peakset = peaksets.elementAt(groupid);
		if (peakset != null)
			peakset.addAnnotation(label, value);
	}
	
	public void load(String filename) throws IOException, XmlParserException
	{
		ParseResult result = PeakMLParser.parse(new FileInputStream(filename), true);
		
		header = result.header;
		peaksets = ((IPeakSet<IPeakSet<MassChromatogram<Centroid>>>) result.measurement).getPeaks();
		
		masschromatograms = new Vector<MassChromatogram<Centroid>>();
		for (int i=0; i<peaksets.size(); ++i)
		{
			IPeakSet<MassChromatogram<Centroid>> peakset = peaksets.get(i);
			for (MassChromatogram<Centroid> mc : peakset)
			{
				mc.setPatternID(i);
				masschromatograms.add(mc);
			}
		}
	}
	
	
	// util
	public double formulaToMass(String formula)
	{
		MolecularFormula f = new MolecularFormula(formula);
		return f.getMass(Mass.MONOISOTOPIC);
	}
	
	
	// Object overrides
	@Override
	public String toString()
	{
		StringBuffer str = new StringBuffer();
		
		str.append("project:\n");
		for (SetInfo set : header.getSetInfos())
		{
			str.append(" [");
			str.append(set.getID());
			str.append("]:");
			for (int measurementid : set.getAllMeasurementIDs())
			{
				str.append(" ");
				str.append(header.getMeasurementInfo(measurementid).getSampleID());
			}
			str.append("\n");
		}
		
		return str.toString();
	}
	
	
	// data
	protected Header header;
	protected Vector<MassChromatogram<Centroid>> masschromatograms;
	protected Vector<IPeakSet<MassChromatogram<Centroid>>> peaksets;
}
