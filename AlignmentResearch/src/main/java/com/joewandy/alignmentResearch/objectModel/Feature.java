package com.joewandy.alignmentResearch.objectModel;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.uib.cipr.matrix.Matrix;
import nu.xom.Attribute;
import nu.xom.Element;


/**
 * Represents a feature being aligned
 * TODO: this really should be an immutable object ...
 * @author joewandy
 *
 */
public class Feature {

	private int peakID;
	private double mass;
	private double massLog;
	private double rt;
	private double intensity;
	private AlignmentFile data;
	private boolean aligned;
	private List<FeatureGroup> groups;
	private double totalScore;
	private double totalIntensityError;
	private int noPairs;
	private boolean delete;
	private Element xmlElem;
	private int theoPeakID;
	
	public Feature(int peakID) {
		// the peakID is also the position index, so it should start from 0
		this.peakID = peakID;
		this.groups = new ArrayList<FeatureGroup>();
	}
	
	public Feature(int peakID, double mass, double rt, double intensity) {
		this.peakID = peakID;
		this.mass = mass;
		this.massLog = Math.log(mass);
		this.rt = rt;
		this.intensity = intensity;
		this.groups = new ArrayList<FeatureGroup>();
	}

	public int getPeakID() {
		return peakID;
	}
	
	public void setPeakID(int peakID) {
		this.peakID = peakID;
	}

	public double getMass() {
		return mass;
	}

	public double getMassLog() {
		return massLog;
	}
	
	public double getRt() {
		return rt;
	}

	public double getIntensity() {
		return intensity;
	}
	
	public void setMass(double mass) {
		this.mass = mass;
		this.massLog = Math.log(mass);
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public AlignmentFile getData() {
		return data;
	}

	public void setData(AlignmentFile data) {
		this.data = data;
	}

	public Element getXmlElem() {
		
		Element featureElem = null;
		
		if (this.xmlElem != null) {
			featureElem = (Element) this.xmlElem.copy();
		} else {
			// generative data has no feature xml element
			featureElem = new Element("feature");
			Attribute id = new Attribute("id", String.valueOf(peakID));
			featureElem.addAttribute(id);
		}
		
		featureElem.removeChildren();
		
		Element rtChild = new Element("position");
		Attribute rtChildAttr = new Attribute("dim", "0");
		rtChild.addAttribute(rtChildAttr);
		rtChild.appendChild(String.valueOf(rt));
		featureElem.appendChild(rtChild);

		Element massChild = new Element("position");
		Attribute massChildAttr = new Attribute("dim", "1");
		massChild.addAttribute(massChildAttr);
		massChild.appendChild(String.valueOf(mass));
		featureElem.appendChild(massChild);

		Element intenseChild = new Element("intensity");
		intenseChild.appendChild(String.valueOf(intensity));
		featureElem.appendChild(intenseChild);

		return featureElem;
		
	}

	public void setXmlElem(Element xmlElem) {
		this.xmlElem = xmlElem;
	}

	public int getTheoPeakID() {
		return theoPeakID;
	}

	public void setTheoPeakID(int theoPeakID) {
		this.theoPeakID = theoPeakID;
	}

	public boolean isAligned() {
		return aligned;
	}

	public void setAligned(boolean aligned) {
		this.aligned = aligned;
	}

	public boolean isGrouped() {
		if (this.groups.isEmpty()) {
			return false;
		} else {
			return true;			
		}
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}

	public FeatureGroup getFirstGroup() {
		if (this.groups.isEmpty()) {
			return null;
		}
		return this.groups.get(0);
	}

	public int getFirstGroupID() {
		return this.groups.get(0).getGroupId();
	}
	
	public List<FeatureGroup> getGroups() {
		return groups;
	}
	
	public void addGroup(FeatureGroup group) {
		this.groups.add(group);
	}
	
	public void clearGroups() {
		this.groups.clear();
	}

	public boolean clearGroup(int groupId) {
		Iterator<FeatureGroup> it = this.groups.iterator();
		while (it.hasNext()) {
			FeatureGroup g = it.next();
			if (g.getGroupId() == groupId) {
				it.remove();
				return true;
			}
		}
		return false;
	}
	
//	public DoubleMatrix getZZProb() {
//		return data.getZZProb();
//	}

	public Matrix getZZProb() {
		return data.getZZProb();
	}	
	
	public String csvForm() {
		// TODO: turn this into decorator
		return peakID + ", " + mass + ", " + rt + ", " + intensity;
	}

	public String csvFormForSima() {
		// TODO: turn this into decorator
		return mass + "\t" + 1 + "\t" + intensity + "\t" + rt;
	}
	
	public void graphScore(AlignmentPair pair) {
		this.totalScore += pair.getScore();
		this.totalIntensityError += pair.getRelativeIntensityError();
		this.noPairs++;
	}

	public void intensityScore(AlignmentPair pair) {
		this.totalScore += pair.getRelativeIntensityErrorScore();
		this.noPairs++;
	}
	
	public double getAverageScore() {
		return this.totalScore / this.noPairs;
	}
	
	public double getAverageIntensityError() {
		return this.totalIntensityError / this.noPairs;
	}
	
	/**
	 * TODO: This is a hack !
	 * @return
	 */
	public String asKey() {
		return "[" + csvForm() + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + peakID;
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
		Feature other = (Feature) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (peakID != other.peakID)
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (data != null) {
			return "Feature [peakID=" + peakID + ", mass=" + mass + ", rt=" + rt
					+ ", intensity=" + intensity + ", data=" + data.getFilenameWithoutExtension()
					+ ", theoPeakID=" + theoPeakID
					+ "]";			
		} else {
			return "Feature [peakID=" + peakID + ", mass=" + mass + ", rt=" + rt
					+ ", intensity=" + intensity + "]";			
		}
	}
	
	public static String csvHeader() {
		return "peakID, mass, rt, intensity";
	}
	
}
