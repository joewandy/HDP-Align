import java.awt.*;
import javax.swing.*;

public class BarChart extends JComponent
{
	public BarChart(RefereeList r)
	{
		refList = r;
	}
	
	public void paintComponent(Graphics g)
	{  
		Graphics2D g2 = (Graphics2D) g;
		
		numReferees = refList.getNumReferees();
		int frameWidth = this.getWidth();
		int frameHeight = this.getHeight();
		
		//int offsetWidth = (int) Math.round(frameWidth*0.05);
		int offsetWidth = 0;
		int offsetHeight = (int) Math.round(frameHeight*0.05);

		int remainderWidth = frameWidth;
		int remainderHeight = (int) Math.round(frameHeight*0.95);
		
		int chartWidth = (int) Math.round(remainderWidth*0.9);
		int chartHeight = (int) Math.round(remainderHeight*0.9);
		
		int bottomLeftX = (remainderWidth - chartWidth) / 2 + offsetWidth;
		int bottomLeftY = (remainderHeight + chartHeight) / 2;
		
		int barWidth = chartWidth / numReferees;
		int maxNumAllocs = refList.getMaxNumAllocations();

		for (int i=0; i<numReferees; i++) {
			int numAllocs = refList.getNumAllocations(i);
			int barHeight = (chartHeight * numAllocs) / maxNumAllocs;
			Rectangle rect = new Rectangle(bottomLeftX, bottomLeftY - barHeight, barWidth, barHeight);
			g2.setColor(Color.BLUE);
			g2.draw(rect);
			g2.fill(rect);
			g2.setColor(Color.RED);
			g2.draw(rect);
			g2.setColor(Color.WHITE);
			String allocString = ""+numAllocs;
			int len = allocString.length();
			int textOffset = barWidth / (len+2);
			g2.drawString(allocString, bottomLeftX+textOffset, bottomLeftY - barHeight + offsetHeight);
			g2.setColor(Color.BLACK);
			String id = refList.getID(i);
			len = id.length();
			textOffset = barWidth / (len+2);
			g2.drawString(id,bottomLeftX+textOffset, bottomLeftY+offsetHeight);
			bottomLeftX += barWidth;
		}
	}
	
	private RefereeList refList;
	private int numReferees;
}