package com.joewandy.alignmentResearch.objectModel;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import no.uib.cipr.matrix.Matrix;
import nu.xom.Attribute;
import nu.xom.Document;
import nu.xom.Element;
import peakml.chemistry.PeriodicTable;

import com.joewandy.alignmentResearch.comparator.FeatureIntensityComparator;

public class AlignmentFile {

	public final static double EPSILON = 0.0001;
	
	private int id;
	private String filename;
	private File file;
	private List<Feature> features;
	private List<Feature> sortedFeatures; // sorted by intensity descending
	private Matrix ZZProb;

	// for HDP alignment
	private int K;
	
	public AlignmentFile(int id, String filename, List<Feature> features) {
		this.id = id;
		this.filename = filename;		
		this.features = features;
		for (Feature feature : features) {
			feature.setData(this);
		}
		this.sortedFeatures = new ArrayList<Feature>(features);
		Collections.sort(this.sortedFeatures, new FeatureIntensityComparator());
		ZZProb = null;
	}

	public AlignmentFile(int id, File file, List<Feature> features) {
		this(id, file.getName(), features);
		this.file = file;
	}
	
	public int getId() {
		return id;
	}

	public String getFilename() {
		return filename;
	}
	
	public String getParentPath() {
		return file.getParent();
	}
	
	public File getFile() {
		return file;
	}

	public String getFilenameWithoutExtension() {
		String fileNameWithOutExt = AlignmentFile.removeExtension(this.filename);
		return fileNameWithOutExt;
	}
	
	public Matrix getZZProb() {
		return ZZProb;
	}
	
	public void setZZProb(Matrix zZProb) {
//		LinkedSparseMatrix temp = new LinkedSparseMatrix(zZProb);
//		ZZProb = temp;
		this.ZZProb = zZProb;
	}

	public List<Feature> getFeatures() {
		return features;
	}
	
	public List<Feature> getUndeletedFeatures() {
		List<Feature> result = new ArrayList<Feature>();
		for (Feature f : features) {
			if (!f.isDelete()) {
				result.add(f);
			}
		}
		return result;
	}

	public List<Feature> getDeletedFeatures() {
		List<Feature> result = new ArrayList<Feature>();
		for (Feature f : features) {
			if (f.isDelete()) {
				result.add(f);
			}
		}
		return result;
	}

	public List<Feature> getSortedFeatures() {
		return sortedFeatures;
	}
		
	public int getFeaturesCount() {
		return features.size();
	}

	public int getN() {
		return features.size();
	}
	
	public int getMaxFeatureID() {
		int max = Integer.MIN_VALUE;
		for (Feature feature : this.features) {
			if (feature.getPeakID() > max) {
				max = feature.getPeakID();
			}
		}
		return max;
	}
	
	public Feature getFeatureByIndex(int index) {
		return features.get(index);
	}
	
	public Feature getFeatureByPeakID(int peakID) {
		for (Feature feature : this.features) {
			if (feature.getPeakID() == peakID) {
				return feature;
			}
		}
		return null;
	}

	public Feature getFeatureByExample(Feature example) {
		return getFeatureByProperties(example.getMass(), example.getRt(), example.getIntensity());
	}
	
	public Feature getFeatureByProperties(double mass, double rt, double intense) {
		for (Feature feature : this.features) {
			if (Math.abs(feature.getMass() - mass) < EPSILON && 
					Math.abs(feature.getRt() - rt) < EPSILON && 
					Math.abs(feature.getIntensity() - intense) < EPSILON) {
				return feature;
			}
		}
		return null;
	}
	
	public Feature removeFeatureByIndex(int index) {
		return features.remove(index);
	}
	
	public void retainFeatures(Set<Feature> whiteList) {
		Iterator<Feature> it = this.features.iterator();
		while (it.hasNext()) {
			Feature nextFeature = it.next();
			if (!featureInWhiteList(nextFeature, whiteList)) {
				it.remove();
			}
		}		
	}
	
	public void addFeatures(List<Feature> newFeatures) {
		this.features.addAll(newFeatures);
	}
	
	/**
	 * see http://stackoverflow.com/questions/122105/java-what-is-the-best-way-to-filter-a-collection
	 * 
	 * @param referenceFeature
	 * @param massTol
	 * @param rtTol
	 * @param usePpm 
	 * @return
	 */
	public Set<Feature> getNextUnalignedFeatures(Feature referenceFeature, double massTol, double rtTol, 
			boolean usePpm) {
		
		Set<Feature> result = new HashSet<Feature>();

		double delta = 0;
		if (usePpm) {
			delta = PeriodicTable.PPM(referenceFeature.getMass(), massTol);			
		} else {
			delta = massTol;			
		}

		double massLower = referenceFeature.getMass() - delta/2;
		double massUpper = referenceFeature.getMass() + delta/2;
		double rtLower = referenceFeature.getRt() - rtTol/2;
		double rtUpper = referenceFeature.getRt() + rtTol/2;		
		
		for (Feature toCheck : this.features) {
			if (toCheck.isAligned()) {
				continue;
			}
			double massToCheck = toCheck.getMass();
			double rtToCheck = toCheck.getRt();
			if (inRange(massToCheck, massLower, massUpper)) {

				// in the mass range
				if (rtTol != -1) {
					
					// not in retention time range
					 if (inRange(rtToCheck, rtLower, rtUpper)) {
							result.add(toCheck);
					 }
					 
				} else {

					// not using retention time check
					result.add(toCheck);					
				
				}
			}
		}

		return result;
		
	}

