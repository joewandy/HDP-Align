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



package peakml.chemistry;


// java
import java.util.*;





/**
 * This class stores the periodic table of elements with precise masses. At present
 * not all the elements are available, check the enumated ints for a complete list
 * of all the available options. The weights and other information is taken from
 * the national institute of standardization (nist).
 * <p />
 * Next to the elements this class provides some convenience methods like a PPM calculator.
 */
public abstract class PeriodicTable
{
	// elements
	/** The index of the carbon element in the {@link PeriodicTable#elements} array. */
	public static final int CARBON			=  0;
	/** The index of the hydrogen element in the {@link PeriodicTable#elements} array. */
	public static final int HYDROGEN		=  1;
	/** The index of the deuterium element in the {@link PeriodicTable#elements} array. */
	public static final int DEUTERIUM		=  2;
	/** The index of the tritium element in the {@link PeriodicTable#elements} array. */
	public static final int TRITIUM			=  3;
	/** The index of the fluor element in the {@link PeriodicTable#elements} array. */
	public static final int FLUORINE		=  4;
	/** The index of the nitrogen element in the {@link PeriodicTable#elements} array. */
	public static final int NITROGEN		=  5;
	/** The index of the oxygen element in the {@link PeriodicTable#elements} array. */
	public static final int OXYGEN			=  6;
	/** The index of the phosphorus element in the {@link PeriodicTable#elements} array. */
	public static final int PHOSPHORUS		=  7;
	/** The index of the chlorine element in the {@link PeriodicTable#elements} array. */
	public static final int CHLORINE		=  8;
	/** The index of the sulfur element in the {@link PeriodicTable#elements} array. */
	public static final int SULFUR			=  9;
	/** The index of the natrium element in the {@link PeriodicTable#elements} array. */
	public static final int NATRIUM			= 10;
	/** The index of the potassium element in the {@link PeriodicTable#elements} array. */
	public static final int POTASSIUM		= 11;
	/** The index of the copper element in the {@link PeriodicTable#elements} array. */
	public static final int COPPER			= 12;
	/** The index of the calcium element in the {@link PeriodicTable#elements} array. */
	public static final int CALCIUM			= 13;
	/** The index of the selenuum element in the {@link PeriodicTable#elements} array. */
	public static final int SELENIUM		= 14;
	/** The index of the lithium element in the {@link PeriodicTable#elements} array. */
	public static final int LITHIUM			= 15;
	/** The index of the bromide element in the {@link PeriodicTable#elements} array. */
	public static final int BROMIDE			= 16;
	/** The index of the magnesium element in the {@link PeriodicTable#elements} array. */
	public static final int MAGNESIUM		= 17;
	/** The index of the iodine element in the {@link PeriodicTable#elements} array. */
	public static final int IODINE			= 18;
	/** The index of the iron element in the {@link PeriodicTable#elements} array. */
	public static final int IRON			= 19;
	/** The index of the magnese element in the {@link PeriodicTable#elements} array. */
	public static final int MANGANESE		= 20;
	/** The index of the zinc element in the {@link PeriodicTable#elements} array. */
	public static final int ZINC			= 21;
	/** The index of the cobalt element in the {@link PeriodicTable#elements} array. */
	public static final int COBALT			= 22;
	/** The index of the nickel element in the {@link PeriodicTable#elements} array. */
	public static final int NICKEL			= 23;
	/** The index of the tungsten element in the {@link PeriodicTable#elements} array. */
	public static final int TUNGSTEN		= 24;
	/** The index of the bromine element in the {@link PeriodicTable#elements} array. */
	public static final int BROMINE			= 25;
	/** The index of the silicon element in the {@link PeriodicTable#elements} array. */
	public static final int SILICON			= 26;
	/** The index of the cesium element in the {@link PeriodicTable#elements} array. */
	public static final int CESIUM			= 27;
	/** The index of the arsenic element in the {@link PeriodicTable#elements} array. */
	public static final int ARSENIC			= 28;
	/** The index of the chromium element in the {@link PeriodicTable#elements} array. */
	public static final int CHROMIUM		= 29;
	/** The index of the aluminum element in the {@link PeriodicTable#elements} array. */
	public static final int ALUMINUM		= 30;
	/** The index of the molybdenum element in the {@link PeriodicTable#elements} array. */
	public static final int MOLYBDENUM		= 31;
	/** The index of the rubidum element in the {@link PeriodicTable#elements} array. */
	public static final int RUBIDUM			= 32;
	/** The index of the zirconium element in the {@link PeriodicTable#elements} array. */
	public static final int ZIRCONIUM		= 33;
	// start of generally used shorthands
	/** The index of methanol in the {@link PeriodicTable#elements} array. */
	public static final int METHANOL		= 34;
	/** The index of acetonitrile in the {@link PeriodicTable#elements} array. */
	public static final int ACETONITRILE	= 35;
	/** The number of elements in the {@link PeriodicTable#elements} array. */
	public static final int NR_ELEMENTS		= 36;
	
