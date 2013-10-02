public class Match {
	
	public Match (int w, String lev, String loc, Referee r1, Referee r2) { 
		weekNum = w;
		level = lev;
		locality = loc;
		referee1 = r1;
		referee2 = r2;
	}
	
	public int getWeekNum() {
		return weekNum;
	}
	
	public String toString() {
		String name1 = referee1.getName();
		String name2 = referee2.getName();
		String s = String.format("%-4s %-6s %-8s %-30s %-30s %n", 
				                 weekNum, level, locality, name1, name2);
		return s;
	}
	
	private int weekNum;
	private String level, locality;
	private Referee referee1, referee2;
}
