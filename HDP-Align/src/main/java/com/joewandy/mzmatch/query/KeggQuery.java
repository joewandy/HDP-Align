package com.joewandy.mzmatch.query;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.joewandy.alignmentResearch.main.MultiAlignConstants;

import domsax.XmlParserException;

import peakml.chemistry.Mass;
import peakml.chemistry.Molecule;
import peakml.io.chemistry.MoleculeIO;

public class KeggQuery extends BaseQuery implements CompoundQuery {

	private static final String KEGG_PATH = "/home/joewandy/Project/mzMatch/scripts/standards/kegg.xml";
	private List<Molecule> localDb;
	
	public KeggQuery(Map<String, Molecule> molecules) throws FileNotFoundException, IOException, XmlParserException {
		
		super.result = new HashSet<Molecule>();
		super.lookup = new HashMap<String, Integer>();
		for (Molecule mol : molecules.values()) {
			super.lookup.put(mol.getPlainFormula(), 1);
		}
		
		Map<String, Molecule> kegg = MoleculeIO.parseXml(new FileInputStream(KeggQuery.KEGG_PATH));
		this.localDb = new ArrayList<Molecule>(kegg.values());

	}

	public Set<Molecule> findCompoundsByMass(double mass, double ppm, double delta) throws Exception {

		System.out.print("Querying local KEGG");
		System.out.print("\tmass=" + String.format(MultiAlignConstants.MASS_FORMAT, mass));
		System.out.print("\tdelta=" + String.format(MultiAlignConstants.MASS_FORMAT, delta));
		System.out.println("\tppm=" + String.format("%.1f", ppm));
		
		Set<Molecule> rows = new HashSet<Molecule>();
		for (Molecule v : this.localDb) {
			double myMass = v.getMass(Mass.MONOISOTOPIC);
			// TODO: double comparison
			if (myMass > (mass-delta) && myMass < (mass+delta)) {
				rows.add(v);
			}
		}
		
	    // filter out the isomers
	    System.out.println("Found " + rows.size() + " matching compound(s) in mass range");
	    Set<Molecule> retrieved = new HashSet<Molecule>();
	    for (Molecule row : rows) {
	    	if (lookup.get(row.getPlainFormula()) == null) {
				System.out.println("\t" + row.getPlainFormula() + " " + row.getMass(Mass.MONOISOTOPIC));	    		
		    	retrieved.add(row);
	    	}
	    }

	    result.addAll(retrieved);	    	
	    return retrieved;
		
	}
		
}
