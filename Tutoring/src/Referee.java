public class Referee {
	
	public Referee (String id, String first, String last, 
			String q, int num, String loc, String v) {
		ID = id;
		firstName = first;
		lastName = last;
		qual = q;
		numAllocations = num;
		locality = loc;
		visit = v;
	}
	
	public Referee (String line) {
		String [] tokens = line.split("[ ]+");
		ID = tokens[0];
		firstName = tokens[1];
		lastName = tokens[2];
		qual=tokens[3];
		numAllocations = (int) Integer.parseInt(tokens[4]);
		locality = tokens[5];
		visit = tokens[6];
	}
	
	public String getID () {
		return ID;
	}
	
	public void setID (String s) {
		ID = s;
	}
	
	public String getFirstName () {
		return firstName;
	}
	
	public void setFirstName (String s) {
		firstName = s;
	}
	public String getLastName () {
		return lastName;
	}
	
	public void setLastName (String s) {
		lastName = s;
	}
	
	public String getName () {
		return firstName + " " + lastName;
	}
	
	public String getQual () {
		return qual;
	}
	
	public void setQual (String s) {
		qual = s;
	}
	
	public String getLocality () {
		return locality;
	}
	
	public void setLocality (String s) {
		locality = s;
	}
	
	public String getVisit () {
		return visit;
	}
	
	public void setVisit (String s) {
		visit = s;
	}
	
	public boolean visitNorth() {
		return visit.charAt(0)=='Y';
	}
	
	public boolean visitCentral() {
		return visit.charAt(1)=='Y';
	}
	
	public boolean visitSouth() {
		return visit.charAt(2)=='Y';
	}
	
	public int getNumAllocations () {
		return numAllocations;
	}
	
	public void setNumAllocations (int n) {
		numAllocations = n;
	}
	
	public void incrementNumAllocations () {
		numAllocations++;
	}
	
	public boolean isSuitable(String level) {
		return level.equals("Junior") || 
			  (level.equals("Senior") && qual.charAt(3)>'1');
	}
	
	public boolean isAdjacent(String reqLocality) {
		return (locality.equals("Central") && !reqLocality.equals("Central")) ||
			   (!locality.equals("Central") && reqLocality.equals("Central"));
	}
	
	public boolean willVisit(String reqLocality) {
		return (reqLocality.equals("North") && visit.charAt(0)=='Y') ||
	           (reqLocality.equals("Central") && visit.charAt(1)=='Y') ||
		       (reqLocality.equals("South") && visit.charAt(2)=='Y');
	}
	
	public String toString() {
		String name = getName();
		String s = String.format("%-5s %-30s %-5s %-3d %-8s %-3s %n", 
				ID, name, qual, numAllocations, locality, visit);
		return s;
	}
	private String ID;
	private String firstName;
	private String lastName;
	private String qual;
	private String locality;
	private String visit;
	private int numAllocations;
}
