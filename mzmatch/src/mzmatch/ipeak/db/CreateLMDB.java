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



package mzmatch.ipeak.db;


// java
import java.io.*;

import java.util.*;
import java.util.regex.*;

// jni-inchi
import net.sf.jniinchi.*;

// libraries
import cmdline.*;

// peakml
import peakml.chemistry.*;
import peakml.io.chemistry.*;

// mzmatch
import mzmatch.util.*;





public class CreateLMDB
{
	// implementation
	final static int STATE_BROWSING			= 0;
	final static int STATE_ID				= 1;
	final static int STATE_SYSTEMATICNAME	= 2;
	final static int STATE_COMMONNAME		= 3;
	final static int STATE_CATEGORY			= 4;
	final static int STATE_CLASS			= 5;
	final static int STATE_SUBCLASS			= 6;
	final static int STATE_CLASSLVL4		= 7;
	final static int STATE_FORMULA			= 8;
	final static int STATE_STRUCTURE		= 9;
	
	
	// http://en.wikipedia.org/wiki/Numerical_prefix
	// 
	public static class GreekNumeral
	{
		public GreekNumeral(String name, int value)
			{ this.name = name; this.value = value; }
		final public String name;
		final public int value;
	}
	
	public static final GreekNumeral greeknumerals[] = new GreekNumeral[] {
		new GreekNumeral("triacont[aey]?",			30),
		new GreekNumeral("hentriacont[aey]?",		31),
		new GreekNumeral("dotriacont[aey]?",		32),
		new GreekNumeral("tritriacont[aey]?",		33),
		new GreekNumeral("tetratriacont[aey]?",		34),
		new GreekNumeral("pentatriacont[aey]?",		35),
		new GreekNumeral("hexatriacont[aey]?",		36),
		new GreekNumeral("heptatriacont[aey]?",		37),
		new GreekNumeral("octatriacont[aey]?",		38),
		new GreekNumeral("nonatriacont[aey]?",		39),
		new GreekNumeral("tetracont[aey]?",			40),
		new GreekNumeral("hentetracont[aey]?",		41),
		new GreekNumeral("dotetracont[aey]?",		42),
		new GreekNumeral("tritetracont[aey]?",		43),
		new GreekNumeral("tetratetracont[aey]?",	44),
		new GreekNumeral("pentatetracont[aey]?",	45),
		new GreekNumeral("hexatetracont[aey]?",		46),
		new GreekNumeral("heptatetracont[aey]?",	47),
		new GreekNumeral("octatetracont[aey]?",		48),
		new GreekNumeral("nonatetracont[aey]?",		49),
		//
		new GreekNumeral("undec[aey]?",				11),	// these go at the back of the list to enable
		new GreekNumeral("dodec[aey]?",				12),	// the regex to try a larger version first
		new GreekNumeral("tridec[aey]?",			13),
		new GreekNumeral("tetradec[aey]?",			14),
		new GreekNumeral("pentadec[aey]?",			15),
		new GreekNumeral("hexadec[aey]?",			16),
		new GreekNumeral("heptadec[aey]?",			17),
		new GreekNumeral("octadec[aey]?",			18),
		new GreekNumeral("nonadec[aey]?",			19),
		new GreekNumeral("eicos[aey]?",				20),
		new GreekNumeral("heneicos[aey]?",			21),
		new GreekNumeral("docos[aey]?",				22),
		new GreekNumeral("tricos[aey]?",			23),
		new GreekNumeral("tetracos[aey]?",			24),
		new GreekNumeral("pentacos[aey]?",			25),
		new GreekNumeral("hexacos[aey]?",			26),
		new GreekNumeral("heptacos[aey]?",			27),
		new GreekNumeral("octacos[aey]?",			28),
		new GreekNumeral("nonacos[aey]?",			29),
		//
		new GreekNumeral("metha",					 1),	// these go at the back of the list to enable
		new GreekNumeral("etha",				 	 2),	// the regex to try a larger version first
		new GreekNumeral("di",						 2),
		new GreekNumeral("prop[aey]?",				 3),
		new GreekNumeral("tri",			 			 3),
		new GreekNumeral("but[aey]?",				 4),
		new GreekNumeral("tetr[aey]?",				 4),
		new GreekNumeral("pent[aey]?",				 5),
		new GreekNumeral("hex[aey]?",				 6),
		new GreekNumeral("hept[aey]?",				 7),
		new GreekNumeral("oct[aey]?",				 8),
		new GreekNumeral("non[aey]?",				 9),
		new GreekNumeral("dec[aey]?",				10),
	};
	
