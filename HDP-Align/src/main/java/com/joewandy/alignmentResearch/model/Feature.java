package com.joewandy.alignmentResearch.model;

import java.io.Serializable;

import nu.xom.Attribute;
import nu.xom.Element;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.joewandy.alignmentResearch.main.MultiAlignConstants;


/**
 * Represents a feature being aligned
 * TODO: this really should be an immutable object ...
 * @author joewandy
 *
 */
public class Feature implements Serializable {

	private static final long serialVersionUID = -3003827310427717456L;

	private transient Element xmlElem;
	
	private Integer peakID;
	private Integer fileID;
	private Integer sequenceID;
	private Integer groupID;
	private Integer theoPeakID;
	
	private double mass;
	private double massLog;
	private double rt;
	private double intensity;

	private boolean aligned;	
	private boolean delete;
	
	private String theoAdductType;
	private String metaboliteID;
	private boolean synthetic;
	private String annotation;
		
	// dummy constructor for jackson
	public Feature() {
		
	}
	
	public Feature(Integer peakID) {
		// the peakID is also the position index, so it should start from 0
		this.peakID = peakID;
	}
	
	public Feature(Integer peakID, double mass, double rt, double intensity) {
		this.peakID = peakID;
		this.mass = mass;
		this.massLog = Math.log(mass);
		this.rt = rt;
		this.intensity = intensity;
	}

	@JsonIgnore
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

	@JsonIgnore
	public void setXmlElem(Element xmlElem) {
		this.xmlElem = xmlElem;
	}
	
	public Integer getPeakID() {
		return peakID;
	}

	public void setPeakID(Integer peakID) {
		this.peakID = peakID;
	}

	public Integer getFileID() {
		return fileID;
	}

	public void setFileID(Integer fileID) {
		this.fileID = fileID;
	}

	public Integer getSequenceID() {
		return sequenceID;
	}

	public void setSequenceID(Integer sequenceID) {
		this.sequenceID = sequenceID;
	}

	public Integer getTheoPeakID() {
		return theoPeakID;
	}

	public void setTheoPeakID(Integer theoPeakID) {
		this.theoPeakID = theoPeakID;
	}

	public double getMass() {
		return mass;
	}

	public void setMass(double mass) {
		this.mass = mass;
	}

	public double getMassLog() {
		return massLog;
	}

	public void setMassLog(double massLog) {
		this.massLog = massLog;
	}

	public double getRt() {
		return rt;
	}

	public void setRt(double rt) {
		this.rt = rt;
	}

	public double getIntensity() {
		return intensity;
	}

	public void setIntensity(double intensity) {
		this.intensity = intensity;
	}

	public boolean isAligned() {
		return aligned;
	}

	public void setAligned(boolean aligned) {
		this.aligned = aligned;
	}

	public String getTheoAdductType() {
		return theoAdductType;
	}

	public void setTheoAdductType(String theoAdductType) {
		this.theoAdductType = theoAdductType;
	}

	public String getMetaboliteID() {
		return metaboliteID;
	}

	public void setMetaboliteID(String metaboliteID) {
		this.metaboliteID = metaboliteID;
	}

	public boolean isSynthetic() {
		return synthetic;
	}

	public void setSynthetic(boolean synthetic) {
		this.synthetic = synthetic;
	}

	@JsonIgnore
	public boolean isGrouped() {
		if (this.groupID == null) {
			return false;
		} else {
			return true;			
		}
	}
	
	public Integer getGroupID() {
		return groupID;
	}

	public void setGroupID(Integer groupID) {
		this.groupID = groupID;
	}

	public void clearGroupID() {
		this.groupID = null;
	}
	
	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
	}
		
	public String getAnnotation() {
		return annotation;
	}

	public void setAnnotation(String annotation) {
		this.annotation = annotation;
	}

	public String csvForm() {
		return peakID + ", " + mass + ", " + rt + ", " + intensity;
	}

	public String csvFormSynthetic() {
		return peakID + ", " + mass + ", " + rt + ", " + intensity + ", " + theoPeakID + ", " + metaboliteID;
	}
	
	public String csvFormForSima() {
		return mass + "\t" + 1 + "\t" + intensity + "\t" + rt;
	}

	public String csvFormForSimaSynthetic() {
		return mass + "\t" + 1 + "\t" + intensity + "\t" + rt + "\t" + theoPeakID + "\t" + metaboliteID + "\t" + theoAdductType;
	}
	
	/**
	 * TODO: This is a hack !
	 * @return
	 */
	public String asKey() {
		return "[" + csvForm() + "]";
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Feature [peakID=" + peakID);
		if (fileID != null) {
			builder.append(", fileID=" + fileID);
		}
		builder.append(", mass=" + String.format(MultiAlignConstants.MASS_FORMAT, mass));
		builder.append(", RT=" + rt);
		builder.append(", intensity=" + intensity);
		if (theoPeakID != null) {
			builder.append(", theoPeakID=" + theoPeakID);
		}
		if (theoAdductType != null) {
			builder.append(", theoAdductType=" + theoAdductType);
		}
		if (metaboliteID != null) {
			builder.append(", metaboliteID=" + metaboliteID);
		}
		builder.append("]");			
		return builder.toString();
	}
	
	public static String csvHeader() {
		return "peakID, mass, rt, intensity";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileID == null) ? 0 : fileID.hashCode());
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
		if (fileID == null) {
			if (other.fileID != null)
				return false;
		} else if (!fileID.equals(other.fileID))
			return false;
		if (peakID != other.peakID)
			return false;
		return true;
	}
	
}
