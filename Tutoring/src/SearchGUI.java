import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SearchGUI extends JFrame implements ActionListener
{
	public SearchGUI(RefereeList r)
	{       
		refList = r;
		
		JPanel northPanel = createNorthPanel();
		add(northPanel, "North");
		
		JPanel centerPanel = createCenterPanel();
		add(centerPanel, "Center");
		
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		add(doneButton, "South");
		
		setTitle("Search for a referee");
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
	
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == searchButton) {
			String name = textField.getText().trim();
			Referee ref = refList.searchForReferee(name);
			if (ref==null) {
				label1.setText("No match for "+name+".");
				label2.setText("");
				label3.setText("");
			}
			else {
				label1.setText("Here are the details for "+name+":");
				label2.setText(String.format("%-5s %-30s %-5s %-3s %-8s %-3s %n", 
						"ID", "Name", "Quals", "Num", "Locality", "Visit"));
				label3.setText(ref.toString());
			}
		}
		else if (e.getSource() == doneButton) {
			this.dispose();     
		}
	}
	
	private RefereeList refList;
	private JTextField textField;
	private JButton searchButton, doneButton;
	private JLabel label1, label2, label3;
	private Font f = new Font("Monospaced", Font.PLAIN, 12);
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 200;
	private final int LOCATION_X = 500;
	private final int LOCATION_Y = 50;
}
