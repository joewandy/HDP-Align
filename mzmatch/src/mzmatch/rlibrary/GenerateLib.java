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



package mzmatch.rlibrary;


// java
import java.io.*;

import java.util.*;
import java.util.regex.*;

import java.lang.reflect.*;

// cmdline
import cmdline.*;

// mzmatch
import mzmatch.util.*;





/**
 * 
 */
public class GenerateLib
{
	// implementation
	public static final String USAGE					= "[USAGE]";
	public static final String AUTHOR					= "[AUTHOR]";
	public static final String DETAILS					= "[DETAILS]";
	public static final String TOOLNAME 				= "[TOOLNAME]";
	public static final String PARAMETERS 				= "[PARAMETERS]";
	public static final String REFERENCES 				= "[REFERENCES]";
	public static final String DESCRIPTION 				= "[DESCRIPTION]";
	public static final String PARAMETERS_PARAMETERS 	= "[PARAMETERS_PARAMETERS]";
	
	public static final Pattern pa_template = Pattern.compile("(\\[.+?\\])");
	
	
	public static String loadTemplate(String filename) throws IOException
	{
		StringBuffer text = new StringBuffer();
		
		String line;
		BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(filename)));
		while ((line = in.readLine()) != null)
		{
			text.append(line);
			text.append("\n");
		}
		return text.toString();
	}
	
	public static void writeFunction(Class<? extends Object> tool, String path, String template) throws IOException
	{
		// get the options-class
		OptionsClass optionscls = tool.getAnnotation(OptionsClass.class);
		
		// open output stream
		String filename = path + "/" + tool.getPackage().getName() + "." + optionscls.name() + ".R";
		Tool.createFilePath(filename, true);
		PrintStream out = new PrintStream(filename);
		
		// write
		int index = 0;
		Matcher ma_template = pa_template.matcher(template);
		while (ma_template.find(index))
		{
			// dump to the output
			out.print(template.substring(index, ma_template.start()));
			index = ma_template.end();
			
			// see what we need to write
			String tag = ma_template.group();
			if (TOOLNAME.equals(tag))
			{
				out.print(tool.getPackage().getName() + "." + optionscls.name());
			}
			else if (PARAMETERS.equals(tag))
			{
				boolean first = true;
				for (Option.Level level : Option.Level.values())
				{
					for (Field field : tool.getFields())
					{
						Option option = field.getAnnotation(Option.class);
						if (option==null || option.level()!=level)
							continue;
						out.print((!first ? ", " : "") + option.name());
//						if (option.type() == Option.Type.OPTIONAL_ARGUMENT)
						out.print("=NULL");
						first = false;
					}
				}
			}
			else if (PARAMETERS_PARAMETERS.equals(tag))
			{
				for (Option.Level level : Option.Level.values())
				{
					for (Field field : tool.getFields())
					{
						Option option = field.getAnnotation(Option.class);
						if (option==null || option.level()!=level)
							continue;
						
						// TODO 'tool <- paste(tool' is not what we want
						if (option.type() == Option.Type.NO_ARGUMENT)
						{
							out.println("\tif (!is.null(" + option.name() + ") && " + option.name() + "==T)");
							out.println("\t\ttool <- paste(tool, " + "\"-" + option.name() + "\")");
						}
						else
						{
							out.println("\tif (!is.null(" + option.name() + "))");
							out.println("\t\ttool <- paste(tool, " + "\"-" + option.name() + "\"" + ", " + option.name() + ")");
						}
					}
				}
			}
		}
		
		out.print(template.substring(index));
		out.flush();
	}
	
	public static void writeHelp(Class<? extends Object> tool, String path, String template) throws IOException
	{
		// get the options-class
		OptionsClass optionscls = tool.getAnnotation(OptionsClass.class);
		
		// open output stream
		String filename = path + "/" + tool.getPackage().getName() + "." + optionscls.name() + ".Rd";
		Tool.createFilePath(filename, true);
		PrintStream out = new PrintStream(filename);
		
		// write
		int index = 0;
		Matcher ma_template = pa_template.matcher(template);
		while (ma_template.find(index))
		{
			// dump to the output
			out.print(template.substring(index, ma_template.start()));
			index = ma_template.end();
			
			// see what we need to write
			String tag = ma_template.group();
			if (USAGE.equals(tag))
			{
				out.print(tool.getPackage().getName() + "." + optionscls.name() + "(JHeapSize=1425, ");
				boolean first = true;
				for (Option.Level level : Option.Level.values())
				{
					for (Field field : tool.getFields())
					{
						Option option = field.getAnnotation(Option.class);
						if (option==null || option.level()!=level)
							continue;
						out.print((!first ? ", " : "") + option.name());
						first = false;
					}
				}
				out.print(")");
			}
			else if (AUTHOR.equals(tag))
			{
				out.print(optionscls.author());
			}
			else if (DETAILS.equals(tag))
			{
				out.print(optionscls.description());
			}
			else if (TOOLNAME.equals(tag))
			{
				out.print(tool.getPackage().getName() + "." + optionscls.name());
			}
			else if (PARAMETERS.equals(tag))
			{
				for (Option.Level level : Option.Level.values())
				{
					for (Field field : tool.getFields())
					{
						Option option = field.getAnnotation(Option.class);
						if (option==null || option.level()!=level)
							continue;
						out.println("\\item{" + option.name() + "}{");
						out.println(option.usage());
						out.println("}");
					}
				}
			}
			else if (REFERENCES.equals(tag))
			{
				out.println(optionscls.references());
			}
			else if (DESCRIPTION.equals(tag))
			{
				String description = optionscls.description();
				int i = description.indexOf('.');
				if (i == -1)
					out.print(description);
				else
					out.print(description.substring(0, i+1));
			}
		}
		
		out.print(template.substring(index));
		out.flush();
	}
	
	
	// main entry point
	final static String version = "1.0.0";
	final static String application = "GenerateLib";
	@OptionsClass(name=application, version=version,
		description=
		""
	)
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"")
		public String output = null;
		
		@Option(name="h", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the help is shown.")
		public boolean help = false;
		@Option(name="v", param="", type=Option.Type.NO_ARGUMENT, usage=
			"When this is set, the progress is shown on the standard output.")
		public boolean verbose = false;
	}
	
	public static void main(String args[])
	{
		try
		{
			Tool.init();
			
			// parse the commandline options
			Options options = new Options();
			CmdLineParser cmdline = new CmdLineParser(options);
			
			// check whether we need to show the help
			cmdline.parse(args);
			if (options.help)
			{
				Tool.printHeader(System.out, application, version);
				
				cmdline.printUsage(System.out, "");
				return;
			}
			
			if (options.verbose)
			{
				Tool.printHeader(System.out, application, version);
				cmdline.printOptions();
			}
			
			// check the command-line parameters
			{
			}
			
			
			// load the templates
			String template_help = loadTemplate("src/mzmatch/rlibrary/help.template");
			String template_function = loadTemplate("src/mzmatch/rlibrary/function.template");
			
			// collect only those classes with an Options-structure
			Vector<Package> packages = new Vector<Package>();
			Vector<Class<? extends Object>> classes = new Vector<Class<? extends Object>>();
			for (Class<? extends Object> c : Tool.getAllClasses("mzmatch.ipeak"))
			{
				OptionsClass optionscls = c.getAnnotation(OptionsClass.class);
				if (optionscls==null || "Example".equals(optionscls.name()))
					continue;
				classes.add(c);
				if (!packages.contains(c.getPackage()))
					packages.add(c.getPackage());
			}
			
			// start dumping the files
			for (Class<? extends Object> tool : classes)
			{
				writeHelp(tool, options.output, template_help);
				writeFunction(tool, options.output, template_function);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
