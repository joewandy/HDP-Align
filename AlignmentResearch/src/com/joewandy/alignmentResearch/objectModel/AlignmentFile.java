package com.joewandy.alignmentResearch.objectModel;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nu.xom.Attribute;
import nu.xom.Element;
import nu.xom.Elements;
import peakml.chemistry.PeriodicTable;

import com.joewandy.alignmentResearch.comparator.FeatureIntensityComparator;

public class AlignmentFile {

	private final static double EPSILON = 0.0001;
	
	private int id;
	private String filename;
	private List<Feature> features;
	
	public AlignmentFile(int id, String filename) {
		this.id = id;
		this.filename = filename;
	}
	
	public AlignmentFile(int id, String filename, List<Feature> features) {
		this.id = id;
		this.filename = filename;		
		this.features = features;
		for (Feature feature : features) {
			feature.setData(this);
		}
	}
			
	public int getId() {
		return id;
	}

	public String getFilename() {
		return filename;
	}
	
	public String getFilenameWithoutExtension() {
		String fileNameWithOutExt = AlignmentFile.removeExtension(this.filename);
		return fileNameWithOutExt;
	}

	public List<Feature> getFeatures() {
		return features;
	}
	
	public int getFeaturesCount() {
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

	public void sortFeatures() {
		Collections.sort(this.features, new FeatureIntensityComparator());
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

		double massLower = referenceFeature.getMass() - delta;
		double massUpper = referenceFeature.getMass() + delta;
		double rtLower = referenceFeature.getRt() - rtTol;
		double rtUpper = referenceFeature.getRt() + rtTol;		
		
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

		double massLower = referenceFeature.getMass() - delta;
		double massUpper = referenceFeature.getMass() + delta;
		double rtLower = referenceFeature.getRt() - rtTol;
		double rtUpper = referenceFeature.getRt() + rtTol;		
		
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

		double massLower = referenceFeature.getMass() - delta;
		double massUpper = referenceFeature.getMass() + delta;
		
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
