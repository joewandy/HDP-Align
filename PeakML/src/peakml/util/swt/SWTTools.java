/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of PeakML.
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



package peakml.util.swt;


// java

// standard widget toolkit
import org.eclipse.swt.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.graphics.*;





/**
 * Provides utility functions for SWT-applications.
 */
public class SWTTools
{
	// menu functionality
	/**
	 * Creates a menu-option in the given menu with the given name. An example
	 * would be "F&ile" for the ubiquitous file-menu. 
	 * 
	 * @param menu			The menu to create for.
	 * @param name			The name of the menu-option.
	 * @return				The menu-entry.
	 */
	public static Menu createMenu(Menu menu, String name)
	{
		Menu m = new Menu(menu.getShell(), SWT.DROP_DOWN);
		MenuItem item = new MenuItem(menu, SWT.CASCADE);
		
		item.setMenu(m);
		item.setText(name);
		
		return m;
	}
	
	/**
	 * Creates an item in the given menu-option.
	 * 
	 * @param listener		Listener for the callbacks.
	 * @param menu			The menu-option.
	 * @param name			The name of the item.
	 * @param icon			Optional icon.
	 * @param style			For example SWT.PUSH
	 * @param accelerator	Accelarator key (can be SWT.NONE)
	 * @return				The menu-item.
	 */
	public static MenuItem createMenuItem(Listener listener, Menu menu, String name, Image icon, int style, int accelerator)
	{
		MenuItem m = new MenuItem(menu, style);
		
		m.setText(name);
		m.setAccelerator(accelerator);
		m.addListener(SWT.Selection, listener);
		
		return m;
	}
}
