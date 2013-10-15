package com.joewandy.alignmentResearch.alignmentMethod.custom;

import com.joewandy.alignmentResearch.objectModel.AlignmentRow;

public class RowPairKey {
	private AlignmentRow row1;
	private AlignmentRow row2;
	public RowPairKey(AlignmentRow row1, AlignmentRow row2) {
		this.row1 = row1;
		this.row2 = row2;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((row1 == null) ? 0 : row1.hashCode());
		result = prime * result + ((row2 == null) ? 0 : row2.hashCode());
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
		RowPairKey other = (RowPairKey) obj;
		if (row1 == null) {
			if (other.row1 != null)
				return false;
		} else if (!row1.equals(other.row1))
			return false;
		if (row2 == null) {
			if (other.row2 != null)
				return false;
		} else if (!row2.equals(other.row2))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "RowPairKey [row1=" + row1 + ", row2=" + row2 + "]";
	}
}