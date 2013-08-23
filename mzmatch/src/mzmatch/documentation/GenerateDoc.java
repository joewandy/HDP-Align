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



package mzmatch.documentation;


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
public class GenerateDoc
{
	// implementation
	public static final String NAME			= "[NAME]";
	public static final String TOOLS		= "[TOOLS]";
	public static final String AUTHOR		= "[AUTHOR]";
	public static final String MZMATCH		= "[MZMATCH]";
	public static final String PACKAGE		= "[PACKAGE]";
	public static final String EXAMPLE		= "[EXAMPLE]";
	public static final String VERSION		= "[VERSION]";
	public static final String OPTIONS		= "[OPTIONS]";
	public static final String PACKAGES		= "[PACKAGES]";
	public static final String DESCRIPTION	= "[DESCRIPTION]";
	public static final String REFERENCES 	= "[REFERENCES]";
	
	public static final Pattern pa_template = Pattern.compile("(\\[.+?\\])");
	
	
	/**
	 * 
	 * @param tool
	 * @param output
	 * @return
	 */
	public static String toolToFilename(Class<? extends Object> tool, String output, boolean adddir)
	{
		if (adddir)
		{
			String packagename = tool.getEnclosingClass().getName().replace(".", "/");
			return output + "/" + packagename + ".html";
		}
		else
		{
			return tool.getEnclosingClass().getSimpleName() + ".html";
		}
	}
	
	/**
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
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
	
	/**
	 * 
	 * @param filename
	 * @param template
	 * @throws IOException
	 */
	public static void writeIndex(String filename, String template) throws IOException
	{
		// open output stream
		Tool.createFilePath(filename, true);
		PrintStream out = new PrintStream(filename);
		
		// write
		out.print(template);
		out.flush();
	}
	
	/**
	 * 
	 * @param filename
	 * @param tools
	 * @param template
	 * @throws IOException
	 */
	public static void writeAllTools(String filename, Vector<Class<? extends Object>> tools, boolean adddir, String template) throws IOException
	{
		// open output stream
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
			if (TOOLS.equals(tag))
			{
				for (Class<? extends Object> tool : tools)
				{
					// retrieve the options
					OptionsClass optionscls = tool.getAnnotation(OptionsClass.class);
					
					// print the data
					out.println("<a href=\"" + toolToFilename(tool, ".", adddir) + "\" target=\"toolFrame\">" + optionscls.name());
					out.println("<br />");
				}
			}
		}
		