	/**
	 * Static, at startup initialized array of all the elements kept in this class. The
	 * index of the elements can be located with the static integers. For each element
	 * general properties like its valency is recorded. Next to this all the isotopes
	 * are recorded, which can be found in mass ascending order.
	 */
	public static final Element elements[] = new Element[] {
			new Element(CARBON, "carbon", "C", 12.01070000000, 0.011021, 4, new Isotope[] {
					new Isotope(12, 12.00000000000,  98.9300),
					new Isotope(13, 13.00335483780,   1.0700),
					new Isotope(14, 14.00324198800,   0.0000),
				}),
			new Element(HYDROGEN, "hydrogen", "H", 1.00794000000, 0.000152, 1, new Isotope[] {
					new Isotope( 1,  1.00782503214,  99.9885),
					new Isotope( 2,  2.01410177800,   0.0115),
					new Isotope( 3,  3.0160492675,    0.0000),
				}),
			new Element(DEUTERIUM, "deuterium", "D", 2*1.00794000000, 0.000152, 1, new Isotope[] {
					new Isotope( 2,  2.01410177800,   0.0115),
				}),
			new Element(TRITIUM, "tritium", "T", 3*1.00794000000, 0.000152, 1, new Isotope[] {
					new Isotope( 3,  3.0160492675,    0.0000),
				}),
			new Element(FLUORINE, "fluorine", "F", 18.99840325000, 0.000000, 1, new Isotope[] {
					new Isotope(19, 18.99840320500, 100.0000),
				}),
			new Element(NITROGEN, "nitrogen", "N", 14.00670000000, 0.003628, 3, new Isotope[] {
					new Isotope(14, 14.00307400524,  99.6320),
					new Isotope(15, 15.00010889840,   0.3680),
				}),
			new Element(OXYGEN, "oxygen", "O", 15.99940000000, 0.008536, 2, new Isotope[] {
					new Isotope(16, 15.99491462210,  99.7570),
					new Isotope(18, 17.99916040000,   0.2050),
					new Isotope(17, 16.99913150000,   0.0380),
				}),
			new Element(PHOSPHORUS, "phosphor", "P", 30.97376149000, 0.000000, 3, new Isotope[] {
					new Isotope(31, 30.97376151200, 100.0000),
				}),
			new Element(CHLORINE, "chlorine", "Cl", 35.45273000000, 0.737129, 1, new Isotope[] {
					new Isotope(35, 34.96885271000,  75.7800),
					new Isotope(37, 36.96590260000,  24.2200),
					new Isotope(36, 35.96830695000,   0.0000),
				}),
			new Element(SULFUR, "sulfur", "S", 32.06533000000, 0.169853, 2, new Isotope[] {
					new Isotope(32, 31.97207069000,  94.9300),
					new Isotope(34, 33.96786683000,	  4.2900),
					new Isotope(33, 32.97145850000,   0.7600),
					new Isotope(35, 34.96903214000,   0.0000),
					new Isotope(36, 35.96708088000,   0.0200),
				}),
			new Element(NATRIUM, "natrium", "Na", 22.989770, 0.000000, 1, new Isotope[] {
					new Isotope(23, /*22.9892213*/22.98976967, 100.0000),
				}),
			new Element(POTASSIUM, "potassium", "K",  39.0983, 0.250703, 1, new Isotope[] {
					new Isotope(39, 38.96370668000,	93.2581),
					new Isotope(41, 40.96182576000, 6.7302),
					new Isotope(40, 39.96399848000, 0.0117),
				}),
			new Element(COPPER, "copper", "Cu", 63.5463, 0, 0, new Isotope[] {
					new Isotope(63,	62.929601115,	69.1730),
					new Isotope(65, 64.927793719,	30.8330),
				}),
			new Element(CALCIUM, "calcium", "Ca", 40.078, 0, 0, new Isotope[] {
					new Isotope(40,	39.9625912,	96.9412),
				}),
			new Element(SELENIUM, "Selenium", "Se", 78.96, 0, 0, new Isotope[] {
					new Isotope(80, 79.9165218, 49.6141),
					new Isotope(82, 81.9167000,  8.7322),
				}),
			new Element(LITHIUM, "lithium", "Li", 6.9412, 0, 0, new Isotope[] {
					new Isotope(7, 7.01600405, 92.41),
					new Isotope(6, 6.01512235,  7.59),
				}),
			new Element(BROMIDE, "bromide", "Br", 79.904, 0, 0, new Isotope[] {
					new Isotope(79, 78.9183376, 50.69),
					new Isotope(81, 80.9162910, 49.31),
				}),
			new Element(MAGNESIUM, "magnesium", "Mg", 24.3050, 0, 0, new Isotope[] {
					new Isotope(24, 23.9850419020,  78.99),
					new Isotope(26, 25.9825930421,  11.01),
					new Isotope(25, 24.9858370220,  10.00),
				}),
			new Element(IODINE, "Iodine", "I", 126.90447, 0, 0, new Isotope[] {
					new Isotope(127, 126.904468, 100),
				}),
			new Element(IRON, "Iron", "Fe", 55.845, 0, 0, new Isotope[] {
					new Isotope(56, 55.9349421, 91.754),
					new Isotope(54, 53.9396148, 5.845),					
					new Isotope(57, 56.9353987, 2.119),
					new Isotope(58, 57.9332805, 0.282),
				}),
			new Element(MANGANESE, "Manganese", "Mn", 54.938049, 0, 0, new Isotope[] {
					new Isotope(55, 54.9380496, 100),
				}),
			new Element(ZINC, "Zinc", "Zn", 65.409, 0, 0, new Isotope[] {
					new Isotope(64, 63.9291466, 48.63),
					new Isotope(66, 65.9260368, 27.90),
					new Isotope(68, 67.9248476, 18.75),
					new Isotope(67, 66.9271309, 4.10),
					new Isotope(70, 69.925325, 0.62),
				}),
			new Element(COBALT, "Cobalt", "Co", 58.933200, 0, 0, new Isotope[] {
					new Isotope(59, 58.9332002, 100),
				}),
			new Element(NICKEL, "Nickel", "Ni", 58.6934, 0, 0, new Isotope[] {
					new Isotope(58, 57.9353479, 68.0769),
					new Isotope(60, 59.9307906, 26.2231),
					new Isotope(62, 61.9283488, 3.6345),
					new Isotope(61, 60.9310604, 1.1399),
					new Isotope(64, 63.9279696, 0.9256),
				}),
			new Element(TUNGSTEN, "Tungsten", "W", 183.84, 0, 0, new Isotope[] {
					new Isotope(184, 183.9509326, 30.64),
					new Isotope(186, 185.954362, 28.43),
					new Isotope(182, 181.948206, 26.50),
					new Isotope(183, 182.9502245, 14.31),
					new Isotope(180, 179.946706, 0.12),
				}),
			new Element(BROMINE, "Bromine", "Br", 79.904, 0, 0, new Isotope[] {
					new Isotope(79, 78.9183376, 50.69),
					new Isotope(81, 80.916291, 49.31),
				}),
			new Element(SILICON, "Silicon", "Si", 28.0855, 0, 0, new Isotope[] {
					new Isotope(28, 27.9769265327, 92.2297),
					new Isotope(29, 28.9764947200,  4.6832),
					new Isotope(30, 29.9737702200,  3.0872),
				}),
			new Element(CESIUM, "Cesium", "Cs", 132.90545, 0, 0, new Isotope[] {
					new Isotope(133, 132.905447, 100),
				}),
			new Element(ARSENIC, "Arsenic", "As", 74.92160, 0, 0, new Isotope[] {
					new Isotope(75, 74.9215964, 100),
				}),
			new Element(CHROMIUM, "Chromium", "Cr", 51.9961, 0, 0, new Isotope[] {
					new Isotope(52, 51.9405119, 83.789),
					new Isotope(53, 52.9406538,  9.501),
					new Isotope(50, 49.9460496,  4.345),
					new Isotope(54, 53.9388849,  2.365),
				}),
			new Element(ALUMINUM, "Aluminum", "Al", 26.981538, 0, 0, new Isotope[] {
					new Isotope(27, 26.98153844, 100),
				}),
			new Element(MOLYBDENUM, "Molybdenum", "Mo", 95.94, 0, 0, new Isotope[] {
					new Isotope(98, 97.9054078, 24.13),
					new Isotope(96, 95.9046789, 16.68),
					new Isotope(95, 94.9058415, 15.92),
					new Isotope(92, 91.906810, 14.84),
					new Isotope(97, 96.9060210, 9.55),
					new Isotope(94, 93.9050876, 9.25),
					new Isotope(100, 99.907477, 9.63),
				}),
			new Element(RUBIDUM, "Rubidum", "Rb", 85.4678, 0, 0, new Isotope[] {
					new Isotope(85, 84.9117893, 72.17),
					new Isotope(87, 86.9091835, 27.83),
				}),
			new Element(ZIRCONIUM, "Zirconium", "Zr", 91.224, 0, 0, new Isotope[] {
					new Isotope(90, 89.9047037, 51.45),
					new Isotope(92, 91.9050401, 17.15),
					new Isotope(94, 93.9063158, 17.38),
					new Isotope(91, 90.9056450, 11.22),
					new Isotope(96, 95.9082760,  2.80),
				}),
			// start of generally used shorthands
			new Element(METHANOL, "Methanol", "Me", 15.03452, 0, 0, new Isotope[] {
					new Isotope(32, 15.023475096420002, 100),
				}),
			new Element(ACETONITRILE, "Acetonitrile", "Acn", 41.05192, 0, 0, new Isotope[] {
					new Isotope(41, 41.02654910166, 100),
				}),
		};
	