	public static int convertToInt(String str)
	{
		if (str == null)
			return 0;
		for (GreekNumeral greeknumeral : greeknumerals)
		{
			String name = greeknumeral.name;
			if (!name.contains("?") && name.equals(str))
				return greeknumeral.value;
			else if (name.contains("?"))
			{
				String shortname = name.substring(0, name.indexOf('['));
				if (str.length()>=shortname.length() && shortname.equalsIgnoreCase(str.substring(0, shortname.length())))
					return greeknumeral.value;
			}
		}
		return -1;
	}
	
	static String numeral = "";
	static String headgroup =
		"epoxy"
		+ "|" +
		"tetrahydroxy|trihydroxy|dihydoxy|hydroxy"
		+ "|" +
		"tetraoxo|trioxo|dioxo|oxo"
		+ "|" +
		"tetraacetyl|triacetyl|diacetyl|acetyl"
		+ "|" +
		"tetramethoxy|trimethoxy|dimethoxy|methoxy"
		+ "|" +
		"tetramethyl|trimethyl|dimethyl|methyl"
		+ "|" +
		"tetraethyl|triethyl|diethyl|ethyl"
		+ "|" +
		"tetraamino|triamino|diamino|amino"
	;
	static String negate = "(?!methyl|hydro|methoxy|oxo|oxy|ene|dec|cos|nol|amm|no)";
	static {
		numeral = "";
		for (int i=0; i<greeknumerals.length; ++i)
		{
			if (i == 0)
				numeral = numeral + greeknumerals[i].name + negate;
			else
				numeral = numeral + "|" + greeknumerals[i].name + negate;
		}
		numeral = numeral + "";
	}
	
	public static final Pattern pa_headgroup = Pattern.compile(
			"(" + headgroup + ")",
			Pattern.CASE_INSENSITIVE
		);
	public static final Pattern pa_sidechain = Pattern.compile(
			"(" + "tri" + negate + ".*?|di" + negate + ".*?)?(" + numeral + ")(" + numeral + ")?",
			Pattern.CASE_INSENSITIVE
		);
	
	public static String getChainCounts(String id, String systematicname)
	{
		if (systematicname == null)
			return "";
		Matcher ma_carbons = pa_sidechain.matcher(systematicname);
		
		StringBuffer str = new StringBuffer();
		while (ma_carbons.find())
		{
			int carbon = convertToInt(ma_carbons.group(2));
			int bonds = convertToInt(ma_carbons.group(3));
			
//			if (ma_carbons.group(1) != null)
//			{
//				System.out.println(systematicname);
				System.out.println("- " + ma_carbons.group(1) + " - " + ma_carbons.group(2) + " - " + ma_carbons.group(3));
//			}
			
			int times = 1;
			if (ma_carbons.group(1) != null)
				if (ma_carbons.group(1).startsWith("di")) times = 2;
				else if (ma_carbons.group(1).startsWith("tri")) times = 3;
			
			for (int i=0; i<times; ++i)
			{
				if (str.length() != 0) str.append("/");
				
				str.append(carbon);
				str.append(":");
				if (bonds==0 && ma_carbons.group(2).endsWith("e"))
					str.append(1);
				else if (bonds==0 && ma_carbons.group(2).endsWith("y"))
					str.append(2);
				else
					str.append(bonds);
			}
		}
		
		return str.toString();
	}
	
