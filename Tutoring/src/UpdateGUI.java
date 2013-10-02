import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class UpdateGUI extends JFrame implements ActionListener
{
	public UpdateGUI(RefereeList r, JTextArea a)
	{       
		refList = r;
		textArea = a;

		JPanel northPanel = createNorthPanel();
		add(northPanel, "North");
		
		JPanel centerPanel = createCenterPanel();
		add(centerPanel, "Center");
		
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		
		southPanel = createSouthPanel();
		
		smallSouthPanel = new JPanel();
		smallSouthPanel.add(cancelButton);
		add(smallSouthPanel,"South");
		
		setTitle("Update a referee's details");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocation(LOCATION_X, LOCATION_Y);
	}
	
	public JPanel createNorthPanel()
	{
		JPanel northPanel = new JPanel();
		JLabel label = new JLabel();
		label.setText("Enter name of referee: ");
		northPanel.add(label);
		textField = new JTextField(10);
		textField.setEditable(true);
		northPanel.add(textField);
		searchButton = new JButton("Search");
		searchButton.addActionListener(this);
		northPanel.add(searchButton);
		return northPanel;
	}
	
	public JPanel createCenterPanel()
	{
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new GridLayout(4,1));
		label1 = new JLabel();
		centerPanel.add(label1);
		label2 = new JLabel();
		label2.setFont(f);
		centerPanel.add(label2);
		label3 = new JLabel();
		label3.setFont(f);
		centerPanel.add(label3);
		return centerPanel;
	}
	
	public JPanel createSouthPanel() {
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new BorderLayout());
		JPanel southPanel1 = new JPanel();
		southPanel1.setLayout(new GridLayout(3,2));
		JLabel southLabel1 = new JLabel("Update qualification");
		southPanel1.add(southLabel1);
		qualCombo = new JComboBox();
		qualCombo.addItem("NJB1");
		qualCombo.addItem("NJB2");
		qualCombo.addItem("NJB3");
		qualCombo.addItem("NJB4");
		qualCombo.addItem("IJB1");
		qualCombo.addItem("IJB2");
		qualCombo.addItem("IJB3");
		qualCombo.addItem("IJB4");
		qualCombo.addActionListener(this);
		southPanel1.add(qualCombo);
		JLabel southLabel2 = new JLabel("Update home locality");
		southPanel1.add(southLabel2);
		localityCombo = new JComboBox();
		localityCombo.addItem("North");
		localityCombo.addItem("Central");
		localityCombo.addItem("South");
		localityCombo.addActionListener(this);
		southPanel1.add(localityCombo);
		JLabel southLabel3 = new JLabel("Update localities to visit");
		southPanel1.add(southLabel3);
		JPanel checkBoxPanel = new JPanel();
		northCheckBox = new JCheckBox("North");
		northCheckBox.addActionListener(this);
		checkBoxPanel.add(northCheckBox);
		centralCheckBox = new JCheckBox("Central");
		centralCheckBox.addActionListener(this);
		checkBoxPanel.add(centralCheckBox);
		southCheckBox = new JCheckBox("South");
		southCheckBox.addActionListener(this);
		checkBoxPanel.add(southCheckBox);
		southPanel1.add(checkBoxPanel);
		southPanel.add(southPanel1, "North");
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		southPanel.add(doneButton,"Center");
		return southPanel;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == searchButton) {
			String name = textField.getText().trim();
			ref = refList.searchForReferee(name);
			if (ref==null) {
				label1.setText("No match for "+name+".");
				label2.setText("");
				label3.setText("");
				if (!addedSmallSouthPanel) {
					if (addedSouthPanel) {
						remove(southPanel);
						addedSouthPanel = false;
					}
					smallSouthPanel.add(cancelButton, "South");
					add(smallSouthPanel, "South");
					addedSmallSouthPanel = true;
				}
			}
			else {
				label1.setText("Here are the details for "+name+":");
				label2.setText(String.format("%-5s %-30s %-5s %-3s %-8s %-3s %n", 
						"ID", "Name", "Quals", "Num", "Locality", "Visit"));
				label3.setText(ref.toString());
				
				qualCombo.setSelectedItem(ref.getQual());
				localityCombo.setSelectedItem(ref.getLocality());
				northCheckBox.setSelected(ref.visitNorth());
				centralCheckBox.setSelected(ref.visitCentral());
				southCheckBox.setSelected(ref.visitSouth());
				updateCheckBoxes();
				if (!addedSouthPanel) {
					if (addedSmallSouthPanel) {
						remove(smallSouthPanel);
						addedSmallSouthPanel = false;
					}
					southPanel.add(cancelButton, "South");
					add(southPanel, "South");
					addedSouthPanel = true;
				}
			}
		}
		else if (e.getSource() == localityCombo)
			updateCheckBoxes();
		else if (e.getSource() == cancelButton)
			this.dispose();
		else if (e.getSource() == doneButton) {
			if (addedSouthPanel) {
				String qual = (String) qualCombo.getSelectedItem();
				ref.setQual(qual);
				String locality = (String) localityCombo.getSelectedItem();
				ref.setLocality(locality);			
				String visit;
				if (northCheckBox.isSelected())
					visit="Y";
				else visit = "N";
				if (centralCheckBox.isSelected())
					visit += "Y";
				else visit += "N";
				if (southCheckBox.isSelected())
					visit+="Y";
				else visit +="N";
				ref.setVisit(visit);
				textArea.setText(refList.toString());
			}
			this.dispose();     
		}
	}
	
	private void updateCheckBoxes() {
		northCheckBox.setEnabled(true);
		centralCheckBox.setEnabled(true);
		southCheckBox.setEnabled(true);
		String s = (String) localityCombo.getSelectedItem();
		if (s.equals("North")) {
			northCheckBox.setSelected(true);
			northCheckBox.setEnabled(false);
		}
		else if (s.equals("Central")) {
			centralCheckBox.setSelected(true);
			centralCheckBox.setEnabled(false);
		}
		else {
			southCheckBox.setSelected(true);
			southCheckBox.setEnabled(false);
		}
	}
	
	private RefereeList refList;
	private JTextArea textArea;
	private JPanel southPanel, smallSouthPanel;
	private JTextField textField;
	private JButton searchButton, cancelButton, doneButton;
	private JComboBox qualCombo, localityCombo;
	private JCheckBox northCheckBox, centralCheckBox, southCheckBox;
	private JLabel label1, label2, label3;
	private Font f = new Font("Monospaced", Font.PLAIN, 12);
	private Referee ref;
	private boolean addedSouthPanel = false;
	private boolean addedSmallSouthPanel = false;
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 300;
	private final int LOCATION_X = 500;
	private final int LOCATION_Y = 50;
}
