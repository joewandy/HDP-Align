package com.joewandy.mzmatch.query;

import java.util.Set;

import peakml.chemistry.Molecule;

public interface CompoundQuery {

	public static final String QUERY_HOST_LOCAL = "localhost";
	public static final int QUERY_PORT_LOCAL = 5984;
	public static final String QUERY_PATH_LOCAL = "/compounds/_design/identify/_view/mass";
	
	public Set<Molecule> findCompoundsByMass(double mass, double ppm, double delta) throws Exception;
		
	public Set<Molecule> getResult();
	
}
