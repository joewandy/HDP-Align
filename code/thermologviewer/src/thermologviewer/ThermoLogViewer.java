/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of ThermoLogViewer.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * PeakML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package thermologviewer;


// java
import java.io.*;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;

// peakml





/**
* 
*/
public class ThermoLogViewer
{
	public static void main(String args[])
	{
		MainWnd mainwnd = new MainWnd(900, 400, "Thermo Log Viewer (" + Version.convertToString() + ")");

		try
		{
			mainwnd.display();
		}
		catch (Exception e)
		{
			File file = new File("error.txt");
			try {
				PrintStream out = new PrintStream(file.getAbsolutePath());
				e.printStackTrace(out);
				out.close();
			} catch (Exception ee) {;}
			
			e.printStackTrace();
			
			MessageBox msg = new MessageBox(mainwnd.getShell(), SWT.OK);
			msg.setMessage(
					"An error-report has been generated\n" +
					"  '" + file.getAbsolutePath() + "'\n\n" +
					"Please send this file to r.a.scheltema@rug.nl"
				);
			msg.open();
			mainwnd.dispose();
		}
	}
}
