/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzmatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzmatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package peakmlviewer.dialog;


// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// peakml





/**
 * Implementation of a progress-dialog. The update-process for the dialog is executed
 * inside a thread, which can update the progress-bar with a call to setProgress. This
 * method is synchronized so it won't wreak havoc on the ui-components.
 * <p/>
 * 
 * ProgressDialog _dlg = new ProgressDialog(shell)
 * {
 *    @Override
 *    public void run()
 *    {
 *       for (int i=1; i<=100; ++i)
 *          setProgress(i);
 *       dispose();
 *    }
 * }
 */
public class ProgressDialog extends Dialog
{
	// constructor(s)
	/**
	 * 
	 */
	public ProgressDialog(Shell parent, String title)
	{
		super(parent, SWT.NONE);
		
		// save the parent pointer
		this.parent = parent;
		
		// create the window and set its properties
		shell = new Shell(parent, SWT.DIALOG_TRIM|SWT.APPLICATION_MODAL);
		shell.setSize(300, 100);
		shell.setText(getText());
		shell.setText(title);
		
		// add the animation-widget
//		try {
//			AnimatedImage img = new AnimatedImage(
//					shell,
//					new Image(parent.getDisplay(), Resource.getResourceAsStream(Resource.ICON_APPLICATION_PROCESSING)),
//					8, 4, 150, SWT.NONE
//				);
//			
//			img.setSize(20, 20);
//			img.setLocation(15, 30);
//			img.start();
//		} catch (Exception e) { ; }
		
		// place the dialog at the center of the parent
		Rectangle parentSize = parent.getBounds();
		Rectangle mySize = shell.getBounds();
		shell.setLocation(new Point(
				(parentSize.width - mySize.width)/2+parentSize.x,
				(parentSize.height - mySize.height)/2+parentSize.y
			));
		
		// add the progress-widget
		progress = new ProgressBar(shell, SWT.SMOOTH);
		
		progress.setSize(230, 20);
		progress.setLocation(45, 30);
		
		progress.setMinimum(0);
		progress.setMaximum(100);
		progress.setSelection(0);
	}
	
	
	// 
	public void setTask(Runnable task)
	{
		// save the task-pointer
		this.task = task;
	}
	
	/**
	 * 
	 */
	public void open()
	{
		// retrieve the display
		Display display = parent.getDisplay();
		
		// open the window
		shell.open();
		// start the task
		new Thread(task).start();
		// enter the event-loop
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch()) display.sleep();
		}
	}
	
	/**
	 * 
	 */
	public synchronized void dispose()
	{
		parent.getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				shell.dispose();
			}
		});
	}
	
	/**
	 * 
	 */
	public synchronized void setProgress(final double p)
	{
		parent.getDisplay().asyncExec(new Runnable() {
			public void run()
			{
				progress.setSelection((int) p);
			}
		});
	}
	
	
	// data
	/** */
	private Shell shell;
	/** */
	private Shell parent;
	/** */
	private Runnable task;
	
	/** */
	private ProgressBar progress;
}
