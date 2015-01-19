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



package peakmlviewer;


// java
import java.io.*;
import javax.imageio.*;

// eclipse
import org.eclipse.swt.*;
import org.eclipse.swt.dnd.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;

// libraries

// peakml
import peakml.graphics.*;

// PeakMLviewer
import peakmlviewer.view.*;
import peakmlviewer.util.*;
import peakmlviewer.dialog.*;





/**
 * 
 */
public class MainWnd implements Listener
{
	// constructor(s)
	public MainWnd(int width, int height, String title)
	{
		display = new Display();
		shell = new Shell(display);
		
		// load the settings
		try {
			Settings.load(new FileInputStream("settings.xml"));
		} catch (Exception e) { e.printStackTrace(); }
		
		// set the properties of the main-window
		shell.setText(title);
		shell.setSize(width, height);
		
//		Image images[] = {
//				new Image(display, Resource.getResourceAsStream(Resource.ICON_APPLICATION_SMALL)),
//				new Image(display, Resource.getResourceAsStream(Resource.ICON_APPLICATION_LARGE))
//			};
//		shell.setImages(images);
		
		// add listeners
		shell.addListener(SWT.KeyDown, this);
		
		// create the document
		document = new Document(this);
		
		// set the layout
		FormLayout layout = new FormLayout();
		shell.setLayout(layout);
		
		// create the menu-bar
		Menu menu = new Menu(shell, SWT.BAR);
		shell.setMenuBar(menu);
		
		// create the file-menu
		Menu file_menu = createMenu(menu, "&File");
		
		file_open				= createMenuItem(file_menu, "&Open...",					null, SWT.PUSH, SWT.None);
		file_save				= createMenuItem(file_menu, "&Save...",					null, SWT.PUSH, SWT.None);
		file_saveas				= createMenuItem(file_menu, "Save &as...",				null, SWT.PUSH, SWT.None);
		createMenuItem(file_menu, "", null, SWT.SEPARATOR, SWT.None);
		file_exportcsv			= createMenuItem(file_menu, "&Export CSV...",			null, SWT.PUSH, SWT.None);
		file_exportgraph		= createMenuItem(file_menu, "&Export graph...",			null, SWT.PUSH, SWT.None);
		file_exportgraph2		= createMenuItem(file_menu, "&Export graph2...",		null, SWT.PUSH, SWT.None);
		file_exportchecked		= createMenuItem(file_menu, "&Export checked...",		null, SWT.PUSH, SWT.None);
		createMenuItem(file_menu, "", null, SWT.SEPARATOR, SWT.None);
		file_exit				= createMenuItem(file_menu, "E&xit",					null, SWT.PUSH, SWT.None);
		
		// create the edit-menu
		Menu edit_menu = createMenu(menu, "&Edit");
		
		edit_copy				= createMenuItem(edit_menu, "&Copy",					null, SWT.PUSH, SWT.None);
		edit_copyall			= createMenuItem(edit_menu, "Copy &all",				null, SWT.PUSH, SWT.None);
		edit_copyselected		= createMenuItem(edit_menu, "Copy &selected",			null, SWT.PUSH, SWT.None);
		edit_copygraph			= createMenuItem(edit_menu, "Copy &graph",				null, SWT.PUSH, SWT.None);
		createMenuItem(edit_menu, "", null, SWT.SEPARATOR, SWT.None);
		edit_selectall			= createMenuItem(edit_menu, "&Select all",				null, SWT.PUSH, SWT.None);
		edit_deselectall		= createMenuItem(edit_menu, "&Deselect all",			null, SWT.PUSH, SWT.None);
		edit_invertselection	= createMenuItem(edit_menu, "&Invert selection",		null, SWT.PUSH, SWT.None);
		createMenuItem(edit_menu, "", null, SWT.SEPARATOR, SWT.None);
		edit_preferences		= createMenuItem(edit_menu, "Preferences",				null, SWT.PUSH, SWT.None);
		createMenuItem(edit_menu, "", null, SWT.SEPARATOR, SWT.None);
		edit_merge_selected		= createMenuItem(edit_menu, "Merge selected",			null, SWT.PUSH, SWT.None);
		
		//edit_merge_selected.setEnabled(false);
		
//		// create the statistics-menu
//		Menu statistics_menu = createMenu(menu, "&Statistics");
//		
//		statistics_pca			= createMenuItem(statistics_menu, "&PCA",				null, SWT.PUSH, SWT.None);
//		statistics_rankproduct	= createMenuItem(statistics_menu, "&Rank product",		null, SWT.PUSH, SWT.None);
		
		// create the statistics-menu
		Menu view_menu = createMenu(menu, "&View");
		
		view_peakinfo			= createMenuItem(view_menu, "&Peak information",		null, SWT.PUSH, SWT.None);
		view_peakcompare		= createMenuItem(view_menu, "&Peak comparison",			null, SWT.PUSH, SWT.None);
		
		
		// create the view(s)
		FormData identificationslayout = new FormData();
		identificationslayout.left		= new FormAttachment(0);
		identificationslayout.right		= new FormAttachment(100);
		identificationslayout.top		= new FormAttachment(80);
		identificationslayout.bottom	= new FormAttachment(100);
		viewidentifications = new IdentificationView(this, shell);
		viewidentifications.setLayoutData(identificationslayout);
		
		FormData infolayout = new FormData();
		infolayout.left		= new FormAttachment(0);
		infolayout.right	= new FormAttachment(100);
		infolayout.top		= new FormAttachment(0);
		infolayout.bottom	= new FormAttachment(0, 40);
		viewinfo = new InfoView(this, shell);
		viewinfo.setLayoutData(infolayout);
		
		FormData controllayout = new FormData();
		controllayout.left		= new FormAttachment(0);
		controllayout.right		= new FormAttachment(25);
		controllayout.top		= new FormAttachment(viewinfo);
		controllayout.bottom	= new FormAttachment(viewidentifications);
		viewcontrol = new ControlView(this, shell);
		viewcontrol.setLayoutData(controllayout);
		
		FormData ipeaklayout = new FormData();
		ipeaklayout.left	= new FormAttachment(viewcontrol);
		ipeaklayout.right	= new FormAttachment(80);
		ipeaklayout.top		= new FormAttachment(viewinfo);
		ipeaklayout.bottom	= new FormAttachment(viewidentifications);
		viewipeak = new IPeakView(this, shell);
		viewipeak.setLayoutData(ipeaklayout);
		
		FormData intensitylayout = new FormData();
		intensitylayout.left	= new FormAttachment(viewipeak);
		intensitylayout.right	= new FormAttachment(100);
		intensitylayout.top		= new FormAttachment(viewinfo);
		intensitylayout.bottom	= new FormAttachment(35);
		viewintensity = new IntensityView(this, shell);
		viewintensity.setLayoutData(intensitylayout);
		
		FormData setslayout = new FormData();
		setslayout.left	= new FormAttachment(viewipeak);
		setslayout.right	= new FormAttachment(100);
		setslayout.top		= new FormAttachment(viewintensity);
		setslayout.bottom	= new FormAttachment(70);
		viewsets = new SetView(this, shell);
		viewsets.setLayoutData(setslayout);
		
		FormData annotationslayout = new FormData();
		annotationslayout.left		= new FormAttachment(viewipeak);
		annotationslayout.right		= new FormAttachment(100);
		annotationslayout.top		= new FormAttachment(viewsets);
		annotationslayout.bottom	= new FormAttachment(viewidentifications);
		viewannotations = new AnnotationsView(this, shell);
		viewannotations.setLayoutData(annotationslayout);
		
		// init
		document.init();
	}
	
	
	// access
	public void update(int event)
	{
		viewinfo.update(event);
		viewsets.update(event);
		viewipeak.update(event);
		viewcontrol.update(event);
		viewintensity.update(event);
		viewannotations.update(event);
		viewidentifications.update(event);
	}
	