	protected static HashMap<String,Element> indexed_elements = new HashMap<String,Element>();
	static {
		// index all the elements on the id's
		for (Element element : elements)
			indexed_elements.put(element.identifier, element);
	}
	
	private static double mass(String formula)
	{
		return new MolecularFormula(formula).getMass(Mass.MONOISOTOPIC);
	}
	
	
	/**
	 * Returns the element with the given identifier (e.g. carbon is identified with C). When
	 * the element is not stored in the class null is returned.
	 * 
	 * @return				The element with the given identifier.
	 */
	public static Element getElement(String identifier)
	{
		return indexed_elements.get(identifier);
	}
	
	/**
	 * Returns the difference of the most common isotope of the given element to the given
	 * isotope. When the element does not exist or the element does not have the indicated
	 * isotope -1 is returned.
	 * 
	 * @param element		The index of the element.
	 * @param isotope		The index of the isotope.
	 * @return				The mass difference between the most common isotope and the given isotope.
	*/
	public static double getIsotopeMassDifference(int element, int isotope)
	{
		if (element<0 || element>=NR_ELEMENTS)
			return -1;
		Element e = elements[element];
		if (isotope<=0 || isotope>=e.getNrIsotopes())
			return -1;
		
		return e.getMonoIsotopicWeight(isotope) - e.getMonoIsotopicWeight();
	}
	
