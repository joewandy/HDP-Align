import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AddGUI extends JFrame implements ActionListener
{
	public AddGUI(RefereeList r, JTextArea a, JMenuItem b)
	{       
		refList = r;
		textArea = a;
		barChartMenuItem = b;
		
		JPanel northPanel = createNorthPanel();
		add(northPanel, "North");
		
		JPanel centerPanel = createCenterPanel();
		add(centerPanel, "Center");
		
		setTitle("Add a referee");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocation(LOCATION_X, LOCATION_Y);
	}
	
	public JPanel createNorthPanel()
	{
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new GridLayout(5,2));
		JLabel label1 = new JLabel("Name");
		northPanel.add(label1);
		nameField = new JTextField(10);
		nameField.setEditable(true);
		northPanel.add(nameField);
		JLabel label2 = new JLabel("Qualification");
		northPanel.add(label2);
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
		northPanel.add(qualCombo);
		JLabel label3 = new JLabel("Home locality");
		northPanel.add(label3);
		localityCombo = new JComboBox();
		localityCombo.addItem("North");
		localityCombo.addItem("Central");
		localityCombo.addItem("South");
		localityCombo.addActionListener(this);
		northPanel.add(localityCombo);
		JLabel label4 = new JLabel("Localities to visit");
		northPanel.add(label4);
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
		northPanel.add(checkBoxPanel);
		JLabel label5 = new JLabel("Number of matches");
		northPanel.add(label5);
		matchesField = new JTextField(1);
		matchesField.setEditable(true);
		northPanel.add(matchesField);
		return northPanel;
	}
	
	public JPanel createCenterPanel()
	{
		JPanel centerPanel = new JPanel();
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		centerPanel.add(cancelButton);
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		centerPanel.add(doneButton);
		return centerPanel;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == localityCombo)
			updateCheckBoxes();
		else if (e.getSource() == cancelButton)
			this.dispose();
		else if (e.getSource() == doneButton) {
			String locality = (String) localityCombo.getSelectedItem();
			String name = nameField.getText().trim();
			String [] nameTokens = name.split("[ ]+");
			if (nameTokens.length != 2)
				JOptionPane.showMessageDialog(null, "Format error in name.", 
						"Input error", JOptionPane.ERROR_MESSAGE);
			else {
				if ((locality.equals("North") && !northCheckBox.isSelected()) ||
						(locality.equals("Central") && !centralCheckBox.isSelected()) ||
						(locality.equals("South") && !southCheckBox.isSelected()))
					JOptionPane.showMessageDialog(null, "Error with visiting localities.", 
							"Input error", JOptionPane.ERROR_MESSAGE);
				else {
					String firstName = nameTokens[0];
					String lastName = nameTokens[1];
					String qual = (String) qualCombo.getSelectedItem();
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
					textArea.setText(refList.toString());
					String matchesString = matchesField.getText().trim();
					try {
						int numMatches = (int) Integer.parseInt(matchesString);
						if (numMatches < 0)
							throw new NumberFormatException();
						String id = ""+firstName.charAt(0)+lastName.charAt(0); 
						Referee ref = new Referee(id,firstName,lastName,qual,numMatches,locality,visit);
						boolean alreadyPresent = refList.addReferee(ref);
						if (alreadyPresent)
							JOptionPane.showMessageDialog(null, "Name already present.", 
								"Input error", JOptionPane.ERROR_MESSAGE);
						else {
							barChartMenuItem.setEnabled(true);
							textArea.setText(refList.toString());
							this.dispose();
						}
					}
					catch (NumberFormatException ex) {
						JOptionPane.showMessageDialog(null, "Error in number of matches allocated.", 
								"Input error", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
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
	private JMenuItem barChartMenuItem;
	private JTextField nameField, matchesField;
	private JButton cancelButton, doneButton;
	private JComboBox qualCombo, localityCombo;
	private JCheckBox northCheckBox, centralCheckBox, southCheckBox;
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 240;
	private final int LOCATION_X = 500;
	private final int LOCATION_Y = 50;
}
