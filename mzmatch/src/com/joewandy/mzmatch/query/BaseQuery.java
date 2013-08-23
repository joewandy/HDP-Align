package com.joewandy.mzmatch.query;

import java.util.Map;
import java.util.Set;

import peakml.chemistry.Molecule;

public abstract class BaseQuery implements CompoundQuery {
	
	protected Map<String, Integer> lookup;
	protected Set<Molecule> result;

	public Set<Molecule> getResult() {
		return result;
	}

}
