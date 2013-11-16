package com.joewandy.alignmentResearch.objectModel;

public class SocialGraphEdge {

	private AlignmentRow masterListRow;
	private AlignmentRow alignedRow;
	
	public SocialGraphEdge(AlignmentRow masterListRow, AlignmentRow alignedRow) {
		this.masterListRow = masterListRow;
		this.alignedRow = alignedRow;
	}

	public AlignmentRow getMasterListRow() {
		return masterListRow;
	}

	public AlignmentRow getAlignedRow() {
		return alignedRow;
	}

	@Override
	public String toString() {
		return "SocialGraphEdge [masterListRow=" + masterListRow
				+ ", alignedRow=" + alignedRow + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((masterListRow == null) ? 0 : masterListRow.hashCode());
		result = prime * result
				+ ((alignedRow == null) ? 0 : alignedRow.hashCode());
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
		SocialGraphEdge other = (SocialGraphEdge) obj;
		if (masterListRow == null) {
			if (other.masterListRow != null)
				return false;
		} else if (!masterListRow.equals(other.masterListRow))
			return false;
		if (alignedRow == null) {
			if (other.alignedRow != null)
				return false;
		} else if (!alignedRow.equals(other.alignedRow))
			return false;
		return true;
	}
	
}
