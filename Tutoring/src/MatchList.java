import java.io.*;

public class MatchList {
	
	public MatchList () {
		numMatches = 0;
		matchList = new Match [1];
		matchPlayed = new boolean [52];
	}
	
	public void addMatch (Match m) {
		matchList[numMatches]=m;
		numMatches++;
		int w = m.getWeekNum();
		matchPlayed[w-1] = true;
		Match [] newMatchList = new Match [numMatches+1];
		System.arraycopy(matchList, 0, newMatchList, 0, matchList.length);
		matchList = newMatchList;
	}
	
	public boolean alreadyMatch(int w) {
		if (w<0 || w>51)
			return false;
		else return matchPlayed[w-1];
	}
	
	public void writeFile() {
		String outputFileName = "MatchAllocs.txt";
		PrintWriter writer = null;
		try {
			try {
				writer = new PrintWriter(outputFileName);
				writer.println("List of allocated matches");
				writer.println("-------------------------");
				writer.println();
				writer.println(String.format("%-4s %-6s %-8s %-30s %-30s %n", 
		                                     "Week", "Level", "Locality", "Referee 1", "Referee 2"));
				writer.print(toString());
			}
			finally {
				if (writer!= null)
					writer.close();
			}
		}
		catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}		
	}

	public String toString() {
		String s = "";
		for (int i=0; i < numMatches; i++)
			s += matchList[i].toString();
		return s;
	}
	
	private int numMatches;
	private Match [] matchList;
	private boolean [] matchPlayed;
}