	/**
	 * Central class describing all the properties of an element. Properties include
	 * the valency, molecular weight, etc. Apart from this also all the known isotopes
	 * are tracked here.
	 * 
	 * http://physics.nist.gov/PhysRefData/Compositions/index.html
	 */
	public static class Element
	{
		// constructor(s)
		/**
		 * Constructs a new element with the given information.
		 * 
		 * @param id				The index of the isotope in the list of elements.
		 * @param name				The full length name of the element.
		 * @param identifier		The short name for the element.
		 * @param molecularweight	The molecular weight of the element
		 * @param massvariance		The mass variance of the element.
		 * @param valency			The valency of the element.
		 * @param isotopes			The list of isotopes.
		 */
		public Element(int id, String name, String identifier, double molecularweight, double massvariance, int valency, Isotope... isotopes)
		{
			this.id = id;
			this.name = name;
			this.identifier = identifier;
			this.molecularweight = molecularweight;
			this.massvariance = massvariance;
			this.valency = valency;
			this.isotopes = isotopes;
		}
		
		
		// Element access
		/**
		 * Returns the index of the element in the table {@link PeriodicTable#elements}.
		 * 
		 * @return				The index of the element.
		 */
		public int getID()
		{
			return id;
		}
		
		/**
		 * Returns the full name of the element.
		 * 
		 * @return				The full name of the element.
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Returns the id (e.g. carbon has C) of the element.
		 * 
		 * @return				The id of the element.
		 */
		public String getIdentifier()
		{
			return identifier;
		}
		
		/**
		 * Returns the mass of the element. The given masstype indicates whether the returned
		 * mass should be molecular mass (the mixture of all isotopes as they occur naturally)
		 * or mono-isotopic (the mass of the most common isotope).
		 * 
		 * @param masstype		The type of mass to return.
		 * @return				The mass of the element.
		 */
		public double getMass(Mass masstype)
		{
			if (masstype == Mass.MOLECULAR)
				return molecularweight;
			else
				return getMonoIsotopicWeight(0);
		}
		
		/**
		 * Returns the molecular weight of the isotope (the mixture of all isotopes as they occur
		 * naturally).
		 * 
		 * @return				The molecular weight.
		 */
		public double getMolecularWeight()
		{
			return molecularweight;
		}
		
		/**
		 * Returns the mass variance for the element.
		 * 
		 * @return				The mass variance for the element.
		 */
		public double getMassVariance()
		{
			return massvariance;
		}
		
		/**
		 * Returns the valency of the element. The valency denotes the number of 'connections'
		 * an element has to other atoms in a molecule. For example the carbon can be connected
		 * to 4 other elements with single bonds (a double bond counts for two connections).
		 * 
		 * @return				The valency for the element.
		 */
		public final int getValency()
		{
			return valency;
		}
		
		/**
		 * Returns the number of isotopes for this element.
		 * 
		 * @return				The number of isotopes for the element.
		 */
		public int getNrIsotopes()
		{
			return isotopes.length;
		}
		
		/**
		 * Returns an array of all the isotopes for the element.
		 * 
		 * @return				An array with the isotopes.
		 */
		public Isotope[] getIsotopes()
		{
			return isotopes;
		}
		
		/**
		 * Returns the atomic mass of the most abundant isotope.
		 * 
		 * @return				The atomic mass of the most abundant isotope.
		 */
		public int getAtomicMass()
		{
			return getAtomicMass(0);
		}
		
		/**
		 * Returns the mono-isotopic weight of the most abundant isotope.
		 * 
		 * @return				The mono-isotopic weight of the most abundant isotope.
		 */
		public double getMonoIsotopicWeight()
		{
			return getMonoIsotopicWeight(0);
		}
		
		/**
		 * Returns the abundance (in percentage) at which the most abundant isotope
		 * occurs naturally.
		 * 
		 * @return				The natural abundancy of the most abundant isotope.
		 */
		public double getAbundance()
		{
			return getAbundance(0);
		}
		
		
		// Isotope access
		/**
		 * Returns the isotope instance at the given position (0 is the most abundant
		 * isotope).
		 * 
		 * @param k				The index of the isotope.
		 * @return				The isotope at the given index.
		 */
		public Isotope getIsotope(int k)
		{
			return isotopes[k];
		}
		
		/**
		 * Returns the atomic mass of the given isotope.
		 * 
		 * @param k				The index of the isotope.
		 * @return				The atomic mass of the isotope.
		 */
		public int getAtomicMass(int k)
		{
			return isotopes[k].atomicmass;
		}
		
		/**
		 * Returns the mono isotopic weight of the isotope at the given index.
		 * 
		 * @param k				The index of the isotope.
		 * @return				The mono isotopic weight of the isotope.
		 */
		public double getMonoIsotopicWeight(int k)
		{
			return isotopes[k].monoisotopicweight;
		}
		
		/**
		 * Returns the natural abundance of the isotope at the given index.
		 * 
		 * @param k				The index of the isotope.
		 * @return				The natural abundance of the isotope.
		 */
		public double getAbundance(int k)
		{
			return isotopes[k].abundance;
		}
		
		
		// data
		/** The index of the isotope in the list of elements. */
		public final int id;
		/** The full length name of the element. */
		public final String name;
		/** The short name for the element. */
		public final String identifier;
		/** The molecular weight of the element. */
		public final double molecularweight;
		/** The mass variance of the element. */
		public final double massvariance;
		/** The valency of the element. */
		public final int valency;
		/** The list of isotopes. */
		public final Isotope[] isotopes;
	}
	