	public Set<Feature> getNextFeatures(Feature referenceFeature, double massTol, double rtTol, 
			boolean usePpm) {
		
		Set<Feature> result = new HashSet<Feature>();

		double delta = 0;
		if (usePpm) {
			delta = PeriodicTable.PPM(referenceFeature.getMass(), massTol);			
		} else {
			delta = massTol;			
		}

		double massLower = referenceFeature.getMass() - delta/2;
		double massUpper = referenceFeature.getMass() + delta/2;
		double rtLower = referenceFeature.getRt() - rtTol/2;
		double rtUpper = referenceFeature.getRt() + rtTol/2;		
		
		for (Feature toCheck : this.features) {
			// get all features in range, even if they have been aligned
//			if (toCheck.isAligned()) {
//				continue;
//			}
			double massToCheck = toCheck.getMass();
			double rtToCheck = toCheck.getRt();
			if (inRange(massToCheck, massLower, massUpper)) {

				// in the mass range
				if (rtTol != -1) {
					
					// not in retention time range
					 if (inRange(rtToCheck, rtLower, rtUpper)) {
							result.add(toCheck);
					 }
					 
				} else {

					// not using retention time check
					result.add(toCheck);					
				
				}
			}
		}

		return result;
		
	}

	public Set<Feature> getNextFeatures(Feature referenceFeature, double massTol, boolean usePpm) {
		
		Set<Feature> result = new HashSet<Feature>();

		double delta = 0;
		if (usePpm) {
			delta = PeriodicTable.PPM(referenceFeature.getMass(), massTol);			
		} else {
			delta = massTol;			
		}

		double massLower = referenceFeature.getMass() - delta/2;
		double massUpper = referenceFeature.getMass() + delta/2;
		
		for (Feature toCheck : this.features) {
			double massToCheck = toCheck.getMass();
			if (inRange(massToCheck, massLower, massUpper)) {
				result.add(toCheck);					 
			}
		}

		return result;
		
	}
	
	public int getUnalignedFeaturesCount() {
		int count = 0;
		for (Feature feature : this.features) {
			if (!feature.isAligned()) {
				count++;
			}
		}
		return count;
	}

	/**
	 * see http://stackoverflow.com/questions/122105/java-what-is-the-best-way-to-filter-a-collection
	 * 
	 * @param referenceFeature
	 * @param massTol
	 * @param rtTol
	 * @return
	 */
	public Set<Feature> getNextUngroupedFeatures(Feature referenceFeature, double rtTol) {
		
		Set<Feature> result = new HashSet<Feature>();

		double rtLower = referenceFeature.getRt() - rtTol;
		double rtUpper = referenceFeature.getRt() + rtTol;		
		
		for (Feature toCheck : this.features) {
			if (toCheck.isGrouped()) {
				continue;
			}
			double rtToCheck = toCheck.getRt();
			 if (inRange(rtToCheck, rtLower, rtUpper)) {
					result.add(toCheck);
			 }
		}

		return result;
		
	}
	
	public void saveFeatures(String path) throws IOException {
		
		// main element
		Element featureMap = new Element("featureMap");
		
		// feature list
		Element featureList = new Element("featureList");
        Attribute count = new Attribute("count", String.valueOf(this.features.size()));
        featureList.addAttribute(count);
        featureMap.appendChild(featureList);
        
        for (Feature feature : this.features) {
        	Element featureElem = feature.getXmlElem();
        	featureList.appendChild(featureElem);
        }

        // Creating a Document object (from "XOM" API).
        Document doc = new Document(featureMap);
        FileWriter xmlFILE = new FileWriter(path);
        PrintWriter write = new PrintWriter(xmlFILE);
        write.print(doc.toXML());
        write.flush();
        write.close();
		
	}

	public void saveCsvFeatures(String path) throws IOException {
		
		PrintWriter pw = new PrintWriter(new FileOutputStream(path));
		pw.println("peakID, mass, rt, intensity");
		for (Feature feature : this.features) {
			pw.println(feature.csvForm());						
		}
		pw.close();
		
	}
	
	public void saveSimaFeatures(String path) throws IOException {
		
		PrintWriter pw = new PrintWriter(new FileOutputStream(path));
		for (Feature feature : this.features) {
			pw.println(feature.csvFormForSima());						
		}
		pw.close();
		
	}
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AlignmentFile other = (AlignmentFile) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "SimpleAlignmentData [filename=" + filename + "]";
	}

	private boolean inRange(double toCheck, double lowerRange, double upperRange) {
		// TODO: double comparison ?
		if (toCheck > lowerRange && toCheck < upperRange) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean featureInWhiteList(Feature feature, Set<Feature> whiteList) {

		// nothing to check if whitelist is empty
		if (whiteList == null || whiteList.isEmpty()) {
			return true;
		}
		
		// linear scan ..
		for (Feature listItem : whiteList) {
			if (Math.abs(feature.getMass() - listItem.getMass()) < EPSILON && 
					Math.abs(feature.getRt() - listItem.getRt()) < EPSILON && 
					Math.abs(feature.getIntensity() - listItem.getIntensity()) < EPSILON) {
				return true;
			}
		}
		return false;
		
	}
	
	public static String removeExtension(String filename) {
		String fileNameWithOutExt = filename.replaceFirst("[.][^.]+$", "");	
		return fileNameWithOutExt;
	}
	
}
