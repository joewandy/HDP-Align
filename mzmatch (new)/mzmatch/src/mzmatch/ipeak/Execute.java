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



package mzmatch.ipeak;


// java
import java.lang.reflect.*;

// mzmatch
import mzmatch.util.*;





/**
 * 
 */
public class Execute
{
	public static void main(String args[])
	{
		args = new String[] { "ExtractMassChromatograms", "-h" };
		
		try
		{
			// check whether there is at least one argument potentially the tool-name
			if (args.length < 1)
			{
				System.err.println("[ERROR]: the tool-name to be executed needs to be supplied.");
				System.exit(0);
			}
			
			// locate the instance of the class
			Class<? extends Object> cls = null;
			for (Class<? extends Object> c : Tool.getAllClasses("mzmatch.ipeak"))
			{
				if (c.getName().endsWith(args[0]))
				{
					cls = c;
					break;
				}
			}
			if (cls == null)
			{
				System.err.println("[ERROR]: unable to locate the requested application '" + args[0] + "'");
				System.exit(0);
			}
			
			// check whether this is really a tool
//			OptionsClass optionscls = cls.getAnnotation(OptionsClass.class);
//			if (optionscls == null)
//			{
//				System.err.println("[ERROR]: ");
//				System.exit(0);
//			}
			
			// remove the tool-name
			String real_args[] = new String[args.length-1];
			for (int i=0; i<args.length-1; ++i)
				real_args[i] = args[i+1];
			
			// execute the tool
			for (Method method : cls.getMethods())
			{
				if (method.getName().equals("main"))
				{
					method.invoke(null, (Object) real_args);
					break;
				}
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, "mzmatch.Execute");
		}
	}
}