	/**
	 * Central class describing all the properties of an isotope.
	 */
	public static class Isotope
	{
		// constructor(s)
		/**
		 * Constructs a new instance of an isotope with the given properties.
		 * 
		 * @param atomicmass			Integer representation of the mass (this is also used to indicate the isotope for example C13).
		 * @param monoisotopicweight	The precise mass of the isotope.
		 * @param abundance				The natural abundance of the element.
		 */
		public Isotope(int atomicmass, double monoisotopicweight, double abundance)
		{
			this.atomicmass = atomicmass;
			this.monoisotopicweight = monoisotopicweight;
			this.abundance = abundance;
		}
		
		
		// data
		/** Integer representation of the mass (this is also used to indicate the isotope for example C13). */
		public final int atomicmass;
		/** The precise mass of the isotope. */
		public final double monoisotopicweight;
		/** The natural abundance of the element. */
		public final double abundance;
	}
	
	
	// natural isotopes
	public static final NaturalIsotope natural_isotopes[] = new NaturalIsotope[] {
		new NaturalIsotope("c13 isotope",	getIsotopeMassDifference(CARBON, 1)),
		new NaturalIsotope("n15 isotope",	getIsotopeMassDifference(NITROGEN, 1)),
		new NaturalIsotope("o18 isotope",	getIsotopeMassDifference(OXYGEN, 2)),
		new NaturalIsotope("s18 isotope",	getIsotopeMassDifference(SULFUR, 2)),
	};
	
	public static class NaturalIsotope
	{
		// constructor(s)
		public NaturalIsotope(String name, double mass)
		{
			this.name = name;
			this.mass = mass;
		}
		
		
		// data
		public final String name;
		public final double mass;
	}
	
	
	// adducts
	/** The derivative can only be an adduct */
	public static final int ADDUCT			= 0;
	/** The derivative can only be a deduct */
	public static final int DEDUCT			= 1;
	/** The derivative can be both an adduct as well as a deduct */
	public static final int ADDUCT_DEDUCT	= 2;
	