		out.print(template.substring(index));
		out.flush();
	}
	
	/**
	 * 
	 * @param filename
	 * @param packages
	 * @param template
	 * @throws IOException
	 */
	public static void writeOverview(String filename, Vector<Package> packages, String template) throws IOException
	{
		// open output stream
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
			if (PACKAGES.equals(tag))
			{
				for (Package p : packages)
				{
					out.println("<a href=\"" + p.getName().replace('.', '/') + "/package-frame.html" + "\" target=\"packageFrame\">" + p.getName());
					out.println("<br />");
				}
			}
		}
		
		out.print(template.substring(index));
		out.flush();
	}
	
	/**
	 * 
	 * @param filename
	 * @param packages
	 * @param template
	 * @throws IOException
	 */
	public static void writeOverviewSummary(String filename, Vector<Package> packages, String template) throws IOException
	{
		// open output stream
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
			if (PACKAGES.equals(tag))
			{
				for (Package p : packages)
				{
					out.println("<tr>");
					out.println("<td width=\"2%\">");
					out.println("\t<a href=\"" + p.getName()+"/tools.html" + "\" target=\"toolFrame\">" + p.getName());
					out.println("</td>");
					out.println("<td>&nbsp;</td>");
					out.println("</tr>");
				}
			}
		}
		
		out.print(template.substring(index));
		out.flush();
	}
		
	/**
	 * 
	 * @param filename
	 * @param tool
	 * @param template
	 * @throws IOException
	 */
	public static void writeTool(Class<? extends Object> tool, String filename, String template) throws IOException, IllegalAccessException, InstantiationException
	{
		// open output stream
		Tool.createFilePath(filename, true);
		PrintStream out = new PrintStream(filename);
		
		// retrieve the options
		OptionsClass optionscls = tool.getAnnotation(OptionsClass.class);
		
		// print the output
		int index = 0;
		Matcher ma_template = pa_template.matcher(template);
		while (ma_template.find(index))
		{
			// dump to the output
			out.print(template.substring(index, ma_template.start()));
			index = ma_template.end();
			
			// see what we need to write
			String tag = ma_template.group();
			if (NAME.equals(tag))
				out.print(optionscls.name());
			else if (PACKAGE.equals(tag))
				out.print(tool.getPackage().getName());
			else if (AUTHOR.equals(tag))
				out.print(optionscls.author());
			else if (MZMATCH.equals(tag))
				out.print(mzmatch.Version.convertToString());
			else if (EXAMPLE.equals(tag))
				out.print(optionscls.example());
			else if (VERSION.equals(tag))
				out.print(optionscls.version());
			else if (DESCRIPTION.equals(tag))
				out.print(optionscls.description().replace("\n", "<br />"));
			else if (REFERENCES.equals(tag))
				out.print(optionscls.references().replace("\n", "<br />"));
			else if (OPTIONS.equals(tag))
			{
				for (Option.Level level : Option.Level.values())
				{
					for (Field field : tool.getFields())
					{
						Option option = field.getAnnotation(Option.class);
						if (option==null || option.level()!=level)
							continue;
						
						String usage = option.usage().replace("\n", "<br />");
						if (usage.length() == 0) usage = "&nbsp;";
						
						String param = option.param();
						if (param.length() == 0)
							param = "&nbsp;";
						else if (!field.getType().isPrimitive() && Collection.class.isInstance(field.getType().newInstance()))
							param = "[" + param + "]";
						else
							param = "&lt;" + param + "&gt;";
						
						out.println("<tr>");
						out.println("\t<td align=\"right\" valign=\"top\" width=\"1%\">" + "-" + option.name() + " " + param + "</td>");
						out.println("\t<td valign=\"top\">" + usage + "</td>");
						out.println("</tr>");
					}
				}
			}
			else
				System.err.println("[WARNING]: unknown tag '" + tag + "'");
		}
		
		out.print(template.substring(index));
		out.flush();
	}
	

	// main entry point
	final static String version = "1.0.0";
	final static String application = "GenerateDoc";
	@OptionsClass(name=application, version=version, author="RA Scheltema (r.a.scheltema@rug.nl)",
		description=
		"",
		example=
		"",
		references=
		""
	)
	public static class Options
	{
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
			String template_tool = loadTemplate("src/mzmatch/documentation/tool.template");
			String template_index = loadTemplate("src/mzmatch/documentation/index.template");
			String template_package = loadTemplate("src/mzmatch/documentation/package.template");
			String template_overview = loadTemplate("src/mzmatch/documentation/overview.template");
			String template_overview_summary = loadTemplate("src/mzmatch/documentation/overview-summary.template");
			
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
			writeIndex(options.output + "/index.html", template_index);
			writeAllTools(options.output + "/alltools-frame.html", classes, true, template_package);
			writeOverview(options.output + "/overview-frame.html", packages, template_overview);
			writeOverviewSummary(options.output + "/overview-summary.html", packages, template_overview_summary);
			for (Class<? extends Object> tool : classes)
				writeTool(tool, toolToFilename(tool, options.output, true), template_tool);
			for (Package p : packages)
			{
				// collect the classes of this package
				Vector<Class<? extends Object>> package_classes = new Vector<Class<? extends Object>>();
				for (Class<? extends Object> c : classes)
					if (p.equals(c.getPackage())) package_classes.add(c);
				
				// write the file
				String filename = options.output + "/" + p.getName().replace('.', '/') + "/package-frame.html";
				Tool.createFilePath(filename, true);
				writeAllTools(filename, package_classes, false, template_package);
			}
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
