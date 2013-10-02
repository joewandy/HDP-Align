import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MainGUI extends JFrame implements ActionListener
{
	public MainGUI(RefereeList r, MatchList m)
	{       
		refList = r;
		matchList = m;
		JMenuBar menuBar = new JMenuBar();     
		setJMenuBar(menuBar);
		menuBar.add(createFileMenu());
		menuBar.add(createRefereeMenu());
		menuBar.add(createAllocMenu());
		if (refList.getNumReferees()==0)
			barChartMenuItem.setEnabled(false);
		label = createTextField();
		add(label, "North");
		
		JScrollPane scrollPane = createScrollPane();
		add(scrollPane, "Center");
		
		setTitle("JavaBall Referee Editor");
		setLocation(LOCATION_X, LOCATION_Y);
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
	}
	
	public JMenu createFileMenu()
	{
		JMenu menu = new JMenu("File");
		exitMenuItem = new JMenuItem("Exit");      
		exitMenuItem.addActionListener(this);
		menu.add(exitMenuItem);
		return menu;
	}
	
	public JMenu createRefereeMenu()
	{
		JMenu menu = new JMenu("Referee");
		searchMenuItem = new JMenuItem("Search");
		searchMenuItem.addActionListener(this);
		menu.add(searchMenuItem);
		updateMenuItem = new JMenuItem("Update");
		updateMenuItem.addActionListener(this);
		menu.add(updateMenuItem);
		addMenuItem = new JMenuItem("Add");
		addMenuItem.addActionListener(this);
		menu.add(addMenuItem);
		deleteMenuItem = new JMenuItem("Delete");
		deleteMenuItem.addActionListener(this);
		menu.add(deleteMenuItem);
		return menu;
	}
	
	public JMenu createAllocMenu()
	{
		JMenu menu = new JMenu("Allocation");
		allocMenuItem = new JMenuItem("Allocate referees");
		allocMenuItem.addActionListener(this);
		menu.add(allocMenuItem);
		barChartMenuItem = new JMenuItem("Bar chart");
		barChartMenuItem.addActionListener(this);
		menu.add(barChartMenuItem);
		return menu;
	}
	
	public JLabel createTextField()
	{
		JLabel label = new JLabel();
		label.setFont(f);
		String s = String.format("%-5s %-30s %-5s %-3s %-8s %-3s %n", 
				"ID", "Name", "Quals", "Num", "Locality", "Visit");
		label.setText(s);
		return label;
	}
	
	public JScrollPane createScrollPane()
	{
		textArea = new JTextArea(AREA_ROWS, AREA_COLUMNS);
		textArea.setEditable(false);
		textArea.setFont(f);
		JScrollPane scrollPane = new JScrollPane(textArea);
		textArea.setText(refList.toString());
		return scrollPane;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		JMenuItem choice = (JMenuItem) e.getSource();
		if (choice == exitMenuItem) {
			matchList.writeFile();
			refList.writeFile();
			System.exit(0);
		}
		else if (choice == searchMenuItem) {
			JFrame frame = new SearchGUI(refList);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
		else if (choice == updateMenuItem) {
			JFrame frame = new UpdateGUI(refList, textArea);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
		else if (choice == deleteMenuItem) {
			JFrame frame = new DeleteGUI(refList, textArea, barChartMenuItem);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
		else if (choice == addMenuItem) {
			JFrame frame = new AddGUI(refList, textArea, barChartMenuItem);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
		else if (choice == barChartMenuItem) {
			JFrame frame = new BarChartGUI(refList);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
		else if (choice == allocMenuItem) {
		    JFrame frame = new AllocGUI(refList, matchList, textArea);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			frame.setVisible(true);
		}
	}
	
	private RefereeList refList;
	private MatchList matchList;
	private JMenuItem exitMenuItem;
	private JMenuItem searchMenuItem, updateMenuItem, addMenuItem, deleteMenuItem;
	private JMenuItem allocMenuItem, barChartMenuItem;
	private JLabel label;
	private JTextArea textArea;
	private Font f = new Font("Monospaced", Font.PLAIN, 12);
	private final int AREA_COLUMNS = 60;
	private final int AREA_ROWS = 10;
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 200;
	private final int LOCATION_X = 300;
	private final int LOCATION_Y = 300;
}