	/**
	 * Static, at startup initialized array of all the positive adducts kept in this
	 * class. The index of the adducts can be located with the static integers. For each
	 * adduct properties like the mass difference are recorded.
	 * 
	 * http://maltese.dbs.aber.ac.uk:8888/hrmet/search/disprules.php
	 */
	public static final Derivative adducts_positive[] = new Derivative[] {
		new Derivative(ADDUCT,			1, 1, "IsoProp",								"[M+C3H8O]+",			mass("C3H8O")),
		new Derivative(ADDUCT, 			1, 1, "IsoProp+Na",								"[M+C3H8O+Na]+",		mass("C3H8O") + mass("Na")),
		new Derivative(ADDUCT, 			1, 1, "DMSO",									"[M+C2H6OS]+",			mass("C2H6OS")),
		
		new Derivative(ADDUCT_DEDUCT,	1, 1, "hydrogen",								"[M+H]+",				mass("H")),
		new Derivative(ADDUCT,			1, 1, "copper", 								"[M+Cu]+",				mass("Cu")-mass("H")), 
		new Derivative(ADDUCT, 			1, 1, "calcium", 								"[M+Ca]+",				mass("Ca")-mass("H")),
		new Derivative(ADDUCT,			1, 1, "potassium",								"[M+K]+",				mass("K")-mass("H")),
		new Derivative(ADDUCT,			2, 1, "2M+K",									"[2M+K]+",				mass("K")-mass("H")),
		
		new Derivative(ADDUCT,			1, 1, "methylation (-H2O)",						"[M+CH2]+",				mass("CH2")),//
		new Derivative(ADDUCT_DEDUCT,	1, 1, "formic acid",							"[M+/-HCOOH]+",			mass("CH2O2")),//
		new Derivative(ADDUCT_DEDUCT,	1, 1, "formic acid + H",						"[M+/-HCOOH+H]+",		mass("CH2O2") + mass("H")),//
		new Derivative(DEDUCT,			1, 1, "formic acid loss (double bond)",			"[M-HCOOH]+",			mass("CH2O2")),//
		
		new Derivative(ADDUCT,			1, 1, "acetonitrile",							"[M+ACN]+",				mass("C2H3N")),
		new Derivative(ADDUCT,			1, 1, "2*acetonitrile",							"[M+2ACN+H]+",			mass("C2H3N") + mass("C2H3N")),//double charged
		new Derivative(ADDUCT,			1, 1, "acetonitrile+sodium",					"[M+ACN+Na]+",			mass("C2H3N") + mass("Na")),//
		new Derivative(ADDUCT,			2, 1, "2M+ACN+H",								"[2M+ACN+H]+",			mass("C2H3N")),
		new Derivative(ADDUCT,			2, 1, "2M+ACN+Na",								"[2M+ACN+Na]+",			mass("C2H3NaN") - mass("H")),
		
		new Derivative(DEDUCT,			1, 1, "M-H2O+acetonitrile",						"[M�H2O+ACN]+",			mass("H2O") - mass("Acn")),
		new Derivative(DEDUCT,			1, 1, "M-HCOOH+acetonitrile",					"[M�HCOOH+ACN]+",		mass("HCOOH") - mass("Acn")),
		new Derivative(DEDUCT,			1, 1, "M-NH3+acetonitrile",						"[M�NH3+ACN]+",			mass("NH3") - mass("Acn")),
		
		new Derivative(ADDUCT_DEDUCT,	1, 1, "ammonium", 								"[M+NH3]+",				mass("NH3")),
		new Derivative(ADDUCT_DEDUCT,	2, 1, "2M+NH3", 								"[2M+NH3]+",			mass("NH3")),
		new Derivative(ADDUCT, 			1, 1, "methanol",								"[M+CH3OH+H]+",			mass("CH4O")),
		new Derivative(ADDUCT,	 		1, 1, "sodium-formate",							"[M+HCO2Na]+",			mass("HCO2Na")),// 
		new Derivative(ADDUCT,			1, 1, "amonium-formate",						"[M+HCOONH4]+",			mass("H4CO2N")),
		new Derivative(DEDUCT, 			1, 1, "water",									"[M+/-H2O]+",			mass("H2O")), 
		new Derivative(ADDUCT_DEDUCT, 	1, 1, "double bond",							"[M+/-H2]+",			mass("H2")), 
		new Derivative(ADDUCT, 			1, 1, "glycine (-H2O)",							"[M+C2H3NO]+",			mass("C2H3NO")),
		new Derivative(ADDUCT, 			1, 1, "ethyl (-H2O)",							"[M+C2H4]+",			mass("C2H4")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "oxygen",									"[M+/-O2]+",			mass("O2")),
		
		new Derivative(ADDUCT,			1, 1, "sodiated",								"[M+Na]+",				mass("Na") - mass("H")),
		new Derivative(ADDUCT,			1, 1, "double sodiated",						"[M+2Na]+",				2*mass("Na") - mass("H")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "salt (Na<>NH4)",							"[M+/-(Na-NH4)]+",		mass("Na") - mass("NH4")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "salt (Na<>K)",							"[M+/-(K-Na)]+",		mass("K") - mass("Na")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "salt (K<>NH4)",							"[M+/-(K-NH4)]+",		mass("K") - mass("NH4")),
		new Derivative(ADDUCT,			2, 1, "2M+Na",									"[2M+Na]+",				mass("Na") - mass("H")),
		
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (F) exchange (hydroxy)",			"[M+/-(F-OH)]+",		mass("F") - mass("OH")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (F) exchange (cyano)",			"[M+/-(F-CN)]+",		mass("CN") - mass("F")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (F) exchange (hydrogen)",		"[M+/-(F-H)]+",			mass("F") - mass("H")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (Cl) exchange (cyano)",			"[M+/-(Cl-CN)]+",		mass("Cl") - mass("CN")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (Cl) exchange (hydroxy)",		"[M+/-(Cl-OH)]+",		mass("Cl") - mass("OH")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (Cl) exchange (hydrogen)",		"[M+/-(Cl-H)]+",		mass("Cl") - mass("H")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (I) exchange (chlorine)",		"[M+/-(I-Cl)]+",		mass("I") - mass("Cl")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (I) exchange (cyano)",			"[M+/-(I-CN)]+",		mass("I") - mass("CN")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (I) exchange (hydroxy)",			"[M+/-(I-OH)]+",		mass("I") - mass("OH")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "halogen (I) exchange (hydrogen)",		"[M+/-(I-H)]+",			mass("I") - mass("H")),
		// -> the Br has the different isotopes

		new Derivative(ADDUCT_DEDUCT,	1, 1, "S<>O (sulfur compounds)",				"[M+/-(S-O)]+",			mass("S") - mass("O")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-S (sulfur compounds)",				"[M+/-S]+",				mass("S")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-H2S (sulfur compounds)",				"[M+/-H2S]+",			mass("H2S")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-SO (sulfur compounds)",				"[M+/-SO]+",			mass("SO")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-SO2 (sulfur compounds)",				"[M+/-SO2]+",			mass("SO2")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-SO3 (sulfur compounds)",				"[M+/-SO3]+",			mass("SO3")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-H2SO4 (sulfur compounds)",			"[M+/-H2SO4]+",			mass("H2SO4")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "CN<>COOH (nitrile compounds)",			"[M+/-(COOH-CN)]+",		mass("CO2H") - mass("CN")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "CN<>H (nitrile compounds)",				"[M+/-(CN-H)]+",		mass("CN") - mass("H")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "HCN (nitrile compounds)",				"[M+/-HCN]+",			mass("HCN")),
//		new Derivative(ADDUCT_DEDUCT,	1, 1, "C2N<>C2OOH (nitro compounds)",			"[M+/-(C2N2-C2OOH)]+",	mass("C2O2H") - mass("C2N")),
		new Derivative(DEDUCT,			1, 1, "-NO (nitroso compounds)",				"[M+/-NO]+",			mass("NO")),
		new Derivative(DEDUCT,			1, 1, "-NH2OH (hydroxamin compounds)",			"[M+/-NH2OH]+",			mass("NH3O")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-H3PO4 (phosphorous compounds)",		"[M+/-H3PO4]+",			mass("H3PO4")),
		
		new Derivative(ADDUCT_DEDUCT,	1, 1, "de-amination",							"[M+/-(OH-NH2)]+",		mass("OH") - mass("NH2")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "oxidation > H2O elimination",			"[M+/-(O-H2)]+",		mass("O") - mass("H2")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "oxidation/reduction",					"[M+/-O]+",				mass("O")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "carbon monoxide",						"[M+/-CO]+",			mass("CO")),
		new Derivative(DEDUCT,			1, 1, "nitrogen loss (azido compounds)",		"[M-N2]+",				mass("N2")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "natural alkane chains",					"[M+/-C2H4]+",			mass("C2H4")),
		new Derivative(ADDUCT,			1, 1, "acetone condisation after dehydration",	"[M+(C3H6O-H2O)]+",		mass("C3H6O") - mass("H2O")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-COCH2",								"[M+/-COCH2]+",			mass("C2OH2")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "propylation",							"[M+/-C3H6]+",			mass("C3H6")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "CONH",									"[M+/-CONH]+",			mass("CONH")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "CO2",									"[M+/-CO2]+",			mass("CO2")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "butylation",								"[M+/-C4H8]+",			mass("C4H8")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "+/-CO2CH2",								"[M+/-CO2CH2]+",		mass("C2H2O2")),
		new Derivative(ADDUCT,			1, 1, "acetone condensation",					"[M+C3H6O]+",			mass("C3H6O")),
		new Derivative(DEDUCT,			1, 1, "loss from singly oxidized methionine R",	"[M-CH3SOH]+",			mass("CH4SO")),
		
//		new Derivative(ADDUCT_DEDUCT,	1, 1, "fucose",									"[M+/-]+",		mass("")),
//		new Derivative(ADDUCT_DEDUCT,	1, 1, "fucose",									"[M+/-]+",		mass("")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "hexose",									"[M+/-C6O6H12]+",		mass("C6O6H12")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "hexose-H2O",								"[M+/-C6O5H10]+",		mass("C6O5H10")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "glucoronic acid",						"[M+/-C6O7H10]+",		mass("C6O7H10")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "glucoronic acid-H2O",					"[M+/-C6O6H8]+",		mass("C6O6H8")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "HexNAc",									"[M+/-C8O6NH15]+",		mass("C8O6NH15")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "HexNAc-H2O",								"[M+/-C8O5NH13]+",		mass("C8O5NH13")),
		new Derivative(ADDUCT,			1, 1, "sinapic acid-H2O",						"[M+C8O5NH13]+",		mass("C11H10O4")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "glutathione",							"[M+/-C10O6N3SH17]+",	mass("C10O6N3SH17")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "glutathione-H2O",						"[M+/-C10O5N3SH15]+",	mass("C10O5N3SH15")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "glutathione+O-H2O",						"[M+/-C10O6N3SH15]+",	mass("C10O6N3SH15")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "Neu5Ac",									"[M+/-C11O9NH19]+",		mass("C11O9NH19")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "Neu5Ac-H2O",								"[M+/-C11O8NH17]+",		mass("C11O8NH17")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "Sucrose",								"[M+/-C12O11H22]+",		mass("C12O11H22")),
		new Derivative(ADDUCT_DEDUCT,	1, 1, "Sucrose-H2O",							"[M+/-C12O10H20]+",		mass("C12O10H20")),
		new Derivative(ADDUCT,			1, 1, "tetraphenyl-tetramethyl-trisiloxane",	"[M+/-C28H32O2Si3]+",	mass("C28H32O2Si3")),
		new Derivative(ADDUCT,			1, 1, "pentaphenyl-trimethyl-trisiloxane",		"[M+/-C33H34O2Si3]+",	mass("C33H34O2Si3")),
	};
	
//	/**
//	 * Static, at startup initialized array of all the negative adducts kept in this
//	 * class. The index of the adducts can be located with the static integers. For each
//	 * adduct properties like the mass difference are recorded.
//	 */
//	public static final Adduct adducts_negative[] = new Adduct[] {
//		new Adduct(false, 1, 1, "formate",					"[M+HCOO]-",			+ 44.997654276340000),
//		new Adduct(false, 1, 1, "acetate",					"[M+CH3COO]-",			+ 59.013304340619996),
//		new Adduct(false, 1, 1, "nitrate",					"[M+NO3]-",				+ 61.987817871540000),
//		new Adduct(false, 1, 1, "bioxalate",				"[M+HC2O4]-",			+ 88.987483520540000),
//		new Adduct(false, 1, 1, "hydrogen",					"[M-H]-",				-  1.007825032140000),
//		new Adduct(false, 1, 1, "chlorine",					"[M+Cl]-",				+ 34.968852710000000),
//		new Adduct(false, 1, 1, "water",					"[M-H2O]-",				- 19.01839),				// Fiehn lab list starts here
//		new Adduct(false, 1, 1, "natrium",					"[M+Na-2H]-",			+ 20.974666),
//		new Adduct(false, 1, 1, "potassium",				"[M+K-2H]-",			+ 36.948606),
//		new Adduct(false, 1, 1, "hac",						"[M+Hac-H]-",			+ 59.013851),
//		new Adduct(false, 1, 1, "bromide",					"[M+Br]-",				+ 78.918337600000000),
//		new Adduct(false, 1, 1, "trifluoroacetic acid",		"[M+TFA-H]-",			+112.985586),
//	};
	
	/**
	 * 
	 * http://mass-spec.stanford.edu/assets/SUMS_common_ESI_ions.pdf
	 * http://fiehnlab.ucdavis.edu/staff/kind/Metabolomics/MS-Adduct-Calculator/
	 */
	public static class Derivative
	{
		// constructor(s)
		/**
		 * Constructs a new adduct instance with the given information.
		 * 
		 * @param adductdeduct		Indicates whether this is only an adduct ({@link PeriodicTable#ADDUCT}), deduct ({@link PeriodicTable#DEDUCT}), or both ({@link PeriodicTable#ADDUCT_DEDUCT}).
		 * @param mer				The number of *-mer's needed for the basepeak.
		 * @param charge			The charge needed for the basepeak of the adduct.
		 * @param name				The name of the adduct.
		 * @param formula			The formula of the adduct.
		 * @param mass				The additional mass of the adduct.
		 */
		public Derivative(int adductdeduct, int mer, int charge, String name, String formula, double mass)
		{
			this.adductdeduct = adductdeduct;
			this.mer = mer;
			this.charge = charge;
			this.name = name;
			this.formula = formula;
			this.mass = mass;
		}
		
		
		// access
		/**
		 * Returns the number of *-mer's needed for the basepeak.
		 * 
		 * @return					The number of *-mer's needed for the basepeak.
		 */
		public int getMer()
		{
			return mer;
		}
		
		/**
		 * Returns the charge needed for the basepeak of the adduct.
		 * 
		 * @return					The charge needed for the basepeak.
		 */
		public int getCharge()
		{
			return charge;
		}
		
		/**
		 * Returns the name of the adduct.
		 * 
		 * @return					The name of the adduct.
		 */
		public String getName()
		{
			return name;
		}
		
		/**
		 * Returns the mass difference of the adduct.
		 * 
		 * @return					The mass difference.
		 */
		public double getMass()
		{
			return this.mass;
		}
		
		public double[] fromDerivative(double derevativemass)
		{
			if (adductdeduct == ADDUCT)
				return new double[] {(derevativemass-mass)/this.mer};
			else if (adductdeduct == DEDUCT)
				return new double[] {(derevativemass+mass)/this.mer};
			else
				return new double[] {(derevativemass-mass)/this.mer, (derevativemass+mass)/this.mer};
		}
		
//		public double[] fromBasePeak(double basepeakmass)
//		{
//			return basepeakmass
//		}
		
		/**
		 * Calculates the mass of the peak of the given basepeak mass and the adduct.
		 * 
		 * @param basepeakmass		The mass of the basepeak.
		 * @return					The mass of the peak of the adduct+basepeak.
		 */
		public double from(double basepeakmass)
		{
			return mass + this.mass;
		}
		
		/**
		 * Calculates the mass of the peak of the given basepeak mass and the adduct.
		 * 
		 * @param basepeakmass		The mass of the basepeak.
		 * @param charge			The charge of the basepeak.
		 * @return					The mass of the peak of the adduct+basepeak.
		 */
		public double getMass(double basepeakmass, int charge)
		{
			return (mass + this.mass) / charge;
		}
		
		
		// data
		/** Indicates whether this is only an adduct ({@link PeriodicTable#ADDUCT}), deduct ({@link PeriodicTable#DEDUCT}), or both ({@link PeriodicTable#ADDUCT_DEDUCT}). */
		public final int adductdeduct;
		/** The number of *-mer's needed for the basepeak. */
		public final int mer;
		/** The charge needed for the basepeak of the adduct. */
		public final int charge;
		/** The additional mass of the adduct. */
		public final double mass;
		/** The name of the adduct. */
		public final String name;
		/** The formula of the adduct. */
		public final String formula;
	}
	
	
	// general constants
	/** The precise value of one dalton. */
	public static final double dalton = elements[CARBON].getMolecularWeight() / 12.;
	/** The precise mass of an electron */
	public static final double electronmass = 0.00054857990924;
	/** The precise mass of a proton */
	public static final double proton = elements[HYDROGEN].getMonoIsotopicWeight() - electronmass;
	
	
	// general methods
	/**
	 * This method calculates the parts per million for the given mass. Parts per million ("ppm")
	 * denotes one particle of a given substance for every 999,999 other particles. This is
	 * roughly equivalent to one drop of ink in a 150 liter (40 gallon) drum of water, or one
	 * second per 280 hours. One part in 106 � a precision of 0.0001%.
	 * <p />
	 * The higher the given mass the bigger the returned value.
	 * 
	 * @param mass			The mass to be used as reference
	 * @param nr			The number of PPM (e.g. 1 PPM, 3 PPM, etc.)
	 * @return				The parts per million value.
	 */
	public static double PPM(double mass, double nr)
	{
		return nr * (0.000001*mass);
	}
	
	/**
	 * Calculates the ppm-value for the given deviation to the given mass. See
	 * {@link PeriodicTable#PPM(double, double)} for more information.
	 * 
	 * @param mass			The mass to be used as reference
	 * @param deviation		The deviation to the given mass
	 */
	public static double PPMNr(double mass, double deviation)
	{
		return (deviation*1000000) / mass;
	}
	
	/**
	 * 
	 */
	public static boolean inRange(double realmass, double observedmass, double ppm)
	{
		return Math.abs(realmass-observedmass) < PeriodicTable.PPM(Math.max(realmass, observedmass), ppm);
	}
	
	public static void main(String args[])
	{
		try
		{
//			for (Element element : PeriodicTable.elements)
//			{
//				System.out.println(element.identifier + "\t" + element.molecularweight + "\t" + element.valency);
//				for (Isotope isotope : element.isotopes)
//					System.out.println("\t" + isotope.atomicmass + "\t" + isotope.monoisotopicweight + "\t" + isotope.abundance);
//			}
			
			for (Derivative d : PeriodicTable.adducts_positive)
				System.out.println(d.name + "\t" + d.mass);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
