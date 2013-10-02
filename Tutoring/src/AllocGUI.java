import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AllocGUI extends JFrame implements ActionListener
{
	public AllocGUI(RefereeList r, MatchList m, JTextArea a)
	{       
		refList = r;
		matchList = m;
		mainTextArea = a;
		
		JPanel northPanel = createNorthPanel();
		add(northPanel, "North");
		
		JPanel centerPanel = createCenterPanel();
		add(centerPanel, "Center");
		
		JPanel southPanel = createSouthPanel();
		add(southPanel, "South");
		
		setTitle("Allocate referees to a match");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocation(LOCATION_X, LOCATION_Y);
	}
	
	public JPanel createNorthPanel() {
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(1,7));

		JLabel northLabel1 = new JLabel("Week no.:");
		northPanel.add(northLabel1);
		weekCombo = new JComboBox();
		for (int i=1; i<=52; i++)
			if (!matchList.alreadyMatch(i))
				weekCombo.addItem(""+i);
		weekCombo.addActionListener(this);
		northPanel.add(weekCombo);

		JLabel northLabel2 = new JLabel("  Level:");
		northPanel.add(northLabel2);
		levelCombo = new JComboBox();
		levelCombo.addItem("Junior");
		levelCombo.addItem("Senior");
		levelCombo.addActionListener(this);
		northPanel.add(levelCombo);

		JLabel northLabel3 = new JLabel("  Locality:");
		northPanel.add(northLabel3);
		localityCombo = new JComboBox();
		localityCombo.addItem("North");
		localityCombo.addItem("Central");
		localityCombo.addItem("South");
		localityCombo.addActionListener(this);
		northPanel.add(localityCombo);

		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		northPanel.add(searchButton);

		return northPanel;
	}
	
	public JPanel createCenterPanel()
	{
		JPanel centerPanel = new JPanel();
		thisTextArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
		thisTextArea.setEditable(false);
		thisTextArea.setFont(f);
		
		JScrollPane scrollPane = new JScrollPane(thisTextArea);
		centerPanel.add(scrollPane);
		return centerPanel;
	}
	
        public JPanel createSouthPanel()
	{
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new GridLayout(1,2));

		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		southPanel.add(cancelButton);

		doneButton = new JButton("Done");
		doneButton.setEnabled(false);
		doneButton.addActionListener(this);
		southPanel.add(doneButton);

		return southPanel;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == searchButton) {
			weekNum = (int) Integer.parseInt((String) weekCombo.getSelectedItem());
			level = (String) levelCombo.getSelectedItem();
			locality = (String) localityCombo.getSelectedItem();
			Referee [] priority1List = refList.findPriority1(level, locality);
			Referee [] priority2List = refList.findPriority2(level, locality);
			Referee [] priority3List = refList.findPriority3(level, locality);
			int numSuitableReferees = priority1List.length + priority2List.length +
								      priority3List.length;
			suitableReferees = new Referee[numSuitableReferees];
			System.arraycopy(priority1List, 0, suitableReferees, 0, priority1List.length);
			System.arraycopy(priority2List, 0, suitableReferees,
					         priority1List.length, priority2List.length);
			System.arraycopy(priority3List, 0, suitableReferees, 
					         priority1List.length + priority2List.length, priority3List.length);
			RefereeList newRefList = new RefereeList(suitableReferees);
			String display = String.format("The ordered list of suitable referees is:%n%n");
			display += String.format("%-5s %-30s %-5s %-3s %-8s %-3s %n%n", 
					                 "ID", "Name", "Quals", "Num", "Locality", "Visit");
			display += newRefList.toString();
			thisTextArea.setText(display);
			if (numSuitableReferees>1)
				doneButton.setEnabled(true);
		}
		else if (e.getSource() == cancelButton)
			this.dispose();
		else if (e.getSource() == doneButton) {
			Referee ref1 = suitableReferees[0];
			Referee ref2 = suitableReferees[1];
			Match match = new Match(weekNum, level, locality, ref1, ref2);
			matchList.addMatch(match);
			ref1.incrementNumAllocations();
			ref2.incrementNumAllocations();
			mainTextArea.setText(refList.toString());
			this.dispose();
		}
	}
	
	private RefereeList refList;
	private MatchList matchList;
	private JTextArea mainTextArea, thisTextArea;
	private JButton searchButton, cancelButton, doneButton;
    private JComboBox weekCombo, levelCombo, localityCombo;
	private Font f = new Font("Monospaced", Font.PLAIN, 12);
	private Referee [] suitableReferees;
	private int weekNum;
	private String level, locality;
	private final int FRAME_WIDTH = 550;
	private final int FRAME_HEIGHT = 300;
	private final int LOCATION_X = 200;
	private final int LOCATION_Y = 50;
	private final int AREA_COLUMNS = 70;
	private final int AREA_ROWS = 10;
}
