package com.joewandy.alignmentResearch.model;

import java.util.HashSet;
import java.util.Set;

import peakml.chemistry.Molecule;

import com.joewandy.mzmatch.query.CompoundQuery;

public class HDPPrecursorMass implements Comparable<HDPPrecursorMass> {

	private double mass;
	private double ppm;
	private int count;
	private Set<Molecule> molecules;
	private CompoundQuery dbQuery;
	
	public HDPPrecursorMass(double mass, double ppm, CompoundQuery dbQuery) {
		this.mass = mass;
		this.ppm = ppm;
		this.count = 1;
		this.dbQuery = dbQuery;
	}

	public double getMass() {
		return mass;
	}
	
	public boolean withinTolerance(double toCheck) {
		double delta = PPM(mass, ppm);
		double upper = mass + delta; 
		double lower = mass - delta;
		if (lower < toCheck && toCheck < upper) {
			return true;
		} else {
			return false;
		}				
	}
		
	public int getCount() {
		return count;
	}
	
	public void incrementCount() {
		count++;
	}
	
	/**
	 * Returns the molecules from database that matches this precursor's mass within tolerance
	 * @return Molecules within tolerance from the DB
	 */
	public Set<Molecule> initMolecules() {
		// if no molecules yet, then search DB for the first time
		if (molecules == null) {
			try {
				molecules = dbQuery.findCompoundsByMass(mass, ppm*3, 0);
			} catch (Exception e) {
				molecules = new HashSet<Molecule>();
			}
		}
		return molecules;
	}
		
	@Override
	public String toString() {
		String msg = "HDPPrecursorMass [mass=" + mass + ", freq=" + count + ", databaseMolecules=" + molecules.size() + " { ";
		for (Molecule mol : molecules) {
			msg += mol.getName() + ", ";
		}
		msg += " }";
		return msg;
	}

	private double PPM(double mass, double q) {
		return q * (0.000001*mass);
	}

	public int compareTo(HDPPrecursorMass o) {
		return -Integer.compare(this.getCount(), o.getCount());
	}
	
}
