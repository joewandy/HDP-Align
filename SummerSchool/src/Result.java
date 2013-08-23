
public class Result {

	private String term1;
	private String term2;
	private int termID1;
	private int termID2;
	private double value;
	
	public Result(String term1, String term2, int termID1, int termID2,
			double value) {
		super();
		this.term1 = term1;
		this.term2 = term2;
		this.termID1 = termID1;
		this.termID2 = termID2;
		this.value = value;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + termID1;
		result = prime * result + termID2;
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
		Result other = (Result) obj;
		if (termID1 != other.termID1)
			return false;
		if (termID2 != other.termID2)
			return false;
		return true;
	}

	public String getTerm1() {
		return term1;
	}

	public String getTerm2() {
		return term2;
	}

	public int getTermID1() {
		return termID1;
	}

	public int getTermID2() {
		return termID2;
	}

	public double getValue() {
		return value;
	}
	
	public String toString() {
		return termID1 + ", " + termID2 + ", " + String.format("%.2f", value);
	}
	
	public static String getHeader() {
		return "termID1, termID2, value";
	}
	
}
