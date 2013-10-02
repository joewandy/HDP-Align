import javax.swing.JFrame;

public class ProgProj {
	public static void main(String[] args)
	{
	    RefereeList refList = new RefereeList();
	    MatchList matchList = new MatchList();
	    refList.inputFromFile();
	    JFrame frame = new MainGUI(refList, matchList);
	    frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    frame.setVisible(true);      
	}
}
