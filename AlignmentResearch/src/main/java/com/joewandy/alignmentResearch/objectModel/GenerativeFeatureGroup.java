package com.joewandy.alignmentResearch.objectModel;




public class GenerativeFeatureGroup extends FeatureGroup {

	private GenerativeMolecule parent;
	private double warpedRT;
	
	public GenerativeFeatureGroup(int groupId) {
		super(groupId);
	}

	public GenerativeMolecule getParent() {
		return parent;
	}

	public void setParent(GenerativeMolecule parentMetabolite) {
		this.parent = parentMetabolite;
	}

	public double getWarpedRT() {
		return warpedRT;
	}

	public void setWarpedRT(double warpedRT) {
		this.warpedRT = warpedRT;
	}
	
}
