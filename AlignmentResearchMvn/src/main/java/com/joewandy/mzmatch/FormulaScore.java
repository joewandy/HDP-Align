package com.joewandy.mzmatch;

public class FormulaScore {
	
	private String molId;
	private String dbId;
	private String formula;
	private Double score;
	
	public FormulaScore(String molId, String dbId, String formula) {
		super();
		this.molId = molId;
		this.dbId = dbId;
		this.formula = formula;
		this.score = 0.0;
	}
	
	public void newScoreByVote(int length) {
		Double newVotes = 1.0 / length;
		this.score = newVotes;
	}
	
	public void incrementScoreByVote(int length) {
		Double newVotes = 1.0 / length;
		this.score += newVotes;
	}
	
	public Double getScore() {
		return score;
	}

	public void setScore(Double newScore) {
		this.score = newScore;
	}

	public String getMolId() {
		return molId;
	}

	public String getDbId() {
		return dbId;
	}
	
	public String getFormula() {
		return formula;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dbId == null) ? 0 : dbId.hashCode());
		result = prime * result + ((molId == null) ? 0 : molId.hashCode());
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
		FormulaScore other = (FormulaScore) obj;
		if (dbId == null) {
			if (other.dbId != null)
				return false;
		} else if (!dbId.equals(other.dbId))
			return false;
		if (molId == null) {
			if (other.molId != null)
				return false;
		} else if (!molId.equals(other.molId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "FormulaScore [molId=" + molId + ", dbId=" + dbId + ", formula="
				+ formula + ", score=" + score + "]";
	}
	
}