	public static String getHeadGroup(String id, String systematicname)
	{
		if (systematicname == null)
			return "";
		Matcher ma_headgroup = pa_headgroup.matcher(systematicname);
		
		StringBuffer str = new StringBuffer();
		while (ma_headgroup.find())
		{
			if (str.length() != 0) str.append(",");
			str.append(ma_headgroup.group(1));
		}
		return str.toString();
	}
	
	
	// main entrance
	final static String version = "1.0.0";
	final static String application = "CreateLMDB";
	@OptionsClass(name=application, version=version, description=
		"Converts a lipid maps database to the moleculedb format. Additionally, the " +
		"tool interprets the systematic names to determine side-chain length(s) and " +
		"the head-group.")
	public static class Options
	{
		@Option(name="i", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Filename of the lipidmaps database.")
		public String input = null;
		@Option(name="o", param="filename", type=Option.Type.REQUIRED_ARGUMENT, usage=
			"Filename of the moleculedb file.")
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
			
			
			// open the streams
			InputStream input = System.in;
			if (options.input != null)
				input = new FileInputStream(options.input);
			OutputStream output = System.out;
			if (options.output != null)
				output = new FileOutputStream(options.output);
			
			// create the containers
			HashMap<String,Molecule> molecules = new HashMap<String,Molecule>();
			
			int curline = 0;
			int state = STATE_STRUCTURE;
			
			String
				id=null, systematicname=null, commonname=null,
				category=null, mainclass=null, subclass=null, classlvl4=null,
				formula=null, inchi=null;
			
			JniInchiInput molecule = new JniInchiInput();
			
			
			// parse the file
			String line;
			BufferedReader in = new BufferedReader(new InputStreamReader(input));
			while ((line = in.readLine()) != null)
			{
				line = line.trim();
				if (line.startsWith("$$$$"))
				{
					try
					{
						String chaincounts = getChainCounts(id, systematicname);
						String headgroup = getHeadGroup(id, systematicname);
						if (formula!=null && !formula.contains("<sup>"))
						{
							if (systematicname==null || systematicname.equals(""))
								systematicname = commonname;
							
							Molecule m = new Molecule(id, systematicname, formula);
							m.setInChi(inchi);
	
							m.setDescription(
									headgroup+(chaincounts!=null&&chaincounts.length()>0 ? "("+chaincounts+") " : "") +
									category + 
									"|" + 
									mainclass +
									(subclass!=null ? ("|" + subclass) : "") +
									(classlvl4!=null ? ("|" + classlvl4) : "")
								);
							if (commonname != null)
								m.setSynonyms(new String[] { commonname });
							molecules.put(m.getDatabaseID(), m);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
					
					curline = 0;
					state = STATE_STRUCTURE;
					molecule = new JniInchiInput();
					id = systematicname = commonname = category = mainclass = subclass = classlvl4 = formula = inchi = null;
				}
				else if (state == STATE_STRUCTURE)
				{
					++curline;
					if (curline > 4)
					{
						if (line.startsWith("M") || line.startsWith(">"))
						{
							state = STATE_BROWSING;
							
							JniInchiOutput jniinchi = JniInchiWrapper.getInchi(molecule);
							inchi = jniinchi.getInchi();
						}
						else
						{
							String tokens[] = line.split(" ");
							int cnt = 0;
							for (String t : tokens)
								if (t.length()>0) cnt++;
							String _tokens[] = new String[cnt];
							int i=0;
							for (String t : tokens)
								if (t.length()>0) _tokens[i++] = t;
							tokens = _tokens;
							
							// atoms
							if (tokens.length > 7)
							{
								/*JniInchiAtom atom = */molecule.addAtom(new JniInchiAtom(
										Double.parseDouble(tokens[0]), // x
										Double.parseDouble(tokens[1]), // y
										Double.parseDouble(tokens[2]), // z
										tokens[3]
									));
							}
							// bonds
							else if (tokens.length == 7)
							{
								int bondtype = Integer.parseInt(tokens[2]);
								/*JniInchiBond bond = */molecule.addBond(new JniInchiBond(
										molecule.getAtom(Integer.parseInt(tokens[0])-1),
										molecule.getAtom(Integer.parseInt(tokens[1])-1),
										(bondtype==1?INCHI_BOND_TYPE.SINGLE:(bondtype==2?INCHI_BOND_TYPE.DOUBLE:INCHI_BOND_TYPE.TRIPLE))
									));
							}
						}
					}
				}
				else if (state == STATE_BROWSING)
				{
					if (line.startsWith("> <LM_ID>"))
						state = STATE_ID;
					else if (line.startsWith("> <SYSTEMATIC_NAME>"))
						state = STATE_SYSTEMATICNAME;
					else if (line.startsWith("> <COMMON_NAME>"))
						state = STATE_COMMONNAME;
					else if (line.startsWith("> <CATEGORY>"))
						state = STATE_CATEGORY;
					else if (line.startsWith("> <MAIN_CLASS>"))
						state = STATE_CLASS;
					else if (line.startsWith("> <SUB_CLASS>"))
						state = STATE_SUBCLASS;
					else if (line.startsWith("> <CLASS_LEVEL4>"))
						state = STATE_CLASSLVL4;
					else if (line.startsWith("> <FORMULA>"))
						state = STATE_FORMULA;
				}
				else if (state == STATE_ID)
				{
					id = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_SYSTEMATICNAME)
				{
					systematicname = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_COMMONNAME)
				{
					commonname = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_CATEGORY)
				{
					category = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_CLASS)
				{
					mainclass = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_SUBCLASS)
				{
					subclass = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_CLASSLVL4)
				{
					classlvl4 = line;
					state = STATE_BROWSING;
				}
				else if (state == STATE_FORMULA)
				{
					formula = line;
					state = STATE_BROWSING;
				}
			}
			
			// write the data
			MoleculeIO.writeXml(molecules, output);
		}
		catch (Exception e)
		{
			Tool.unexpectedError(e, application);
		}
	}
}
