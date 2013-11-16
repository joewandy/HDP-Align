/* Copyright (C) 2008, Groningen Bioinformatics Centre (http://gbic.biol.rug.nl/)
 * This file is part of mzMatch.
 * 
 * PeakML is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * mzMatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with PeakML; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */



package com.joewandy.util;


// java
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.Vector;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
// logging (for all those pesky libraries using this)
// mzmatch





/**
 * Central class for initializing tools and printing status information. The
 * initialization is necessary because logging tools from apache are being
 * used in libraries, causing logging information to be printed to standard
 * out. This is annoying because the standard out is used in the tools to
 * print result information, which other tools can pick up with a pipe.
 */
public class Tool
{
	/**
	 * Initialize functions for tools. Basically this method disables the log4j logging
	 * to the standard output. This logger is used by many libraries to print status
	 * information, however this interferes with the use of pipes to build a tool-chain.
	 */
	public static void init()
	{
		// disable loggers
		// BasicConfigurator.configure();
		// Logger.getRootLogger().setLevel(Level.OFF);
	}

	/**
	 * Print an informative header about the tool. The application name can be passed,
	 * which will be printed together version information of the libraries used within
	 * the project.
	 * 
	 * @param output		The output stream to write to.
	 * @param appname		The name of the application printing the information.
	 */
	public static void printHeader(PrintStream output, String appname, String version)
	{
		String libraries[] =
		{
			//"jfreechart    " + org.jfree.chart.JFreeChart.INFO.getVersion(),
			"itext         " + com.lowagie.text.Document.getVersion().split(" ")[1],
			"jama          " + "1.0.2",
			"lma           " + "1.4.0",
			"cmdline       " + cmdline.Version.convertToString(),
			"domsax        " + domsax.Version.convertToString(),
			"peakml        " + peakml.Version.convertToString(),
			"mzmatch       " + Version.convertToString(),
		};
		
		output.println(" ------------------------------------------------------");
		output.println("| Copyright 2007-2009");
		output.println("| Groningen Bioinformatics Centre");
		output.println("| University of Groningen");
		output.println("|");
		output.println("| " + appname + " " + version + "");
		output.println("|");
		output.println("| libraries:");
		for (String library : libraries)
			output.println("|  - " + library);
		output.println(" ------------------------------------------------------");
	}
	
	/**
	 * Handler for unexpected errors in tools. In principle tools are required to check
	 * all possible error situations and give clean reports to the user. However, nothing
	 * can be 100% covered and errors that slip through will be caught and handled by
	 * this method.
	 * <p />
	 * Basically a file is generated containing the stacktrace of when the error occured
	 * and instructions are printed to mail this file.
	 */
	public static void unexpectedError(Exception e, String app)
	{
		final File file = new File("error.txt");
		final String message =
			"An unexpected error occurred in %s."
			+ "\n" +
			"The error message has been stored in '" + file.getAbsolutePath() + "'. " +
			"If the problem persists please contact 'gbic@rug.nl' with a description " +
			"of the setup and the error-message. Please start the header of the e-mail " +
			"with [mzmatch].";
		
		if (Version.status == Version.Status.DEBUG)
			e.printStackTrace();
		else
			System.err.println(String.format(message, app));
		
		try {
			e.printStackTrace(new PrintStream(file));
		} catch (Exception exc) { ; }
	}
	
	/**
	 * 
	 */
	public static void createFilePath(String filename, boolean parent)
	{
		if (parent)
		{
			File _parent = new File(filename).getParentFile();
			if (_parent != null)
				_parent.mkdirs();
		}
		else
		{
			File _file = new File(filename);
			if (_file != null)
				_file.mkdirs();
		}
	}
	
	/**
	 * 
	 */
	public static boolean filesExist(Vector<String> filenames)
	{
		for (String filename : filenames)
		{
			File f = new File(filename);
			if (!f.exists())
				return false;
		}
		return true;
	}
	
	/**
	 * 
	 * 
	 * @param pckgname
	 * @return
	 * @throws ClassNotFoundException
	 */
	public static Vector<Class<? extends Object>> getAllClasses(String pckgname) throws ClassNotFoundException
	{
		Vector<Class<? extends Object>> classes = new Vector<Class<? extends Object>>();
		
		// load the current package
		ClassLoader cld = Thread.currentThread().getContextClassLoader();
		if (cld == null)
			throw new ClassNotFoundException("Can't get class loader.");
		
		String path = pckgname.replace('.', '/');
		URL resource = cld.getResource(path);
		if (resource == null)
			throw new ClassNotFoundException("No resource for " + path);
		
		// parse the directory
		File directory = new File(resource.getFile());
		if (directory.isDirectory() && directory.exists())
		{
			for (File f : directory.listFiles())
			{
				if (f.getName().endsWith(".class"))
					classes.add(Class.forName(pckgname + '.' + f.getName().substring(0, f.getName().length() - 6)));
			}
			for (File f : directory.listFiles())
			{
				if (f.isDirectory())
					classes.addAll(getAllClasses(pckgname + "." + f.getName()));
			}
		}
		else
			throw new ClassNotFoundException(pckgname + " does not appear to be a valid package");
		
		return classes;
	}
}
