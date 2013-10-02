import java.awt.event.*;
import javax.swing.*;

public class BarChartGUI extends JFrame implements ActionListener
{
	public BarChartGUI(RefereeList r)
	{
		BarChart barChart = new BarChart(r);
		add(barChart);
		
		doneButton = new JButton("Done");
		doneButton.addActionListener(this);
		add(doneButton, "South");
		
		setTitle("Bar chart of match allocations");
		setSize(FRAME_WIDTH, FRAME_HEIGHT);
		setLocation(LOCATION_X, LOCATION_Y);
	}
		
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == doneButton)
			this.dispose();
	}
	
	private JButton doneButton;
	private final int FRAME_WIDTH = 500;
	private final int FRAME_HEIGHT = 500;
	private final int LOCATION_X = 500;
	private final int LOCATION_Y = 50;
}
