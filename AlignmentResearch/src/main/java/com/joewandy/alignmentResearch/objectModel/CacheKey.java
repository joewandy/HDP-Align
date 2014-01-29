package com.joewandy.alignmentResearch.objectModel;


public class CacheKey implements java.io.Serializable {
	
	private static final long serialVersionUID = 1L;
	private String filename;
	private int row;
	private int col;
	
	public CacheKey(String filename, int row, int col) {
		super();
		this.filename = filename;
		this.row = row;
		this.col = col;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + col;
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime * result + row;
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
		CacheKey other = (CacheKey) obj;
		if (col != other.col)
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (row != other.row)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CacheKey [filename=" + filename + ", row=" + row + ", col="
				+ col + "]";
	}
	
}
