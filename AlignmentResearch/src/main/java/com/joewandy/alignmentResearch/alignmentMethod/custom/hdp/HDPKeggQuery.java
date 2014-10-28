package com.joewandy.alignmentResearch.alignmentMethod.custom.hdp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joewandy.mzmatch.query.BaseQuery;
import com.joewandy.mzmatch.query.CompoundQuery;

import domsax.XmlParserException;
import peakml.chemistry.Mass;
import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;

public class HDPKeggQuery extends BaseQuery implements CompoundQuery {

	private static final String KEGG_PATH = "/home/joewandy/Project/mzMatch/scripts/standards/kegg.xml";
	private List<Molecule> localDb;
	
	public HDPKeggQuery() {
		
		super.result = new HashSet<Molecule>();
		try {
			Map<String, Molecule> kegg = MoleculeIO.parseXml(new FileInputStream(HDPKeggQuery.KEGG_PATH));
			this.localDb = new ArrayList<Molecule>(kegg.values());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlParserException e) {
			e.printStackTrace();
		}

	}

	public Set<Molecule> findCompoundsByMass(double mass, double ppm, double delta) throws Exception {

//		System.out.print("Querying local KEGG");
//		System.out.print("\tmass=" + String.format("%.5f", mass));
//		System.out.print("\tdelta=" + String.format("%.5f", delta));
//		System.out.println("\tppm=" + String.format("%.1f", ppm));
		
		double diff = PPM(mass, ppm*3);
		diff = Math.max(diff, delta);
		double upper = mass + diff; 
		double lower = mass - diff;
		
		Set<Molecule> rows = new HashSet<Molecule>();
		for (Molecule v : this.localDb) {
			double myMass = v.getMass(Mass.MONOISOTOPIC);
			if (lower < myMass && myMass < upper) {
				rows.add(v);
			}
		}
		
	    result.addAll(rows);	    	
	    return rows;
		
	}
	
	private double PPM(double mass, double q) {
		return q * (0.000001*mass);
	}
		
}
