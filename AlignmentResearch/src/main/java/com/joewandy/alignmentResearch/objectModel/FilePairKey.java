package com.joewandy.alignmentResearch.objectModel;


public class FilePairKey {
	
	private AlignmentFile file1;
	private AlignmentFile file2;
	public FilePairKey(AlignmentFile file1, AlignmentFile file2) {
		this.file1 = file1;
		this.file2 = file2;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((file1 == null) ? 0 : file1.hashCode());
		result = prime * result + ((file2 == null) ? 0 : file2.hashCode());
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
		FilePairKey other = (FilePairKey) obj;
		if (file1 == null) {
			if (other.file1 != null)
				return false;
		} else if (!file1.equals(other.file1))
			return false;
		if (file2 == null) {
			if (other.file2 != null)
				return false;
		} else if (!file2.equals(other.file2))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FilePairKey [file1=" + file1 + ", file2=" + file2 + "]";
	}
}