	public Document getDocument()
	{
		return document;
	}
	
	
	// SWT-specific
	/**
	 * 
	 */
	public void dispose()
	{
		// save the settings
		try {
			Settings.save(new FileOutputStream("settings.xml"));
		} catch (Exception e) { e.printStackTrace(); }
		
		// destroy it all
		document.finish();
		display.dispose();
	}
	
	/**
	 * 
	 */
	public void display()
	{
		shell.open();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
				display.sleep();
		}
		
		dispose();
	}
	
	/**
	 * 
	 */
	public Shell getShell()
	{
		return shell;
	}
	
	/**
	 * 
	 */
	public Display getDisplay()
	{
		return display;
	}
	
	/**
	 * 
	 */
	public void getFocus()
	{
		display.asyncExec(new Runnable() {
			public void run() {
				shell.forceFocus();
			}
		});
	}
	
	/**
	 * 
	 */
	public void handleEvent(Event event)
	{
		if (event.widget == file_open)
		{
			FileDialog dlg = new FileDialog(shell, SWT.OPEN);
			if (document.lastdirectory != null)
				dlg.setFilterPath(document.lastdirectory);
			String filename = dlg.open();
			if (filename == null)
				return;
			document.lastdirectory = dlg.getFilterPath();
			
			try
			{
				document.loadFile(filename);
				update(Document.UPDATE_FILE_LOAD);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			
			//edit_merge_selected.setEnabled(document.data.header.getNrSetInfos() > 1);
		}
		else if (event.widget == file_save)
		{
			try
			{
				document.saveAs(document.getFilename());
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (event.widget == file_saveas)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			if (document.lastdirectory != null)
				dlg.setFilterPath(document.lastdirectory);
			String filename = dlg.open();
			if (filename == null)
				return;
			document.lastdirectory = dlg.getFilterPath();
			
			try
			{
				document.saveAs(filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (event.widget == file_exportcsv)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			if (document.lastdirectory != null)
				dlg.setFilterPath(document.lastdirectory);
			String filename = dlg.open();
			if (filename == null)
				return;
			document.lastdirectory = dlg.getFilterPath();
			
			try
			{
				document.exportCSV(filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (event.widget == file_exportgraph)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			if (document.lastdirectory != null)
				dlg.setFilterPath(document.lastdirectory);
			String filename = dlg.open();
			if (filename == null)
				return;
			document.lastdirectory = dlg.getFilterPath();
			
			try
			{
				java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1200, 500, java.awt.image.BufferedImage.TYPE_INT_ARGB);
				java.awt.Graphics g = img.getGraphics();
				
				g.setColor(java.awt.Color.WHITE);
				g.fillRect(0, 0, 1200, 500);
				g.drawImage(viewipeak.getGraphImage(800, 500), 0, 0, null);
				g.drawImage(viewintensity.getGraphImage(400, 250), 800, 0, null);
				
				ImageIO.write(img, "png", new File(filename));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (event.widget == file_exportgraph2)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			if (document.lastdirectory != null)
				dlg.setFilterPath(document.lastdirectory);
			String filename = dlg.open();
			if (filename == null)
				return;
			document.lastdirectory = dlg.getFilterPath();
			
			try
			{
				JFreeChartTools.writeAsPDF(
						new FileOutputStream(filename),
						viewipeak.getGraph(),
						800, 500
					);
				JFreeChartTools.writeAsPDF(
						new FileOutputStream(filename + "_2"),
						this.viewintensity.getGraph(),
						800, 500
					);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (event.widget == file_exportchecked)
		{
			FileDialog dlg = new FileDialog(shell, SWT.SAVE);
			if (document.lastdirectory != null)
				dlg.setFilterPath(document.lastdirectory);
			String filename = dlg.open();
			if (filename == null)
				return;
			document.lastdirectory = dlg.getFilterPath();
			
			try
			{
				document.exportChecked(filename);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else if (event.widget == file_exit)
		{
			dispose();
		}
		else if (event.widget == edit_copy)
		{
			viewcontrol.copy();
		}
		else if (event.widget == edit_copyall)
		{
			viewcontrol.copyAll();
		}
		else if (event.widget == edit_copygraph)
		{
			// create the image
			java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(1200, 500, java.awt.image.BufferedImage.TYPE_INT_ARGB);
			java.awt.Graphics g = img.getGraphics();
			
			g.setColor(java.awt.Color.WHITE);
			g.fillRect(0, 0, 1200, 500);
			g.drawImage(viewipeak.getGraphImage(800, 500), 0, 0, null);
			g.drawImage(viewintensity.getGraphImage(400, 250), 800, 0, null);
			
//			java.awt.datatransfer.
			
			// copy the image to the clipboard
			Clipboard clipboard = new Clipboard(getDisplay());
			clipboard.setContents(
					new Object[] { ImageConvert.convertToSWT(img) },
					new Transfer[] { ImageTransfer.getInstance() }
				);
			clipboard.dispose();
		}
		else if (event.widget == edit_copyselected)
		{
			viewcontrol.copySelected();
		}
		else if (event.widget == edit_selectall)
		{
			document.selection(true);
		}
		else if (event.widget == edit_deselectall)
		{
			document.selection(false);
		}
		else if (event.widget == edit_invertselection)
		{
			document.invertSelection();
		}
		else if (event.widget == edit_preferences)
		{
			PreferencesDialog dlg = new PreferencesDialog(this, shell, "Preferences");
			dlg.open();
			update(Document.UPDATE_SETTINGS);
		}
		else if (event.widget == edit_merge_selected)
		{
			document.mergeSelected();
		}
		else if (event.widget == statistics_pca)
		{
			PCADialog dlg = new PCADialog(this, shell, "PCA analysis");
			dlg.open();
		}
		else if (event.widget == view_peakinfo)
		{
			PeakInformationDialog dlg = new PeakInformationDialog(this, shell, "Peak information");
			dlg.open();
		}
		else if (event.widget == view_peakcompare)
		{
			CompareDialog dlg = new CompareDialog(this, shell, "Peak comparison");
			dlg.open();
		}
	}
	
	
	// utility functions
	/** */
	protected Menu createMenu(Menu menu, String name)
	{
		Menu m = new Menu(shell, SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.CASCADE);
		
		item.setMenu(m);
		item.setText(name);
		
		return m;
	}
	
	/** */
	protected MenuItem createMenuItem(Menu menu, String name, Image icon, int style, int accelerator)
	{
		MenuItem m = new MenuItem(menu, style);
		
		m.setText(name);
		m.setAccelerator(accelerator);
		m.addListener(SWT.Selection, this);
		
		return m;
	}
	
	
	// data
	protected Shell shell;
	protected Display display;
	
	protected Document document;
	
	protected InfoView viewinfo;
	protected SetView viewsets;
	protected IPeakView viewipeak;
	protected ControlView viewcontrol;
	protected IntensityView viewintensity;
	protected AnnotationsView viewannotations;
	protected IdentificationView viewidentifications;
	
	protected MenuItem file_open;
	protected MenuItem file_save;
	protected MenuItem file_saveas;
	protected MenuItem file_exportcsv;
	protected MenuItem file_exportgraph;
	protected MenuItem file_exportgraph2;
	protected MenuItem file_exportchecked;
	protected MenuItem file_exit;
	protected MenuItem edit_copy;
	protected MenuItem edit_copyall;
	protected MenuItem edit_copygraph;
	protected MenuItem edit_copyselected;
	protected MenuItem edit_selectall;
	protected MenuItem edit_deselectall;
	protected MenuItem edit_merge_selected;
	protected MenuItem edit_preferences;
	protected MenuItem edit_invertselection;
	protected MenuItem statistics_pca;
	protected MenuItem statistics_rankproduct;
	protected MenuItem view_peakinfo;
	protected MenuItem view_peakcompare;
}
