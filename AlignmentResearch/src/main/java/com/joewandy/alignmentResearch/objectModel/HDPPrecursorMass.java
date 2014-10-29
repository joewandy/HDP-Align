package com.joewandy.alignmentResearch.objectModel;

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
		// 3 times the window to match the gaussian distribution used in the model
		double delta = PPM(mass, ppm*3);
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
	
	public Set<Molecule> loadMoleculesFromDB() {
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
