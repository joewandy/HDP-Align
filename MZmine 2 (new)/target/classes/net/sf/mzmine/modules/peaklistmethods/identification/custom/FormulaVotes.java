package net.sf.mzmine.modules.peaklistmethods.identification.custom;



public class FormulaVotes {
	
	private String molId;
	private String dbId;
	private String formula;
	private Double votes;
	
	public FormulaVotes(String molId, String dbId, String formula) {
		super();
		this.molId = molId;
		this.dbId = dbId;
		this.formula = formula;
		this.votes = 0.0;
	}
	
	public void newVotes(int length) {
		Double newVotes = 1.0 / length;
		this.votes = newVotes;
	}
	
	public void incrementVotes(int length) {
		Double newVotes = 1.0 / length;
		this.votes += newVotes;
	}
	
	public Double getVotes() {
		return votes;
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
		FormulaVotes other = (FormulaVotes) obj;
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
		return "FormulaVote [molId=" + molId + ", dbId=" + dbId + ", formula="
				+ formula + ", votes=" + votes + "]";
	}
	
}
