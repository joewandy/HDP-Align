import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DeleteGUI extends JFrame implements ActionListener
{
	public DeleteGUI(RefereeList r, JTextArea a, JMenuItem b)
	{       
		refList = r;
		textArea = a;
		barChartMenuItem = b;
		
		JPanel northPanel = createNorthPanel();
		add(northPanel, "North");
		
		JPanel centerPanel = createCenterPanel();
		add(centerPanel, "Center");
		
		JPanel southPanel = createSouthPanel();
		add(southPanel, "South");
		
		setTitle("Delete a referee");
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
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(this);
		southPanel.add(cancelButton);
		doneButton = new JButton("Delete");
		doneButton.addActionListener(this);
		doneButton.setEnabled(false);
		southPanel.add(doneButton);
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
				doneButton.setEnabled(false);
			}
			else {
				label1.setText("Here are the details for "+name+":");
				label2.setText(String.format("%-5s %-30s %-5s %-3s %-8s %-3s %n", 
						"ID", "Name", "Quals", "Num", "Locality", "Visit"));
				label3.setText(ref.toString());
				doneButton.setEnabled(true);
			}
		}
		else if (e.getSource() == cancelButton)
			this.dispose();     
		else if (e.getSource() == doneButton) {
			refList.deleteReferee(ref);
			if (refList.getNumReferees()==0)
				barChartMenuItem.setEnabled(false);
			textArea.setText(refList.toString());
			this.dispose();     
		}
	}
	
	private RefereeList refList;
	private JTextArea textArea;
	private JMenuItem barChartMenuItem;
	private JTextField textField;
	private JButton searchButton, cancelButton, doneButton;
	private JLabel label1, label2, label3;
	private Font f = new Font("Monospaced", Font.PLAIN, 12);
	private Referee ref;
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 300;
	private final int LOCATION_X = 500;
	private final int LOCATION_Y = 50;
}
